package com.explore.androidaudioplayer.model

data class RecordingItem(
    val fileName: String,
    val filePath: String,
    val recordTime: String, // 录制日期
    val duration: Long,
    var isPlaying: Boolean = false // 默认未播放
)
