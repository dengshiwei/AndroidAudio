package com.explore.androidaudioplayer.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.explore.androidaudioplayer.model.RecordingItem
import com.explore.androidaudioplayer.utils.TimeUtils
import java.io.File

object RecordUtils {
    private var AUDIO_ROOT_PATH = ""

    // 外部回调（可以有多个监听者）
    private val listeners = mutableListOf<OnAudioPlayListener>()
    private lateinit var mediaRecorder: AudioRecorderHelper
    private val playerHelper = MediaPlayerHelper(
        onCompletion = {
            // 通知所有监听者
            listeners.forEach { it.onPlayCompleted() }
        }
    )

    fun initRecorder(context: Context) {
        AUDIO_ROOT_PATH = context.cacheDir.absolutePath
        mediaRecorder = AudioRecorderHelper(context, File(AUDIO_ROOT_PATH))
    }

    fun startRecording() {
        mediaRecorder.startRecording()
    }

    fun stopRecording(): File? {
        return mediaRecorder.stopRecording()
    }

    fun playRecording(file: File) {
        playerHelper.play(file)
    }

    fun stopPlaying() {
        playerHelper.stop()
    }

    /** 获取音量峰值，0~32767 */
    fun getMaxAmplitude(): Int {
        return mediaRecorder.getMaxAmplitude()
    }

    /**
     * 删除录音文件
     * @param file 录音文件
     */
    fun removeRecording(filePath: String) {
        val file = File(filePath)
        file.delete()
    }

    fun getPlayProcess(): Int {
        val currentPos = playerHelper.getProgress()
        val duration = playerHelper.getDuration()
        if (duration > 0) {
            val progress = (currentPos * 100) / duration
            return progress
        }
        return 0
    }

    // 获取所有录音文件
    fun loadAllRecordings(context: Context): List<RecordingItem> {
        val dir = File(context.cacheDir.absolutePath)
        val files = dir.listFiles { file ->
            file.extension == "mp3" || file.extension == "wav" || file.extension == "m4a"
        } ?: return emptyList()

        return files.map { file ->
            RecordingItem(
                fileName = file.name,
                filePath = file.absolutePath,
                duration = getAudioDuration(file.absolutePath),
                recordTime = TimeUtils.formatTime(file.lastModified())
            )
        }.sortedByDescending { it.recordTime } // 按时间倒序排列
    }

    // 获取音频时长（毫秒）
    private fun getAudioDuration(path: String): Long {
        return try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(path)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            mmr.release()
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun addOnAudioPlayListener(listener: OnAudioPlayListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnAudioPlayListener(listener: OnAudioPlayListener) {
        listeners.remove(listener)
    }

    interface OnAudioPlayListener {
        fun onPlayCompleted()
    }
}
