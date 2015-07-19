package com.open.tooltip;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 
 * @author skyace
 *
 */
public class ExLinearLayout extends LinearLayout {

	public ExLinearLayout(Context context) {
		super(context);
	}

	public ExLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
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
