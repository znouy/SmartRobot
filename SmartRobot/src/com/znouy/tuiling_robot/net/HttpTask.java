package com.znouy.tuiling_robot.net;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class HttpTask extends AsyncTask<Void, Void, String> {

	private static final String tag = "HttpTask";

	private String urlStr;
	private OnHttpListener listener;

	private boolean b;

	public HttpTask(String urlStr, OnHttpListener listener) {
		this.urlStr = urlStr;
		this.listener = listener;
	}

	@Override
	protected String doInBackground (Void... params) {
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
				b = true;
				Log.d(tag, "res====" + res);

			}
		} catch (Exception e) {

			b = false;
			res = e.toString();

		} finally {
			if (conn != null) {
				conn.disconnect();
			}

		}
		return res;
	}

	@Override
	protected void onPostExecute(String result) {
		if (b) {
			listener.onFinish(result);
		} else {
			listener.onError(result);
		}
	}

}
