package com.idianwoo.tv.steward2.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.steward2.ShoppingCartGoods
import com.idianwoo.tv.steward2.adapter.ShoppingCartAdapter
import com.idianwoo.tv.utils.MacTools
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.BorderEffect
import com.idianwoo.tv.widget.BorderView
import com.yatoooon.screenadaptation.ScreenAdapterTools

class ShoppingCartActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var selectAllCheckBox: AppCompatCheckBox
    lateinit var goodsCntTextView: TextView
    lateinit var priceSumTextView: TextView

    private lateinit var goodsItemBorder: BorderView
    private lateinit var effect: BorderEffect

    private lateinit var goodsRecyclerView: RecyclerView
    private lateinit var goodsData: MutableList<ShoppingCartGoods>

    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)
        ScreenAdapterTools.getInstance().loadView(window.decorView)

        MyHttp.getToken(this).then { json, _, _ ->
            runOnUiThread {
                findViewById<TextView>(R.id.version_name).text = MyApplication.instance.versionName?:""

                JSON.parseObject(json).getJSONObject("data").let {
                    findViewById<TextView>(R.id.room_name).text = (it["room_name"] as String?)?:getString(R.string.unbind_room)
                    findViewById<TextView>(R.id.hotel_name).text = (it["hotel_name"] as String?)?:getString(R.string.unbind_hotel)
                }

                if(MyApplication.instance.error != null) return@runOnUiThread
            }
        }

        initGoodsList()

        selectAllCheckBox = findViewById(R.id.select_all)
        selectAllCheckBox.nextFocusDownId = R.id.submit_order
        selectAllCheckBox.isChecked = true
        selectAllCheckBox.setOnClickListener { _ ->
            goodsData.forEach { it2 ->
                it2.checked = selectAllCheckBox.isChecked
            }
            goodsRecyclerView.adapter.notifyDataSetChanged()
            MyHttp.get("/api.php?ac=index_isSelectAllPro&type=${if(!selectAllCheckBox.isChecked) "1" else "3"}")
        }
        selectAllCheckBox.requestFocus()

        goodsCntTextView = findViewById(R.id.sum)
        goodsCntTextView.text = "0"
        priceSumTextView = findViewById(R.id.total)
        priceSumTextView.text = "0.00"

        effect = BorderEffect()
        effect.margin = 16
        effect.scale = 1F
        goodsItemBorder = BorderView(this, effect)
        goodsItemBorder.setBackgroundResource(R.drawable.focus)
        goodsItemBorder.attachTo(findViewById(R.id.wrapper))

        submitButton = findViewById(R.id.submit_order)
        submitButton.setOnClickListener(this)

        updateDataList()
    }
    /**
     * 初始化商品列表
     */
    private fun initGoodsList() {
        goodsRecyclerView = findViewById(R.id.goods)
        goodsData = MyApplication.instance.shoppingCart
        //goodsAdapter.setOnItemClickListener(this)
        goodsRecyclerView.adapter = ShoppingCartAdapter(this, R.layout.item_shopping_cart, goodsData)
        goodsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun updateDataList() {
        var flag = true
        var cnt = 0
        var sum = 0F
        goodsData.forEach {
            if(!it.checked) {
                selectAllCheckBox.isChecked = false
                flag = false
            } else {
                try {
                    sum += it.goods?.get("exprice").toString().toFloat() * it.amount
                } catch (e: NumberFormatException) { }
                cnt += it.amount
            }
        }
        goodsCntTextView.text = cnt.toString()
        priceSumTextView.text = String.format("%.2f", sum)
        selectAllCheckBox.isChecked = flag
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.submit_order -> {
                if(goodsCntTextView.text.toString().toInt() > 0){
                    val intent = Intent(this, PayActivity::class.java)
                    val goodsList = MyApplication.instance.shoppingCart.filter { it.checked }
                    intent.putExtra("goodsList", JSON.toJSONString(goodsList))
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "您还没有添加任何商品", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
