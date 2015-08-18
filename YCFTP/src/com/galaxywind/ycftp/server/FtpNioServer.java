package com.galaxywind.ycftp.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FtpNioServer extends Service {

	public static String ipAddress;
	public static int PORT;
	public static int connum = 0;
	public static final int MAX = 5000;
	public static String ROOT;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		getPort();
		getRootDir();
		
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				initServer();
			}
		}.start();
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	/**
	 * 获取端口
	 */
	private void getPort() {
		String portStr = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.SERVER_PORT_SP,
				YCFTPApplication.PORT);
		if (StringUtil.isNotEmpty(portStr)) {
			PORT = Integer.valueOf(portStr);
		} else {
			PORT = 2121;
		}
	}
	
	/**
	 * 获取根吗目录
	 * @return
	 */
	private String getRootDir() {
		String rootdir = SharePreferenceUtil.getPreference(
				getApplicationContext(), YCFTPApplication.ROOT_DIR_SP,
				YCFTPApplication.ROOT_DIR);
		ROOT = StringUtil.isNotEmpty(rootdir) ? rootdir : "/";
		return ROOT;
	}

	private void initServer() {
		try {
			Selector selector = Selector.open();
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);

			ServerSocket ss = ssc.socket();
			ss.bind(new InetSocketAddress(PORT));

			SelectionKey skey = ssc.register(selector, SelectionKey.OP_ACCEPT);
			skey.attach(new ServerHandler(getApplicationContext(),ssc, selector));

			System.out.println("Start ftp server on " + PORT);
			
			while (!Thread.interrupted()) {
				int n = selector.select();
				if (n == 0){
					continue;
				}
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();

//					ServerHandler handler = (ServerHandler) key.attachment();
					NioHandler handler = (NioHandler) key.attachment();
					handler.execute(key);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
