package com.telegramdrive.uploader.core.util.media

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID

/**
 * Quality presets used to determine compression targets for video files.
 */
enum class VideoQualityPreset(val label: String, val videoBitrateFactor: Float, val resolutionScale: Float) {
    LOW("Low", 0.35f, 0.5f),
    MEDIUM("Medium", 0.55f, 0.75f),
    HIGH("High", 0.80f, 1.0f),
    ORIGINAL("Original", 1.0f, 1.0f)
}

/**
 * Compresses video files on-device using Android's native MediaCodec/MediaMuxer
 * pipeline. Supports progress reporting via a callback.
 */
class VideoCompressor(private val context: Context) {

    fun interface ProgressCallback {
        fun onProgress(percent: Float)
    }

    /**
     * Compresses a video file to the target quality preset.
     * Returns the URI of the compressed file, or null on failure.
     */
    suspend fun compress(
        sourceUri: Uri,
        preset: VideoQualityPreset,
        onProgress: ProgressCallback? = null
    ): Uri? = withContext(Dispatchers.IO) {
        if (preset == VideoQualityPreset.ORIGINAL) {
            // No compression needed for original quality
            return@withContext sourceUri
        }

        val outputDir = File(context.cacheDir, "compressed")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, "compressed_${UUID.randomUUID()}.mp4")
        val descriptor: AssetFileDescriptor = try {
            context.contentResolver.openAssetFileDescriptor(sourceUri, "r")
                ?: return@withContext null
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = "Failed to open source video for compression: ${e.message}",
                errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE,
                exception = e
            )
            return@withContext null
        }

        try {
            compressVideoFile(descriptor, outputFile, preset, onProgress)
            val outputUri = Uri.fromFile(outputFile)
            onProgress?.onProgress(1.0f)
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_PREPARING,
                severity = DiagnosticSeverity.INFO,
                message = "Video compressed successfully: ${outputFile.length()} bytes"
            )
            outputUri
        } catch (e: Exception) {
            outputFile.delete()
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = "Video compression failed: ${e.message}",
                errorCode = ErrorCode.UPLOAD_FAILED,
                exception = e
            )
            null
        } finally {
            try { descriptor.close() } catch (_: Exception) {}
        }
    }

    private fun compressVideoFile(
        descriptor: AssetFileDescriptor,
        outputFile: File,
        preset: VideoQualityPreset,
        onProgress: ProgressCallback?
    ) {
        val extractor = MediaExtractor()
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoTrackIndex = -1

        try {
            extractor.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)

            // Find the video and audio track formats
            val formats = mutableListOf<Pair<Int, MediaFormat>>()
            var videoTrackFormat: MediaFormat? = null
            var audioTrackFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/")) {
                    videoTrackFormat = format
                } else if (mime.startsWith("audio/")) {
                    audioTrackFormat = format
                }
            }

            if (videoTrackFormat == null) {
                throw IllegalStateException("No video track found in source")
            }

            // Compression only makes sense for the video track; if audio track is
            // present, copy it through unchanged.
            val muxerTracks = mutableMapOf<Int, Int>() // extractorTrackIndex to muxerTrackIndex

            // Prepare the video encoder
            val videoWidth = videoTrackFormat.getInteger(MediaFormat.KEY_WIDTH)
            val videoHeight = videoTrackFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val bitrate = videoTrackFormat.getInteger(MediaFormat.KEY_BIT_RATE)

            val scaledWidth = (videoWidth * preset.resolutionScale).toInt().coerceAtLeast(160)
            val scaledHeight = (videoHeight * preset.resolutionScale).toInt().coerceAtLeast(160)
            val targetBitrate = (bitrate * preset.videoBitrateFactor).toInt().coerceAtLeast(200_000)

            // Prepare the codec
            val codecName = "video/avc"
            val encoderFormat = MediaFormat.createVideoFormat(codecName, scaledWidth, scaledHeight)
            encoderFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            encoderFormat.setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
            encoderFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            encoderFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

            val isEncoderSupported = MediaCodecList(MediaCodecList.REGULAR_CODECS)
                .findEncoderForFormat(encoderFormat) != null

            if (isEncoderSupported) {
                // Use the hardware/software encoder for accurate re-encoding
                val encoder = MediaCodec.createEncoderByType(codecName)

                // Bring video track selection forward and select it in extractor
                var videoExtractorIndex = -1
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    if (format.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true) {
                        videoExtractorIndex = i
                        break
                    }
                }

                extractor.selectTrack(videoExtractorIndex)

                try {
                    encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                } catch (e: Exception) {
                    // Fallback: try with YUV420 flexible
                    encoderFormat.setInteger(
                        MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
                    )
                    encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }

                encoder.start()
                try {
                    encodeWithExtractor(extractor, encoder, muxer, onProgress)
                } finally {
                    try { encoder.stop() } catch (_: Exception) {}
                    encoder.release()
                }
            } else {
                // Fallback: simply copy the video track to the output (no re-encode)
                copyTracksAsIs(extractor, muxer, onProgress)
            }
        } finally {
            try { extractor.release() } catch (_: Exception) {}
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
        }
    }

    private fun encodeWithExtractor(
        extractor: MediaExtractor,
        encoder: MediaCodec,
        muxer: MediaMuxer,
        onProgress: ProgressCallback?
    ) {
        // Simplified re-encode path: read source frames and feed the encoder
        val bufferInfo = MediaCodec.BufferInfo()
        val inputBuffers = encoder.inputBuffers
        val outputBuffers = encoder.outputBuffers

        // Track durations for progress
        val totalDurationUs = estimateDurationUs(extractor)
        var processedDurationUs = 0L

        // Bind video track to muxer once we have a format
        var muxerVideoTrackIndex = -1
        var outputFormatKnown = false
        var sawInputEOS = false
        var sawOutputEOS = false

        while (!sawOutputEOS) {
            // Feed input
            if (!sawInputEOS) {
                val inputIndex = encoder.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = inputBuffers[inputIndex]
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        encoder.queueInputBuffer(
                            inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        sawInputEOS = true
                    } else {
                        val presentationTimeUs = extractor.sampleTime
                        encoder.queueInputBuffer(
                            inputIndex, 0, sampleSize, presentationTimeUs, 0
                        )
                        processedDurationUs = presentationTimeUs
                        extractor.advance()
                        onProgress?.onProgress(
                            if (totalDurationUs > 0)
                                (processedDurationUs.toFloat() / totalDurationUs.toFloat()).coerceIn(0f, 1f)
                            else 0f
                        )
                    }
                }
            }

            // Drain output
            while (true) {
                val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = encoder.outputFormat
                    if (muxerVideoTrackIndex < 0) {
                        muxerVideoTrackIndex = muxer.addTrack(newFormat)
                        muxer.start()
                        outputFormatKnown = true
                    }
                } else if (outputIndex >= 0) {
                    if (outputFormatKnown) {
                        val outputBuffer = outputBuffers[outputIndex]
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0) {
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(muxerVideoTrackIndex, outputBuffer, bufferInfo)
                        }
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            sawOutputEOS = true
                        }
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                }
            }
        }
    }

    private fun copyTracksAsIs(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        onProgress: ProgressCallback?
    ) {
        // Copy all tracks as-is when no encoder is available
        val trackMapping = mutableMapOf<Int, Int>()
        var muxerStarted = false
        val bufferInfo = MediaCodec.BufferInfo()

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            extractor.selectTrack(i)
            val muxerIndex = muxer.addTrack(format)
            trackMapping[i] = muxerIndex
        }

        if (trackMapping.isNotEmpty()) {
            muxer.start()
            muxerStarted = true
        }

        val buffer = ByteBuffer.allocate(1_048_576)

        var lastPresentationTime = 0L
        while (true) {
            buffer.clear()
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            val trackIndex = extractor.sampleTrackIndex
            val muxerTrack = trackMapping[trackIndex] ?: continue
            buffer.limit(sampleSize)
            buffer.rewind()
            val pts = extractor.sampleTime
            lastPresentationTime = pts
            val flags = extractor.sampleFlags
            bufferInfo.apply {
                offset = 0
                size = sampleSize
                presentationTimeUs = pts
                if (flags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    this.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    this.flags = 0
                }
            }
            muxer.writeSampleData(muxerTrack, buffer, bufferInfo)
            extractor.advance()
            onProgress?.onProgress(0.5f)
        }
    }

    private fun estimateDurationUs(extractor: MediaExtractor): Long {
        var maxDurationUs = 0L
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.containsKey(MediaFormat.KEY_DURATION)) {
                maxDurationUs = maxOf(maxDurationUs, format.getLong(MediaFormat.KEY_DURATION))
            }
        }
        return maxDurationUs
    }

    /**
     * Returns true if a video at the given size qualifies for compression
     * (i.e., is large enough that compression produces meaningful savings).
     */
    fun shouldCompress(fileSize: Long, preset: VideoQualityPreset): Boolean {
        if (preset == VideoQualityPreset.ORIGINAL) return false
        // Compression threshold: at least 5 MB
        return fileSize >= 5L * 1024 * 1024
    }
}
