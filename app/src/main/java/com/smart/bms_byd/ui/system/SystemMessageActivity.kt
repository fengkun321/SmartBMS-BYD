package com.smart.bms_byd.ui.system

import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_system_message.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SystemMessageActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_message)

        EventBus.getDefault().register(this);
        myNetState.initView(this, true, this);

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