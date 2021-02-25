package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.MainActivityTest
import com.smart.bms_byd.R
import com.smart.bms_byd.data.AnalysisInfo
import com.smart.bms_byd.data.CreateControlData
import com.smart.bms_byd.data.DeviceStateInfo
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.util.BaseVolume
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

        tvProgressValue.setOnClickListener {
            var iProgress = updateProgressBar.progress
            tvProgressValue.text = "${iProgress+50}%"
            updateProgressBar.progress = (iProgress+50)
            checkProgressValue()
        }

        tvUpdateInfo.setOnClickListener {
            if (!updateProgressBar.tag.toString().equals("download")) {
                showDialog("Update failed","Since the verification failed 3 times,you can choose the following to continue.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                    }
                    override fun cancelListener() {
                    }
                },false,"Skip","Retry")
            }
        }

    }

    override fun onResume() {
        super.onResume()
        checkProgressValue()

    }

    private fun checkProgressValue() {
        // 正在下载
        if (updateProgressBar.tag.toString().equals("download")) {
            tvTitleName.text = "DOWNLOAD FIRMWARE"
            if (updateProgressBar.progress == 100) {
                tvUpdateInfo.text = "The firmware download is successful"
                tvUpdateHint.text = "wait for the jump"
                // 当前不是设备热点，则提示连接
                if (BaseApplication.getInstance().nowNetWorkType != NetWorkType.WIFI_DEVICE) {
                    showDialog("Connect Device","The firmware download has been completed. Please connect the device to WIFI",object : AreaAddWindowHint.PeriodListener{
                        override fun refreshListener(string: String?) {
                            startActivity(Intent(mContext,ConnectWIFIActivity().javaClass))
                        }
                        override fun cancelListener() {
                        }
                    },true)
                }
                // 下载完成，且当前是设备热点，则开始更新固件
                else {
                    // 未连接则开始连接
//                    if (TCPClientS.getInstance(BaseApplication.getInstance()).connectionState != TCPClientS.TCP_CONNECT_STATE_CONNECTED) {
//                        DeviceStateInfo.getInstance().initState()
//                        TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP, BaseVolume.TCP_PORT)
//                    }
//                    // 已连接的，则查询版本号
//                    else {
//                        queryBMSBMUVerInfo()
//                    }
                    updateProgressBar.tag = "update"
                    updateProgressBar.progress = 0
                    tvProgressValue.text = "0%"
                    tvTitleName.text = "UPDATE FIRMWARE"
                    tvUpdateInfo.text = "This should take about a minute"
                    tvUpdateHint.text = "please wait"
                }
            }
        }
        // 更新完成啦
        else {
            if (updateProgressBar.progress == 100) {
                tvUpdateInfo.text = "The firmware update is successful"
                tvUpdateHint.text = "wait for the jump"
                // 询问是否配置参数
                showDialog("Configuation","Do you want to configure the system?Configuation is necessary when you commission the system", object : AreaAddWindowHint.PeriodListener {
                    override fun refreshListener(string: String?) {
                        startActivity(Intent(mContext, ConfigSystemActivity().javaClass))
                        finish()
                    }
                    override fun cancelListener() {
                        val intent: Intent = Intent(mContext, MainActivityTest().javaClass)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }, false,"No","Yes")
            }
        }

    }

    /** 读取BMU 系统参数寄存器:  0x0000 寄存器个数：102 */
    private fun queryBMSBMUVerInfo() {
        val strSendData = CreateControlData.readInfoByAddress("0000","0065")
        BaseApplication.getInstance().StartSendDataByTCP(strSendData)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            MessageInfo.i_TCP_CONNECT_SUCCESS -> {
                // 已连接的，则查询版本号
                queryBMSBMUVerInfo()
            }
            MessageInfo.i_TCP_CONNECT_FAIL -> {
                val strFailInfo= msg.anyInfo as String
                showToast(strFailInfo)
            }// 接收数据
            MessageInfo.i_RECEIVE_DATA -> {
                val analysisInfo = msg.anyInfo as AnalysisInfo
                showToast(analysisInfo.strType)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }


}