package com.open.tooltip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 播放声音，声音从右往左边走
 * @author DexYang
 *
 */
public class AudioRecordViewRight extends View {

	private NinePatchDrawable fristBg;
	private NinePatchDrawable secondBg;
	private Rect fristRect=new Rect();
	private Rect secondRect=new Rect();
	private int maxDrawLength;//进度可绘制长度
	
	private int bgDrawableRectMinWidth;
	
	private String mText=null;
	private Paint mTextpaint =new Paint();
	private Rect textBounds = new Rect();  
	private int lineHeight;  
	private int progress=0;
	private int maxProgress=60;
	
	public AudioRecordViewRight(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AudioRecordViewRight(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioRecordViewRight(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		fristBg=(NinePatchDrawable)getResources().getDrawable(R.drawable.tooltip_audiorecord_bg_1);
		secondBg=(NinePatchDrawable)getResources().getDrawable(R.drawable.tooltip_audiorecord_bg_2);
		
		mTextpaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.name_size));    
		mTextpaint.setAntiAlias(true);   
		mTextpaint.setColor(Color.WHITE);
		
	    mTextpaint.getTextBounds("8", 0, 1, textBounds);  
	    lineHeight=textBounds.height();
	}
	
	public void setMaxProgress(int max)
	{
		this.maxProgress=max;
	}
	
	public int getMaxProgress()
	{
		return this.maxProgress;
	}
	

	public void setProgress(int progress)
	{
		if(this.progress!=progress)
		{
			if(progress>=maxProgress)
			{
				this.progress=maxProgress;
			}
			else
			{
				this.progress=progress;
			}
			
			int drawLength=(int)((double)(progress*maxDrawLength)/(double)(maxProgress));//
			
			int top=(getHeight()-fristBg.getIntrinsicHeight())/2;
			secondRect.set(getWidth()-bgDrawableRectMinWidth-drawLength, top, getWidth(), top+fristBg.getIntrinsicHeight());
			secondBg.setBounds(secondRect);
			
			mText=new StringBuilder().append(progress).append("\"").toString();
			
			invalidate();
		}
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int measuredHeight=fristBg.getIntrinsicHeight();
			int height=0;
		 	int specMode = MeasureSpec.getMode(heightMeasureSpec);  
	        int specSize =  MeasureSpec.getSize(heightMeasureSpec);  
	        switch (specMode) 
	        {  
		        case MeasureSpec.UNSPECIFIED:  
		            height = measuredHeight;  
		            break;  
		        case MeasureSpec.AT_MOST:  
		            height = Math.min(measuredHeight, specSize);  
		            break;  
		        case MeasureSpec.EXACTLY:  
		            height = specSize;  
		            break;  
	        }  
	        
	        int width=MeasureSpec.getSize(widthMeasureSpec);
	        setMeasuredDimension(width, height);  
	        
	        int top=(height-fristBg.getIntrinsicHeight())/2;
	        fristRect.set(0,top,width,top+fristBg.getIntrinsicHeight());
	        fristBg.setBounds(fristRect);
	        
			if(bgDrawableRectMinWidth==0)
			{
				int defaultTextLength=(int) mTextpaint.measureText("0\"", 0, "0\"".length());
				int dis=DensityUtil.dip2px(getContext(), 5);
				int length=defaultTextLength+dis;
				bgDrawableRectMinWidth=Math.max(length, secondBg.getIntrinsicWidth()/2);
			}
	        maxDrawLength=getMeasuredWidth()-bgDrawableRectMinWidth;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		fristBg.draw(canvas);
		if(progress>0) 
		{
			secondBg.draw(canvas);
	        canvas.drawText(mText, secondRect.left+15, (getHeight()-lineHeight)/2+lineHeight, mTextpaint);
		}
	}
}
