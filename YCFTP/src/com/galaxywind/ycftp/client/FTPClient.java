package com.galaxywind.ycftp.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.galaxywind.ycftp.client.handler.ResponseHandler;
import com.galaxywind.ycftp.entities.Site;

import android.content.Context;

public class FTPClient {
	private static ExecutorService SINGLE_TASK_EXECUTOR;
	private static ExecutorService LIMITED_TASK_EXECUTOR;
	private static ExecutorService FULL_TASK_EXECUTOR;

	static {
		SINGLE_TASK_EXECUTOR = (ExecutorService) Executors
				.newSingleThreadExecutor();
		LIMITED_TASK_EXECUTOR = (ExecutorService) Executors
				.newFixedThreadPool(7);
		FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
	};
	
	public void request(Context context, Site site, JSONObject requestJo,
			ResponseHandler responseHandler){
		RequestTask requestTask = new RequestTask(context, site,responseHandler);
		requestTask.executeOnExecutor(FULL_TASK_EXECUTOR, requestJo);
	}
	
	public void request(Context context, Site site, JSONArray requestJarray,
			ResponseHandler responseHandler){
		
	}

}
