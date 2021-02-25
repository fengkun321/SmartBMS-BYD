package com.smart.bms_byd.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smart.bms_byd.BaseApplication
import com.smart.bms_byd.R
import com.smart.bms_byd.ui.more.NotificationMessageActivity
import com.smart.bms_byd.ui.more.PrivacyInfoActivity
import com.smart.bms_byd.ui.more.VisitWebsiteActivity
import com.smart.bms_byd.util.BaseVolume
import kotlinx.android.synthetic.main.fragment_more.*

class MoreFragment : Fragment(),View.OnClickListener{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {

        tvVersion.text = "V ${BaseVolume.getVersion(BaseApplication.getInstance())}"

        rlNotification.setOnClickListener(this)
        rlPrivacy.setOnClickListener(this)
        rlVisit.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rlNotification -> {
                startActivity(Intent(context, NotificationMessageActivity().javaClass))
            }
            R.id.rlPrivacy -> {
                startActivity(Intent(context, PrivacyInfoActivity().javaClass))
            }
            R.id.rlVisit -> {
                startActivity(Intent(context, VisitWebsiteActivity().javaClass))
            }

        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // 隐藏啦
        if (hidden) {

        }
        // 显示啦
        else {

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
    }



}