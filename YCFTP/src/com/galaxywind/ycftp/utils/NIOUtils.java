package com.galaxywind.ycftp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

public class NIOUtils {
	public static String ByteBufferToString(ByteBuffer byteBuffer) {
		String result = null;
		if (byteBuffer != null) {
			byteBuffer.flip();
			byte[] tempb = new byte[byteBuffer.limit()];
			byteBuffer.get(tempb);
			result = new String(tempb);
		}
		return result;
	}

	public static ByteBuffer StringToByteBuffer(String s) {
		ByteBuffer other = null;
		if (s != null) {
			other = ByteBuffer.wrap(s.getBytes());
		}
		return other;
	}

	public static JSONObject ByteBufferToJsonObject(ByteBuffer byteBuffer)
			throws JSONException {
		JSONObject myRequestObject = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byteBuffer.flip();
			byte[] bytes = new byte[byteBuffer.limit()];
			baos.write(bytes);
			bytes = baos.toByteArray();
			Object obj = SerializableUtil.toObject(bytes);
			myRequestObject = new JSONObject((String) obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (Exception ex) {
			}
		}
		return myRequestObject;
	}

	public static ByteBuffer JsonObjectToByteBuffer(JSONObject jo) {
		byte[] bytes = SerializableUtil.toBytes(jo.toString());
		return ByteBuffer.wrap(bytes);
	}
}
