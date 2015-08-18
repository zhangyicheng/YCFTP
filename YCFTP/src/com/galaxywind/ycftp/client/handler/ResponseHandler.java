package com.galaxywind.ycftp.client.handler;

import java.io.IOException;
import java.net.URI;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.galaxywind.ycftp.protocal.ResponseObject;
import com.galaxywind.ycftp.utils.AssertsUtils;
import com.lidroid.xutils.util.LogUtils;

public abstract class ResponseHandler implements ResponseHandlerInterface {

	protected static final int SUCCESS_MESSAGE = 0;
	protected static final int FAILURE_MESSAGE = 1;
	protected static final int START_MESSAGE = 2;
	protected static final int FINISH_MESSAGE = 3;
	protected static final int PROGRESS_MESSAGE = 4;
	protected static final int RETRY_MESSAGE = 5;
	protected static final int CANCEL_MESSAGE = 6;

	protected static final int BUFFER_SIZE = 4096;

	public static final String DEFAULT_CHARSET = "UTF-8";
	public static final String UTF8_BOM = "\uFEFF";
	private String responseCharset = DEFAULT_CHARSET;
	private Handler handler;
	private Looper looper = null;
	private boolean useSynchronousMode;

	public ResponseHandler() {
		this(null);
	}

	public ResponseHandler(Looper looper) {
		this.looper = looper == null ? Looper.myLooper() : looper;
		setUseSynchronousMode(false);
	}

	/**
	 * Avoid leaks by using a non-anonymous handler class.
	 */
	private static class ResponderHandler extends Handler {
		private final ResponseHandler mResponder;

		ResponderHandler(ResponseHandler mResponder, Looper looper) {
			super(looper);
			this.mResponder = mResponder;
		}

		@Override
		public void handleMessage(Message msg) {
			mResponder.handleMessage(msg);
		}
	}

	public void onStart() {
		// default log warning is not necessary, because this method is just
		// optional notification
	}

	public void onProgress(int bytesWritten, int totalSize) {
		LogUtils.v(String.format("Progress %d from %d (%2.0f%%)", bytesWritten,
				totalSize,
				(totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1));
	}

	public void onFinish() {
		// default log warning is not necessary, because this method is just
		// optional notification
	}

	public abstract void onSuccess(int statusCode, byte[] responseBody);

	public abstract void onFailure(int statusCode, byte[] responseBody,
			Throwable error);

	public void onRetry(int retryNo) {
		LogUtils.i(String.format("Request retry no. %d", retryNo));
	}

	public void onCancel() {
		LogUtils.i("Request got cancelled");
	}

	protected void handleMessage(Message message) {
		Object[] response;

		switch (message.what) {
		case SUCCESS_MESSAGE:
			response = (Object[]) message.obj;
			if (response != null && response.length >= 2) {
				onSuccess((Integer) response[0], (byte[]) response[1]);
			} else {
				LogUtils.e("SUCCESS_MESSAGE didn't got enough params");
			}
			break;
		case FAILURE_MESSAGE:
			response = (Object[]) message.obj;
			if (response != null && response.length >= 3) {
				onFailure((Integer) response[0], (byte[]) response[1],
						(Throwable) response[2]);
			} else {
				LogUtils.e("FAILURE_MESSAGE didn't got enough params");
			}
			break;
		case START_MESSAGE:
			onStart();
			break;
		case FINISH_MESSAGE:
			onFinish();
			break;
		case PROGRESS_MESSAGE:
			response = (Object[]) message.obj;
			if (response != null && response.length >= 2) {
				try {
					onProgress((Integer) response[0], (Integer) response[1]);
				} catch (Throwable t) {
					LogUtils.e("custom onProgress contains an error", t);
				}
			} else {
				LogUtils.e("PROGRESS_MESSAGE didn't got enough params");
			}
			break;
		case RETRY_MESSAGE:
			response = (Object[]) message.obj;
			if (response != null && response.length == 1) {
				onRetry((Integer) response[0]);
			} else {
				LogUtils.e("RETRY_MESSAGE didn't get enough params");
			}
			break;
		case CANCEL_MESSAGE:
			onCancel();
			break;
		}
	}

	protected void sendMessage(Message msg) {
		if (getUseSynchronousMode() || handler == null) {
			handleMessage(msg);
		} else if (!Thread.currentThread().isInterrupted()) { // do not send
																// messages if
																// request has
																// been
																// cancelled
			AssertsUtils
					.asserts(handler != null, "handler should not be null!");
			handler.sendMessage(msg);
		}
	}

	protected void postRunnable(Runnable runnable) {
		if (runnable != null) {
			if (getUseSynchronousMode() || handler == null) {
				// This response handler is synchronous, run on current thread
				runnable.run();
			} else {
				// Otherwise, run on provided handler
				AssertsUtils.asserts(handler != null,
						"handler should not be null!");
				handler.post(runnable);
			}
		}
	}

	@Override
	final public void sendProgressMessage(int bytesWritten, int bytesTotal) {
		sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] {
				bytesWritten, bytesTotal }));
	}

	@Override
	final public void sendSuccessMessage(int statusCode, byte[] responseBytes) {
		sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { statusCode,
				responseBytes }));
	}

	@Override
	final public void sendFailureMessage(int statusCode, byte[] responseBody,
			Throwable throwable) {
		sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { statusCode,
				responseBody, throwable }));
	}

	@Override
	final public void sendStartMessage() {
		sendMessage(obtainMessage(START_MESSAGE, null));
	}

	@Override
	final public void sendFinishMessage() {
		sendMessage(obtainMessage(FINISH_MESSAGE, null));
	}

	@Override
	final public void sendRetryMessage(int retryNo) {
		sendMessage(obtainMessage(RETRY_MESSAGE, new Object[] { retryNo }));
	}

	@Override
	final public void sendCancelMessage() {
		sendMessage(obtainMessage(CANCEL_MESSAGE, null));
	}

	@Override
	public void sendResponseMessage(JSONObject response) throws IOException {
		// do not process if request has been cancelled
		if (!Thread.currentThread().isInterrupted()) {
			if (response == null) {
				sendFailureMessage(0, null, new Throwable("No response"));
			} else {
				sendSuccessMessage(1, response.toString().getBytes());
			}
		}
	}

	@Override
	public URI getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRequestURI(URI requestURI) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreProcessResponse(ResponseHandlerInterface instance,
			ResponseObject response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPostProcessResponse(ResponseHandlerInterface instance,
			ResponseObject response) {
		// TODO Auto-generated method stub

	}

	protected Message obtainMessage(int responseMessageId,
			Object responseMessageData) {
		return Message.obtain(handler, responseMessageId, responseMessageData);
	}

	@Override
	public boolean getUseSynchronousMode() {
		return useSynchronousMode;
	}

	@Override
	public void setUseSynchronousMode(boolean sync) {
		// A looper must be prepared before setting asynchronous mode.
		if (!sync && looper == null) {
			sync = true;
			LogUtils.w("Current thread has not called Looper.prepare(). Forcing synchronous mode.");
		}

		// If using asynchronous mode.
		if (!sync && handler == null) {
			// Create a handler on current thread to submit tasks
			handler = new ResponderHandler(this, looper);
		} else if (sync && handler != null) {
			// TODO: Consider adding a flag to remove all queued messages.
			handler = null;
		}

		useSynchronousMode = sync;
	}

	public void setCharset(final String charset) {
		this.responseCharset = charset;
	}

	public String getCharset() {
		return this.responseCharset == null ? DEFAULT_CHARSET
				: this.responseCharset;
	}

}
