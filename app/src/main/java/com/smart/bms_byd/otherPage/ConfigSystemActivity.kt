package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.viewpager.widget.ViewPager
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.MainActivityTest
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.MyPagerAdapter
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_configsystem.*
import kotlinx.android.synthetic.main.page_four.view.*
import kotlinx.android.synthetic.main.page_one.view.*
import kotlinx.android.synthetic.main.page_three.*
import kotlinx.android.synthetic.main.page_three.view.*
import kotlinx.android.synthetic.main.page_two.view.*
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

        imgLeft.setOnClickListener { finish() }

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
        switchPager(0)

        btnNext.setOnClickListener {
            val iNowCurrent = viewPager.currentItem
            if (iNowCurrent < 3) {
                switchPager((iNowCurrent + 1))
            }
            else {
                // 确认参数
                if (llSureConfigData.visibility != View.VISIBLE && llSuccess.visibility != View.VISIBLE) {
                    initSureConfigData()
                }
                // 提交
                else if (llSureConfigData.visibility == View.VISIBLE && llSuccess.visibility != View.VISIBLE) {
                    llSureConfigData.visibility = View.GONE
                    llSuccess.visibility = View.VISIBLE
                    btnNext.text = "OK"
                }
                // 跳转页面
                else if (llSureConfigData.visibility != View.VISIBLE && llSuccess.visibility == View.VISIBLE){
                    val intent: Intent = Intent(mContext, MainActivityTest().javaClass)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

            }
        }

    }

    private fun switchPager(iNowCurrent: Int) {
        viewPager.currentItem = iNowCurrent
        when(iNowCurrent) {
            0 -> initView1()
            1 -> initView2()
            2 -> initView3()
            3 -> initView4()
        }
    }

    private fun initView1() {
        imgFow.setImageResource(R.drawable.img_fow_one)
        val strNowTimeInfo = BaseVolume.getNowSystemTime()
        view1.tvDate.text = BaseVolume.getDateInfo(strNowTimeInfo.split(" ")[0])
        view1.tvTime.text = strNowTimeInfo.split(" ")[1]
    }

    private lateinit var inverterAdapter : ArrayAdapter<String>
    private val inverterArray = arrayListOf<String>()
    private fun initView2() {
        imgFow.setImageResource(R.drawable.img_fow_two)
        view2.spinnerInverter.tag = "Inverter"
        inverterArray.add("GOODWE")
        inverterArray.add("FRONIUS")
        inverterArray.add("KOSTAL")
        inverterArray.add("SELECTRONIC")
        //将可选内容与ArrayAdapter连接起来
        inverterAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inverterArray)
        //设置下拉列表的风格
        inverterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view2.spinnerInverter.adapter = inverterAdapter
        //添加事件Spinner事件监听
        view2.spinnerInverter.onItemSelectedListener = OnItemSelectedListener


    }

    private lateinit var systemAdapter : ArrayAdapter<String>
    private val systemArray = arrayListOf<String>()
    private lateinit var inputAdapter : ArrayAdapter<String>
    private val inputArray = arrayListOf<String>()

    private fun initView3() {
        imgFow.setImageResource(R.drawable.img_fow_three)

        view3.spinnerSystem.tag = "System"
        systemArray.add("HVS")
        systemArray.add("LVL")
        systemArray.add("DDD")
        systemArray.add("EEE")
        //将可选内容与ArrayAdapter连接起来
        systemAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, systemArray)
        //设置下拉列表的风格
        systemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view3.spinnerSystem.adapter = systemAdapter
        //添加事件Spinner事件监听
        view3.spinnerSystem.onItemSelectedListener = OnItemSelectedListener

        view3.spinnerSystem.tag = "Input"
        for (iN in 1 .. 30) {
            inputArray.add("$iN")
        }
        //将可选内容与ArrayAdapter连接起来
        inputAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inputArray)
        //设置下拉列表的风格
        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view3.spinnerInput.adapter = inputAdapter
        //添加事件Spinner事件监听
        view3.spinnerInput.onItemSelectedListener = OnItemSelectedListener


    }

    private lateinit var networkAdapter : ArrayAdapter<String>
    private val networkArray = arrayListOf<String>()
    private lateinit var phaseAdapter : ArrayAdapter<String>
    private val phaseArray = arrayListOf<String>()
    private fun initView4() {
        imgFow.setImageResource(R.drawable.img_fow_four)
        view4.spinnerNetwork.tag = "Network"
        networkArray.add("Off Grid")
        networkArray.add("LVL")
        networkArray.add("DDD")
        networkArray.add("EEE")
        //将可选内容与ArrayAdapter连接起来
        networkAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, networkArray)
        //设置下拉列表的风格
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view4.spinnerNetwork.adapter = networkAdapter
        //添加事件Spinner事件监听
        view4.spinnerNetwork.onItemSelectedListener = OnItemSelectedListener

        phaseArray.add("Single Phase")
        phaseArray.add("LVL")
        phaseArray.add("DDD")
        phaseArray.add("EEE")
        //将可选内容与ArrayAdapter连接起来
        phaseAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phaseArray)
        //设置下拉列表的风格
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view4.spinnerPhase.adapter = phaseAdapter
        //添加事件Spinner事件监听
        view4.spinnerPhase.onItemSelectedListener = OnItemSelectedListener
    }

    /** 下拉框的回调 */
    private val OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (parent?.tag.toString().equals("Inverter")) {
//                showToast(inverterArray[position])
            }
            else if (parent?.tag.toString().equals("System")) {
                onChangeSystemInfo()
            }
            else if (parent?.tag.toString().equals("Input")) {
                onChangeSystemInfo()
            }
            else if (parent?.tag.toString().equals("Network")) {

            }
            else if (parent?.tag.toString().equals("Phase")) {

            }

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            showToast(parent?.tag.toString())
        }

    }


    // 确认要配置的所有参数
    private fun initSureConfigData() {
        llConfigPage.visibility = View.GONE
        llSureConfigData.visibility = View.VISIBLE

        val strTimeInfo = "${view1.tvDate.text} ${view1.tvTime.text}"
        val strInverter = "${view2.spinnerInverter.selectedItem.toString()}"
        val strSystem = "${view3.spinnerSystem.selectedItem.toString()}"
        val strNumber = "${view3.spinnerInput.selectedItem.toString()}"
        val strNetwork = "${view4.spinnerNetwork.selectedItem.toString()}"
        val strPhase = "${view4.spinnerPhase.selectedItem.toString()}"

        tvSureTime.text = strTimeInfo
        tvSureInverter.text = strInverter
        tvSureSystem.text = strSystem
        tvSureNumber.text = strNumber
        tvSureNetwork.text = strNetwork
        tvSurePhase.text = strPhase
    }

    private fun onChangeSystemInfo() {
        val strSystem = spinnerSystem.selectedItem.toString()
        val strInput = spinnerInput.selectedItem.toString()
        // 系统类型
        if (strSystem.equals("HVS")) {
            view3.imgOne.setImageResource(R.drawable.img_hvs_icon)
            view3.imgTwo.setImageResource(R.drawable.img_hvs_icon)
            view3.imgTwo.visibility = View.VISIBLE
            view3.tvNumber.visibility = View.VISIBLE
        }
        else if (strSystem.equals("LVL")) {
            view3.imgTwo.visibility = View.GONE
            view3.tvNumber.visibility = View.GONE
        }

        // 数量
        view3.tvNumber.text = "X$strInput"

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
            MessageInfo.i_RECEIVE_DATA -> {
                val netWorkType = msg.anyInfo as NetWorkType
                myNetState.updateNetInfo(netWorkType)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }


}