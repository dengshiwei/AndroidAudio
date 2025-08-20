package com.explore.androidaudioplayer.audio

import android.media.MediaPlayer
import android.util.Log
import java.io.File

class MediaPlayerHelper(
    private val onCompletion: (() -> Unit)? = null,
    private val onError: ((Exception) -> Unit)? = null
) {
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying = false
        private set

    fun play(file: File) {
        stop() // 如果正在播放则先停止
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    this@MediaPlayerHelper.isPlaying = false
                    onCompletion?.invoke()
                }
            }
            isPlaying = true
            Log.d("AudioPlayerHelper", "Playing: ${file.name}")
        } catch (e: Exception) {
            onError?.invoke(e)
            Log.e("AudioPlayerHelper", "Play failed", e)
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }

    fun getProgress(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
}