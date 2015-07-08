package com.open.tooltip;

import android.content.Context;
import android.content.Intent;

import com.open.data.ChatMsg;

public class TooltipUtil {
	
	/**
	 * 需要展示悬浮框的Activity名字，只有注册过的Activity才能显示悬浮框
	 * @param activityName
	 */
	public static void register(Context context , String activityName)
	{
		Intent mIntent=new Intent(context, TooltipService.class);
		mIntent.putExtra("command", TooltipMgr.REGISTER_TOOLTIP);
		mIntent.putExtra("data", activityName);
		context.startService(mIntent);
	}
	
	/**
	 * 接收消息，并展示悬浮框
	 * @param context
	 */
	public static void receMessage(Context context,ChatMsg msg)
	{
		Intent mIntent=new Intent(context, TooltipService.class);
		mIntent.putExtra("command", TooltipMgr.STATUS_MESSAGE_ADD);
		mIntent.putExtra("data", msg);
		context.startService(mIntent);
	}
	
	/**
	 * 关闭悬浮框
	 * @param context
	 */
	public static void closeTooltip(Context context)
	{
		Intent mIntent=new Intent(context, TooltipService.class);
		mIntent.putExtra("command", TooltipMgr.STATUS_CLOSE);
		context.startService(mIntent);
	}

}
