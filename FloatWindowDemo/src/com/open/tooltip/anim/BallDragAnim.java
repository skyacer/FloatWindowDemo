package com.open.tooltip.anim;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.open.tooltip.R;
import com.open.tooltip.ToolTipConfig;
import com.open.tooltip.TooltipMgr;

public class BallDragAnim implements IAnimation {
	
	public Context context;
	protected PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	protected Drawable ballIcon;
	protected Rect ballIconRect=new Rect();
	protected ArrayList<int[]> mStack=new ArrayList<int[]>();
	protected boolean isDragEnd;//手势触发事件
	
	public BallDragAnim(Context context) {
		super();
		this.context = context;
	}

	@Override
	public void onAnimStart() {
		ballIcon=context.getResources().getDrawable(R.drawable.tooltip_icon_nf);
		ballIconRect.set(0, 0, ballIcon.getIntrinsicWidth()*2, ballIcon.getIntrinsicHeight()*2);
		ballIcon.setBounds(ballIconRect);
		mStack.clear();
	}

	@Override
	public void onAnimDraw(SurfaceHolder holder) {
		
		int oldX = 0, oldY = 0;
		while(!isDragEnd)
		{
			Canvas canvas=null;
			if(oldX!=_lastX&&oldY!=_lastY)
			{
				try {
					
					canvas=holder.lockCanvas(null);
					canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
					canvas.setDrawFilter(pdf);

					ballIconRect.set(_lastX-ballIcon.getIntrinsicWidth()/2, _lastY-ballIcon.getIntrinsicHeight()/2, _lastX+ballIcon.getIntrinsicWidth()/2, _lastY+ballIcon.getIntrinsicHeight()/2);
					ballIcon.setBounds(ballIconRect);
					ballIcon.draw(canvas);
					
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(null!=canvas)
					{
						holder.unlockCanvasAndPost(canvas);
					}
					else
					{
						break;
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onAnimEnd() {
		
		int []src=new int[]{_lastX,_lastY};
		int []des=new int[]{0,0};
		
		_lastX=(_lastX<=(TooltipMgr.getInstance().getScreenWidth())/2)?TooltipMgr.getInstance().getBallWidth()/2:TooltipMgr.getInstance().getScreenWidth()-TooltipMgr.getInstance().getBallWidth()/2;
		if(_lastY<TooltipMgr.getInstance().getBallHeight()/2)
		{
			_lastY=TooltipMgr.getInstance().getBallHeight()/2;
		}
		else if(_lastY>TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2)
		{
			_lastY=TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2;
		}
		
		des[0]=_lastX;
		des[1]=_lastY;
		
		IAnimation anim=ToolTipConfig.isAnimWithShadow?
				new TransAnimShadow(context, src, des, TooltipMgr.STATUS_BALL_DRAG_END, 150, TooltipMgr.getInstance().getHeadBitmaps()):
				new TransAnim(context, src, des, TooltipMgr.STATUS_BALL_DRAG_END, 150);
		TooltipMgr.getInstance().getWindowFlashBall().postAnimation(anim);
	}

	int _lastX;
	int _lastY;
	public void reflesh(MotionEvent event)
	{
		int _newlastX=(int) event.getRawX();
		int _newlastY=(int) event.getRawY()- TooltipMgr.getInstance().getStatusBarHeight(context);
		isDragEnd=(event.getAction()==MotionEvent.ACTION_UP)||(event.getAction()==MotionEvent.ACTION_CANCEL);
		if(Math.abs(_lastX-_newlastX)>8||Math.abs(_lastY-_newlastY)>8)
		{
			_lastX=_newlastX;
			_lastY=_newlastY;
			
			mStack.add(new int[]{_lastX,_lastY});
			Log.v("BallDragAnim _lastX:"+_lastX, "_lastY:"+_lastY);
		}
		
//		_lastX=(int) event.getRawX();
//		_lastY=(int) event.getRawY()- TooltipMgr.getInstance().getStatusBarHeight(context);
//		mStack.push(new int[]{_lastX,_lastY});
//		isDragEnd=(event.getAction()==MotionEvent.ACTION_UP)||(event.getAction()==MotionEvent.ACTION_CANCEL);
//		Log.v("BallDragAnim _lastX:"+_lastX, "_lastY:"+_lastY);
	}
}
