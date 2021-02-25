package com.smart.bms_byd.ui.more

import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import kotlinx.android.synthetic.main.activity_privacyinfo.*

class PrivacyInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacyinfo)

        myNetState.initView(this,true,this);

        imgLeft.setOnClickListener { finish() }

        btnNext.setOnClickListener {

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        myNetState.unRegisterEventBus()

    }


}