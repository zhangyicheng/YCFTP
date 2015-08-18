package com.galaxywind.ycftp.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.galaxywind.ycftp.protocal.FTPConstant;

import android.content.Context;
import android.util.Log;

public class ClientHandler implements NioHandler {
	private Selector selector;
	private SocketChannel sc = null;
	private FileChannel fileChannel = null;
	private ByteBuffer buf = ByteBuffer.allocate(1024);
	private JSONObject requestJo;
	 private Context mContext;

	private long sum = 0;

	public ClientHandler(Context mContext,SocketChannel sc, Selector selector) {
		this.mContext = mContext;
		this.sc = sc;
		this.selector = selector;

		FtpNioServer.connum++;
		System.out.println(FtpNioServer.connum + " Client:"
				+ sc.socket().getRemoteSocketAddress().toString() + " open");
	}

	public void InitClientHandler(JSONObject requestJo) {
		// 参数判空
		this.requestJo = requestJo;

		try {
			SelectionKey key = null;
			//客户端上传，服务端关注于读，其他操作关注写
			if (requestJo.getInt(FTPConstant.OPERATION)==FTPConstant.UPLOAD_OPR) {
				key = this.sc.register(this.selector, SelectionKey.OP_READ);
				key.attach(this);
			} else {
				key = this.sc.register(this.selector, SelectionKey.OP_WRITE);
				key.attach(this);
			}
			
			while (true) {
				selector.select();// 返回值为本次触发的事件数
				Set<SelectionKey> selectionKeys = selector.selectedKeys();

				for (SelectionKey key1 : selectionKeys) {
					execute(key1);
				}
				Log.d("log", "=======selectionKeys.clear()");
				selectionKeys.clear();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void execute(SelectionKey key) {
		if (key.isReadable()) {
			try {
				switch (requestJo.getInt(FTPConstant.OPERATION)) {
				case FTPConstant.UPLOAD_OPR:
					proccessUpLoad(key);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			return;
		}

		if (key.isWritable()) {
			try {
				switch (requestJo.getInt(FTPConstant.OPERATION)) {
				case FTPConstant.DOWNLOAD_OPR:
					proccessDownLoad(key);
					break;
				case FTPConstant.DELETE_OPR:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			return;
		}
	}

	private void proccessUpLoad(SelectionKey key) throws JSONException {

		String filepath = requestJo.getString(FTPConstant.FILE_PATH);

		try {
			int n = sc.read(buf);
			if (n >= 0) {
				sum += n;
				WriteToFile(filepath, buf);
			} else {
				ReleaseResource(key);
				System.out.println(FtpNioServer.connum + " read sum:" + sum
						+ " Client:"
						+ sc.socket().getRemoteSocketAddress().toString()
						+ " close");
				FtpNioServer.connum--;
				return;
			}
		} catch (IOException e) {
			try {
				ReleaseResource(key);
				FtpNioServer.connum--;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("IOException " + FtpNioServer.connum
					+ " Client:"
					+ sc.socket().getRemoteSocketAddress().toString()
					+ " close");
			return;
		}
	}

	private void proccessDownLoad(SelectionKey key) throws JSONException {

		String filepath = requestJo.getString(FTPConstant.FILE_PATH);

		try {
			int n = ReadFromFile(filepath, buf);
			if (n >= 0) {
				sum += n;
				buf.flip();
				sc.write(buf);
			} else {
				ReleaseResource(key);
				System.out.println(FtpNioServer.connum + " send sum:" + sum
						+ " Client:"
						+ sc.socket().getRemoteSocketAddress().toString()
						+ " close");
				FtpNioServer.connum--;
				return;
			}
		} catch (Exception e) {
			try {
				ReleaseResource(key);
				FtpNioServer.connum--;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("IOException " + FtpNioServer.connum
					+ " Client:"
					+ sc.socket().getRemoteSocketAddress().toString()
					+ " close");
			return;
		}
	}
	
	private void proccessDelete(SelectionKey key) throws JSONException{
		String path = requestJo.getString(FTPConstant.FILE_PATH);
		File file = new File(path);
		if(file.exists()&&file.canWrite()){
			file.delete();
		}
	}

	private void proccessLsCmd(SelectionKey key) throws IOException {
		File dir = new File(FtpNioServer.ROOT);
		File files[] = dir.listFiles();

		String ret = null;
		for (File f : files) {
			if (ret == null) {
				ret = f.getName();
			} else {
				ret += ";";
				ret += f.getName();
			}
		}

		ByteBuffer src = null;
		if (ret != null) {
			src = ByteBuffer.wrap(ret.getBytes());
		} else {
			String error = "cmd execute fail!!!";
			src = ByteBuffer.wrap(error.getBytes());
		}

		// write data to client socket channel
		this.sc.write(src);

		ReleaseResource(key);

		System.out.println(FtpNioServer.connum + " Client:"
				+ sc.socket().getRemoteSocketAddress().toString() + " close");

		FtpNioServer.connum--;
	}

	private void WriteToFile(String path, ByteBuffer buf) throws IOException {

		if (fileChannel == null) {
			fileChannel = new RandomAccessFile(path, "rw").getChannel();
		}

		buf.flip();
		fileChannel.write(buf);
		buf.clear();
	}

	@SuppressWarnings("resource")
	private int ReadFromFile(String path, ByteBuffer buf) throws IOException {

		if (fileChannel == null) {
			fileChannel = new RandomAccessFile(path, "r").getChannel();
		}

		buf.clear();
		return fileChannel.read(buf);
	}

	private void ReleaseResource(SelectionKey key) throws IOException {

		sc.close();
		key.cancel();

		if (fileChannel != null) {
			fileChannel.close();
		}
	}
}
