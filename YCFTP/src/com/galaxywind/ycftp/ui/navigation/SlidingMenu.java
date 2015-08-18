package com.galaxywind.ycftp.ui.navigation;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.galaxywind.ycftp.R;
import com.galaxywind.ycftp.utils.ScreenUtil;
import com.nineoldandroids.view.ViewHelper;


public class SlidingMenu extends HorizontalScrollView {
	
	/**
	 * 屏幕宽度
	 */
	private int mScreenWidth;
	/**
	 * dp
	 */
	private int mMenuRightPadding;
	/**
	 * 菜单的宽度
	 */
	private int mMenuWidth;
	private int mHalfMenuWidth;
	private float openedWidth = 0;

	private boolean isOpen;

	public boolean isOpen() {
		return isOpen;
	}

	private boolean once;

	private ViewGroup mMenu;
	private ViewGroup mContent;

	// 当前页面处于那个界面
	private int indexpos = 0;

	// 计算用户滑动的速度
	private VelocityTracker vt = null;
	

	public SlidingMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScreenWidth = ScreenUtil.getScreenWidth(context);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SlidingMenu, defStyle, 0);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.SlidingMenu_rightPadding:
				// 默认50
				mMenuRightPadding = a.getDimensionPixelSize(attr,
						(int) TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP, 50f,
								getResources().getDisplayMetrics()));// 默认为10DP
				break;
			}
		}
		a.recycle();
	}

	public SlidingMenu(Context context) {
		this(context, null, 0);
	}

	private View mView;

	public void setChildView(View v) {
		mView = v;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/**
		 * 显示的设置一个宽度
		 */
		if (!once) {
			LinearLayout wrapper = (LinearLayout) getChildAt(0);
			mMenu = (ViewGroup) wrapper.getChildAt(0);
			mContent = (ViewGroup) wrapper.getChildAt(1);

			mMenuWidth = mScreenWidth - mMenuRightPadding;
			mHalfMenuWidth = mMenuWidth / 2;
			mMenu.getLayoutParams().width = mMenuWidth;
			mContent.getLayoutParams().width = mScreenWidth;

		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			// 将菜单隐藏
			this.scrollTo(mMenuWidth, 0);
			once = true;
		}
	}

	private float startX, offsetX, startY, offsetY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub

		try {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 记录用户按下时x，y的坐标
				startX = ev.getX();
				startY = ev.getY();

				if (vt == null) {
					// 初始化velocityTracker的对象 vt 用来监测motionevent的动作
					vt = VelocityTracker.obtain();
				} else {
					vt.clear();
				}
				vt.addMovement(ev);

				break;
			case MotionEvent.ACTION_MOVE:

				// 移动的速度
				vt.addMovement(ev);
				// 代表的是监测每1000毫秒手指移动的距离（像素）即m/px，这是用来控制vt的单位，若括号内为1，则代表1毫秒手指移动过的像素数即ms/px
				vt.computeCurrentVelocity(1000);
				
				
				  
				offsetX = ev.getX() - startX;
				offsetY = ev.getY() - startY;

				// 左右滑动
				if (offsetX < -5) {
					// 向左滑动

					// 如果用户在第一个界面的时候并且菜单是打开的状态下
					if (indexpos == 0 && isOpen) {
						return super.dispatchTouchEvent(ev);
					} else {
						return mView.onTouchEvent(ev);
//						return mView.dispatchTouchEvent(ev);
					}

				} else if (offsetX > 5) {
					// 向右滑动

					// 如果用户在第一个界面的时候并且菜单是关闭的状态下

					if (indexpos == 0 && !isOpen) {
						return super.dispatchTouchEvent(ev);
					} else {
						return mView.onTouchEvent(ev);
//						return mView.dispatchTouchEvent(ev);
					}
				}

				break;
			case MotionEvent.ACTION_UP:

				float XVelocity=vt.getXVelocity();
				
				Log.i("sudu", "X:的速度"+vt.getXVelocity()+"  indexpos "+indexpos+"  mMenuWidth:"+mMenuWidth);
				
				if(XVelocity>800&&indexpos == 0&&!isOpen&&offsetX > 0){
					openMenu();
					return true;
				}
				
				if(XVelocity<-800&&indexpos == 0&&isOpen&&offsetX < 0){
					closeMenu();
					return true;
				}
				
				// 记录用户手指离开时候相差的值
				offsetX = ev.getX() - startX;
				offsetY = ev.getY() - startY;

				// 左右滑动
				if (offsetX < 0 && indexpos == 0 && isOpen) {
					// 向左滑动
					int scrollX = getScrollX();
					if (scrollX > mHalfMenuWidth) {
						this.smoothScrollTo(mMenuWidth, 0);
						isOpen = false;
						performMenuStateListner(false);
					} else {
						this.smoothScrollTo(0, 0);
						isOpen = true;
						performMenuStateListner(true);
					}
					return true;
				} else if (offsetX > 0 && indexpos == 0) {
					// 向右滑动
					int scrollX = getScrollX();
					if (scrollX > mHalfMenuWidth) {
						this.smoothScrollTo(mMenuWidth, 0);
						isOpen = false;
						performMenuStateListner(false);
					} else {
						this.smoothScrollTo(0, 0);
						isOpen = true;
						performMenuStateListner(true);
					}
					return true;
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mView.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 打开菜单
	 */
	public void openMenu() {
		if (isOpen)
			return;
		this.smoothScrollTo(0, 0);
		isOpen = true;
		performMenuStateListner(true);
	}

	/**
	 * 关闭菜单
	 */
	public void closeMenu() {
		if (isOpen) {
			this.smoothScrollTo(mMenuWidth, 0);
			isOpen = false;
			performMenuStateListner(false);
		}
	}

	/**
	 * 切换菜单状态
	 */
	public void toggle() {
		if (isOpen) {
			closeMenu();
		} else {
			openMenu();
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		float scale = l * 1.0f / mMenuWidth;
		float leftScale = 1 - 0.3f * scale;
		float rightScale = 0.8f + scale * 0.2f;

		ViewHelper.setScaleX(mMenu, leftScale);
		ViewHelper.setScaleY(mMenu, leftScale);
		ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
		openedWidth = mMenuWidth * scale * 0.7f;
		ViewHelper.setTranslationX(mMenu, openedWidth);

		ViewHelper.setPivotX(mContent, 0);
		ViewHelper.setPivotY(mContent, mContent.getHeight() / 2);
		ViewHelper.setScaleX(mContent, rightScale);
		ViewHelper.setScaleY(mContent, rightScale);

	}

	public int getIndexpos() {
		return indexpos;
	}

	public void setIndexpos(int indexpos) {
		this.indexpos = indexpos;
	}
	
	/**
	 * 菜单是否划开状态事件处理
	 * @author zyc
	 *
	 */
	public interface MenuStateListner{
		void onMenuStateChange(boolean isOpen);
	}
	
	private List<MenuStateListner> menuStateListners = new ArrayList<SlidingMenu.MenuStateListner>();
	
	public void deleteMenuStateListner(MenuStateListner menuStateListner){
		menuStateListners.remove(menuStateListner);
	}
	
	public void performMenuStateListner(boolean isOpen){
		for(MenuStateListner listner:menuStateListners){
			listner.onMenuStateChange(isOpen);
		}
	}
	
	public void setOnMenuStateChangeListner(MenuStateListner menuStateListner){
		menuStateListners.add(menuStateListner);
	}

}
