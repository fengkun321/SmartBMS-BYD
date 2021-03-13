package com.smart.bms_byd.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object BaseVolume {


    const val FIRST_RUN_APPLICATION = "FIRST_RUN_APPLICATION"
    const val TCP_IP = "192.168.16.254"
    const val TCP_PORT = 8080

    const val COMMAND_SEND_START = "COMMAND_SEND_START"
    const val COMMAND_SEND_TIMEOUT = "COMMAND_SEND_TIMEOUT"
    const val COMMAND_SEND_STOP = "COMMAND_SEND_STOP"

    const val CMD_TYPE_READ_DATA = "03"
    const val CMD_TYPE_READ_DATA_ERROR = "83"
    const val CMD_TYPE_WRITE_ONLY = "06"
    const val CMD_TYPE_WRITE_ONLY_ERROR = "86"
    const val CMD_TYPE_WRITE_MORE = "10"
    const val CMD_TYPE_WRITE_MORE_ERROR = "90"
    const val DATA_TYPE = "DATA_TYPE"
    const val DATA_VALUE = "DATA_VALUE"
    const val WIFI_SIGN = "BYD"

    // BMU 升级的起始地址
    const val CMD_UPDATE_BMU_START_ADDRESS = "05F0"
    // BMS 升级的起始地址
    const val CMD_UPDATE_BMS_START_ADDRESS = "0640"
    // 阈值表更新的起始地址
    const val CMD_UPDATE_TABLE_START_ADDRESS = "0400"

    // BMU 升级的起始地址
    const val CMD_UPDATE_BMU_ADDRESS = "05F7"
    // BMS 升级的起始地址
    const val CMD_UPDATE_BMS_ADDRESS = "0647"
    // 阈值表更新的起始地址
    const val CMD_UPDATE_TABLE_ADDRESS = "0407"

    // 读取BMS工作参数的起始位置
    const val CMD_READ_BMS_WORK_0550 = "0550"
    // 查询BMS的读取状态
    const val CMD_READ_BMS_WORK_READ_STATE_0551 = "0551"
    // 从DATA开始读
    const val CMD_READ_BMS_WORK_DATA_ADDRESS_0558 = "0558"

    // 读取历史数据的起始位置
    const val CMD_READ_DEV_HISTORY_05A0 = "05A0"
    // 查询历史数据的读取状态
    const val CMD_READ_DEV_HISTORY_STATE_05A1 = "05A1"
    // 从DATA开始读
    const val CMD_READ_DEV_HISTORY_ADDRESS_05A8 = "05A8"




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

    /**
     * 获取当前系统时间
     */
    fun getNowSystemTime() : String {
        var currentTime = ""
        val cal = Calendar.getInstance()
        //格式化指定形式的时间
        currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.time) //获取到完整的时间
        return currentTime
    }

    /** 年月日
     * 输入格式：2020-12-09
     * 输出格式：Dec-09,2020
     */
    fun getDateInfo(strDate : String) :String{
        val iMonth = strDate.split("-")[1].toString().toInt()
        when(iMonth) {
            1 -> return "Jan-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            2 -> return "Feb-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            3 -> return "Mar-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            4 -> return "Apr-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            5 -> return "May-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            6 -> return "Jun-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            7 -> return "Jul-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            8 -> return "Aug-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            9 -> return "Sept-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            10 -> return "Oct-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            11 -> return "Nov-${strDate.split("-")[2]},${strDate.split("-")[0]}"
            12 -> return "Dec-${strDate.split("-")[2]},${strDate.split("-")[0]}"
        }
        return strDate
    }



}