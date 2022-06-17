package com.idianwoo.tv.utils

import android.content.Context
import android.provider.UserDictionary.Words.APP_ID
import com.tencent.mm.opensdk.diffdev.DiffDevOAuthFactory
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WxApi {
    companion object {
        const val APP_ID = ""
    }
    lateinit var api: IWXAPI
    private lateinit var context: Context

    fun regToWx (context: Context) {
        this.context = context
        api = WXAPIFactory.createWXAPI(context, APP_ID, true)
        api.registerApp(APP_ID)
    }

    val iDiffDevOAuth = DiffDevOAuthFactory.getDiffDevOAuth()

}