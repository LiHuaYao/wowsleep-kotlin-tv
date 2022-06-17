package com.idianwoo.tv.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.idianwoo.tv.steward2.R

class GoodsOptionsView (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs){

    private lateinit var container: ViewGroup
    private lateinit var name: TextView

    private var data = "{\"name\":\"尺寸：\"," +
                            "\"options\":[" +
                                "{\"id\":\"1\",\"name\":\"套餐A\"}" +
                                "{\"id\":\"2\",\"name\":\"套餐BBBBBBBBBBBB\"}" +
                                "{\"id\":\"3\",\"name\":\"套餐CCCCCC\"}" +
                                "{\"id\":\"4\",\"name\":\"套餐DDD\"}" +
                            "]}"

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_goods_options, this)

        container = findViewById(R.id.options)
        name = findViewById(R.id.name)

        if(this.data != null && this.data != ""){
            val data = JSON.parseObject(this.data)
            this.name.setText(data.getString("name"))
            var options = data.getJSONArray("options")
            for (i in 0 until options.size) {
                val tv = TextView(context)
                val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50)
                params.setMargins(5, 5, 5, 5)
                tv.layoutParams = params

                //边框颜色
                tv.setBackgroundResource(R.drawable.goods_options_background);
                //字体大小
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.toFloat());
                //字体颜色
                tv.setTextColor(getResources().getColor(R.color.goods_options));
                //获取焦点
                tv.isFocusable = true
                //居中
                tv.setGravity(Gravity.CENTER);
                //填充
                tv.setPadding(30, 0, 30, 0);
                //最大宽度
                tv.setMaxWidth(200)
                //文字轮播
                tv.setEllipsize(TextUtils.TruncateAt.MARQUEE)
                //单行
                tv.setSingleLine(true)

                val data2 = options.getJSONObject(i)
                tv.setText(data2.getString("name"))

                container.addView(tv)// 将TextView 添加到container中
            }
        }
    }
}


