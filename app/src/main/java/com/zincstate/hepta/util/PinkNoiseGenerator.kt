package com.zincstate.hepta.util

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.random.Random

class PinkNoiseGenerator {
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var thread: Thread? = null

    fun toggle() {
        if (isPlaying) {
            stop()
        } else {
            play()
        }
    }

    fun isPlaying() = isPlaying

    fun play() {
        if (isPlaying) return
        isPlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioTrack?.play()

        thread = Thread {
            val buffer = ShortArray(bufferSize)
            
            // Brown noise state
            var lastOut = 0.0

            while (isPlaying) {
                for (i in buffer.indices) {
                    val white = (Random.nextDouble() * 2.0) - 1.0
                    
                    // Simple low-pass filter (leaky integrator) for Brown Noise
                    // This creates a deep, warm, ocean-like sound perfect for meditation
                    lastOut = (0.98 * lastOut) + (0.02 * white)
                    
                    // Boost volume slightly since low-pass reduces energy, then coerce to prevent clipping
                    val sample = (lastOut * 3.5 * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
        thread?.start()
    }

    fun stop() {
        isPlaying = false
        thread?.join(500)
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
