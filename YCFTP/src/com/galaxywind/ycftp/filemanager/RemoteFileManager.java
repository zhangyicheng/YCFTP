package com.galaxywind.ycftp.filemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RemoteFileManager {
	private static final int BUFFER = 2048;
	private static final int SORT_NONE = 0;
	private static final int SORT_ALPHA = 1;
	private static final int SORT_TYPE = 2;
	private static final int SORT_SIZE = 3;
	private boolean mShowHiddenFiles = false;
	private int mSortType = SORT_ALPHA;
	private long mDirSize = 0;
	// 文件目录栈
	private Stack<String> mPathStack;
	private List<YCFile> mRemoteFiles;

	public RemoteFileManager() {
		mRemoteFiles = new ArrayList<YCFile>();
		mPathStack = new Stack<String>();
		mPathStack.push("/");
		// mPathStack.push(mPathStack.peek() + "sdcard");
	}

	/**
	 * 获取当前目录
	 * @return 当前目录
	 */
	public String getCurrentDir() {
		return mPathStack.peek();
	}
	
	/**
	 * 在特定目录下搜索文件
	 * 
	 * @param dir
	 * @param pathName
	 * @return
	 */
	public ArrayList<String> searchInDirectory(String dir, String pathName) {
		ArrayList<String> names = new ArrayList<String>();

		return names;
	}
	
	public int copyToDirectory(String old, String newDir) {
		return 0;
	}
	
	public int deleteTarget(YCFile file) {
		return 0;
	}
}
