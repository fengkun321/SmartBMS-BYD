package com.smart.bms_byd

import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import com.smart.bms_byd.ui.dashboard.DashboardFragment
import com.smart.bms_byd.ui.home.HomeFragment
import com.smart.bms_byd.ui.notifications.NotificationsFragment
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.MyStyleTitleView
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

        myTitleView.initView(this,"主页",false,false,true,onTitleClickListener);


        initData()

        llItem1.setOnClickListener { setFragmentPosition(0) }
        llItem2.setOnClickListener { setFragmentPosition(1) }
        llItem3.setOnClickListener { setFragmentPosition(2) }


    }


    private fun initData() {
        mFragments = ArrayList()
        mFragments.add(HomeFragment())
        mFragments.add(NotificationsFragment())
        mFragments.add(DashboardFragment())
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
    }

    private val onTitleClickListener = object : MyStyleTitleView.MyStyleTitleViewListener{
        override fun onClickListenerByLeft(view: View?) {

        }

        override fun onClickListenerByRight(view: View?) {

        }

        override fun onClickListenerByNetInfo(view: View?) {
            showToast("嘿嘿嘿")
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 接收数据
            MessageInfo.i_NET_WORK_STATE -> {
                val netWorkType = msg.anyInfo as NetWorkType
                myTitleView.updateNetInfo(netWorkType)
            }

        }

    }



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }


}