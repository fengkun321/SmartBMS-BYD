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
	private TextView tvContent;
	private boolean isShowTost;
	
	private TextView titleTv;
	private String period = "";
	private PeriodListener listener;
	private String defaultName = "",title;
	private String strCancel = "";
	private String strConfirm = "";

	
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
		tvContent = (TextView) findViewById(R.id.areaName);
		titleTv = (TextView) findViewById(R.id.dialog_title);
		titleTv.setText(title);

		if (strCancel.equalsIgnoreCase("")) strCancel = "cancel";
		if (strConfirm.equalsIgnoreCase("")) strConfirm = "continue";

		if (isShowTost) {
			cancelBtn.setVisibility(View.GONE);
		}

		cancelBtn.setText(strCancel);
		confirmBtn.setText(strConfirm);

		setCancelable(false);
		
		confirmBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		tvContent.setText(defaultName);
		
		
	}
	
	public void setCancelText(String cancelText) {
		strCancel = cancelText;
		if (strCancel.equalsIgnoreCase("")) strCancel = "cancel";
		if (cancelBtn != null) cancelBtn.setText(strCancel);
	}

	public void setConfirmText(String confirmText) {
		strConfirm = confirmText;
		if (strConfirm.equalsIgnoreCase("")) strConfirm = "continue";
		if (confirmBtn != null) confirmBtn.setText(strConfirm);
	}


	public void setShowTost(boolean showTost) {
		isShowTost = showTost;
		if (cancelBtn == null) return;
		if (isShowTost)
			cancelBtn.setVisibility(View.GONE);
		else
			cancelBtn.setVisibility(View.VISIBLE);

	}

	public void setListener(PeriodListener listener) {
		this.listener = listener;
	}

	public void updateTitle(String strTitle) {
		this.title = strTitle;
		if (titleTv != null) {
			titleTv.setText(title);
		}
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