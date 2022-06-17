package com.idianwoo.tv.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout


import com.idianwoo.tv.steward2.R
import com.yatoooon.screenadaptation.ScreenAdapterTools

class UpDownBtnView (context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs), View.OnClickListener {

    private val btnUp: Button
    private val btnDown: Button

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_up_down_btn, this)
        if (!isInEditMode) ScreenAdapterTools.getInstance().loadView(view)

        btnUp = findViewById<View>(R.id.up_btn) as Button
        btnDown = findViewById<View>(R.id.down_btn) as Button
        btnUp.setOnClickListener(this)
        btnDown.setOnClickListener(this)

        val iconfont = Typeface.createFromAsset(context.assets, "iconfont/iconfont.ttf")
        btnUp.typeface = iconfont
        btnDown.typeface = iconfont
    }

    override fun onClick(v: View) {
        /*val i = v.id
        if (i == R.id.up_btn) {

        } else if (i == R.id.down_btn) {

        }*/
    }

    companion object {
        private val TAG = "UpDownBtnView"
    }
}
