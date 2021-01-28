package com.smart.bms_byd.ui.more

import android.content.Intent
import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.BaseVolume
import kotlinx.android.synthetic.main.activity_privacyinfo.*

class PrivacyInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacyinfo)

        myNetState.initView(this,true,this);

        btnNext.setOnClickListener {

        }

    }

}