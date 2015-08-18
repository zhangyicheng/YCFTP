package com.galaxywind.ycftp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreferenceUtil {

	
	//存
	public static void savePreference(Context context,String spName,String spKey,String spValue){
		Editor spEditor = context.getSharedPreferences(spName, 0).edit();
		spEditor.putString(spKey, spValue);
		spEditor.commit();
	}
	
	//取
	public static String getPreference(Context context,String spName,String spKey){
		SharedPreferences sp = context.getSharedPreferences(spName, 0);
		String value = "";
		if(sp!=null){
			value = sp.getString(spKey, "");
		}
		return value;
	}
	
	//删
	public void removePreference(Context context,String spName,String spKey){
		SharedPreferences sp = context.getSharedPreferences(spName, 0);
		if(sp!=null){
			sp.edit().remove(spKey).commit();
		}
	}
}
