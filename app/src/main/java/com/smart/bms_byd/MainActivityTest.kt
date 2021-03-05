package com.smart.bms_byd


import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.smart.bms_byd.data.AnalysisInfo
import com.smart.bms_byd.data.CreateControlData
import com.smart.bms_byd.otherPage.ConfigSystemActivity
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.ui.*
import com.smart.bms_byd.util.NetWorkType
import com.smart.bms_byd.view.SelectTimeWindowDialog
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_main_test.*
import kotlinx.android.synthetic.main.stumenutop.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivityTest : BaseActivity() {

    private var lastIndex = 0
    private var mFragments = mutableListOf<Fragment>()
    lateinit var drawer : DrawerLayout
    lateinit var selectTimeWindowDialog: SelectTimeWindowDialog

    companion object {
        private lateinit var mainActivityTest: MainActivityTest
        fun getInstance() : MainActivityTest{
            return mainActivityTest
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)

        mainActivityTest = this

        EventBus.getDefault().register(this);

        myNetState.initView(this, true, this)

        initView()
        initData()


//        TCPClientS.getInstance(BaseApplication.getInstance()).connect(BaseVolume.TCP_IP,BaseVolume.TCP_PORT)


    }


    private fun initData() {
        mFragments = ArrayList()
        mFragments.add(SystemFragment())
        mFragments.add(DiagnosisFragment())
        mFragments.add(ServiceGuideFragment())
        mFragments.add(InformationFragment())
        mFragments.add(ContactUsFragment())
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
    }

    private fun initView() {

        selectTimeWindowDialog = SelectTimeWindowDialog(mContext,R.style.dialog_style,object : SelectTimeWindowDialog.PeriodListener{
            override fun refreshListener(strStartTime: String?, strStopTime: String?) {
                showToast("$strStartTime -- $strStopTime")
            }
            override fun cancelListener() {

            }
        })

        imgLeft.setOnClickListener { onShowMenu() }

        //实现左右滑动
        drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        //菜单控件
        val nv = findViewById<NavigationView>(R.id.nav_view)
        nv.getHeaderView(0).rlStatus.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlError.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlSendLogs.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlService.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlConfiguration.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlInformation.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlContactUs.setOnClickListener { selectMenuItem(it.id) }
        nv.getHeaderView(0).rlMore.setOnClickListener { selectMenuItem(it.id) }

    }

    private fun selectMenuItem(iViewID : Int) {
        drawer.closeDrawer(Gravity.LEFT) //关闭左侧菜单栏
        when(iViewID) {
            R.id.rlStatus -> {
                setFragmentPosition(0)
            }
            R.id.rlError -> {
                setFragmentPosition(1)
            }
            R.id.rlSendLogs -> {
                selectTimeWindowDialog.showDialogByTime()
            }
            R.id.rlService -> {
                setFragmentPosition(2)
            }
            R.id.rlConfiguration -> {
                startActivity(Intent(mContext,ConfigSystemActivity().javaClass))
            }
            R.id.rlInformation -> {
                setFragmentPosition(3)
            }
            R.id.rlContactUs -> {
                setFragmentPosition(4)
            }
            R.id.rlMore -> {
                setFragmentPosition(5)
            }
        }

    }


    fun onShowMenu() {
        drawer.openDrawer(Gravity.LEFT) // 打开左侧菜单栏
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {

            MessageInfo.i_NET_WORK_STATE -> {
                val netWorkType = msg.anyInfo as NetWorkType

            }
            MessageInfo.i_TCP_CONNECT_SUCCESS -> {
                val strSendData = CreateControlData.readInfoByAddress("0000", "000b")
                BaseApplication.getInstance().StartSendDataByTCP(strSendData)
            }
            MessageInfo.i_TCP_CONNECT_FAIL -> {
                val strFailInfo = msg.anyInfo as String
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
        myNetState.unRegisterEventBus()

    }


}