package com.idianwoo.tv.steward2.activity

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import com.idianwoo.tv.utils.MacTools
import com.idianwoo.tv.utils.MyHttp
import com.idianwoo.tv.utils.debug
import com.idianwoo.tv.widget.BorderEffect
import com.idianwoo.tv.widget.BorderView
import com.yatoooon.screenadaptation.ScreenAdapterTools

class LoginActivity : AppCompatActivity() {

    private lateinit var border: BorderView
    //private lateinit var mobileEditText: EditText
    //private lateinit var codeEditText: EditText
    //private lateinit var sendCodeButton: Button
    //private lateinit var submitButton: Button
    private lateinit var qrCodeView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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

//        mobileEditText = findViewById(R.id.mobile_edit)
//        codeEditText = findViewById(R.id.captche_et)
//
//        sendCodeButton = findViewById(R.id.send_code)
//        submitButton = findViewById(R.id.submit)

        border = BorderView(this)
        val effect = BorderEffect()
        effect.margin = 12
        effect.scale = 1F
        border.effect = effect
        border.setBackgroundResource(R.drawable.focus)
//        border.attachTo(findViewById(R.id.send_code_wrapper))
//        border.attachTo(findViewById(R.id.submit_wrapper))

//        sendCodeButton.setOnClickListener {
//            "isClickable false".debug()
//            sendCodeButton.text = "稍候重发"
//            sendCodeButton.isClickable = false
//            sendCodeButton.setBackgroundResource(R.drawable.button_gray)
//            Handler().postDelayed({
//                "isClickable true".debug()
//                sendCodeButton.setBackgroundResource(R.drawable.button_green)
//                sendCodeButton.text = "获取验证码"
//                sendCodeButton.isClickable = true
//            }, 600000)
//            sendSm()
//        }
//        submitButton.setOnClickListener {
//
//            submit()
//        }
        qrCodeView = findViewById(R.id.qrcode_view)
//        qrCodeView.settings.useWideViewPort = true
//        qrCodeView.settings.loadWithOverviewMode = true

        MyHttp.get("").then { json, _, _ ->
            val jsonObj = JSON.parseObject(json)
            if(jsonObj.getString("msg").toLowerCase() == "success") {
                runOnUiThread {
                    qrCodeView.loadUrl(jsonObj.getString("data"))
                    qrCodeView.settings.javaScriptEnabled = true
                    qrCodeView.settings.setAppCacheEnabled(false)
                    qrCodeView.addJavascriptInterface(JavaScriptInterface(this), "klsmmpsObject")
                    qrCodeView.webViewClient = WebViewClient()
                }
            }
        }
    }


//    private fun sendSm() {
//
//        val mobile = mobileEditText.text.toString()
//        val mobileJson = JSON.toJSONString(mapOf("mobile" to mobile))
//        MyHttp.post("", mobileJson).then { json, _, _ ->
//            runOnUiThread {
//                val jsonObj = JSON.parseObject(json)
//                val data = jsonObj.getString("data")
//                Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
//                data.debug()
//            }
//        }
//    }


//    private fun submit() {
//        val mobile = mobileEditText.text.toString()
//        val code = codeEditText.text.toString()
//        val submitJson = JSON.toJSONString(mapOf("mobile" to mobile, "code" to code))
//        MyHttp.post("", submitJson).then { json, _, _ ->
//            val jsonObj = JSON.parseObject(json)
//            if (jsonObj.getString("msg") != "success") {
//                runOnUiThread { Toast.makeText(this, jsonObj.getString("data"), Toast.LENGTH_LONG).show() }
//            } else {
//                runOnUiThread {
//                    val data = jsonObj.getJSONObject("data")
//                    MyHttp.token = data.getString("token")
//                    MyHttp.isLogin = data.getString("isLogin") == "1"
//                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//        }
//    }
}

private class JavaScriptInterface(var context: Activity) {

    /**
     * 与js交互时用到的方法，在js里直接调用的
     */
    @JavascriptInterface
    fun syncToken(token: String) {
        MyHttp.token = token
        Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
        context.finish()
    }
}
