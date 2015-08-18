package com.galaxywind.ycftp.client;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.client.handler.ResponseHandler;
import com.galaxywind.ycftp.entities.Site;

public class FTPClientHelper {

	public static FTPClient getClient(){
		return YCFTPApplication.getInstance().getClient();
	}
	
	public static void request(Context context, Site site, JSONObject requestJo,
			ResponseHandler responseHandler){
		getClient().request(context, site, requestJo, responseHandler);
	}
	
	public static void request(Context context, Site site, JSONArray requestJarray,
			ResponseHandler responseHandler){
		getClient().request(context, site, requestJarray, responseHandler);
	}
}
