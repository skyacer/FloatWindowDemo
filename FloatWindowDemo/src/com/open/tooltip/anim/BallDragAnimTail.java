package com.open.tooltip.anim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.open.tooltip.TooltipMgr;

public class BallDragAnimTail extends BallDragAnim {

	private Bitmap shadowBitmap[];
	private Rect dst=new Rect();
	private int headWidth;
//	private Path mPath=new Path();
    private Bitmap mDstB=null;
    private PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
	private Paint paint = new Paint();
    {
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
	    paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了 
    }
	
	public BallDragAnimTail(Context context,Bitmap shadowBitmap[]) {
		super(context);
		this.shadowBitmap=shadowBitmap;
	}

	@Override
	public void onAnimStart() {
		super.onAnimStart();
		headWidth=TooltipMgr.getInstance().getBallWidth();
		mDstB=TooltipMgr.getInstance().getCircleBitmap(headWidth, headWidth);
	}


	@Override
	public void onAnimDraw(SurfaceHolder holder) {
		
		while(!isDragEnd)
		{
			Canvas canvas=null;
			int size=0;
			int oldsize=0;
			while((size=mStack.size())>0&&null!=holder)
			{
				if(oldsize==size)
				{
					break;
				}
				oldsize=size;
				
				try {
						canvas=holder.lockCanvas(null);
						canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
						canvas.setDrawFilter(pdf);
	
						for(int i=0;i<shadowBitmap.length;i++)
						{
							int []_point=mStack.get((int)(size*(1-(i+1)*0.1)));
							
							dst.set(_point[0]-headWidth/2, _point[1]-headWidth/2, _point[0]+headWidth/2, _point[1]+headWidth/2);
					        canvas.drawBitmap(mDstB, null, dst, paint);
					        paint.setXfermode(xfermode);
					        canvas.drawBitmap(shadowBitmap[i], null, dst, paint);
					        paint.setXfermode(null);
						}
						
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
			mStack.clear();
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
}
