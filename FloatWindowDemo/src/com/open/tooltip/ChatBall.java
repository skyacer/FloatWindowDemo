package com.open.tooltip;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * 小球
 * @author DexYang
 *
 */
public class ChatBall extends ImageView{
	
	private final String TAG="ChatBall";

	public ChatBall(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ChatBall(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ChatBall(Context context) {
		super(context);
		init(context);		
	}
	
	private void init(Context context)
	{
		this.setImageResource(R.drawable.tooltip_icon_f);
		this.setOnTouchListener(onTouchListener);
	}
	
	private OnTouchListener onTouchListener=new OnTouchListener() {

		private boolean isMove=false;
		private Rect mViewRect=new Rect();
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					isMove=false;
					
					//有些机子上调用下面代码无效
//					int[] location = new int[2];
//					v.getLocationOnScreen(location);
//					mViewRect.set(location[0], location[1], location[0]+TooltipMgr.getInstance().getBallWidth(), location[1]+TooltipMgr.getInstance().getBallHeight());
					
					int left=TooltipMgr.getInstance().src[0]-TooltipMgr.getInstance().getBallWidth()/2;
					int top=TooltipMgr.getInstance().src[1]-TooltipMgr.getInstance().getBallHeight()/2;
					mViewRect.set(left, top, left+TooltipMgr.getInstance().getBallWidth(), top+TooltipMgr.getInstance().getBallHeight());
					
					break;
					
				case MotionEvent.ACTION_MOVE:
					if(!isMove)
					{
						int _lastX = (int) event.getRawX();
						int _lastY = (int) event.getRawY()-TooltipMgr.getInstance().getStatusBarHeight(getContext());
						if(!mViewRect.contains(_lastX, _lastY))
						{
							isMove=true;
							
							if(ToolTipConfig.isUseSurfaceView)
							{
								Bundle mBundle=new Bundle();
								mBundle.putIntArray("data", new int[]{_lastX,_lastY});
								TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_BALL_DRAG, mBundle);
							}
							TooltipMgr.getInstance().updataChatBall(event);
						}
					}
					else
					{
							TooltipMgr.getInstance().updataChatBall(event);
					}
					break;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					
					if(isMove)
					{
						TooltipMgr.getInstance().updataChatBall(event);
					}
					else
					{
						TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_CHATWINDOW_OPEN, null);
					}
					break;
			}
			return true;
		}
	};
}
