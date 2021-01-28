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

public class NetStateInfoView extends LinearLayout {
    private Context context;
    private ImageView imgNetIcon;
    private TextView tvNetInfo;
    private LinearLayout llNetInfo;
    private NetStateInfoListener myStyleTitleViewListener;
    public NetStateInfoView(Context context) {
        super(context);
        this.context = context;
    }

    public NetStateInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.net_state_view, this);

        imgNetIcon =findViewById(R.id.imgNetIcon);
        tvNetInfo =findViewById(R.id.tvNetInfo);
        llNetInfo =findViewById(R.id.llNetInfo);

        llNetInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myStyleTitleViewListener != null)
                    myStyleTitleViewListener.onClickListenerByNetInfo(v);
            }
        });

    }

    public NetStateInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public NetStateInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public void initView(Context context, Boolean isVisibleNetInfo, NetStateInfoListener myStyleTitleViewListener) {
        this.context = context;
        llNetInfo.setVisibility((isVisibleNetInfo)? View.VISIBLE : View.GONE);
        this.myStyleTitleViewListener = myStyleTitleViewListener;
        updateNetInfo(BaseApplication.getInstance().nowNetWorkType);
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

    public interface NetStateInfoListener {

        void onClickListenerByNetInfo(View view);

    }


}
