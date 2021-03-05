package com.smart.bms_byd.ui.more

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.NotificationListAdapter
import com.smart.bms_byd.data.NotificationMessageInfo
import kotlinx.android.synthetic.main.activity_notification_message.*


class NotificationMessageActivity : BaseActivity(),NotificationListAdapter.OnItemClickListener{

    private var notificationList = arrayListOf<NotificationMessageInfo>()
    private lateinit var notificationListAdapter : NotificationListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_message)

        myNetState.initView(this, true, this);
        imgLeft.setOnClickListener { finish() }

        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))
        notificationList.add(NotificationMessageInfo("2020-01-21 14:20:58","important Notice","The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site...The booking time is only 7 days,please be at the store site..."))

        notificationList[1].strType = "URL"

        notificationListAdapter = NotificationListAdapter(notificationList,mContext)
        notificationListAdapter.onItemClickListener = this
        recyclerDevice.layoutManager = LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false)
        recyclerDevice.adapter = notificationListAdapter
        recyclerDevice.itemAnimator = DefaultItemAnimator()


    }

    override fun onItemClick(view: View?, position: Int) {
        showToast(notificationList[position].strTitle)
        startActivity(Intent(mContext,OpenNotificationActivity().javaClass).putExtra("NotificationInfo",notificationList[position]))

    }

    override fun onDestroy() {
        super.onDestroy()
        myNetState.unRegisterEventBus()

    }


}