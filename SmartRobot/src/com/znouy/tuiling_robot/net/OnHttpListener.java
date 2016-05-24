package com.znouy.tuiling_robot.net;

public interface OnHttpListener {

	/**
	 * 网络访问成功的回调
	 * 
	 * @param result
	 */
	void onFinish(String result);

	/**
	 * 网络访问失败的回调
	 * 
	 * @param result
	 */
	void onError(String result);

}
