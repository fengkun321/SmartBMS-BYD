package com.smart.bms_byd.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.smart.bms_byd.R
import com.smart.bms_byd.data.ScanWiFiInfo
import com.smart.bms_byd.data.SystemStatusInfo

class SystemStatusAdapter(deviceList: ArrayList<SystemStatusInfo>, mContext: Context?) : RecyclerView.Adapter<SystemStatusAdapter.ViewHolder>() {

    var deviceList = arrayListOf<SystemStatusInfo>()
    lateinit var mContext: Context

    init {
        this.deviceList = deviceList
        this.mContext = mContext!!

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_system_status, p0, false))
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
//        p0.tvName.text = deviceList[p1].SSID
//        p0.tvSign.text = "${deviceList[p1].level}"

        if (p1 == 0) {
            p0.tvName.text = "System"
            p0.llSystemMain.visibility = View.VISIBLE
            p0.llSystemSub.visibility = View.GONE
            p0.tvBatteryMain.text =  deviceList[p1].strBatteryVoltage
        }
        else {
            p0.tvName.text = "Sub-system $p1"
            p0.llSystemMain.visibility = View.GONE
            p0.llSystemSub.visibility = View.VISIBLE
            p0.tvBatterySub.text =  deviceList[p1].strBatteryVoltage
        }

    }


    override fun getItemCount(): Int {
        return deviceList.size
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tvName: TextView
        lateinit var tvBatteryMain: TextView
        lateinit var tvBatterySub: TextView
        lateinit var llSystemMain : LinearLayout
        lateinit var llSystemSub : LinearLayout

        init {

            tvName = itemView.findViewById(R.id.tvName)
            tvBatteryMain = itemView.findViewById(R.id.tvBatteryMain)
            tvBatterySub = itemView.findViewById(R.id.tvBatterySub)
            llSystemMain = itemView.findViewById(R.id.llSystemMain)
            llSystemSub = itemView.findViewById(R.id.llSystemSub)
        }
    }

}