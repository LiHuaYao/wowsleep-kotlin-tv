package com.idianwoo.tv.steward2.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.widget.BorderEffect
import com.yatoooon.screenadaptation.ScreenAdapterTools
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder


class GoodsAdapter(context: Context?, layoutId: Int, data: MutableList<MutableMap<String, Any>>) : CommonAdapter<MutableMap<String, Any>>(context, layoutId, data) {

    //private val TAG = BorderEffect::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        ScreenAdapterTools.getInstance().loadView(viewHolder.convertView)
        return viewHolder
    }

    override fun convert(holder: ViewHolder?, v: MutableMap<String, Any>, position: Int) {
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
            it.setImageURI(v["photo"] as String)
            val roundingParams = RoundingParams()
            roundingParams.setCornersRadii(20F, 20F, 0F, 0F)
            it.hierarchy.roundingParams = roundingParams
        }
        holder?.setText(R.id.item_title, v["title"] as String)
        holder?.setText(R.id.item_smallmemo, v["smallmemo"] as String)
        holder?.setText(R.id.item_exprice, v["exprice"] as String)

        if(v["zan"] != null){
            holder?.setText(R.id.item_hits, v["zan"] as String)
        }

        if(v["kucun"] != null) {
            holder?.setText(R.id.item_kucun, v["kucun"] as String)
        }
    }
}
