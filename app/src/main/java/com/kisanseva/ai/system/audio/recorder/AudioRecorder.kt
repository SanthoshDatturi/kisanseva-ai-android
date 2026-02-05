package com.kisanseva.ai.system.audio.recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    fun cancel()
    fun getMaxAmplitude(): Int
}
