package com.smart.bms_byd.ui.diagnosis

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smart.bms_byd.R
import com.smart.bms_byd.otherPage.ConnectWIFIActivity
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.NetStateInfoView
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_diagnosis.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DiagnosisFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diagnosis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        EventBus.getDefault().register(this);
    }

    private fun initUI() {
        myNetState.initView(context,true,object : NetStateInfoView.NetStateInfoListener{
            override fun onClickListenerByNetInfo(view: View?) {
                startActivity(Intent(context, ConnectWIFIActivity().javaClass))
            }
        })
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 隐藏啦
        if (hidden) {

        }
        // 显示啦
        else {

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

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }


}