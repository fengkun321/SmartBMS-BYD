package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.AreaAddWindowHint
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

//        BaseApplication.getInstance().saveBooleanBySharedPreferences(BaseVolume.FIRST_RUN_APPLICATION,false)

        myNetState.initView(this,true,this);

        btnNext.setOnClickListener {
            // 未联网
            if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.NOTHING_NET) {
                showDialog("Not Connected","Please go to connect to external WiFi or open the data connection.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                    }
                    override fun cancelListener() {
                    }
                },true)
            }
            // 设备热点
            else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.WIFI_DEVICE) {
                showDialog("Device Network","The current device network,please go to connect to the external network.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                    }
                    override fun cancelListener() {
                    }
                },true)
            }
            // 手机移动网
            else if (BaseApplication.getInstance().nowNetWorkType == NetWorkType.MOBILE_NET) {
                showDialog("Non-WiFi Environment","whether to download the firmware use traffic,the firmware is about 236M.",object : AreaAddWindowHint.PeriodListener{
                    override fun refreshListener(string: String?) {
                        startActivity(Intent(mContext,UpdateFirmwareActivity().javaClass))
                        finish()
                    }
                    override fun cancelListener() {
                    }
                },false)
            }
            // 其他wifi
            else {
                startActivity(Intent(mContext,UpdateFirmwareActivity().javaClass))
                finish()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        myNetState.unRegisterEventBus()

    }


}