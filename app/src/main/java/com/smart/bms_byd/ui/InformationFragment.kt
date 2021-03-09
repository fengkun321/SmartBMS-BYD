package com.smart.bms_byd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.data.DeviceStateInfo
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.AddMenuWindowDialog
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_informaction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class InformationFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_informaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {

    }


    private fun initData() {
        tvBMUVer.text = DeviceStateInfo.getInstance().BCU_Now_Version
        tvBMSVer.text = DeviceStateInfo.getInstance().BMS_Version
        tvTableVer.text = DeviceStateInfo.getInstance().Five_Table_Version
        tvInverterInfo.text = DeviceStateInfo.getInstance().getInverterTypeInfo()
        tvGridInfo.text = "${DeviceStateInfo.getInstance().getNetWorkInfo()}" + ";" +
                "${DeviceStateInfo.getInstance().getDanSanInfo()}"

        tvApplication.text = ""

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 隐藏啦
        if (hidden) {

        }
        // 显示啦
        else {
            initData()
        }

    }




}