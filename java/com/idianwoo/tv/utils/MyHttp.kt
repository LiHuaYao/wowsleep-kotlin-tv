package com.idianwoo.tv.utils

import android.content.Context
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.idianwoo.tv.steward2.MyApplication
import com.idianwoo.tv.steward2.R
import okhttp3.*
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MyHttp {

    companion object{
        const val SERVER_ADDRESS = ""
        //创建okHttpClient对象
        private val okHttpClient = OkHttpClient()
        var token: String? = null
        var isLogin = false
        private var tokenHttp: MyHttp? = null

        fun getToken(context: Context): MyHttp {
            if(tokenHttp != null) return tokenHttp!!
            else tokenHttp = MyHttp()

            val mac = MacTools.getMac(context)
            MyApplication.instance.mac = mac
            val requestData = mapOf("mac" to mac, "info" to mapOf<String, Any>())
            val request = Request.Builder()
                    .url("$SERVER_ADDRESS")
                    .addHeader("Accept", "application/json;charset=utf-8")
                    .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(requestData)))
                    .build()

            val call = okHttpClient.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    val map = mapOf(Pair("success", false), Pair("message", e?.message))
                    tokenHttp?.errorList?.forEach{ it(JSON.toJSONString(map), call, e) }
                    tokenHttp = null
                }
                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        val result = response.body()?.string()
                        val res = JSON.parseObject(result)
                        if (res["msg"].toString().toLowerCase() == "success") {
                            MyApplication.instance.error = null

                            if (token.isNullOrBlank()) {
                                val data = res.getJSONObject("data")
                                token = data.getString("token")
                                isLogin = data.getString("isLogin") == "1"
                            }

                            tokenHttp?.thenList?.forEach{ it(result?: "", call, response) }
                            res.getJSONObject("data").let {
                                MyApplication.instance.roomName = (it["room_name"] as String?)?:context.getString(R.string.unbind_room)
                                MyApplication.instance.hotelName = (it["hotel_name"] as String?)?:context.getString(R.string.unbind_hotel)
                                MyApplication.instance.roomCommand = (it["room_command"] as String?)?:null
                                MyApplication.instance.deviceType = (it["device_type"] as String?)?:null
                            }
                            tokenHttp = null
                        }else{
                            val error = res["data"].toString()
                            MyApplication.instance.error = error

                            val data = mapOf<String, Any?>("room_name" to mac, "hotel_name" to error)
                            val res = mapOf<String, Any?>("data" to data)
                            tokenHttp?.thenList?.forEach{ it(JSON.toJSONString(res), call, response) }
                            tokenHttp = null
                        }
                    }
                }
            })
            return tokenHttp!!
        }

        fun get(url: String): MyHttp {
            //url.debug()
            val http = MyHttp()
            val request = Request.Builder()
                    .url("$SERVER_ADDRESS$url")
                    .addHeader("Accept", "application/json")
                    .addHeader("Token", token?:"")
                    .build()

            val call = okHttpClient.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    val map = mapOf(Pair("msg", false), Pair("message", e?.message))
                    http.errorList.forEach{ it(JSON.toJSONString(map), call, e) }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        val result = response.body()?.string()
                        http.thenList.forEach{ it(result?: "", call, response) }
                    }
                }
            })
            return http
        }

        fun get(url: String, baseUrl: String): MyHttp {
            //url.debug()
            val http = MyHttp()
            val request = Request.Builder()
                    .url("$baseUrl$url")
                    .addHeader("Accept", "application/json")
                    .addHeader("Token", token?:"")
                    .build()

            val call = okHttpClient.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    val map = mapOf(Pair("msg", false), Pair("message", e?.message))
                    http.errorList.forEach{ it(JSON.toJSONString(map), call, e) }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        val result = response.body()?.string()
                        http.thenList.forEach{ it(result?: "", call, response) }
                    }
                }
            })
            return http
        }

        fun post(url: String, requestBody: String): MyHttp {
            //url.debug()
            val http = MyHttp()

            val request = Request.Builder()
                    .url("$SERVER_ADDRESS$url")
                    .addHeader("Accept", "application/json")
                    .addHeader("Token", token?:"")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody))
                    .build()

            val call = okHttpClient.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    val map = mapOf(Pair("msg", false), Pair("message",e?.message))
                    http.errorList.forEach{ it(JSON.toJSONString(map), call, e) }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        val result = response.body()?.string()
                        http.thenList.forEach{ it(result?: "", call, response) }
                    }
                }
            })
            return http
        }

        fun post(url: String, requestBody: RequestBody): MyHttp {
            //url.debug()
            val http = MyHttp()

            val request = Request.Builder()
                    .url("$SERVER_ADDRESS$url")
                    .addHeader("Accept", "application/json")
                    .addHeader("Token", token?:"")
                    .post(requestBody)
                    .build()

            val call = okHttpClient.newCall(request)
            call.enqueue(object: Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    val map = mapOf(Pair("msg", false), Pair("message",e?.message))
                    http.errorList.forEach{ it(JSON.toJSONString(map), call, e) }
                }

                override fun onResponse(call: Call?, response: Response?) {
                    response?.let {
                        val result = response.body()?.string()
                        http.thenList.forEach{ it(result?: "", call, response) }
                    }
                }
            })
            return http
        }
    }

    private var thenList = Vector<(json: String, call: Call?, response: Response?)->Unit>()
    private var errorList = Vector<(json: String, call: Call?, e: IOException?)->Unit>()

    fun then(action: (json: String, call: Call?, response: Response?)->Unit): MyHttp {
        thenList.add(action)
        return this
    }
    fun error(error: (json: String, call: Call?, e: IOException?)->Unit): MyHttp {
        errorList.add(error)
        return this
    }
}
