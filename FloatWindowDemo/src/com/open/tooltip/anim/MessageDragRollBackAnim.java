package com.open.tooltip.anim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.open.tooltip.DensityUtil;
import com.open.tooltip.TooltipMgr;

/**
 * 消息删除的时候回滚
 * @author DexYang
 *
 */
public class MessageDragRollBackAnim implements IAnimation {

	private PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	public Context context;
	public int []srcLoc;
	public int []desLoc;
	public int status;
	public int duration=350;//动画持续时间
	private Bitmap headBitmap;
	private Rect headRect=new Rect();
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
	
	public MessageDragRollBackAnim(Context context,Bitmap headBitmap,int[] srcLoc, int[] desLoc, int status, int duration) {
		this.context=context;
		this.headBitmap=headBitmap;
		this.srcLoc = srcLoc;
		this.desLoc = desLoc;
		this.status = status;
		this.duration = duration;
	}

	@Override
	public void onAnimStart() {
		headWidth=DensityUtil.dip2px(context, 60);
		mDstB=TooltipMgr.getInstance().getCircleBitmap(headWidth, headWidth);
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
						
//						headRect.set(x-headWidth/2, y-headWidth/2, x+headWidth/2, y+headWidth/2);
//						canvas.drawBitmap(headBitmap, null, headRect, null);
						
						
//						headRect.set(x-headWidth/2,y-headWidth/2,x+headWidth/2, y+headWidth/2);
//				        canvas.save();
//				        mPath.reset();
//				        canvas.clipPath(mPath); // makes the clip empty
//				        mPath.addCircle(x, y, headWidth/2, Path.Direction.CCW);
//				        canvas.clipPath(mPath, Region.Op.REPLACE);
//				        canvas.drawBitmap(headBitmap, null, headRect, null);
//				        canvas.restore();
				        
						headRect.set(x-headWidth/2,y-headWidth/2,x+headWidth/2, y+headWidth/2);
				        canvas.save();
				        canvas.drawBitmap(mDstB, null, headRect, paint);
				        paint.setXfermode(xfermode);
				        canvas.drawBitmap(headBitmap, null, headRect, paint);
				        paint.setXfermode(null);
				        canvas.restore();
				        
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
		TooltipMgr.getInstance().updateUI(context, TooltipMgr.STATUS_MESSAGE_DRAG_FLASHVIEW_END, null);
	}
}
