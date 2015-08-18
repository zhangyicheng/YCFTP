package com.galaxywind.ycftp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MainFragment extends BaseFragment {
	protected MainActivity mActivity;
	protected ListView lv;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}

	@Override
	public void onActivityCreated(Bundle saveInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(saveInstanceState);
//		mActivity.getPager().setChildView(getView());
	}
	
	public ListView getListView(){
		return lv;
	}
}
