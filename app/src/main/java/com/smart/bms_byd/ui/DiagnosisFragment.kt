package com.smart.bms_byd.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.DiagnosticListAdapter
import com.smart.bms_byd.data.DiagnosticMessageInfo
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.fragment_diagnosis.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DiagnosisFragment : Fragment(),View.OnClickListener{

    var diagnosisList = arrayListOf<DiagnosticMessageInfo>()
    lateinit var diagnosticListAdapter : DiagnosticListAdapter
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diagnosis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        EventBus.getDefault().register(this)
    }

    private fun initUI() {

        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))

        diagnosticListAdapter = DiagnosticListAdapter(diagnosisList,context)
        recyclerDevice.adapter = diagnosticListAdapter
        recyclerDevice.itemAnimator = DefaultItemAnimator()
        recyclerDevice.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        tvRealTime.setOnClickListener(this)
        tvHistory.setOnClickListener(this)

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


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.tvRealTime -> {
                if (tvRealTime.tag.toString().toBoolean()) return
                tvRealTime.tag = true
                tvRealTime.setTextColor(resources.getColor(R.color.white))
                tvRealTime.setBackgroundResource(R.drawable.error_red_bg_border)
                tvHistory.tag = false
                tvHistory.setTextColor(resources.getColor(R.color.black))
                tvHistory.setBackgroundResource(0)
                llErrorTitle.visibility = View.GONE
                diagnosticListAdapter.changeHistory(false)
            }
            R.id.tvHistory -> {
                if (tvHistory.tag.toString().toBoolean()) return
                tvRealTime.tag = false
                tvRealTime.setTextColor(resources.getColor(R.color.black))
                tvRealTime.setBackgroundResource(0)
                tvHistory.tag = true
                tvHistory.setTextColor(resources.getColor(R.color.white))
                tvHistory.setBackgroundResource(R.drawable.error_red_bg_border)
                llErrorTitle.visibility = View.VISIBLE
                diagnosticListAdapter.changeHistory(true)
            }
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