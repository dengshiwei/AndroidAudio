package com.explore.androidaudioplayer.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

class WaveMicButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val wavePaints = listOf(
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3F51B5") // 蓝
            style = Paint.Style.STROKE
            strokeWidth = 6f
            alpha = 180
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF4081") // 粉
            style = Paint.Style.STROKE
            strokeWidth = 4f
            alpha = 150
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4CAF50") // 绿
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 120
        }
    )

    private var amplitude: Int = 0 // 实时音量
    private var phaseShift = 0f    // 相位偏移
    private val wavePaths = Array(wavePaints.size) { Path() }

    private val animator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
        duration = 1000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phaseShift = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        animator.start()
    }

    fun updateAmplitude(amp: Int) {
        amplitude = amp.coerceIn(0, 32767)
        invalidate()
        animator.start()
    }

    fun stopAnimation() {
        amplitude = 0
        invalidate()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2

        val normalizedAmp = amplitude / 32767f
        val baseWaveHeight = normalizedAmp * height * 0.8f

        val waveLength = width / 1.2f
        val step = 5f

        wavePaths.forEachIndexed { index, path ->
            path.reset()
            path.moveTo(0f, centerY)

            val waveHeight = baseWaveHeight * (0.6f + 0.4f * index)
            val offset = phaseShift + index * 0.8f

            var x = 0f
            while (x <= width) {
                val y = (waveHeight * sin((2 * Math.PI * x / waveLength + offset))).toFloat()
                path.lineTo(x, centerY - y)
                x += step
            }
            canvas.drawPath(path, wavePaints[index])
        }
    }
}
