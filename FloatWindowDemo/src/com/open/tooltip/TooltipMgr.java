package com.open.tooltip;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.open.data.ChatMsg;
import com.open.tooltip.anim.BallDragAnim;
import com.open.tooltip.anim.BallDragAnimTail;
import com.open.tooltip.anim.IAnimation;
import com.open.tooltip.anim.MessageDragAnim;
import com.open.tooltip.anim.TransAnim;
import com.open.tooltip.anim.TransAnimShadow;

/**
 * 管理布局
 * @author skyace
 *
 */
public class TooltipMgr {
	
	private final String TAG="TooltipMgr";
	
	public static final int STATUS_OPEN=1;//开始悬浮框
	public static final int STATUS_CLOSE=2;//关闭所有悬浮窗
	
	public static final int STATUS_MESSAGE_NULL2FULL=3;//无消息→有消息
	public static final int STATUS_MESSAGE_ADD=4;//有消息→添加了新消息
	public static final int STATUS_MESSAGE_FULL2NULL=5;//有消息→无消息
	
	public static final int STATUS_CHATWINDOW_OPEN=6;//点击小球,动画开始
	public static final int STATUS_CHATWINDOW_EXPAND=7;//有消息，展开聊天窗口
	public static final int STATUS_CHATWINDOW_DRAWBACK=8;//点击对话框，收缩
	public static final int STATUS_CHATWINDOW_CLOSE=9;//有消息，收缩
	public static final int STATUS_CHATWINDOW_DRAWBACK_MESSAGE_NULL=10;//点击对话框，收缩
	
	public static final int STATUS_BALL_DRAG=11;//拖拽小球
	public static final int STATUS_BALL_DRAG_END=12;//拖拽小球,松手
	public static final int STATUS_BALL_DRAG_END_BALL=13;//松手后，从新显示小球
	
	public static final int STATUS_RECRODING_START=14;//录音
	public static final int STATUS_RECRODING_STOP=15;//录音
	
	public static final int STATUS_MESSAGE_DRAG=16;//拖拽消息头像
	public static final int STATUS_MESSAGE_DRAG_DELETE_SUCCESS=17;//拖拽消息头像结束,并且删除成功
	public static final int STATUS_MESSAGE_DRAG_DELETE_FAILED=18;//拖拽消息头像结束，并且删除失败
	public static final int STATUS_MESSAGE_DRAG_FLASHVIEW_END=19;//拖拽消息头像结束
	
	public static final int REGISTER_TOOLTIP=100;//注册悬浮框，注册过才可以显示


	private WindowManager mWindowManager;
	
	private ChatBall mChatBall;//小球
	private ChatUI mChatUI;//聊天框
	private AnimSurfaceView mAnimSurfaceView;//小球动画
	private AudioRecordMicView mMicImageView;//麦克风
	
	private WindowManager.LayoutParams mChatBallParams;
	private WindowManager.LayoutParams mChatUIParams;
	private WindowManager.LayoutParams mAnimSurfaceViewParams;
	private WindowManager.LayoutParams mMicParams;
	
	public boolean isChatBallAddWindow=false;//小球是否添加了
	public boolean isChatUIAddWindow=false;//对话框是否添加了
	public boolean isAnimViewAddWindow=false;//动画View是否添加了
	
    private int statusBarHeight;//系统状态栏的高度 
    private int screenWidth;  
    private int screenHeight;  
    private int mBallWidth;  
    private int mBallHeight;
    public int []src=new int[]{0,0};//初始点
	private final int []des=new int[]{0,0};//左上角

	private BallDragAnim mBallDragAnim=null;
	private MessageDragAnim mMessageDragAnim=null;
	private ArrayList<DrawMessage> msgArrayList=new ArrayList<DrawMessage>(5);
	private Context mContext;
	private Handler mHandler;
	private static TooltipMgr instance;
	
	private TooltipMgr(){}
	
	public static TooltipMgr getInstance()
	{
		if(null==instance)
		{
			instance=new TooltipMgr();
		}
		return instance;
	}
	
	public void init(Context context)
	{
		if(null==mContext)
		{
			mContext=context.getApplicationContext();
			mWindowManager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			mHandler=new Handler(context.getMainLooper());
			
			Drawable ballIcon=context.getResources().getDrawable(R.drawable.tooltip_icon_nf);
			mBallWidth=ballIcon.getIntrinsicWidth();
			mBallHeight=ballIcon.getIntrinsicHeight();
			
			des[0]=mBallWidth/2;
			des[1]=mBallHeight/2;
			
			src[0]=getScreenWidth()-mBallWidth/2;
			src[1]=(getScreenHeight()-getStatusBarHeight(context))/2;
			
			getChatBallParam(context);
			getChatUIParam(context);
		}
	}
	
	public WindowManager getWindowManager()
	{
		return mWindowManager;
	}
	
    public int getStatusBarHeight(Context context) 
    {  
        if (statusBarHeight == 0) 
        {  
            try {  
                Class<?> c = Class.forName("com.android.internal.R$dimen");  
                Object o = c.newInstance();  
                Field field = c.getField("status_bar_height");  
                int x = (Integer) field.get(o);  
                statusBarHeight = context.getResources().getDimensionPixelSize(x);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return statusBarHeight;  
    } 
    
    public int getScreenWidth()
    {
		if(screenWidth==0)
		{
			screenWidth =mWindowManager.getDefaultDisplay().getWidth();
			screenHeight=mWindowManager.getDefaultDisplay().getHeight();
		}
    	return screenWidth;
    }
    
    public int getScreenHeight()
    {
    	if(screenHeight==0)
    	{
    		screenWidth =mWindowManager.getDefaultDisplay().getWidth();
    		screenHeight=mWindowManager.getDefaultDisplay().getHeight();
    	}
    	return screenHeight;
    }
    
    public int getBallWidth()
    {
    	return mBallWidth;
    }
    
    public int getBallHeight()
    {
    	return mBallHeight;
    }
    
    public ArrayList<DrawMessage> getMessageList()
    {
    	return msgArrayList;
    }
    
	public AnimSurfaceView getWindowFlashBall()
	{
		return mAnimSurfaceView;
	}

	private int _lastX;
	private int _lastY;
	public void updataChatBall(MotionEvent event) 
	{
		if(ToolTipConfig.isUseSurfaceView)
		{
			if(null!=mBallDragAnim)
			{
				mBallDragAnim.reflesh(event);
			}
		}
		else
		{
			int _newlastX=(int) event.getRawX();
			int _newlastY=(int) event.getRawY()-getStatusBarHeight(mContext);
			boolean isDragEnd=(event.getAction()==MotionEvent.ACTION_UP)||(event.getAction()==MotionEvent.ACTION_CANCEL);
			if(Math.abs(_lastX-_newlastX)>8||Math.abs(_lastY-_newlastY)>8)
			{
				_lastX=_newlastX;
				_lastY=_newlastY;
				
				mChatBallParams.x = _lastX-des[0];
				mChatBallParams.y = _lastY-des[1];
				mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
			}
			
			if(isDragEnd)
			{
				_lastX=_newlastX;
				_lastY=_newlastY;
				
				int []src=new int[]{_lastX,_lastY};
				final int []des=new int[]{0,0};
				
				_lastX=(_lastX<=(TooltipMgr.getInstance().getScreenWidth())/2)?TooltipMgr.getInstance().getBallWidth()/2:TooltipMgr.getInstance().getScreenWidth()-TooltipMgr.getInstance().getBallWidth()/2;
				if(_lastY<TooltipMgr.getInstance().getBallHeight()/2)
				{
					_lastY=TooltipMgr.getInstance().getBallHeight()/2;
				}
				else if(_lastY>TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2)
				{
					_lastY=TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2;
				}
				
				des[0]=_lastX;
				des[1]=_lastY;
				
				if(null!=trimRunnable)
				{
					trimRunnable.stopRun();
				}
				trimRunnable=new TransAnimRunnable(src, des,200,new Runnable() {
					
					@Override
					public void run() {
						
						mChatBallParams.x = des[0]-TooltipMgr.this.des[0];
						mChatBallParams.y = des[1]-TooltipMgr.this.des[1];
						
						mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
						TooltipMgr.this.src=des;
					}
				});
				mHandler.post(trimRunnable);
			}
		}
	}

	public void updateDraggedMessage(MotionEvent event) 
	{
		if(null!=mMessageDragAnim)
		{
			mMessageDragAnim.reflesh(event);
		}
	}

	public boolean isDraggedMessageDelete(MotionEvent event)
	{
		if(null!=mMessageDragAnim)
		{
			return mMessageDragAnim.isDelete(event);
		}
		return false;
	}
	
	private HashMap<String, SoftReference<Bitmap>> cache=new HashMap<String, SoftReference<Bitmap>>();
	public Bitmap[] getHeadBitmaps()
	{
		int size=Math.min(3, msgArrayList.size());
		Bitmap[] ret=new Bitmap[size];
		Bitmap bmp;
		for(int i=0;i<ret.length;i++)
		{
			String url=msgArrayList.get(i).msgList.get(0).getAvatar();
			
			SoftReference<Bitmap> ref = cache.get(url);
            if ( ref != null ) 
            {
            	bmp =ref.get();
                if (bmp == null)
                {
                	cache.remove(url);
                }
                else
                {
                	cache.put(url, new SoftReference<Bitmap>(bmp));
                	ret[i]=bmp;
                	continue;
                }
            }
			 ret[i]=getFromAssetBitmap(msgArrayList.get(i).msgList.get(0).getAvatar());
			if(null!=ret[i])
			{
				cache.put(url, new SoftReference<Bitmap>(ret[i]));
			}
		}
		return ret;
	}
	
	public Bitmap getBitmap(String url)
	{
		Bitmap bmp=null;
		SoftReference<Bitmap> ref = cache.get(url);
        if ( ref != null ) 
        {
        	bmp =ref.get();
            if (bmp == null)
            {
            	cache.remove(url);
            }
            else
            {
            	cache.put(url, new SoftReference<Bitmap>(bmp));
            	return bmp;
            }
        }
        bmp=getFromAssetBitmap(url);
        
		if(null!=bmp)
		{
			cache.put(url, new SoftReference<Bitmap>(bmp));
		}
		return bmp;
	}
	
	public Bitmap getFromAssetBitmap(String fileName){ 
         try { 
              return BitmapFactory.decodeStream(mContext.getResources().getAssets().open(fileName));
         } catch (Exception e) { 
             e.printStackTrace(); 
         }
         return null;
	 } 
	
	@SuppressLint("DefaultLocale")
	public Bitmap getCircleBitmap(int width,int height)
	{
		String url=String.format("image:width:%d_height:%d",width,height);
		Bitmap bmp=null;
		SoftReference<Bitmap> ref = cache.get(url);
        if ( ref != null ) 
        {
        	bmp =ref.get();
            if (bmp == null)
            {
            	cache.remove(url);
            }
            else
            {
            	cache.put(url, new SoftReference<Bitmap>(bmp));
            	return bmp;
            }
        }
        bmp=makeDst(width, height);
		if(null!=bmp)
		{
			cache.put(url, new SoftReference<Bitmap>(bmp));
		}
		return bmp;
	}
	
	private Bitmap makeDst(int w, int h) 
    {
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.parseColor("#ffffffff"));   
        c.drawOval(new RectF(0, 0, w, h), p);
        return bm;
    }
	
	public void updateData(Bundle mBundle)
	{
		ChatMsg msg=(ChatMsg)mBundle.getSerializable("data");
		
		boolean isFind=false;
		for(int i=0;i<msgArrayList.size();i++)
		{
			if(msgArrayList.get(i).uid==msg.getFriendId())
			{
				msgArrayList.get(i).msgList.add(msg);
				msgArrayList.get(i).unReadCount++;
				isFind=true;
				break;
			}
		}
		
		if(!isFind)
		{
			DrawMessage dm=new DrawMessage();
			dm.msgList.add(msg);
			dm.uid=msg.getFriendId();
			dm.unReadCount++;
			msgArrayList.add(0,dm);
		}
	}
	
	public void updateUI(final Context context,final int status,final Bundle mBundle)
	{
Log.v(TAG,"updateUI status:"+status);

		mHandler.post(new Runnable(){

			@Override
			public void run() {
				
				if(status==STATUS_OPEN)
				{
					hideChatUI();
					hideAnimView();
					showChatBall(context);
					mChatBall.setImageResource(R.drawable.tooltip_icon_f);
				}
				else if(status==STATUS_MESSAGE_NULL2FULL)
				{
					hideChatUI();
					hideAnimView();
					showChatBall(context);
					mChatBall.setImageResource(R.drawable.tooltip_icon_f);
				}
				else if(status==STATUS_MESSAGE_ADD)
				{
					if(isChatUIAddWindow)
					{
						showChatUI(context);
					}
					else if(!isChatBallAddWindow)
					{
						showChatBall(context);
					}
					else if(isChatBallAddWindow)
					{
						mChatBall.setImageResource(R.drawable.tooltip_icon_f);
						if(null!=mVirRunnable)
						{
							mVirRunnable.stopRun();
						}
						mVirRunnable=new VirRunnable();
						mHandler.post(mVirRunnable);
					}
				}
				else if(status==STATUS_MESSAGE_FULL2NULL)
				{
					
				}
				else if(status==STATUS_CHATWINDOW_OPEN)//点击小球，开始动画OK
				{
					if(ToolTipConfig.isUseSurfaceView)
					{
						hideChatBall();
						hideChatUI();
						
						IAnimation anim=ToolTipConfig.isAnimWithShadow?new TransAnimShadow(context, src, des, status, 300, getHeadBitmaps()):new TransAnim(context, src, des, status, 300);
						showAnimView(context, anim);
					}
					else
					{
						hideChatUI();
						if(null!=trimRunnable)
						{
							trimRunnable.stopRun();
						}
						trimRunnable=new TransAnimRunnable(src, des,showChatUIRunnable);
						mHandler.post(trimRunnable);
					}
				}
				else if(status==STATUS_CHATWINDOW_EXPAND)//小球动画完毕，显示聊天窗口 OK
				{
					hideChatBall();
					hideAnimView();
					
					showChatUI(context);
				}
				else if(status==STATUS_CHATWINDOW_DRAWBACK||status==STATUS_CHATWINDOW_DRAWBACK_MESSAGE_NULL)//点击聊天窗口下半部分，收缩 OK
				{
					if(ToolTipConfig.isUseSurfaceView)
					{
						hideAnimView();
						hideChatBall();
						hideChatUI();
						
						mChatBall.setImageResource(R.drawable.tooltip_icon_nf);
						IAnimation anim=ToolTipConfig.isAnimWithShadow?new TransAnimShadow(context, des, src, status, 300, getHeadBitmaps()):new TransAnim(context, des, src, status, 300);
						showAnimView(context, anim);
					}
					else
					{
						hideAnimView();
						hideChatUI();
						showChatBall(mContext);
						mChatBall.setImageResource(R.drawable.tooltip_icon_nf);
						
						if(null!=trimRunnable)
						{
							trimRunnable.stopRun();
						}
						trimRunnable=new TransAnimRunnable(des, src,showChatBallRunnable);
						mHandler.post(trimRunnable);
					}
				}
				else if(status==STATUS_CHATWINDOW_CLOSE)//收缩动画完成，显示原来位置OK
				{
					hideChatUI();
					hideAnimView();
					
					showChatBall(context);
				}
				else if(status==STATUS_BALL_DRAG)//拖动小球OK
				{
					hideChatBallInvisible();
					hideChatUI();
					
					mBallDragAnim=ToolTipConfig.isAnimWithShadow?new BallDragAnimTail(context, getHeadBitmaps()):new BallDragAnim(context);
					showAnimView(context, mBallDragAnim);
				}
				else if(status==STATUS_BALL_DRAG_END_BALL)//拖动小球结束OK
				{
					hideAnimView();
					
					int []_src=mBundle.getIntArray("data");//重置位置
					retSettingBall(context, _src);
					showChatBall(context);
				}
				else if(status==STATUS_RECRODING_START)
				{
					showMicView(context);
				}
				else if(status==STATUS_RECRODING_STOP)
				{
					hideMicView();
				}
				else if(status==STATUS_MESSAGE_DRAG)//OK
				{
					int []viewLocation=mBundle.getIntArray("data");
					String headPath=mBundle.getString("data1");
					mMessageDragAnim=new MessageDragAnim(context, getScreenWidth(), getScreenHeight()-getStatusBarHeight(context),viewLocation, getBitmap(headPath));
					showAnimView(context, mMessageDragAnim);
				}
				else if(status==STATUS_MESSAGE_DRAG_FLASHVIEW_END)//OK
				{
					hideAnimView();
				}
				else if(status==STATUS_CLOSE)
				{
					closeWindows();
				}
			}
		});

	}
	
	private WindowManager.LayoutParams getChatBallParam(Context context)
	{
		if(null==mChatBallParams)
		{
			mChatBallParams=new WindowManager.LayoutParams();
			mChatBallParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mChatBallParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
			mChatBallParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

			mChatBallParams.width=getBallWidth();
			mChatBallParams.height=getBallHeight();
			mChatBallParams.gravity=Gravity.LEFT| Gravity.TOP; // 调整悬浮窗口至右侧中间
			mChatBallParams.x=src[0]-des[0];  
			mChatBallParams.y=src[1]-des[1];
		}
		return mChatBallParams;
	}
	
	private WindowManager.LayoutParams getChatUIParam(Context context)
	{
		if(null==mChatUIParams)
		{
			mChatUIParams=new WindowManager.LayoutParams();
			mChatUIParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mChatUIParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
			mChatUIParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
			mChatUIParams.width=WindowManager.LayoutParams.MATCH_PARENT;
			mChatUIParams.height=WindowManager.LayoutParams.MATCH_PARENT;
			mChatUIParams.gravity=Gravity.LEFT| Gravity.TOP; // 调整悬浮窗口至右侧中间
			mChatUIParams.x=0;  
			mChatUIParams.y=0;  
		}
		return mChatUIParams;
	}
	
	private WindowManager.LayoutParams getAnimSurfaceViewParam(Context context)
	{
		if(null==mAnimSurfaceViewParams)
		{
			mAnimSurfaceViewParams=new WindowManager.LayoutParams();
			mAnimSurfaceViewParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mAnimSurfaceViewParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
			mAnimSurfaceViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			mAnimSurfaceViewParams.width=WindowManager.LayoutParams.MATCH_PARENT;
			mAnimSurfaceViewParams.height=WindowManager.LayoutParams.MATCH_PARENT;
			mAnimSurfaceViewParams.gravity=Gravity.LEFT| Gravity.TOP; // 调整悬浮窗口至右侧中间
			mAnimSurfaceViewParams.x=0;  
			mAnimSurfaceViewParams.y=0; 
		}
		return mAnimSurfaceViewParams;
	}
	
	private WindowManager.LayoutParams getMicParam(Context context)
	{
		if(null==mMicParams)
		{
			mMicParams=new WindowManager.LayoutParams();
			mMicParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			mMicParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
			mMicParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			mMicParams.width=context.getResources().getDrawable(R.drawable.tooltip_mic_bottom).getIntrinsicWidth();
			mMicParams.height=context.getResources().getDrawable(R.drawable.tooltip_mic_bottom).getIntrinsicHeight();
			mMicParams.x = 0;
			mMicParams.y = 0;
		}
		return mMicParams;
	}
	
	private void showChatBall(Context context)
	{
		if(null==mChatBall)
		{
			mChatBall=new ChatBall(context);
		}
		
		if(null==mChatUI)
		{
			mChatUI=new ChatUI(context);
		}
			
		if(mChatBall.getVisibility()!=View.VISIBLE)
		{
			mChatBall.setVisibility(View.VISIBLE);
		}
		
		if(!isChatBallAddWindow)
		{
			mChatBallParams=getChatBallParam(context);
			mWindowManager.addView(mChatBall, mChatBallParams);
			isChatBallAddWindow=true;
		}
	}
	
	private void showChatUI(Context context)
	{
		if(null==mChatUI)
		{
			mChatUI=new ChatUI(context);
		}
		mChatUI.onUIupdate();
		
		if(!isChatUIAddWindow)
		{
			mChatUIParams=getChatUIParam(context);
			mWindowManager.addView(mChatUI, mChatUIParams);
			isChatUIAddWindow=true;
		}
	}
	
	private void showAnimView(Context context,IAnimation anim)
	{
		if(null==mAnimSurfaceView)
		{
			mAnimSurfaceView=new AnimSurfaceView(context);
		}
		
		if(!isAnimViewAddWindow)
		{
			mAnimSurfaceViewParams=getAnimSurfaceViewParam(context);
			mWindowManager.addView(mAnimSurfaceView, mAnimSurfaceViewParams);
			isAnimViewAddWindow=true;
		}
		
		mAnimSurfaceView.postAnimation(anim);
	}
	
	private void showMicView(Context context)
	{
		if(null==mMicImageView)
		{
			mMicImageView=new AudioRecordMicView(context);
		}
		mWindowManager.addView(mMicImageView, getMicParam(context));
	}
	
	private void retSettingBall(Context context,int [] loc)
	{
		
		loc[0]=(loc[0]<=(TooltipMgr.getInstance().getScreenWidth())/2)?TooltipMgr.getInstance().getBallWidth()/2:TooltipMgr.getInstance().getScreenWidth()-TooltipMgr.getInstance().getBallWidth()/2;
		if(loc[1]<TooltipMgr.getInstance().getBallHeight()/2)
		{
			loc[1]=TooltipMgr.getInstance().getBallHeight()/2;
		}
		else if(loc[1]>TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2)
		{
			loc[1]=TooltipMgr.getInstance().getScreenHeight()-TooltipMgr.getInstance().getBallHeight()/2;
		}
		
		mChatBallParams.x=loc[0]-des[0];
		mChatBallParams.y= (loc[1]-des[1]);
		
		mChatBall.setVisibility(View.VISIBLE);
		mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
		isChatBallAddWindow=true;
		
		src=loc;
	}
	
	private void hideChatBallInvisible()
	{
		mChatBall.setVisibility(View.INVISIBLE);
	}
	
	private void hideChatBall()
	{
		if(isChatBallAddWindow)
		{
			if(null!=mChatBall)
			{
				mWindowManager.removeView(mChatBall);
			}
			isChatBallAddWindow=false;
		}
	}
	
	private void hideChatUI()
	{
		if(isChatUIAddWindow)
		{
			if(null!=mChatUI)
			{
				mWindowManager.removeView(mChatUI);
			}
			isChatUIAddWindow=false;
		}
	}
	
	private void hideAnimView()
	{
		if(isAnimViewAddWindow)
		{
			if(null!=mAnimSurfaceView)
			{
				mWindowManager.removeView(mAnimSurfaceView);
			}
			isAnimViewAddWindow=false;
		}
	}
	
	private void hideMicView()
	{
		if(null!=mMicImageView)
		{
			mWindowManager.removeView(mMicImageView);
			mMicImageView=null;
		}
	}
	
	public void closeWindows()
	{
		hideChatBall();
		hideChatUI();
		hideAnimView();
		hideMicView();
		
		mChatBall=null;
		mChatUI=null;
		mAnimSurfaceView=null;
		mMicImageView=null;
		
		msgArrayList.clear();
	}
	
	public void onResume()
	{
		if(status.isChatBallAddWindow)
		{
			hideChatUI();
			hideAnimView();
			
			showChatBall(mContext);
		}
		else if(status.isChatUIAddWindow)
		{
			hideChatBall();
			hideAnimView();
			
			showChatUI(mContext);
		}
		else
		{
			hideChatUI();
			hideAnimView();
			
			if(getMessageList().size()>0)
			{
				showChatBall(mContext);
			}
		}
	}
	
	public void onPause()
	{
		if(isChatBallAddWindow||isChatUIAddWindow||isAnimViewAddWindow)
		{
			status.isChatBallAddWindow=isChatBallAddWindow;
			status.isChatUIAddWindow=isChatUIAddWindow;
			status.isAnimViewAddWindow=isAnimViewAddWindow;
		
			hideChatBall();
			hideChatUI();
			hideAnimView();
			
			isChatBallAddWindow=false;
			isChatUIAddWindow=false;
			isAnimViewAddWindow=false;
		}

	}
	
    public void onConfigurationChanged()
    {
    	float oldHPrecent=(float)src[1]/(float)(screenHeight-getStatusBarHeight(mContext));
    	
    	int tmp=screenWidth;
    	screenWidth=screenHeight;
    	screenHeight=tmp;
    	
    	if(src[0]>getBallWidth()/2)//在右边
    	{
    		src[0]=screenWidth-getBallWidth()/2;
    		getChatBallParam(mContext).x=src[0]-des[0];  
    	}
    	src[1]=(int)(oldHPrecent*(screenHeight-getStatusBarHeight(mContext)));
    	getChatBallParam(mContext).y=src[1]-des[1];  
    	
    	if(isChatBallAddWindow)
    	{
    		mWindowManager.updateViewLayout(mChatBall,getChatBallParam(mContext));
    	}
    	
    	Log.v(TAG,"onConfigurationChanged sWidth:"+screenWidth+" sHeight:"+screenHeight);
    	
    	//重新设置Src的值，与parms的值
    	
    	if(null!=mChatUI)
    	mChatUI.onUIupdate();
    }
	
    private VirRunnable mVirRunnable;
	private TransAnimRunnable trimRunnable;
	private Runnable showChatUIRunnable=new Runnable() {
		
		@Override
		public void run() {
			hideChatBall();
			showChatUI(mContext);
		}
	};
	private Runnable showChatBallRunnable=new Runnable() {
		
		@Override
		public void run() {
			
			if(getMessageList().size()==0)
			{
				hideChatBall();
			}
		}
	};
	private TooltipState status=new TooltipState();
	
	public class DrawMessage
	{
		public long uid;
		public ArrayList<ChatMsg> msgList=new ArrayList<ChatMsg>(1);
		public int unReadCount;
	}
	
	private class TooltipState
	{
		public boolean isChatBallAddWindow=false;//小球是否添加了
		public boolean isChatUIAddWindow=false;//对话框是否添加了
		public boolean isAnimViewAddWindow=false;//动画View是否添加了
	}
	
	private class VirRunnable implements Runnable
	{
		final long end;
		final int oldX;
		long run;
		private int amplitude;
		
		public VirRunnable()
		{
			end=System.currentTimeMillis()+500;
			oldX=(mChatBallParams.x=(src[0]<=(getScreenWidth())/2)?0:getScreenWidth()-getBallWidth());
			amplitude=DensityUtil.dip2px(mContext, 25);
		}
		
		public void stopRun()
		{
			mHandler.removeCallbacks(this);
			getChatBallParam(mContext).x=oldX;
			mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
		}
		
		@Override
		public void run() {
			
			if((run=(end-System.currentTimeMillis()))>0)
			{
				int value=(int) (amplitude*Math.sin((float)4*Math.PI*(500-run)/(float)500));
				if(oldX>0&&value<0||oldX<=0&&value>0)
				{
					value=(-value);
				}
				
				getChatBallParam(mContext).x=oldX-value;
				mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
				mHandler.postDelayed(this, 20);
			}
			else
			{
				mHandler.removeCallbacks(this);
				getChatBallParam(mContext).x=oldX;
				mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
			}
		}
	}
	
	private class TransAnimRunnable implements Runnable
	{
		private int dx;
		private int dy;
		private int duration=300;
		private long start;
		private long runMills=0;
		private int[] srcLoc;
		private int[] desLoc;
		private Runnable run;
		
		public TransAnimRunnable(int[] srcLoc, int[] desLoc, Runnable run) {
			this(srcLoc, desLoc, 300, run);
		}
		
		public TransAnimRunnable(int[] srcLoc, int[] desLoc, int duration ,Runnable run) {
			this.srcLoc=srcLoc;
			this.desLoc=desLoc;
			dx=desLoc[0]-srcLoc[0];
			dy=desLoc[1]-srcLoc[1];
			start=System.currentTimeMillis();
			this.duration=duration;
			this.run = run;
		}

		public void stopRun()
		{
			mHandler.removeCallbacks(this);
		}
		
		@Override
		public void run() {
			
			if((runMills=(System.currentTimeMillis()-start))<duration)
			{
				int _dx=(int)((float)runMills*dx/(float)duration);
				int _dy=(int)((float)runMills*dy/(float)duration);
				
				mChatBallParams.x = (srcLoc[0]+_dx)-des[0];
				mChatBallParams.y = (srcLoc[1]+_dy)-des[1];
				mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
				
				mHandler.postDelayed(this, 20);
			}
			else
			{
				mChatBallParams.x = desLoc[0]-des[0];
				mChatBallParams.y = desLoc[1]-des[1];
				mWindowManager.updateViewLayout(mChatBall,mChatBallParams);
				
				if(null!=run)
				{
					run.run();
				}
				mHandler.removeCallbacks(this);
			}
		}
	}
}
