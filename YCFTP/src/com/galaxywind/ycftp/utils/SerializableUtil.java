package com.galaxywind.ycftp.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 把java对象序列化成字节数组，或者把字节数组反序列化成java对象
 * @author zyc
 *
 */
public class SerializableUtil {
	
	/**
	 * 把java对象序列化成字节数组
	 * @param object
	 * @return
	 */
	public static byte[] toBytes(Object object) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			try {
				oos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 把字节数组反序列化成java对象
	 * @param bytes
	 * @return
	 */
	public static Object toObject(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			Object object = ois.readObject();
			return object;
		} catch(IOException ex) {
			return ex;
		} catch(ClassNotFoundException ex) {
			return ex;
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
				return e;
			}
		}
	}
	/**
	 * 把字节数组反序列化成json对象
	 * @param bytes
	 * @return
	 * @throws JSONException 
	 */
	public static JSONObject toJSONObject(byte[] bytes) throws JSONException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			Object object = ois.readObject();
			return (JSONObject)object;
		} catch(Exception ex) {
			JSONObject jo = new JSONObject();
			jo.put("SerializableError", "error");
			return jo;
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
				
			}
		}
	}
}