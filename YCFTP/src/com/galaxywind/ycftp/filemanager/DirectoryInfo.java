package com.galaxywind.ycftp.filemanager;

import java.io.File;
import java.util.Date;

import com.galaxywind.ycftp.BaseActivity;
import com.galaxywind.ycftp.R;

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.StatFs;
import android.os.Environment;
import android.content.Intent;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;

public class DirectoryInfo extends BaseActivity {
	private static final int KB = 1024;
	private static final int MG = KB * KB;
	private static final int GB = MG * KB;
	private String mPathName;
	private TextView mNameLabel, mPathLabel, mDirLabel,
					 mFileLabel, mTimeLabel, mTotalLabel;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.info_layout);
		
		Intent i = getIntent();
		if(i != null) {
			if(i.getAction() != null && i.getAction().equals(Intent.ACTION_VIEW)) {
				mPathName = i.getData().getPath();
				
				if(mPathName == null)
					mPathName = "";
			} else {
				mPathName = i.getExtras().getString("PATH_NAME");
			}
		}
		initViews();
	}
	
	
	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();

		titleTv.setText(R.string.dir_info);
		mNameLabel = (TextView)findViewById(R.id.name_label);
		mPathLabel = (TextView)findViewById(R.id.path_label);
		mDirLabel = (TextView)findViewById(R.id.dirs_label);
		mFileLabel = (TextView)findViewById(R.id.files_label);
		mTimeLabel = (TextView)findViewById(R.id.time_stamp);
		mTotalLabel = (TextView)findViewById(R.id.total_size);
		
		Button zip = (Button)findViewById(R.id.zip_button);
		zip.setVisibility(Button.GONE);	 
		new GetFileSystemInfoTask().execute(mPathName);
	}


	/**
	 * 文件操作异步任务
	 * @author Administrator
	 *
	 */
	private class GetFileSystemInfoTask extends AsyncTask<String, Void, Long> {
		private ProgressDialog dialog;
		private String mDisplaySize;
		private int mFileCount = 0;
		private int mDirCount = 0;
		
		protected void onPreExecute(){
			dialog = ProgressDialog.show(DirectoryInfo.this, "", "Calculating information...", true, true);
		}
		
		protected Long doInBackground(String... vals) {
			FileManager flmg = new FileManager();
			File dir = new File(vals[0]);
			long size = 0;
			int len = 0;

			File[] list = dir.listFiles();
			if(list != null)
				len = list.length;
			
			for (int i = 0; i < len; i++){
				if(list[i].isFile())
					mFileCount++;
				else if(list[i].isDirectory())
					mDirCount++;
			}
			
			if(vals[0].equals("/")) {				
				StatFs fss = new StatFs(Environment.getRootDirectory().getPath());
				size = fss.getAvailableBlocks() * (fss.getBlockSize() / KB);
				
				mDisplaySize = (size > GB) ? 
						String.format("%.2f Gb ", (double)size / MG) :
						String.format("%.2f Mb ", (double)size / KB);
				
			}else if(vals[0].equals(Environment.getExternalStorageDirectory().getPath())) {
				StatFs fs = new StatFs(Environment.getExternalStorageDirectory()
										.getPath());
				size = fs.getBlockCount() * (fs.getBlockSize() / KB);
				
				mDisplaySize = (size > GB) ? 
					String.format("%.2f Gb ", (double)size / GB) :
					String.format("%.2f Gb ", (double)size / MG);
				
			} else {
				size = flmg.getDirSize(vals[0]);
						
				if (size > GB)
					mDisplaySize = String.format("%.2f Gb ", (double)size / GB);
				else if (size < GB && size > MG)
					mDisplaySize = String.format("%.2f Mb ", (double)size / MG);
				else if (size < MG && size > KB)
					mDisplaySize = String.format("%.2f Kb ", (double)size/ KB);
				else
					mDisplaySize = String.format("%.2f bytes ", (double)size);
			}
			
			return size;
		}
		
		protected void onPostExecute(Long result) {
			File dir = new File(mPathName);
			
			mNameLabel.setText(dir.getName());
			mPathLabel.setText(dir.getAbsolutePath());
			mDirLabel.setText(mDirCount + " folders ");
			mFileLabel.setText(mFileCount + " files ");
			mTotalLabel.setText(mDisplaySize);
			mTimeLabel.setText(new Date(dir.lastModified()) + " ");
			
			dialog.cancel();
		}	
	}
}
