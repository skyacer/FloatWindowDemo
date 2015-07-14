# FloatWindowDemo
华丽的悬浮球Demo，附聊天，语音识别（在完善中），消息提醒等功能。
先来看几张图片
![](http://skyace-skyace.stor.sinaapp.com/20150708_floatwindow1.png) 
![](http://skyace-skyace.stor.sinaapp.com/20150708_floatwindow2.png) 
![](http://skyace-skyace.stor.sinaapp.com/20150708_floatwindow3.png) 

实现要点：
1.     WindowManager

addView(View view, LayoutParams params) ，添加一个悬浮窗
updateViewLayout(View view, LayoutParams params)，要使悬浮窗做出改变，需通过改变params的属性，并调用此方法更新。
removeView()移除一个悬浮窗。

2.  WindowManager.LayoutParams属性的设置。

WindowManager.LayoutParams mParams=new WindowManager.LayoutParams();
mParams.type=WindowManager.LayoutParams.TYPE_PHONE;//悬浮窗的类型
mParams.format= PixelFormat.RGBA_8888;  //效果为透明
mParams.flags=
WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //悬浮窗的行为，比如说不可聚焦，不可触摸，全屏对等
mParams.width=100;//指定悬浮窗的宽度
mParams.height=100;//指定悬浮窗的高度。
mParams.gravity=Gravity.LEFT|Gravity.TOP; //悬浮窗的对齐方式
mParams.x=0;  //悬浮窗的横坐标
mParams.y=0;//悬浮窗的纵坐标
