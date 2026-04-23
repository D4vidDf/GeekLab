package com.daviddf.geeklab.ui.screens.notification.call

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

object AudioLoopSource {
    private const val SAMPLE_RATE = 44100
    private const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
    private const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
    private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT)

    @SuppressLint("MissingPermission")
    suspend fun openAudioLoop() = withContext(Dispatchers.IO) {
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG_IN,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return@withContext
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG_OUT)
                    .build()
            )
            .setBufferSizeInBytes(BUFFER_SIZE)
            .build()

        val buffer = ByteArray(BUFFER_SIZE)
        recorder.startRecording()
        track.play()

        try {
            while (coroutineContext.isActive) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    track.write(buffer, 0, read)
                }
            }
        } finally {
            recorder.stop()
            recorder.release()
            track.stop()
            track.release()
        }
    }
}
