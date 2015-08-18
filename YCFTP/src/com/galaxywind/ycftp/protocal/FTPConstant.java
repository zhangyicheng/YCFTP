package com.galaxywind.ycftp.protocal;

public class FTPConstant {
	public static final int LOGON_OPR = 1;
	public static final int DOWNLOAD_OPR = 2;
	public static final int UPLOAD_OPR = 3;
	public static final int DELETE_OPR = 4;
	
	public static final int DOWNLOAD_FILE = 5;
	public static final int UPLOAD_FILE = 6;
	
	public static final String OPERATION = "operation";
	
	public static final int STATUS_CODE_FAIL = 0;
	public static final int STATUS_CODE_SUCCESS = 1;
	public static final int STATUS_CODE_ERROR = 2;
	
	public static final String FILE_PATH = "FilePath";
	public static final String FILE_NAME = "FileName";
	public static final String IS_FILE = "IsFile";
	
	//数据包的包头结构“F0F1F2F3”+“XXXXXXXX”+“F3F2F1F0” 中间8位表示json数据的长度
	public static final String HEADER_START = "F0F1F2F3";
	public static final String HEADER_END = "F3F2F1F0"; 
	
	
}
