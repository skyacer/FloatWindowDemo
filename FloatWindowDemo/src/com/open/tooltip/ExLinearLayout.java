package com.open.tooltip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 
 * @author Administrator
 *
 */
public class ExLinearLayout extends LinearLayout {

	public ExLinearLayout(Context context) {
		super(context);
	}

	public ExLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("NewApi")
	public ExLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height=0;
		for(int i=0;i<getChildCount();i++)
		{
			int _h=getChildAt(i).getMeasuredHeight();
			if(_h>height)
			{
				height=_h;
			}
		}
		setMeasuredDimension(getMeasuredWidth(), height);
	}
}
