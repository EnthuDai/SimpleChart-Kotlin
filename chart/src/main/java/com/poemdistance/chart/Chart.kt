package com.poemdistance.chart

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

// kotlin拓展函数
val gson = Gson()
fun Any.toJson(): JsonObject {
    return JsonParser.parseString(this.toString()).asJsonObject
}
fun Any.toJsonString():String{
    return gson.toJson(this)
}


open class Chart(context: Context, attrs: AttributeSet) : View(context, attrs) {
    data class Point(var x: Float, var y: Float)
    open val LOG_TAG = "Chart"

    val theme = arrayListOf( Color.parseColor("#5470c6"), Color.parseColor("#91cc75"), Color.parseColor("#fac858"), Color.parseColor("#ee6666"),Color.parseColor("#73c0de"),Color.parseColor("#3ba272"),Color.parseColor("#fc8452"),Color.parseColor("#9a60b4"),Color.parseColor("#ea7ccc"))
    val paint = Paint().apply {
        this.color = Color.BLUE
        isAntiAlias = true
    }
    val titlePaint = Paint().apply {
        isAntiAlias = true
        this.textSize = 50f
        this.strokeWidth =  8f
        this.textAlign = Paint.Align.LEFT
    }
    var data:List<JsonObject> = emptyList()
    var paddingLeft = 160f  //图表左边距，此值在直角坐标系中将影响到y轴刻度量的显示
    var paddingRight = 160f
    var paddingBottom = 160f
    var paddingTop = 160f
    var showLegend = true //是否显示图例
    var animation = true // 是否开启动画
    var animationDuration = 500L // 动画时长
    private var title:String? = null

    var listener : ((String?) -> Unit)? = null

    var focusedDataIndex: Int = -1 // 用户点击的数据下标

    val shadowPaint = Paint().apply { // 绘制说明区域阴影
        color = Color.GRAY
    }
    init {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.Chart)
        title = obtainStyledAttributes.getString(R.styleable.Chart_title)
        animation = obtainStyledAttributes.getBoolean(R.styleable.Chart_animation,true)
        animationDuration = obtainStyledAttributes.getInteger(R.styleable.Chart_animationDuration,500).toLong()
        showLegend = obtainStyledAttributes.getBoolean(R.styleable.Chart_showLegend, true)
        obtainStyledAttributes.recycle()
        if(isDarkTheme(getContext())){
            titlePaint.color = Color.WHITE
        }else{
            titlePaint.color = Color.BLACK
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(canvas!==null) {
            Log.d(LOG_TAG, titlePaint.fontMetrics.toJsonString())
            if(title!=null){
                titlePaint.textAlign = Paint.Align.LEFT
                canvas.drawText(title?:"",10f, 10f + titlePaint.fontMetrics.bottom - titlePaint.fontMetrics.ascent, titlePaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    open fun updateData(list:List<Any>){
        data = list.map {
            it.toJson()
        }
        onDataUpdate(data)
        invalidate()
    }

    open fun updateData(jsonString:String){
        val json = JsonParser.parseString(jsonString)
        if(json.isJsonArray){
            data = json.asJsonArray.map {
                it.asJsonObject
            }
        }else if(json.isJsonObject){
            data = listOf(json.asJsonObject)
        }else{
            Log.e(LOG_TAG, "不支持的JSON数据格式 $jsonString")
        }
        onDataUpdate(data)
        invalidate()
    }


    /**
     * 数据更新后的回调，主要用于重置动画参数
     */
    open fun onDataUpdate(newData:List<JsonObject>){

    }


    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(data.isEmpty()){
            return false
        }
        if (event != null) {
            onTouchEvent(event)
        }
        return true
    }

    /**
     * 触摸事件反馈
     */
    fun onFocused(obj:JsonObject?){
        listener?.invoke(obj?.toJsonString())
    }

//    fun setJsonDataFocusedListener(listener: (JsonObject?) -> Unit){
//        this.jsonListener = listener
//    }

    // 处理触控事件冲突 https://blog.csdn.net/qq_38547512/article/details/90479862
    fun attemptClaimDrag() {
        this.parent?.requestDisallowInterceptTouchEvent(true)
    }

    fun isDarkTheme(context: Context): Boolean {
        val flag = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return flag == Configuration.UI_MODE_NIGHT_YES
    }
}