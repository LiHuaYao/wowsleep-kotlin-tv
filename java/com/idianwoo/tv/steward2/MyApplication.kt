package com.idianwoo.tv.steward2

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.facebook.drawee.backends.pipeline.Fresco
import com.idianwoo.tv.steward2.R.id.goods
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.yatoooon.screenadaptation.ScreenAdapterTools
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.pm.PackageInfo
import com.idianwoo.tv.steward2.R.id.name


class MyApplication : Application() {

    val WX_APP_ID = ""

    lateinit var iconfont: Typeface
        private set

    val shoppingCart: MutableList<ShoppingCartGoods> = mutableListOf()
    var hotelName: String? = null
    var roomName: String? = null
    var error: String? = null
    var versionName: String? = null
    var mac: String? = null
    var roomCommand: String? = null
    var deviceType: String? = null

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Fresco.initialize(this)
        ScreenAdapterTools.init(this)
        iconfont = Typeface.createFromAsset(assets, "iconfont/iconfont.ttf")

        val api = WXAPIFactory.createWXAPI(this, WX_APP_ID, true)
        api.registerApp(WX_APP_ID)

        try {
            val packageManager = this.getPackageManager()
            val packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun addGoodsToCart(goods: ShoppingCartGoods, callback: ()->Unit = {}) {
        val id = goods.goods?.get("pid")
        MyHttp.get("").then { json, _, _ ->
            val jsonObj = JSON.parseObject(json)
            val data = try {
                jsonObj.getJSONObject("data").getString("isdummy")
            } catch (e: Exception) {
                jsonObj.getString("data")
            }
            if (data == "1") MyHttp.get("")
            else {
                MyHttp.post("", JSON.toJSONString(mapOf("pid" to id))).then { json2, _, _ ->
                    val jsonObj2 = JSON.parseObject(json2)
                    if (jsonObj2.getString("code").toLowerCase() == "success") {
                        MyHttp.post("", JSON.toJSONString(mapOf("pid" to id, "num" to goods.amount))).then { _, _, _ ->
                            //json3.debug()
                            shoppingCart.find { it.goods?.get("pid") == id }.let {
                                if (it == null) shoppingCart.add(goods)
                                else it.amount += goods.amount
                            }
                            callback()
                        }
                    }
                }
            }
        }
    }

    fun shoppingCartCnt(): Int {
        return shoppingCart.size
    }
}

data class ShoppingCartGoods(var goods: MutableMap<String, Any>? = null, var amount: Int = 0, var checked: Boolean = true)