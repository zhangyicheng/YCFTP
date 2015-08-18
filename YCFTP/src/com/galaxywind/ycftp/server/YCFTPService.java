package com.galaxywind.ycftp.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
import com.galaxywind.ycftp.filemanager.FileManager;
import com.galaxywind.ycftp.filemanager.YCFile;
import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.protocal.RequestObject;
import com.galaxywind.ycftp.protocal.ResponseObject;
import com.galaxywind.ycftp.utils.LogUtil;
import com.galaxywind.ycftp.utils.SerializableUtil;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.utils.ToastUtil;
import com.lidroid.xutils.util.LogUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/***
 * YCFTP通道服务
 * 
 * @author zyc
 */
public class YCFTPService extends Service {
	ServerSocketChannel serverSocketChannel;
	Message msg;
	public static String ipAddress;
	public static int port;
	// 服务开关标识
	public static boolean isServerRunning = false;
	//接受数据缓冲区
	private static ByteBuffer revBuffer = ByteBuffer.allocate(1024);
	//发送数据缓冲区
	private static ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private static Selector selector;
	private static FileOutputStream fout;
	private static FileChannel fileChannel;
	private JSONObject uploadJo;

	// UI刷新Handler
	Handler uiHanlder = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				ToastUtil.showShortToast(getApplicationContext(), "客户端进入！");
				break;
			case 2:
				ToastUtil.showShortToast(getApplicationContext(), "readable！");
				break;
			case 3:
				ToastUtil.showShortToast(getApplicationContext(), "writable！");
				break;
			case 4:
				ToastUtil.showShortToast(getApplicationContext(), "FTP服务启动!");
				break;
			case 5:
				ToastUtil.showToast(getApplicationContext(),
						((JSONObject) msg.obj).toString());
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		getPort();
		try {
			initFTPService();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					receiveRequest();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		ToastUtil.showToast(getApplicationContext(), "Service Destroy");
		super.onDestroy();
		try {
			if (serverSocketChannel != null) {
				serverSocketChannel.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取端口
	 */
	private void getPort() {
		String portStr = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.SERVER_PORT_SP,
				YCFTPApplication.PORT);
		if (StringUtil.isNotEmpty(portStr)) {
			port = Integer.valueOf(portStr);
		} else {
			port = 2121;
		}
	}

	/**
	 * 获取根吗目录
	 * 
	 * @return
	 */
	private String getRootDir() {
		String rootdir = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.ROOT_DIR_SP,
				YCFTPApplication.ROOT_DIR);
		return StringUtil.isNotEmpty(rootdir) ? rootdir : "/";
	}

	/**
	 * FTP service初始化
	 * 
	 * @throws Exception
	 */
	private void initFTPService() throws Exception {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));

		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		msg = Message.obtain();
		msg.what = 4;
		uiHanlder.sendMessage(msg);
		Log.d("server start on port:", port + "");
	}

	private void receiveRequest() throws IOException, JSONException {
		while (true) {
			selector.select();// 返回值为本次触发的事件数
			Set<SelectionKey> selectionKeys = selector.selectedKeys();

			for (SelectionKey key : selectionKeys) {
				receiveFTPRequest(key);
			}
			Log.d("log", "=======selectionKeys.clear()");
			selectionKeys.clear();
		}
	}
	
	/** 
     * 根据byte数组，生成文件 
	 * @throws FileNotFoundException 
     */  
    public static void getFile(byte[] bytes, String filePath,String fileName,boolean isFile){
    	RandomAccessFile aFile = null;
		try {
			aFile = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	FileChannel fileChannel = aFile.getChannel();
    	
    	ByteBuffer buf = ByteBuffer.allocate(48);
    	buf.clear();
    	buf.put(bytes);
    	buf.flip();
    	while(buf.hasRemaining()) {
    		try {
				fileChannel.write(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }  
	
	/**
	 * 接收文件
	 * @param key
	 * @param fileName 文件名（带后缀为文件，不带后缀为文件夹）
	 * @throws IOException
	 * @throws ClosedChannelException
	 * @throws FileNotFoundException
	 */
	private void receiveFile(SelectionKey key,String fileName) throws IOException,
			ClosedChannelException, FileNotFoundException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		int count = 0;
		if (key.isAcceptable()) {
			server = (ServerSocketChannel) key.channel();
			sendMsg(1, null);
			Log.d("client in", "有客户端连接进入=============");
			client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
			fout = new FileOutputStream(getRootDir() + "/" + fileName);
			fileChannel = fout.getChannel();
		} else if (key.isReadable()) {
			sendMsg(2, null);
			client = (SocketChannel) key.channel();
			revBuffer.clear();
			count = client.read(revBuffer);
			int k = 0;
			// 循环读取缓存区的数据，
			while (count > 0) {
				Log.d("read", "k=" + (k++) + " 读取到数据量:" + count);
				revBuffer.flip();
				fileChannel.write(revBuffer);
				fout.flush();
				revBuffer.clear();
				count = client.read(revBuffer);
			}
			if (count == -1) {
				client.close();
				fileChannel.close();
				fout.close();
			}
		} else if (key.isWritable()) {
			sendMsg(3, null);
			Log.d("log", "selectionKey.isWritable()");
		}
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
		}
	}

	private void receiveData(SelectionKey key) throws IOException,
			ClosedChannelException, FileNotFoundException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		byte[] bytes;
		ByteArrayOutputStream baos = null;
		int count = 0;
		if (key.isAcceptable()) {
			server = (ServerSocketChannel) key.channel();
			sendMsg(1, null);
			Log.d("client in", "有客户端连接进入=============");
			client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
		} else if (key.isReadable()) {
			baos = new ByteArrayOutputStream();
			sendMsg(2, null);
			client = (SocketChannel) key.channel();
			revBuffer.clear();
			count = client.read(revBuffer);
			int k = 0;
			// 循环读取缓存区的数据，
			while (count > 0) {
				Log.d("read", "k=" + (k++) + " 读取到数据量:" + count);
				revBuffer.flip();
				bytes = new byte[count];
				revBuffer.get(bytes);
				baos.write(bytes);
				revBuffer.clear();
				count = client.read(revBuffer);
			}
			LogUtils.i("Server Read:" + new String(baos.toByteArray()));
			Object obj = SerializableUtil.toObject(baos.toByteArray());
			RequestObject requestObject = (RequestObject) obj;
			sendMsg(5, requestObject);

			if (count == -1) {
				client.close();
				baos.close();
			}
		} else if (key.isWritable()) {
			sendMsg(3, null);
			client = (SocketChannel) key.channel();
			ResponseObject myResponseObject = new ResponseObject(
					"Server Response", "Haha");
			// sendData(client, myResponseObject);
			Log.d("log", "selectionKey.isWritable()");
		}
	}

	private void sendMsg(int what, Object obj) {
		msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		uiHanlder.sendMessage(msg);
	}

	/**
	 * 接收ftp协议请求
	 * @param serverSocketChannel
	 * @throws IOException
	 */
	private void receiveFTPRequest(SelectionKey key)
			throws IOException, JSONException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = null;
		JSONObject requestObject = null;
		try {
			socketChannel = serverSocketChannel.accept();
			if(uploadJo==null){
				requestObject = receiveData(socketChannel);
				LogUtil.e(requestObject.toString());
			}else{
				receiveFile(socketChannel, uploadJo.getString(FTPConstant.FILE_NAME), uploadJo.getBoolean(FTPConstant.IS_FILE));
			}
			
			JSONObject responseObject;
			if(requestObject!=null){
				switch (requestObject.getInt(FTPConstant.OPERATION)) {
				case FTPConstant.LOGON_OPR:
				case FTPConstant.DOWNLOAD_OPR:
				case FTPConstant.DELETE_OPR:
					responseObject = FtpProtocalLogic.handleRequest(
							getApplicationContext(), requestObject);
					sendData(socketChannel, responseObject);
					LogUtil.e(responseObject.toString());
					uploadJo = null;
					break;
				case FTPConstant.DOWNLOAD_FILE:
					sendFile(socketChannel, requestObject);
					uploadJo = null;
					break;
				case FTPConstant.UPLOAD_OPR:
					responseObject = FtpProtocalLogic.handleRequest(
							getApplicationContext(), requestObject);
					sendData(socketChannel, responseObject);
					LogUtil.e(responseObject.toString());
					uploadJo = requestObject;
					break;
				}
			}else{
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(socketChannel.isOpen()){
					socketChannel.close();
				}
			} catch (Exception ex) {
				LogUtils.e(ex.toString());
			}
		}
	}
	/**
	 * 接收ftp协议请求
	 * @param serverSocketChannel
	 * @throws IOException
	 */
	private void receiveFTPRequest1(SelectionKey key)
			throws IOException, JSONException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = null;
		
		try {
			socketChannel = serverSocketChannel.accept();
			
			byte[] bytes = receiveBytes(socketChannel);
			JSONObject jsonObject = SerializableUtil.toJSONObject(bytes);
			
			if(jsonObject.getString("SerializableError")!=null){
				if(uploadJo!=null){
					getFileFromBytes(bytes, uploadJo.getString(getRootDir()+"/"+FTPConstant.FILE_NAME));
				}else{
					LogUtils.i("Lala");
				}
			}else{
				LogUtil.e(jsonObject.toString());
				switch (jsonObject.getInt(FTPConstant.OPERATION)) {
				case FTPConstant.LOGON_OPR:
				case FTPConstant.DOWNLOAD_OPR:
				case FTPConstant.DELETE_OPR:
					JSONObject responseObject = FtpProtocalLogic.handleRequest(
							getApplicationContext(), jsonObject);
					sendData(socketChannel, responseObject);
					LogUtil.e(responseObject.toString());
					break;
				case FTPConstant.DOWNLOAD_FILE:
					sendFile(socketChannel, jsonObject);
					break;
				case FTPConstant.UPLOAD_OPR:
					uploadJo = jsonObject;
					break;
				case FTPConstant.UPLOAD_FILE:
					receiveFile(key, jsonObject.getString(FTPConstant.FILE_NAME));
					break;
				}
			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(socketChannel.isOpen()){
					socketChannel.close();
				}
			} catch (Exception ex) {
				LogUtils.e(ex.toString());
			}
		}
	}

	/**
	 * 接收数据
	 * 
	 * @param socketChannel
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	private JSONObject receiveData(SocketChannel socketChannel)
			throws JSONException {
		JSONObject myRequestObject = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			byte[] bytes;
			int size = 0;
			while ((size = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[size];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}
			bytes = baos.toByteArray();
			Object obj = SerializableUtil.toObject(bytes);
			myRequestObject = new JSONObject((String) obj);
			sendMsg(5, myRequestObject);
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
	
	private byte[] receiveBytes(SocketChannel socketChannel){

		ByteArrayOutputStream baos = null;
		byte[] bytes = null;
		try {
			baos = new ByteArrayOutputStream();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int size = 0;
			while ((size = socketChannel.read(buffer)) >= 0) {
				buffer.flip();
				bytes = new byte[size];
				buffer.get(bytes);
				baos.write(bytes);
				buffer.clear();
			}
			bytes = baos.toByteArray();
			
			byte[] header = new byte[4];
			System.arraycopy(bytes, 0, header, 0, header.length);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (Exception ex) {
			}
		}
		return bytes;
	}
	
	/**
	 * 把字节数组保存为一个文件
	 * 
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File getFileFromBytes(byte[] b, String outputFile) {
		File ret = null;
		BufferedOutputStream stream = null;
		try {
			ret = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(ret);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			// log.error("helper:get file from byte process error!");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// log.error("helper:get file from byte process error!");
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	
	/**
	 * 发送数据
	 * 
	 * @param socketChannel
	 * @param myResponseObject
	 * @throws IOException
	 */
	private void sendData(SocketChannel socketChannel,
			JSONObject myResponseObject) throws IOException {
		byte[] bytes = SerializableUtil.toBytes(myResponseObject.toString());
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		socketChannel.write(buffer);
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
		} catch (Exception e) {
			e.printStackTrace();
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
}
