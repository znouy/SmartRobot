package com.znouy.tuiling_robot.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.znouy.tuiling_robot.bean.MessageInfo;
import com.znouy.tuiling_robot.bean.MsgResult;
import com.znouy.tuiling_robot.config.Constants;

/**
 * @author znouy 访问网络的工具类
 */
public class HttpUtil {

	private static final String tag = "HttpUtil";

	/**
	 * 发送消息并获取回复的消息
	 * 
	 * @param msg
	 *            发送的消息内容
	 * @return
	 */
	public static MessageInfo sendAndReceiveMsg(String msg, Context context) {
		MessageInfo messageInfo = new MessageInfo();

		String url = Constants.URL + "?key=" + Constants.API_KEY + "&info="
				+ msg;
		String result = getDataFromNet(url);

		// 使用Gson 解析json数据
		Gson gson = new Gson();
		MsgResult msgResult = gson.fromJson(result, MsgResult.class);

		if (msgResult.getCode() > 400000 || msgResult.getText() == null
				|| "".equals(msgResult.getText().trim())) {
			Toast.makeText(context, "抱歉，目前没有这个功能！", Toast.LENGTH_LONG).show();
		} else {
			messageInfo.setMsgType(MessageInfo.RECEIVE);
			messageInfo.setText(msgResult.getText());
		}
		return messageInfo;
	}

	/**
	 * 从图林api获取返回的聊天消息
	 * 
	 * @param url
	 *            请求路径
	 * @return
	 */
	private static String getDataFromNet(String urlStr) {
		String res = null;

		URL url;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();

			// 设置超时时间
			conn.setReadTimeout(5000);
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");

			// 获取响应码
			int code = conn.getResponseCode();

			if (code == 200) {
				is = conn.getInputStream();
				baos = new ByteArrayOutputStream();

				int len = 0;
				byte[] buffer = new byte[128];
				while ((len = is.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
				res = baos.toString();
				Log.d(tag, "res====" + res);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}

		}
		return res;
	}
}
