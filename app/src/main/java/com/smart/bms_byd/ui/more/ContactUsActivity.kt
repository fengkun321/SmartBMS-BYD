package com.smart.bms_byd.ui.more

import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import kotlinx.android.synthetic.main.activity_contactus.*


class ContactUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactus)

        myNetState.initView(this, true, this);



    }



}