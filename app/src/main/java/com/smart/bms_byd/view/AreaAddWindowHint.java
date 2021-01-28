package com.smart.bms_byd.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smart.bms_byd.R;

public class AreaAddWindowHint extends Dialog implements View.OnClickListener {
	private Context context;
	private Button confirmBtn;
	private Button cancelBtn;
	private View view1;
	private TextView tvContent;
	private boolean isShowTost;
	
	private TextView titleTv;
	private String period = "";
	private PeriodListener listener;
	private String defaultName = "",title;
	
	private String strConfirmText = "";
	
	public AreaAddWindowHint(Context context) {
		super(context);
		this.context = context;
	}

	public AreaAddWindowHint(Context context, int theme, String titleName, PeriodListener listener, String defaultName) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
		this.defaultName = defaultName;
		this.title = titleName;
	}
	
	public AreaAddWindowHint(Context context, int theme, String titleName, PeriodListener listener, String defaultName, boolean isTost) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
		this.defaultName = defaultName;
		this.title = titleName;
		this.isShowTost = isTost;
	}

	
	/****
	 * 
	 * @author mqw
	 *
	 */
	public interface PeriodListener {
		public void refreshListener(String string);
		public void cancelListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.window_area_hint);
		confirmBtn = (Button) findViewById(R.id.confirm_btn);
		cancelBtn = (Button) findViewById(R.id.cancel_btn);
		view1 = findViewById(R.id.view1);
		tvContent = (TextView) findViewById(R.id.areaName);
		titleTv = (TextView) findViewById(R.id.dialog_title);
		titleTv.setText(title);
		
		if (isShowTost) {
			view1.setVisibility(View.GONE);
			cancelBtn.setVisibility(View.GONE);
		}
		
		if (!strConfirmText.equals("")) {
			confirmBtn.setText(strConfirmText);
		}

		setCancelable(false);
		
		confirmBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		tvContent.setText(defaultName);
		
		
	}
	
	public void setConfirmText(String confirmText) {
		strConfirmText = confirmText;
		
	}

	public void setShowTost(boolean showTost) {
		isShowTost = showTost;
		if (view1 != null) {
			if (isShowTost) {
				view1.setVisibility(View.GONE);
				cancelBtn.setVisibility(View.GONE);
			}
			else {
				view1.setVisibility(View.VISIBLE);
				cancelBtn.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setListener(PeriodListener listener) {
		this.listener = listener;
	}
	
	/** ������ʾ���� */
	public void updateContent(String strNewContent) {
		this.defaultName = strNewContent;
		if (tvContent != null) {
			tvContent.setText(defaultName);
		}

	}
		 
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.cancel_btn:
			dismiss();
			if (listener != null)
				listener.cancelListener();
			break;
		case R.id.confirm_btn:
				dismiss();
				if (listener != null)
					listener.refreshListener(period);


			break;

		default:
			break;
		}
	}
}