package com.smart.bms_byd.ui.more

import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.smart.bms_byd.BaseActivity
import com.smart.bms_byd.R
import kotlinx.android.synthetic.main.activity_visit_website.*


class VisitWebsiteActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_website)

        myNetState.initView(this, true, this);

        initWebView()

        //加载一个网页
        mWebView.loadUrl("http://www.baidu.com/")
        mWebView.setWebViewClient(object : WebViewClient() {
            // 加载失败
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                showToast("visit fail:${errorCode} , $description by $failingUrl")
            }
            // 打开网页时不调用系统浏览器， 而是在本WebView中显示
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })

    }

    private fun initWebView() {
        //声明WebSettings子类
        val webSettings: WebSettings = mWebView.getSettings()
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.javaScriptEnabled = true
        // webSettings.setPluginsEnabled(true) //支持插件
        //设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true //将图片调整到适合webview的大小
        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
        //缩放操作
        webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.displayZoomControls = false //隐藏原生的缩放控件
        //其他细节操作
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE //关闭webview中缓存
        webSettings.allowFileAccess = true //设置可以访问文件
        webSettings.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSettings.loadsImagesAutomatically = true //支持自动加载图片
        webSettings.defaultTextEncodingName = "utf-8" //设置编码格式

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        mWebView.destroy()
    }

}