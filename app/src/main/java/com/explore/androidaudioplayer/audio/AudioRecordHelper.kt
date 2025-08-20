package com.explore.androidaudioplayer.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * val audioRecordHelper = AudioRecordHelper(
 *     outputDir = File(getExternalFilesDir(null), "pcm_records")
 * )
 *
 * // 开始录音
 * val file = audioRecordHelper.startRecording()
 *
 * // 停止录音
 * val recordedFile = audioRecordHelper.stopRecording()
 * println("录音文件保存在: ${recordedFile?.absolutePath}")
 */
class AudioRecordHelper(
    private val outputDir: File,
    private val sampleRate: Int = 44100,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {

    private var audioRecord: AudioRecord? = null
    private var bufferSize = 0
    private var recordingThread: Thread? = null
    private val isRecording = AtomicBoolean(false)
    private var outputFile: File? = null

    init {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = sampleRate * 2
        }
    }

    /**
     * 开始录音
     * @return 生成的文件
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(fileName: String = "record_${System.currentTimeMillis()}.pcm"): File {
        outputFile = File(outputDir, fileName)
        if (!outputDir.exists()) outputDir.mkdirs()

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording.set(true)

        // 启动写文件线程
        recordingThread = Thread({
            writeAudioDataToFile(outputFile!!)
        }, "AudioRecord Thread").apply { start() }

        return outputFile!!
    }

    /**
     * 停止录音
     */
    fun stopRecording(): File? {
        isRecording.set(false)
        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
        return outputFile
    }

    /**
     * 写入 PCM 数据到文件
     */
    private fun writeAudioDataToFile(file: File) {
        val data = ByteArray(bufferSize)
        FileOutputStream(file).use { fos ->
            while (isRecording.get()) {
                val read = audioRecord?.read(data, 0, data.size) ?: 0
                if (read > 0 && read != AudioRecord.ERROR_INVALID_OPERATION) {
                    try {
                        fos.write(data, 0, read)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}