package com.explore.androidaudioplayer.utils

import android.app.AlertDialog
import android.content.Context

object DialogUtils {

    /**
     * 显示确认对话框
     * @param context 上下文
     * @param message 提示语
     * @param onConfirm 点击确认的回调
     * @param onCancel 点击取消的回调（可选）
     */
    fun showConfirmDialog(
        context: Context,
        message: String,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                onCancel?.invoke()
                dialog.dismiss()
            }
            .create()
            .show()
    }
}