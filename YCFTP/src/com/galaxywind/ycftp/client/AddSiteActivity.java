package com.galaxywind.ycftp.client;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.galaxywind.ycftp.BaseActivity;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.entities.Site;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class AddSiteActivity extends BaseActivity {
	private List<Site> siteList;
	private static int ADD = 0;
	private static int EDIT = 1;
	//操作类型 ADD新加;EDIT 编辑
	private int oprTag = ADD;
	
	@ViewInject(R.id.nameEt)
	private EditText nameEt;
	
	@ViewInject(R.id.hostEt)
	private EditText hostEt;
	
	@ViewInject(R.id.portEt)
	private EditText portEt;
	
	@ViewInject(R.id.userEt)
	private EditText userEt;
	
	@ViewInject(R.id.passWordEt)
	private EditText passWordEt;
	
	DbUtils dbmDbUtils;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dbmDbUtils = DbUtils.create(this);
		setContentView(R.layout.activity_addsite);
		ViewUtils.inject(this);
		initActionBar();
//		initViews();
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
	}
	

	@Override
	protected void initActionBar() {
		// TODO Auto-generated method stub
		super.initActionBar();
		titleTv.setText("New Site");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_site_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.save:
			String host = hostEt.getText().toString();
			String port = portEt.getText().toString();
			if(StringUtil.isEmpty(host)){
				ToastUtil.showToast(this, "Host must be entered!");
			}
			if(StringUtil.isEmpty(port)){
				ToastUtil.showToast(this, "Port must be entered!");
			}
			
			Site site = new Site();
			site.setName(nameEt.getText().toString());
			site.setHost(host);
			site.setPort(port);
			site.setUser(userEt.getText().toString());
			site.setPassWord(passWordEt.getText().toString());
			
//			DbUtils db = DbUtils.create(this, "ycftp.db");
			
			try {
				if(oprTag==ADD){
					dbmDbUtils.save(site);
					siteList = dbmDbUtils.findAll(Site.class);
					LogUtils.i(siteList.toString());
				}else{
					dbmDbUtils.update(site, "name","host","port","user","passWord");
					siteList = dbmDbUtils.findAll(Site.class);
					LogUtils.i(siteList.toString());
				}
				Intent resultIntent = new Intent();
				resultIntent.putExtra("sites", (Serializable)siteList);
				setResult(110, resultIntent);
				finish();
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}
}
