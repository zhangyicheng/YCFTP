package com.galaxywind.ycftp;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
/**
 * Activity基类
 * @author zyc
 *
 */
public class BaseActivity extends FragmentActivity  implements OnClickListener{
	
	/*返回按钮,右上操作按钮*/
	protected View backBtn,actionBtn;
	/*标题,操作按钮名*/
	protected TextView titleTv,actionTv;
	protected Dialog mydialog;
	public FragmentManager fragmentManager;
	public FragmentTransaction fragmentTransaction;
	/*ListView为空时的View*/
	protected View emptyView,actionbarView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//不自动弹出软键盘
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		fragmentManager = getSupportFragmentManager();
	}
	
	//初始化页面view
	protected void initViews(){
		backBtn = findViewById(R.id.backButton);
		if(null!=backBtn){
			backBtn.setOnClickListener(this);
		}
		
		actionBtn = findViewById(R.id.actionBtn);
		if(null!=actionBtn){
			actionBtn.setOnClickListener(this);
		}
		titleTv = (TextView) findViewById(R.id.header_title);
		actionTv = (TextView) findViewById(R.id.actionTv);
	}
	
	protected void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		actionbarView = inflator.inflate(R.layout.page_header, null);
		ActionBar.LayoutParams layout = new ActionBar.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		actionBar.setCustomView(actionbarView, layout);

		titleTv = (TextView) actionbarView.findViewById(R.id.header_title);

		backBtn = actionbarView.findViewById(R.id.backButton);
		backBtn.setOnClickListener(this);
		
		actionBtn = actionbarView.findViewById(R.id.actionBtn);
		if(null!=actionBtn){
			actionBtn.setOnClickListener(this);
		}
		
		actionTv = (TextView) actionbarView.findViewById(R.id.actionTv);
	}
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.backButton:
			onBackPressed();
			break;
		default:
			break;
		}
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//再切换页面时   影藏软键盘
//		initFloat(false);
		View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log.d("onPause", "onPause");
	}
	
	
	/**
	 * 替换Fragment
	 * 
	 * @param fragClass
	 */
	public void replaceFragment(Fragment fragClass,int fragmentContainer,String tag)
	{
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(fragmentContainer, fragClass, tag);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	/**
	 * 替换Fragment
	 * 
	 * @param fragClass
	 */
	public void replaceFragmentNoBack(Fragment fragClass,int fragmentContainer,String tag)
	{
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(fragmentContainer, fragClass, tag);
		fragmentTransaction.commitAllowingStateLoss();
	}

	/**
	 * 添加Fragment
	 * 
	 * @param fragClass
	 */
	public void addFragment(Fragment fragClass,int fragmentContainer,String tag)
	{
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(fragmentContainer, fragClass, tag);
		fragmentTransaction.commit();
	}
	/***
	 * 
	 * @param form 当前fragment
	 * @param to 添加fragment
	 * @param tag 添加的标志tag
	 */
	public void hideFragment(Fragment form,Fragment to, String tag)
	{
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.hide(form).add(to, tag);
		fragmentTransaction.commit();
	}

	/**
	 * 移除Fragment
	 * 
	 * @param fragClass
	 */
	public void removeFragment(Fragment fragClass)
	{
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.remove(fragClass);
		fragmentTransaction.commit();
	}	
}
