package com.smart.bms_byd.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object BaseVolume {


    const val zigbee_write = "write"



    /**
     * 判断某个界面是否在前台
     *
     * @param context
     * @param className
     * 某个界面名称
     */
    fun isForeground(context: Context?, className: String): Boolean {
        if (context == null || TextUtils.isEmpty(className)) {
            return false
        }
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(1)
        if (list != null && list.size > 0) {
            val cpn = list[0].topActivity
            val firstName = cpn!!.className
            if (className == firstName) {
                return true
            }
        }
        return false
    }



    /**
     * 获取SSID
     * @param activity 上下文
     * @return  WIFI 的SSID
     */
    fun getWIFISSID(activity: Activity): String {
        val ssid = "unknown id"
        if (Build.VERSION.SDK_INT <= 26 || Build.VERSION.SDK_INT == 28) {
            val mWifiManager = (activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            val info = mWifiManager.connectionInfo
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                info.ssid
            } else {
                info.ssid.replace("\"", "")
            }
        } else if (Build.VERSION.SDK_INT == 27) {
            val connManager = (activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            val networkInfo = connManager.activeNetworkInfo!!
            if (networkInfo.isConnected) {
                if (networkInfo.extraInfo != null) {
                    return networkInfo.extraInfo.replace("\"", "")
                }
            }
        }
        return ssid
    }



    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    fun getVersion(con: Context): String {
        return try {
            val manager = con.packageManager
            val info = manager.getPackageInfo(con.packageName, 0)
            info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "0.0"
        }
    }

    /**
     * 获取SDK版本�?
     * @return
     */
    @JvmStatic
    fun getAndroidSDKVersion(): Int {
        var version = 0
        try {
            version = Integer.valueOf(Build.VERSION.SDK)
        } catch (e: NumberFormatException) {
        }
        return version
    }





}