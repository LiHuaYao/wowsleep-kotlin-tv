package com.idianwoo.tv.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.idianwoo.tv.steward2.R
import com.yatoooon.screenadaptation.ScreenAdapterTools


/**
 * 自定义组件：购买数量，带减少增加按钮
 * Created by hiwhitley on 2016/7/4.
 */
class AmountView2 (context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), View.OnClickListener {
    var amount = 1 //购买数量
        set(value) {
            etAmount.text = value.toString()
            field = value
        }
    var goodsStorage = 10 //商品库存

    private val etAmount: TextView
    private val btnDecrease: Button
    private val btnIncrease: Button

    private var amountChangeListener: ((Int, Int) -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_amount2, this)
        if (!isInEditMode) ScreenAdapterTools.getInstance().loadView(view)

        etAmount = findViewById(R.id.tv_amount)
        btnDecrease = findViewById(R.id.btn_decrease)
        btnIncrease = findViewById(R.id.btn_increase)
        btnDecrease.setOnClickListener(this)
        btnIncrease.setOnClickListener(this)
        val iconfont = Typeface.createFromAsset(context.assets, "iconfont/iconfont.ttf")
        btnIncrease.typeface = iconfont
        btnDecrease.typeface = iconfont

    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btn_decrease -> {
                if (amount > 1) {
                    amount--
                    etAmount.text = amount.toString()
                    amountChangeListener?.invoke(amount, 1)
                }
            }
            R.id.btn_increase -> {
                if (amount < goodsStorage) {
                    amount++
                    etAmount.text = amount.toString()
                    amountChangeListener?.invoke(amount, 2)
                }
            }
        }
    }

    fun setAmountChangeListener (listener: ((Int, Int) -> Unit)?) {
        amountChangeListener = listener
    }

    companion object {
        private val TAG = "AmountView"
    }
}
