package com.galaxywind.ycftp.ui.navigation;

import com.lidroid.xutils.util.LogUtils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class YCViewPager extends ViewPager {
	// 是否可滑动
	private boolean isScrollable = true;
	private View childView;
	private float startX, offsetX, startY, offsetY;

	public void setChildView(View v) {
		childView = v;
		LogUtils.i("ChildView"+v.toString());
	}

	public boolean isScrollable() {
		return isScrollable;
	}

	public void setScrollable(boolean isScrollable) {
		this.isScrollable = isScrollable;
	}

	public YCViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public YCViewPager(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		// TODO Auto-generated constructor stub
	}
	
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		// TODO Auto-generated method stub
//		final int action = ev.getAction();
//		final int actionMasked = action & MotionEvent.ACTION_MASK;
//		switch (actionMasked) {
//		case MotionEvent.ACTION_DOWN:
//			// 记录用户按下时x，y的坐标
//			startX = ev.getX();
//			startY = ev.getY();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			offsetX = ev.getX() - startX;
//			offsetY = ev.getY() - startY;
//			// 左右滑动
//			if (Math.abs(offsetX) > 60) {
//				 super.dispatchTouchEvent(ev);
//			} else {
//				if(childView!=null){
//					return childView.onTouchEvent(ev);
//				}else{
//					super.dispatchTouchEvent(ev);
//				}
//			}
//			// case MotionEvent.ACTION_CANCEL:
//		case MotionEvent.ACTION_UP:
//			// 记录用户手指离开时候相差的值
//			offsetX = ev.getX() - startX;
//			offsetY = ev.getY() - startY;
//			if (Math.abs(offsetX) > 60) {
//				 super.dispatchTouchEvent(ev);
//			} else {
//				if(childView!=null){
//					return childView.onTouchEvent(ev);
//				}else{
//					super.dispatchTouchEvent(ev);
//				}
//			}
//		}
//		return super.dispatchTouchEvent(ev);
//	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if (isScrollable) {
			return super.onTouchEvent(arg0);
		} else {
			return isScrollable;
		}
	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (distanceY != 0 && distanceX != 0) {

			}
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				return true;
			}
			return false;
		}
	}
}
