package com.znouy.tuiling_robot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.znouy.tuiling_robot.adapter.ChatAdapter;
import com.znouy.tuiling_robot.bean.MessageInfo;
import com.znouy.tuiling_robot.bean.MsgResult;
import com.znouy.tuiling_robot.config.Constants;
import com.znouy.tuiling_robot.net.HttpTask;
import com.znouy.tuiling_robot.net.OnHttpListener;
import com.znouy.tuiling_robot.utils.JsonParser;

public class MainActivity extends Activity implements OnHttpListener {

	private static final String tag = "MainActivity";

	private ImageButton ib_voice;
	private EditText et_msg;
	private Button btn_send;
	private ListView lv_info;

	private List<MessageInfo> datas;
	private ChatAdapter adapter;
	private Toast mToast;
	private String text = "";
	private SharedPreferences sp;

	private long oldTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 57407690 初始化即创建语音配置对象，
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=57407690");

		// 初始化语音合成
		initTts();
		initView();
		initData();
		initListener();
	}

	// private Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	//
	// MessageInfo message = (MessageInfo) msg.obj;
	// datas.add(message);
	// adapter.notifyDataSetChanged();
	// // lv_info.setSelection(datas.size() - 1);
	// }
	// };

	private void initView() {
		// bottom
		ib_voice = (ImageButton) findViewById(R.id.ib_voice);
		et_msg = (EditText) findViewById(R.id.et_send);
		btn_send = (Button) findViewById(R.id.btn_send);
		// listview
		lv_info = (ListView) findViewById(R.id.lv_info);

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		sp = getSharedPreferences(Constants.IFLYTEK_PREF_NAME, 0);

		datas = new ArrayList<MessageInfo>();
		adapter = new ChatAdapter(datas, this);
		lv_info.setAdapter(adapter);
	}

	private void initData() {
		// 初始化聊天数据
		String msg = initWelcomeMsg();
		MessageInfo messageInfo = new MessageInfo(MessageInfo.RECEIVE, msg,
				getCurrentTime());
		datas.add(messageInfo);
		//
		startSpeak(msg);
	}

	/**
	 * @return 获取聊天的当前时间
	 */
	private String getCurrentTime() {
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date();
		String time = format.format(curDate);
		if (currentTime - oldTime >= 1000 * 60 * 2) {
			oldTime = currentTime;
			return time;
		} else {
			return "";
		}
	}

	/**
	 * 初始化机器人问候语
	 */
	private String initWelcomeMsg() {
		String str = null;
		String[] array = getResources().getStringArray(R.array.welcome_tips);

		int index = (int) (Math.random() * array.length);
		str = array[index];
		return str;
	}

	private void initListener() {
		ib_voice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 声音识别
				initIat();
			}
		});

		// 发送消息
		btn_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 发送消息
				sendMessage();
			}
		});

	}

	/**
	 * 发送消息
	 */
	private void sendMessage() {
		final String msg = et_msg.getText().toString().trim();
		et_msg.setText("");
		text = "";
		if (TextUtils.isEmpty(msg)) {
			Toast.makeText(MainActivity.this, "发送的消息内容不能为空，请重新输入！",
					Toast.LENGTH_LONG).show();
			return;
		}
		// 发送端

		// 不为空就创建消息对象并加入容器
		MessageInfo sendMessage = new MessageInfo(MessageInfo.SEND, msg,
				getCurrentTime());

		datas.add(sendMessage);// 加入集合
		adapter.notifyDataSetChanged();
		lv_info.setSelection(datas.size() - 1);

		// 接收端
		String url = Constants.URL + "?key=" + Constants.API_KEY + "&info="
				+ msg;
		HttpTask httpTask = new HttpTask(url, this);
		httpTask.execute();

		// new Thread() {
		// public void run() {
		// MessageInfo receiveMessage = null;
		// try {
		// receiveMessage = HttpUtil.sendAndReceiveMsg(msg,
		// MainActivity.this);
		//
		// Message message = Message.obtain();
		// message.obj = receiveMessage;
		// handler.sendMessage(message);
		// } catch (Exception e) {
		// Toast.makeText(MainActivity.this, "服务器连接失败！",
		// Toast.LENGTH_LONG).show();
		// }
		// };
		// }.start();
	}

	@Override
	public void onFinish(String result) {
		parseJsonString(result);

	}

	/**
	 * 解析json数据
	 * 
	 * @param result
	 */
	private void parseJsonString(String result) {
		Gson gson = new Gson();
		MsgResult msgResult = gson.fromJson(result, MsgResult.class);

		if (msgResult.getCode() > 400000 || msgResult.getText() == null
				|| "".equals(msgResult.getText().trim())) {
			Toast.makeText(this, "抱歉，目前没有这个功能！", Toast.LENGTH_LONG).show();
		} else {
			MessageInfo messageInfo = new MessageInfo(MessageInfo.RECEIVE,
					msgResult.getText(), getCurrentTime());
			datas.add(messageInfo);
			adapter.notifyDataSetChanged();
			lv_info.setSelection(datas.size() - 1);

			// 语音播放
			startSpeak(messageInfo.getText());
		}

	}

	@Override
	public void onError(String result) {
		Log.d(tag, "onerror-==" + result);
		Toast.makeText(MainActivity.this, "服务器连接失败！", Toast.LENGTH_LONG).show();

	}

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时释放连接
		if (mIat != null) {
			mIat.cancel();
			mIat.destroy();
		}
		if (mTts != null) {
			mTts.stopSpeaking();
			mTts.destroy();
		}
	}

	/**
	 * ============科大语音合成 begin===============
	 * 
	 * @param msg
	 **/

	protected void initTts() {
		mTts = SpeechSynthesizer.createSynthesizer(this, null);
		// 2.合成参数设置，详见《MSC Reference Manual》SpeechSynthesizer类
		// 设置发音人（更多在线发音人，用户可参见 附录13.2
		mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaomei"); // 设置发音人
		mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
		mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围 0~100
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
		// 设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
		// 保存在 SD 卡需要在AndroidManifest.xml 添加写 SD 卡权限
		// 仅支持保存为 pcm 和 wav格式，如果不需要保存合成音频，注释该行代码
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");

	}

	private void startSpeak(String msg) {
		// 3.开始合成
		mTts.startSpeaking(msg, mTtsListener);
	}

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		// 缓冲进度
		private int mPercentForBuffering = 0;
		// 播放进度
		private int mPercentForPlaying = 0;

		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		// 恢复播放回调接口
		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		// 缓冲进度回调
		// percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在
		// 文本中结束位置，info为附加信息。
		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			int mPercentForBuffering = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		// 播放进度回调
		// percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文 本中结束位置.
		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		// 会话结束回调接口，没有错误时，error为null
		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				showTip("播放完成");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}
	};

	/** ============科大语音合成end=============== **/
	/** ============科大语音识别 begin=============== **/

	protected void initIat() {
		mIat = SpeechRecognizer.createRecognizer(this, null);

		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		RecognizerDialog mIatDialog = new RecognizerDialog(this, mInitListener);
		// 2.设置听写参数，详见《MSC Reference Manual》SpeechConstant类

		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
		// 3.开始听写
		// mIat.startListening(mRecoListener);
		String iat_show = getResources().getString(R.string.pref_key_iat_show);
		boolean isShowDialog = sp.getBoolean(iat_show, true);
		if (isShowDialog) {
			// 显示听写对话框
			mIatDialog.setListener(mRecognizerDialogListener);
			mIatDialog.show();
			showTip(getString(R.string.text_begin));
		} else {
			// 不显示听写对话框
			int ret = mIat.startListening(mRecoListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("听写失败,错误码：" + ret);
			} else {
				showTip(getString(R.string.text_begin));
			}
		}
	}

	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			parseResult(results);
			if (isLast) {
				// 自动发送消息
				sendMessage();
			}
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(tag, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	// 听写监听器
	private RecognizerListener mRecoListener = new RecognizerListener() {
		// 听写结果回调接口(返回Json格式结果，用户可参见附录13.1)；
		// 一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
		// 关于解析Json的代码可参见Demo中JsonParser类；
		// isLast等于true时会话结束。
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.i(tag, results.getResultString());
			parseResult(results);
			if (isLast) {
				// 自动发送消息
				sendMessage();
			}
		}

		// 会话发生错误回调接口
		public void onError(SpeechError error) { // 打印错误码描述
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			showTip(error.getPlainDescription(true));

		}

		// 开始录音
		public void onBeginOfSpeech() {
			showTip("开始说话");
		}

		// volume音量值0~30，data音频数据
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
		}

		// 结束录音
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			showTip("结束说话");
		}

		// 扩展用接口
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

		}
	};

	private SpeechRecognizer mIat;

	private long currentTime;

	private SpeechSynthesizer mTts;

	private void parseResult(RecognizerResult results) {
		text += JsonParser.parseIatResult(results.getResultString());
		Log.i(tag, "text==" + text);
		et_msg.setText(text);
	}

	/** ============科大语音识别 end=============== **/

}
