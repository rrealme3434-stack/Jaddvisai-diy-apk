package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object JarvisSoundFx {
  private val scope = CoroutineScope(Dispatchers.Default)
  var soundEnabled: Boolean = true

  private fun playPcmTones(tones: List<Pair<Double, Int>>, sampleRate: Int = 44100) {
    if (!soundEnabled) return
    scope.launch {
      try {
        var totalSamples = 0
        for ((_, durationMs) in tones) {
          totalSamples += (sampleRate * durationMs / 1000)
        }
        val buffer = ShortArray(totalSamples)
        var bufferIdx = 0

        for ((freq, durationMs) in tones) {
          val samples = sampleRate * durationMs / 1000
          for (i in 0 until samples) {
            // Apply attack/decay envelope to prevent clipping clicks
            val envelope = when {
              i < samples * 0.15f -> (i / (samples * 0.15f))
              i > samples * 0.85f -> ((samples - i) / (samples * 0.15f))
              else -> 1.0f
            }
            val angle = 2.0 * Math.PI * i / (sampleRate / freq)
            val sample = (sin(angle) * 16000.0 * envelope).toInt()
            if (bufferIdx < buffer.size) {
              buffer[bufferIdx++] = sample.toShort()
            }
          }
        }

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(buffer.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        // Wait duration then release
        kotlinx.coroutines.delay((totalSamples * 1000L / sampleRate) + 50)
        track.stop()
        track.release()
      } catch (_: Exception) {
        // Fallback gracefully if audio hardware is occupied
      }
    }
  }

  fun playClick() {
    playPcmTones(listOf(1400.0 to 18))
  }

  fun playCommandAcknowledged() {
    playPcmTones(
      listOf(
        880.0 to 50,
        1174.0 to 90
      )
    )
  }

  fun playSystemOnline() {
    playPcmTones(
      listOf(
        440.0 to 70,
        554.0 to 70,
        659.0 to 90,
        880.0 to 140,
        1320.0 to 220
      )
    )
  }

  fun playReactorCharge() {
    val sweepTones = mutableListOf<Pair<Double, Int>>()
    var freq = 240.0
    while (freq <= 1100.0) {
      sweepTones.add(freq to 15)
      freq += 60.0
    }
    sweepTones.add(1100.0 to 100)
    playPcmTones(sweepTones)
  }

  fun playAlert() {
    playPcmTones(
      listOf(
        880.0 to 90,
        0.0 to 30,
        880.0 to 130
      )
    )
  }
}
