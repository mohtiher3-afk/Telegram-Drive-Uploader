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
import android.view.Surface
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
            // Guard against a silent no-op compression that either produced nothing or
            // failed to write the muxer output; otherwise we would hand the uploader a
            // broken/empty file disguised as a successful compression.
            if (!outputFile.exists() || outputFile.length() <= 0L) {
                throw IllegalStateException("Compression produced an empty or missing output file")
            }
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
        var audioExtractor: MediaExtractor? = null

        try {
            extractor.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)

            // Find the video and audio track formats
            var videoTrackFormat: MediaFormat? = null
            var videoExtractorIndex = -1
            var audioTrackFormat: MediaFormat? = null
            var audioExtractorIndex = -1

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("video/") && videoTrackFormat == null) {
                    videoTrackFormat = format
                    videoExtractorIndex = i
                } else if (mime.startsWith("audio/") && audioTrackFormat == null) {
                    audioTrackFormat = format
                    audioExtractorIndex = i
                }
            }

            if (videoTrackFormat == null) {
                throw IllegalStateException("No video track found in source")
            }

            // The display rotation must be applied to the muxer before start() in
            // every path (re-encode and passthrough): neither the surface pipeline
            // nor a sample copy bakes rotation into the pixels, so it has to ride
            // on the output track metadata.
            val sourceRotation = if (videoTrackFormat.containsKey(MediaFormat.KEY_ROTATION)) {
                videoTrackFormat.getInteger(MediaFormat.KEY_ROTATION)
            } else {
                0
            }
            muxer.setOrientationHint(sourceRotation)

            // Compression targets per the selected preset.
            val videoWidth = videoTrackFormat.getInteger(MediaFormat.KEY_WIDTH)
            val videoHeight = videoTrackFormat.getInteger(MediaFormat.KEY_HEIGHT)
            // Not every container exposes KEY_BIT_RATE on the track format.
            val sourceBitrate = if (videoTrackFormat.containsKey(MediaFormat.KEY_BIT_RATE)) {
                videoTrackFormat.getInteger(MediaFormat.KEY_BIT_RATE)
            } else {
                DEFAULT_SOURCE_BITRATE
            }

            val scaledWidth = scaledDimension((videoWidth * preset.resolutionScale).toInt())
            val scaledHeight = scaledDimension((videoHeight * preset.resolutionScale).toInt())
            val targetBitrate = (sourceBitrate * preset.videoBitrateFactor).toInt().coerceAtLeast(MIN_TARGET_BITRATE)

            // Prepare the codec
            val encoderFormat = MediaFormat.createVideoFormat(AVC_MIME, scaledWidth, scaledHeight)
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
                // Full re-encode: MediaExtractor -> DECODER -> (encoder input Surface)
                // -> ENCODER -> MediaMuxer. Source samples are already compressed
                // H.264/HEVC bitstream bytes and can never be fed to an encoder
                // directly; they must be decoded and rendered into the encoder's
                // input surface first.
                val videoMime = videoTrackFormat.getString(MediaFormat.KEY_MIME)!!

                if (audioTrackFormat != null && audioExtractorIndex >= 0) {
                    // Second extractor dedicated to the audio track so video decoding
                    // and audio passthrough can be interleaved without fighting over
                    // a single track selection.
                    audioExtractor = MediaExtractor().apply {
                        setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                        selectTrack(audioExtractorIndex)
                    }
                }

                extractor.selectTrack(videoExtractorIndex)

                val encoder = MediaCodec.createEncoderByType(AVC_MIME)
                var inputSurface: Surface? = null
                var decoder: MediaCodec? = null
                try {
                    try {
                        encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    } catch (e: Exception) {
                        throw IllegalStateException(
                            "H.264 encoder rejected target format ${scaledWidth}x$scaledHeight " +
                                "@$targetBitrate bps with COLOR_FormatSurface: ${e.message}",
                            e
                        )
                    }
                    // createInputSurface() must be called after configure() and before start().
                    inputSurface = encoder.createInputSurface()

                    decoder = try {
                        MediaCodec.createDecoderByType(videoMime)
                    } catch (e: Exception) {
                        throw IllegalStateException(
                            "No MediaCodec decoder available for '$videoMime': ${e.message}",
                            e
                        )
                    }
                    try {
                        decoder.configure(videoTrackFormat, inputSurface, null, 0)
                    } catch (e: Exception) {
                        throw IllegalStateException(
                            "Decoder '$videoMime' rejected the source video format: ${e.message}",
                            e
                        )
                    }

                    decoder.start()
                    encoder.start()
                    reencodeVideoToMuxer(
                        extractor,
                        audioExtractor,
                        audioTrackFormat,
                        decoder,
                        encoder,
                        muxer,
                        onProgress
                    )
                } finally {
                    try { decoder?.stop() } catch (_: Exception) {}
                    try { decoder?.release() } catch (_: Exception) {}
                    try { encoder.stop() } catch (_: Exception) {}
                    try { encoder.release() } catch (_: Exception) {}
                    try { inputSurface?.release() } catch (_: Exception) {}
                }
            } else {
                // Fallback: simply copy the tracks as-is (no re-encode) when no
                // suitable encoder exists on the device.
                copyTracksAsIs(extractor, muxer, onProgress)
            }
        } finally {
            try { audioExtractor?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
            try { muxer.stop() } catch (_: Exception) {}
            muxer.release()
        }
    }

    private fun reencodeVideoToMuxer(
        videoExtractor: MediaExtractor,
        audioExtractor: MediaExtractor?,
        audioFormat: MediaFormat?,
        decoder: MediaCodec,
        encoder: MediaCodec,
        muxer: MediaMuxer,
        onProgress: ProgressCallback?
    ) {
        val decoderInfo = MediaCodec.BufferInfo()
        val encoderInfo = MediaCodec.BufferInfo()
        val audioInfo = MediaCodec.BufferInfo()

        // Track durations for progress
        val totalDurationUs = estimateDurationUs(videoExtractor)
        var processedDurationUs = 0L

        // Bind tracks to the muxer once the encoder reports its output format; the
        // audio track is added at the same time so it can be copied through the
        // muxer as compressed passthrough samples.
        var muxerVideoTrackIndex = -1
        var muxerAudioTrackIndex = -1

        var decoderInputDone = false
        var decoderOutputDone = false
        var encoderOutputDone = false

        val audioCopyBuffer = if (audioExtractor != null && audioFormat != null) {
            val audioMaxInputSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                0
            }
            ByteBuffer.allocate(computeCopyBufferSizeBytes(listOf(audioMaxInputSize)))
        } else {
            null
        }
        var audioEof = false

        var encoderIdleSpins = 0

        while (!encoderOutputDone) {
            // 1. Feed compressed source samples into the decoder.
            if (!decoderInputDone) {
                val inputIndex = decoder.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        val sampleSize = videoExtractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            decoderInputDone = true
                        } else {
                            decoder.queueInputBuffer(
                                inputIndex, 0, sampleSize, videoExtractor.sampleTime, 0
                            )
                            videoExtractor.advance()
                            onProgress?.onProgress(
                                if (totalDurationUs > 0) {
                                    (videoExtractor.sampleTime.toFloat() / totalDurationUs.toFloat())
                                        .coerceIn(0f, 1f)
                                } else 0f
                            )
                        }
                    }
                }
            }

            // 2. Render decoded frames straight into the encoder's input surface;
            // the render call carries the decoder PTS over to the encoder.
            if (!decoderOutputDone) {
                val outputIndex = decoder.dequeueOutputBuffer(decoderInfo, 0)
                when {
                    outputIndex >= 0 -> {
                        decoder.releaseOutputBuffer(outputIndex, true)
                        if (decoderInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            decoderOutputDone = true
                        }
                    }
                    // INFO_TRY_AGAIN_LATER / INFO_OUTPUT_FORMAT_CHANGED /
                    // INFO_OUTPUT_BUFFERS_CHANGED: nothing to do on this path.
                    else -> Unit
                }
            }

            // 3. Drain the encoder into the muxer.
            var encoderTryAgain = false
            while (true) {
                val outputIndex = encoder.dequeueOutputBuffer(
                    encoderInfo, if (decoderOutputDone) 10_000L else 0L
                )
                if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    encoderTryAgain = true
                    break
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (muxerVideoTrackIndex < 0) {
                        muxerVideoTrackIndex = muxer.addTrack(encoder.outputFormat)
                        if (audioExtractor != null && audioFormat != null) {
                            muxerAudioTrackIndex = muxer.addTrack(audioFormat)
                        }
                        muxer.start()
                    }
                } else if (outputIndex >= 0) {
                    val isCodecConfig =
                        encoderInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                    if (muxerVideoTrackIndex < 0) {
                        // Buffers may only arrive after the output format is reported.
                        if (!isCodecConfig || encoderInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            throw IllegalStateException(
                                "H.264 encoder produced encoded data or EOS before reporting an output format"
                            )
                        }
                    } else {
                        if (isCodecConfig) {
                            encoderInfo.size = 0
                        }
                        if (encoderInfo.size > 0) {
                            val outputBuffer = encoder.getOutputBuffer(outputIndex)
                            if (outputBuffer != null) {
                                outputBuffer.position(encoderInfo.offset)
                                outputBuffer.limit(encoderInfo.offset + encoderInfo.size)
                                muxer.writeSampleData(muxerVideoTrackIndex, outputBuffer, encoderInfo)
                                if (encoderInfo.presentationTimeUs > processedDurationUs) {
                                    processedDurationUs = encoderInfo.presentationTimeUs
                                }
                            }
                        }
                        if (encoderInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            encoderOutputDone = true
                        }
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                    if (encoderOutputDone) break
                }
                // INFO_OUTPUT_BUFFERS_CHANGED: nothing to do with the modern buffer API.
            }

            // 4. Copy compressed audio samples through the muxer (passthrough),
            // paced slightly ahead of the encoded video timestamps.
            if (audioCopyBuffer != null && audioExtractor != null &&
                muxerAudioTrackIndex >= 0 && !audioEof
            ) {
                audioEof = copyAudioSamplesUpTo(
                    audioExtractor,
                    muxerAudioTrackIndex,
                    audioCopyBuffer,
                    audioInfo,
                    muxer,
                    maxSampleCount = 64,
                    maxPtsUs = processedDurationUs + AUDIO_PACING_WINDOW_US
                )
            }

            // Avoid a busy spin while the encoder drains after decoder EOS, with a
            // bounded stall guard so a wedged codec fails loudly instead of hanging.
            if (decoderOutputDone && encoderTryAgain && !encoderOutputDone) {
                if (++encoderIdleSpins > MAX_ENCODER_IDLE_SPINS) {
                    throw IllegalStateException(
                        "H.264 encoder stalled after decoder EOS; aborting compression"
                    )
                }
                Thread.sleep(10)
            } else {
                encoderIdleSpins = 0
            }
        }
    }

    private fun copyAudioSamplesUpTo(
        audioExtractor: MediaExtractor,
        muxerAudioTrackIndex: Int,
        buffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
        muxer: MediaMuxer,
        maxSampleCount: Int,
        maxPtsUs: Long
    ): Boolean {
        var copied = 0
        while (copied < maxSampleCount) {
            val samplePts = audioExtractor.sampleTime
            if (samplePts > maxPtsUs) return false
            buffer.clear()
            val sampleSize = audioExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) return true
            buffer.position(0)
            buffer.limit(sampleSize)
            bufferInfo.apply {
                offset = 0
                size = sampleSize
                presentationTimeUs = samplePts
                flags = if (audioExtractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    0
                }
            }
            muxer.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
            audioExtractor.advance()
            copied++
        }
        return false
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
        val trackMaxInputSizes = mutableListOf<Int>()

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            extractor.selectTrack(i)
            val muxerIndex = muxer.addTrack(format)
            trackMapping[i] = muxerIndex
            if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                trackMaxInputSizes.add(format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
            }
        }

        if (trackMapping.isNotEmpty()) {
            muxer.start()
            muxerStarted = true
        }

        // Size the copy buffer from KEY_MAX_INPUT_SIZE so oversized samples are not
        // silently truncated (readSampleData returns -1 when the buffer is too small).
        val buffer = ByteBuffer.allocate(computeCopyBufferSizeBytes(trackMaxInputSizes))

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

    companion object {
        private const val AVC_MIME = "video/avc"
        private const val DEFAULT_SOURCE_BITRATE = 5_000_000
        private const val MIN_TARGET_BITRATE = 200_000

        // Audio passthrough is paced at most half a second ahead of the encoded
        // video timestamps so the muxer interleaves without buffering whole tracks.
        private const val AUDIO_PACING_WINDOW_US = 500_000L

        // ~50s grace at 10ms per spin before declaring the encoder wedged.
        private const val MAX_ENCODER_IDLE_SPINS = 5_000

        internal const val DEFAULT_COPY_BUFFER_BYTES = 1_048_576

        /**
         * Smallest even dimension accepted by H.264 encoders after preset scaling.
         * Odd dimensions make encoder configure() fail outright.
         */
        internal fun scaledDimension(value: Int): Int {
            val scaled = value.coerceAtLeast(160)
            return scaled - (scaled % 2)
        }

        /**
         * Copy-buffer size large enough for the biggest sample of any track;
         * never smaller than the historical 1 MiB default.
         */
        internal fun computeCopyBufferSizeBytes(maxInputSizes: Collection<Int>): Int =
            (maxInputSizes.maxOrNull() ?: 0).coerceAtLeast(DEFAULT_COPY_BUFFER_BYTES)
    }
}
