package com.znouy.tuiling_robot.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.znouy.tuiling_robot.R;
import com.znouy.tuiling_robot.bean.MessageInfo;

public class ChatAdapter extends BaseAdapter {
	private List<MessageInfo> datas;
	private Context context;

	public ChatAdapter(List<MessageInfo> datas, Context context) {
		this.datas = datas;
		this.context = context;
	}

	@Override
	public int getCount() {
		if (datas.size() != 0) {
			return datas.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	//发送消息为0，接收消息为1
	@Override
	public int getItemViewType(int position) {
		// range 0 to getViewTypeCount - 1.
		MessageInfo messageInfo = datas.get(position);

		return messageInfo.getMsgType() == MessageInfo.SEND ? 0 : 1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		ViewHolder viewHolder = null;
		// 获取数据
		MessageInfo messageInfo = datas.get(position);
		// 初始化视图
		if (convertView == null) {
			viewHolder = new ViewHolder();
			if (messageInfo.getMsgType() == MessageInfo.SEND) {
				convertView = inflater.inflate(R.layout.item_chat_send, null);
				bindView(convertView, viewHolder);
			} else if (messageInfo.getMsgType() == MessageInfo.RECEIVE) {
				convertView = inflater
						.inflate(R.layout.item_chat_receive, null);
				bindView(convertView, viewHolder);
			}

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// 设置数据
		viewHolder.content.setText(messageInfo.getText());
		if (!"".equals(messageInfo.getTime())) {
			viewHolder.dataStr.setVisibility(View.VISIBLE);
			viewHolder.dataStr.setText(messageInfo.getTime());
		} else {
			viewHolder.dataStr.setVisibility(View.GONE);
		}
		return convertView;
	}

	private void bindView(View convertView, ViewHolder viewHolder) {
		viewHolder.dataStr = (TextView) convertView
				.findViewById(R.id.tv_item_time);
		viewHolder.content = (TextView) convertView
				.findViewById(R.id.tv_item_content);
		convertView.setTag(viewHolder);
	}

	private class ViewHolder {
		public TextView dataStr;
		public TextView content;
	}
}
