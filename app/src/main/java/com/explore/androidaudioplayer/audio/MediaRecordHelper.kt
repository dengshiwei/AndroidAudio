package com.explore.androidaudioplayer.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderHelper(
    private val context: Context,
    private val outputDir: File, // 存储录音文件的目录
    private val onError: ((Exception) -> Unit)? = null
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    var isRecording = false
        private set

    /**
     * 开始录音
     * @param filePath 可选，指定保存路径；为空则自动生成
     */
    fun startRecording(filePath: String? = null) {
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // 自动生成文件名
            val file = if (filePath.isNullOrBlank()) {
                File(outputDir, generateFileName())
            } else {
                File(filePath)
            }
            currentFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            Log.d("AudioRecorderHelper", "Start recording: ${file.absolutePath}")
        } catch (e: Exception) {
            onError?.invoke(e)
            Log.e("AudioRecorderHelper", "Start recording failed", e)
            releaseRecorder()
        }
    }

    /**
     * 停止录音并返回录音文件
     */
    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            Log.d("AudioRecorderHelper", "Stop recording: ${currentFile?.absolutePath}")
        } catch (e: Exception) {
            onError?.invoke(e)
            Log.e("AudioRecorderHelper", "Stop recording failed", e)
        } finally {
            releaseRecorder()
        }
        return currentFile
    }

    /** 获取音量峰值，0~32767 */
    fun getMaxAmplitude(): Int {
        return mediaRecorder?.maxAmplitude ?: 0
    }

    private fun releaseRecorder() {
        mediaRecorder = null
        isRecording = false
    }

    private fun generateFileName(): String {
        val sdf = SimpleDateFormat("MMdd_HHmmss", Locale.getDefault())
        return "${sdf.format(Date())}"
    }
}