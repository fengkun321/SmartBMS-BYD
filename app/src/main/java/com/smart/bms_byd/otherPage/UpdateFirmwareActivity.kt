package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.AreaAddWindowHint
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_update_firmware.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class UpdateFirmwareActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_firmware)

        myNetState.initView(this,true,this);

        EventBus.getDefault().register(this)

    }

    override fun onResume() {
        super.onResume()
        when(BaseApplication.getInstance().nowNetWorkType) {
            // 未联网
            NetWorkType.NOTHING_NET -> {
                showDialog("未联网，快去！",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {

                    }

                    override fun cancelListener() {

                    }
                },false)
            }
            // 移动网 或 其他WIFI
            NetWorkType.MOBILE_NET,NetWorkType.WIFI_OTHER -> {
                showDialog("固件下载完成，请连接设备热点",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                        startActivity(Intent(mContext,ConnectWIFIActivity().javaClass))
                    }
                    override fun cancelListener() {

                    }
                },false)
            }
            // 设备热点
            NetWorkType.WIFI_DEVICE -> {
                showDialog("固件已升级完成，前往下一步控制",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                        startActivity(Intent(mContext,ConfigSystemActivity().javaClass))
                        finish()
                    }

                    override fun cancelListener() {

                    }
                },false)
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 接收数据
            MessageInfo.i_NET_WORK_STATE -> {
                val netWorkType = msg.anyInfo as NetWorkType
                myNetState.updateNetInfo(netWorkType)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


}