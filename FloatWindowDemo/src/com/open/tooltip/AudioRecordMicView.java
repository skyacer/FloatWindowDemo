package com.open.tooltip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 麦克风的录音效果
 * @author DexYang
 *
 */
public class AudioRecordMicView extends View{

	private Drawable micBgDrawable;
	private Drawable mic_1Drawable;
	private Drawable mic_2Drawable;
	private Drawable mic_cancelDrawable;
	private Rect micBgDrawableRect=new Rect();
	private Rect mic_1DrawableRect=new Rect();
	private Rect mic_2DrawableRect=new Rect();
	private Rect mic_cancelDrawableRect=new Rect();
	private Rect spaceRect=new Rect();
	private int maxLevel=7;
	
	private boolean isMoveOut=false;
	private String releaseText="松开手指 取消发送";
	private Paint textPaint =new Paint();
	private Rect textBounds = new Rect(); 
	private int linewidth;
	private int lineHeight;
	private int textBegX;
	private int textBegY;
	
	public AudioRecordMicView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AudioRecordMicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioRecordMicView(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		micBgDrawable=getResources().getDrawable(R.drawable.tooltip_mic_bg);
		mic_1Drawable=getResources().getDrawable(R.drawable.tooltip_mic_1);
		mic_2Drawable=getResources().getDrawable(R.drawable.tooltip_mic_2);
		mic_cancelDrawable=getResources().getDrawable(R.drawable.tooltip_mic_cancel);
		
		textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.name_size));    
		textPaint.setAntiAlias(true);   
		textPaint.setColor(Color.RED);
		
		textPaint.getTextBounds(releaseText, 0, releaseText.length(), textBounds);  
		linewidth=(int) textPaint.measureText(releaseText, 0, releaseText.length());//单个字符宽度 
        lineHeight=textBounds.height();
	}
	
	public void setMoveOut(boolean isMoveOut)
	{
		this.isMoveOut=isMoveOut;
		invalidate();
	}
	
	public boolean isMoveOut()
	{
		return isMoveOut;
	}
	
	/**
	 * 设置录音的级数，从1-5
	 * @param level
	 */
	public void setLevel(int level)
	{
		if(!isMoveOut)
		{
			int progress=level*mic_2DrawableRect.height()/maxLevel;
			setVolume(progress);
		}
	}
	
	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel=maxLevel;
	}
	
	public int getMaxLevel()
	{
		return maxLevel;
	}
	 
	private void setVolume(int progress)
	{
		int bottom=0;
		if(progress>mic_2DrawableRect.height())
		{
			bottom=0;
		}
		else
		{
			bottom=mic_2DrawableRect.height()-progress;
		}
		spaceRect.set(mic_2DrawableRect.left, mic_2DrawableRect.top, mic_2DrawableRect.right, mic_2DrawableRect.top+bottom);
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width=micBgDrawable.getIntrinsicWidth();
		if(width<linewidth)
		{
			width=linewidth+DensityUtil.dip2px(getContext(), 15);
		}
		else if(width-linewidth<DensityUtil.dip2px(getContext(), 15))
		{
			width+=(DensityUtil.dip2px(getContext(), 10)-(width-linewidth));
		}
		setMeasuredDimension(width, width); 
		
		micBgDrawableRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
		micBgDrawable.setBounds(micBgDrawableRect);
		
		int left=(getMeasuredWidth()-mic_1Drawable.getIntrinsicWidth())/2;
		int top=(getMeasuredHeight()-mic_1Drawable.getIntrinsicHeight())/2;
		
		mic_1DrawableRect.set(left, top, left+mic_1Drawable.getIntrinsicWidth(), top+mic_1Drawable.getIntrinsicHeight());
		mic_1Drawable.setBounds(mic_1DrawableRect);
		
		mic_2DrawableRect.set(mic_1DrawableRect);
		mic_2Drawable.setBounds(mic_2DrawableRect);
		
		spaceRect.set(mic_1DrawableRect);
		
		left=(getMeasuredWidth()-mic_cancelDrawable.getIntrinsicWidth())/2;
		top=(getMeasuredHeight()-mic_cancelDrawable.getIntrinsicHeight())/2;
		mic_cancelDrawableRect.set(left, top, left+mic_cancelDrawable.getIntrinsicWidth(), top+mic_cancelDrawable.getIntrinsicHeight());
		mic_cancelDrawable.setBounds(mic_cancelDrawableRect);
		
		textBegX=(getMeasuredWidth()-linewidth)/2;
		textBegY=mic_cancelDrawableRect.bottom+(getMeasuredHeight()-mic_cancelDrawableRect.bottom-lineHeight)/2+lineHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		micBgDrawable.draw(canvas);
		if(!isMoveOut)
		{
			mic_1Drawable.draw(canvas);
	        canvas.save();
	        canvas.clipRect(mic_2DrawableRect);
	        canvas.clipRect(spaceRect,Region.Op.DIFFERENCE);
	        mic_2Drawable.draw(canvas);
	        canvas.restore();
		}
		else
		{
			mic_cancelDrawable.draw(canvas);
			canvas.drawText(releaseText, textBegX, textBegY, textPaint);
		}
	}
}
