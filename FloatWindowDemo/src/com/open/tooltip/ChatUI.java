package com.open.tooltip;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.open.data.ChatMsg;
import com.open.tooltip.ExListView.IonTouch;
import com.open.tooltip.HorImageListView.IHeadClick;
import com.open.tooltip.TooltipMgr.DrawMessage;

/**
 * 聊天对话框
 * @author skyace
 *
 */
public class ChatUI extends ExRelativeLayout {

	private ExRelativeLayout mContentView;
	private HorImageListView horImageListView;//头像区域
	
	private LinearLayout msgAreaLayout;
	private ExLinearLayout mXLinearLayout;
	private ExListView mExListView;
	private EditText inputEditText;//输入框区域
	private AudioPlayViewLeft audioPlayBtn;//语音播放
	private AudioRecordViewRight recordingProgressBar;//录音进度条
	
	private AudioRecordMicView mMicImageView;//麦克风
	
	private RelativeLayout areaInputLayout;//回复区域
	private View recordingBtn;//录音按钮
	private View sendBtn;//发送按钮
	private View keyBroadBtn;//键盘切换
	private ImageView emBtn;
	
	private Handler mHandler=new Handler();
	
	public ChatUI(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);		
	}

	public ChatUI(Context context) {
		super(context);
		init(context);		
	}
	
	private void init(Context context)
	{
		LayoutInflater inflater = LayoutInflater.from(context);  
		mContentView=(ExRelativeLayout)inflater.inflate(R.layout.tooltip_chatwindow, this);
		horImageListView=(HorImageListView)(mContentView.findViewById(R.id.horListview));
		horImageListView.setOnHeadClickListener(headClickListener);
		msgAreaLayout=(LinearLayout)findViewById(R.id.msgAreaLayout);
		
		mMicImageView=(AudioRecordMicView)findViewById(R.id.mic);
		mMicImageView.setVisibility(View.GONE);
		
		this.setOnSizeChangeListener(new IOnSizeChangeListener() {
			
			@Override
			public void onSizeChange(int state) {
				if(state==ExRelativeLayout.KeyBoard_VISIBLE)
				{

				}
			}
		});
		
		//聊天输入框的监听
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction())
				{
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						int dis=msgAreaLayout.getBottom()+DensityUtil.dip2px(getContext(), 50);
						if(event.getRawY()>dis)
						{
							if(mContentView.getKeyBoardState()==ExRelativeLayout.KeyBoard_VISIBLE)
							{
								InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
								mContentView.reSetKeyBoradState();
								return true;
							}
							else
							{
								TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_CHATWINDOW_DRAWBACK, null);
							}
						}
						
						break;
				}
				return true;
			}
		});
		
		onUIupdate();
	}
	
	public void onUIupdate()
	{
		if(TooltipMgr.getInstance().getMessageList().size()<=0)
		{
			TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_CHATWINDOW_DRAWBACK_MESSAGE_NULL, null);
		}
		else
		{
			horImageListView.setAdapter(TooltipMgr.getInstance().getMessageList());
			onUIupdateInner(TooltipMgr.getInstance().getMessageList().get(0));
		}
	}
	
	private void onUIupdateInner(DrawMessage mDrawMessage)
	{
		if(null==mXLinearLayout)
		{
			mXLinearLayout=(ExLinearLayout) findViewById(R.id.parent);
			mExListView=(ExListView) msgAreaLayout.findViewById(R.id.listview);
			areaInputLayout=(RelativeLayout) msgAreaLayout.findViewById(R.id.areaEditText);
			recordingProgressBar=(AudioRecordViewRight) msgAreaLayout.findViewById(R.id.recordingProgressBar);
			recordingBtn=msgAreaLayout.findViewById(R.id.audioBtn);
			inputEditText=(EditText) msgAreaLayout.findViewById(R.id.inputEditText);
			
			inputEditText.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_UP)
					{
						
					}
					return false;
				}
			});
			
			mExListView.setOnSelfTouchListener(new IonTouch() {
				
				@Override
				public void onTouchHandle() {
					if(mXLinearLayout.getPaddingTop()<0)
					{
						if(null!=mSmoothRunnable)
						{
							mSmoothRunnable.stop();
						}
						mSmoothRunnable=new SmoothRunnable(mXLinearLayout.getPaddingTop(), 0);
						mHandler.post(mSmoothRunnable);
					}
				}
			});
			
			keyBroadBtn=msgAreaLayout.findViewById(R.id.keyBroadBtn);
			keyBroadBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					areaInputLayout.setVisibility(View.VISIBLE);
					recordingProgressBar.setVisibility(View.GONE);
					keyBroadBtn.setVisibility(View.GONE);
					sendBtn.setVisibility(View.VISIBLE);
					
					mHandler.post(new SmoothRunnable(0, -DensityUtil.dip2px(getContext(), 45)));
				}
			});
			
			sendBtn=msgAreaLayout.findViewById(R.id.sendBtn);
			sendBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//发送操作
					String content = inputEditText.getText().toString();
					if (!TextUtils.isEmpty(content)) 
					{
//						sendText(content);
						
						inputEditText.setText("");
						keyBroadBtn.setVisibility(View.VISIBLE);
						sendBtn.setVisibility(View.GONE);
						
						if(null!=mSmoothRunnable)
						{
							mSmoothRunnable.stop();
						}
						mSmoothRunnable=new SmoothRunnable(mXLinearLayout.getPaddingTop(), 0);
						mHandler.post(mSmoothRunnable);
					}
					else
					{
						Toast.makeText(getContext(), "发送内容不能为空", Toast.LENGTH_LONG).show();
					}
					
				}
			});
			
			emBtn=(ImageView)msgAreaLayout.findViewById(R.id.emBtn);
			emBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

				}
			});
			
			recordingBtn.setOnTouchListener(audioOnTouchListener);
		}
		
		keyBroadBtn.setVisibility(View.VISIBLE);
		sendBtn.setVisibility(View.GONE);
		recordingProgressBar.setProgress(0);
		msgAreaLayout.setTag(mDrawMessage);
		mXLinearLayout.setPadding(0, 0, 0, 0);
		mExListView.setAdapter(new MessageAdapter(getContext(),mDrawMessage.msgList));
	}
	
	private SmoothRunnable mSmoothRunnable=null;
	private class SmoothRunnable implements Runnable
	{
		private int startPaddingTop;
		private int endPaddingTop;
		private long duration=70;
		private long interval=15;
		private long startTime;
		private long endTime;
		
		public SmoothRunnable(int startPaddingTop, int endPaddingTop) {
			this.startPaddingTop = startPaddingTop;
			this.endPaddingTop = endPaddingTop;
		}

		public void stop()
		{
			mHandler.removeCallbacks(this);
		}
		
		@Override
		public void run() {
			if(startTime==0)
			{
				startTime=System.currentTimeMillis();
				endTime=startTime+duration;
			}
			
			long currentTime=System.currentTimeMillis();
			if(currentTime<endTime)
			{
				int mPaddingTop=(int)(startPaddingTop+(endPaddingTop-startPaddingTop)*((float)(currentTime-startTime)/(float)(duration)));
				mXLinearLayout.setPadding(0, mPaddingTop, 0, 0);
				mHandler.postDelayed(this, interval);
			}
			else
			{
				mXLinearLayout.setPadding(0, endPaddingTop, 0, 0);
				if(endPaddingTop==0)
				{
					keyBroadBtn.setVisibility(View.VISIBLE);
					sendBtn.setVisibility(View.GONE);
				}
				mHandler.post(new Runnable() {
			        @Override
			        public void run() {
			            // Select the last row so it will scroll into view...
			        	mExListView.setSelection(mExListView.getCount() - 1);
			        }
			    });
			}
		}
	} 
	
	private OnTouchListener audioOnTouchListener=new OnTouchListener() {
		Rect mViewRect=new Rect();
		boolean isMoveOut=false;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundResource(R.drawable.tooltip_audio_f);
					areaInputLayout.setVisibility(View.GONE);
					
					mMicImageView.setTag(msgAreaLayout.getTag());
					mMicImageView.setVisibility(View.VISIBLE);
					
					recordingProgressBar.setProgress(0);
					recordingProgressBar.setVisibility(View.VISIBLE);
					if(mXLinearLayout.getPaddingTop()<0)
					{
//						mXLinearLayout.setPadding(0, 0, 0, 0);
					}
					else
					{
						mHandler.post(new SmoothRunnable(0, -DensityUtil.dip2px(getContext(), 45)));
					}
					
					
					isMoveOut=false;
					int[] location = new int[2];
					v.getLocationOnScreen(location);
					mViewRect.set(location[0], location[1], location[0]+v.getWidth(), location[1]+v.getHeight());
					
					break;
					
				case MotionEvent.ACTION_MOVE:
					
					int _lastX = (int) event.getRawX();
					int _lastY = (int) event.getRawY();
					
					if(!isMoveOut)
					{
						if(!mViewRect.contains(_lastX, _lastY))
						{
							isMoveOut=true;
							mMicImageView.setMoveOut(isMoveOut);
						}
					}
					else
					{
						if(mViewRect.contains(_lastX, _lastY))
						{
							isMoveOut=false;
							mMicImageView.setMoveOut(isMoveOut);
						}
					}
					break;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					v.setBackgroundResource(R.drawable.tooltip_audio_nf);
					_lastX = (int) event.getRawX();
					_lastY = (int) event.getRawY();
					
					if(!isMoveOut)
					{
						if(!mViewRect.contains(_lastX, _lastY))
						{
							isMoveOut=true;
							mMicImageView.setMoveOut(isMoveOut);
						}
					}
					else
					{
						if(mViewRect.contains(_lastX, _lastY))
						{
							isMoveOut=false;
							mMicImageView.setMoveOut(isMoveOut);
						}
					}
					
			    	mMicImageView.setVisibility(View.GONE);
			    	mMicImageView.setMoveOut(false);
			    	
					mSmoothRunnable=new SmoothRunnable(mXLinearLayout.getPaddingTop(), 0);
					mHandler.post(mSmoothRunnable);
					break;
			}
			return true;
		}
	};
	
	public IHeadClick headClickListener=new IHeadClick(){

		@Override
		public void onItemClick(int position) {
			
			onUIupdateInner(TooltipMgr.getInstance().getMessageList().get(0));
		}
	};
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) 
	{
		Log.v("ChatUI", "dispatchKeyEvent " + event.getKeyCode()+"mContentView.getKeyBoardState():"+mContentView.getKeyBoardState());
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0) 
		{
			if(mContentView.getKeyBoardState()==ExRelativeLayout.KeyBoard_VISIBLE)
			{
				InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
				mContentView.reSetKeyBoradState();
				return true;
			}
			else
			{
				TooltipMgr.getInstance().updateUI(getContext(), TooltipMgr.STATUS_CHATWINDOW_DRAWBACK, null);
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	public class MessageAdapter extends BaseAdapter
	{
		public ArrayList<ChatMsg> msgList;
		public LayoutInflater mLayoutInflater;
		public MessageAdapter(Context context,ArrayList<ChatMsg> msgList)
		{
			this.msgList=msgList;
			this.mLayoutInflater=LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return msgList.size();
		}

		@Override
		public Object getItem(int position) {
			return msgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = null;
			final ChatMsg msg=msgList.get(position);
			if(msg.getContentType()==ChatMsg.TYPE_TEXT)
			{
				view=mLayoutInflater.inflate(R.layout.tooltip_chatwindow_rec_text, null);
				((EditText)view.findViewById(R.id.rectText)).setText(msg.getMsgContent());
			}
			else if(msg.getContentType()==ChatMsg.TYPE_VOICE)
			{
				view=mLayoutInflater.inflate(R.layout.tooltip_chatwindow_rec_audio, null);
				final AudioPlayViewLeft _audioPlayBtn=((AudioPlayViewLeft)view.findViewById(R.id.rProgressBar));
				_audioPlayBtn.setTag(msg);
				_audioPlayBtn.setMaxLength(60);
				_audioPlayBtn.setLength(10);
				_audioPlayBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(null!=audioPlayBtn)
						{
							audioPlayBtn.playingStop();
						}
						audioPlayBtn=_audioPlayBtn;
					}
				});
			}
			return view;
		}
	}
}

