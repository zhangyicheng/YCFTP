package com.galaxywind.ycftp.client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.galaxywind.ycftp.MainFragment;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.filemanager.RemoteFileManager;
import com.galaxywind.ycftp.filemanager.RemoteFileOprHandler;
import com.galaxywind.ycftp.filemanager.YCFile;
import com.galaxywind.ycftp.filemanager.RemoteFileOprHandler.FileListAdapter;
import com.galaxywind.ycftp.filemanager.RemoteFileOprHandler.RemoteFileOperTask;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.galaxywind.ycftp.widgets.YCListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class RemoteFileListFragment extends MainFragment {
	private Intent intent;

	private RemoteFileManager mFileMag;
	private RemoteFileOprHandler mHandler;
	private RemoteFileOprHandler.FileListAdapter fileListAdapter;

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
	public void onCreate(Bundle saveInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(saveInstanceState);
		setHasOptionsMenu(true);
		mFileMag = new RemoteFileManager();
		mHandler = new RemoteFileOprHandler(mActivity, mFileMag);
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
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		mHandler.setUpdateLabels(mPathLabel, mDetailLabel);
		fileListAdapter = mHandler.new FileListAdapter(true);
		filsLv.setAdapter(fileListAdapter);
		mHandler.setListAdapter(fileListAdapter);
		setEmptyView(filsLv, "Empty");
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.remote_file_option, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
//		final String[] data;
//		int at;
//		AlertDialog.Builder builder;
		switch (item.getItemId()) {
		case R.id.download:
			showEnsureDialog(RemoteFileOprHandler.DOWNLOAD_TYPE,"download");
			return true;
		case R.id.delete:
			showEnsureDialog(RemoteFileOprHandler.DELETE_TYPE,"delete");
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void showEnsureDialog(final int oprType,String oprName){
		if (mHandler.mMultiSelectData == null || mHandler.mMultiSelectData.isEmpty()) {
			fileListAdapter.killMultiSelect(true);
			ToastUtil.showToast(mActivity, R.string.no_selected_file);
			return;
		}

		final YCFile[] data = new YCFile[mHandler.mMultiSelectData.size()];
		int at = 0;

		for (YCFile ycfile : mHandler.mMultiSelectData)
			data[at++] = ycfile;

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage("Are you sure you want to " +oprName+" "+ data.length
				+ " files?");
		builder.setCancelable(false);
		builder.setPositiveButton(oprName,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mHandler.new RemoteFileOperTask(oprType).execute(data);
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
