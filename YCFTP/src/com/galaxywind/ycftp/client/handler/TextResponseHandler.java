package com.galaxywind.ycftp.client.handler;

import java.io.UnsupportedEncodingException;
import com.lidroid.xutils.util.LogUtils;


public abstract class TextResponseHandler extends ResponseHandler {

	public TextResponseHandler() {
		this(DEFAULT_CHARSET);
	}

	/**
	 * Creates new instance with given string encoding
	 * 
	 * @param encoding
	 *            String encoding, see {@link #setCharset(String)}
	 */
	public TextResponseHandler(String encoding) {
		super();
		setCharset(encoding);
	}

	public abstract void onFailure(int statusCode, Throwable throwable);

	public abstract void onSuccess(int statusCode, String responseString);

	@Override
	public void onSuccess(int statusCode, byte[] responseBytes) {
		onSuccess(statusCode, getResponseString(responseBytes, getCharset()));
	}

	@Override
	public void onFailure(int statusCode, byte[] responseBytes,
			Throwable throwable) {
		onFailure(statusCode, throwable);
	}

	/**
	 * 字节数组转字符串
	 * 
	 * @param stringBytes
	 * @param charset
	 * @return
	 */
	public static String getResponseString(byte[] stringBytes, String charset) {
		try {
			String toReturn = (stringBytes == null) ? null : new String(
					stringBytes, charset);
			if (toReturn != null && toReturn.startsWith(UTF8_BOM)) {
				return toReturn.substring(1);
			}
			return toReturn;
		} catch (UnsupportedEncodingException e) {
			LogUtils.e("Encoding response into string failed", e);
			return null;
		}
	}

}
