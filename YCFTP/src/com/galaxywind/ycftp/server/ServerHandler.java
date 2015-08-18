package com.galaxywind.ycftp.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.json.JSONException;
import org.json.JSONObject;

import com.galaxywind.ycftp.protocal.FTPConstant;
import com.galaxywind.ycftp.utils.LogUtil;
import com.galaxywind.ycftp.utils.NIOUtils;
import com.galaxywind.ycftp.utils.SerializableUtil;

import android.content.Context;

public class ServerHandler implements NioHandler {

	private ServerSocketChannel ssc;
	private Selector selector;
	private Context mContext;

	public ServerHandler(Context mContext, ServerSocketChannel ssc,
			Selector selector) {
		this.mContext = mContext;
		this.ssc = ssc;
		this.selector = selector;
	}

//	@Override
	public void execute1(SelectionKey key) {
		// TODO Auto-generated method stub
		try {
//			SocketChannel socketChannel = ssc.accept();
			SocketChannel socketChannel = (SocketChannel)key.channel();

			JSONObject requeObject = receiveData(socketChannel);
			LogUtil.e("server received "+requeObject.toString());

			JSONObject responseObject = FtpProtocalLogic.handleRequest(
					mContext, requeObject);
			sendData(socketChannel, responseObject);
			socketChannel.finishConnect();
			LogUtil.e("server send "+responseObject.toString());

			if (requeObject.getInt(FTPConstant.OPERATION) != FTPConstant.LOGON_OPR) {
				// 配置客户端Socket为非阻塞
				socketChannel.configureBlocking(false);

				// new 一个新的客户端对象
				ClientHandler clientHandler = new ClientHandler(mContext,
						socketChannel, selector);
				clientHandler.InitClientHandler(requeObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void execute(SelectionKey key){
		// TODO Auto-generated method stub
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = null;

		try {
			socketChannel = serverSocketChannel.accept();

			JSONObject requeObject = receiveData(socketChannel);
			LogUtil.e(requeObject.toString());

			JSONObject responseObject = FtpProtocalLogic.handleRequest(
					mContext, requeObject);
			sendData(socketChannel, responseObject);
			LogUtil.e(responseObject.toString());

			if (requeObject.getInt(FTPConstant.OPERATION) != FTPConstant.LOGON_OPR) {
				// 配置客户端Socket为非阻塞
				socketChannel.configureBlocking(false);

				// new 一个新的客户端对象
				ClientHandler clientHandler = new ClientHandler(mContext,
						socketChannel, selector);
				clientHandler.InitClientHandler(requeObject);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				socketChannel.close();
			} catch (Exception ex) {
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
}
