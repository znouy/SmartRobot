package com.znouy.tuiling_robot.bean;

/**
 * @author znouy 服务器返回的数据封装
 */
public class MsgResult {
	private int code;
	private String text;

	public MsgResult() {
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
