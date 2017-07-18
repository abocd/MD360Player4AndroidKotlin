package com.xiuhu.xiuhuvrplayerandroid

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by hzqiujiadi on 2017/4/21.
 * hzqiujiadi ashqalcn@gmail.com
 */

class HoverView : View {

    private var paint: Paint? = null

    private var x1: Float = 0.toFloat()

    private var y1: Float = 0.toFloat()

    private var radius: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }

    private fun initView() {
        paint = Paint()
        paint!!.color = 0xFF0000FF.toInt()
    }

    override fun onHoverEvent(event: MotionEvent): Boolean {
        super.onHoverEvent(event)

        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_HOVER_MOVE -> {
                x1 = event.x
                y1 = event.y
                radius = ((event.eventTime - event.downTime) / 100 + 1).toFloat()
            }

            MotionEvent.ACTION_HOVER_EXIT -> radius = 0f
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (radius != 0f) {
            canvas.drawCircle(x1, y1, radius, paint!!)
        }
    }
}
