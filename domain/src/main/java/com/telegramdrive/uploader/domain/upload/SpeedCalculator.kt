package com.telegramdrive.uploader.domain.upload

import java.util.concurrent.TimeUnit

class SpeedCalculator {
    private var lastBytes: Long = 0
    private var lastTime: Long = 0
    private val windowSize = 5 // Window size for moving average
    private val measurements = mutableListOf<Measurement>()

    private data class Measurement(val bytes: Long, val time: Long)

    fun update(currentBytes: Long): SpeedInfo {
        val currentTime = System.currentTimeMillis()
        
        if (lastTime == 0L) {
            lastBytes = currentBytes
            lastTime = currentTime
            return SpeedInfo(0, 0)
        }

        val bytesDiff = currentBytes - lastBytes
        val timeDiff = currentTime - lastTime

        if (timeDiff > 0) {
            measurements.add(Measurement(bytesDiff, timeDiff))
            if (measurements.size > windowSize) {
                measurements.removeAt(0)
            }
        }

        lastBytes = currentBytes
        lastTime = currentTime

        val totalBytes = measurements.sumOf { it.bytes }
        val totalTime = measurements.sumOf { it.time }

        val speed = if (totalTime > 0) {
            (totalBytes * 1000) / totalTime
        } else {
            0L
        }

        return SpeedInfo(speed, speed) // For now, simple average in window
    }

    fun reset() {
        lastBytes = 0
        lastTime = 0
        measurements.clear()
    }

    data class SpeedInfo(val currentSpeed: Long, val averageSpeed: Long)
}
