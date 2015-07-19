package com.open.tooltip;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.open.tooltip.TooltipMgr.DrawMessage;

/**
 * 
 * @author skyace
 *
 */
public class HorImageListView extends View {

	private int current_OffsetX=0;//当前屏X轴的偏移量
	private int cuurent_OffsetY=0;//当前屏Y轴的偏移量
	private int moving_OffsetX=0;//当前S手势X轴偏移量
//	private int moving_OffsetY=0;//当前S手势Y轴偏移量
	private int current_Page=1;//当前页码，开始页码为1
	private int portraitNumberPerScreen;//每屏头像个数
	private int charHeight=0;//一个字的高度
	private int maxPage=1;//最大页码
	
	private int current_foucsIndex=0;//当前焦点
	private int current_longPressIndex=-1;//当前长按焦点
	private int current_clickIndex=0;//当前点击
	
	//绘制头像相关
	private int headWidth=0;//头像宽度
	private int paddingLeft;//左边距
//	private Bitmap []bitmapArray;//头像数组
	private Rect headRectArray[]=null;//头像位置
	private Rect drawingRect=new Rect();
	
	//绘制小红点
	private Drawable unReadDrawable;//未读小红点
	private Rect unReadDrawableRect=new Rect();//未读小红点位置
	
	//绘制姓名相关
    private StringBuilder nameSb=new StringBuilder();
	private Rect nameBounds = new Rect();  
	private Paint namePaint =new Paint(); 

    private Bitmap mCircleBitmap=null;
    private PorterDuffXfermode xfermode=new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
    private PaintFlagsDrawFilter pdf=new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
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
    
    private IHeadClick headClickListener;
    
    private Handler mHandler=new Handler();
    private ArrayList<DrawMessage> data=null;
    
	public HorImageListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public HorImageListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorImageListView(Context context) {
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
		
		namePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.name_size));    
		namePaint.setAntiAlias(true);   
		namePaint.setColor(Color.WHITE);
		
		unReadDrawable=getResources().getDrawable(R.drawable.tooltip_msg_unread);
		unReadDrawableRect.set(0, 0, unReadDrawable.getIntrinsicWidth(), unReadDrawable.getIntrinsicHeight());
		
		mGestureDetector=new GestureDetector(new CusGestureListener());
		setLongClickable(true); 
		setOnTouchListener(onTouchListener);
	}
	
	public void setAdapter(ArrayList<DrawMessage> data)
	{
		this.data=data;
		this.data.get(0).unReadCount=0;
		
		current_Page=1;
		current_OffsetX=0;
		current_clickIndex=0;
		current_foucsIndex=0;
		current_longPressIndex=-1;
		
		if(headWidth==0)
		{
			headWidth=DensityUtil.dip2px(getContext(), 60);
		}
		mCircleBitmap=TooltipMgr.getInstance().getCircleBitmap(headWidth, headWidth);
		headRectArray=new Rect[data.size()];
		for(int i=0;i<headRectArray.length;i++)//随时滑动
		{
			headRectArray[i]=new Rect();
		}
		requestLayout();
		invalidate();
	}
	
	private void calculate()
	{
		if(getMeasuredWidth()<=0)
		{
			return;
		}
		
		int size=data.size();
		portraitNumberPerScreen=getMeasuredWidth()/headWidth;
		portraitNumberPerScreen--;//少一个元素，使不会那么拥挤
		paddingLeft=(int)((float)(getMeasuredWidth()-portraitNumberPerScreen*headWidth)/(float)(portraitNumberPerScreen+1)+0.5f);
		
		if(size>portraitNumberPerScreen)
		{
			maxPage=(size%portraitNumberPerScreen==0)?size/portraitNumberPerScreen:size/portraitNumberPerScreen+1;
		}
		else
		{
			maxPage=1;
		}
		int left = 0;
		int top = DensityUtil.dip2px(getContext(), 10);
		int right = 0;
		int bottom = top+headWidth;
		
		for(int i=0;i<maxPage;i++)//分页效果
		{
			int pageWidthPadding=i*getMeasuredWidth();
			for(int j=0;j<portraitNumberPerScreen&&(i*portraitNumberPerScreen+j)<size;j++)
			{
				left=pageWidthPadding+paddingLeft*(j+1)+j*headWidth;
				right=left+headWidth;
				headRectArray[i*portraitNumberPerScreen+j].set(left, top, right, bottom);
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int height=DensityUtil.dip2px(getContext(), 75);
        namePaint.getTextBounds("H", 0, 1, nameBounds);  
        charHeight=nameBounds.height();
        height+=charHeight;  
        height+=5;
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height); 
		
		calculate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int size=data.size();
		if(size==0)
		{
			return;
		}
		
		if(current_clickIndex>0)
		{
				canvas.setDrawFilter(pdf);
				for(int i=0;i<size;i++)
				{
					drawingRect.left=headRectArray[i].left+current_OffsetX+clickOffsetBackDx;
					drawingRect.top=headRectArray[i].top+cuurent_OffsetY;
					drawingRect.right=headRectArray[i].right+current_OffsetX+clickOffsetBackDx;
					drawingRect.bottom=headRectArray[i].bottom+cuurent_OffsetY;
					
					if(i>current_clickIndex)//还需要优化
					{
						if(current_Page==1)
						{
							drawingRect.left=headRectArray[i].left+current_OffsetX;
							drawingRect.top=headRectArray[i].top+cuurent_OffsetY;
							drawingRect.right=headRectArray[i].right+current_OffsetX;
							drawingRect.bottom=headRectArray[i].bottom+cuurent_OffsetY;
						}
						else if(current_Page>1)
						{
							
						}
					}
					
					if(drawingRect.right<getLeft())
					{
						continue;
					}
					else if(drawingRect.left>getRight())
					{
						break;
					}
					
					if(current_clickIndex!=i)
					{
						//绘制头像
				        canvas.drawBitmap(mCircleBitmap, null, drawingRect, paint);
				        paint.setXfermode(xfermode);
				        canvas.drawBitmap(TooltipMgr.getInstance().getBitmap(data.get(i).msgList.get(0).getAvatar()), null, drawingRect, paint);
				        paint.setXfermode(null);
				        
				        //绘制小红点(有未读消息的情况下)
				        if(data.get(i).unReadCount>0)
				        {
				        	int top=drawingRect.top;
				        	int right= drawingRect.right;
				        	unReadDrawableRect.set(right-unReadDrawable.getIntrinsicWidth(), top, right, top+unReadDrawable.getIntrinsicHeight());
				        	unReadDrawable.setBounds(unReadDrawableRect);
				        	unReadDrawable.draw(canvas);
				        }
				        
				       //绘制姓名
				        if(nameSb.length()>0)
				        {
				        	nameSb.delete(0, nameSb.length());
				        }
				        nameSb.append(data.get(i).msgList.get(0).getName());
				        namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);  
				        int width=nameBounds.width();
				        if(width>drawingRect.width())
				        {
				        	while(true)
				        	{
				        		nameSb.deleteCharAt(nameSb.length()-1);
				        		namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);
				        		if(nameBounds.width()<=drawingRect.width())
				        		{
				        			width=nameBounds.width();
				        			break;
				        		}
				        	}
				        }
				        int dw=(drawingRect.width()-width)/2;
				        canvas.drawText(nameSb.toString(), drawingRect.left+dw, drawingRect.bottom+charHeight+10, namePaint);
					}
				}
				
				drawingRect.left=headRectArray[0].left+clickOffsetforwarDx;
				drawingRect.top=headRectArray[0].top;
				drawingRect.right=headRectArray[0].right+clickOffsetforwarDx;
				drawingRect.bottom=headRectArray[0].bottom;
				
                int sc = canvas.saveLayer(drawingRect.left, drawingRect.top, drawingRect.right, drawingRect.bottom, null,LAYER_FLAGS);
				
                //绘制头像
		        canvas.drawBitmap(mCircleBitmap, null, drawingRect, paint);
		        paint.setXfermode(xfermode);
		        canvas.drawBitmap(TooltipMgr.getInstance().getBitmap(data.get(current_clickIndex).msgList.get(0).getAvatar()), null, drawingRect, paint);
		        paint.setXfermode(null);
		        
			    //绘制姓名
		        if(nameSb.length()>0)
		        {
		        	nameSb.delete(0, nameSb.length());
		        }
		        nameSb.append(data.get(current_clickIndex).msgList.get(0).getName());
		        namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);  
		        int width=nameBounds.width();
		        if(width>drawingRect.width())
		        {
		        	while(true)
		        	{
		        		nameSb.deleteCharAt(nameSb.length()-1);
		        		namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);
		        		if(nameBounds.width()<=drawingRect.width())
		        		{
		        			width=nameBounds.width();
		        			break;
		        		}
		        	}
		        }
		        int dw=(drawingRect.width()-width)/2;
		        canvas.drawText(nameSb.toString(), drawingRect.left+dw, drawingRect.bottom+charHeight+10, namePaint);
		        
		        canvas.restoreToCount(sc);
		}
		else
		{
				canvas.setDrawFilter(pdf);
				for(int i=0;i<size;i++)
				{
					drawingRect.left=headRectArray[i].left+current_OffsetX+moving_OffsetX;
					drawingRect.top=headRectArray[i].top+cuurent_OffsetY;
					drawingRect.right=headRectArray[i].right+current_OffsetX+moving_OffsetX;
					drawingRect.bottom=headRectArray[i].bottom+cuurent_OffsetY;
					
					if(drawingRect.right<getLeft())
					{
						continue;
					}
					else if(drawingRect.left>getRight())
					{
						break;
					}
					
					if(current_longPressIndex==i)
					{
//							int insetDx=(int)((float)drawingRect.height()/(float)8);
//							drawingRect.inset(insetDx, insetDx);
						continue;
					}
					
					//绘制头像
			        canvas.drawBitmap(mCircleBitmap, null, drawingRect, paint);
			        paint.setXfermode(xfermode);
			        canvas.drawBitmap(TooltipMgr.getInstance().getBitmap(data.get(i).msgList.get(0).getAvatar()), null, drawingRect, paint);
			        paint.setXfermode(null);
			        
			      //绘制小红点(有未读消息的情况下)
			        if(data.get(i).unReadCount>0)
			        {
			        	int top=drawingRect.top;
			        	int right= drawingRect.right;
			        	unReadDrawableRect.set(right-unReadDrawable.getIntrinsicWidth(), top, right, top+unReadDrawable.getIntrinsicHeight());
			        	unReadDrawable.setBounds(unReadDrawableRect);
			        	unReadDrawable.draw(canvas);
			        }
			        
			       //绘制姓名
			        if(nameSb.length()>0)
			        {
			        	nameSb.delete(0, nameSb.length());
			        }
			        nameSb.append(data.get(i).msgList.get(0).getName());
			        namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);  
			        int width=nameBounds.width();
			        if(width>drawingRect.width())
			        {
			        	while(true)
			        	{
			        		nameSb.deleteCharAt(nameSb.length()-1);
			        		namePaint.getTextBounds(nameSb.toString(), 0, nameSb.length(), nameBounds);
			        		if(nameBounds.width()<=drawingRect.width())
			        		{
			        			width=nameBounds.width();
			        			break;
			        		}
			        	}
			        }
			        int dw=(drawingRect.width()-width)/2;
			        canvas.drawText(nameSb.toString(), drawingRect.left+dw, drawingRect.bottom+charHeight+10, namePaint);
				}
		}
	}
	
	private OnTouchListener onTouchListener=new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean isConsumed =mGestureDetector.onTouchEvent(event);
			if (isConsumed) return true;
			
			if(event.getAction()==MotionEvent.ACTION_CANCEL||event.getAction()==MotionEvent.ACTION_UP)
			{
				//----分页代码
				int direct=0;//往左边为1，右边为-1,保持不变为0
				if(moving_OffsetX>getMeasuredWidth()/2)//向右边滑过半屏
				{
					current_Page--;
					direct=1;
					if(current_Page<1)
					{
						current_Page=1;
						direct=0;
					}
				}
				else if(moving_OffsetX<-getMeasuredWidth()/2)//向左边滑过半屏
				{
					current_Page++;
					direct=-1;
					if(current_Page>maxPage)
					{
						current_Page=maxPage;
						direct=0;
					}
				}
				
				int old=current_OffsetX+moving_OffsetX;
				int newX=current_OffsetX+direct*getMeasuredWidth();
				if(direct!=0)
				{
					current_foucsIndex=(current_Page-1)*portraitNumberPerScreen;
					
					if(null!=headClickListener)
					headClickListener.onItemClick(current_foucsIndex);
				}
				
Log.v("dx:"+current_OffsetX, "----------------------");
				
				if(current_longPressIndex!=-1)
				{
					TooltipMgr.getInstance().updateDraggedMessage(event);
					if(TooltipMgr.getInstance().isDraggedMessageDelete(event))
					{
						TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_MESSAGE_DRAG_FLASHVIEW_END, null);
						
						data.remove(current_longPressIndex);
						if(TooltipMgr.getInstance().getMessageList().size()<=0)
						{
							TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_CHATWINDOW_DRAWBACK, null);
						}
						else
						{
							HorImageListView.this.data.get(0).unReadCount=0;
							HorImageListView.this.current_Page=1;
							HorImageListView.this.current_OffsetX=0;
							HorImageListView.this.current_clickIndex=0;
							HorImageListView.this.current_foucsIndex=0;
							HorImageListView.this.current_longPressIndex=-1;
							invalidate();
							
							if(null!=headClickListener)
								headClickListener.onItemClick(current_clickIndex);
						}
					}
					else
					{
						postDelayed(new Runnable() {
							public void run() {
								invalidate();
							}
						}, 200);
					}
					current_longPressIndex=-1;//恢复
					return false;
				}
				else
				{
					mHandler.post(new SmoothRunnable(old, newX));
				}
				return false;
			}
			
			if(current_longPressIndex!=-1)
			{
				TooltipMgr.getInstance().updateDraggedMessage(event);
			}
			invalidate();
			return isConsumed; 
		}
	};
	
	private GestureDetector mGestureDetector;
	private class CusGestureListener extends SimpleOnGestureListener
	{
		private Rect mDragRect=new Rect();
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.v("CusGestureListener", "onSingleTapUp");
			
			int x=(int) e.getX();
			int y=(int) e.getY();
			
			boolean isInner=false;
			Rect mRect=new Rect();
			int size=data.size();
			for(int i=0;i<size;i++)
			{
				mRect.left=headRectArray[i].left+current_OffsetX+moving_OffsetX;
				mRect.top=headRectArray[i].top+cuurent_OffsetY;
				mRect.right=headRectArray[i].right+current_OffsetX+moving_OffsetX;
				mRect.bottom=headRectArray[i].bottom+cuurent_OffsetY;
				
				if(!isInner)
				{
					if(mRect.top<=y&&mRect.bottom>=y)
					{
						isInner=true;
					}
					else
					{
						break;
					}
				}
				
				if(mRect.contains(x, y))
				{
					current_foucsIndex=i;
					current_clickIndex=i;
					
					//动画：被点击向前移动，未点击向后移动
					if(null!=mClickRunnable)
					{
						mClickRunnable.stop();
					}
					mClickRunnable=new ClickRunnable(current_clickIndex);
					mHandler.post(mClickRunnable);
				}
			} 
			return super.onSingleTapUp(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
//			Log.v("CusGestureListener", "onLongPress");
			
			int x=(int) e.getRawX();
			int y=(int) (e.getRawY()-TooltipMgr.getInstance().getStatusBarHeight(getContext()));
			
			Rect mRect=new Rect();
			int size=data.size();
			for(int i=0;i<size;i++)
			{
				mRect.left=headRectArray[i].left+current_OffsetX+moving_OffsetX;
				mRect.top=headRectArray[i].top+cuurent_OffsetY;
				mRect.right=headRectArray[i].right+current_OffsetX+moving_OffsetX;
				mRect.bottom=headRectArray[i].bottom+cuurent_OffsetY;
				
				if(mRect.contains(x, y))
				{
					current_longPressIndex=i;
					
					Bundle mBundle=new Bundle();
					mBundle.putIntArray("data", new int[]{mRect.centerX(),mRect.centerY()});
					mBundle.putString("data1", TooltipMgr.getInstance().getMessageList().get(current_longPressIndex).msgList.get(0).getAvatar());
					TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_MESSAGE_DRAG, mBundle);
					TooltipMgr.getInstance().updateDraggedMessage(e);
					return ;
				}
			}
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
//			Log.v("CusGestureListener", "onScroll");
			
			int tx=(int) e1.getRawX();
			int ty=(int) (e1.getRawY()-TooltipMgr.getInstance().getStatusBarHeight(getContext()));
			
			int tx2=(int) e2.getRawX();
			int ty2=(int) (e2.getRawY()-TooltipMgr.getInstance().getStatusBarHeight(getContext()));
			
			if(mDragRect.contains(tx, ty)&&mDragRect.contains(tx2, ty2))
			{
				moving_OffsetX=(int) (e2.getRawX()-e1.getRawX());
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
//			Log.d("CDH", "onFling currentPage:"+currentPage+" x1:"+e1.getRawX()+" x2:"+e2.getRawX()+" velocityX:"+velocityX+"velocityY:"+velocityY);
			
			if (Math.abs(velocityX) < 1000) return false;
			
			int direct=0;//往左边为1，右边为-1,保持不变为0
			if(velocityX > 0)
			{
				current_Page--;
				direct=1;
				if(current_Page<1) 
				{
					current_Page=1;
					direct=0;
				}
			} 
			else 
			{
				current_Page++;
				direct=-1;
				if(current_Page>maxPage) 
				{
					current_Page=maxPage;
					direct=0;
				}
			}

			int old=current_OffsetX+moving_OffsetX;
			int newX=current_OffsetX+direct*getMeasuredWidth();
			mHandler.post(new SmoothRunnable(old, newX));
			
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
//			Log.v("CusGestureListener", "onShowPress");
			
			super.onShowPress(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
//			Log.v("CusGestureListener", "onDown");
			
			moving_OffsetX=0;
			mDragRect.set(getLeft(), getTop(), getRight(), getBottom());
			return super.onDown(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
//			Log.v("CusGestureListener", "onDoubleTap");
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
//			Log.v("CusGestureListener", "onDoubleTapEvent");
			
			return super.onDoubleTapEvent(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
//			Log.v("CusGestureListener", "onSingleTapConfirmed");
			
			return super.onSingleTapConfirmed(e);
		}
	}
	
	//平滑移动
	private class SmoothRunnable implements Runnable
	{
		private int startDx;
		private int endDx;
		private long duration=250;
		private long interval=10;
		private long startTime;
		private long endTime;
		
		public SmoothRunnable(int startDx, int endDx) {
			super();
			this.startDx = startDx;
			this.endDx = endDx;
		}

		@Override
		public void run() {
			if(startTime==0)
			{
				moving_OffsetX=0;
				startTime=System.currentTimeMillis();
				endTime=startTime+duration;
			}
			
			long currentTime=System.currentTimeMillis();
			if(currentTime<endTime)
			{
				current_OffsetX=(int)(startDx+(endDx-startDx)*((float)(currentTime-startTime)/(float)(duration)));
				invalidate();
				mHandler.postDelayed(this, interval);
			}
			else
			{
				current_OffsetX=endDx;
				moving_OffsetX=0;
				invalidate();
			}
		}
	} 

	private int clickOffsetforwarDx=0;
	private int clickOffsetBackDx;
	//点击移动到队头部,其他往后面移动
	private ClickRunnable mClickRunnable=null;
	private class ClickRunnable implements Runnable
	{
		private int current_clickIndex;
		private int forwardDx=0;//到队列头部的总长度
		private int backwardsDx=0;//到第二个的长度
		private long duration=300;
		private long interval=10;
		private long startTime;
		private long endTime;
		
		public ClickRunnable(int current_clickIndex)
		{
			this.current_clickIndex=current_clickIndex;
			duration=Math.max((long) (duration*(float)current_clickIndex/(float)portraitNumberPerScreen),duration);
			duration=Math.min(duration, 700);
		}
		
		public void stop()
		{
			mHandler.removeCallbacks(this);
		}

		@Override
		public void run() {
			if(startTime==0)
			{
				Rect mRect=new Rect();
				mRect.left=headRectArray[current_clickIndex].left+current_OffsetX+moving_OffsetX;
				mRect.top=headRectArray[current_clickIndex].top+cuurent_OffsetY;
				mRect.right=headRectArray[current_clickIndex].right+current_OffsetX+moving_OffsetX;
				mRect.bottom=headRectArray[current_clickIndex].bottom+cuurent_OffsetY;
				
				forwardDx=mRect.left-headRectArray[0].left;
				
				int left=paddingLeft*2+headWidth;
				int top=DensityUtil.dip2px(getContext(), 10);
				Rect secord=new Rect(left,top, left+headWidth, top+headWidth);
				backwardsDx=secord.left-headRectArray[0].left+Math.abs(current_OffsetX);
				
				startTime=System.currentTimeMillis();
				endTime=startTime+duration;
			}
			
			long currentTime=System.currentTimeMillis();
			if(currentTime<endTime)
			{
				clickOffsetforwarDx=(int) (forwardDx-forwardDx*((float)(currentTime-startTime)/(float)(duration)));
				clickOffsetBackDx=(int) (backwardsDx*((float)(currentTime-startTime)/(float)(duration)));
				invalidate();
				mHandler.postDelayed(this, interval);
			}
			else
			{
				clickOffsetforwarDx=0;
				clickOffsetBackDx=0;
				
				DrawMessage tmp=data.remove(current_clickIndex);
				data.add(0, tmp);
				
				HorImageListView.this.data.get(0).unReadCount=0;
				HorImageListView.this.current_Page=1;
				HorImageListView.this.current_OffsetX=0;
				HorImageListView.this.current_clickIndex=0;
				HorImageListView.this.current_foucsIndex=0;
				HorImageListView.this.current_longPressIndex=-1;
				invalidate();
				
				if(null!=headClickListener)
				headClickListener.onItemClick(current_clickIndex);
			}
		}
	}
	
	public void setOnHeadClickListener(IHeadClick headClickListener)
	{
		this.headClickListener=headClickListener;
	}
	
	public static interface IHeadClick
	{
		public void onItemClick(int position);
	}
}
