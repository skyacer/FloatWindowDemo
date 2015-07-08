package com.open.tooltip.anim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.open.tooltip.R;
import com.open.tooltip.TooltipMgr;

/**
 * 平移动画
 * @author DexYang
 *
 */
public class TransAnim implements IAnimation {

	public Context context;
	public int []srcLoc;
	public int []desLoc;
	public int status;
	public int duration=350;//动画持续时间
	protected Drawable ballIcon;
	protected Rect ballIconRect=new Rect();
	protected PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	
	public TransAnim(Context context,int[] srcLoc, int[] desLoc, int status, int duration) {
		super();
		this.context=context;
		this.srcLoc = srcLoc;
		this.desLoc = desLoc;
		this.status = status;
		this.duration = duration;
	}

	@Override
	public void onAnimStart() {
		ballIcon=context.getResources().getDrawable(R.drawable.tooltip_icon_nf);
		ballIconRect.set(0, 0, ballIcon.getIntrinsicWidth()*2, ballIcon.getIntrinsicHeight()*2);
		ballIcon.setBounds(ballIconRect);
		
//		int maxLength=Math.max(Math.abs(srcLoc[0]-desLoc[0]), Math.abs(srcLoc[1]-desLoc[1]));
//		this.duration=(int) ((float)(350*maxLength)/(float)900);
	}

	@Override
	public void onAnimDraw(SurfaceHolder holder) {

		int dx=srcLoc[0]-desLoc[0];
		int dy=srcLoc[1]-desLoc[1];
		
		long start=System.currentTimeMillis();
		long runMills=0;
		
		Canvas canvas=null;
		int x;
		int y;
		while((runMills=(System.currentTimeMillis()-start))<duration)
		{
			try {
					canvas=holder.lockCanvas(null);
					if(null!=canvas)
					{
						x=srcLoc[0]-(int)((float)runMills*dx/(float)duration);
						y=srcLoc[1]-(int)((float)runMills*dy/(float)duration);
						canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
						canvas.setDrawFilter(pdf);
						
						ballIconRect.set(x-ballIcon.getIntrinsicWidth()/2, y-ballIcon.getIntrinsicHeight()/2, x+ballIcon.getIntrinsicWidth()/2, y+ballIcon.getIntrinsicHeight()/2);
						ballIcon.setBounds(ballIconRect);
						ballIcon.draw(canvas);
					}
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
	}

	@Override
	public void onAnimEnd() {
		
		if(status==TooltipMgr.STATUS_CHATWINDOW_OPEN)
		{
			TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_CHATWINDOW_EXPAND, null);
		}
		else if(status==TooltipMgr.STATUS_CHATWINDOW_DRAWBACK)
		{
			Bundle mBundle=new Bundle();
			mBundle.putIntArray("data", desLoc);
			TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_CHATWINDOW_CLOSE, mBundle);
		}
		else if(status==TooltipMgr.STATUS_CHATWINDOW_DRAWBACK_MESSAGE_NULL)
		{
			TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_CLOSE,null);
		}
		else if(status==TooltipMgr.STATUS_BALL_DRAG_END)
		{
			Bundle mBundle=new Bundle();
			mBundle.putIntArray("data", desLoc);
			TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_BALL_DRAG_END_BALL, mBundle);
		}
		else if(status==TooltipMgr.STATUS_MESSAGE_DRAG_DELETE_FAILED)
		{
			TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_MESSAGE_DRAG_FLASHVIEW_END, null);
		}
	}
}
