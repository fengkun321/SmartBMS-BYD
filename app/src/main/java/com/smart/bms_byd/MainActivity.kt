package com.smart.bms_byd

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.smart.bms_byd.data.AnalysisInfo
import com.smart.bms_byd.data.CreateControlData
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.ui.more.MoreFragment
import com.smart.bms_byd.ui.system.SystemFragment
import com.smart.bms_byd.ui.diagnosis.DiagnosisFragment
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BaseActivity() {

    private var lastIndex = 0
    private var mFragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this);


        initData()

        llItem1.setOnClickListener { setFragmentPosition(0) }
        llItem2.setOnClickListener { setFragmentPosition(1) }
        llItem3.setOnClickListener { setFragmentPosition(2) }

//        TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)


    }


    private fun initData() {
        mFragments = ArrayList()
        mFragments.add(SystemFragment())
        mFragments.add(DiagnosisFragment())
        mFragments.add(MoreFragment())
        // 初始化展示MessageFragment
        setFragmentPosition(0)

    }

    private fun setFragmentPosition(position: Int) {
        val ft = supportFragmentManager.beginTransaction()
        val currentFragment = mFragments[position]
        val lastFragment = mFragments[lastIndex]
        ft.hide(lastFragment)
        lastIndex = position
        if (!currentFragment.isAdded) {
            supportFragmentManager.beginTransaction().remove(currentFragment).commit()
            ft.add(R.id.nav_host_fragment, currentFragment)
        }
        ft.show(currentFragment)
        ft.commitAllowingStateLoss()

        tvOne.setTextColor(resources.getColor(R.color.black))
        tvTwo.setTextColor(resources.getColor(R.color.black))
        tvThree.setTextColor(resources.getColor(R.color.black))

        when(position) {
            0 -> {
                tvOne.setTextColor(resources.getColor(R.color.text_color))
            }
            1 -> {
                tvTwo.setTextColor(resources.getColor(R.color.text_color))
            }
            2 -> {
                tvThree.setTextColor(resources.getColor(R.color.text_color))
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
            MessageInfo.i_TCP_CONNECT_SUCCESS -> {
                val strSendData = CreateControlData.readInfoByAddress("0000","000b")
                BaseApplication.getInstance().StartSendDataByTCP(strSendData)
            }
            MessageInfo.i_TCP_CONNECT_FAIL -> {
                val strFailInfo= msg.anyInfo as String
                showToast(strFailInfo)
            }
            MessageInfo.i_RECEIVE_DATA -> {
                val analysisInfo = msg.anyInfo as AnalysisInfo
                showToast(analysisInfo.strType)
            }

        }

    }

    private var time = System.currentTimeMillis()
    override fun onBackPressed() {
        if (System.currentTimeMillis() - time > 1500) {
            time = System.currentTimeMillis()
            Toast.makeText(this, "双击退出应用", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        TCPClientS.getInstance(BaseApplication.getInstance()).manuallyDisconnect()
        EventBus.getDefault().unregister(this)

    }


}