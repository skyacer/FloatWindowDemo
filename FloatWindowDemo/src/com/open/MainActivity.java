package com.open;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioGroup;

import com.open.data.ChatMsg;
import com.open.tooltip.R;
import com.open.tooltip.ToolTipConfig;
import com.open.tooltip.TooltipUtil;

public class MainActivity extends Activity {
	
	private ArrayList<String> headList=new ArrayList<String>();
	{
		headList.add("1.jpg");
		headList.add("2.jpg");
		headList.add("3.jpg");
		headList.add("4.jpg");
		headList.add("5.jpg");
		headList.add("6.jpg");
		headList.add("7.jpg");
		headList.add("8.jpg");
		headList.add("9.jpg");
		headList.add("10.jpg");
		headList.add("11.jpg");		
		headList.add("12.jpg");
		headList.add("13.jpg");
		headList.add("14.jpg");
		headList.add("15.jpg");
		headList.add("16.jpg");
		headList.add("17.jpg");
		headList.add("18.jpg");
		headList.add("19.jpg");
		headList.add("20.jpg");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MainActivity", "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
	}
		
	
	@Override
	protected void onDestroy() {
		Log.v("MainActivity", "onDestroy");
		super.onDestroy();
	}



	private void initView()
	{
		findViewById(R.id.regTooltip).setOnClickListener(listener);
		findViewById(R.id.recMessage).setOnClickListener(listener);
		findViewById(R.id.closeTooltip).setOnClickListener(listener);
		((RadioGroup)findViewById(R.id.radiogroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            
          @Override  
          public void onCheckedChanged(RadioGroup group, int checkedId) {  
              if(checkedId==R.id.radiobutton1)  
              {  
            	  ToolTipConfig.isUseSurfaceView=false;
            	  ToolTipConfig.isAnimWithShadow=false;
              }
              else if(checkedId==R.id.radiobutton2)  
              {  
            	  ToolTipConfig.isUseSurfaceView=true;
            	  ToolTipConfig.isAnimWithShadow=false;
              }  
              else if(checkedId==R.id.radiobutton3)  
              {  
            	  ToolTipConfig.isUseSurfaceView=true;
            	  ToolTipConfig.isAnimWithShadow=true;
              }
          }  
      });  
	}
	
	static int i=0;
	
	private View.OnClickListener listener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
				case R.id.regTooltip:
					TooltipUtil.register(getApplicationContext(), MainActivity.class.getName());
					break;
					
				case R.id.recMessage:
					
					int index=(int)(Math.random()*headList.size());
					if(i%2==0)
					{
						ChatMsg msg=new ChatMsg();
						msg.setFriendId(index);
						msg.setContentType(3);
						msg.setName("名字:"+index);
						msg.setAvatar(headList.get(index));
						TooltipUtil.receMessage(getApplicationContext(), msg);
					}
					else if(i%2==1)
					{
						
						ChatMsg msg=new ChatMsg();
						msg.setFriendId(index);
						msg.setContentType(1);
						msg.setName("name:"+index);
						msg.setAvatar(headList.get(index));
						msg.setMsgContent(index+"：我来自文字消息，我来自悬浮框ToolTip，我来自https://github.com/zz7zz7zz");
						TooltipUtil.receMessage(getApplicationContext(), msg);
					}
					i++;
					
					break;
					
				case R.id.closeTooltip:
					TooltipUtil.closeTooltip(getApplicationContext());
					break;
			}
		}
	};

	
}
