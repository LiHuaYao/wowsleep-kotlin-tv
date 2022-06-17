package com.idianwoo.tv.steward2.adapter

import android.support.v7.widget.AppCompatCheckBox
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.steward2.ShoppingCartGoods
import com.idianwoo.tv.steward2.activity.ShoppingCartActivity
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.AmountView2
import com.yatoooon.screenadaptation.ScreenAdapterTools
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder

class ShoppingCartAdapter(val context: ShoppingCartActivity?, layoutId: Int, val data: MutableList<ShoppingCartGoods>) : CommonAdapter<ShoppingCartGoods>(context, layoutId, data) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        ScreenAdapterTools.getInstance().loadView(viewHolder.convertView)
        return viewHolder
    }

    override fun convert(holder: ViewHolder?, v: ShoppingCartGoods, position: Int) {
        val itemHitsIcon = holder?.getView<TextView>(R.id.item_hits_icon)
        val itemKucunIcon = holder?.getView<TextView>(R.id.item_kucun_icon)
        val itemTrashIcon = holder?.getView<TextView>(R.id.item_trash)
        val btnDecrease = holder?.getView<TextView>(R.id.btn_decrease)
        val btnIncrease = holder?.getView<TextView>(R.id.btn_increase)
        val iconfont = MyApplication.instance.iconfont
        itemHitsIcon?.typeface = iconfont
        itemKucunIcon?.typeface = iconfont
        itemTrashIcon?.typeface = iconfont
        btnIncrease?.typeface = iconfont
        btnDecrease?.typeface = iconfont

        holder?.getView<SimpleDraweeView>(R.id.item_photo)?.let {
            it.setImageURI("https://wxgj.klsmmps.com${v.goods?.get("photo")}")
            val roundingParams = RoundingParams()
            roundingParams.setCornersRadii(20F, 20F, 0F, 0F)
            it.hierarchy.roundingParams = roundingParams
        }

        holder?.setText(R.id.item_title, v.goods?.get("title") as String)
        holder?.setText(R.id.item_smallmemo, v.goods?.get("smallmemo") as String)
        holder?.setText(R.id.item_exprice, v.goods?.get("exprice") as String)
        holder?.getView<AmountView2>(R.id.amount_view)?.let {
            it.amount = v.amount
            it.goodsStorage = v.goods?.get("kucun")?.toString()?.toInt()?: 1000
            it.setAmountChangeListener{amount, type ->
                v.amount = amount
                context?.updateDataList()
                MyHttp.get("").then { json, _, _ ->
                    val jsonObj = JSON.parseObject(json)
                    if (jsonObj.getString("code").toLowerCase() == "success") {
                        MyHttp.post("", "")
                    }
                }
            }
        }

        holder?.getView<Button>(R.id.item_trash)?.setOnClickListener {
            data.removeAt(position)
            notifyDataSetChanged()
            context?.updateDataList()
            MyHttp.get("")
        }

        holder?.getView<AppCompatCheckBox>(R.id.check)?.let { it ->
            it.isChecked = v.checked
            it.setOnClickListener {
                data[position].checked = !v.checked
                context?.updateDataList()
                MyHttp.get("")
            }
        }
    }
}
