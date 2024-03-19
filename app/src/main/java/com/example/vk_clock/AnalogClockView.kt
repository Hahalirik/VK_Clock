package com.example.vk_clock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockView @JvmOverloads constructor (
    context : Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private var dialColor: Int = ContextCompat.getColor(context, R.color.light_gray)
    private var pointsColor: Int = ContextCompat.getColor(context, R.color.black)
    private var textColor: Int = ContextCompat.getColor(context, R.color.black)
    private var hourHandColor: Int = ContextCompat.getColor(context, R.color.black)
    private var minuteHandColor: Int = ContextCompat.getColor(context, R.color.black)
    private var secondHandColor: Int = ContextCompat.getColor(context, R.color.red)
    private var hourHandWidth: Float = 32f
    private var minuteHandWidth: Float = 16f
    private var secondHandWidth: Float = 8f

    private var handler: Handler = Handler()
    private lateinit var timeUpdater: Runnable

    private var paintText: Paint = Paint()
    private var paintDial: Paint = Paint()
    private var paintPointer: Paint = Paint()
    private val handPaint = Paint()

    private var hourData: Float = 0f
    private var minuteData: Float = 0f
    private var secondData: Int = 0

    init {
        paintText.color = textColor
        paintText.style = Paint.Style.FILL_AND_STROKE
        paintText.textSize = 40f

        paintDial.flags = Paint.ANTI_ALIAS_FLAG
        paintDial.color = dialColor
        paintDial.style = Paint.Style.FILL_AND_STROKE

        paintPointer.flags = Paint.ANTI_ALIAS_FLAG
        paintPointer.color = pointsColor
        paintPointer.style = Paint.Style.FILL

        handPaint.flags = Paint.ANTI_ALIAS_FLAG
        handPaint.style = Paint.Style.FILL

        timeUpdater = object : Runnable {
            override fun run() {

                val currentTime: String =
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                setClockTime(
                    currentTime.substring(0, 2).toFloat() % 12,
                    currentTime.substring(3, 5).toFloat(),
                    currentTime.substring(6, 8).toInt()
                )

                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2f

        canvas.drawCircle(
            centerX,
            centerY,
            radius,
            paintDial
        )

        val numberCircleRadius = radius - 60f
        val pointHourRadius = 20f
        val pointMinuteRadius = 10f
        lateinit var xyPointData: Pair<Float, Float>
        lateinit var xyTextData: Pair<Float, Float>
        for (i in 0..11) {
            val rect = Rect()
            val text = i.toString()
            paintText.getTextBounds(text, 0, text.length, rect)
            xyPointData = getXY(i.toFloat(), numberCircleRadius, 30)
            xyTextData = getXY(i.toFloat(), numberCircleRadius - 35f, 30)
            canvas.drawCircle(xyPointData.first, xyPointData.second, pointHourRadius, paintPointer)
            canvas.drawText(
                text,
                xyTextData.first - rect.exactCenterX(),
                xyTextData.second - rect.exactCenterY(),
                paintText)
        }
        for (i in 0..60){
            xyPointData = getXY(i.toFloat(), numberCircleRadius, 6)
            canvas.drawCircle(xyPointData.first, xyPointData.second, pointMinuteRadius, paintPointer)
        }

        drawHandWithPaint(
            canvas,
            hourHandColor,
            hourHandWidth,
            getXY(hourData, numberCircleRadius - 130, 30)
        )

        drawHandWithPaint(
            canvas,
            minuteHandColor,
            minuteHandWidth,
            getXY(minuteData, numberCircleRadius - 80, 6)
        )

        drawHandWithPaint(
            canvas,
            secondHandColor,
            secondHandWidth,
            getXY(secondData.toFloat(), numberCircleRadius - 30, 6)
        )
    }

    private fun getXY(pos: Float, rad: Float, skipAngle: Int): Pair<Float, Float> {
        val startAngle = 270f
        val angle = startAngle + (pos * skipAngle)

        val x = (rad * cos(angle * Math.PI / 180) + width / 2).toFloat()
        val y = (height / 2 + rad * sin(angle * Math.PI / 180)).toFloat()
        return Pair(x, y)
    }

    private fun drawHandWithPaint(
        canvas: Canvas?,
        handColor: Int,
        strokeWidth: Float,
        xyData: Pair<Float, Float>
    ) {
        handPaint.color = handColor
        handPaint.strokeWidth = strokeWidth
        canvas?.drawLine(width / 2f, height / 2f, xyData.first, xyData.second, handPaint)
    }

    private fun setClockTime(hour: Float, minute: Float, second: Int) {
        hourData = hour + (minute / 60)
        minuteData = minute
        secondData = second
        invalidate()
    }

    fun startClock() {
        handler.post(timeUpdater)
    }

    // stop clock
    fun stopClock() {
        handler.removeCallbacks(timeUpdater)
    }
}