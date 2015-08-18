package com.galaxywind.ycftp;

import java.util.ArrayList;
import java.util.List;

import com.galaxywind.ycftp.client.FTPClient;
import com.galaxywind.ycftp.entities.Site;
import com.galaxywind.ycftp.filemanager.FileManager;
import com.galaxywind.ycftp.filemanager.RemoteFileManager;
import com.galaxywind.ycftp.filemanager.YCFile;

import android.app.Application;

public class YCFTPApplication extends Application {
	private static YCFTPApplication instance;
	//设置根目录请求Request Code
	public static final int ROOT_REQUEST_CODE = 100;
	public static final int ROOT_RESULT_CODE = 101;
	
	public static final int PORT_REQUEST_CODE = 200;
	public static final int PORT_RESULT_CODE = 201;
	
	/**
	 *spName
	 */
	public static final String SERVER_PORT_SP = "ServerPortSp";
	public static final String ROOT_DIR_SP = "RootDirSp";
	public static final String USER_NAME_SP = "User_Name_SP";
	public static final String PASSWORD_SP = "Password_SP";
	
	/**
	 * spKey
	 */
	public static final String PORT = "Port";
	public static final String ROOT_DIR = "RootDir";
	public static final String USER_NAME = "UserName";
	public static final String PASS_WORD = "PassWord"; 
	
	private FTPClient client;
	private static FileManager fileManager;
	public static Site site;

	public static List<YCFile> remoteFiles = new ArrayList<YCFile>();
	
	public static YCFTPApplication getInstance(){
		if(instance==null){
			instance = new YCFTPApplication();
		}
		return instance;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		client = new FTPClient();
	}

	public FTPClient getClient() {
		if(client == null){
			client = new FTPClient();
		}
		return client;
	}

	public void setClient(FTPClient client) {
		this.client = client;
	}

	public static FileManager getFileManager() {
		if(fileManager==null){
			fileManager = new FileManager();
		}
		return fileManager;
	}
	

	public static void setFileManager(FileManager fileManager) {
		YCFTPApplication.fileManager = fileManager;
	}
}
