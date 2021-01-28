package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.MyPagerAdapter
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_configsystem.*
import kotlinx.android.synthetic.main.page_one.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ConfigSystemActivity : BaseActivity(){

    private lateinit var view1 : View
    private lateinit var view2 : View
    private lateinit var view3 : View
    private lateinit var view4 : View
    private var pagerList = arrayListOf<View>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configsystem)

        myNetState.initView(this, true, this)

        EventBus.getDefault().register(this)
        initUI()

    }

    private fun initUI() {
        val mInflater = layoutInflater
        view1 = mInflater.inflate(R.layout.page_one, null)
        view2 = mInflater.inflate(R.layout.page_two, null)
        view3 = mInflater.inflate(R.layout.page_three, null)
        view4 = mInflater.inflate(R.layout.page_four, null)
        pagerList.add(view1)
        pagerList.add(view2)
        pagerList.add(view3)
        pagerList.add(view4)
        viewPager.adapter = MyPagerAdapter(pagerList)
        viewPager.setOnPageChangeListener(pageChange)
        viewPager.currentItem = 0
        viewPager.setPagingEnabled(false)// 禁止翻页
        initView1()
    }

    private fun initView1() {

        view1.tvNowTime.text = BaseVolume.getNowSystemTime()
        view1.btnNext.setOnClickListener {
            viewPager.currentItem = 1
            initView2()
        }

    }

    private fun initView2() {
        view2.btnNext.setOnClickListener {
            viewPager.currentItem = 2
            initView3()
        }
    }
    private fun initView3() {
        view3.btnNext.setOnClickListener {
            viewPager.currentItem = 3
            initView4()
        }
    }
    private fun initView4() {
        view4.btnNext.setOnClickListener {
            startActivity(Intent(mContext,FinallyConfigActivity().javaClass))
        }
    }

    private val pageChange: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }
        override fun onPageSelected(position: Int) {
            Log.e("界面切换", " 界面选择：$position")
        }
        override fun onPageScrollStateChanged(state: Int) {
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