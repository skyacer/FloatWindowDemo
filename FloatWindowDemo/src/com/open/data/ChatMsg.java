package com.open.data;

import java.io.Serializable;


public class ChatMsg implements Serializable {

	protected static final long serialVersionUID = -7499419174293493978L;

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_IMG = 2;
	public static final int TYPE_VOICE = 3;
	public static final int TYPE_LOCATE = 4;
	public static final int TYPE_VEDIO = 5;
	
	protected long friendId;
	protected String name;
	protected String avatar;
	protected String msgContent;
	protected int unreadCount;
	protected int contentType ;//1,文字；2,图片；3,语音; 4,地理位置; 5，视频
	
	public int getContentType() {
		return contentType;
	}
	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}


	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public long getFriendId() {
		return friendId;
	}

	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}


	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
}
