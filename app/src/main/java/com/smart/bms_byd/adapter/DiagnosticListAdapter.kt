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
import com.smart.bms_byd.data.DiagnosticMessageInfo
import com.smart.bms_byd.data.NotificationMessageInfo
import com.smart.bms_byd.data.ScanWiFiInfo

class DiagnosticListAdapter(deviceList: ArrayList<DiagnosticMessageInfo>, mContext: Context?) : RecyclerView.Adapter<DiagnosticListAdapter.ViewHolder>() {

    var deviceList = arrayListOf<DiagnosticMessageInfo>()
    lateinit var onItemClickListener : OnItemClickListener
    lateinit var mContext: Context
    var isHistory = false

    init {
        this.deviceList = deviceList
        this.mContext = mContext!!

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_diagnostic_info, p0, false));
    }

    public fun changeHistory(isHistory : Boolean) {
        this.isHistory = isHistory
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        var strTime = deviceList[p1].strTime
        strTime = strTime.replace(" ","\n")
        p0.tvTime.text = strTime
        p0.tvTitle.text = "(${deviceList[p1].strTitle})"
        p0.tvContent.text = "${deviceList[p1].strContent}"

        if (isHistory) {
            if (p1 % 2 == 0)
                p0.llParent.setBackgroundResource(R.color.white)
            else
                p0.llParent.setBackgroundResource(R.color.color_hui_white)
        }
        else  {
            p0.tvTime.visibility = View.GONE
            if (p1 % 2 == 1)
                p0.llParent.setBackgroundResource(R.color.white)
            else
                p0.llParent.setBackgroundResource(R.color.color_hui_white)
        }



//        p0.llParent.setOnClickListener {
//            onItemClickListener.onItemClick(it,p0.adapterPosition)
//        }

    }


    override fun getItemCount(): Int {
        return deviceList.size
    }


    /** 自定义的接口 */
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tvTime: TextView
        lateinit var tvTitle: TextView
        lateinit var tvContent: TextView
        lateinit var llParent: LinearLayout

        init {

            llParent = itemView.findViewById(R.id.llParent)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvTitle = itemView.findViewById(R.id.tvTitle)
            tvContent = itemView.findViewById(R.id.tvContent)
        }
    }

}