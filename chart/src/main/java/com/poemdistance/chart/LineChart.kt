package com.poemdistance.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.gson.JsonObject
import java.lang.StringBuilder
import kotlin.math.ceil
import kotlin.math.floor

class LineChart(context: Context, attrs: AttributeSet) : RectangularCoordinateSystem(context, attrs) {

    val seriesPaint = Paint().apply {
        strokeWidth = 4f
        isAntiAlias = true
    }
    var inited = false // 数据变化后是否已绘制动画
    var progress = 0f // 当前动画绘制进度，取值 (0 - data.size)

    init {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.Chart)
        animationDuration = obtainStyledAttributes.getInteger(R.styleable.Chart_animationDuration,2000).toLong()
        obtainStyledAttributes.recycle()
    }

    override fun drawSeries(canvas: Canvas) {
        if(animation){
            if(!inited){
                inited = true
                startAnimation()
            }
            drawAnimateLines(canvas,progress)
        }else{
            drawLines(canvas)
        }
    }

    private fun drawLines(canvas: Canvas){
        if(data.isEmpty()) return
        val prevPoint = arrayOfNulls<Point>(yAxis.size)
        for(index in data.indices){
            if(index == 0 ){
                yAxis.mapIndexed { yIndex, s ->
                    val pointX = origin.x + index * xWidthUnit + xWidthUnit/2
                    val pointY = ((data[index][s].asFloat - yMinValue) / (yMaxValue - yMinValue)) * (yEndPoint.y - origin.y) + origin.y
                    seriesPaint.color = theme[yIndex % theme.size]
                    prevPoint[yIndex] = Point(pointX, pointY)
                    if(index == focusedDataIndex)
                        canvas.drawCircle(pointX, pointY,10f, seriesPaint)
                    else
                        canvas.drawCircle(pointX, pointY,5f, seriesPaint)
                }
            }else{
                yAxis.mapIndexed { yIndex, s ->
                    val pointY =
                        ((data[index][s].asFloat - yMinValue) / (yMaxValue - yMinValue)) * (yEndPoint.y - origin.y) + origin.y
                    val pointX = origin.x + index * xWidthUnit + xWidthUnit / 2
                    seriesPaint.color = theme[yIndex % theme.size]
                    canvas.drawLine(prevPoint[yIndex]!!.x,prevPoint[yIndex]!!.y,pointX, pointY, seriesPaint)
                    prevPoint[yIndex] = Point(pointX, pointY)
                    if(index == focusedDataIndex)
                        canvas.drawCircle(pointX, pointY,10f, seriesPaint)
                    else
                        canvas.drawCircle(pointX, pointY,5f, seriesPaint)
                }
            }
        }
    }

    private fun drawAnimateLines(canvas: Canvas, progress:Float){
        if(data.isEmpty()) return
        val prevPoint = arrayOfNulls<Point>(yAxis.size)
        for(index in 0 .. progress.toInt()){
            if(index == 0){
                yAxis.mapIndexed { yIndex, s ->
                    val pointX = origin.x + index * xWidthUnit + xWidthUnit/2
                    val pointY = ((data[index][s].asFloat - yMinValue) / (yMaxValue - yMinValue)) * (yEndPoint.y - origin.y) + origin.y
                    seriesPaint.color = theme[yIndex % theme.size]
                    prevPoint[yIndex] = Point(pointX, pointY)
                    if(index == focusedDataIndex)
                        canvas.drawCircle(pointX, pointY,10f, seriesPaint)
                    else
                        canvas.drawCircle(pointX, pointY,5f, seriesPaint)
                }
            }else{
                yAxis.mapIndexed { yIndex, s ->
                    val pointY =
                        ((data[index][s].asFloat - yMinValue) / (yMaxValue - yMinValue)) * (yEndPoint.y - origin.y) + origin.y
                    val pointX = origin.x + index * xWidthUnit + xWidthUnit / 2
                    seriesPaint.color = theme[yIndex % theme.size]
                    canvas.drawLine(prevPoint[yIndex]!!.x,prevPoint[yIndex]!!.y,pointX, pointY, seriesPaint)
                    prevPoint[yIndex] = Point(pointX, pointY)
                    if(index == focusedDataIndex)
                        canvas.drawCircle(pointX, pointY,10f, seriesPaint)
                    else
                        canvas.drawCircle(pointX, pointY,5f, seriesPaint)
                }
            }
        }
        yAxis.mapIndexed { yIndex, s ->
            val nextIndex = ceil(progress).toInt()
            val pointY =
                ((data[nextIndex][s].asFloat - yMinValue) / (yMaxValue - yMinValue)) * (yEndPoint.y - origin.y) + origin.y
            val pointX = origin.x + nextIndex * xWidthUnit + xWidthUnit / 2
            val k = (pointY - prevPoint[yIndex]!!.y) / (pointX - prevPoint[yIndex]!!.x)
            seriesPaint.color = theme[yIndex % theme.size]
            canvas.drawLine(prevPoint[yIndex]!!.x,prevPoint[yIndex]!!.y, prevPoint[yIndex]!!.x + (progress - floor(progress))*xWidthUnit,prevPoint[yIndex]!!.y + k * (progress - floor(progress))* xWidthUnit, seriesPaint)
        }
    }

    private fun startAnimation(){
        ValueAnimator.ofFloat(0f,(data.size - 1).toFloat()).apply {
            addUpdateListener {
                progress = animatedValue as Float
                invalidate()
            }
            interpolator = AccelerateDecelerateInterpolator()
            duration = animationDuration
        }.start()
    }

    /**
     * 绘制说明区域
     */
    override fun drawFocusedInfoText(canvas: Canvas, focusedDataIndex: Int) {
        return
            }

    override fun drawLegendItem(
        canvas: Canvas,
        yAxisIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        seriesPaint.color = theme[yAxisIndex]
        canvas.drawLine(x, y + height/2,x+width,y+height/2, seriesPaint)
        canvas.drawCircle(x+width/2, y+height/2,10f, seriesPaint)
    }

    override fun onDataUpdate(newData: List<JsonObject>) {
        super.onDataUpdate(newData)
        if(animation){
            inited = false
            progress = 0f
        }
    }
}