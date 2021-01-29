package com.smart.bms_byd.ui.diagnosis

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
import com.smart.bms_byd.adapter.DiagnosticListAdapter
import com.smart.bms_byd.data.DiagnosticMessageInfo
import com.smart.bms_byd.otherPage.ConnectWIFIActivity
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.NetStateInfoView
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_notification_message.*
import kotlinx.android.synthetic.main.fragment_diagnosis.*
import kotlinx.android.synthetic.main.fragment_diagnosis.myNetState
import kotlinx.android.synthetic.main.fragment_diagnosis.recyclerDevice
import kotlinx.android.synthetic.main.fragment_diagnosis.tvRight
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
        EventBus.getDefault().register(this);
    }

    private fun initUI() {
        myNetState.initView(context,true,object : NetStateInfoView.NetStateInfoListener{
            override fun onClickListenerByNetInfo(view: View?) {
                startActivity(Intent(context, ConnectWIFIActivity().javaClass))
            }
        })

        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))
        diagnosisList.add(DiagnosticMessageInfo("2021-01-29 12:30:25","BMS","xxxx event type xxxx event type xxxx"))

        diagnosticListAdapter = DiagnosticListAdapter(diagnosisList,context!!)
        recyclerDevice.adapter = diagnosticListAdapter
        recyclerDevice.itemAnimator = DefaultItemAnimator()
        recyclerDevice.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        tvRealTime.setOnClickListener(this)
        tvHistory.setOnClickListener(this)
        tvRight.setOnClickListener(this)
        imgPush.setOnClickListener(this)

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
                tvRealTime.setBackgroundResource(R.drawable.system_blue_border)
                tvHistory.tag = false
                tvHistory.setTextColor(resources.getColor(R.color.text_color))
                tvHistory.setBackgroundResource(R.drawable.system_tran_border)

            }
            R.id.tvHistory -> {
                if (tvHistory.tag.toString().toBoolean()) return
                tvRealTime.tag = false
                tvRealTime.setTextColor(resources.getColor(R.color.text_color))
                tvRealTime.setBackgroundResource(R.drawable.system_tran_border)
                tvHistory.tag = true
                tvHistory.setTextColor(resources.getColor(R.color.white))
                tvHistory.setBackgroundResource(R.drawable.system_blue_border)
            }
            R.id.tvRight -> {

            }
            R.id.imgPush -> {

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

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }




}