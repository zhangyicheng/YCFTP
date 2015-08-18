package com.galaxywind.ycftp.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.VelocityTracker;
import android.view.MotionEvent;
import android.widget.ListView;

public class YCListView extends ListView {
	// 滑动距离及坐标
	private float startX, offsetX, startY, offsetY;
	// 计算用户滑动的速度
	private boolean responseTouch = true;

	public YCListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public YCListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public YCListView(Context context, AttributeSet attrs) {
		super(context, attrs);
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
//			if (Math.abs(offsetX) > 40) {
//				setFocusable(false);
//				setClickable(false);
//				setLongClickable(false);
//				return false;
//			} else {
//				setFocusable(true);
//				setClickable(true);
//				setLongClickable(true);
//				return super.dispatchTouchEvent(ev);
//			}
//		case MotionEvent.ACTION_CANCEL:
//		case MotionEvent.ACTION_UP:
//			// 记录用户手指离开时候相差的值
//			offsetX = ev.getX() - startX;
//			offsetY = ev.getY() - startY;
//			if (Math.abs(offsetX) > 40) {
//				setFocusable(false);
//				setClickable(false);
//				setLongClickable(false);
//				return false;
//			} else {
//				setFocusable(true);
//				setClickable(true);
//				setLongClickable(true);
//				return super.dispatchTouchEvent(ev);
//			}
//		}
//		return super.dispatchTouchEvent(ev);
//	}


	public boolean isResponseTouch() {
		return responseTouch;
	}

	public void setResponseTouch(boolean responseTouch) {
		this.responseTouch = responseTouch;
	}
}
