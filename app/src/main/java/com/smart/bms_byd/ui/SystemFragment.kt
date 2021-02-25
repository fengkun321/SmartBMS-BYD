package com.smart.bms_byd.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.SystemStatusAdapter
import com.smart.bms_byd.data.SystemStatusInfo
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_system.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SystemFragment : Fragment() {

    private var systemStatusArrayList = arrayListOf<SystemStatusInfo>()
    private lateinit var systemStatusAdapter: SystemStatusAdapter
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        EventBus.getDefault().register(this);
    }

    private fun initUI() {
        systemStatusArrayList.add(SystemStatusInfo("0"))
        systemStatusArrayList.add(SystemStatusInfo("1"))
        systemStatusArrayList.add(SystemStatusInfo("2"))
        systemStatusArrayList.add(SystemStatusInfo("3"))
        systemStatusArrayList.add(SystemStatusInfo("4"))
        systemStatusArrayList.add(SystemStatusInfo("5"))

        systemStatusAdapter = SystemStatusAdapter(systemStatusArrayList,context)
        recyclerSystemInfo.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        recyclerSystemInfo.adapter = systemStatusAdapter
        recyclerSystemInfo.itemAnimator = DefaultItemAnimator()

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

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)

    }



}