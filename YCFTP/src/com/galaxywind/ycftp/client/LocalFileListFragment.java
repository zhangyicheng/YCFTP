package com.galaxywind.ycftp.client;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.galaxywind.ycftp.MainFragment;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.filemanager.DirectoryInfo;
import com.galaxywind.ycftp.filemanager.FileManager;
import com.galaxywind.ycftp.filemanager.FileOprHandler;
import com.galaxywind.ycftp.filemanager.Settings;
import com.galaxywind.ycftp.filemanager.YCFile;
import com.galaxywind.ycftp.filemanager.FileOprHandler.FileListAdapter;
import com.galaxywind.ycftp.filemanager.RemoteFileOprHandler.RemoteFileOperTask;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.galaxywind.ycftp.widgets.YCListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.lidroid.xutils.view.annotation.event.OnItemClick;

public class LocalFileListFragment extends MainFragment{
	public static final String ACTION_WIDGET = "com.nexes.manager.Main.ACTION_WIDGET";

	private static final String PREFS_NAME = "ManagerPrefsFile"; // user
																	// preference
																	// file name
	private static final String PREFS_HIDDEN = "hidden";
	private static final String PREFS_COLOR = "color";
	private static final String PREFS_THUMBNAIL = "thumbnail";
	private static final String PREFS_SORT = "sort";
	private static final String PREFS_STORAGE = "sdcard space";

	private static final int MENU_MKDIR = 0x00; // option menu id
	private static final int MENU_SETTING = 0x01; // option menu id
	private static final int MENU_SEARCH = 0x02; // option menu id
	private static final int MENU_QUIT = 0x04; // option menu id
	private static final int SEARCH_B = 0x09;

	private static final int D_MENU_DELETE = 0x05; // context menu id
	private static final int D_MENU_RENAME = 0x06; // context menu id
	private static final int D_MENU_COPY = 0x07; // context menu id
	private static final int D_MENU_PASTE = 0x08; // context menu id
	private static final int D_MENU_ZIP = 0x0e; // context menu id
	private static final int D_MENU_UNZIP = 0x0f; // context menu id
	private static final int D_MENU_MOVE = 0x30; // context menu id
	private static final int F_MENU_MOVE = 0x20; // context menu id
	private static final int F_MENU_DELETE = 0x0a; // context menu id
	private static final int F_MENU_RENAME = 0x0b; // context menu id
	private static final int F_MENU_ATTACH = 0x0c; // context menu id
	private static final int F_MENU_COPY = 0x0d; // context menu id
	private static final int SETTING_REQ = 0x10; // request code for intent

	private Intent intent;

	private FileManager mFileMag;
	private FileOprHandler mHandler;
	private FileOprHandler.FileListAdapter fileListAdapter;

	private SharedPreferences mSettings;
	private boolean mReturnIntent = false;
	private boolean mHoldingFile = false;
	private boolean mHoldingZip = false;
	private boolean mUseBackKey = true;
	private String mCopiedTarget;
	private String mZippedTarget;
	private String mSelectedListItem; // item from context menu

	@ViewInject(R.id.path_label)
	private TextView mPathLabel;
	
	@ViewInject(R.id.detail_label)
	private TextView mDetailLabel;

	@ViewInject(R.id.hidden_buttons)
	private LinearLayout hidden_lay;
	
	@ViewInject(R.id.up_dir_LL)
	private View up_dir_LL;

	
	private String action;
	
	@ViewInject(R.id.filsLv)
	private YCListView filsLv;
	
	private String location;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		action = getArguments().getString("action");
	}

	@Override
	public void onCreate(Bundle saveInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(saveInstanceState);
		setHasOptionsMenu(true);
		/* 读取设置 */
		mSettings = mActivity.getSharedPreferences(PREFS_NAME, 0);
		boolean hide = mSettings.getBoolean(PREFS_HIDDEN, false);
		boolean thumb = mSettings.getBoolean(PREFS_THUMBNAIL, true);
		int space = mSettings.getInt(PREFS_STORAGE, View.VISIBLE);
		int color = mSettings.getInt(PREFS_COLOR, -1);
		int sort = mSettings.getInt(PREFS_SORT, 3);

		mFileMag = new FileManager();
		mFileMag.setShowHiddenFiles(hide);
		mFileMag.setSortType(sort);

		location = SharePreferenceUtil.getPreference(mActivity, YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.ROOT_DIR);
		if(StringUtil.isEmpty(location)){
			location = "/";
		}
		if (saveInstanceState != null) {
			mHandler = new FileOprHandler(mActivity, mFileMag,
					saveInstanceState.getString("location"));
		} else {
			mHandler = new FileOprHandler(mActivity, mFileMag,location);
		}
		mHandler.setTextColor(color);
		mHandler.setShowThumbnails(thumb);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		fragmentView = inflater.inflate(R.layout.fragment_filelist, null);
		ViewUtils.inject(this, fragmentView);
		initViews();
		return fragmentView;
	}
	
	
	@Override
	public ListView getListView() {
		// TODO Auto-generated method stub
		return filsLv;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LogUtils.i("FileListFragment onResume");
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		fileListAdapter = mHandler.new FileListAdapter(true);

		mHandler.setListAdapter(fileListAdapter);
//		filsLv = (filsLv) fragmentView.findViewById(R.id.filsLv);
//		filsLv = (ListView) fragmentView.findViewById(R.id.filsLv);
//		filsLv.setOnItemClickListener(this);
		filsLv.setAdapter(fileListAdapter);
		setEmptyView(filsLv, "Empty");
		

		/* 注册ContextMenu */
		registerForContextMenu(filsLv);

		mPathLabel.setText(location);

		mHandler.setUpdateLabels(mPathLabel, mDetailLabel);

		/* 按钮点击事件设置 */
		int[] img_button_id = { R.id.home_button, R.id.back_button,
				R.id.info_button, R.id.multiselect_button };

		int[] button_id = { R.id.hidden_copy, R.id.hidden_attach,
				R.id.hidden_delete, R.id.hidden_move };

		View[] bimg = new View[img_button_id.length];
		Button[] bt = new Button[button_id.length];

		for (int i = 0; i < img_button_id.length; i++) {

			bimg[i] = fragmentView.findViewById(img_button_id[i]);
			bimg[i].setOnClickListener(mHandler);

			if (i < button_id.length) {
				bt[i] = (Button) fragmentView.findViewById(button_id[i]);
				bt[i].setOnClickListener(mHandler);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
	}

	@OnItemClick(R.id.filsLv)
	public void onItemClick(ListView parent, View view, int position, long id) {
		final String item = mHandler.getData(position);
		boolean multiSelect = mHandler.isMultiSelected();
		File file = new File(mFileMag.getCurrentDir() + "/" + item);
		String item_ext = null;

		try {
			item_ext = item.substring(item.lastIndexOf("."), item.length());

		} catch (IndexOutOfBoundsException e) {
			item_ext = "";
		}

		// 是文件夹进下一级；是文件显示打开方式选项
		if (file.isDirectory()) {
			if (file.canRead()) {
				mHandler.stopThumbnailThread();
				mHandler.updateDirectory(mFileMag.getNextDir(item, false));
				mPathLabel.setText(mFileMag.getCurrentDir());

				/*
				 * set back button switch to true (this will be better
				 * implemented later)
				 */
				if (!mUseBackKey) {
					mUseBackKey = true;
				}

			} else {
				ToastUtil.showToast(mActivity,
						"Can't read folder due to permissions");
			}
		}

		/* music file selected--add more audio formats */
		else if (item_ext.equalsIgnoreCase(".mp3")
				|| item_ext.equalsIgnoreCase(".m4a")
				|| item_ext.equalsIgnoreCase(".mp4")) {

			if (mReturnIntent) {
				returnIntentResults(file);
			} else {
				Intent i = new Intent();
				i.setAction(android.content.Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(file), "audio/*");
				startActivity(i);
			}
		}

		/* photo file selected */
		else if (item_ext.equalsIgnoreCase(".jpeg")
				|| item_ext.equalsIgnoreCase(".jpg")
				|| item_ext.equalsIgnoreCase(".png")
				|| item_ext.equalsIgnoreCase(".gif")
				|| item_ext.equalsIgnoreCase(".tiff")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent picIntent = new Intent();
					picIntent.setAction(android.content.Intent.ACTION_VIEW);
					picIntent.setDataAndType(Uri.fromFile(file), "image/*");
					startActivity(picIntent);
				}
			}
		}

		/* video file selected--add more video formats */
		else if (item_ext.equalsIgnoreCase(".m4v")
				|| item_ext.equalsIgnoreCase(".3gp")
				|| item_ext.equalsIgnoreCase(".wmv")
				|| item_ext.equalsIgnoreCase(".mp4")
				|| item_ext.equalsIgnoreCase(".ogg")
				|| item_ext.equalsIgnoreCase(".wav")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent movieIntent = new Intent();
					movieIntent.setAction(android.content.Intent.ACTION_VIEW);
					movieIntent.setDataAndType(Uri.fromFile(file), "video/*");
					startActivity(movieIntent);
				}
			}
		}

		/* zip file */
		else if (item_ext.equalsIgnoreCase(".zip")) {

			if (mReturnIntent) {
				returnIntentResults(file);

			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				AlertDialog alert;
				mZippedTarget = mFileMag.getCurrentDir() + "/" + item;
				CharSequence[] option = { "Extract here", "Extract to..." };

				builder.setTitle("Extract");
				builder.setItems(option, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							String dir = mFileMag.getCurrentDir();
							mHandler.unZipFile(item, dir + "/");
							break;

						case 1:
							mDetailLabel.setVisibility(View.VISIBLE);
							mDetailLabel.setText("Holding " + item
									+ " to extract");
							mHoldingZip = true;
							break;
						}
					}
				});

				alert = builder.create();
				alert.show();
			}
		}

		/* gzip files, this will be implemented later */
		else if (item_ext.equalsIgnoreCase(".gzip")
				|| item_ext.equalsIgnoreCase(".gz")) {

			if (mReturnIntent) {
				returnIntentResults(file);

			} else {
				// TODO:
			}
		}

		/* pdf file selected */
		else if (item_ext.equalsIgnoreCase(".pdf")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent pdfIntent = new Intent();
					pdfIntent.setAction(android.content.Intent.ACTION_VIEW);
					pdfIntent.setDataAndType(Uri.fromFile(file),
							"application/pdf");

					try {
						startActivity(pdfIntent);
					} catch (ActivityNotFoundException e) {
						ToastUtil.showToast(mActivity,
								"Sorry, couldn't find a pdf viewer");
					}
				}
			}
		}

		/* Android application file */
		else if (item_ext.equalsIgnoreCase(".apk")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent apkIntent = new Intent();
					apkIntent.setAction(android.content.Intent.ACTION_VIEW);
					apkIntent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					startActivity(apkIntent);
				}
			}
		}

		/* HTML file */
		else if (item_ext.equalsIgnoreCase(".html")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent htmlIntent = new Intent();
					htmlIntent.setAction(android.content.Intent.ACTION_VIEW);
					htmlIntent.setDataAndType(Uri.fromFile(file), "text/html");

					try {
						startActivity(htmlIntent);
					} catch (ActivityNotFoundException e) {
						ToastUtil.showToast(mActivity,
								"Sorry, couldn't find a HTML viewer");
					}
				}
			}
		}

		/* text file */
		else if (item_ext.equalsIgnoreCase(".txt")) {

			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent txtIntent = new Intent();
					txtIntent.setAction(android.content.Intent.ACTION_VIEW);
					txtIntent.setDataAndType(Uri.fromFile(file), "text/plain");

					try {
						startActivity(txtIntent);
					} catch (ActivityNotFoundException e) {
						txtIntent.setType("text/*");
						startActivity(txtIntent);
					}
				}
			}
		}

		/* generic intent */
		else {
			if (file.exists()) {
				if (mReturnIntent) {
					returnIntentResults(file);

				} else {
					Intent generic = new Intent();
					generic.setAction(android.content.Intent.ACTION_VIEW);
					generic.setDataAndType(Uri.fromFile(file), "text/plain");

					try {
						startActivity(generic);
					} catch (ActivityNotFoundException e) {
						ToastUtil.showToast(mActivity,
								"Sorry, couldn't find anything " + "to open "
										+ file.getName());
					}
				}
			}
		}
	}

	/**
	 * 返回结果
	 * 
	 * @param data
	 */
	private void returnIntentResults(File data) {
		mReturnIntent = false;
		if (getTargetFragment() == null)
			return;
		String rootdir = mFileMag.getCurrentDir();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(YCFTPApplication.ROOT_DIR, rootdir);
		getTargetFragment().onActivityResult(1, Activity.RESULT_OK,
				resultIntent);
	}

	/*
	 * *************菜单开始****************
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.local_file_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.newdir:
			// case MENU_MKDIR:
			getActivity().showDialog(MENU_MKDIR);
			// showDialog(R.id.newdir);
			return true;

		case R.id.search:
			// case MENU_SEARCH:
			getActivity().showDialog(MENU_SEARCH);
			// showDialog(R.id.search);
			return true;
		case R.id.setting:
			// case MENU_SETTING:
			Intent settings_int = new Intent(mActivity, Settings.class);
			settings_int.putExtra("HIDDEN",
					mSettings.getBoolean(PREFS_HIDDEN, false));
			settings_int.putExtra("THUMBNAIL",
					mSettings.getBoolean(PREFS_THUMBNAIL, true));
			settings_int.putExtra("COLOR", mSettings.getInt(PREFS_COLOR, -1));
			settings_int.putExtra("SORT", mSettings.getInt(PREFS_SORT, 0));
			settings_int.putExtra("SPACE",
					mSettings.getInt(PREFS_STORAGE, View.VISIBLE));
			startActivityForResult(settings_int, SETTING_REQ);
			return true;
		case R.id.root:
			if (mHandler.isMultiSelected()) {
				fileListAdapter.killMultiSelect(true);
				ToastUtil.showShortToast(mActivity, "Multi-select is now off");
			}

			mHandler.stopThumbnailThread();
			mHandler.updateDirectory(mFileMag.setHomeDir("/"));
			if (mPathLabel != null)
				mPathLabel.setText(mFileMag.getCurrentDir());
			return true;
		case R.id.select_all:
			if (mHandler.isMultiSelected()) {
				// 取消全选
				fileListAdapter.killMultiSelect(true);

			} else {
				// 全选
				ArrayList<String> dataSouce = mHandler.getmDataSource();
				int listSize = dataSouce.size();
				for (int i = 0; i < listSize; i++) {
					fileListAdapter.addMultiPosition(i, dataSouce.get(i));
					fileListAdapter.getItemSelected().put(i, true);
				}
				mHandler.setMultiSelectFlag(true);

				hidden_lay.setVisibility(LinearLayout.VISIBLE);
			}
			return true;
		case R.id.about:
			Intent info = new Intent(mActivity, DirectoryInfo.class);
			info.putExtra("PATH_NAME", mFileMag.getCurrentDir());
			startActivity(info);
			return true;
		case R.id.upload:
			showEnsureDialog(FileOprHandler.UPLOAD_TYPE, "upload");
			break;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo info) {
		super.onCreateContextMenu(menu, v, info);

		boolean multi_data = mHandler.hasMultiSelectData();
		AdapterContextMenuInfo _info = (AdapterContextMenuInfo) info;
		mSelectedListItem = mHandler.getData(_info.position);

		/* is it a directory and is multi-select turned off */
		if (mFileMag.isDirectory(mSelectedListItem)
				&& !mHandler.isMultiSelected()) {
			menu.setHeaderTitle("Folder operations");
			menu.add(0, D_MENU_DELETE, 0, "Delete Folder");
			menu.add(0, D_MENU_RENAME, 0, "Rename Folder");
			menu.add(0, D_MENU_COPY, 0, "Copy Folder");
			menu.add(0, D_MENU_MOVE, 0, "Move(Cut) Folder");
			menu.add(0, D_MENU_ZIP, 0, "Zip Folder");
			menu.add(0, D_MENU_PASTE, 0, "Paste into folder").setEnabled(
					mHoldingFile || multi_data);
			menu.add(0, D_MENU_UNZIP, 0, "Extract here")
					.setEnabled(mHoldingZip);

			/* is it a file and is multi-select turned off */
		} else if (!mFileMag.isDirectory(mSelectedListItem)
				&& !mHandler.isMultiSelected()) {
			menu.setHeaderTitle("File Operations");
			menu.add(0, F_MENU_DELETE, 0, "Delete File");
			menu.add(0, F_MENU_RENAME, 0, "Rename File");
			menu.add(0, F_MENU_COPY, 0, "Copy File");
			menu.add(0, F_MENU_MOVE, 0, "Move(Cut) File");
			menu.add(0, F_MENU_ATTACH, 0, "Email File");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case D_MENU_DELETE:
		case F_MENU_DELETE:
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("Warning ");
			builder.setIcon(R.drawable.warning);
			builder.setMessage("Deleting " + mSelectedListItem
					+ " cannot be undone. Are you sure you want to delete?");
			builder.setCancelable(false);

			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.setPositiveButton("Delete",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mHandler.deleteFile(mFileMag.getCurrentDir() + "/"
									+ mSelectedListItem);
						}
					});
			AlertDialog alert_d = builder.create();
			alert_d.show();
			return true;

		case D_MENU_RENAME:
			getActivity().showDialog(D_MENU_RENAME);
			return true;

		case F_MENU_RENAME:
			getActivity().showDialog(F_MENU_RENAME);
			return true;

		case F_MENU_ATTACH:
			File file = new File(mFileMag.getCurrentDir() + "/"
					+ mSelectedListItem);
			Intent mail_int = new Intent();

			mail_int.setAction(android.content.Intent.ACTION_SEND);
			mail_int.setType("application/mail");
			mail_int.putExtra(Intent.EXTRA_BCC, "");
			mail_int.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
			startActivity(mail_int);
			return true;

		case F_MENU_MOVE:
		case D_MENU_MOVE:
		case F_MENU_COPY:
		case D_MENU_COPY:
			if (item.getItemId() == F_MENU_MOVE
					|| item.getItemId() == D_MENU_MOVE)
				mHandler.setDeleteAfterCopy(true);

			mHoldingFile = true;

			mCopiedTarget = mFileMag.getCurrentDir() + "/" + mSelectedListItem;
			mDetailLabel.setVisibility(View.VISIBLE);
			mDetailLabel.setText("Holding " + mSelectedListItem);
			return true;

		case D_MENU_PASTE:
			boolean multi_select = mHandler.hasMultiSelectData();

			if (multi_select) {
				mHandler.copyFileMultiSelect(mFileMag.getCurrentDir() + "/"
						+ mSelectedListItem);

			} else if (mHoldingFile && mCopiedTarget.length() > 1) {

				mHandler.copyFile(mCopiedTarget, mFileMag.getCurrentDir() + "/"
						+ mSelectedListItem);
				mDetailLabel.setVisibility(View.GONE);
			}

			mHoldingFile = false;
			return true;

		case D_MENU_ZIP:
			String dir = mFileMag.getCurrentDir();

			mHandler.zipFile(dir + "/" + mSelectedListItem);
			return true;

		case D_MENU_UNZIP:
			if (mHoldingZip && mZippedTarget.length() > 1) {
				String current_dir = mFileMag.getCurrentDir() + "/"
						+ mSelectedListItem + "/";
				String old_dir = mZippedTarget.substring(0,
						mZippedTarget.lastIndexOf("/"));
				String name = mZippedTarget.substring(
						mZippedTarget.lastIndexOf("/") + 1,
						mZippedTarget.length());

				if (new File(mZippedTarget).canRead()
						&& new File(current_dir).canWrite()) {
					mHandler.unZipFileToDir(name, current_dir, old_dir);
					mPathLabel.setText(current_dir);

				} else {
					ToastUtil.showToast(mActivity,
							"You do not have permission to unzip " + name);
				}
			}

			mHoldingZip = false;
			mDetailLabel.setVisibility(View.GONE);
			mZippedTarget = "";
			return true;
		}
		return false;
	}
	
	@OnClick(R.id.up_dir_LL)
	private void onClicked(View v){
		switch(v.getId()){
		case R.id.up_dir_LL:
			String current = mFileMag.getCurrentDir();
			if (!current.equals("/")) {

				if (mHandler.isMultiSelected()) {
					fileListAdapter.killMultiSelect(true);
					ToastUtil.showToast(mActivity, "Multi-select is now off");
				} else {
					// stop updating thumbnail icons if its running
					mHandler.stopThumbnailThread();
					mHandler.updateDirectory(mFileMag.getPreviousDir());
					mPathLabel.setText(mFileMag.getCurrentDir());
				}
			} else {
				if (mHandler.isMultiSelected()) {
					fileListAdapter.killMultiSelect(true);
					ToastUtil.showToast(mActivity, "Multi-select is now off");
				}

				mUseBackKey = false;
				mPathLabel.setText(mFileMag.getCurrentDir());
			}
			break;
		}
	}	
	
	private void showEnsureDialog(final int oprType,String oprName){
		if (mHandler.mMultiSelectData == null || mHandler.mMultiSelectData.isEmpty()) {
			fileListAdapter.killMultiSelect(true);
			ToastUtil.showToast(mActivity, R.string.no_selected_file);
			return;
		}

		final String[] data = new String[mHandler.mMultiSelectData.size()];
		int at = 0;

		for (String path : mHandler.mMultiSelectData)
			data[at++] = path;

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage("Are you sure you want to " +oprName+" "+ data.length
				+ " files?");
		builder.setCancelable(false);
		builder.setPositiveButton(oprName,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mHandler.new FileOperTask(oprType).execute(data);
						fileListAdapter.killMultiSelect(true);
					}
				});
		builder.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						fileListAdapter.killMultiSelect(true);
						dialog.cancel();
					}
				});

		builder.create().show();
	}
	
}
