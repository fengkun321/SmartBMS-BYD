package com.smart.bms_byd.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.smart.bms_byd.BaseApplication;
import com.smart.bms_byd.R;
import com.smart.bms_byd.util.NetWorkType;
import com.smartIPandeInfo.data.MessageInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NetStateInfoView extends LinearLayout {
    private Context context;
    private ImageView imgNetIcon,imgDelete;
    private TextView tvErrorCodeInfo,tvNetInfo,tvMessageInfo;
    private RelativeLayout rlMessageInfo;
    private LinearLayout llErrorCode,llNetInfo;
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
        tvErrorCodeInfo =findViewById(R.id.tvErrorCodeInfo);
        tvNetInfo =findViewById(R.id.tvNetInfo);
        tvMessageInfo =findViewById(R.id.tvMessageInfo);
        llErrorCode =findViewById(R.id.llErrorCode);
        llNetInfo =findViewById(R.id.llNetInfo);
        rlMessageInfo =findViewById(R.id.rlMessageInfo);
        imgDelete =findViewById(R.id.imgDelete);

        llNetInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myStyleTitleViewListener != null)
                    myStyleTitleViewListener.onClickListenerByNetInfo(v);
            }
        });

        imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除消息
                EventBus.getDefault().post(new MessageInfo(MessageInfo.i_MESSAGE_INFO,""));
//                rlMessageInfo.setVisibility(View.GONE);
            }
        });

    }

    public NetStateInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NetStateInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public void initView(Context context, Boolean isVisibleNetInfo, NetStateInfoListener myStyleTitleViewListener) {
        this.context = context;
        llNetInfo.setVisibility((isVisibleNetInfo)? View.VISIBLE : View.GONE);
        this.myStyleTitleViewListener = myStyleTitleViewListener;
        updateNetInfo(BaseApplication.getInstance().nowNetWorkType);

        EventBus.getDefault().register(this);

        setMessage(BaseApplication.getInstance().strMessageInfo);
        setErrorCode(BaseApplication.getInstance().strErrorInfo);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessageInfo(MessageInfo msg) {
        switch (msg.getICode()) {
            case MessageInfo.i_NET_WORK_STATE:
                NetWorkType netWorkType = (NetWorkType) msg.getAnyInfo();
                updateNetInfo(netWorkType);
                break;
            case MessageInfo.i_ERROR_INFO:
                String strErrorInfo = msg.getAnyInfo().toString();
                setErrorCode(strErrorInfo);
                break;
            case MessageInfo.i_MESSAGE_INFO:
                String strMsgInfo = msg.getAnyInfo().toString();
                setMessage(strMsgInfo);
                break;
            case MessageInfo.i_TCP_CONNECT_FAIL:
                String strTCPFailInfo = msg.getAnyInfo().toString();
                setMessage("Disconnected:"+strTCPFailInfo);
                break;
            case MessageInfo.i_TCP_CONNECT_SUCCESS:
                setMessage("Connected");
                break;
        }
    }

    public void setMessage(String strMessage) {
        this.tvMessageInfo.setText(strMessage);
        if (!strMessage.equalsIgnoreCase(""))
            BaseApplication.getInstance().isShowMessageInfo = View.VISIBLE;
        else
            BaseApplication.getInstance().isShowMessageInfo = View.GONE;
        rlMessageInfo.setVisibility(BaseApplication.getInstance().isShowMessageInfo);
    }
    public void setErrorCode(String strErrorInfo) {
        this.tvErrorCodeInfo.setText(strErrorInfo);
        if (!strErrorInfo.equalsIgnoreCase(""))
            BaseApplication.getInstance().isShowErrorInfo = View.VISIBLE;
        else
            BaseApplication.getInstance().isShowErrorInfo = View.GONE;
        llErrorCode.setVisibility(BaseApplication.getInstance().isShowErrorInfo);
    }

    public void updateNetInfo(NetWorkType iNetWorkType) {
        switch (iNetWorkType) {
            // 没有网络
            case NOTHING_NET:
                tvNetInfo.setText("Unreachable");
                imgNetIcon.setVisibility(View.GONE);
                break;
            case MOBILE_NET:
                tvNetInfo.setText("WWAN");
                imgNetIcon.setVisibility(View.GONE);
                break;
            case WIFI_OTHER:
                tvNetInfo.setText(BaseApplication.getInstance().strNowSSID);
                imgNetIcon.setVisibility(View.VISIBLE);
                break;
            case WIFI_DEVICE:
                tvNetInfo.setText(""+ BaseApplication.getInstance().strNowSSID);
                imgNetIcon.setVisibility(View.VISIBLE);
                break;
        }
    }

    public interface NetStateInfoListener {

        void onClickListenerByNetInfo(View view);

    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }




}
