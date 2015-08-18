package com.galaxywind.ycftp;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.galaxywind.ycftp.client.LocalFileListFragment;
import com.galaxywind.ycftp.client.RemoteFileListFragment;
import com.galaxywind.ycftp.client.SiteFragment;
import com.galaxywind.ycftp.filemanager.FileListActivity;
import com.galaxywind.ycftp.server.LoginSettingActivity;
import com.galaxywind.ycftp.server.PortSettingActivity;
import com.galaxywind.ycftp.server.ServerFragment;
import com.galaxywind.ycftp.ui.navigation.PagerSlidingTabStrip;
import com.galaxywind.ycftp.ui.navigation.SlidingMenu;
import com.galaxywind.ycftp.ui.navigation.PagerSlidingTabStrip.OnPagerSlidingTabStripChanged;
import com.galaxywind.ycftp.ui.navigation.SlidingMenu.MenuStateListner;
import com.galaxywind.ycftp.ui.navigation.YCViewPager;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.lidroid.xutils.util.LogUtils;

public class MainActivity extends BaseActivity implements MenuStateListner,
		OnNavigationListener {
	// 选项卡
	private PagerSlidingTabStrip tabs;
	// 滑动页
	private YCViewPager pager;
	// 滑动的菜单
	private SlidingMenu menu;
	// 每天个界面
	private ServerFragment serverFragment;
	private SiteFragment siteFragment;
	private LocalFileListFragment fileListFragment;
	private RemoteFileListFragment remoteFileListFragment;
	private List<MainFragment> fragments = new ArrayList<MainFragment>();
	private ListView settingsLv;
	private String[] titles, settings, roles;
	private Intent settingIntent = new Intent();
	private SettingsListAdapter settinsAdapter;
	private Resources res;
	private static final int SERVER = 0;
	private static final int CLIENT = 1;
	private int roleTag = SERVER;
	private MyAdapter myAdapter;
	private ImageButton drawerIB;
	private Spinner roleSpinner;
	private MySpinnerAdapter mySpinnerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initConfig();
		initData();
		initView();
	}

	@Override
	protected void initActionBar() {
		// TODO Auto-generated method stub
		/************ 自定义ActionBar ***********/
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		actionbarView = inflator.inflate(R.layout.home_actionbar_view, null);
		ActionBar.LayoutParams layout = new ActionBar.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		actionBar.setCustomView(actionbarView, layout);
		drawerIB = (ImageButton) actionbarView.findViewById(R.id.drawerIB);
		drawerIB.setOnClickListener(this);
		roleSpinner = (Spinner) actionbarView.findViewById(R.id.roleSpinner);
		mySpinnerAdapter = new MySpinnerAdapter(this,
				R.layout.spinner_dropdown_item, R.id.itemTv, roles);
		roleSpinner.setAdapter(mySpinnerAdapter);
		roleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case SERVER:
					roleTag = SERVER;
					titles = res.getStringArray(R.array.server_tabs);
					initViewPager(SERVER);
					break;
				case CLIENT:
					roleTag = CLIENT;
					titles = res.getStringArray(R.array.client_tabs);
					initViewPager(CLIENT);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		/*********** ActionBar自身实现home icon和Spinner *************/
		// ActionBar bar = getActionBar();
		// bar.setIcon(R.drawable.ic_drawer_dark);
		// bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
		// ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		//
		// // 设置 List Navigation Mode
		// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// MySpinnerAdapter adapter = new MySpinnerAdapter(this,
		// R.layout.spinner_dropdown_item,
		// R.id.itemTv,
		// new String[] {"服务端", "客户端"});
		// // 设置下拉列表项的布局
		// adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// // 设置list adapter以及navigation callback
		// bar.setListNavigationCallbacks(adapter, this);
	}

	private void initData() {
		res = getResources();
		titles = res.getStringArray(R.array.server_tabs);
		settings = res.getStringArray(R.array.settings);
		settings[1] = getString(R.string.port) + ":" + getPort();
		settings[2] = getString(R.string.root_dir) + getRootDir();
		roles = res.getStringArray(R.array.roles);
	}

	private void initConfig() {
		LogUtils.customTagPrefix = "YCFTP";
		LogUtils.allowI = true;
	}

	/**
	 * 获取根吗目录
	 * 
	 * @return
	 */
	private String getRootDir() {
		String rootdir = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.ROOT_DIR_SP,
				YCFTPApplication.ROOT_DIR);
		return StringUtil.isNotEmpty(rootdir) ? rootdir : "/";
	}

	private void initView() {
		initActionBar();
		pager = (YCViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		menu = (SlidingMenu) findViewById(R.id.id_menu);
		menu.setOnMenuStateChangeListner(this);
		menu.setChildView(pager);
		initViewPager(SERVER);
		tabs.setOnPagerSlidingTabStripChanged(new OnPagerSlidingTabStripChanged() {
			@Override
			public void onPageChanged(int position) {
				// 将当前的界面的下标设置到silmenu中
				menu.setIndexpos(position);
			}
		});
		tabs.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
//				LogUtils.i(arg0+" onPageSelected");
//				pager.setChildView(fragments.get(arg0).getListView());
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
//				LogUtils.i(arg0+" onPageScrolled");
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
//				LogUtils.i(arg0+" onPageScrollStateChanged");
			}
		});

		settingsLv = (ListView) findViewById(R.id.settingsLv);
		settinsAdapter = new SettingsListAdapter();
		settingsLv.setAdapter(settinsAdapter);

		settingsLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					settingIntent.setClass(MainActivity.this,
							LoginSettingActivity.class);
					startActivity(settingIntent);
					break;
				case 1:
					settingIntent.setClass(MainActivity.this,
							PortSettingActivity.class);
					startActivityForResult(settingIntent,
							YCFTPApplication.PORT_REQUEST_CODE);
					break;
				case 2:
					settingIntent.setClass(MainActivity.this,
							FileListActivity.class);
					settingIntent
							.setAction("android.intent.action.GET_CONTENT");
					// settingIntent.setAction("test");
					startActivityForResult(settingIntent,
							YCFTPApplication.ROOT_REQUEST_CODE);
					break;
				case 3:
					break;
				}
			}
		});
	}

	/**
	 * 初始化ViewPager
	 * @param type SERVER|CLIENT
	 */
	private void initViewPager(int type) {
		switch(type){
		case SERVER:
			serverFragment = new ServerFragment();
			fragments.clear();
			fragments.add(serverFragment);
			break;
		case CLIENT:
			siteFragment = new SiteFragment();
			
			fileListFragment = new LocalFileListFragment();
			Bundle bundle = new Bundle();
			bundle.putString("action", "");
			fileListFragment.setArguments(bundle);
			
			remoteFileListFragment = new RemoteFileListFragment();
			
			fragments.clear();
			fragments.add(siteFragment);
			fragments.add(fileListFragment);
			fragments.add(remoteFileListFragment);
			break;
		}
		
//		pager.removeAllViewsInLayout();
		myAdapter = new MyAdapter(getSupportFragmentManager());
		pager.setAdapter(myAdapter);
		tabs.setViewPager(pager);
	}
	
	public void setPage(int position){
		pager.setCurrentItem(position);
		menu.setIndexpos(position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			toggleMenu(menu);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case YCFTPApplication.ROOT_RESULT_CODE:
			String rootDir = data.getStringExtra(YCFTPApplication.ROOT_DIR);
			settings[2] = getString(R.string.root_dir) + rootDir;
			settinsAdapter.notifyDataSetChanged();
			SharePreferenceUtil.savePreference(this,
					YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.ROOT_DIR,
					rootDir);
			break;
		case YCFTPApplication.PORT_RESULT_CODE:
			String port = data.getStringExtra(YCFTPApplication.PORT);
			port = StringUtil.isEmpty(port) ? "2121(Default)" : port;
			settings[1] = getString(R.string.port) + ":" + port;
			settinsAdapter.notifyDataSetChanged();
			SharePreferenceUtil.savePreference(this,
					YCFTPApplication.SERVER_PORT_SP, YCFTPApplication.PORT,
					port);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.drawerIB:
			toggleMenu(menu);
			break;
		}
	}


	public class MyAdapter extends FragmentStatePagerAdapter {

		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			switch (roleTag) {
			case SERVER:
				switch (position) {
				case 0:
					return fragments.get(0);
				}
			case CLIENT:
				switch (position) {
				case 0:
					return fragments.get(0);
				case 1:
					return fragments.get(1);
				case 2:
					return fragments.get(2);
				}
			}

			return null;
		}
	}

	public void toggleMenu(View view) {
		menu.toggle();
	}

	/*
	 * 设置项列表
	 */
	class SettingsListAdapter extends BaseAdapter {
		private ViewHolder vh;

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return settings.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return settings[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.settings_item, null);
				vh = new ViewHolder();
				vh.settingsItemTv = (TextView) convertView
						.findViewById(R.id.settings_item_Tv);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			vh.settingsItemTv.setText(settings[position]);
			return convertView;
		}
	}

	static class ViewHolder {
		public TextView settingsItemTv;
	}

	/**
	 * 侧滑菜单打开时，tabs和pagers不可滑动 侧滑菜单关闭时，tabs和pagers可滑动
	 */
	@Override
	public void onMenuStateChange(boolean isOpen) {
		// TODO Auto-generated method stub
		if (isOpen) {
			tabs.setScrollable(false);
			pager.setScrollable(false);
		} else {
			tabs.setScrollable(true);
			pager.setScrollable(true);
		}
	}

	/**
	 * 角色选择Spinner Adapter
	 * 
	 * @author zyc
	 * 
	 */
	class MySpinnerAdapter extends ArrayAdapter<String> {
		private SpinnerViewHolder vh;
		private Context mContext;
		private String[] items;

		public MySpinnerAdapter(Context context, int resource,
				int textViewResourceId, String[] objects) {
			super(context, resource, textViewResourceId, objects);
			this.mContext = context;
			this.items = objects;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				vh = new SpinnerViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.spinner_dropdown_item, null);
				vh.itemTv = (TextView) convertView.findViewById(R.id.itemTv);
				convertView.setTag(vh);
			} else {
				vh = (SpinnerViewHolder) convertView.getTag();
			}
			vh.itemTv.setText(items[position]);
			return convertView;
		}

	}

	class SpinnerViewHolder {
		public TextView itemTv;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		if (res == null) {
			res = getResources();
		}
		switch (itemPosition) {
		case SERVER:
			roleTag = SERVER;
			titles = res.getStringArray(R.array.server_tabs);
			initViewPager(SERVER);
			break;
		case CLIENT:
			roleTag = CLIENT;
			titles = res.getStringArray(R.array.client_tabs);
			initViewPager(CLIENT);
			break;
		}
		return true;
	}

	/**
	 * 获取端口
	 */
	private String getPort() {
		String portStr = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.SERVER_PORT_SP,
				YCFTPApplication.PORT);
		if (StringUtil.isNotEmpty(portStr)) {
			return portStr;
		} else {
			return 2121 + "(Default)";
		}
	}

	public YCViewPager getPager() {
		return pager;
	}

	public PagerSlidingTabStrip getTabs() {
		return tabs;
	}

	public SlidingMenu getMenu() {
		return menu;
	}
}
