package com.galaxywind.ycftp.server;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.galaxywind.ycftp.BaseActivity;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PortSettingActivity extends BaseActivity {
	@ViewInject(R.id.portEt)
	private EditText portEt;
	
	@ViewInject(R.id.okBtn)
	private Button okBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_port_setting);
		ViewUtils.inject(this);
		initActionBar();
		initViews();
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		titleTv.setText("Port Setting");
	}
	
	@OnClick(R.id.okBtn)
	public void onViewClick(View v){
		Intent resultIntent = new Intent();
		resultIntent.putExtra(YCFTPApplication.PORT, portEt.getText().toString());
		setResult(YCFTPApplication.PORT_RESULT_CODE, resultIntent);
		finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}
}
