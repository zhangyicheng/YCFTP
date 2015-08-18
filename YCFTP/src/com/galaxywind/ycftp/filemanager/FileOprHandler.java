package com.galaxywind.ycftp.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.client.FTPClientHelper;
import com.galaxywind.ycftp.client.handler.JsonResponsehandler;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.utils.ToastUtil;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
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
import android.widget.Toast;

/**
 * UI点击事件处理器 FileListActivity和FileManager之间的纽带 使FileManager模块化（解耦）
 * 
 * @author Joe Berria
 */
public class FileOprHandler implements OnClickListener {
	/* w文件操作类型 */
	public static final int SEARCH_TYPE = 0x00;
	public static final int COPY_TYPE = 0x01;
	public static final int UNZIP_TYPE = 0x02;
	public static final int UNZIPTO_TYPE = 0x03;
	public static final int ZIP_TYPE = 0x04;
	public static final int DELETE_TYPE = 0x05;
	public static final int MANAGE_DIALOG = 0x06;
	public static final int UPLOAD_TYPE = 0X07;

	private final Context mContext;
	private final FileManager mFileMang;
	private ThumbnailCreator mThumbnail;
	private FileListAdapter mDelegate;

	private boolean multi_select_flag = false;
	private boolean delete_after_copy = false;
	private boolean thumbnail_flag = true;
	private int mColor = Color.WHITE;

	/*
	 * mDataSource:当前目录下文件的路径List; mMultiSelectData:多选选中的路径List
	 */
	public ArrayList<String> mDataSource, mMultiSelectData;
	private TextView mPathLabel;
	private TextView mInfoLabel;

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
				ToastUtil.showToast(mContext, (String) msg.obj);
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
	public FileOprHandler(Context context, final FileManager manager) {
		mContext = context;
		mFileMang = manager;
		mDataSource = new ArrayList<String>(mFileMang.setHomeDir(""));
	}

	/**
	 * @param context
	 *            上下文
	 * @param manager
	 *            从context里实例化的FileManager
	 * @param location
	 *            路径
	 */
	public FileOprHandler(Context context, final FileManager manager,
			String location) {
		mContext = context;
		mFileMang = manager;

		mDataSource = new ArrayList<String>(
				mFileMang.getNextDir(location, true));
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

	public ArrayList<String> getmDataSource() {
		return mDataSource;
	}

	public void setmDataSource(ArrayList<String> mDataSource) {
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
	 * 按文件名搜索
	 * 
	 * @param name
	 *            文件、文件夹名
	 */
	public void searchForFile(String name) {
		new FileOperTask(SEARCH_TYPE).execute(name);
	}

	/**
	 * 删除文件
	 * 
	 * @param name
	 *            文件名
	 */
	public void deleteFile(String name) {
		new FileOperTask(DELETE_TYPE).execute(name);
	}

	/**
	 * 复制文件
	 * 
	 * @param oldLocation
	 *            源路径
	 * @param newLocation
	 *            目标路径
	 */
	public void copyFile(String oldLocation, String newLocation) {
		String[] data = { oldLocation, newLocation };

		new FileOperTask(COPY_TYPE).execute(data);
	}

	/**
	 * 复制多选文件
	 * 
	 * @param newLocation
	 */
	public void copyFileMultiSelect(String newLocation) {
		String[] data;
		int index = 1;

		if (mMultiSelectData.size() > 0) {
			data = new String[mMultiSelectData.size() + 1];
			data[0] = newLocation;

			for (String s : mMultiSelectData)
				data[index++] = s;

			new FileOperTask(COPY_TYPE).execute(data);
		}
	}

	/**
	 * 解压zip文件到当前目录
	 * 
	 * @param file
	 *            zip文件名
	 * @param path
	 *            当前目录
	 */
	public void unZipFile(String file, String path) {
		new FileOperTask(UNZIP_TYPE).execute(file, path);
	}

	/**
	 * 解压zip文件到指定路径
	 * 
	 * @param name
	 *            the name of the of the new file (the dir name is used)
	 * @param newDir
	 *            the dir where to extract to
	 * @param oldDir
	 *            the dir where the zip file is
	 */
	public void unZipFileToDir(String name, String newDir, String oldDir) {
		new FileOperTask(UNZIPTO_TYPE).execute(name, newDir, oldDir);
	}

	/**
	 * 创建zip
	 * 
	 * @param zipPath
	 *            the path to the directory you want to zip
	 */
	public void zipFile(String zipPath) {
		new FileOperTask(ZIP_TYPE).execute(zipPath);
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
			if (mFileMang.getCurrentDir() != "/") {
				if (multi_select_flag) {
					mDelegate.killMultiSelect(true);
					Toast.makeText(mContext, "Multi-select is now off",
							Toast.LENGTH_SHORT).show();
				}

				stopThumbnailThread();
				updateDirectory(mFileMang.getPreviousDir());
				if (mPathLabel != null)
					mPathLabel.setText(mFileMang.getCurrentDir());
			}
			break;
		case R.id.home_button:
			if (multi_select_flag) {
				mDelegate.killMultiSelect(true);
				Toast.makeText(mContext, "Multi-select is now off",
						Toast.LENGTH_SHORT).show();
			}

			stopThumbnailThread();
			updateDirectory(mFileMang.setHomeDir("/"));
			if (mPathLabel != null)
				mPathLabel.setText(mFileMang.getCurrentDir());
			break;

		case R.id.info_button:
			Intent info = new Intent(mContext, DirectoryInfo.class);
			info.putExtra("PATH_NAME", mFileMang.getCurrentDir());
			mContext.startActivity(info);
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
			/* check if user selected objects before going further */
			if (mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				break;
			}

			ArrayList<Uri> uris = new ArrayList<Uri>();
			int length = mMultiSelectData.size();
			Intent mail_int = new Intent();

			mail_int.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
			mail_int.setType("application/mail");
			mail_int.putExtra(Intent.EXTRA_BCC, "");
			mail_int.putExtra(Intent.EXTRA_SUBJECT, " ");

			for (int i = 0; i < length; i++) {
				File file = new File(mMultiSelectData.get(i));
				uris.add(Uri.fromFile(file));
			}

			mail_int.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			mContext.startActivity(Intent.createChooser(mail_int,
					"Email using..."));

			mDelegate.killMultiSelect(true);
			break;

		case R.id.hidden_move:
		case R.id.hidden_copy:
			/* check if user selected objects before going further */
			if (mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				break;
			}

			if (v.getId() == R.id.hidden_move)
				delete_after_copy = true;

			mInfoLabel.setText("Holding " + mMultiSelectData.size()
					+ " file(s)");

			mDelegate.killMultiSelect(false);
			break;

		case R.id.hidden_delete:
			/* check if user selected objects before going further */
			if (mMultiSelectData == null || mMultiSelectData.isEmpty()) {
				mDelegate.killMultiSelect(true);
				break;
			}

			final String[] data = new String[mMultiSelectData.size()];
			int at = 0;

			for (String string : mMultiSelectData)
				data[at++] = string;

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Are you sure you want to delete " + data.length
					+ " files? This cannot be " + "undone.");
			builder.setCancelable(false);
			builder.setPositiveButton("Delete",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new FileOperTask(DELETE_TYPE).execute(data);
							mDelegate.killMultiSelect(true);
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mDelegate.killMultiSelect(true);
							dialog.cancel();
						}
					});

			builder.create().show();
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
	public String getData(int position) {

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
	public void updateDirectory(ArrayList<String> content) {
		// 取消全选
		mDelegate.killMultiSelect(true);

		if (!mDataSource.isEmpty())
			mDataSource.clear();

		for (String data : content)
			mDataSource.add(data);
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
	public class FileListAdapter extends ArrayAdapter<String> {
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

		private void refreshData() {
			initCheck();
			notifyDataSetChanged();
		}

		/**
		 * 向多选路径List添加数据 mMultiSelectData里没有该路径记录就加入，有就删除
		 * 
		 * @param index
		 * @param path
		 */
		public void addMultiPosition(int index, String path) {
			if (positions == null)
				positions = new ArrayList<Integer>();

			if (mMultiSelectData == null) {
				positions.add(index);
				add_multiSelect_file(path);

			} else if (mMultiSelectData.contains(path)) {
				if (positions.contains(index))
					positions.remove(new Integer(index));

				mMultiSelectData.remove(path);

			} else {
				positions.add(index);
				add_multiSelect_file(path);
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
			int num_items = 0;
			String temp = mFileMang.getCurrentDir();
			File file = new File(temp + "/" + mDataSource.get(position));
			String[] list = file.list();

			if (list != null)
				num_items = list.length;

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
				if (showCheckBox) {
					mViewHolder.mSelect.setVisibility(View.VISIBLE);
				} else {
					mViewHolder.mSelect.setVisibility(View.GONE);
				}
				convertView.setTag(mViewHolder);

			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}

			mViewHolder.mSelect.setOnClickListener(new CheckBoxClickLister(
					position, file.getAbsolutePath()));
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

					if (thumbnail_flag && file.length() != 0) {
						Bitmap thumb = mThumbnail
								.isBitmapCached(file.getPath());

						if (thumb == null) {
							final Handler handle = new Handler(
									new Handler.Callback() {
										public boolean handleMessage(Message msg) {
											notifyDataSetChanged();

											return true;
										}
									});

							mThumbnail.createNewThumbnail(mDataSource,
									mFileMang.getCurrentDir(), handle);

							if (!mThumbnail.isAlive())
								mThumbnail.start();

						} else {
							mViewHolder.icon.setImageBitmap(thumb);
						}

					} else {
						mViewHolder.icon.setImageResource(R.drawable.image);
					}

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

			} else if (file != null && file.isDirectory()) {
				if (file.canRead() && file.list().length > 0)
					mViewHolder.icon.setImageResource(R.drawable.folder_full);
				else
					mViewHolder.icon.setImageResource(R.drawable.folder);
			}

			String permission = getFilePermissions(file);

			if (file.isFile()) {
				double size = file.length();
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

				if (file.isHidden())
					mViewHolder.bottomView.setText("(hidden) | " + display_size
							+ " | " + permission);
				else
					mViewHolder.bottomView.setText(display_size + " | "
							+ permission);

			} else {
				if (file.isHidden())
					mViewHolder.bottomView.setText("(hidden) | " + num_items
							+ " items | " + permission);
				else
					mViewHolder.bottomView.setText(num_items + " items | "
							+ permission);
			}

			mViewHolder.topView.setText(file.getName());

			return convertView;
		}

		private void add_multiSelect_file(String src) {
			if (mMultiSelectData == null)
				mMultiSelectData = new ArrayList<String>();

			mMultiSelectData.add(src);
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
			// mDelegate.addMultiPosition(position, path);
			//
			// if (mDelegate.getItemSelected().get(position)) {
			// mDelegate.getItemSelected().put(position, false);
			// } else {
			// mDelegate.getItemSelected().put(position, true);
			// }
		}
	}

	/*
	 * 复选框监听
	 */
	class CheckBoxClickLister implements OnClickListener {

		int position;// 列表下标
		String path;// 路径

		public CheckBoxClickLister(int position, String path) {
			super();
			this.position = position;
			this.path = path;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mDelegate.addMultiPosition(position, path);

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
	public class FileOperTask extends
			AsyncTask<String, Void, ArrayList<String>> {
		private String file_name;
		private ProgressDialog pr_dialog;
		private int type;
		private int copy_rtn;

		public FileOperTask(int type) {
			this.type = type;
		}

		@Override
		protected void onPreExecute() {

			switch (type) {
			case SEARCH_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Searching",
						"Searching current file system...", true, true);
				break;

			case COPY_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Copying",
						"Copying file...", true, false);
				break;

			case UNZIP_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Unzipping",
						"Unpacking zip file please wait...", true, false);
				break;

			case UNZIPTO_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Unzipping",
						"Unpacking zip file please wait...", true, false);
				break;

			case ZIP_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Zipping",
						"Zipping folder...", true, false);
				break;

			case DELETE_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Deleting",
						"Deleting files...", true, false);
				break;
			case UPLOAD_TYPE:
				pr_dialog = ProgressDialog.show(mContext, "Uploading",
						"Deleting files...", true, false);
				break;
			}
		}

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			int len = params.length;
			switch (type) {
			case SEARCH_TYPE:
				file_name = params[0];
				ArrayList<String> found = mFileMang.searchInDirectory(
						mFileMang.getCurrentDir(), file_name);
				return found;

			case COPY_TYPE:

				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					for (int i = 1; i < len; i++) {
						copy_rtn = mFileMang.copyToDirectory(params[i],
								params[0]);

						if (delete_after_copy)
							mFileMang.deleteTarget(params[i]);
					}
				} else {
					copy_rtn = mFileMang.copyToDirectory(params[0], params[1]);

					if (delete_after_copy)
						mFileMang.deleteTarget(params[0]);
				}

				delete_after_copy = false;
				return null;

			case UNZIP_TYPE:
				mFileMang.extractZipFiles(params[0], params[1]);
				return null;

			case UNZIPTO_TYPE:
				mFileMang.extractZipFilesFromDir(params[0], params[1],
						params[2]);
				return null;

			case ZIP_TYPE:
				mFileMang.createZipFile(params[0]);
				return null;

			case DELETE_TYPE:
				int size = params.length;

				for (int i = 0; i < size; i++)
					mFileMang.deleteTarget(params[i]);

				return null;
			case UPLOAD_TYPE:
				if(YCFTPApplication.site==null){
					sendMsg(DEFAULT, "Please login");
					break;
				}
				for (int i = 0; i < len; i++) {
					final File file = new File(params[i]);
					final JSONObject requestJo = new JSONObject();
					try {
						requestJo.put(FTPConstant.OPERATION,
								FTPConstant.UPLOAD_OPR);
						requestJo.put("FilePath", file.getPath());
						requestJo.put("FileName", file.getName());
						requestJo.put("IsFile", file.isFile());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FTPClientHelper.request(mContext, YCFTPApplication.site,
							requestJo, new JsonResponsehandler() {

								@Override
								public void onSuccess(int statusCode,
										JSONObject responseJo) {
									// TODO Auto-generated method stub
									try {
										switch (responseJo
												.getInt("ResponseCode")) {
										case FTPConstant.STATUS_CODE_SUCCESS:
											sendMsg(DEFAULT, responseJo.get("Response"));
											JSONObject jo = new JSONObject();
											jo.put(FTPConstant.OPERATION,
													FTPConstant.UPLOAD_FILE);
											jo.put("FilePath", file.getPath());
											jo.put("FileName", file.getName());
											jo.put("IsFile", file.isFile());
											
											FTPClientHelper.request(mContext, YCFTPApplication.site, jo, new JsonResponsehandler() {
												
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
													sendMsg(DEFAULT, throwable.toString());
												}
											});
											break;
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								@Override
								public void onFailure(int statusCode,
										Throwable throwable) {
									// TODO Auto-generated method stub
									sendMsg(DEFAULT, throwable.toString());
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
			case SEARCH_TYPE:
				if (len == 0) {
					Toast.makeText(mContext, "Couldn't find " + file_name,
							Toast.LENGTH_SHORT).show();

				} else {
					names = new CharSequence[len];

					for (int i = 0; i < len; i++) {
						String entry = file.get(i);
						names[i] = entry.substring(entry.lastIndexOf("/") + 1,
								entry.length());
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setTitle("Found " + len + " file(s)");
					builder.setItems(names,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int position) {
									String path = file.get(position);
									updateDirectory(mFileMang.getNextDir(
											path.substring(0,
													path.lastIndexOf("/")),
											true));
								}
							});

					AlertDialog dialog = builder.create();
					dialog.show();
				}

				pr_dialog.dismiss();
				break;

			case COPY_TYPE:
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					multi_select_flag = false;
					mMultiSelectData.clear();
				}

				if (copy_rtn == 0)
					Toast.makeText(mContext,
							"File successfully copied and pasted",
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(mContext, "Copy pasted failed",
							Toast.LENGTH_SHORT).show();

				pr_dialog.dismiss();
				mInfoLabel.setText("");
				break;

			case UNZIP_TYPE:
				updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(),
						true));
				pr_dialog.dismiss();
				break;

			case UNZIPTO_TYPE:
				updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(),
						true));
				pr_dialog.dismiss();
				break;

			case ZIP_TYPE:
				updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(),
						true));
				pr_dialog.dismiss();
				break;

			case DELETE_TYPE:
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					mMultiSelectData.clear();
					multi_select_flag = false;
				}

				updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(),
						true));
				pr_dialog.dismiss();
				mInfoLabel.setText("");
				break;
			case UPLOAD_TYPE:
				if (mMultiSelectData != null && !mMultiSelectData.isEmpty()) {
					mMultiSelectData.clear();
					multi_select_flag = false;
				}

				updateDirectory(mFileMang.getNextDir(mFileMang.getCurrentDir(),
						true));
				pr_dialog.dismiss();
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
