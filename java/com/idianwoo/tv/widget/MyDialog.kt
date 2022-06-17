package com.idianwoo.tv.widget

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import com.idianwoo.tv.steward2.R


class MyDialog : Dialog {

    private lateinit var main: ViewGroup
    private lateinit var border: BorderView

    public constructor(context: Context?): this(context, R.style.Dialog) {

    }

    public constructor(context: Context?, themeResId: Int): super(context, themeResId)

    public constructor(context: Context?, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener): super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
//
//        border = BorderView(this.context)
//        val effect = BorderEffect()
//        effect.margin = 16
//        border.effect = effect
//        border.setBackgroundResource(R.drawable.focus)
//        main = findViewById(R.id.root)
//        border.attachTo(main)
    }
}