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

/**
 * 平移动画，伴随着影子(后面跟着)
 * @author DexYang
 *
 */
public class TransAnimShadow extends TransAnim {

	private Bitmap shadowBitmap[];
	private Rect shadowRect=new Rect();
	private int shadowWidth;
//	private Path mPath=new Path();
    private Bitmap mDstB=null;
    private PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
	private Paint paint = new Paint();
    {
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
	    paint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了 
    }
	
	public TransAnimShadow(Context context, int[] srcLoc, int[] desLoc,
			int status, int duration,Bitmap shadowBitmap[]) {
		super(context, srcLoc, desLoc, status, duration);
		this.shadowBitmap=shadowBitmap;
	}

	@Override
	public void onAnimStart() {
		super.onAnimStart();
		shadowWidth=TooltipMgr.getInstance().getBallWidth();
		mDstB=TooltipMgr.getInstance().getCircleBitmap(shadowWidth, shadowWidth);
	}

	@Override
	public void onAnimDraw(SurfaceHolder holder) {

		int dx=srcLoc[0]-desLoc[0];
		int dy=srcLoc[1]-desLoc[1];
		
		long shadDuration[]=new long[shadowBitmap.length];
		for(int i=0;i<shadDuration.length;i++)
		{
			shadDuration[i]=duration+75*(i+1);
		}
		
		long start=System.currentTimeMillis();
		long runMills=0;
		Canvas canvas=null;
		int x;
		int y;
		while((runMills=(System.currentTimeMillis()-start))<shadDuration[shadDuration.length-1])
		{
			try {
					canvas=holder.lockCanvas(null);
					canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
					canvas.setDrawFilter(pdf);
					
					for(int i=0;i<shadowBitmap.length;i++)
					{
						long _runMills=runMills;
						if(runMills>shadDuration[i])
						{
							_runMills=shadDuration[i];
						}
						int _x=srcLoc[0]-(int)((float)_runMills*dx/(float)shadDuration[i]);
						int _y=srcLoc[1]-(int)((float)_runMills*dy/(float)shadDuration[i]);
						
//						shadowRect.set(_x-shadowWidth/2, _y-shadowWidth/2, _x+shadowWidth/2, _y+shadowWidth/2);
//						canvas.drawBitmap(shadowBitmap[i], null, shadowRect, paint);
						
//						shadowRect.set(_x-shadowWidth/2, _y-shadowWidth/2,_x+shadowWidth/2, _y+shadowWidth/2);
//				        canvas.save();
//				        mPath.reset();
//				        canvas.clipPath(mPath); // makes the clip empty
//				        mPath.addCircle(_x, _y, shadowWidth/2, Path.Direction.CCW);
//				        canvas.clipPath(mPath, Region.Op.REPLACE);
//				        canvas.drawBitmap(shadowBitmap[i], null, shadowRect, null);
//				        canvas.restore();
						
						shadowRect.set(_x-shadowWidth/2, _y-shadowWidth/2,_x+shadowWidth/2, _y+shadowWidth/2);
				        canvas.save();
				        canvas.drawBitmap(mDstB, null, shadowRect, paint);
				        paint.setXfermode(xfermode);
				        canvas.drawBitmap(shadowBitmap[i], null, shadowRect, paint);
				        paint.setXfermode(null);
				        canvas.restore();
					}
					
					if(runMills>duration)
					{
						runMills=duration;
					}
					x=srcLoc[0]-(int)((float)runMills*dx/(float)duration);
					y=srcLoc[1]-(int)((float)runMills*dy/(float)duration);
					ballIconRect.set(x-ballIcon.getIntrinsicWidth()/2, y-ballIcon.getIntrinsicHeight()/2, x+ballIcon.getIntrinsicWidth()/2, y+ballIcon.getIntrinsicHeight()/2);
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
	}
}
