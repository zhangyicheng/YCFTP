package com.galaxywind.ycftp.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import com.galaxywind.ycftp.BaseFragment;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.MainActivity.MyAdapter;
import com.galaxywind.ycftp.protocal.RequestObject;
import com.galaxywind.ycftp.protocal.ResponseObject;
import com.galaxywind.ycftp.server.ServerFragment;
import com.galaxywind.ycftp.ui.navigation.PagerSlidingTabStrip;
import com.galaxywind.ycftp.ui.navigation.YCViewPager;
import com.galaxywind.ycftp.utils.LogUtil;
import com.galaxywind.ycftp.utils.SerializableUtil;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

public class ClientFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.client_layout, null);
		initViews();
		 for (int i = 0; i < 10; i++) {
		 final int idx = i;
		 new Thread(new MyRunnable(idx)).start();
		 }
		return fragmentView;
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();

	}

	private static final class MyRunnable implements Runnable {

		private final int idx;

		private MyRunnable(int idx) {
			this.idx = idx;
		}

		public void run() {
			SocketChannel socketChannel = null;
			try {
				socketChannel = SocketChannel.open();
				SocketAddress socketAddress = new InetSocketAddress(
						"172.29.29.2", 2121);
				socketChannel.connect(socketAddress);

				RequestObject myRequestObject = new RequestObject("request_"
						+ idx, "request_" + idx);
				LogUtil.i(myRequestObject.toString());
				sendData(socketChannel, myRequestObject);

				ResponseObject myResponseObject = receiveData(socketChannel);
				LogUtil.i(myResponseObject.toString());
			} catch (Exception ex) {
				LogUtil.i(ex.toString());
			} finally {
				try {
					socketChannel.close();
				} catch (Exception ex) {
				}
			}
		}

		private void sendData(SocketChannel socketChannel,
				RequestObject myRequestObject) throws IOException {
			byte[] bytes = SerializableUtil.toBytes(myRequestObject);
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			socketChannel.write(buffer);
			socketChannel.socket().shutdownOutput();
		}

		private ResponseObject receiveData(SocketChannel socketChannel)
				throws IOException {
			ResponseObject myResponseObject = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
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
				myResponseObject = (ResponseObject) obj;
				socketChannel.socket().shutdownInput();
			} finally {
				try {
					baos.close();
				} catch (Exception ex) {
				}
			}
			return myResponseObject;
		}
	}
}
