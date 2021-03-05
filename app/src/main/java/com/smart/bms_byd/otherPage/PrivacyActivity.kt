package com.smart.bms_byd.otherPage

import android.content.Intent
import android.os.Bundle
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import kotlinx.android.synthetic.main.activity_privacy.*

class PrivacyActivity : BaseActivity(), OnPageChangeListener,
    OnLoadCompleteListener,
    OnPageErrorListener{

    val SAMPLE_FILE = "BYD Battery-Box Privacy Policy-20191223-en.pdf"
    var pdfFileName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

//        BaseApplication.getInstance().saveBooleanBySharedPreferences(BaseVolume.FIRST_RUN_APPLICATION,false)

        myNetState.initView(this, true, this);

        btnNext.setOnClickListener {
            if (!cbAgree.isChecked) {
                showToast("请先阅读并勾选条款！")
                return@setOnClickListener
            }
            startActivity(Intent(mContext, UpdateFirmwareActivity().javaClass))
            finish()

        }
        displayFromAsset(SAMPLE_FILE)

    }

    open fun displayFromAsset(assetFileName: String) {
        pdfFileName = assetFileName
        pdfView.fromAsset(SAMPLE_FILE)
            .defaultPage(0)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(0) // in dp
            .onPageError(this)
            .pageFitPolicy(FitPolicy.BOTH)
            .load()
    }

    override fun onDestroy() {
        super.onDestroy()
        myNetState.unRegisterEventBus()

    }

    override fun onPageChanged(page: Int, pageCount: Int) {

    }

    override fun loadComplete(nbPages: Int) {

    }

    override fun onPageError(page: Int, t: Throwable?) {

    }


}