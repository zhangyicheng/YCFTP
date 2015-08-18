package com.galaxywind.ycftp.filemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.util.Log;

/**
 * 文件管理类 完全模块化，完全与UI类解耦 文件操作 copied/pasted, (un)zipped renamed and searched.
 * 
 * @author Joe Berria
 * 
 */
public class FileManager {
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
	private ArrayList<String> mDirContent;

	public FileManager() {
		mDirContent = new ArrayList<String>();
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
	 * This will return a string of the current home path.
	 * 
	 * @return the home directory
	 */
	public ArrayList<String> setHomeDir(String name) {
		// This will eventually be placed as a settings item
		mPathStack.clear();
		mPathStack.push("/");
		if (name != null && !name.equals("")) {
			mPathStack.push(name);
		}
		return populate_list();
	}

	/**
	 * 设置是否显示隐藏文件
	 * 
	 * @param choice
	 *            true 显示,false 不显示
	 */
	public void setShowHiddenFiles(boolean choice) {
		mShowHiddenFiles = choice;
	}

	/**
	 * 设置排序方式
	 * 
	 * @param type
	 */
	public void setSortType(int type) {
		mSortType = type;
	}

	/**
	 * 获取上级路径
	 * 
	 * @return returns the previous path
	 */
	public ArrayList<String> getPreviousDir() {
		int size = mPathStack.size();

		if (size >= 2)
			mPathStack.pop();

		else if (size == 0)
			mPathStack.push("/");

		return populate_list();
	}

	/**
	 * 获取下级路径
	 * 
	 * @param path
	 * @param isFullPath
	 * @return
	 */
	public ArrayList<String> getNextDir(String path, boolean isFullPath) {
		int size = mPathStack.size();

		if (!path.equals(mPathStack.peek()) && !isFullPath) {
			if (size == 1)
				mPathStack.push("/" + path);
			else
				mPathStack.push(mPathStack.peek() + "/" + path);
		}

		else if (!path.equals(mPathStack.peek()) && isFullPath) {
			mPathStack.push(path);
		}

		return populate_list();
	}
	
	public String getPaths(String path, boolean isFullPath) {
		int size = mPathStack.size();

		if (!path.equals(mPathStack.peek()) && !isFullPath) {
			if (size == 1)
				mPathStack.push("/" + path);
			else
				mPathStack.push(mPathStack.peek() + "/" + path);
		}

		else if (!path.equals(mPathStack.peek()) && isFullPath) {
			mPathStack.push(path);
		}
		ArrayList<String> pathList = populate_list();
		StringBuilder sb = new StringBuilder();
		int listSize = pathList.size();
		for (int i = 0; i < listSize; i++) {
			sb.append(pathList.get(i));
			if (i < (listSize - 1)) {
				sb.append(",");
			}
		}
		setHomeDir(path);
		return sb.toString();
	}
	
	public JSONArray getPathsJArray(String path, boolean isFullPath) throws JSONException {
		int size = mPathStack.size();

		if (!path.equals(mPathStack.peek()) && !isFullPath) {
			if (size == 1)
				mPathStack.push("/" + path);
			else
				mPathStack.push(mPathStack.peek() + "/" + path);
		}

		else if (!path.equals(mPathStack.peek()) && isFullPath) {
			mPathStack.push(path);
		}
		ArrayList<String> pathList = populate_list();
		JSONArray ja = new JSONArray();
		Gson gson  =new Gson();
		int listSize = pathList.size();
		for(String pathStr:pathList){
			File file = new File(getCurrentDir()+"/"+pathStr);
			YCFile ycFile = new YCFile();
			ycFile.setName(file.getName());
			ycFile.setPath(file.getPath());
			ycFile.setSize(file.length());
			ycFile.setFile(file.isFile());
			String json = gson.toJson(ycFile);
			JSONObject jo = new JSONObject(json);
			ja.put(jo);
		}
		setHomeDir(path);
		return ja;
	}


	

	/**
	 * 复制
	 * 
	 * @param old
	 *            源路径
	 * @param newDir
	 *            目标路径
	 * @return 0 成功 -1 失败
	 */
	public int copyToDirectory(String old, String newDir) {
		File old_file = new File(old);
		File temp_dir = new File(newDir);
		byte[] data = new byte[BUFFER];
		int read = 0;

		if (old_file.isFile() && temp_dir.isDirectory() && temp_dir.canWrite()) {
			String file_name = old
					.substring(old.lastIndexOf("/"), old.length());
			File cp_file = new File(newDir + file_name);

			try {
				BufferedOutputStream o_stream = new BufferedOutputStream(
						new FileOutputStream(cp_file));
				BufferedInputStream i_stream = new BufferedInputStream(
						new FileInputStream(old_file));

				while ((read = i_stream.read(data, 0, BUFFER)) != -1)
					o_stream.write(data, 0, read);

				o_stream.flush();
				i_stream.close();
				o_stream.close();

			} catch (FileNotFoundException e) {
				Log.e("FileNotFoundException", e.getMessage());
				return -1;

			} catch (IOException e) {
				Log.e("IOException", e.getMessage());
				return -1;
			}

		} else if (old_file.isDirectory() && temp_dir.isDirectory()
				&& temp_dir.canWrite()) {
			String files[] = old_file.list();
			String dir = newDir
					+ old.substring(old.lastIndexOf("/"), old.length());
			int len = files.length;

			if (!new File(dir).mkdir())
				return -1;

			for (int i = 0; i < len; i++)
				copyToDirectory(old + "/" + files[i], dir);

		} else if (!temp_dir.canWrite())
			return -1;

		return 0;
	}

	/**
	 * 
	 * @param zipName
	 *            zip文件名
	 * @param toDir
	 *            目标路径
	 * @param fromDir
	 *            源路径
	 */
	public void extractZipFilesFromDir(String zipName, String toDir,
			String fromDir) {
		if (!(toDir.charAt(toDir.length() - 1) == '/'))
			toDir += "/";
		if (!(fromDir.charAt(fromDir.length() - 1) == '/'))
			fromDir += "/";

		String org_path = fromDir + zipName;

		extractZipFiles(org_path, toDir);
	}

	/**
	 * 
	 * @param zip_file
	 *            zip文件名
	 * @param directory
	 *            原文件路径
	 */
	public void extractZipFiles(String zip_file, String directory) {
		byte[] data = new byte[BUFFER];
		String name, path, zipDir;
		ZipEntry entry;
		ZipInputStream zipstream;

		if (!(directory.charAt(directory.length() - 1) == '/'))
			directory += "/";

		if (zip_file.contains("/")) {
			path = zip_file;
			name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
			zipDir = directory + name + "/";

		} else {
			path = directory + zip_file;
			name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
			zipDir = directory + name + "/";
		}

		new File(zipDir).mkdir();

		try {
			zipstream = new ZipInputStream(new FileInputStream(path));

			while ((entry = zipstream.getNextEntry()) != null) {
				String buildDir = zipDir;
				String[] dirs = entry.getName().split("/");

				if (dirs != null && dirs.length > 0) {
					for (int i = 0; i < dirs.length - 1; i++) {
						buildDir += dirs[i] + "/";
						new File(buildDir).mkdir();
					}
				}

				int read = 0;
				FileOutputStream out = new FileOutputStream(zipDir
						+ entry.getName());
				while ((read = zipstream.read(data, 0, BUFFER)) != -1)
					out.write(data, 0, read);

				zipstream.closeEntry();
				out.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param path
	 */
	public void createZipFile(String path) {
		File dir = new File(path);
		String[] list = dir.list();
		String name = path.substring(path.lastIndexOf("/"), path.length());
		String _path;

		if (!dir.canRead() || !dir.canWrite())
			return;

		int len = list.length;

		if (path.charAt(path.length() - 1) != '/')
			_path = path + "/";
		else
			_path = path;

		try {
			ZipOutputStream zip_out = new ZipOutputStream(
					new BufferedOutputStream(new FileOutputStream(_path + name
							+ ".zip"), BUFFER));

			for (int i = 0; i < len; i++)
				zip_folder(new File(_path + list[i]), zip_out);

			zip_out.close();

		} catch (FileNotFoundException e) {
			Log.e("File not found", e.getMessage());

		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
		}
	}

	/**
	 * 文件更名
	 * 
	 * @param filePath
	 *            文件路径
	 * @param newName
	 *            新名称
	 * @return
	 */
	public int renameTarget(String filePath, String newName) {
		File src = new File(filePath);
		String ext = "";
		File dest;

		if (src.isFile())
			/* get file extension */
			ext = filePath.substring(filePath.lastIndexOf("."),
					filePath.length());

		if (newName.length() < 1)
			return -1;

		String temp = filePath.substring(0, filePath.lastIndexOf("/"));

		dest = new File(temp + "/" + newName + ext);
		if (src.renameTo(dest))
			return 0;
		else
			return -1;
	}

	/**
	 * 创建文件夹
	 * 
	 * @param path
	 * @param name
	 * @return
	 */
	public int createDir(String path, String name) {
		int len = path.length();

		if (len < 1 || len < 1)
			return -1;

		if (path.charAt(len - 1) != '/')
			path += "/";

		if (new File(path + name).mkdir())
			return 0;

		return -1;
	}

	/**
	 * 删除文件或文件夹 是文件夹（递归删除）
	 * 
	 * @param path
	 *            路径
	 * @return
	 */
	public int deleteTarget(String path) {
		File target = new File(path);

		if (target.exists() && target.isFile() && target.canWrite()) {
			target.delete();
			return 0;
		}

		else if (target.exists() && target.isDirectory() && target.canRead()) {
			String[] file_list = target.list();

			if (file_list != null && file_list.length == 0) {
				target.delete();
				return 0;

			} else if (file_list != null && file_list.length > 0) {

				for (int i = 0; i < file_list.length; i++) {
					File temp_f = new File(target.getAbsolutePath() + "/"
							+ file_list[i]);

					if (temp_f.isDirectory())
						deleteTarget(temp_f.getAbsolutePath());
					else if (temp_f.isFile())
						temp_f.delete();
				}
			}
			if (target.exists())
				if (target.delete())
					return 0;
		}
		return -1;
	}

	/**
	 * 判断是否文件夹
	 * 
	 * @param name
	 * @return
	 */
	public boolean isDirectory(String name) {
		return new File(mPathStack.peek() + "/" + name).isDirectory();
	}

	/**
	 * 将整型数转为ip地址
	 * 
	 * @param des
	 * @return
	 */
	public static String integerToIPAddress(int ip) {
		String ascii_address = "";
		int[] num = new int[4];

		num[0] = (ip & 0xff000000) >> 24;
		num[1] = (ip & 0x00ff0000) >> 16;
		num[2] = (ip & 0x0000ff00) >> 8;
		num[3] = ip & 0x000000ff;

		ascii_address = num[0] + "." + num[1] + "." + num[2] + "." + num[3];

		return ascii_address;
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
		search_file(dir, pathName, names);

		return names;
	}

	/*
	 * 递归搜索文件
	 * 
	 * @param dir 搜索范围
	 * 
	 * @param fileName 文件名
	 * 
	 * @param path 路径List
	 */
	private void search_file(String dir, String fileName,
			ArrayList<String> pathLsit) {
		File root_dir = new File(dir);
		String[] list = root_dir.list();

		if (list != null && root_dir.canRead()) {
			int len = list.length;

			for (int i = 0; i < len; i++) {
				File check = new File(dir + "/" + list[i]);
				String name = check.getName();

				if (check.isFile()
						&& name.toLowerCase().contains(fileName.toLowerCase())) {
					pathLsit.add(check.getPath());
				} else if (check.isDirectory()) {
					if (name.toLowerCase().contains(fileName.toLowerCase()))
						pathLsit.add(check.getPath());

					else if (check.canRead() && !dir.equals("/"))
						search_file(check.getAbsolutePath(), fileName, pathLsit);
				}
			}
		}
	}

	/**
	 * 获取文件夹大小
	 * 
	 * @param path
	 * @return
	 */
	public long getDirSize(String path) {
		get_dir_size(new File(path));

		return mDirSize;
	}

	/**
	 * 字符串比较器
	 */
	private static final Comparator alph = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			return arg0.toLowerCase().compareTo(arg1.toLowerCase());
		}
	};

	/**
	 * 文件大小比较器
	 */
	private final Comparator size = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			String dir = mPathStack.peek();
			Long first = new File(dir + "/" + arg0).length();
			Long second = new File(dir + "/" + arg1).length();

			return first.compareTo(second);
		}
	};

	/**
	 * 文件类型比较 ret == 0 ，相等，同种文件
	 */
	private final Comparator type = new Comparator<String>() {
		@Override
		public int compare(String arg0, String arg1) {
			String ext = null;
			String ext2 = null;
			int ret;

			try {
				ext = arg0.substring(arg0.lastIndexOf(".") + 1, arg0.length())
						.toLowerCase();
				ext2 = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length())
						.toLowerCase();

			} catch (IndexOutOfBoundsException e) {
				return 0;
			}
			ret = ext.compareTo(ext2);

			if (ret == 0)
				return arg0.toLowerCase().compareTo(arg1.toLowerCase());

			return ret;
		}
	};

	/**
	 * 将路径栈里的
	 * 
	 * @return
	 */
	private ArrayList<String> populate_list() {

		if (!mDirContent.isEmpty())
			mDirContent.clear();

		File file = new File(mPathStack.peek());

		if (file.exists() && file.canRead()) {
			String[] list = file.list();
			int len = list.length;

			/* 按隐藏状态将文件名名加入arrayList */
			for (int i = 0; i < len; i++) {
				if (!mShowHiddenFiles) {
					if (list[i].toString().charAt(0) != '.')
						mDirContent.add(list[i]);

				} else {
					mDirContent.add(list[i]);
				}
			}

			/*
			 * arrayList排序 按大小和类型排序时，文件夹在前，文件在后
			 */
			switch (mSortType) {
			case SORT_NONE:
				// no sorting needed
				break;

			case SORT_ALPHA:
				Object[] tt = mDirContent.toArray();
				mDirContent.clear();

				Arrays.sort(tt, alph);

				for (Object a : tt) {
					mDirContent.add((String) a);
				}
				break;

			case SORT_SIZE:
				int index = 0;
				Object[] size_ar = mDirContent.toArray();
				String dir = mPathStack.peek();

				Arrays.sort(size_ar, size);

				mDirContent.clear();
				for (Object a : size_ar) {
					if (new File(dir + "/" + (String) a).isDirectory())
						mDirContent.add(index++, (String) a);
					else
						mDirContent.add((String) a);
				}
				break;

			case SORT_TYPE:
				int dirindex = 0;
				Object[] type_ar = mDirContent.toArray();
				String current = mPathStack.peek();

				Arrays.sort(type_ar, type);
				mDirContent.clear();

				for (Object a : type_ar) {
					if (new File(current + "/" + (String) a).isDirectory())
						mDirContent.add(dirindex++, (String) a);
					else
						mDirContent.add((String) a);
				}
				break;
			}

		} else {
			mDirContent.add("Emtpy");
		}

		return mDirContent;
	}

	/*
	 * zip压缩文件夹
	 * 
	 * @param file
	 * 
	 * @param zout
	 * 
	 * @throws IOException
	 */
	private void zip_folder(File file, ZipOutputStream zout) throws IOException {
		byte[] data = new byte[BUFFER];
		int read;

		if (file.isFile()) {
			ZipEntry entry = new ZipEntry(file.getName());
			zout.putNextEntry(entry);
			BufferedInputStream instream = new BufferedInputStream(
					new FileInputStream(file));

			while ((read = instream.read(data, 0, BUFFER)) != -1)
				zout.write(data, 0, read);

			zout.closeEntry();
			instream.close();

		} else if (file.isDirectory()) {
			String[] list = file.list();
			int len = list.length;

			for (int i = 0; i < len; i++)
				zip_folder(new File(file.getPath() + "/" + list[i]), zout);
		}
	}

	/*
	 * 获取文件夹大小
	 * 
	 * @param path
	 */
	private void get_dir_size(File path) {
		File[] list = path.listFiles();
		int len;

		if (list != null) {
			len = list.length;

			for (int i = 0; i < len; i++) {
				try {
					if (list[i].isFile() && list[i].canRead()) {
						mDirSize += list[i].length();

					} else if (list[i].isDirectory() && list[i].canRead()
							&& !isSymlink(list[i])) {
						get_dir_size(list[i]);
					}
				} catch (IOException e) {
					Log.e("IOException", e.getMessage());
				}
			}
		}
	}

	// Inspired by org.apache.commons.io.FileUtils.isSymlink()
	private static boolean isSymlink(File file) throws IOException {
		File fileInCanonicalDir = null;
		if (file.getParent() == null) {
			fileInCanonicalDir = file;
		} else {
			File canonicalDir = file.getParentFile().getCanonicalFile();
			fileInCanonicalDir = new File(canonicalDir, file.getName());
		}
		return !fileInCanonicalDir.getCanonicalFile().equals(
				fileInCanonicalDir.getAbsoluteFile());
	}

}
