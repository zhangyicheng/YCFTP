package com.galaxywind.ycftp.server;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.galaxywind.ycftp.BaseActivity;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.utils.ToastUtil;

public class LoginSettingActivity extends BaseActivity {
	private EditText userNameEt, passwordEt;
	private CheckBox showPwdCb;
	private String userName,passWord;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login_setting);
		initData();
		initViews();
	}
	
	private void initData(){
		userName = SharePreferenceUtil.getPreference(this, YCFTPApplication.USER_NAME_SP, YCFTPApplication.USER_NAME);
		passWord = SharePreferenceUtil.getPreference(this, YCFTPApplication.PASSWORD_SP, YCFTPApplication.PASS_WORD);
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		titleTv.setText(R.string.login_settings);
		
		userNameEt = (EditText) findViewById(R.id.userNameEt);
		userNameEt.setText(userName);
		
		passwordEt = (EditText) findViewById(R.id.passWordEt);
		passwordEt.setText(passWord);
		
		showPwdCb = (CheckBox) findViewById(R.id.showPwdCb);
		showPwdCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
                    //如果选中，显示密码      
					passwordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    //否则隐藏密码
                	passwordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
			}
		});
		
		if(null!=actionBtn){
			actionBtn.setVisibility(View.VISIBLE);
		}
		if(null!=actionTv){
			actionTv.setText(R.string.save);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch(v.getId()){
		case R.id.actionBtn:
			SharePreferenceUtil.savePreference(this, YCFTPApplication.USER_NAME_SP, YCFTPApplication.USER_NAME, userNameEt.getText().toString());
			SharePreferenceUtil.savePreference(this, YCFTPApplication.PASSWORD_SP, YCFTPApplication.PASS_WORD, passwordEt.getText().toString());
			finish();
			break;
		}
	}
}
