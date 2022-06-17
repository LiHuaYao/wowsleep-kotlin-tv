package com.idianwoo.tv.steward2.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.view.View
import com.alibaba.fastjson.JSON
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.steward2.ShoppingCartGoods
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.BorderEffect
import com.idianwoo.tv.widget.BorderView
import com.yatoooon.screenadaptation.ScreenAdapterTools
import android.R.attr.data
import android.support.v4.content.ContextCompat.startActivity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.alibaba.fastjson.JSONObject
import com.facebook.drawee.view.SimpleDraweeView
import com.idianwoo.tv.utils.MacTools
import com.idianwoo.tv.utils.QRCodeTools
import com.idianwoo.tv.widget.GoodsOptionsView
import com.idianwoo.tv.widget.MyDialog
import kotlinx.android.synthetic.main.item_shopping_cart.view.*


class PayActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var borderView: BorderView
    private lateinit var effect: BorderEffect

    private lateinit var rightContent: LinearLayout

    private lateinit var wechatPayButton: View
    private lateinit var cashPayButton: View
    private lateinit var chargeUpPayButton: View
    private lateinit var freePayButton: View

    private lateinit var wechatPayCheckBox: AppCompatCheckBox
    private lateinit var cashPayCheckBox: AppCompatCheckBox
    private lateinit var chargeUpPayCheckBox: AppCompatCheckBox
    private lateinit var freePayCheckBox: AppCompatCheckBox

    private lateinit var priceSumTextView: TextView

    private lateinit var submitButton: Button

    private lateinit var goodsList: List<ShoppingCartGoods>

    private var submitDirectly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)
        ScreenAdapterTools.getInstance().loadView(window.decorView)

        MyHttp.getToken(this).then { json, _, _ ->
            runOnUiThread {
                findViewById<TextView>(R.id.version_name).text = MyApplication.instance.versionName?:""

                JSON.parseObject(json).getJSONObject("data").let {
                    findViewById<TextView>(R.id.room_name).text = (it["room_name"] as String?)?:getString(R.string.unbind_room)
                    findViewById<TextView>(R.id.hotel_name).text = (it["hotel_name"] as String?)?:getString(R.string.unbind_hotel)
                }

                if(MyApplication.instance.error != null)  return@runOnUiThread
            }
        }

        effect = BorderEffect()
        effect.margin = 16
        effect.scale = 1F
        borderView = BorderView(this, effect)
        borderView.setBackgroundResource(R.drawable.focus)
        borderView.attachTo(findViewById(R.id.left_content))
        borderView.attachTo(findViewById(R.id.center_content))
        rightContent = findViewById(R.id.right_content)
        borderView.attachTo(rightContent)

        wechatPayButton = findViewById(R.id.wechat_pay)
        wechatPayButton.setOnClickListener(this)
        cashPayButton = findViewById(R.id.cash_pay)
        cashPayButton.setOnClickListener(this)
        chargeUpPayButton = findViewById(R.id.charge_up_pay)
        chargeUpPayButton.setOnClickListener(this)

        cashPayCheckBox = findViewById(R.id.cash_pay_check_box)
        cashPayCheckBox.isChecked = true

        chargeUpPayCheckBox = findViewById(R.id.charge_up_pay_check_box)
        wechatPayCheckBox = findViewById(R.id.wechat_pay_check_box)

        freePayButton = findViewById(R.id.free_pay)
        freePayCheckBox = findViewById(R.id.free_pay_check_box)

        submitButton = findViewById(R.id.submit)
        submitButton.setOnClickListener(this)

        initData()
    }

    private fun initData() {
        submitDirectly = intent.getBooleanExtra("submitDirectly", false)
        goodsList = JSON.parseArray(intent.getSerializableExtra("goodsList").toString(), ShoppingCartGoods::class.java)
        var sumPrice = 0F
        goodsList.forEachIndexed { index, shoppingCartGoods ->
            val view = View.inflate(this, R.layout.item_pay_goods, null)
            ScreenAdapterTools.getInstance().loadView(view)
            view.findViewById<TextView>(R.id.name).text = shoppingCartGoods.goods?.get("title").toString()
            view.findViewById<TextView>(R.id.num).text = shoppingCartGoods.amount.toString()
            var price = 0F
            try {
                price = shoppingCartGoods.goods?.get("exprice").toString().toFloat() * shoppingCartGoods.amount
            } catch (e: Exception) { }
            view.findViewById<TextView>(R.id.price).text =  String.format("%s %s", getString(R.string.icon_cny), price.toString())
            rightContent.addView(view, index + 1)
            sumPrice += price
        }
        priceSumTextView = findViewById(R.id.price_sum)
        priceSumTextView.text = String.format("%s %.2f", getString(R.string.icon_cny), sumPrice)

        if(sumPrice.toInt() == 0){
            cashPayButton.visibility = View.GONE
            chargeUpPayButton.visibility = View.GONE
            wechatPayButton.visibility = View.GONE
            freePayButton.visibility = View.VISIBLE
            freePayCheckBox.isChecked = true
            cashPayCheckBox.isChecked = false
        }else{
            freePayButton.visibility = View.GONE
            freePayCheckBox.isChecked = false
            cashPayCheckBox.isChecked = true
            MyHttp.get("").then { json, _, _ ->
                val jsonObj = JSON.parseObject(json)
                if(jsonObj.getString("code").toLowerCase() == "success") {
                    jsonObj.getJSONArray("data").forEach { it ->
                        (it as JSONObject).let { it2 ->
                            runOnUiThread {
                                if (it2["id"] == "1") cashPayButton.visibility = View.VISIBLE
                                else if (it2["id"] == "2") chargeUpPayButton.visibility = View.VISIBLE
                                else if (it2["id"] == "3") wechatPayButton.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.wechat_pay -> {
                wechatPayCheckBox.isChecked = true
                cashPayCheckBox.isChecked = false
                chargeUpPayCheckBox.isChecked = false
            }
            R.id.cash_pay -> {
                cashPayCheckBox.isChecked = true
                wechatPayCheckBox.isChecked = false
                chargeUpPayCheckBox.isChecked = false
            }
            R.id.charge_up_pay -> {
                chargeUpPayCheckBox.isChecked = true
                wechatPayCheckBox.isChecked = false
                cashPayCheckBox.isChecked = false
            }
            R.id.submit -> {
                submitOrder()
            }
        }
    }

    private fun getPayType(): Int {
        return when {
            cashPayCheckBox.isChecked -> 1
            chargeUpPayCheckBox.isChecked -> 2
            wechatPayCheckBox.isChecked -> 3
            else -> 4
        }
    }

    fun submitOrder() {
        MyHttp.get("").then { json, _, _ ->
            val jsonObj = JSON.parseObject(json)
            if (jsonObj.getInteger("data") == 1) {
                val dialog = MyDialog(this)
                val view = View.inflate(this, R.layout.dialog_alert, null)
                ScreenAdapterTools.getInstance().loadView(view)
                dialog.setContentView(view)
                dialog.findViewById<TextView>(R.id.content).text = "当前是繁忙时间，可能需要等待较长时间，是否继续提交。"
                val window = dialog.window
                val lp = window.attributes
                lp.gravity = Gravity.CENTER
                lp.width = ScreenAdapterTools.getInstance().loadCustomAttrValue(900)//宽高可设置具体大小
                window.attributes = lp
                val button = dialog.findViewById<Button>(R.id.submit)
                button.requestFocus()
                button.setOnClickListener {
                    dialog.dismiss()
                    createOrder()
                }
                dialog.show()
            }
            else createOrder()
        }
    }

    private fun createOrder () {
        val order = mutableMapOf(
                "fanghao" to MyApplication.instance.roomName,
                "command" to MyApplication.instance.roomCommand,
                "time" to "20分钟左右",
                "paytype" to getPayType(),
                "baogao" to "0",
                "fapiao" to "0",
                "isshuidou" to "0"
        )

        if (submitDirectly) {
            order["pid"] = goodsList[0].goods!!["id"]
            order["num"] = goodsList[0].amount
            order["daynum"] = 1
        }

        MyHttp.post("", JSON.toJSONString(order)).then { json2, _, _ ->
            val jsonObj2 = JSON.parseObject(json2)
            if(jsonObj2.getString("data") == getString(R.string.please_login)) {
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.please_login), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            } else if (jsonObj2.getString("msg").toLowerCase() == "error") {
                runOnUiThread {
                    Toast.makeText(this, jsonObj2.getString("data"), Toast.LENGTH_SHORT).show()
                }
            } else {
                if(getPayType() == 3) {
                    showDialog(jsonObj2.getJSONObject("data").getString("order_id"))
                } else {
                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.successful), Toast.LENGTH_SHORT).show()
                        MyHttp.get("")
                        val intent = Intent(this, GoodsActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showDialog(orderId: String) {
        MyHttp.get("").then { json, _, _ ->
            val jsonObj = JSON.parseObject(json)
            val qrCodeBitMap = QRCodeTools.Create2DCode(jsonObj.getJSONObject("data").getString("code_url"))
            runOnUiThread {
                val dialog = MyDialog(this)
                val view = View.inflate(this, R.layout.dialog_pay_qr_code, null)
                ScreenAdapterTools.getInstance().loadView(view)

                val qrCode = view.findViewById<ImageView>(R.id.qr_code)
                qrCode.setImageBitmap(qrCodeBitMap)

                val ok = view.findViewById<Button>(R.id.ok)
                ok.requestFocus()
                ok.setOnClickListener{
                    MyHttp.get("").then { json2, _, _ ->
                        val jsonObj2 = JSON.parseObject(json2)
                        runOnUiThread {
                            if(jsonObj2.getString("msg").toLowerCase() == "success") {
                                val intent = Intent(this, GoodsActivity::class.java)
                                startActivity(intent)
                            }
                            Toast.makeText(this, jsonObj2.getString("data"), Toast.LENGTH_LONG).show()
                        }
                    }
                }

                dialog.setContentView(view)
                dialog.show()
                val window = dialog.window
                val lp = window.attributes
                lp.gravity = Gravity.CENTER
                lp.width = ScreenAdapterTools.getInstance().loadCustomAttrValue(1000)//宽度
                lp.height = ScreenAdapterTools.getInstance().loadCustomAttrValue(850)//高度
                window.attributes = lp
            }
        }
    }
}
