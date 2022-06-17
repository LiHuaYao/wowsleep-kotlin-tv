package com.idianwoo.tv.utils

import android.util.Log

fun <T> T.debug(tag: String = "Extend.debug"): T {
    Log.d(tag, "${this}")
    return this
}

