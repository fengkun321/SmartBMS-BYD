package com.smart.bms_byd

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smart.bms_byd.otherPage.ConnectWIFIActivity
import com.smart.bms_byd.util.BaseVolume
import com.smart.bms_byd.view.AreaAddWindowHint
import com.smart.bms_byd.view.LoadingDialog
import com.smart.bms_byd.view.NetStateInfoView


open class BaseActivity : AppCompatActivity(),
    NetStateInfoView.NetStateInfoListener {

    protected lateinit var mContext: Context
    protected lateinit var mHandler: Handler
    public lateinit var loadingDialog: LoadingDialog
    protected lateinit var areaAddWindowHint: AreaAddWindowHint
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mContext = this
        mHandler = Handler()
        loadingDialog = LoadingDialog(mContext, R.style.dialog_style)
        areaAddWindowHint = AreaAddWindowHint(mContext, R.style.dialog_style, "系统提示", null, "是否")

        registerBroadcast()

    }

    private fun registerBroadcast() {
        val filter = IntentFilter()
        filter.addAction(BaseVolume.COMMAND_SEND_START)
        filter.addAction(BaseVolume.COMMAND_SEND_TIMEOUT)
        filter.addAction(BaseVolume.COMMAND_SEND_STOP)
        registerReceiver(mBroadcastReceiver, filter)
    }

    protected fun showToast(str: String?) {
        mHandler.post { Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show() }
    }

    protected fun showToastAndTitle(str: String?) {
        mHandler.post { Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show() }
        BaseApplication.getInstance().updateMessageInfo(str)
    }

    protected open fun showDialog(strTitle : String,strMsg: String, periodListener: AreaAddWindowHint.PeriodListener?, isToast: Boolean) {
        if (areaAddWindowHint.isShowing()) return
        areaAddWindowHint.updateTitle(strTitle)
        areaAddWindowHint.updateContent(strMsg)
        areaAddWindowHint.setShowTost(isToast)
        areaAddWindowHint.setCancelText("")
        areaAddWindowHint.setConfirmText("")
        areaAddWindowHint.setListener(periodListener)
        areaAddWindowHint.show()
    }

    protected open fun showDialog(strTitle : String,strMsg: String, periodListener: AreaAddWindowHint.PeriodListener?, isToast: Boolean,
                                  strCancel:String,strConfirm:String) {
        if (areaAddWindowHint.isShowing()) return
        areaAddWindowHint.updateTitle(strTitle)
        areaAddWindowHint.updateContent(strMsg)
        areaAddWindowHint.setShowTost(isToast)
        areaAddWindowHint.setCancelText(strCancel)
        areaAddWindowHint.setConfirmText(strConfirm)
        areaAddWindowHint.setListener(periodListener)
        areaAddWindowHint.show()
    }

    protected fun dismissDialog() {
        mHandler.post {
            loadingDialog.dismiss()
        }

    }




    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                val view = currentFocus
                hideKeyboard(ev, view, this@BaseActivity) //调用方法判断是否需要隐藏键盘
            }
            else -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 根据传入控件的坐标和用户的焦点坐标，判断是否隐藏键盘，如果点击的位置在控件内，则不隐藏键盘
     *
     * @param view
     * 控件view
     * @param event
     * 焦点位置
     * @return 是否隐藏
     */
    fun hideKeyboard(event: MotionEvent, view: View?, activity: Activity) {
        try {
            if (view != null && view is EditText) {
                val location = intArrayOf(0, 0)
                view.getLocationInWindow(location)
                val left = location[0]
                val top = location[1]
                val right = (left
                        + view.getWidth())
                val bootom = top + view.getHeight()
                // 判断焦点位置坐标是否在空间内，如果位置在控件外，则隐藏键盘
                if (event.rawX < left || event.rawX > right || event.y < top || event.rawY > bootom) {
                    // 隐藏键盘
                    val token = view.getWindowToken()
                    val inputMethodManager = activity
                        .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(
                        token,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BaseVolume.COMMAND_SEND_START) {
                if (!loadingDialog.isShowing) {
                    loadingDialog.show()
                }
            } else if (intent.action == BaseVolume.COMMAND_SEND_TIMEOUT) {
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
                showToast("操作超时！")
            } else if (intent.action == BaseVolume.COMMAND_SEND_STOP) {
//                if (loadingDialog.isShowing) {
//                    loadingDialog.dismiss()
//                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }

    // 网络信息点击事件
    override fun onClickListenerByNetInfo(view: View?) {
        startActivity(Intent(mContext,ConnectWIFIActivity().javaClass).putExtra("wifiSign",BaseApplication.DEVICE_WIFI_SIGN))
    }


}