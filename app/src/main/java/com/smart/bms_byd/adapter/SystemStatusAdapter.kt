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

        p0.tvName.text = deviceList[p1].strType

        if (p1 == 0) {
            p0.llSystemMain.visibility = View.VISIBLE
            p0.llSystemSub.visibility = View.GONE
            p0.tvSOCMain.text = deviceList[p1].strSOC
            p0.tvBatteryMain.text = deviceList[p1].strBatteryVoltage
            p0.tvOutputMain.text = deviceList[p1].strOutputVoltage
            p0.tvCurrentMain.text = deviceList[p1].strCurrent
            p0.tvParrellalMain.text = deviceList[p1].strParrellalConnection
            p0.tvModulesMain.text = deviceList[p1].strModulesPerTower
            p0.tvMaxVolMain.text = deviceList[p1].strMaxCellVoltage
//            p0.tvMaxVolNumberMain.text = deviceList[p1].strMaxCellVoltageNumber
            p0.tvMinVolMain.text = deviceList[p1].strMinCellVoltage
//            p0.tvMinVolNumberMain.text = deviceList[p1].strMinCellVoltageNumber
            p0.tvMaxTempMain.text = deviceList[p1].strMaxCellTemperature
//            p0.tvMaxTempNumberMain.text = deviceList[p1].strMaxCellTemperatureNumber
            p0.tvMinTempMain.text = deviceList[p1].strMinCellTemperature
//            p0.tvMinTempNumberMain.text = deviceList[p1].strMinCellTemperatureNumber

        }
        else {
            p0.llSystemMain.visibility = View.VISIBLE
            p0.llSystemSub.visibility = View.GONE

            p0.tvBatterySub.text = deviceList[p1].strBatteryVoltage
            p0.tvOutputSub.text = deviceList[p1].strOutputVoltage
            p0.tvCurrentSub.text = deviceList[p1].strCurrent
            p0.tvMaxVolSub.text = deviceList[p1].strMaxCellVoltage
            p0.tvMaxVolNumberSub.text = deviceList[p1].strMaxCellVoltageNumber
            p0.tvMinVolSub.text = deviceList[p1].strMinCellVoltage
            p0.tvMinVolNumberSub.text = deviceList[p1].strMinCellVoltageNumber
            p0.tvMaxTempSub.text = deviceList[p1].strMaxCellTemperature
            p0.tvMaxTempNumberSub.text = deviceList[p1].strMaxCellTemperatureNumber
            p0.tvMinTempSub.text = deviceList[p1].strMinCellTemperature
            p0.tvMinTempNumberSub.text = deviceList[p1].strMinCellTemperatureNumber

        }

    }


    override fun getItemCount(): Int {
        return deviceList.size
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tvName: TextView

        lateinit var llSystemMain : LinearLayout
        lateinit var tvSOCMain: TextView
        lateinit var tvBatteryMain: TextView
        lateinit var tvOutputMain: TextView
        lateinit var tvCurrentMain: TextView
        lateinit var tvParrellalMain: TextView
        lateinit var tvModulesMain: TextView
        lateinit var tvMaxVolMain: TextView
//        lateinit var tvMaxVolNumberMain: TextView
        lateinit var tvMinVolMain: TextView
//        lateinit var tvMinVolNumberMain: TextView
        lateinit var tvMaxTempMain: TextView
//        lateinit var tvMaxTempNumberMain: TextView
        lateinit var tvMinTempMain: TextView
//        lateinit var tvMinTempNumberMain: TextView



        lateinit var llSystemSub : LinearLayout
        lateinit var tvBatterySub: TextView
        lateinit var tvOutputSub: TextView
        lateinit var tvCurrentSub: TextView
        lateinit var tvMaxVolSub: TextView
        lateinit var tvMaxVolNumberSub: TextView
        lateinit var tvMinVolSub: TextView
        lateinit var tvMinVolNumberSub: TextView
        lateinit var tvMaxTempSub: TextView
        lateinit var tvMaxTempNumberSub: TextView
        lateinit var tvMinTempSub: TextView
        lateinit var tvMinTempNumberSub: TextView

        init {

            tvName = itemView.findViewById(R.id.tvName)

            llSystemMain = itemView.findViewById(R.id.llSystemMain)
            tvSOCMain = itemView.findViewById(R.id.tvSOCMain)
            tvBatteryMain = itemView.findViewById(R.id.tvBatteryMain)
            tvOutputMain = itemView.findViewById(R.id.tvOutputMain)
            tvCurrentMain = itemView.findViewById(R.id.tvCurrentMain)
            tvParrellalMain = itemView.findViewById(R.id.tvParrellalMain)
            tvModulesMain = itemView.findViewById(R.id.tvModulesMain)
            tvMaxVolMain = itemView.findViewById(R.id.tvMaxVolMain)
//            tvMaxVolNumberMain = itemView.findViewById(R.id.tvMaxVolNumberMain)
            tvMinVolMain = itemView.findViewById(R.id.tvMinVolMain)
//            tvMinVolNumberMain = itemView.findViewById(R.id.tvMinVolNumberMain)
            tvMaxTempMain = itemView.findViewById(R.id.tvMaxTempMain)
//            tvMaxTempNumberMain = itemView.findViewById(R.id.tvMaxTempNumberMain)
            tvMinTempMain = itemView.findViewById(R.id.tvMinTempMain)
//            tvMinTempNumberMain = itemView.findViewById(R.id.tvMinTempNumberMain)


            llSystemSub = itemView.findViewById(R.id.llSystemSub)
            tvBatterySub = itemView.findViewById(R.id.tvBatterySub)
            tvOutputSub = itemView.findViewById(R.id.tvOutputSub)
            tvCurrentSub = itemView.findViewById(R.id.tvCurrentSub)
            tvMaxVolSub = itemView.findViewById(R.id.tvMaxVolSub)
            tvMaxVolNumberSub = itemView.findViewById(R.id.tvMaxVolNumberSub)
            tvMinVolSub = itemView.findViewById(R.id.tvMinVolSub)
            tvMinVolNumberSub = itemView.findViewById(R.id.tvMinVolNumberSub)
            tvMaxTempSub = itemView.findViewById(R.id.tvMaxTempSub)
            tvMaxTempNumberSub = itemView.findViewById(R.id.tvMaxTempNumberSub)
            tvMinTempSub = itemView.findViewById(R.id.tvMinTempSub)
            tvMinTempNumberSub = itemView.findViewById(R.id.tvMinTempNumberSub)


        }
    }

}