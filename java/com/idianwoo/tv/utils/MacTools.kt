package com.idianwoo.tv.utils

import android.content.Context
import android.text.TextUtils
import android.os.Build
import android.net.wifi.WifiManager
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import java.io.*

object MacTools {

    fun getMac(context: Context): String? {
        val macByFile = GetMacByFile.readMacFilePath("/proc/net/dev")
        if(!TextUtils.isEmpty(macByFile)) return macByFile

        //var strMac: String? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //Log.e("=====", "6.0以下")
            return getLocalMacAddressFromWifiInfo(context)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Log.e("=====", "6.0以上7.0以下")
            return getMacAddress(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Log.e("=====", "7.0以上")
            return if (!TextUtils.isEmpty(getMacAddress())) {
                //Log.e("=====", "7.0以上1")
                getMacAddress()
            } else if (!TextUtils.isEmpty(getMachineHardwareAddress())) {
                //Log.e("=====", "7.0以上2")
                getMachineHardwareAddress()
            } else {
                //Log.e("=====", "7.0以上3")
                getLocalMacAddressFromBusybox()
            }
        }
        return "02:00:00:00:00:00"
    }
    /**
     * 根据wifi信息获取本地mac
     */
    fun getLocalMacAddressFromWifiInfo(context: Context): String {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wInfo = wifi.connectionInfo
        return wInfo.macAddress
    }
    /**
     * android 6.0及以上、7.0以下 获取mac地址
     */
    fun getMacAddress(context: Context): String {

        // 如果是6.0以下，直接通过wifimanager获取
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val macAddress0 = getMacAddress0(context)
            if (!TextUtils.isEmpty(macAddress0)) return macAddress0
        }
        var str: String? = ""
        var macSerial: String = ""
        try {
            val pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address")
            val ir = InputStreamReader(pp.inputStream)
            val input = LineNumberReader(ir)
            while (null != str) {
                str = input.readLine()
                if (str != null) {
                    macSerial = str.trim { it <= ' ' }// 去空格
                    break
                }
            }
        } catch (ex: Exception) {
            //Log.e("----->" + "NetInfoManager", "getMacAddress:" + ex.toString())
        }

        if ("" == macSerial) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17)
            } catch (e: Exception) {
                //e.printStackTrace()
                //Log.e("----->" + "NetInfoManager", "getMacAddress:" + e.toString())
            }
        }
        return macSerial
    }

    private fun getMacAddress0(context: Context): String {
        if (isAccessWifiStateAuthorized(context)) {
            val wifiMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            var wifiInfo: WifiInfo? = null
            try {
                wifiInfo = wifiMgr.connectionInfo
                return wifiInfo!!.macAddress
            } catch (e: Exception) {
                //Log.e("----->" + "NetInfoManager","getMacAddress0:" + e.toString())
            }
        }
        return ""
    }
    /**
     * Check whether accessing wifi state is permitted
     */
    private fun isAccessWifiStateAuthorized(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE")
    }

    private fun loadFileAsString(fileName: String): String {
        val reader = FileReader(fileName)
        val text = loadReaderAsString(reader)
        reader.close()
        return text
    }

    private fun loadReaderAsString(reader: Reader): String {
        val builder = StringBuilder()
        val buffer = CharArray(4096)
        var readLength = reader.read(buffer)
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength)
            readLength = reader.read(buffer)
        }
        return builder.toString()
    }
    /**
     * 根据IP地址获取MAC地址
     */
    fun getMacAddress(): String? {
        var strMacAddr: String? = null
        try {
            // 获得IpD地址
            val ip = getLocalInetAddress()
            val b = NetworkInterface.getByInetAddress(ip).hardwareAddress
            val buffer = StringBuffer()
            for (i in b.indices) {
                if (i != 0) buffer.append(':')

                val str = Integer.toHexString(b[i].toInt() and 0xFF)
                buffer.append(if (str.length == 1) 0.toString() + str else str)
            }
            strMacAddr = buffer.toString().toUpperCase()
        } catch (e: Exception) { }
        return strMacAddr
    }
    /**
     * 获取移动设备本地IP
     */
    private fun getLocalInetAddress(): InetAddress? {
        var ip: InetAddress? = null
        try {
            // 列举
            val en_netInterface = NetworkInterface
                    .getNetworkInterfaces()
            while (en_netInterface.hasMoreElements()) {// 是否还有元素
                val ni = en_netInterface
                        .nextElement() as NetworkInterface// 得到下一个元素
                val en_ip = ni.inetAddresses// 得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement()
                    if (!ip!!.isLoopbackAddress && ip.hostAddress.indexOf(":") === -1)  break
                    else ip = null
                }

                if (ip != null) break
            }
        } catch (e: SocketException) {
            //e.printStackTrace()
        }
        return ip
    }
    /**
     * 获取本地IP
     */
    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface
                    .getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) return inetAddress.hostAddress.toString()
                }
            }
        } catch (ex: SocketException) {
            //ex.printStackTrace()
        }
        return null
    }
    /**
     * android 7.0及以上 （2）扫描各个网络接口获取mac地址
     */

    /**
     * 获取设备HardwareAddress地址
     */
    fun getMachineHardwareAddress(): String? {
        var interfaces: Enumeration<NetworkInterface>? = null
        try {
            interfaces = NetworkInterface.getNetworkInterfaces()
        } catch (e: SocketException) {
            //e.printStackTrace()
        }

        var hardWareAddress: String? = null
        var iF: NetworkInterface?
        if (interfaces == null) return null

        while (interfaces.hasMoreElements()) {
            iF = interfaces.nextElement()
            try {
                hardWareAddress = bytesToString(iF!!.hardwareAddress)
                if (hardWareAddress != null)
                    break
            } catch (e: SocketException) {
                //e.printStackTrace()
            }
        }
        return hardWareAddress
    }

    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private fun bytesToString(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null

        val buf = StringBuilder()
        for (b in bytes) {
            buf.append(String.format("%02X:", b))
        }

        if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
        return buf.toString()
    }
    /**
     * android 7.0及以上 （3）通过busybox获取本地存储的mac地址
     */

    /**
     * 根据busybox获取本地Mac
     */
    fun getLocalMacAddressFromBusybox(): String {
        var result: String?
        val mac: String
        result = callCmd("busybox ifconfig", "HWaddr")
        // 如果返回的result == null，则说明网络不可取
        if (result == null) return "网络异常"

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.isNotEmpty() && result.contains("HWaddr")) {
            mac = result.substring(result.indexOf("HWaddr") + 6, result.length - 1)
            result = mac
        }
        return result
    }

    private fun callCmd(cmd: String, filter: String): String? {
        var result = ""
        var line: String
        try {
            val proc = Runtime.getRuntime().exec(cmd)
            val isr = InputStreamReader(proc.inputStream)
            val br = BufferedReader(isr)
            line = br.readLine()
            while (!line.contains(filter)) {
                result += line
                line = br.readLine()
            }
            result = line
        } catch (e: Exception) {
            //e.printStackTrace()
        }
        return result
    }





}