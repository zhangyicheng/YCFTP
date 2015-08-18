package com.galaxywind.ycftp.client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.client.handler.ResponseHandler;
import com.galaxywind.ycftp.entities.Site;
import com.galaxywind.ycftp.filemanager.FileManager;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.protocal.RequestObject;
import com.galaxywind.ycftp.utils.LogUtil;
import com.galaxywind.ycftp.utils.SerializableUtil;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.lidroid.xutils.util.LogUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RequestTask extends AsyncTask<JSONObject, Integer, JSONObject> {
	private Context context;
	private Site site;
	/* 发送数据缓冲区 */
	private static ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	/* 接受数据缓冲区 */
	private static ByteBuffer revBuffer = ByteBuffer.allocate(1024);
	private static Selector selector;
	private static SocketChannel client;
	private static FileOutputStream fout;
	private static FileChannel fileChannel;
	private ResponseHandler responseHandler;
	private JSONObject successJo; 

	public RequestTask(Context context, Site site,
			ResponseHandler responseHandler) {
		super();
		this.site = site;
		this.context = context;
		this.responseHandler = responseHandler;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(JSONObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		// ToastUtil.showToast(context, result.toString());
	}

	@Override
	protected JSONObject doInBackground(JSONObject... params) {
		// TODO Auto-generated method stub
		return sendRequest(params[0]);
	}

	private JSONObject sendRequest(JSONObject requestObject) {
		SocketChannel socketChannel = null;
		JSONObject myResponseObject = null;
		try {
			socketChannel = SocketChannel.open();
			selector = Selector.open();
			SocketAddress socketAddress = new InetSocketAddress(site.host,
					Integer.valueOf(site.port));
			socketChannel.connect(socketAddress);

			LogUtil.i("requestobj" + requestObject.toString());
			sendData(socketChannel, requestObject);

			switch (requestObject.getInt(FTPConstant.OPERATION)) {
			case FTPConstant.LOGON_OPR:
			case FTPConstant.DOWNLOAD_OPR:
			case FTPConstant.UPLOAD_OPR:
			case FTPConstant.DELETE_OPR:
				myResponseObject = receiveData(socketChannel);
				responseHandler.sendResponseMessage(myResponseObject);
				LogUtil.i("responseObj" + myResponseObject.toString());
				break;
			case FTPConstant.DOWNLOAD_FILE:
				receiveFile(socketChannel, requestObject.getString("FileName"),requestObject.getBoolean("IsFile"));
				break;
			case FTPConstant.UPLOAD_FILE:
//				socketChannel.close();
//				socketChannel = SocketChannel.open();
//				socketChannel.connect(socketAddress);
				sendFile(socketChannel, requestObject);
				break;
			}
		} catch (Exception ex) {
			responseHandler.sendFailureMessage(1, null,
					new Throwable(ex.toString()));
			LogUtil.i(ex.toString());
		} finally {
			try {
				socketChannel.close();
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return myResponseObject;
	}

	private void sendData(SocketChannel socketChannel,
			JSONObject myRequestObject) {
		byte[] bytes = SerializableUtil.toBytes(myRequestObject.toString());
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		try {
			socketChannel.write(buffer);
			socketChannel.socket().shutdownOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseHandler.sendFailureMessage(1, null,
					new Throwable(e.toString()));
		}
	}
	
	/**
	 * 发送文件
	 * @param client
	 * @throws JSONException 
	 */
	private void sendFile(SocketChannel client,JSONObject requestJo) throws JSONException {
		FileInputStream fis = null;
		FileChannel channel = null;
		boolean isFile = requestJo.getBoolean(FTPConstant.IS_FILE);
		String path = requestJo.getString(FTPConstant.FILE_PATH);
		String name = requestJo.getString(FTPConstant.FILE_NAME);
		FileManager fileManager = YCFTPApplication.getFileManager();
		try {
			if(isFile){
				fis = new FileInputStream(path);
			}else{
				fileManager.createZipFile(path);
				fis = new FileInputStream(path+"/"+name+".zip");
			}
			channel = fis.getChannel();
			int i = 1;
			int count = 0;
			while ((count = channel.read(sendBuffer)) != -1) {
				sendBuffer.flip();
				int send = client.write(sendBuffer);
				Log.d("log", "i===========" + (i++) + "   count:" + count+ " send:" + send);
				// 服务器端可能因为缓存区满，而导致数据传输失败，需要重新发送
				while (send == 0) {
					Thread.sleep(10);
					send = client.write(sendBuffer);
					Log.d("log","i重新传输====" + i + "   count:" + count+ " send:" + send);
				}
				sendBuffer.clear();
			}
			sendResponseMessage("upload "+i, FTPConstant.STATUS_CODE_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			responseHandler.sendFailureMessage(1, null,
					new Throwable(e.toString()));
			
		} finally {
			if(!isFile){
				fileManager.deleteTarget(path+"/"+name+".zip");
			}
			try {
				channel.close();
				fis.close();
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private JSONObject receiveData(SocketChannel socketChannel)
			throws JSONException {
		JSONObject myResponseObject = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			byte[] bytes;
			int count = 0;
			while ((count = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[count];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}
			bytes = baos.toByteArray();
			Object obj = SerializableUtil.toObject(bytes);
			String ggg = (String) obj;
			myResponseObject = new JSONObject((String) obj);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseHandler.sendFailureMessage(1, null,
					new Throwable(e.toString()));
		}
		return myResponseObject;
	}

	/**
	 * 接收文件
	 * 
	 * @param key
	 * @throws IOException
	 * @throws ClosedChannelException
	 * @throws FileNotFoundException
	 * @throws JSONException 
	 */
	private void receiveFile(SocketChannel client, String fileName,boolean isFile)
			throws IOException, ClosedChannelException, FileNotFoundException, JSONException {
		int count = 0;
		if(isFile){
			fout = new FileOutputStream(getRootDir() + "/" + fileName);
			LogUtils.i("path "+getRootDir() + "/" + fileName);
		}else{
			fout = new FileOutputStream(getRootDir() + "/" + fileName+".zip");
			LogUtils.i("path "+getRootDir() + "/" + fileName+".zip");
		}
		fileChannel = fout.getChannel();
		revBuffer.clear();
		int k = 0;
		// 循环读取缓存区的数据，
		while ((count = client.read(revBuffer)) > 0) {
			Log.d("read", "k=" + (k++) + " 读取到数据量:" + count);
			revBuffer.flip();
			fileChannel.write(revBuffer);
			fout.flush();
			revBuffer.clear();
		}
		if (count == -1) {
			fileChannel.close();
			fout.close();
			if(!isFile){
				FileManager fileMag = YCFTPApplication.getFileManager();
				fileMag.extractZipFiles(fileName+".zip", getRootDir()+"/");
				fileMag.deleteTarget(getRootDir() + "/" + fileName+".zip");
			}
			successJo = new JSONObject();
			if(k!=0){
				sendResponseMessage("Download ok", FTPConstant.STATUS_CODE_SUCCESS);
			}else{
				sendResponseMessage("Download failed", FTPConstant.STATUS_CODE_FAIL);
			}
		}
	}
	
	/**
	 * 获取根吗目录
	 * 
	 * @return
	 */
	private String getRootDir() {
		String rootdir = SharePreferenceUtil.getPreference(context,
				YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.ROOT_DIR);
		return StringUtil.isNotEmpty(rootdir) ? rootdir : "/";
	}
	
	private void sendResponseMessage(String response,int stattusCode) throws JSONException, IOException{
		successJo = new JSONObject();
		successJo.put("Response",response);
		successJo.put("ResponseCode", stattusCode);
		responseHandler.sendResponseMessage(successJo);
	}
}
