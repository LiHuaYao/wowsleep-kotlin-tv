package com.idianwoo.tv.steward2.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.method.TextKeyListener.clear
import android.view.Gravity
import android.view.View
import com.idianwoo.tv.steward2.adapter.GoodsAdapter
import com.idianwoo.tv.steward2.adapter.GoodsTypeAdapter
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.steward2.ShoppingCartGoods
import com.idianwoo.tv.utils.MacTools
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.BorderEffect
import com.idianwoo.tv.widget.BorderView
import com.idianwoo.tv.widget.MyDialog
import com.yatoooon.screenadaptation.ScreenAdapterTools
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.app.PendingIntent
import android.content.Context
import android.util.Log


class GoodsActivity : AppCompatActivity(), MultiItemTypeAdapter.OnItemClickListener, ViewTreeObserver.OnGlobalFocusChangeListener, View.OnClickListener {

    private lateinit var goodsItemBorder: BorderView
    private lateinit var effect: BorderEffect

    private lateinit var goodsTypeRecyclerView: RecyclerView
    private lateinit var goodsRecyclerView: RecyclerView
    private var goodsTypeData: MutableList<MutableMap<String, Any>> = mutableListOf()
    private var goodsData: MutableList<MutableMap<String, Any>> = mutableListOf()

    private lateinit var shoppingCartButton: View
    private lateinit var cartAmountTextView: TextView

    private var nowTypeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods)
        ScreenAdapterTools.getInstance().loadView(window.decorView)

        MyHttp.getToken(this).then { json, _, _ ->
            runOnUiThread {
                findViewById<TextView>(R.id.version_name).text = MyApplication.instance.versionName?:""

                JSON.parseObject(json).getJSONObject("data").let {
                    findViewById<TextView>(R.id.room_name).text = (it["room_name"] as String?)?:getString(R.string.unbind_room)
                    findViewById<TextView>(R.id.hotel_name).text = (it["hotel_name"] as String?)?:getString(R.string.unbind_hotel)
                }

                if(MyApplication.instance.error != null){
                    if(MyApplication.instance.error == getString(R.string.please_bind_hotel)){
                        Handler().postDelayed({
                            showRestartDialog()
                        }, 3000)
                    }
                    return@runOnUiThread
                }

                initTypeList()
                initGoodsList()
            }

            MyHttp.get("").then { json2, _, _ ->
                try {
                    val data = JSON.parseObject(json2).getJSONArray("data")
                    if(data.toString() == getString(R.string.please_login)) return@then

                    runOnUiThread {
                        MyApplication.instance.shoppingCart.clear()
                        for (i in 0 until data.size) {
                            val goodsMap = data.getJSONObject(i).toMutableMap()
                            val shoppingCartGoods = ShoppingCartGoods(goodsMap, goodsMap["num"].toString().toInt(), goodsMap["status"] == "1")
                            MyApplication.instance.shoppingCart.add(shoppingCartGoods)
                        }
                        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()
                    }
                } catch (e: Exception) { }
            }
        }

        effect = BorderEffect()
        effect.margin = 16
        goodsItemBorder = BorderView(this, effect)
        goodsItemBorder.setBackgroundResource(R.drawable.focus)
        goodsItemBorder.attachTo(findViewById(R.id.shopping_cart_wrapper))

        goodsRecyclerView = findViewById(R.id.goods)
        goodsTypeRecyclerView = findViewById(R.id.goods_type)
        goodsItemBorder.attachTo(goodsRecyclerView)
        goodsItemBorder.attachTo(goodsTypeRecyclerView)

        shoppingCartButton = findViewById(R.id.shopping_cart)
        shoppingCartButton.setOnClickListener(this)

        cartAmountTextView = findViewById(R.id.shopping_cart_amount)
        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()

        cartAmountTextView = findViewById(R.id.shopping_cart_amount)
    }
    /**
     * 初始化商品类型列表
     */
    private fun initTypeList() {
        val goodsTypeAdapter = GoodsTypeAdapter(this, R.layout.item_goods_type, goodsTypeData)
        //监听点击事件
        goodsTypeAdapter.setOnItemClickListener(this)

        goodsTypeRecyclerView.adapter = goodsTypeAdapter
        MyHttp.get("").then { json, _, _ ->
            val data = JSON.parseObject(json).getJSONArray("data")
            runOnUiThread {
                for (i in 0 until data.size) {
                    goodsTypeData.add(data.getJSONObject(i).toMutableMap())
                }
                updateGoodsTypeList()
            }
        }
        val viewTreeObserver = goodsTypeRecyclerView.viewTreeObserver
        viewTreeObserver.addOnGlobalFocusChangeListener(this)
    }

    override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
        if (newFocus != null && (newFocus.parent as ViewGroup).id == R.id.goods_type) {
            nowTypeId = newFocus.findViewById<TextView>(R.id.item_id).text.toString().toInt()
            initGoodsList()
        }

        if ((oldFocus?.parent as ViewGroup?)?.id == R.id.goods_type && (newFocus?.parent as ViewGroup?)?.id != R.id.goods_type) {
            oldFocus?.isSelected = true
            for (i in 0 until goodsTypeRecyclerView.childCount) {
                val t = goodsTypeRecyclerView.getChildAt(i)
                if (t != oldFocus) t.isFocusable = false
            }
        }

        if ((newFocus?.parent as ViewGroup?)?.id == R.id.goods_type  && (oldFocus?.parent as ViewGroup?)?.id != R.id.goods_type) {
            for (i in 0 until goodsTypeRecyclerView.childCount) {
                val t = goodsTypeRecyclerView.getChildAt(i)
                if (t?.isSelected == true) t.isSelected = false
                t.isFocusable = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()
        MyHttp.get("").then { json, _, _ ->
            try {
                val data = JSON.parseObject(json).getJSONArray("data")
                runOnUiThread {
                    MyApplication.instance.shoppingCart.clear()
                    for (i in 0 until data.size) {
                        val goodsMap = data.getJSONObject(i).toMutableMap()
                        val shoppingCartGoods = ShoppingCartGoods(goodsMap, goodsMap["num"].toString().toInt(), goodsMap["status"] == "1")
                        MyApplication.instance.shoppingCart.add(shoppingCartGoods)
                    }
                    cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()
                }
            } catch (e: Exception) { }
        }
    }
    /**
     * 初始化商品列表
     */
    private fun initGoodsList(){
        val goodsAdapter = GoodsAdapter(this, R.layout.item_goods, goodsData)
        goodsAdapter.setOnItemClickListener(this)

        goodsRecyclerView.adapter = goodsAdapter
        goodsRecyclerView.layoutManager = GridLayoutManager(this, 4)

        updateGoodsList()
    }

    private fun updateGoodsList(){
        if (goodsRecyclerView.adapter == null) return

        val map = mapOf("cid" to nowTypeId.toString(), "type" to 2, "page" to 1)
        MyHttp.post("", JSON.toJSONString(map)).then { json, _, _ ->
            val data = JSON.parseObject(json).getJSONArray("data")
            runOnUiThread {
                goodsData.clear()
                for (i in 0 until data.size) {
                    goodsData.add(data.getJSONObject(i).toMutableMap())
                }
                goodsRecyclerView.adapter.notifyDataSetChanged() //更新列表
            }
        }
    }
    /**
     * 当商品分类数据变动时调用
     */
    private fun updateGoodsTypeList() {
        goodsTypeRecyclerView.adapter.notifyDataSetChanged() //更新列表
        Handler().postDelayed({
            val t = goodsTypeRecyclerView.getChildAt(0)
            if(t !== null){
                nowTypeId = t.findViewById<TextView>(R.id.item_id).text.toString().toInt()
                initGoodsList()
            }
        }, 1000)
    }

    override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
        return false
    }

    override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
        val v = (view?.parent as View)
        when(v?.id) {
            R.id.goods_type -> {
                for (i in 0 until goodsTypeRecyclerView.childCount) {
                    val t = goodsTypeRecyclerView.getChildAt(i)
                    if(!t.isFocusable) t.isFocusable = true

                    if(i == position){
                        t.requestFocus()
                        t.isSelected = true
                        nowTypeId = t.findViewById<TextView>(R.id.item_id).text.toString().toInt()
                    }else{
                        t.isSelected = false
                    }
                }
                initGoodsList()
            }
            R.id.goods -> {
                try {
                    val intent = Intent(this, GoodsDetailsActivity::class.java)
                    intent.putExtra("goods", JSON.toJSONString(goodsData[position]))
                    startActivity(intent)
                } catch (e: Exception){}
            }
        }
    }

    override fun onClick(view: View?) {
        if(MyApplication.instance.error != null) return

        when(view?.id) {
            R.id.shopping_cart -> {
                val intent = Intent(this, ShoppingCartActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showRestartDialog() {
        val dialog = MyDialog(this)
        val view = View.inflate(this, R.layout.dialog_restart, null)
        ScreenAdapterTools.getInstance().loadView(view)
        dialog.setContentView(view)
        val window = dialog.window
        val lp = window.attributes
        lp.gravity = Gravity.CENTER
        lp.width = ScreenAdapterTools.getInstance().loadCustomAttrValue(900)//宽高可设置具体大小
        window.attributes = lp

        val button = dialog.findViewById<Button>(R.id.submit)
        button.setOnClickListener {
            dialog.dismiss()
            //重启应用
            val intent = packageManager.getLaunchIntentForPackage(application.packageName)
            val restartIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
            val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent) // 1秒钟后重启应用
            System.exit(0)
        }
        dialog.show()
    }
}
