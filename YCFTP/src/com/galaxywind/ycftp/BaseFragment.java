package com.galaxywind.ycftp;

import com.galaxywind.ycftp.widgets.YCListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 基础Fragment Fragment都应该继承此类
 * @author Administrator
 *
 */
public class BaseFragment extends Fragment  implements OnClickListener{
	/*标题*/
	protected TextView title;
	/*Fragment视图*/
	protected View fragmentView;
	/*ListView为空时的View*/
	protected View emptyView;
	private TextView txt_no_data;
	//是否相应事件
	private boolean responseEvent;
	
		
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ViewUtils.inject(this, fragmentView);
		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle saveInstanceState) {
		super.onActivityCreated(saveInstanceState);
	}
	
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveInstanceState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(saveInstanceState);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}
	
	//初始化页面view
	protected void initViews(){
		emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.view_empty, null);
		title = (TextView) fragmentView.findViewById(R.id.header_title);
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	/**
     * 当listview为空时，进行页面展示处理
     * @param mListView
     * @param no_data (0暂无数据，点击和下拉是可刷新)(1加载失败，请检查网络或稍后重试)(2数据解析失败)
     */
    protected void setEmptyView(ListView mListView,String no_data){
    	emptyView.setOnClickListener(this);
    	txt_no_data=(TextView) emptyView.findViewById(R.id.txt_no_data);
    	txt_no_data.setText(no_data);
    	
    	//先移除
    	ViewParent newEmptyViewParent = emptyView.getParent();
		if (null != newEmptyViewParent && newEmptyViewParent instanceof ViewGroup) {
			((ViewGroup) newEmptyViewParent).removeView(emptyView);
		}
    	//添加
		emptyView.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		((ViewGroup) mListView.getParent()).addView(emptyView);
    	mListView.setEmptyView(emptyView);
    }
    
    protected void initData(){
    	
    }

	public boolean isResponseEvent() {
		return responseEvent;
	}

	public void setResponseEvent(boolean responseEvent) {
		this.responseEvent = responseEvent;
	}
}
