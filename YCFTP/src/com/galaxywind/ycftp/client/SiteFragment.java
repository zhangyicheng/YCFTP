package com.galaxywind.ycftp.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.galaxywind.ycftp.MainFragment;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.client.handler.JsonResponsehandler;
import com.galaxywind.ycftp.entities.Site;
import com.galaxywind.ycftp.filemanager.YCFile;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;

public class SiteFragment extends MainFragment{
	private SitesAdapter sitesAdapter;
	private List<Site> sitesList = new ArrayList<Site>();
	private Site selectedSite;
	private DbUtils mDbUtils;
	@ViewInject(R.id.sitesLv)
	private ListView sitesLv;
	
	@Override
	public void onCreate(Bundle saveInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(saveInstanceState);
		setHasOptionsMenu(true);
		mDbUtils = DbUtils.create(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		fragmentView = inflater.inflate(R.layout.fragment_sites, null);
		ViewUtils.inject(this, fragmentView);
		initViews();
		return fragmentView;
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		sitesLv = (ListView) fragmentView.findViewById(R.id.sitesLv);
		setEmptyView(sitesLv, "No sites!");
		registerForContextMenu(sitesLv);

		try {
			if (mDbUtils.findAll(Site.class) != null) {
				sitesList.clear();
				sitesList.addAll(mDbUtils.findAll(Site.class));
			}
			sitesAdapter = new SitesAdapter(sitesList);
			sitesLv.setAdapter(sitesAdapter);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void initData() {
		// TODO Auto-generated method stub
		super.initData();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.client_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.addSite:
			Intent intent = new Intent(getActivity(), AddSiteActivity.class);
			startActivityForResult(intent, 110);
			return true;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		selectedSite = sitesList.get(info.position);
		MenuInflater inflator = new MenuInflater(getActivity());
		// 装填R.menu.context对应的菜单,并添加到menu中
		inflator.inflate(R.menu.sites_context_menu, menu);
		menu.setHeaderIcon(R.drawable.ic_site);
		menu.setHeaderTitle(R.string.site);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.delete:
			try {
				mDbUtils.delete(selectedSite);
				sitesList.clear();
				sitesList.addAll(mDbUtils.findAll(Site.class));
				sitesAdapter.notifyDataSetChanged();
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case R.id.edit:
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 110:
			@SuppressWarnings("unchecked")
			List<Site> sites = (List<Site>) data.getSerializableExtra("sites");
			sitesList.clear();
			sitesList.addAll(sites);
			sitesAdapter.notifyDataSetChanged();
			LogUtils.i("result:" + sites.toString());
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	class SitesAdapter extends BaseAdapter {
		List<Site> sites;
		ViewHolder vh;

		public SitesAdapter(List<Site> sites) {
			super();
			this.sites = sites;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return sites.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return sites.get(position);
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
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.site_item, null);
				// ViewUtils.inject(this,convertView);
				vh = new ViewHolder();
				vh.siteNameTv = (TextView) convertView
						.findViewById(R.id.siteNameTv);
				vh.siteAddressTv = (TextView) convertView
						.findViewById(R.id.siteAddressTv);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			Site site = sites.get(position);
			vh.siteNameTv.setText(site.name);
			vh.siteAddressTv.setText(site.user + "@" + site.host + ":"
					+ site.port);
			return convertView;
		}
	}

	class ViewHolder {
		// @ViewInject(R.id.siteNameTv)
		public TextView siteNameTv;

		// @ViewInject(R.id.siteAddressTv)
		public TextView siteAddressTv;
	}

	@OnItemClick(R.id.sitesLv)
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		/**
		 * ***********发登录请求，成功返回目录*************
		 */
		Site site = sitesList.get(position);
		YCFTPApplication.site = site;
		JSONObject requestJo = new JSONObject();
		try {
			requestJo.put(FTPConstant.OPERATION, FTPConstant.LOGON_OPR);
			requestJo.put("username", site.user);
			requestJo.put("password", site.passWord);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FTPClientHelper.request(getActivity(), site, requestJo,
				new JsonResponsehandler() {

					@Override
					public void onSuccess(int statusCode, JSONObject responseJo) {
						// TODO Auto-generated method stub
						try {
							String jsonStr = responseJo.getJSONObject("Response").getJSONArray("Files").toString();
							Gson gson = new Gson();
							List<YCFile> fileList = gson.fromJson(jsonStr,  
					                new TypeToken<List<YCFile>>() {  
			                }.getType()); 
							
							//重置全局远程文件集合
							YCFTPApplication.remoteFiles.clear();
							YCFTPApplication.remoteFiles.addAll(fileList);
							//切换Tab
							mActivity.getPager().setCurrentItem(2);
							mActivity.getMenu().setIndexpos(2);
							LogUtils.i(fileList.toString());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ToastUtil.showToast(getActivity(), responseJo.toString());
					}

					@Override
					public void onFailure(int statusCode, Throwable throwable) {
						// TODO Auto-generated method stub
						if(throwable!=null){
							ToastUtil.showToast(getActivity(), throwable.toString());
						}
					}});
	}
}
