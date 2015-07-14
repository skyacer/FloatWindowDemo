package com.open.tooltip.anim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;

import com.open.tooltip.R;

public class AudioRecrodingAnim implements IAnimation{

	private Drawable micBottmDrawable;
	private Drawable micForeDrawable;
	private Rect bottomRect=new Rect();
	private Rect foreRect=new Rect();
	private Rect precentRect=new Rect();
	private final int maxLevel=7;
	
	private Context context;
	
	public AudioRecrodingAnim(Context context) {
		this.context = context;
	}

	@Override
	public void onAnimStart() {
		micBottmDrawable=context.getResources().getDrawable(R.drawable.tooltip_mic_bottom);
		micForeDrawable=context.getResources().getDrawable(R.drawable.tooltip_mic_1);
		bottomRect.set(0, 0, micBottmDrawable.getIntrinsicWidth(), micBottmDrawable.getIntrinsicHeight());
		micBottmDrawable.setBounds(bottomRect);
		foreRect.set(0, 0, micForeDrawable.getIntrinsicWidth(), micForeDrawable.getIntrinsicHeight());
		micForeDrawable.setBounds(foreRect);
		precentRect.set(0, 0, micForeDrawable.getIntrinsicWidth(), micForeDrawable.getIntrinsicHeight());
	}

	@Override
	public void onAnimDraw(SurfaceHolder holder) {
		
		Canvas canvas=null;
		while(true)
		{
			try {
					canvas=holder.lockCanvas(null);
					if(null!=canvas)
					{
						micBottmDrawable.draw(canvas);
						
				        canvas.save();
//				        canvas.clipRect(0, 0, getWidth(), getHeight());
				        canvas.clipRect(precentRect,Region.Op.DIFFERENCE);
				        micForeDrawable.draw(canvas);
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
		
	}
	
	
	public void refresh(int level)
	{
		
	}

}
