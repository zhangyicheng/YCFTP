package com.galaxywind.ycftp.client.handler;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.lidroid.xutils.util.LogUtils;

public abstract class JsonResponsehandler extends TextResponseHandler {

	public JsonResponsehandler() {
		super(DEFAULT_CHARSET);
	}

	public JsonResponsehandler(String encoding) {
		super(encoding);
	}

	public abstract void onSuccess(int statusCode, JSONObject responseJo);

	public abstract void onFailure(int statusCode, Throwable throwable);

	@Override
	public void onSuccess(int statusCode, String responseString) {
		// TODO Auto-generated method stub

	}

	@Override
	public final void onSuccess(final int statusCode, final byte[] responseBytes) {
		Runnable parser = new Runnable() {
			@Override
			public void run() {
				try {
					final Object jsonResponse = parseResponse(responseBytes);
					postRunnable(new Runnable() {
						@Override
						public void run() {
							if (jsonResponse instanceof JSONObject) {
								onSuccess(statusCode, (JSONObject) jsonResponse);
							} else if (jsonResponse instanceof String) {
								onFailure(
										statusCode,
										new JSONException(
												"Response cannot be parsed as JSON data"));
							} else {
								onFailure(statusCode, new JSONException(
										"Unexpected response type "
												+ jsonResponse.getClass()
														.getName()));
							}

						}
					});
				} catch (final JSONException ex) {
					postRunnable(new Runnable() {
						@Override
						public void run() {
							onFailure(statusCode, ex);
						}
					});
				}
			}
		};
		if (!getUseSynchronousMode()) {
			new Thread(parser).start();
		} else {
			// In synchronous mode everything should be run on one thread
			parser.run();
		}
	}

	@Override
	public final void onFailure(final int statusCode,
			final byte[] responseBytes, final Throwable throwable) {
		if (responseBytes != null) {
			Runnable parser = new Runnable() {
				@Override
				public void run() {
					try {
						final Object jsonResponse = parseResponse(responseBytes);
						postRunnable(new Runnable() {
							@Override
							public void run() {
								if (jsonResponse instanceof String) {
									onFailure(statusCode, throwable);
								} else {
									onFailure(statusCode, new JSONException(
											"Unexpected response type "
													+ jsonResponse.getClass()
															.getName()));
								}
							}
						});

					} catch (final JSONException ex) {
						postRunnable(new Runnable() {
							@Override
							public void run() {
								onFailure(statusCode, ex);
							}
						});

					}
				}
			};
			if (!getUseSynchronousMode()) {
				new Thread(parser).start();
			} else {
				// In synchronous mode everything should be run on one thread
				parser.run();
			}
		} else {
			LogUtils.v("response body is null, calling onFailure(Throwable, JSONObject)");
			onFailure(statusCode, throwable);
		}
	}

	/**
	 * 字节数组转json
	 * 
	 * @param responseBody
	 * @return
	 * @throws JSONException
	 */
	protected Object parseResponse(byte[] responseBody) throws JSONException {
		if (null == responseBody)
			return null;
		Object result = null;
		// trim the string to prevent start with blank, and test if the string
		// is valid JSON, because the parser don't do this :(. If JSON is not
		// valid this will return null
		String jsonString = getResponseString(responseBody, getCharset());
		if (jsonString != null) {
			jsonString = jsonString.trim();
			if (jsonString.startsWith(UTF8_BOM)) {
				jsonString = jsonString.substring(1);
			}
			if (jsonString.startsWith("{") || jsonString.startsWith("[")) {
				result = new JSONTokener(jsonString).nextValue();
			}
		}
		if (result == null) {
			result = jsonString;
		}
		return result;
	}

}
