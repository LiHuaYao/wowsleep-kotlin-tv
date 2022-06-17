package com.idianwoo.tv.widget

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
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
class AmountView (context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), View.OnClickListener, TextWatcher {
    var amount = 1 //购买数量
    private var goodsStorage = 10 //商品库存

    private var mListener: OnAmountChangeListener? = null

    private val etAmount: TextView
    private val btnDecrease: Button
    private val btnIncrease: Button

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_amount, this)
        if (!isInEditMode) {
            ScreenAdapterTools.getInstance().loadView(view)
        }

        etAmount = findViewById<View>(R.id.tv_amount) as TextView
        btnDecrease = findViewById<View>(R.id.btn_decrease) as Button
        btnIncrease = findViewById<View>(R.id.btn_increase) as Button
        btnDecrease.setOnClickListener(this)
        btnIncrease.setOnClickListener(this)
        etAmount.addTextChangedListener(this)

        val iconfont = Typeface.createFromAsset(context.assets, "iconfont/iconfont.ttf")
        btnIncrease.typeface = iconfont
        btnDecrease.typeface = iconfont

    }

    fun setOnAmountChangeListener(onAmountChangeListener: OnAmountChangeListener) {
        this.mListener = onAmountChangeListener
    }

    fun setGoodsStorage(goods_storage: Int) {
        this.goodsStorage = goods_storage
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_decrease) {
            if (amount > 1) {
                amount--
                etAmount.text = amount.toString()
            }
        } else if (i == R.id.btn_increase) {
            if (amount < goodsStorage) {
                amount++
                etAmount.text = amount.toString()
            }
        }

        etAmount.clearFocus()

        mListener?.onAmountChange(this, amount)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        if (s.toString().isEmpty())
            return
        amount = Integer.valueOf(s.toString())
        if (amount > goodsStorage) {
            etAmount.text = goodsStorage.toString()
            return
        }

        if (mListener != null) {
            mListener!!.onAmountChange(this, amount)
        }
    }

    interface OnAmountChangeListener {
        fun onAmountChange(view: View, amount: Int)
    }

    companion object {
        private val TAG = "AmountView"
    }
}
