package com.idianwoo.tv.steward2.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.steward2.R.id.goods
import com.idianwoo.tv.steward2.ShoppingCartGoods
import com.idianwoo.tv.utils.MacTools
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.AmountView
import com.idianwoo.tv.widget.BorderEffect
import com.idianwoo.tv.widget.BorderView
import com.yatoooon.screenadaptation.ScreenAdapterTools
import okhttp3.FormBody

class GoodsDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var goodsItemBorder: BorderView
    private lateinit var effect: BorderEffect

    private lateinit var mAmountView: AmountView
    private lateinit var btnDecrease: Button
    private lateinit var btnIncrease: Button
    //private lateinit var tvDesc: TextView

    private lateinit var goodsDetailsCart: ImageView
    private lateinit var addCartButton: Button
    private lateinit var submitOrderButton: Button
    private lateinit var cartAmountTextView: TextView

    private lateinit var nameTextView: TextView
    private lateinit var smallmemoTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var likesNumTextView: TextView
    private lateinit var storedNumTextView: TextView
    private lateinit var goodsDetailsQuota: TextView
    private lateinit var goodsDetailsSellTime: TextView

    private lateinit var likeButton: ImageView

    private lateinit var goods: MutableMap<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goods_details)
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

        mAmountView = findViewById(R.id.amount_view)

        effect = BorderEffect()
        effect.margin = 16
        effect.scale = 1F
        goodsItemBorder = BorderView(this, effect)
        goodsItemBorder.setBackgroundResource(R.drawable.focus)
        goodsItemBorder.attachTo(mAmountView.findViewById(R.id.root))
        goodsItemBorder.attachTo(findViewById(R.id.info1))
        goodsItemBorder.attachTo(findViewById(R.id.info2))
        goodsItemBorder.attachTo(findViewById(R.id.button_group))
        goodsItemBorder.attachTo(findViewById(R.id.goods_details_frame))

        btnDecrease = findViewById(R.id.btn_decrease)
        btnIncrease = findViewById(R.id.btn_increase)

        val iconfont = MyApplication.instance.iconfont
        btnIncrease.typeface = iconfont
        btnDecrease.typeface = iconfont

        goods = JSON.parseObject(intent.getSerializableExtra("goods").toString()).toMutableMap()

        val webView = findViewById<WebView>(R.id.goods_details_desc)
        val html = """
            ${goods["content"]}
            <style>
                body{padding-top: 20px!important;}
                img {max-width: 100%!important;min-width: 100%!important;}
            </style>
        """.trimIndent()

        webView.loadDataWithBaseURL(MyHttp.SERVER_ADDRESS, html, "text/html", "UTF-8", null)
        webView.settings.textZoom = 150

        goodsDetailsCart = findViewById(R.id.goods_details_cart)
        goodsDetailsCart.setOnClickListener(this)
        addCartButton = findViewById(R.id.add_cart)
        addCartButton.setOnClickListener(this)
        submitOrderButton = findViewById(R.id.submit_order)
        submitOrderButton.setOnClickListener(this)

        cartAmountTextView = findViewById(R.id.shopping_cart_amount)
        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()

        nameTextView = findViewById(R.id.goods_details_name)
        nameTextView.text = goods["title"] as String

        smallmemoTextView = findViewById(R.id.goods_details_smallmemo)
        smallmemoTextView.text = goods["smallmemo"] as String

        priceTextView = findViewById(R.id.goods_details_free)
        priceTextView.text = goods["exprice"] as String

        likesNumTextView = findViewById(R.id.goods_details_likes2_num)
        likesNumTextView.text = goods["zan"].toString()

        storedNumTextView = findViewById(R.id.goods_details_stored_num)
        storedNumTextView.text = goods["kucun"].toString()

        goodsDetailsQuota = findViewById(R.id.goods_details_quota)
        var quota = ""
        if(goods["room_quota"].toString() != "0"){//房间限购
            quota = quota.plus("每房${goods["room_quota"]}${goods["unit"]}")
        }
        if(goods["person_quota"].toString() != "0"){//个人限购
            if(quota.isNotEmpty()) quota = quota.plus("，")

            quota = quota.plus("每人${goods["room_quota"]}${goods["unit"]}")
        }
        if(quota.isNotEmpty()){
            goodsDetailsQuota.visibility = View.VISIBLE
            goodsDetailsQuota.text = "限购：$quota"
        }

        goodsDetailsSellTime = findViewById(R.id.goods_details_sell_time)
        goodsDetailsSellTime.text = "供应：${goods["time"].toString()}"

        likeButton = findViewById(R.id.goods_details_likes)
        likeButton.setOnClickListener(this)

        MyHttp.get("").then { json, _, _ ->
            val jsonObj = JSONObject.parseObject(json)
            if (jsonObj.getString("msg") == "1") {
                runOnUiThread {
                    likeButton.setBackgroundResource(R.drawable.likes2)
                }
            } else {
                runOnUiThread {
                    likeButton.setBackgroundResource(R.drawable.likes)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.goods_details_cart -> {
                val intent = Intent(this, ShoppingCartActivity::class.java)
                startActivity(intent)
            }
            R.id.add_cart -> {
                val goodsMap = mutableMapOf<String, Any>()
                goodsMap["photo"] = (goods["photo"] as String).replace("https://wxgj.klsmmps.com", "")
                goodsMap["pid"] = goods["id"]!!
                goodsMap["title"] = goods["title"]!!
                goodsMap["smallmemo"] = goods["smallmemo"]!!
                goodsMap["exprice"] = goods["exprice"]!!

                MyApplication.instance.addGoodsToCart(ShoppingCartGoods(goodsMap, mAmountView.amount)) {
                    runOnUiThread {
                        Toast.makeText(this, "成功加入购物车", Toast.LENGTH_SHORT).show()
                        cartAmountTextView.text = MyApplication.instance.shoppingCartCnt().toString()
                    }
                }
            }
            R.id.submit_order -> {
                val intent = Intent(this, PayActivity::class.java)
                val goodsList = listOf(ShoppingCartGoods(goods, mAmountView.amount))
                intent.putExtra("goodsList", JSON.toJSONString(goodsList))
                intent.putExtra("submitDirectly", true)
                startActivity(intent)
            }
            R.id.goods_details_likes -> {
                MyHttp.get("").then { json, _, _ ->
                    val jsonObj = JSON.parseObject(json)
                    if(jsonObj.getString("data") == getString(R.string.please_login)) {
                        runOnUiThread {
                            Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        if(jsonObj.getIntValue("code") == 1) {
                            runOnUiThread {
                                likeButton.setBackgroundResource(R.drawable.likes2)
                                likesNumTextView.text =  jsonObj.getString("data")
                                //likesNumTextView.text = (likesNumTextView.text.toString().toInt()+1).toString()
                            }
                        } else if(jsonObj.getIntValue("code") == 0) {
                            runOnUiThread {
                                likeButton.setBackgroundResource(R.drawable.likes)
                                likesNumTextView.text =  jsonObj.getString("data")
                                //likesNumTextView.text =  (likesNumTextView.text.toString().toInt()-1).toString()
                            }
                        } else{
                            runOnUiThread {
                                Toast.makeText(this, "服务器好像开小差了", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

