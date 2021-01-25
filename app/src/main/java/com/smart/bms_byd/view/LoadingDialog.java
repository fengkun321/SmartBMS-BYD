package com.smart.bms_byd.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.smart.bms_byd.R;

public class LoadingDialog extends Dialog {

	private TextView mTextView;
	private String strMsg = "";
	public LoadingDialog(Context context) {
		super(context);
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.loading_dialog);
		this.setCanceledOnTouchOutside(false);
		mTextView = (TextView) findViewById(R.id.loading_text);
		if (strMsg != "") {
			mTextView.setText(strMsg);
		}
	}



	public void showAndMsg(String strText) {
		this.strMsg = strText;
		if (!isShowing())
			show();
		if (this.mTextView != null) {
			this.mTextView.setText(strText);
		}
	}

	public void updateStatusText(String strText) {
		this.strMsg = strText;
		if (!isShowing())
			show();
		if (this.mTextView != null) {
			this.mTextView.setText(strText);
		}

	}

}
