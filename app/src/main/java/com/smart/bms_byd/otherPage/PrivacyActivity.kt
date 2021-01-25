package com.smart.bms_byd.otherPage

import android.os.Bundle
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.util.BaseVolume
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        BaseApplication.getInstance().saveBooleanBySharedPreferences(BaseVolume.FIRST_RUN_APPLICATION,false)

        myTitleView.initView(this,"隐私政策",false,false,false,null);



    }

}