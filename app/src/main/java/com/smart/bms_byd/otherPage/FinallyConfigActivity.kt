package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.MainActivity
import com.smart.bms_byd.R
import kotlinx.android.synthetic.main.activity_finallyconfig.*


class FinallyConfigActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finallyconfig)

        myNetState.initView(mContext, true, this)

        btnNext.setOnClickListener {
            val intent: Intent = Intent(mContext,MainActivity().javaClass)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

}