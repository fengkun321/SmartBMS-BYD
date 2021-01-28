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

class WiFiLsitAdapter(deviceList: ArrayList<ScanResult>, mContext: Context) : RecyclerView.Adapter<WiFiLsitAdapter.ViewHolder>() {

    var deviceList = arrayListOf<ScanResult>()
    lateinit var onItemClickListener : OnItemClickListener
    lateinit var mContext: Context

    init {
        this.deviceList = deviceList
        this.mContext = mContext

    }




    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_wifi_info, p0, false));
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.tvName.text = deviceList[p1].SSID
        p0.tvSign.text = "${deviceList[p1].level}"

        p0.rlParent.setOnClickListener {
            onItemClickListener.onItemClick(it,p0.adapterPosition)
        }

    }


    override fun getItemCount(): Int {
        return deviceList.size
    }


    /** 自定义的接口 */
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tvName: TextView
        lateinit var tvSign: TextView
        lateinit var rlParent: RelativeLayout

        init {

            rlParent = itemView.findViewById(R.id.rlParent)
            tvName = itemView.findViewById(R.id.tvName)
            tvSign = itemView.findViewById(R.id.tvSign)
        }
    }

}