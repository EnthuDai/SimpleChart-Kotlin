package com.poemdistance.chart

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import com.google.gson.JsonObject
import kotlin.math.*


class PieChart(context: Context, attrs: AttributeSet) :
    Chart(context, attrs) {

    class PointEvaluator : TypeEvaluator<Point> {
        override fun evaluate(fraction: Float, startValue: Point?, endValue: Point?): Point {
            if (startValue != null && endValue != null)
                return Point(
                    startValue.x + fraction * (endValue.x - startValue.x),
                    startValue.y + fraction * (endValue.y - startValue.y)
                )
            else
                return Point(0f, 0f)
        }

    }

    override val LOG_TAG = "Pie Chart"

    var origin = Point(0f, 0f)
    var touchedOrigin = Point(0f, 0f) // 在触摸动画中，坐标系原点的位置
    var nameProperty: String
    var valueProperty: String
    var radius: Float = 100f
    var touchedOffset = 20f // 触摸区域扇形原点与极坐标系圆点的偏移量
    var touchedAngle = 0f // 取值范围0°-360°
    var touchSectorStart = 0f // 触摸点所在的扇形区域
    var touchSectorEnd = 0f // 触摸点所在的扇形区域
    val piePaint = Paint().apply {
        isAntiAlias = true
    }
    var prevFocusedDataIndex: Int = -1

    init {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.PieChart)
        nameProperty = obtainStyledAttributes.getString(R.styleable.PieChart_name) ?: "name"
        valueProperty =
            obtainStyledAttributes.getString(R.styleable.PieChart_value) ?: "value"
        obtainStyledAttributes.recycle()
    }

    val path = Path()
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            // 确定极坐标点位置
            origin.x = width / 2f
            origin.y = height / 2f
            radius = min(width, height) / 3f
            var totalValue = 0f
            data.map {
                totalValue += it[valueProperty].asFloat
            }
            var totalAngle = 0f
            data.mapIndexed { index, it ->
                piePaint.color = theme[index % theme.size]
                path.reset()
                val angle = it[valueProperty].asFloat / totalValue * 360
                if (touchedAngle > totalAngle && touchedAngle < totalAngle + angle) { // 绘制触摸区域的扇形
                    drawFocusedInfoText(canvas, index)
                    // 上一个点击动画未完成时，用户点击触发另一个扇面后，如不处理，将会新扇面将在上个扇面动画的绘制参数下绘制一次，造成视觉上的动画闪烁
                    if (prevFocusedDataIndex != focusedDataIndex) {
                        Log.i(LOG_TAG, "此次点击的扇形下标为 $focusedDataIndex ,上次下标为 $prevFocusedDataIndex ")
                        touchedOrigin.x = origin.x
                        touchedOrigin.y = origin.y
                        prevFocusedDataIndex = focusedDataIndex
                    }
                    path.moveTo(touchedOrigin.x, touchedOrigin.y)
                    path.lineTo(
                        radius * cos(totalAngle / 360 * 2 * Math.PI).toFloat() + touchedOrigin.x,
                        radius * sin(totalAngle / 360 * 2 * Math.PI).toFloat() + touchedOrigin.y
                    )
                    path.arcTo(
                        touchedOrigin.x - radius,
                        touchedOrigin.y - radius,
                        touchedOrigin.x + radius,
                        touchedOrigin.y + radius,
                        totalAngle,
                        angle,
                        false
                    )
                    path.close()
                    if (touchedAngle < touchSectorStart || touchedAngle > touchSectorEnd) { // 触摸点与上次触摸点所在扇形不同, 触发动画效果
                        touchSectorStart = totalAngle
                        touchSectorEnd = totalAngle + angle
                        startAnimation()
                    }
                } else {
                    path.moveTo(origin.x, origin.y)
                    path.lineTo(
                        radius * cos(totalAngle / 360 * 2 * Math.PI).toFloat() + origin.x,
                        radius * sin(totalAngle / 360 * 2 * Math.PI).toFloat() + origin.y
                    )
                    path.arcTo(
                        origin.x - radius,
                        origin.y - radius,
                        origin.x + radius,
                        origin.y + radius,
                        totalAngle,
                        angle,
                        false
                    )
                    path.close()
                }
                totalAngle += angle
                // 当上一个点击动画未完成时，用户点击触发另一个扇面后，如不处理，将会新扇面将在上个扇面动画的绘制参数下绘制一次，造成视觉上的动画闪烁
//                if(prevFocusedDataIndex != focusedDataIndex){
//                    Log.i(LOG_TAG,"此次点击的扇形下标为 $focusedDataIndex ,上次下标为 $prevFocusedDataIndex ")
//                    touchedOrigin.x = origin.x
//                    touchedOrigin.y = origin.y
//                    prevFocusedDataIndex = focusedDataIndex
//                }else{
                canvas.drawPath(path, piePaint)
//                }
            }
            if (showLegend) drawLegend(canvas)
        }
    }

    /**
     * 绘制说明区域
     */
    fun drawFocusedInfoText(canvas: Canvas, focusedDataIndex: Int) {
        var totalValue = 0f
        data.map {
            totalValue += it[valueProperty].asFloat
        }
        val text =
            " ${data[focusedDataIndex][nameProperty].asString}:${data[focusedDataIndex][valueProperty].asString}  ${"%.1f".format(data[focusedDataIndex][valueProperty].asFloat / totalValue * 100)}% "
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, origin.x,origin.y + radius + 10 +titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top, titlePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(LOG_TAG, event?.action.toString())
        Log.d(LOG_TAG, event?.x.toString())
        Log.d(LOG_TAG, event?.y.toString())
        if (event != null && data.isNotEmpty()) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val distance =
                    (event.x - origin.x).pow(2) + (event.y - origin.y).pow(2) // 触控点距离原点的距离
                if (distance <= radius.pow(2)) { // 触摸点落在饼图内
                    if (event.x == origin.x) {
                        touchedAngle = if (event.y - origin.y > 0) 90f
                        else if (event.y - origin.y < 0) 270f
                        else 0f
                    } else {
                        val y = event.y - origin.y
                        val x = event.x - origin.x
                        touchedAngle = if (x > 0 && y > 0) { // 触摸点位于第一象限 夹角 = arctan（y/x）
                            atan(y / x) / PI.toFloat() * 180
                        } else if (x < 0 && y > 0) { // 触摸点位于第二象限 夹角 = PI - arctan（y/x）
                            (PI.toFloat() - atan(y / -x)) / PI.toFloat() * 180
                        } else if (x < 0 && y < 0) { // 触摸点位于第三象限 夹角 = PI + arctan(y/x)
                            (PI.toFloat() + atan(-y / -x)) / PI.toFloat() * 180
                        } else { // 触摸点位于第三象限 夹角 = 2*PI - arctan(y/x)
                            (2 * PI.toFloat() - atan(-y / x)) / PI.toFloat() * 180
                        }
                        Log.d(LOG_TAG, "触摸点的极坐标角度为：%5f°".format(touchedAngle))
                    }
                    if (touchedAngle < touchSectorStart || touchedAngle > touchSectorEnd) {

                        var totalValue = 0f
                        data.map {
                            totalValue += it[valueProperty].asFloat
                        }
                        var tmp = 0.0
                        var index = 0
                        while (tmp < touchedAngle && index < data.size) {
                            tmp += data[index][valueProperty].asFloat / totalValue * 360
                            index++
                        }
                        focusedDataIndex = index - 1
                        onFocused(data[focusedDataIndex])
                        invalidate()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            onTouchEvent(event)
        }
        return true
    }

    private fun startAnimation() {
        val endPoint = Point(
            origin.x + touchedOffset * cos((touchSectorStart + touchSectorEnd) / 360 * PI.toFloat()),
            origin.y + touchedOffset * sin((touchSectorStart + touchSectorEnd) / 360 * PI.toFloat())
        )
        ValueAnimator.ofObject(PointEvaluator(), origin, endPoint).apply {
            addUpdateListener {
                touchedOrigin = it.animatedValue as Point
                invalidate()
            }
            interpolator = DecelerateInterpolator()
            duration = animationDuration
        }.start()
    }

    var legendMarginTop = 10f + 80f // 需要加上title所占用的高度
    var legendMarginLeft = 10f
    val legendItemWidth = 100f
    val legendPaint = Paint().apply {
        textSize = 50f
        isAntiAlias = true
    }

    private fun drawLegend(canvas: Canvas) {
        val legendItemHeight = legendPaint.fontMetrics.descent - legendPaint.fontMetrics.ascent
        var maxRightEdge = 0f
        var nextItemLeft = legendMarginLeft // 图标绘制的起始坐标
        var nextItemTop = legendMarginTop // 图标绘制的起始坐标
        data.mapIndexed { index, obj ->
            if (nextItemTop + legendItemHeight > height) { //图例过多时换列排布
                nextItemLeft = maxRightEdge + 20f
                nextItemTop = legendMarginTop
            }
            legendPaint.color = theme[index % theme.size]
            canvas.drawRoundRect(
                nextItemLeft,
                nextItemTop,
                nextItemLeft + legendItemWidth,
                nextItemTop + legendItemHeight,
                15f, 15f,
                legendPaint
            )
            legendPaint.color = if(isDarkTheme(context)) Color.WHITE else Color.BLACK
            canvas.drawText(
                obj[nameProperty].asString,
                nextItemLeft + legendItemWidth + 10f,
                nextItemTop + legendItemHeight - legendPaint.fontMetrics.bottom,
                legendPaint
            )
            maxRightEdge = maxOf(
                maxRightEdge,
                legendPaint.measureText(obj[nameProperty].asString) + nextItemLeft + legendItemWidth + 10f
            )
            nextItemTop += legendItemHeight + 20f
        }
    }

    override fun onDataUpdate(newData: List<JsonObject>) {
        touchedOrigin = Point(origin.x, origin.y)
        touchSectorStart = 0f
        touchSectorEnd = 0f
        touchedAngle = 0f
    }
}
