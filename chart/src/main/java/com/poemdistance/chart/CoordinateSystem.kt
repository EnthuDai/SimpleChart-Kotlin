package com.poemdistance.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.google.gson.JsonObject
import java.security.InvalidParameterException
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 直角坐标系下的图表
 */
abstract class RectangularCoordinateSystem(context: Context, attrs: AttributeSet) :
    Chart(context, attrs) {

    var origin: Point = Point(0f, 0f) // 坐标系原点坐标
    var xEndPoint: Point = Point(0f, 0f) // x轴终点坐标
    var yEndPoint: Point = Point(0f, 0f) // y轴终点坐标
    var xWidthUnit = 0f //x轴单位刻度的宽度，指一个bar在x轴上占用的总宽度（包含了bar两边的空白区域）
    val linePaint = Paint().apply {
        this.color = Color.BLACK
    }
    var xAxis: String // x轴字段名称
    var yAxis: List<String>
    var yAxisDesc: List<String> // y轴字段中文描述
    var yMaxValue = 0f // y轴刻度最大值
    var yMinValue = 0 // y轴刻度最小值
    var yIntervalCounts: Int = 5 // y轴分为几段

    init {
        val obtainStyledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.RectangularCoordinateSystem)
        xAxis =
            obtainStyledAttributes.getString(R.styleable.RectangularCoordinateSystem_xAxis) ?: ""
        if (xAxis == "") throw InvalidParameterException("图表 xAxis[x轴参数属性名] 缺失，请在图表组件配置项中添加 xAxis 参数，值为该图表的x轴内容属性名")
        yAxis =
            (obtainStyledAttributes.getString(R.styleable.RectangularCoordinateSystem_yAxis) ?: "")
                .split(Regex("[,，]"))
        if (yAxis.isEmpty()) throw InvalidParameterException("图表 yAxis[y轴参数属性名] 缺失，请在图表组件配置项中添加 yAxis 参数，值为该图表的y轴内容属性名，多个y轴可用“，”隔开")
        yAxisDesc = yAxis
        obtainStyledAttributes.getString(R.styleable.RectangularCoordinateSystem_yAxisDesc)?.let {
            yAxisDesc = it.split(Regex("[,，]"))
        }
        showLegend = yAxis.size > 1
        obtainStyledAttributes.recycle()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            origin.x = paddingLeft
            origin.y = height - paddingBottom
            xEndPoint.x = width - paddingRight
            xEndPoint.y = height - paddingTop
            yEndPoint.x = paddingLeft
            yEndPoint.y = paddingTop

            xWidthUnit = if (data.isEmpty()) {
                xEndPoint.x - origin.x
            } else {
                (xEndPoint.x - origin.x) / data.size
            }

            // 绘制x轴
            canvas.drawLine(origin.x, origin.y, xEndPoint.x, xEndPoint.y, linePaint)
            // 绘制y轴
            canvas.drawLine(origin.x, origin.y, yEndPoint.x, yEndPoint.y, linePaint)

            drawXTick(canvas) // 绘制刻度
            drawYTick(canvas)
            drawSeries(canvas)
            if (showLegend) drawLegend(canvas)
            if (focusedDataIndex != -1) {
                drawFocusedInfo(canvas)
            }

        }
    }

    private fun drawLegend(canvas: Canvas) {
        var totalWidth = 0f
        val legendItemWidth = 100f
        val legendItemInterval = 10f
        titlePaint.textAlign = Paint.Align.LEFT
        val height = titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top
        val legendItemHeight = titlePaint.fontMetrics.descent - titlePaint.fontMetrics.ascent
        val marginTop = yEndPoint.y - height
        yAxisDesc.map {
            totalWidth += titlePaint.measureText(it) + 10 + legendItemWidth + legendItemInterval
        }
        var prevEnd = (xEndPoint.x - origin.x) / 2 + origin.x - totalWidth / 2
        yAxisDesc.mapIndexed { index, s ->
            drawLegendItem(canvas, index, prevEnd, marginTop, legendItemWidth, legendItemHeight)
            prevEnd += legendItemWidth + 10f // 设置图标与文字间隙
            canvas.drawText(
                s,
                prevEnd,
                marginTop + height / 2 + titlePaint.fontMetrics.bottom,
                titlePaint
            )
            prevEnd += titlePaint.measureText(s) + legendItemInterval
        }
    }

    /**
     * 绘制单个图例
     */
    abstract fun drawLegendItem(
        canvas: Canvas,
        yAxisIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    )


    /**
     * 绘制坐标系内的bar或line
     */
    abstract fun drawSeries(canvas: Canvas)

    /**
     * 绘制描述触摸区域的描述信息
     */
    private fun drawFocusedInfo(canvas: Canvas) {
        canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), 0x88)
        // 绘制y轴触摸点的标尺线
        canvas.drawLine(
            origin.x + focusedDataIndex * xWidthUnit + xWidthUnit / 2,
            origin.y,
            origin.x + focusedDataIndex * xWidthUnit + xWidthUnit / 2,
            yEndPoint.y,
            linePaint
        )
        // 绘制x轴触摸点的标尺线
        yAxis.mapIndexed { yIndex, s ->
            canvas.drawLine(
                origin.x,
                origin.y + (data[focusedDataIndex][yAxis[yIndex]].asFloat - yMinValue) / (yMaxValue - yMinValue) * (yEndPoint.y - origin.y),
                xEndPoint.x,
                origin.y + (data[focusedDataIndex][yAxis[yIndex]].asFloat - yMinValue) / (yMaxValue - yMinValue) * (yEndPoint.y - origin.y),
                linePaint
            )
        }
        //绘制说明区域
        drawFocusedInfoText(canvas, focusedDataIndex)
    }

    /**
     * 绘制说明区域
     */
    open fun drawFocusedInfoText(canvas: Canvas, focusedDataIndex: Int) {
        return
        val text =
            "${data[focusedDataIndex][xAxis].asString}:${data[focusedDataIndex][yAxis[0]].asString}"
        //绘制说明区域阴影框
        titlePaint.color = Color.GRAY
        canvas.drawRect(
            xEndPoint.x - titlePaint.measureText(text) - titlePaint.fontMetrics.descent,
            yEndPoint.y,
            xEndPoint.x + titlePaint.fontMetrics.descent,
            yEndPoint.y + titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top,
            linePaint
        )
        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.saveLayerAlpha(0f, 0f, width.toFloat(), height.toFloat(), 255)
        titlePaint.color = Color.WHITE
        canvas.drawText(text, xEndPoint.x, yEndPoint.y - titlePaint.fontMetrics.ascent, titlePaint)
    }

    /**
     * 绘制x轴刻度及值
     */
    private fun drawXTick(canvas: Canvas) {
        titlePaint.color = Color.BLACK
        titlePaint.textAlign = Paint.Align.CENTER
        if (focusedDataIndex != -1) { // 在触摸时只绘制触摸点的信息
            val x = xWidthUnit * focusedDataIndex + xWidthUnit / 2 + origin.x
            canvas.drawLine(x, origin.y, x, origin.y + 10, linePaint)
            canvas.drawText(
                data[focusedDataIndex][xAxis].asString,
                x,
                origin.y + titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top,
                titlePaint
            )
            titlePaint.alpha = 15
        }
        var prevTextEnd = origin.x
        for (index in data.indices) {
            val x = xWidthUnit * index + xWidthUnit / 2 + origin.x
            val textWidth = titlePaint.measureText(data[index][xAxis].asString)
            if (x - textWidth / 2 > prevTextEnd + textWidth * 0.1) {
                canvas.drawLine(x, origin.y, x, origin.y + 10, linePaint)
                canvas.drawText(
                    data[index][xAxis].asString,
                    x,
                    origin.y + titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top,
                    titlePaint
                )
                prevTextEnd = x + textWidth / 2
            }
        }
        if (focusedDataIndex != -1) {
            titlePaint.alpha = 255
        }
    }

    /**
     * 绘制y轴刻度及值
     */
    private fun drawYTick(canvas: Canvas) {
        titlePaint.color = Color.BLACK
        titlePaint.textAlign = Paint.Align.RIGHT
        var y = 0f
        if (focusedDataIndex != -1) {
            yAxis.map {
                y =
                    origin.y + (yEndPoint.y - origin.y) / (yMaxValue - yMinValue) * (data[focusedDataIndex][it].asFloat - yMinValue)
                canvas.drawLine(origin.x - 10, y, origin.x, y, linePaint)
                canvas.drawText(
                    formatFloatStr(data[focusedDataIndex][it].asFloat),
                    origin.x - 15,
                    y + (titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top) / 2 - titlePaint.fontMetrics.bottom,
                    titlePaint
                )
            }
            titlePaint.alpha = 15
        }
        for (index in 0..yIntervalCounts) {
            y = origin.y + (yEndPoint.y - origin.y) / yIntervalCounts * index
            canvas.drawLine(origin.x - 10, y, origin.x, y, linePaint)
            canvas.drawText(
                formatFloatStr((yMaxValue - yMinValue) / yIntervalCounts * index),
                origin.x - 15,
                y + (titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.top) / 2 - titlePaint.fontMetrics.bottom,
                titlePaint
            )
        }
        if (focusedDataIndex != -1) {
            titlePaint.alpha = 255
        }
    }

    private fun formatFloatStr(float: Float): String {
        val result = float.toString().toCharArray()
        var dotIndex = 0 // 小数点的下标
        while (dotIndex < result.size) { // 寻找小数点下标
            if (result[dotIndex] == '.') break
            dotIndex++
        }
        var lastIndex = result.size - 1
        while (lastIndex >= dotIndex) {
            if (result[lastIndex] == '0') {
                result[lastIndex] = ' '
                lastIndex--
            } else if (result[lastIndex] == '.') {
                result[lastIndex] = ' '
                break
            } else {
                break
            }
        }
        return String(result).trim()
    }


    override fun onDataUpdate(newData: List<JsonObject>) {
        focusedDataIndex = -1
        if (data.isEmpty()) {
            return
        }
        yMinValue = 0
        yMaxValue = data.maxOf {
            yAxis.maxOf { t ->
                it[t].asFloat
            }
        }
        // 优化y值上限，使y轴上限及数据刻度更合理
        val unitValue = getUnitValue(yMaxValue)
        val pair1 = Pair(ceil(yMaxValue / unitValue).toInt(), unitValue)
        val pair2 = Pair(ceil(yMaxValue / unitValue * 2).toInt(), unitValue / 2)
        val pair3 = Pair(ceil(yMaxValue / unitValue / 1.5).toInt(), unitValue * 1.5f)
        var resultPair = pair1
        if (abs(5 - resultPair.first) > abs(5 - pair2.first)) {
            resultPair = pair2
        }
        if (abs(5 - resultPair.first) > abs(5 - pair3.first)) {
            resultPair = pair3
        }
        yMaxValue = resultPair.first * resultPair.second
        yIntervalCounts = resultPair.first
    }

    /**
     * 如果x轴字段是数字类型，把数据x轴字段从小到大排序
     */
    private fun sortData(list: List<JsonObject>): List<JsonObject> {
        return list.sortedBy {
            it[xAxis].asString
        }
    }

    /**
     * 获取一个数值的最高位的值，例如 1234 最高位数值单位为 1000,0.02的最高位数值单位为0.01
     */
    private fun getUnitValue(value: Float): Float {
        val str = value.toString().toCharArray()
        var index = 0
        while (index < str.size) {
            if (str[index].isDigit() && str[index] != '0') {
                str[index] = '1'
                index++
                break
            }
            index++
        }
        while (index < str.size) {
            if (str[index].isDigit()) {
                str[index] = '0'
            }
            index++
        }
        return String(str).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(LOG_TAG, event?.action.toString())
        Log.d(LOG_TAG, event?.x.toString())
        Log.d(LOG_TAG, event?.y.toString())
        if (event != null) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                attemptClaimDrag()
                // 点击点在坐标系内部，确定点击的是哪一个柱体
                if (event.x > origin.x && event.x < xEndPoint.x && event.y < origin.y && event.y > yEndPoint.y) {
                    val dataIndex = ((event.x - origin.x) / xWidthUnit).toInt()
                    Log.d(LOG_TAG, data[dataIndex].toJsonString())
                    if (focusedDataIndex == -1) {
                        focusedDataIndex = dataIndex
                        onFocused(data[dataIndex])
                        invalidate()
                        return true
                    } else if (dataIndex != focusedDataIndex) {
                        focusedDataIndex = dataIndex
                        onFocused(data[dataIndex])
                        invalidate()
                        return true
                    }
                } else {
                    Log.d(LOG_TAG, "触点不在坐标系内！")
                }
            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_OUTSIDE) {
                focusedDataIndex = -1
                onFocused(null)
                invalidate()
                return true
            }
        }
        performClick()
        return super.onTouchEvent(event)
    }
}