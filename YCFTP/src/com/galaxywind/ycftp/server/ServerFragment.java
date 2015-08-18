package com.galaxywind.ycftp.server;

import com.galaxywind.ycftp.MainFragment;
import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.YCFTPApplication;
import com.galaxywind.ycftp.utils.NetworkUtil;
import com.galaxywind.ycftp.utils.SharePreferenceUtil;
import com.galaxywind.ycftp.utils.StringUtil;
import com.galaxywind.ycftp.widgets.ToggleButton;
import com.galaxywind.ycftp.widgets.ToggleButton.OnSwitchChangedListener;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ServerFragment extends MainFragment {
	//开关FTP服务的按钮
	private ToggleButton toggleBtn;
	private TextView ftpserver_state_Tv,addressTv;
	private View server_address_tip;
	private String port;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_server, null);
		initViews();
		return fragmentView;
	}

	@Override
	protected void initViews() {
		// TODO Auto-generated method stub
		super.initViews();
		toggleBtn = (ToggleButton) fragmentView.findViewById(R.id.toggleBtn);
		toggleBtn.setOnChangeListener(new OnSwitchChangedListener() {
			
			@Override
			public void onSwitchChange(ToggleButton switchView, boolean isChecked) {
				// TODO Auto-generated method stub
				//true为关，false为开
				if(isChecked){
					stopFtpService();
					ftpserver_state_Tv.setText(R.string.start_server);
					server_address_tip.setVisibility(View.GONE);
				}else{
					startFtpService();	
					ftpserver_state_Tv.setText(R.string.stop_server);
					server_address_tip.setVisibility(View.VISIBLE);
				}
			}

			
		});
		
		ftpserver_state_Tv = (TextView) fragmentView.findViewById(R.id.ftpserver_state_Tv);
		
		server_address_tip = fragmentView.findViewById(R.id.server_address_tip);
		
		addressTv = (TextView) fragmentView.findViewById(R.id.addressTv);
		port = SharePreferenceUtil.getPreference(getActivity(), YCFTPApplication.ROOT_DIR_SP, YCFTPApplication.PORT);
		port = StringUtil.isNotEmpty(port)?port:"2121";
		addressTv.setText(NetworkUtil.getLocalIpAddress(getActivity())+":"+port);
		
	}
	
	/**
	 * 开启ftp服务
	 */
	private void startFtpService() {
		Intent ftpServiceintent = new Intent(getActivity(),YCFTPService.class);
//		Intent ftpServiceintent = new Intent(getActivity(),FtpNioServer.class);
		//开启ftp服务
		getActivity().startService(ftpServiceintent);
	}
	
	
	/**
	 * 关闭ftp服务
	 */
	private void stopFtpService(){
		Intent ftpServiceintent = new Intent(getActivity(),YCFTPService.class);
		//开启ftp服务
		getActivity().stopService(ftpServiceintent);
	}
}
