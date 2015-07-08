package com.open.tooltip.anim;

import java.util.Stack;

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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.open.tooltip.DensityUtil;
import com.open.tooltip.R;
import com.open.tooltip.TooltipMgr;

public class MessageDragAnim implements IAnimation {

	private PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
	private Drawable msgdeleteBg;
	private Rect msgdeleteBgRect=null;
	private Drawable msgdeleteNf;
	private Rect msgdeleteNfRect=new Rect();
	private Drawable msgdeleteF;
	private Rect msgdeleteFRect=new Rect();
	private Rect xRect=new Rect();;
	private Drawable msgdeleteTextBg;
	private Rect msgdeleteTextBgRect=null;
	private Bitmap headBitmap = null;//绘制头像
	private Paint mPaint =new Paint();
	private int lineHeight;
	private int linwidth;
	private int msgdeleteTextBgWidth;
	private int msgdeleteTextBgHeight;
	private String mText="拖到这里关闭与Ta的聊天";
	private Stack<int[]> mStack=new Stack<int[]>();
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
    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG |
            Canvas.CLIP_SAVE_FLAG |
            Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
            Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
            Canvas.CLIP_TO_LAYER_SAVE_FLAG;
    
	private Context context;
	private int animViewWidth;
	private int animViewHeight;
	private boolean isDragEnd;//手势触发事件
	private int[] viewLocation;//view的原始位置
	
	public MessageDragAnim(Context context, int animViewWidth, int animViewHeight,Bitmap headBitmap) {
		super();
		this.context = context;
		this.animViewWidth = animViewWidth;
		this.animViewHeight = animViewHeight;
		this.headBitmap=headBitmap;
	}
	
	public MessageDragAnim(Context context, int animViewWidth, int animViewHeight,int[] viewLocation,Bitmap headBitmap) {
		super();
		this.context = context;
		this.animViewWidth = animViewWidth;
		this.animViewHeight = animViewHeight;
		this.headBitmap=headBitmap;
		this.viewLocation=viewLocation;
	}

	@Override
	public void onAnimStart() {
		msgdeleteBg=context.getResources().getDrawable(R.drawable.tooltip_msg_delete_bg);
		msgdeleteNf=context.getResources().getDrawable(R.drawable.tooltip_msg_delete_nf);
		msgdeleteF=context.getResources().getDrawable(R.drawable.tooltip_msg_delete_f);
		msgdeleteTextBg=context.getResources().getDrawable(R.drawable.tooltip_msg_delete_textbg);
		
		mPaint=new Paint();
		mPaint.setTextSize(25);    
		mPaint.setAntiAlias(true);   
		mPaint.setColor(Color.WHITE);
		
		headWidth=DensityUtil.dip2px(context, 60);
		mDstB=TooltipMgr.getInstance().getCircleBitmap(headWidth, headWidth);
		
		if(null==msgdeleteBgRect)
		{
			msgdeleteBgRect=new Rect();
			msgdeleteBgRect.set(0, 3*animViewHeight/4, animViewWidth,animViewHeight);
			msgdeleteBg.setBounds(msgdeleteBgRect);
		}

		if(null==msgdeleteTextBgRect)
		{
			msgdeleteTextBgRect=new Rect();
			Rect textBounds = new Rect();  
	        mPaint.getTextBounds("8", 0, 1, textBounds);  
	        lineHeight=textBounds.bottom-textBounds.top;  
	        linwidth=(int) mPaint.measureText(mText, 0, mText.length());
	        msgdeleteTextBgWidth=linwidth+50;
	        msgdeleteTextBgHeight=lineHeight+50;
	        
	        int left=(animViewWidth-msgdeleteTextBgWidth)/2;
	        int bottom =animViewHeight-50;
	        
	        msgdeleteTextBgRect.set(left, bottom-msgdeleteTextBgHeight, left+msgdeleteTextBgWidth, bottom);
	        msgdeleteTextBg.setBounds(msgdeleteTextBgRect);
		}
	}

	@Override
	public void onAnimDraw(SurfaceHolder holder) {
		
		while(!isDragEnd)
		{
			Canvas canvas=null;
			int []point;
			int size=0;
			int oldsize=0;
			while((size=mStack.size())>0&&null!=(point=mStack.peek())&&null!=holder)
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
					
					//第一绘制背景图
					msgdeleteBg.draw(canvas);
					
					//第二绘制文字背景
			        msgdeleteTextBg.draw(canvas);
					
			      //第三绘制文字
			        canvas.drawText(mText, msgdeleteTextBgRect.left+(msgdeleteTextBgWidth-linwidth)/2, msgdeleteTextBgRect.top+(msgdeleteTextBgHeight+lineHeight)/2, mPaint);
			        
			      //第四绘制叉叉
//			        if(null==xRect)//原来
//			        {
//						int left =(getWidth()-msgdeleteF.getIntrinsicWidth())/2;
//						int bottom = msgdeleteTextBgRect.top-50;
//						xRect.set(left, bottom-msgdeleteF.getIntrinsicHeight(), left+msgdeleteF.getIntrinsicWidth(), bottom);
//			        }
			        
		        	int width=(int) ((1+(float)point[1]/(float)animViewHeight)*msgdeleteF.getIntrinsicWidth());//现在
		        	int height=(int) ((1+(float)point[1]/(float)animViewHeight)*msgdeleteF.getIntrinsicHeight());
					int left =(animViewWidth-width)/2;
					int centerY=msgdeleteTextBgRect.top-msgdeleteF.getIntrinsicHeight();
					xRect.set(left, centerY-height/2, left+width, centerY+height/2);
					
					if(msgdeleteBgRect.contains(point[0], point[1]))
					{
						msgdeleteFRect.set(xRect);
						msgdeleteF.setBounds(msgdeleteFRect);
						msgdeleteF.draw(canvas);
					}
					else
					{
						msgdeleteNfRect.set(xRect);
						msgdeleteNf.setBounds(msgdeleteNfRect);
						msgdeleteNf.draw(canvas);
					}

					//第五绘制头像
//					headRect.set(point[0]-headWidth/2, point[1]-headWidth/2, point[0]+headWidth/2, point[1]+headWidth/2);
//					canvas.drawBitmap(headBitmap, null, headRect, paint);
					
//					headRect.set(point[0]-headWidth/2, point[1]-headWidth/2, point[0]+headWidth/2, point[1]+headWidth/2);
//			        canvas.save();
//			        mPath.reset();
//			        canvas.clipPath(mPath); // makes the clip empty
//			        mPath.addCircle(point[0], point[1], headWidth/2, Path.Direction.CCW);
//			        canvas.clipPath(mPath, Region.Op.REPLACE);
//			        canvas.drawBitmap(headBitmap, null, headRect, null);
//			        canvas.restore();
					
					headRect.set(point[0]-headWidth/2, point[1]-headWidth/2, point[0]+headWidth/2, point[1]+headWidth/2);
					int sc = canvas.saveLayer(headRect.left, headRect.top, headRect.right, headRect.bottom, null,LAYER_FLAGS);
			        canvas.save();
			        canvas.drawBitmap(mDstB, null, headRect, paint);
			        paint.setXfermode(xfermode);
			        canvas.drawBitmap(headBitmap, null, headRect, paint);
			        paint.setXfermode(null);
			        canvas.restoreToCount(sc);
					
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(null!=canvas)
					{
						holder.unlockCanvasAndPost(canvas);
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

	@Override
	public void onAnimEnd() {
		Log.v("onAnimEnd", "-------------");
//		WindowMgr.getInstance().updateView(context, WindowMgr.STATUS_MESSAGE_DRAG_FLASHVIEW_END, null);
		
		if(msgdeleteBgRect.contains(_lastX, _lastY))//如果是删除了
		{
			
		}
		else
		{
			//先动画，再回去
			MessageDragRollBackAnim anim=new MessageDragRollBackAnim(context, headBitmap,new int[]{_lastX,_lastY}, viewLocation, TooltipMgr.STATUS_MESSAGE_DRAG_DELETE_FAILED, 150);
			TooltipMgr.getInstance().getWindowFlashBall().postAnimation(anim);
		}
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
			mStack.push(new int[]{_lastX,_lastY});
			Log.v("BallDragAnim _lastX:"+_lastX, "_lastY:"+_lastY);
		}
		
//		_lastX=(int) event.getRawX();
//		_lastY=(int) event.getRawY()- TooltipMgr.getInstance().getStatusBarHeight(context);
//		mStack.push(new int[]{_lastX,_lastY});
//		isDragEnd=(event.getAction()==MotionEvent.ACTION_UP)||(event.getAction()==MotionEvent.ACTION_CANCEL);
//		Log.v("MessageDragAnim _lastX:"+_lastX, "_lastY:"+_lastY);
	}
	
	public boolean isDelete(MotionEvent event)
	{
		if(null!=msgdeleteBgRect)
		{
			int _lastX=(int) event.getRawX();
			int _lastY=(int) event.getRawY()- TooltipMgr.getInstance().getStatusBarHeight(context);
			return msgdeleteBgRect.contains(_lastX, _lastY);
		}
		return false;
	}
}
