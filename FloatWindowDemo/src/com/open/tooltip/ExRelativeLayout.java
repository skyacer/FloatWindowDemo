package com.open.tooltip;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 监听键盘的弹起的RelativeLayout
 * @author Administrator
 *
 */
public class ExRelativeLayout extends RelativeLayout {

	public ExRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExRelativeLayout(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		Log.v("ExRelativeLayout", "h:"+h+"oldh:"+oldh);
		
		if(oldh>0)
		{
			if(h<oldh)//说明键盘弹起
			{
				state=KeyBoard_VISIBLE;
			}
			else if(h>oldh)//说明键盘收缩
			{
				state=KeyBoard_GONE;
			}
			
			if(null!=mISizeChangeListener)
			{
				mISizeChangeListener.onSizeChange(state);
			}
		}
	}

	private int state=KeyBoard_GONE;
	public int getKeyBoardState()
	{
		return state;
	}
	
	public void reSetKeyBoradState()
	{
		state=KeyBoard_GONE;
	}
	
	public static final int KeyBoard_VISIBLE=1;
	public static final int KeyBoard_GONE=2;
	public static final int KeyBoard_OTHER=3;
	
	private IOnSizeChangeListener mISizeChangeListener;
	public void setOnSizeChangeListener(IOnSizeChangeListener mISizeChangeListener)
	{
		this.mISizeChangeListener=mISizeChangeListener;
	}
	
	public static interface IOnSizeChangeListener
	{
		public void onSizeChange(int state);
	}
}
