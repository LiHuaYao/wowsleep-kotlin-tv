package com.idianwoo.tv.steward2.adapter

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.widget.BorderEffect
import com.yatoooon.screenadaptation.ScreenAdapterTools
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder


class GoodsTypeAdapter(context: Context?, layoutId: Int, data: MutableList<MutableMap<String, Any>>) : CommonAdapter<MutableMap<String, Any>>(context, layoutId, data) {

    //private val TAG = BorderEffect::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        ScreenAdapterTools.getInstance().loadView(viewHolder.convertView)
        return viewHolder
    }

    override fun convert(holder: ViewHolder?, v: MutableMap<String, Any>, position: Int) {
        //Log.d(TAG, "convert==>${v["name"]}")
        holder?.setText(R.id.item_name, v["name"] as String?)
        holder?.setText(R.id.item_id, v["id"] as String?)
    }
}