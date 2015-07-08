package com.open.tooltip.anim;

import android.view.SurfaceHolder;

/**
 * 动画接口类
 * @author yanglonghui
 *
 */
public interface IAnimation
{
	public abstract void onAnimStart();
	
	public abstract void onAnimDraw(SurfaceHolder holder);
	
	public abstract void onAnimEnd();
}
