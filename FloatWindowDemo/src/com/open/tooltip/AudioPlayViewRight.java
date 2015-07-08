package com.open.tooltip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 播放声音时
 * @author DexYang
 *
 */
public class AudioPlayViewRight extends View {

	private NinePatchDrawable bgDrawable;
	private Drawable speakerDrawable;
	private Rect bgDrawableRect=new Rect();
	private Rect speakerDrawableRect=new Rect();
	private int bgDrawableRectMinWidth;
	
	private int maxDrawLength;//可绘制长度
	private String mText=null;
	private Paint mTextpaint =new Paint();
	private Rect mTextBounds = new Rect(); 
	private int mTextHeight;  
//	private int mTextwidth; 
	
	private int length=-1;
	private int maxLength=60;
	private int playProgress;//播放进度
	
	public AudioPlayViewRight(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AudioPlayViewRight(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioPlayViewRight(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		bgDrawable=(NinePatchDrawable)getResources().getDrawable(R.drawable.tooltip_audiorecord_bg_2);
		speakerDrawable=getResources().getDrawable(R.drawable.tooltip_speaker_right_3);
		
		mTextpaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.name_size));    
		mTextpaint.setAntiAlias(true);   
		mTextpaint.setColor(Color.WHITE);
	}
	
	public void setMaxLength(int maxLength)
	{
		this.maxLength=maxLength;
	}
	
	public void setLength(int length)
	{
		if(this.length!=length)
		{
			if(length>=maxLength)
			{
				this.length=maxLength;
			}
			else
			{
				this.length=length;
			}
			
			int drawLength=(int)((double)(length*maxDrawLength)/(double)(maxLength));//
			bgDrawableRect.set(getMeasuredWidth()-bgDrawableRectMinWidth-drawLength, 0, getMeasuredWidth(), getMeasuredHeight());
			bgDrawable.setBounds(bgDrawableRect);
			
			playProgress=this.length;
			mText=new StringBuilder().append(playProgress).append("\"").toString();
			mTextpaint.getTextBounds(mText, 0, mText.length()-1, mTextBounds);
			mTextHeight=mTextBounds.height();
			
			invalidate();
		}
	}
	
	public void playingProgress(int playProgress)
	{
		if(this.playProgress!=playProgress)
		{
			this.playProgress=playProgress;
			if(this.playProgress>=this.length)
			{
				this.playProgress=this.length;
			}
			
			mText=new StringBuilder().append(playProgress).append("\"").toString();
			mTextpaint.getTextBounds(mText, 0, mText.length()-1, mTextBounds);
			mTextHeight=mTextBounds.height();
			
			if(speakerPlayRun.isRunning)
			{
				speakerPlayRun.Stop();
			}
			speakerPlayRun=new SpeakerPlayRunnable();
			speakerPlayRun.start();
		}
	}
	
	private SpeakerPlayRunnable speakerPlayRun=new SpeakerPlayRunnable();
	private class SpeakerPlayRunnable implements Runnable {
		
		private int playStatus=0;//0是喇叭第一张，1是喇叭第二张，2是喇叭第三张图片
		public boolean isRunning =false;
		
		public void start()
		{
			playStatus=0;
			isRunning=true;
			
			speakerDrawable=getResources().getDrawable(R.drawable.tooltip_speaker_right_1+playStatus);
			speakerDrawable.setBounds(speakerDrawableRect);
			invalidate();
			
			postDelayed(this, 300);
		}
		
		public void Stop()
		{
			playStatus=2;
			isRunning=false;
			
			speakerDrawable=getResources().getDrawable(R.drawable.tooltip_speaker_right_1+playStatus);
			speakerDrawable.setBounds(speakerDrawableRect);
			invalidate();
			
			removeCallbacks(this);
		}
		
		@Override
		public void run() {
			playStatus++;
			if(playStatus>2)
			{
				playStatus=0;
			}
			
			speakerDrawable=getResources().getDrawable(R.drawable.tooltip_speaker_right_1+playStatus);
			speakerDrawable.setBounds(speakerDrawableRect);
			invalidate();
			
			postDelayed(this, 300);
		}
	};
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredHeight=bgDrawable.getIntrinsicHeight();
		int result=0;
		 int specMode = MeasureSpec.getMode(heightMeasureSpec);  
	        int specSize =  MeasureSpec.getSize(heightMeasureSpec);  
	        switch (specMode) {  
	        case MeasureSpec.UNSPECIFIED:  
	            result = measuredHeight;  
	            break;  
	        case MeasureSpec.AT_MOST:  
	            result = Math.min(measuredHeight, specSize);  
	            break;  
	        case MeasureSpec.EXACTLY:  
	            result = specSize;  
	            break;  
	        }  
	        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), result);
	        
			int left=DensityUtil.dip2px(getContext(), 15);
			int top=(int)((getMeasuredHeight()-speakerDrawable.getIntrinsicHeight())/2);
			speakerDrawableRect.set(getMeasuredWidth()-speakerDrawable.getIntrinsicWidth()-left, top, getMeasuredWidth()-left, top+speakerDrawable.getIntrinsicHeight());
			speakerDrawable.setBounds(speakerDrawableRect);
			
			if(bgDrawableRectMinWidth==0)
			{
				int defaultTextLength=(int) mTextpaint.measureText("0\"", 0, "0\"".length());
				int dis=DensityUtil.dip2px(getContext(), 5);
				int length=left+speakerDrawableRect.width()+defaultTextLength+dis;
				bgDrawableRectMinWidth=Math.max(length, bgDrawable.getIntrinsicWidth());
			}
			
	        maxDrawLength=getMeasuredWidth()-bgDrawableRectMinWidth;
	        
	        int drawLength=(int)((double)(length*maxDrawLength)/(double)(maxLength));//
	        bgDrawableRect.set(getMeasuredWidth()-bgDrawableRectMinWidth-drawLength, 0, getMeasuredWidth(), getMeasuredHeight());
			bgDrawable.setBounds(bgDrawableRect);
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
			bgDrawable.draw(canvas);
			speakerDrawable.draw(canvas);
	        canvas.drawText(mText, getMeasuredWidth()-bgDrawableRect.width()+DensityUtil.dip2px(getContext(), 8), (bgDrawableRect.height()-mTextHeight)/2+mTextHeight, mTextpaint);
	}

}
