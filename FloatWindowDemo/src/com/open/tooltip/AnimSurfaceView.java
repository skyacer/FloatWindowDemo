package com.open.tooltip;

import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;

import com.open.tooltip.anim.IAnimation;

/**
 * 小球动画
 * @author skyace
 *
 */
public class AnimSurfaceView extends SurfaceView {

	public AnimSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AnimSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AnimSurfaceView(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		try {  
            if(android.os.Build.VERSION.SDK_INT>=11)  
            {  
                setLayerType(LAYER_TYPE_SOFTWARE, null);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
		
		holder=this.getHolder();
		setZOrderOnTop(true);
		holder.addCallback(callBack);
		holder.setFormat(PixelFormat.TRANSPARENT);
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_UP)
				{
					if(animList.size()==0)
					{
						TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_MESSAGE_DRAG_FLASHVIEW_END, null);
					}
				}
				return false;
			}
		});
	}
	
	private LinkedBlockingQueue<IAnimation> animList=new LinkedBlockingQueue<IAnimation>();//动画队列
	private DrawThread drawThread;
	private SurfaceHolder holder;
	private SurfaceHolder.Callback callBack=new Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
//			Log.v("surfaceDestroyed ", "------------");
			drawThread.isRunning=false;
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
//			Log.v("surfaceCreated ", "------------");
			if(null!=drawThread)
			{
				drawThread.isRunning=false;
			}
			drawThread=new DrawThread();
			drawThread.start();
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
//			Log.v("surfaceChanged: format:"+format, "width："+width+"height: "+height);
		}
	};
	
	public void postAnimation(IAnimation anim)
	{
		animList.add(anim);
	}
	
	private class DrawThread extends Thread
	{
		private boolean isRunning =true;
		
		@Override
		public void run() 
		{
			try {
					while(isRunning)
					{
						IAnimation anim=animList.poll();
						if(null!=anim)
						{
							anim.onAnimStart();
							anim.onAnimDraw(holder);
							anim.onAnimEnd();
							anim=null;
						}
						else
						{
							Thread.sleep(50);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}
