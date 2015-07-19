package com.open.tooltip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * 固定高度的ListView
 * @author skyace
 *
 */
public class ExListView extends ListView {

	public ExListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExListView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), Math.min(getMeasuredHeight(), (int)(2.7f*DensityUtil.dip2px(getContext(), 45))));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		if(ev.getAction()==MotionEvent.ACTION_DOWN)
		{
			if(null!=mIonTouchListener)
			{
				mIonTouchListener.onTouchHandle();
			}
		}		
		return super.onInterceptTouchEvent(ev);
	}
	
	public void setOnSelfTouchListener(IonTouch mIonTouchListener)
	{
		this.mIonTouchListener=mIonTouchListener;
	}
	
	public IonTouch mIonTouchListener;
	public interface IonTouch
	{
		public void onTouchHandle();
	}
}
