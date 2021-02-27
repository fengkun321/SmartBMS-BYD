package com.smart.bms_byd.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.bms_byd.R;
import com.smart.bms_byd.util.DateFormatUtils;

public class SelectTimeWindowDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private PeriodListener listener;
	private TextView tvStartTime,tvStopTime;
	private MyCustomDatePicker myCustomDatePicker;
	private LinearLayout llTimeInfo,llSelectTime;

	public SelectTimeWindowDialog(Context context) {
		super(context);
		this.context = context;
	}

	public SelectTimeWindowDialog(Context context, int theme,PeriodListener listener) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
	}

	
	/****
	 * 
	 * @author mqw
	 *
	 */
	public interface PeriodListener {
		public void refreshListener(String strStartTime,String strStopTime);
		public void cancelListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.window_select_time);
		llTimeInfo = findViewById(R.id.llTimeInfo);
		llSelectTime = findViewById(R.id.llSelectTime);
		tvStartTime = findViewById(R.id.tvStartTime);
		tvStopTime = findViewById(R.id.tvStopTime);
		myCustomDatePicker = findViewById(R.id.myCustomDatePicker);

		setCancelable(false);
		findViewById(R.id.confirm_btn).setOnClickListener(this);
		findViewById(R.id.cancel_btn).setOnClickListener(this);
		findViewById(R.id.ok_btn).setOnClickListener(this);
		findViewById(R.id.imgDismiss).setOnClickListener(this);
		findViewById(R.id.rlStartTime).setOnClickListener(this);
		findViewById(R.id.rlStopTime).setOnClickListener(this);
		findViewById(R.id.imgDismiss).setOnClickListener(this);

		
	}

	public void showDialogByTime() {
		if (!isShowing()) show();
		long beginTimestamp = DateFormatUtils.str2Long("2020-01-01", false);
		long endTimestamp = System.currentTimeMillis();
		String strNowTime = DateFormatUtils.long2Str(endTimestamp, false);
		myCustomDatePicker.initView(beginTimestamp, endTimestamp, new MyCustomDatePicker.Callback() {
			@Override
			public void onTimeSelected(long timestamp) {
//				tvTimeStamp.setText(timestamp+"");
//				tvTimeInfo.setText(DateFormatUtils.long2Str(timestamp, false));
			}
		});
		// 不显示时和分
		myCustomDatePicker.setCanShowPreciseTime(false);
		myCustomDatePicker.updateSelectTime(strNowTime);
		tvStartTime.setText(strNowTime);
		tvStopTime.setText(strNowTime);

	}

	private boolean isStartShow = true;
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.rlStartTime:
			isStartShow = true;
			String strTime = tvStartTime.getText().toString();
			llTimeInfo.setVisibility(View.GONE);
			llSelectTime.setVisibility(View.VISIBLE);
			myCustomDatePicker.updateSelectTime(strTime);
			break;
		case R.id.rlStopTime:
			isStartShow = false;
			String strTime1 = tvStopTime.getText().toString();
			llTimeInfo.setVisibility(View.GONE);
			llSelectTime.setVisibility(View.VISIBLE);
			myCustomDatePicker.updateSelectTime(strTime1);
			break;
		case R.id.confirm_btn:
			dismiss();
			if (listener != null) {
				String strStart = tvStartTime.getText().toString();
				String strStop = tvStopTime.getText().toString();
				listener.refreshListener(strStart,strStop);
			}
			break;
		case R.id.cancel_btn:
			llTimeInfo.setVisibility(View.VISIBLE);
			llSelectTime.setVisibility(View.GONE);
			break;
		case R.id.ok_btn:
			llTimeInfo.setVisibility(View.VISIBLE);
			llSelectTime.setVisibility(View.GONE);
			String strNowTime = DateFormatUtils.long2Str(myCustomDatePicker.mSelectedTime.getTimeInMillis(), false);
			if (isStartShow)
				tvStartTime.setText(strNowTime);
			else
				tvStopTime.setText(strNowTime);
			break;
		case R.id.imgDismiss:
				dismiss();
				if (listener != null)
					listener.cancelListener();
			break;

		default:
			break;
		}
	}
}