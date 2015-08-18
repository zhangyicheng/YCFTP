package com.galaxywind.ycftp.server;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;

/**
 * 服务端逻辑处理
 * 
 * @author zyc
 * 
 */
public class FtpProtocalLogic {

	public static JSONObject handleRequest(Context context, JSONObject requestJo)
			throws JSONException {
		JSONObject responseJo = new JSONObject();
		if (requestJo == null) {
			try {
				responseJo.put("Response", null);
				responseJo.put("ResponseCode", FTPConstant.STATUS_CODE_ERROR);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			switch (requestJo.getInt(FTPConstant.OPERATION)) {
			case FTPConstant.LOGON_OPR:
				String userName = requestJo.getString("username");
				String passWord = requestJo.getString("password");
				if (checkUser(context, userName, passWord)) {
					JSONObject filesJo = new JSONObject();
					filesJo.put("Files", getFilesListJa(context));
					responseJo.put("Response", filesJo);
					responseJo.put("ResponseCode",
							FTPConstant.STATUS_CODE_SUCCESS);
				} else {
					responseJo.put("Response",
							"Login failed!Login info incorrect!");
					responseJo
							.put("ResponseCode", FTPConstant.STATUS_CODE_FAIL);
				}
				return responseJo;
			case FTPConstant.DOWNLOAD_OPR:
				File file = new File(requestJo.getString("FilePath"));
				if(file.exists()){
					responseJo.put("Response", "available");
					responseJo.put("ResponseCode",
							FTPConstant.STATUS_CODE_SUCCESS);
				}else{
					responseJo.put("Response", "file not exists");
					responseJo.put("ResponseCode",
							FTPConstant.STATUS_CODE_ERROR);
				}
				break;
			case FTPConstant.UPLOAD_OPR:
				responseJo.put("Response", "uploadable");
				responseJo.put("ResponseCode",
						FTPConstant.STATUS_CODE_SUCCESS);
				break;
			case FTPConstant.DELETE_OPR:
				File fileToDelete = new File(requestJo.getString("FilePath"));
				if(fileToDelete.exists()){
					if(fileToDelete.canWrite()){
						fileToDelete.delete();
						responseJo.put("Response", requestJo.getString("FileName")+" was deleted");
						responseJo.put("ResponseCode",
								FTPConstant.STATUS_CODE_SUCCESS);
					}else{
						responseJo.put("Response", requestJo.getString("FileName")+" is not writable");
						responseJo.put("ResponseCode",
								FTPConstant.STATUS_CODE_FAIL);
					}
				}else{
					responseJo.put("Response", requestJo.getString("FileName")+" is not exists");
					responseJo.put("ResponseCode",
							FTPConstant.STATUS_CODE_FAIL);
				}
				break;
			}
		}
		return responseJo;
	}

	private static String getFilesList(Context context) {
		String rootPath = SharePreferenceUtil.getPreference(context,
				YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.ROOT_DIR);
		return YCFTPApplication.getFileManager().getPaths(
				rootPath, false);
	}
	
	private static JSONArray getFilesListJa(Context context) throws JSONException {
		String rootPath = SharePreferenceUtil.getPreference(context,
				YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.ROOT_DIR);
		return YCFTPApplication.getFileManager().getPathsJArray(
				rootPath, false);
	}

	/**
	 * 登陆校验
	 * 
	 * @param context
	 * @param userName
	 * @param passWord
	 * @return
	 */
	private static boolean checkUser(Context context, String userName,
			String passWord) {
		if (userName == null) {
			userName = "";
		}
		if (passWord == null) {
			passWord = "";
		}
		String user = SharePreferenceUtil.getPreference(context,
				YCFTPApplication.USER_NAME_SP, YCFTPApplication.USER_NAME);
		String pwd = SharePreferenceUtil.getPreference(context,
				YCFTPApplication.PASSWORD_SP, YCFTPApplication.PASS_WORD);
		return userName.equals(user) && passWord.equals(pwd);
	}
}
