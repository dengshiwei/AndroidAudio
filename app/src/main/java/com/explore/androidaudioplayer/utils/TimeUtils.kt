package com.explore.androidaudioplayer.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = sdf.format(Date(timestamp))
        return formattedDate
    }

    // 添加毫秒转秒级（保留一位小数）的格式化函数
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds.toDouble() / 1000.0
        return String.format("%.1f", seconds) // 保留一位小数
    }
}