package com.kisanseva.ai.system.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var fileOutputStream: FileOutputStream? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    override fun start(outputFile: File) {
        outputFile.parentFile?.mkdirs()

        val fos = FileOutputStream(outputFile)
        fileOutputStream = fos

        val newRecorder = createRecorder()
        try {
            with(newRecorder) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(fos.fd)

                prepare()
                start()
            }
            recorder = newRecorder
            audioFile = outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                newRecorder.release()
            } catch (releaseEx: Exception) {
                releaseEx.printStackTrace()
            }
            try {
                fos.close()
            } catch (closeEx: Exception) {
                closeEx.printStackTrace()
            }
            fileOutputStream = null
            recorder = null
            audioFile = null
            throw e
        }
    }

    override fun stop() {
        recorder?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder = null

        try {
            fileOutputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fileOutputStream = null
    }

    override fun cancel() {
        stop()
        audioFile?.delete()
        audioFile = null
    }

    override fun getMaxAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
}