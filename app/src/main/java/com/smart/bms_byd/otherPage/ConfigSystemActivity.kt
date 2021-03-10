package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.viewpager.widget.ViewPager
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.MainActivityTest
import com.smart.bms_byd.R
import com.smart.bms_byd.adapter.MyPagerAdapter
import com.smart.bms_byd.data.AnalysisInfo
import com.smart.bms_byd.data.CreateControlData
import com.smart.bms_byd.data.DeviceStateInfo
import com.smart.bms_byd.tcpclient.TCPClientS
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.util.NetWorkType
import com.smartIPandeInfo.data.MessageInfo
import kotlinx.android.synthetic.main.activity_configsystem.*
import kotlinx.android.synthetic.main.page_four.*
import kotlinx.android.synthetic.main.page_four.view.*
import kotlinx.android.synthetic.main.page_one.view.*
import kotlinx.android.synthetic.main.page_three.*
import kotlinx.android.synthetic.main.page_three.view.*
import kotlinx.android.synthetic.main.page_two.*
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
                    startSetWorkData()
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

    private var strNowTimeInfo = ""
    private fun initView1() {
        imgFow.setImageResource(R.drawable.img_fow_one)
//        strNowTimeInfo = BaseVolume.getNowSystemTime()
        strNowTimeInfo = DeviceStateInfo.getInstance().BCU_Time
        view1.tvDate.text = BaseVolume.getDateInfo(strNowTimeInfo.split(" ")[0])
        view1.tvTime.text = strNowTimeInfo.split(" ")[1]
    }

    private lateinit var inverterAdapter : ArrayAdapter<String>
    private var inverterArray = arrayListOf<String>()
    private fun initView2() {
        imgFow.setImageResource(R.drawable.img_fow_two)
        view2.spinnerInverter.tag = "Inverter"
        inverterArray.clear()
        // 低压
        if ((DeviceStateInfo.getInstance().BCU_SN.indexOf("P02") == 0) || (DeviceStateInfo.getInstance().BCU_SN.indexOf("P01") == 0)) {
            inverterArray = arrayListOf("GOODWE","Selectronic"
                ,"SMA","Victron","SUNTECH","Studer","SolarEdge","Sungrow")
        }
        // 高压
        else if (DeviceStateInfo.getInstance().BCU_SN.indexOf("P03") == 0){
            inverterArray = arrayListOf("Fronius","GOODWE","KOSTAL","SMA SBS 3.7-6.0"
                ,"Sungrow","Kaco","Ingeteam","Schneider","SMA SBS 2.5")
        }


        //将可选内容与ArrayAdapter连接起来
        inverterAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inverterArray)
        //设置下拉列表的风格
        inverterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view2.spinnerInverter.adapter = inverterAdapter
        //添加事件Spinner事件监听
        view2.spinnerInverter.onItemSelectedListener = OnItemSelectedListener
        // 自动选中当前选项
        val strNowInverterName = DeviceStateInfo.getInstance().inverterAllArray[DeviceStateInfo.getInstance().Inverter_Type]
        for (iN in inverterArray.indices) {
            if (inverterArray[iN].equals(strNowInverterName)) {
                view2.spinnerInverter.setSelection(iN)
                break
            }
        }


    }

    private lateinit var systemAdapter : ArrayAdapter<String>
    private var systemArray = arrayListOf<String>()
    private lateinit var inputAdapter : ArrayAdapter<String>
    private var inputArray = arrayListOf<String>()
    private fun initView3() {
        imgFow.setImageResource(R.drawable.img_fow_three)

        view3.spinnerSystem.tag = "System"
        systemArray.clear()
        // 低压
        if ((DeviceStateInfo.getInstance().BCU_SN.indexOf("P02") == 0) || (DeviceStateInfo.getInstance().BCU_SN.indexOf("P01") == 0)) {
            systemArray.addAll(DeviceStateInfo.getInstance().lowSystemArray)
        }
        // 高压
        else if (DeviceStateInfo.getInstance().BCU_SN.indexOf("P03") == 0){
            systemArray.addAll(DeviceStateInfo.getInstance().highSystemArray)
        }

        //将可选内容与ArrayAdapter连接起来
        systemAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, systemArray)
        //设置下拉列表的风格
        systemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view3.spinnerSystem.adapter = systemAdapter

        // 自动选中当前选项
        view3.spinnerSystem.setSelection(DeviceStateInfo.getInstance().BMS_Type)

        view3.spinnerInput.tag = "Input"
        //将可选内容与ArrayAdapter连接起来
        updateSelectInputArrayBySystem()
//        inputAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inputArray)
        //设置下拉列表的风格
//        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
//        view3.spinnerInput.adapter = inputAdapter
        //添加事件Spinner事件监听
        view3.spinnerInput.onItemSelectedListener = OnItemSelectedListener

        //添加事件Spinner事件监听
        view3.spinnerSystem.onItemSelectedListener = OnItemSelectedListener



    }

    /** 根据高低压类型，动态变换数量选择器 */
    private fun updateSelectInputArrayBySystem() {
        inputArray.clear()
        var iStartN = 0
        var iStopN = 0
        val strSystemSelect = spinnerSystem.selectedItem.toString()
        if (strSystemSelect.equals("HVL") || strSystemSelect.equals("HVM")) {
            iStartN = 3
            iStopN = 8
        }
        else if (strSystemSelect.equals("HVS")) {
            iStartN = 2
            iStopN = 5
        }
        else if (strSystemSelect.equals("LVL") || strSystemSelect.equals("LV Flex") ||
            strSystemSelect.equals("LVS")) {
            iStartN = 1
            iStopN = 64
        }
        for (iN in iStartN .. iStopN) {
            inputArray.add("$iN")
        }
        //将可选内容与ArrayAdapter连接起来
        inputAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inputArray)
        //设置下拉列表的风格
        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view3.spinnerInput.adapter = inputAdapter
        // 自动选中当前选项
        for (iN in inputArray.indices) {
            if (inputArray[iN].equals("${DeviceStateInfo.getInstance().getBMSNumberNow()}")) {
                view3.spinnerInput.setSelection(iN)
                break
            }
        }
    }

    private lateinit var networkAdapter : ArrayAdapter<String>
    private lateinit var phaseAdapter : ArrayAdapter<String>
    private fun initView4() {
        imgFow.setImageResource(R.drawable.img_fow_four)
        view4.spinnerNetwork.tag = "Network"
        //将可选内容与ArrayAdapter连接起来
        networkAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DeviceStateInfo.getInstance().networkArray)
        //设置下拉列表的风格
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view4.spinnerNetwork.adapter = networkAdapter
        //添加事件Spinner事件监听
        view4.spinnerNetwork.onItemSelectedListener = OnItemSelectedListener
        // 自动选中当前选项
        view4.spinnerNetwork.setSelection(DeviceStateInfo.getInstance().User_Scene)

        //将可选内容与ArrayAdapter连接起来
        phaseAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DeviceStateInfo.getInstance().phaseArray)
        //设置下拉列表的风格
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //将adapter 添加到spinner中
        view4.spinnerPhase.adapter = phaseAdapter
        //添加事件Spinner事件监听
        view4.spinnerPhase.onItemSelectedListener = OnItemSelectedListener
        // 自动选中当前选项
        view4.spinnerPhase.setSelection(DeviceStateInfo.getInstance().Dan_or_San)
    }

    /** 下拉框的回调 */
    private val OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (parent?.tag.toString().equals("Inverter")) {
//                showToast(inverterArray[position])
            }
            else if (parent?.tag.toString().equals("System")) {
                updateSelectInputArrayBySystem()
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
        val strSystemSelect = spinnerSystem.selectedItem.toString()
        val strInputNumberSelect = spinnerInput.selectedItem.toString()
        // 系统类型
        if (strSystemSelect.equals("LVS") && strInputNumberSelect.toInt() > 6) {
            view3.imgOne.setImageResource(R.drawable.img_lvs_icon)
            view3.imgTwo.setImageResource(R.drawable.img_lvs_icon)
            view3.imgTwo.visibility = View.VISIBLE
            view3.tvNumber.visibility = View.GONE
        }
        else {
            view3.imgOne.setImageResource(R.drawable.img_hvs_icon)
            view3.imgTwo.visibility = View.GONE
            view3.tvNumber.visibility = View.GONE
        }
//        else if (strSystem.equals("LVS")) {
//            view3.imgTwo.visibility = View.GONE
//            view3.tvNumber.visibility = View.GONE
//        }

        // 数量
        view3.tvNumber.text = "X$strInputNumberSelect"

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

    /** 开始设置参数 0x0010 3个寄存器 */
    private fun startSetWorkData() {
        var strInverterType = ""
        var strInverterItem = view2.spinnerInverter.selectedItem.toString()
        // 如果是 GOODWE,则需要根据当前高低压 单独处理
        if (strInverterItem.equals("GOODWE")) {
            // 低压
            if ((DeviceStateInfo.getInstance().BCU_SN.indexOf("P02") == 0) || (DeviceStateInfo.getInstance().BCU_SN.indexOf("P01") == 0))
                strInverterType = "02"
            // 高压
            else if (DeviceStateInfo.getInstance().BCU_SN.indexOf("P03") == 0)
                strInverterType = "01"
        }
        else {
            for (iN in DeviceStateInfo.getInstance().inverterAllArray.indices) {
                if (DeviceStateInfo.getInstance().inverterAllArray[iN].equals(strInverterItem)) {
                    strInverterType = String.format("%02X",iN)
                    break
                }
            }
        }

        var strBMSNum = String.format("%02X",view3.spinnerInput.selectedItem.toString().toInt())
        // 高压，则只改变低四位
        if (DeviceStateInfo.getInstance().isHighVolInfo())
            strBMSNum = "${DeviceStateInfo.getInstance().BMS_Number_HEX.substring(0,1)}${String.format("%01X",view3.spinnerInput.selectedItem.toString().toInt())}"

        var strBMSType = String.format("%02X",view3.spinnerSystem.selectedItemPosition)

        val strUseScene = String.format("%02X",view4.spinnerNetwork.selectedItemPosition)
        val strDanSan = String.format("%02X",view4.spinnerPhase.selectedItemPosition)
        loadingDialog.showAndMsg("setting...")
        // 设置从逆变器类型到单三相 五个参数
        val strSendData = CreateControlData.writeMoreByAddress("0010",
            strInverterType+strBMSNum+strBMSType+strUseScene+strDanSan+"00")
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)
    }

    /** 设置BCU时间 0x0063  3个寄存器 */
    private fun setBCUTimeInfo() {
        val strDateInfo = strNowTimeInfo.split(" ")[0]
        val strTimeInfo = strNowTimeInfo.split(" ")[1]
        val strY = String.format("%02X",strDateInfo.split("-")[0].substring(2).toInt())
        val strM = String.format("%02X",strDateInfo.split("-")[1].toInt())
        val strD = String.format("%02X",strDateInfo.split("-")[2].toInt())
        val strH = String.format("%02X",strTimeInfo.split(":")[0].toInt())
        val strMin = String.format("%02X",strTimeInfo.split(":")[1].toInt())
        val strSec = String.format("%02X",strTimeInfo.split(":")[2].toInt())
        // 设置时间
        val strSendData = CreateControlData.writeMoreByAddress("0063",
            strY+strM+strD+strH+strMin+strSec)
        BaseApplication.getInstance().StartSendDataByTCPTimeOut(strSendData)

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onReceiveMessageInfo(msg: MessageInfo) {
        when(msg.iCode) {
            // 接收数据
            MessageInfo.i_RECEIVE_DATA -> {
                val analysisInfo = msg.anyInfo as AnalysisInfo
                if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE)) {
                    // 在设置 逆变器类型到单三相 五个参数
                    if (analysisInfo.strWriteMoreAddress == "0010" &&
                        analysisInfo.iWriteMoreRegisterNumber == 3 &&
                        loadingDialog.isShowing) {

                        loadingDialog.dismiss()
                        BaseApplication.getInstance().StopSend()
                        llSureConfigData.visibility = View.GONE
                        llSuccess.visibility = View.VISIBLE
                        btnNext.text = "OK"

                        // 继续设置时间
//                        setBCUTimeInfo()
                    }
                    // 在设置 BCU时间标定
                    else if (analysisInfo.strWriteMoreAddress == "0063" &&
                        analysisInfo.iWriteMoreRegisterNumber == 3 &&
                        loadingDialog.isShowing) {
//                        loadingDialog.dismiss()
//                        BaseApplication.getInstance().StopSend()
//                        llSureConfigData.visibility = View.GONE
//                        llSuccess.visibility = View.VISIBLE
//                        btnNext.text = "OK"
                    }
                }
                // 设置失败的处理
                else if (analysisInfo.strType.equals(BaseVolume.CMD_TYPE_WRITE_MORE_ERROR)) {
                    BaseApplication.getInstance().StopSend()
                    val strMsg = analysisInfo.strErrorInfo
                    myNetState.setMessage(strMsg)
                    showToast(strMsg)
                }

            }
            MessageInfo.i_SEND_DATA_ERROR -> {
                val strError = msg.anyInfo.toString()
                BaseApplication.getInstance().StopSend()
                loadingDialog.dismiss()
                showToast(strError)
            }
        }

    }



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        myNetState.unRegisterEventBus()
    }


}