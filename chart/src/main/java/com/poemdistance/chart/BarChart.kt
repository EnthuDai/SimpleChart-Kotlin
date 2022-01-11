package com.poemdistance.chart

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.google.gson.JsonObject

class BarChart(context: Context, attrs: AttributeSet) : RectangularCoordinateSystem(context, attrs) {


    override val LOG_TAG = "Chart Bar"
    val barPaint = Paint().apply {
        this.color = Color.BLUE
    }

    override fun drawLegendItem(
        canvas: Canvas,
        yAxisIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        barPaint.color = theme[yAxisIndex]
        canvas.drawRoundRect(x,y,x+width, y+height,15f, 15f,barPaint)
    }


    var inited = false // 数据变化后是否已绘制动画
    var animationBarHeightRatio = 0f
    /**
     * 绘制柱子
     */
    override fun drawSeries(canvas: Canvas) {
        if(animation){
            if(!inited){
                inited = true
                startAnimation()
            }
            drawBar(canvas,animationBarHeightRatio)
        }else{
            drawBar(canvas,1f)
        }
    }

    private fun drawBar(canvas: Canvas,ratio:Float){
        val barWidth = xWidthUnit * 0.8f
        for (index in data.indices) {
            val barHeight =
                (data[index][yAxis[0]].asFloat - yMinValue) / (yMaxValue - yMinValue) * (yEndPoint.y - origin.y) * ratio
            canvas.drawRect(
                origin.x + xWidthUnit * index + (xWidthUnit - barWidth) / 2,
                origin.y + barHeight,
                origin.x + xWidthUnit * index + barWidth,
                origin.y,
                barPaint
            )
        }
    }

    private fun startAnimation() {
        ValueAnimator.ofObject(FloatEvaluator(),0,1).apply{
            addUpdateListener {
                animationBarHeightRatio = it.animatedValue as Float
                invalidate()
            }
            duration = animationDuration
        }.start()
    }

    override fun onDataUpdate(newData: List<JsonObject>) {
        super.onDataUpdate(newData)
        if(animation){
            inited = false
            animationBarHeightRatio = 0f
        }
    }
}