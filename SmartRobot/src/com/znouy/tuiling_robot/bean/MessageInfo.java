package com.znouy.tuiling_robot.bean;

public class MessageInfo {

	public static final int SEND = 10001;
	public static final int RECEIVE = 10002;

	private String time;
	private String text;// 消息内容
	private int msgType;// 消息类型

	public MessageInfo() {
		super();
	}

	public MessageInfo(int msgType, String msgContent, String time) {
		setMsgType(msgType);
		setText(msgContent);
		setTime(time);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

}
