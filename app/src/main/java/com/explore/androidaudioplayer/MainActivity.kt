package com.explore.androidaudioplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.explore.androidaudioplayer.adapter.AudioListAdapter
import com.explore.androidaudioplayer.audio.RecordUtils
import com.explore.androidaudioplayer.model.RecordingItem
import com.explore.androidaudioplayer.utils.DialogUtils
import com.explore.androidaudioplayer.utils.PermissionUtils
import com.explore.androidaudioplayer.utils.TimeUtils
import com.explore.androidaudioplayer.views.WaveMicButton
import com.google.android.material.button.MaterialButton
import java.io.File

class MainActivity: Activity() {
    private lateinit var audioListAdapter: AudioListAdapter
    private lateinit var  recordingRecyclerView: RecyclerView
    private lateinit var btnRecord: MaterialButton
    private lateinit var tvState: TextView
    private val recordings = mutableListOf<RecordingItem>()
    private lateinit var waveMicButton: WaveMicButton
    private var pressTime: Long = 0
    private var amplitudeHandler: Handler = Handler(Looper.getMainLooper())
    private val amplitudeRunnable = object : Runnable {
        override fun run() {
            waveMicButton.updateAmplitude(RecordUtils.getMaxAmplitude())
            amplitudeHandler.postDelayed(this, 50)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recordingRecyclerView = findViewById(R.id.recording_list)
        waveMicButton = findViewById<WaveMicButton>(R.id.waveMicButton)
        tvState = findViewById<TextView>(R.id.tv_state)
        btnRecord = findViewById(R.id.record_button)
        audioListAdapter = AudioListAdapter(
            recordings,
            onPlayClick = { item ->
                if (item.isPlaying) {
                    // 停止播放
                    RecordUtils.stopPlaying()
                } else {
                    // 开始播放
                    RecordUtils.playRecording(File(item.filePath))
                }
            },
            onDeleteClick = { item ->
                DialogUtils.showConfirmDialog(
                    context = this@MainActivity,
                    message = "确定要删除该录音吗？",
                    onConfirm = {
                        audioListAdapter.removeItem(item)
                        RecordUtils.removeRecording(item.filePath)
                    }
                )
            }
        )
        recordingRecyclerView.adapter = audioListAdapter
        recordingRecyclerView.layoutManager = LinearLayoutManager(this)
        // 从缓存读取录音文件
        audioListAdapter.addAllItem(RecordUtils.loadAllRecordings(this@MainActivity))
        btnRecord.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true       // 手动触发 pressed
                    // 录音按钮点击前检查
                    if (!PermissionUtils.hasPermission(baseContext, Manifest.permission.RECORD_AUDIO)) {
                        PermissionUtils.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            100
                        )
                    } else {
                        // init recorder
                        RecordUtils.initRecorder(baseContext)
                        // 开始录音
                        RecordUtils.startRecording()
                        amplitudeHandler.post(amplitudeRunnable)
                        tvState.text = "正在录音中..."
                        pressTime = SystemClock.elapsedRealtime()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false      // 手动取消 pressed
                    // 结束录音保存
                    val file = RecordUtils.stopRecording() // 你可以让它返回 Pair<File, Long>
                    if (file != null) {
                        val item = RecordingItem(
                            fileName = file.name,
                            filePath = file.absolutePath,
                            recordTime = TimeUtils.formatTime(System.currentTimeMillis()),
                            duration = SystemClock.elapsedRealtime() - pressTime
                        )
                        audioListAdapter.addItem(item)
                    }
                    amplitudeHandler.removeCallbacks(amplitudeRunnable)
                    waveMicButton.stopAnimation()
                    tvState.text = "点击开始录音"
                }
            }
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // init recorder
                RecordUtils.initRecorder(baseContext)
                Toast.makeText(this, "录音权限已授权，请重新点击录音", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "录音权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
}