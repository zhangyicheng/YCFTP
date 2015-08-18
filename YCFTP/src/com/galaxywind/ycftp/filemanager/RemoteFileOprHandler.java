package com.galaxywind.ycftp.filemanager;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.client.FTPClientHelper;
import com.galaxywind.ycftp.client.handler.JsonResponsehandler;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.lidroid.xutils.util.LogUtils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * UI点击事件处理器 FileListActivity和FileManager之间的纽带 使FileManager模块化（解耦）
 * 
 * @author Joe Berria
 */
public class RemoteFileOprHandler implements OnClickListener {
	/* w文件操作类型 */
	public static final int DOWNLOAD_TYPE = 0x00;
	public static final int DELETE_TYPE = 0x01;
	private final Context mContext;
	private final RemoteFileManager mFileMang;
	private ThumbnailCreator mThumbnail;
	private FileListAdapter mDelegate;

	private boolean multi_select_flag = false;
	private boolean delete_after_copy = false;
	private boolean thumbnail_flag = true;
	private int mColor = Color.WHITE;

	/*
	 * mDataSource:当前目录下文件的路径List; mMultiSelectData:多选选中的路径List
	 */
	public List<YCFile> mDataSource = new ArrayList<YCFile>(), mMultiSelectData = new ArrayList<YCFile>();
	private TextView mPathLabel;
	private TextView mInfoLabel;
	
//	private static final int FILE_NO_EXSIST = 0;
//	private static final int FILE_AVAILABLE = 1;
//	private static final int FILE_DELETED = 2;
//	private static final int NO_PERMITION = 3;
	private static final int DEFAULT = -1;
	
	private Message msg;
	// UI刷新Handler
		Handler uiHanlder = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case DEFAULT:
					ToastUtil.showToast(mContext, (String)msg.obj);
					break;
				}
			}
		};


	/**
	 * @param context
	 *            上下文
	 * @param manager
	 *            从context里实例化的FileManager
	 */
	public RemoteFileOprHandler(Context context, final RemoteFileManager manager) {
		mContext = context;
		mFileMang = manager;
		mDataSource = YCFTPApplication.remoteFiles;
	}

	/**
	 * @param context
	 *            上下文
	 * @param manager
	 *            从context里实例化的FileManager
	 * @param location
	 *            路径
	 */
	public RemoteFileOprHandler(Context context, final RemoteFileManager manager,
			String location) {
		mContext = context;
		mFileMang = manager;
		mDataSource = YCFTPApplication.remoteFiles;
	}

	/**
	 * 设置ListView适配器 在UI类里调用
	 * 
	 * @param adapter
	 *            The FileListAdapter object
	 */
	public void setListAdapter(FileListAdapter adapter) {
		mDelegate = adapter;
	}

	/**
	 * 设置路径信息标签
	 * 
	 * @param path
	 *            路径标签
	 * @param label
	 *            详情标签
	 */
	public void setUpdateLabels(TextView path, TextView label) {
		mPathLabel = path;
		mInfoLabel = label;
	}

	/**
	 * 设置字体颜色
	 * 
	 * @param color
	 */
	public void setTextColor(int color) {
		mColor = color;
	}

	/**
	 * 设置是否显示图片缩略图 true 显示缩略图 false 显示默认图片
	 * 
	 * @param show
	 */
	public void setShowThumbnails(boolean show) {
		thumbnail_flag = show;
	}

	/**
	 * 设置复制后删除原文件 用于剪切文件
	 * 
	 * @param delete
	 *            是否删除
	 */
	public void setDeleteAfterCopy(boolean delete) {
		delete_after_copy = delete;
	}

	/**
	 * 判断是否多选
	 * 
	 * @return true 多选
	 */
	public boolean isMultiSelected() {
		return multi_select_flag;
	}
	
	public void setMultiSelectFlag(boolean multi_select_flag) {
		this.multi_select_flag = multi_select_flag;
	}

	public List<YCFile> getmDataSource() {
		return mDataSource;
	}

	public void setmDataSource(ArrayList<YCFile> mDataSource) {
		this.mDataSource = mDataSource;
	}
	

	/**
	 * 判断是否有多选选中的文件、文件夹
	 * 
	 * @return returns true 有
	 */
	public boolean hasMultiSelectData() {
		return (mMultiSelectData != null && mMultiSelectData.size() > 0);
	}

	/**
	 * 停止缩略图线程 离开图片目录时调用
	 */
	public void stopThumbnailThread() {
		if (mThumbnail != null) {
			mThumbnail.setCancelThumbnails(true);
			mThumbnail = null;
		}
	}

	/**
	 * 接收UI类实例的点击事件
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		// 文件导航返回键
		case R.id.back_button:
			break;
		case R.id.home_button:
			break;

		case R.id.info_button:
			break;
		case R.id.multiselect_button:
			if (multi_select_flag) {
				// 取消全选
				mDelegate.killMultiSelect(true);

			} else {
				// 全选
				int listSize = mDataSource.size();
				for (int i = 0; i < listSize; i++) {
					mDelegate.addMultiPosition(i, mDataSource.get(i));
					mDelegate.getItemSelected().put(i, true);
				}
				multi_select_flag = true;

				LinearLayout hidden_lay = (LinearLayout) ((Activity) mContext)
						.findViewById(R.id.hidden_buttons);
				hidden_lay.setVisibility(LinearLayout.VISIBLE);
			}
			break;

		// 分享
		case R.id.hidden_attach:
		case R.id.hidden_move:
		case R.id.hidden_copy:
		case R.id.hidden_delete:
			break;
		}
	}


	

	/**
	 * 返回ArrayList某个位置对应的路径信息
	 * 
	 * @param position
	 *            下标
	 * @return 路径
	 */
	public YCFile getData(int position) {

		if (position > mDataSource.size() - 1 || position < 0)
			return null;

		return mDataSource.get(position);
	}

	/**
	 * 刷新目录列表信息
	 * 
	 * @param content
	 *            存有当前目录下的文件、文件夹信息的ArrayList +
	 */
	public void updateDirectory(List<YCFile> content) {
		// 取消全选
		mDelegate.killMultiSelect(true);

		if (!mDataSource.isEmpty()){
			mDataSource.clear();
		}
		mDataSource.addAll(content);
		mDelegate.refreshData();
	}
	
	
	

	private static class ViewHolder {
		TextView topView;
		TextView bottomView;
		ImageView icon;
		CheckBox mSelect; // multi-select check mark icon
	}

	/**
	 * 文件列表Adapter
	 * 
	 * @author Joe Berria
	 */
	public class FileListAdapter extends ArrayAdapter<YCFile> {
		private final int KB = 1024;
		private final int MG = KB * KB;
		private final int GB = MG * KB;
		private String display_size;
		private ArrayList<Integer> positions;
		// 用来控制CheckBox的选中状况
		private HashMap<Integer, Boolean> itemSelected;
		LayoutInflater inflater;
		private LinearLayout hidden_layout;
		private boolean showCheckBox;

		public FileListAdapter(boolean showCheckBox) {
			super(mContext, R.layout.file_list_item, mDataSource);
			this.showCheckBox = showCheckBox;
			itemSelected = new HashMap<Integer, Boolean>();
			inflater = LayoutInflater.from(mContext);
			initCheck();
		}

		private void initCheck() {
			int listSize = mDataSource.size();
			for (int i = 0; i < listSize; i++) {
				getItemSelected().put(i, false);
			}
		}
		
		private void refreshData(){
			initCheck();
			notifyDataSetChanged();
		}
		
		/**
		 * 向多选路径List添加数据 mMultiSelectData里没有该路径记录就加入，有就删除
		 * 
		 * @param index
		 * @param path
		 */
		public void addMultiPosition(int index, YCFile file) {
			if (positions == null)
				positions = new ArrayList<Integer>();

			if (mMultiSelectData == null) {
				positions.add(index);
				add_multiSelect_file(file);

			} else if (mMultiSelectData.contains(file)) {
				if (positions.contains(index))
					positions.remove(new Integer(index));

				mMultiSelectData.remove(file);

			} else {
				positions.add(index);
				add_multiSelect_file(file);
			}

			notifyDataSetChanged();
		}

		/**
		 * 关闭多选 隐藏多选操作菜单 clearData为true清除多选数据，为false保留多选数据
		 */
		public void killMultiSelect(boolean clearData) {
			hidden_layout = (LinearLayout) ((Activity) mContext)
					.findViewById(R.id.hidden_buttons);
			hidden_layout.setVisibility(LinearLayout.GONE);
			multi_select_flag = false;

			if (positions != null && !positions.isEmpty()) {
				positions.clear();
			}

			if (clearData) {
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					mMultiSelectData.clear();
				}
			}

			int listSize = mDataSource.size();
			for (int i = 0; i < listSize; i++) {
				mDelegate.getItemSelected().put(i, false);
			}
			notifyDataSetChanged();
		}

		/**
		 * 获取文件权限
		 * 
		 * @param file
		 * @return
		 */
		public String getFilePermissions(File file) {
			String per = "-";

			if (file.isDirectory())
				per += "d";
			if (file.canRead())
				per += "r";
			if (file.canWrite())
				per += "w";
			return per;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder mViewHolder;
			YCFile file = mDataSource.get(position);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.file_list_item, parent,
						false);

				mViewHolder = new ViewHolder();
				mViewHolder.topView = (TextView) convertView
						.findViewById(R.id.top_view);
				mViewHolder.bottomView = (TextView) convertView
						.findViewById(R.id.bottom_view);
				mViewHolder.icon = (ImageView) convertView
						.findViewById(R.id.row_image);
				mViewHolder.mSelect = (CheckBox) convertView
						.findViewById(R.id.fileListCB);
				if(showCheckBox){
					mViewHolder.mSelect.setVisibility(View.VISIBLE);
				}else{
					mViewHolder.mSelect.setVisibility(View.GONE);
				}
				convertView.setTag(mViewHolder);

			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			
			mViewHolder.mSelect.setOnClickListener(new CheckBoxClickLister(position, file));
			mViewHolder.mSelect.setChecked(getItemSelected().get(position));

			mViewHolder.topView.setTextColor(mColor);
			mViewHolder.bottomView.setTextColor(mColor);

			if (mThumbnail == null)
				mThumbnail = new ThumbnailCreator(100, 100);

			if (file != null && file.isFile()) {
				String ext = file.toString();
				String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

				/*
				 * This series of else if statements will determine which icon
				 * is displayed
				 */
				if (sub_ext.equalsIgnoreCase("pdf")) {
					mViewHolder.icon.setImageResource(R.drawable.pdf);

				} else if (sub_ext.equalsIgnoreCase("mp3")
						|| sub_ext.equalsIgnoreCase("wma")
						|| sub_ext.equalsIgnoreCase("m4a")
						|| sub_ext.equalsIgnoreCase("m4p")) {

					mViewHolder.icon.setImageResource(R.drawable.music);

				} else if (sub_ext.equalsIgnoreCase("png")
						|| sub_ext.equalsIgnoreCase("jpg")
						|| sub_ext.equalsIgnoreCase("jpeg")
						|| sub_ext.equalsIgnoreCase("gif")
						|| sub_ext.equalsIgnoreCase("tiff")) {

					mViewHolder.icon.setImageResource(R.drawable.image);
				} else if (sub_ext.equalsIgnoreCase("zip")
						|| sub_ext.equalsIgnoreCase("gzip")
						|| sub_ext.equalsIgnoreCase("gz")) {

					mViewHolder.icon.setImageResource(R.drawable.zip);

				} else if (sub_ext.equalsIgnoreCase("m4v")
						|| sub_ext.equalsIgnoreCase("wmv")
						|| sub_ext.equalsIgnoreCase("3gp")
						|| sub_ext.equalsIgnoreCase("mp4")) {

					mViewHolder.icon.setImageResource(R.drawable.movies);

				} else if (sub_ext.equalsIgnoreCase("doc")
						|| sub_ext.equalsIgnoreCase("docx")) {

					mViewHolder.icon.setImageResource(R.drawable.word);

				} else if (sub_ext.equalsIgnoreCase("xls")
						|| sub_ext.equalsIgnoreCase("xlsx")) {

					mViewHolder.icon.setImageResource(R.drawable.excel);

				} else if (sub_ext.equalsIgnoreCase("ppt")
						|| sub_ext.equalsIgnoreCase("pptx")) {

					mViewHolder.icon.setImageResource(R.drawable.ppt);

				} else if (sub_ext.equalsIgnoreCase("html")) {
					mViewHolder.icon.setImageResource(R.drawable.html32);

				} else if (sub_ext.equalsIgnoreCase("xml")) {
					mViewHolder.icon.setImageResource(R.drawable.xml32);

				} else if (sub_ext.equalsIgnoreCase("conf")) {
					mViewHolder.icon.setImageResource(R.drawable.config32);

				} else if (sub_ext.equalsIgnoreCase("apk")) {
					mViewHolder.icon.setImageResource(R.drawable.appicon);

				} else if (sub_ext.equalsIgnoreCase("jar")) {
					mViewHolder.icon.setImageResource(R.drawable.jar32);

				} else {
					mViewHolder.icon.setImageResource(R.drawable.text);
				}

			} else if (file != null && !file.isFile()) {
					mViewHolder.icon.setImageResource(R.drawable.folder);
			}

//			String permission = getFilePermissions(file);

			if (file.isFile()) {
				double size = file.getSize();
				if (size > GB)
					display_size = String
							.format("%.2f Gb ", (double) size / GB);
				else if (size < GB && size > MG)
					display_size = String
							.format("%.2f Mb ", (double) size / MG);
				else if (size < MG && size > KB)
					display_size = String
							.format("%.2f Kb ", (double) size / KB);
				else
					display_size = String.format("%.2f bytes ", (double) size);
			}

			mViewHolder.topView.setText(file.getName());

			return convertView;
		}

		private void add_multiSelect_file(YCFile file) {
			if (mMultiSelectData == null)
				mMultiSelectData = new ArrayList<YCFile>();

			mMultiSelectData.add(file);
		}

		public HashMap<Integer, Boolean> getItemSelected() {
			return itemSelected;
		}

		public void setItemSelected(HashMap<Integer, Boolean> itemSelected) {
			this.itemSelected = itemSelected;
		}

	}

	/**
	 * CheckBox选择变化监听器
	 * 
	 * @author zyc
	 * 
	 */
	class ItemCheckListner implements OnCheckedChangeListener {

		int position;// 列表下标
		String path;// 路径

		public ItemCheckListner(int position, String path) {
			super();
			this.position = position;
			this.path = path;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
//			mDelegate.addMultiPosition(position, path);
//
//			if (mDelegate.getItemSelected().get(position)) {
//				mDelegate.getItemSelected().put(position, false);
//			} else {
//				mDelegate.getItemSelected().put(position, true);
//			}
		}
	}

	/*
	 *复选框监听
	 */
	class CheckBoxClickLister implements OnClickListener {

		int position;// 列表下标
		YCFile file;// 文件模型

		public CheckBoxClickLister(int position, YCFile file) {
			super();
			this.position = position;
			this.file = file;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mDelegate.addMultiPosition(position, file);

			if (mDelegate.getItemSelected().get(position)) {
				mDelegate.getItemSelected().put(position, false);
			} else {
				mDelegate.getItemSelected().put(position, true);
			}
		}

	}
	
	/**
	 * 文件操作异步任务
	 */
	public class RemoteFileOperTask extends
			AsyncTask<YCFile, Void, ArrayList<String>> {
		private String file_name;
		private ProgressDialog pr_dialog;
		private int type;
		private int copy_rtn;
		int paramsNum;

		public RemoteFileOperTask(int type) {
			this.type = type;
		}

		@Override
		protected void onPreExecute() {

			switch (type) {
			case DOWNLOAD_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Downloading",
						"Downloading files...", true, false);
				break;
			case DELETE_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Deleting",
						"Deleting files...", true, false);
				break;
			}
		}

		@Override
		protected ArrayList<String> doInBackground(final YCFile... params) {
			paramsNum = params.length;
			switch (type) {
			case DOWNLOAD_TYPE:
				for(int i = 0; i < paramsNum; i++){
					final JSONObject requestJo = new JSONObject();
					try {
						requestJo.put(FTPConstant.OPERATION, FTPConstant.DOWNLOAD_OPR);
						requestJo.put("FilePath", params[i].getPath());
						requestJo.put("FileName", params[i].getName());
						requestJo.put("IsFile",params[i].isFile());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FTPClientHelper.request(mContext, YCFTPApplication.site, requestJo, new JsonResponsehandler() {
						
						@Override
						public void onSuccess(int statusCode, JSONObject responseJo) {
							// TODO Auto-generated method stub
							try {
								switch(responseJo.getInt("ResponseCode")){
								case FTPConstant.STATUS_CODE_SUCCESS:
//									sendMsg(FILE_AVAILABLE, responseJo.get("Response"));
									JSONObject reqJo = new JSONObject();
									try {
										reqJo.put(FTPConstant.OPERATION, FTPConstant.DOWNLOAD_FILE);
										reqJo.put("FilePath", requestJo.get("FilePath"));
										reqJo.put("FileName", requestJo.get("FileName"));
										reqJo.put("IsFile", requestJo.get("IsFile"));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									FTPClientHelper.request(mContext, YCFTPApplication.site, reqJo, new JsonResponsehandler() {
										
										@Override
										public void onSuccess(int statusCode, JSONObject responseJo) {
											// TODO Auto-generated method stub
											
										}
										
										@Override
										public void onFailure(int statusCode, Throwable throwable) {
											// TODO Auto-generated method stub
											LogUtils.e("download fail "+throwable.toString());
										}
									});
									break;
								case FTPConstant.STATUS_CODE_ERROR:
									sendMsg(DEFAULT, responseJo.get("Response"));
									break;
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							LogUtils.e("download request seccess"+responseJo.toString());
							sendMsg(DEFAULT, "Downloading");
						}
						
						@Override
						public void onFailure(int statusCode, Throwable throwable) {
							// TODO Auto-generated method stub
							LogUtils.e("download request fail "+throwable.toString());
							sendMsg(DEFAULT, "Download request failed");
						}
					});
				}
				return null;
			case DELETE_TYPE:
				for(int i = 0; i < paramsNum; i++){
					final JSONObject requestJo = new JSONObject();
					try {
						requestJo.put(FTPConstant.OPERATION, FTPConstant.DELETE_OPR);
						requestJo.put("FilePath", params[i].getPath());
						requestJo.put("FileName", params[i].getName());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FTPClientHelper.request(mContext, YCFTPApplication.site, requestJo, new JsonResponsehandler() {
						
						@Override
						public void onSuccess(int statusCode, JSONObject responseJo) {
							// TODO Auto-generated method stub
							try {
								sendMsg(DEFAULT, responseJo.get("Response"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure(int statusCode, Throwable throwable) {
							// TODO Auto-generated method stub
							
						}
					});
				}
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<String> file) {
			final CharSequence[] names;
			int len = file != null ? file.size() : 0;

			switch (type) {
			case DOWNLOAD_TYPE:
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					mMultiSelectData.clear();
					multi_select_flag = false;
				}

				pr_dialog.dismiss();
				mInfoLabel.setText("download ok");
				break;
			case DELETE_TYPE:
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					mMultiSelectData.clear();
					multi_select_flag = false;
				}

				updateDirectory(mDataSource);
				pr_dialog.dismiss();
				mInfoLabel.setText("delete ok");
				break;
			}
		}
	}
	
	private void sendMsg(int what, Object obj) {
		msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		uiHanlder.sendMessage(msg);
	}
}
