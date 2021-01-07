package com.smart.bms_byd

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.smart.bms_byd.ui.dashboard.DashboardFragment
import com.smart.bms_byd.ui.home.HomeFragment
import com.smart.bms_byd.ui.notifications.NotificationsFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    private var lastIndex = 0
    private var mFragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


}