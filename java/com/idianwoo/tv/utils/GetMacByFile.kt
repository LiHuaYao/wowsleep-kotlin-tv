package com.idianwoo.tv.utils

import android.text.TextUtils
import android.util.Log
import com.tencent.wxop.stat.event.i
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object GetMacByFile {
    fun readMacFilePath(filename: String): String? {
        val strings = ArrayList<String>()
        var `in`: FileInputStream? = null
        var mac: String? = ""
        var bufReader: BufferedReader? = null
        var inReader: InputStreamReader? = null
        try {
            `in` = FileInputStream(filename)
            inReader = InputStreamReader(`in`, "UTF-8")
            bufReader = BufferedReader(inReader)
            //var i = 1
            bufReader.use { r ->
                r.lineSequence().forEach{
                    val s = it.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (j in s.indices) {
                        if (s[j].contains("eth") || s[j].contains("wlan")) {
                            strings.add(s[j].trim { it <= ' ' })
                        }
                    }
                    //i++
                }
            }

            Collections.sort(strings)
            //Log.e("readMacFilePath sort", strings.toString())
            for (string in strings) {
                mac = getMac("/sys/class/net/$string/address")
                if (!TextUtils.isEmpty(mac)) return mac
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            //Log.e("readMacFilePath e", e.message)
        } finally {
            try {
                `in`?.close()
                bufReader?.close()
                inReader?.close()
            } catch (e: IOException) {
                //e.printStackTrace()
            }
        }
        //Log.d("readMacFilePath mac", mac)
        return mac
    }

    fun getMac(filename: String): String? {
        var mac: String? = null
        try {
            val `in` = FileInputStream(filename)
            val inReader = InputStreamReader(`in`, "UTF-8")
            val bufReader = BufferedReader(inReader)
            //var i = 1
            bufReader.use { r ->
                r.lineSequence().forEach{
                    //Log.d("in getMac line", i.toString() + it.trim { it <= ' ' })
                    mac = it
                    //i++
                }
            }

            bufReader.close()
            inReader.close()
            `in`.close()
        } catch (e: Exception) {
            //e.printStackTrace()
            //Log.d("in getMac", filename + "error " + e.message)
        }
        //Log.d("in getMac mac is", mac)
        return mac
    }
}