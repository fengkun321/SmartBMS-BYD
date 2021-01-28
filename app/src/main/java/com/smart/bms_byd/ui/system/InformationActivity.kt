package com.smart.bms_byd.ui.system

import android.os.Bundle
import android.view.View
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.AddMenuWindowDialog
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_informaction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class InformationActivity : BaseActivity(){

    private var iNowSelectNumber = 0
    var systemList = arrayListOf<String>()
    lateinit var addMenuWindowDialog: AddMenuWindowDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informaction)

        EventBus.getDefault().register(this);
        myNetState.initView(this, true, this);

        initUI()

    }

    private fun initUI() {
        tvSystem1.setOnClickListener{selectSystemByNumber(0)}
        tvSystem2.setOnClickListener {selectSystemByNumber(1)}
        tvSystem3.setOnClickListener {selectSystemByNumber(2) }
        tvSystemMore.setOnClickListener{
            addMenuWindowDialog.show()
        }

        systemList.add("system1");systemList.add("system2");systemList.add("system3");
        systemList.add("system4");systemList.add("system5");systemList.add("system6");

        addMenuWindowDialog = AddMenuWindowDialog(mContext, R.style.dialog_style, systemList, "选择楼层")
        addMenuWindowDialog.setListener(object : AddMenuWindowDialog.PeriodListener {
            override fun refreshListener(number: Int, strItem: String) {
                selectSystemByNumber(number)
            }
        })

    }

    private fun selectSystemByNumber(iNumber : Int) {
        if (iNowSelectNumber == iNumber) return
        iNowSelectNumber = iNumber
        tvSystem1.setTextColor(resources.getColor(R.color.text_color))
        tvSystem1.setBackgroundResource(R.drawable.system_tran_border)
        tvSystem2.setTextColor(resources.getColor(R.color.text_color))
        tvSystem2.setBackgroundResource(R.drawable.system_tran_border)
        tvSystem3.setTextColor(resources.getColor(R.color.text_color))
        tvSystem3.setBackgroundResource(R.drawable.system_tran_border)
        when(iNumber) {
            0 -> {
                tvSystem1.setTextColor(resources.getColor(R.color.white))
                tvSystem1.setBackgroundResource(R.drawable.system_blue_border)
            }
            1 -> {
                tvSystem2.setTextColor(resources.getColor(R.color.white))
                tvSystem2.setBackgroundResource(R.drawable.system_blue_border)
            }
            2 -> {
                tvSystem3.setTextColor(resources.getColor(R.color.white))
                tvSystem3.setBackgroundResource(R.drawable.system_blue_border)
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