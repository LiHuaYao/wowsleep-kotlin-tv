package com.idianwoo.tv.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.idianwoo.tv.steward2.R

class RatingView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private lateinit var container: LinearLayout
    private var distance: Int = 0//间距
    private var size: Int = 0//大小
    private var count: Int = 0//总数
    private var mark: Int = 0//评分
    private lateinit var empty: Drawable
    private lateinit var fill: Drawable
    private val focusAble = true

    /*
    *  初始方法
    *  获取属性值
    * */
    init {
        LayoutInflater.from(context).inflate(R.layout.widget_star, this)
        container = findViewById(R.id.rating)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Rating)
        this.distance = typedArray.getDimension(R.styleable.Rating_distance, 20f).toInt()
        this.size = typedArray.getDimension(R.styleable.Rating_size, 42f).toInt()
        this.count = typedArray.getInteger(R.styleable.Rating_count, 5)
        this.mark = typedArray.getInteger(R.styleable.Rating_mark, 4)
        this.empty = typedArray.getDrawable(R.styleable.Rating_empty)
        this.fill = typedArray.getDrawable(R.styleable.Rating_fill)
        typedArray.recycle()//释放资源

        generate(context)
    }

    /*
    * 生成方法
    * */
    private fun generate(context: Context) {
        for (j in 0 until count) {
            val tv = TextView(context)
            val params = LinearLayout.LayoutParams(this.size, this.size)
            params.setMargins(0, 0, this.distance, 0)
            tv.layoutParams = params
            if (j < this.mark) tv.background = this.fill
            else tv.background = this.empty

            tv.isFocusable = this.focusAble
            container.addView(tv)// 将TextView 添加到container中
        }
    }

}
