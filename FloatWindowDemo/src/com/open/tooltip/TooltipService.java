package com.open.tooltip;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

/**
 * 控制悬浮框
 * @author DexYang
 *
 */
public class TooltipService extends Service {
	
	private final String TAG="TooltipService";

	private ArrayList<String> mActivityList=new ArrayList<String>();
	private Handler mHandler=new Handler();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate");
		super.onCreate();
		TooltipMgr.getInstance().init(getApplicationContext());
		mHandler.post(heartRunnable);
		
		try {
				ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
				if(null!=appInfo.metaData)
				{
					String mActivityNames=appInfo.metaData.getString("tooltipdata");
					if(!TextUtils.isEmpty(mActivityNames))
					{
						String[] names=mActivityNames.split("\\|");
						for(int i=0;i<names.length;i++)
						{
							if(!TextUtils.isEmpty(names[i])&&!mActivityList.contains(names[i]))
							{
								mActivityList.add(names[i]);
								Log.v(TAG, " onCreate Activity:"+names[i]);
							}
						}
					}
				}
			} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		mHandler.removeCallbacks(heartRunnable);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(null!=intent)
		{
			int command=intent.getIntExtra("command", TooltipMgr.STATUS_OPEN);
			if(command==TooltipMgr.STATUS_MESSAGE_ADD)
			{
				int size=TooltipMgr.getInstance().getMessageList().size();
				if(size==0)
				{
					command=TooltipMgr.STATUS_MESSAGE_NULL2FULL;
				}
				else 
				{
					command=TooltipMgr.STATUS_MESSAGE_ADD;
				}
				
				TooltipMgr.getInstance().updateData(intent.getExtras());
				if(isContain())
				{
					TooltipMgr.getInstance().updateUI(getApplicationContext(),command,intent.getExtras());
				}
			}
			else if(command==TooltipMgr.REGISTER_TOOLTIP)
			{
				String mActivity=intent.getExtras().getString("data");
				if(!TextUtils.isEmpty(mActivity)&&!mActivityList.contains(mActivity))
				{
					mActivityList.add(mActivity);
				}
				Log.v(TAG, "Reg Activity:"+mActivity);
			}
			else if(command==TooltipMgr.STATUS_CLOSE)
			{
				TooltipMgr.getInstance().closeWindows();
				mHandler.post(heartRunnable);
				this.stopSelf();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 检测当前界面是不是要显示的界面
	 */
	private Runnable heartRunnable=new Runnable() {
		
		@Override
		public void run() {
//			Log.v(TAG, "heartRunnable");
			if(isContain())
			{
				if(TooltipMgr.getInstance().isChatBallAddWindow||TooltipMgr.getInstance().isChatUIAddWindow||TooltipMgr.getInstance().isAnimViewAddWindow)
				{
					
				}
				else
				{
					TooltipMgr.getInstance().onResume();
				}
			}
			else
			{
				TooltipMgr.getInstance().onPause();
			}
			mHandler.postDelayed(this, 1000L);
		}
	};
	
	private boolean isContain()
	{
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> mRunningTaskInfo = mActivityManager.getRunningTasks(1);
		String topActivityName=mRunningTaskInfo.get(0).topActivity.getClassName();
		boolean isContain=false;
		for(int i=0;i<mActivityList.size();i++)
		{
			isContain=mActivityList.contains(topActivityName);
			if(isContain)
			{
				break;
			}
		}
		return isContain;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.v(TAG, "onConfigurationChanged()");
		if(TooltipMgr.getInstance().getScreenWidth()!=TooltipMgr.getInstance().getWindowManager().getDefaultDisplay().getWidth())
		{
			TooltipMgr.getInstance().onConfigurationChanged();
		}
		super.onConfigurationChanged(newConfig);
	}
	
}
