package com.smart.bms_byd.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.smart.bms_byd.BaseApplication;
import com.smart.bms_byd.R;
import com.smart.bms_byd.util.NetWorkType;

public class MyStyleTitleView extends LinearLayout {
    private Context context;
    private ImageView imgLeft,imgNetIcon;
    private TextView tvTitleName,tvRight,tvNetInfo;
    private LinearLayout llNetInfo;
    private MyStyleTitleViewListener myStyleTitleViewListener;
    public MyStyleTitleView(Context context) {
        super(context);
        this.context = context;
    }

    public MyStyleTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.my_title_view, this);
        imgLeft =findViewById(R.id.imgLeft);
        imgNetIcon =findViewById(R.id.imgNetIcon);
        tvTitleName =findViewById(R.id.tvTitleName);
        tvRight =findViewById(R.id.tvRight);
        tvNetInfo =findViewById(R.id.tvNetInfo);
        llNetInfo =findViewById(R.id.llNetInfo);

        imgLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myStyleTitleViewListener != null)
                    myStyleTitleViewListener.onClickListenerByLeft(v);
            }
        });
        tvRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myStyleTitleViewListener != null)
                    myStyleTitleViewListener.onClickListenerByRight(v);
            }
        });
        llNetInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myStyleTitleViewListener != null)
                    myStyleTitleViewListener.onClickListenerByNetInfo(v);
            }
        });

    }

    public MyStyleTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public MyStyleTitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public void initView(Context context,String strTitleName,Boolean isVisibleLeft,Boolean isVisibleRight,Boolean isVisibleNetInfo,MyStyleTitleViewListener myStyleTitleViewListener) {
        this.context = context;
        tvTitleName.setText(strTitleName);
        imgLeft.setVisibility((isVisibleLeft)? View.VISIBLE : View.GONE);
        tvRight.setVisibility((isVisibleRight)? View.VISIBLE : View.GONE);
        llNetInfo.setVisibility((isVisibleNetInfo)? View.VISIBLE : View.GONE);

        this.myStyleTitleViewListener = myStyleTitleViewListener;

        updateNetInfo(BaseApplication.getInstance().nowNetWorkType);
    }

    public void setLeftVisible(Boolean isVisible) {
        imgLeft.setVisibility((isVisible)? View.VISIBLE : View.GONE);
    }

    public void setRightVisible(Boolean isVisible) {
        tvRight.setVisibility((isVisible)? View.VISIBLE : View.GONE);
    }

    public void setNetInfoVisible(Boolean isVisible) {
        llNetInfo.setVisibility((isVisible)? View.VISIBLE : View.GONE);
    }

    public void updateTitleName(String strName) {
        tvTitleName.setText(strName);
    }

    public void updateNetInfo(NetWorkType iNetWorkType) {
        switch (iNetWorkType) {
            // 没有网络
            case NOTHING_NET:
                tvNetInfo.setText("暂无网络");
                break;
            case MOBILE_NET:
                tvNetInfo.setText("移动网络");
                break;
            case WIFI_OTHER:
                tvNetInfo.setText(BaseApplication.getInstance().strNowSSID);
                break;
            case WIFI_DEVICE:
                tvNetInfo.setText(""+ BaseApplication.getInstance().strNowSSID);
                break;
        }
    }

    public interface MyStyleTitleViewListener {

        void onClickListenerByLeft(View view);
        void onClickListenerByRight(View view);
        void onClickListenerByNetInfo(View view);

    }


}
