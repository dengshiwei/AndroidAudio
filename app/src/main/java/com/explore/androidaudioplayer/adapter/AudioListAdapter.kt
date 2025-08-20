package com.explore.androidaudioplayer.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.explore.androidaudioplayer.R
import com.explore.androidaudioplayer.audio.RecordUtils
import com.explore.androidaudioplayer.model.RecordingItem
import com.explore.androidaudioplayer.utils.TimeUtils

class AudioListAdapter(
    private val items: MutableList<RecordingItem>,
    private val onPlayClick: (RecordingItem) -> Unit,
    private val onDeleteClick: (RecordingItem) -> Unit
) : RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var processBar: ProgressBar
    private lateinit var btnPlay: ImageButton

    init {
        RecordUtils.addOnAudioPlayListener(object : RecordUtils.OnAudioPlayListener {
            override fun onPlayCompleted() {
                // 重置进度条
                processBar.progress = 0
                handler.removeCallbacks(updateSeekRunnable)
                btnPlay.setImageResource(R.drawable.ic_play)
            }
        })
    }

    private val updateSeekRunnable = object : Runnable {
        override fun run() {
            processBar.progress = RecordUtils.getPlayProcess()
            Log.d("AudioListAdapter", "processBar.progress = ${RecordUtils.getPlayProcess()}")
            handler.postDelayed(this, 500) // 每 0.5s 更新一次
        }
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.recording_title)
        val recordTime: TextView = itemView.findViewById(R.id.recording_date)
        val recordDuration: TextView = itemView.findViewById(R.id.recording_duration)
        val btnPlay: ImageButton = itemView.findViewById(R.id.play_button)
        val btnDelete: ImageButton = itemView.findViewById(R.id.delete_button)
        val processBar: ProgressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recording, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val item = items[position]
        holder.fileName.text = item.fileName
        holder.recordTime.text = item.recordTime
        holder.recordDuration.text = "${TimeUtils.formatDuration(item.duration)} s"
        holder.btnPlay.setOnClickListener {
            onPlayClick(item)
            if (item.isPlaying) {
                item.isPlaying = false
                holder.btnPlay.setImageResource(R.drawable.ic_play)
                handler.removeCallbacks(updateSeekRunnable)
            } else {
                item.isPlaying = true
                holder.btnPlay.setImageResource(R.drawable.ic_pause)
                processBar = holder.processBar
                btnPlay = holder.btnPlay
                // 启动进度更新
                handler.post(updateSeekRunnable)
            }
        }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: RecordingItem) {
        items.add(0, item) // 新的放前面
        notifyItemInserted(0)
    }

    fun addAllItem(itemTemps: List<RecordingItem>) {
        items.addAll(itemTemps)
        notifyItemInserted(0)
    }

    fun removeItem(item: RecordingItem) {
        val index = items.indexOf(item)
        items.remove(item)
        notifyItemRemoved(index)
    }
}
