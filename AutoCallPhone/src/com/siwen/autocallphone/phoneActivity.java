package com.siwen.autocallphone;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.telephony.ITelephony;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;

import android.app.Activity;  
import android.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

/**
 * @author andong
 * 程序刚运行就显示的界面
 */
public class phoneActivity extends Activity {
	// 1、计算时间准备打电话. 2、拨打 3、呼叫中。 4、呼叫成功。 5、呼叫结束中. 6、呼叫结束完成。
	
	private int   curStep;    
	private Timer timer1;
	private int   callTimeCnt;
	private Intent intent;
	ITelephony iPhoney = null;
	TelephonyManager tm = null;
	private boolean  endCall = false;
	private boolean  runable = false;
	
	private int  callNums = 50;
	private int  callSpaceTime = 5;
	private int  callHoldTime = 10;
	
	ViewGroup bannerContainer;
	BannerView bv;
		
	/**
	 * 当界面刚被创建时回调此方法
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		// 必须执行此句代码. 执行父类的初始化操作.
		
		setContentView(R.layout.activity_main);		// 设置当前界面显示的布局:加载布局的xml文件.	
		
		timer1 = new Timer();
		curStep = 1;
		endCall = false;
		
		tm=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		iPhoney = getITelephony(this);
		
		startCount();
		
		
		bannerContainer = (ViewGroup) this.findViewById(R.id.bannerContainer);
	    this.initBanner();
	    this.bv.loadAD();
	}
	
	public void call(View v) 
	{
		if (runable)
		{
			showTips("已经在呼叫中，如需更改呼叫设定，请先停止呼叫！");
			return;
		} 
		System.out.println("拨打电话.");
		EditText etNumber1 = (EditText) findViewById(R.id.phoneNum);	// 输入框对象
		String number = etNumber1.getText().toString();	// 将要拨打的号码转为数字
		if ("".equals(number))
		{
			showTips("请输入电话号码！");
			return ;
		}
		
		runable = true;
		
		EditText etNumber = (EditText) findViewById(R.id.editText3); //呼叫次数
		String str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callNums = Integer.parseInt(str_data);
		
		
		etNumber = (EditText) findViewById(R.id.editText4); //间隔时间
		etNumber.setEnabled(false);
		str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callSpaceTime = Integer.parseInt(str_data);
		
		etNumber = (EditText) findViewById(R.id.editText1); //呼叫持续时间
		etNumber.setEnabled(false);
		str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callHoldTime = Integer.parseInt(str_data);
		
		TimePicker timerPicker = (TimePicker) findViewById(R.id.timePicker1);
		timerPicker.setEnabled(false);
		
		System.out.println("起始数据： 次数" + callNums + ",间隔时间" + callSpaceTime + ",呼叫持续时间" + callHoldTime);
	}
	
	public void stop(View v) 
	{
		if (!runable)
		{
			showTips("还未进行呼叫！", false);
			return;
		}
		runable = false;
		endCall = endCallPhone();
		
		EditText etNumber = (EditText) findViewById(R.id.editText3); //呼叫次数
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText4); //间隔时间
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText1); //呼叫持续时间
		etNumber.setEnabled(true);
		
		TimePicker timerPicker = (TimePicker) findViewById(R.id.timePicker1);
		timerPicker.setEnabled(true);
		
		Button btn = (Button) findViewById(R.id.Button01);
		btn.setText("呼叫");
		
		System.out.println("停止拨打： 次数" + callNums );
		
	}
	
	public void startCallPhone()
	{
		// 1. 取出输入框中的号码
		EditText etNumber = (EditText) findViewById(R.id.phoneNum);	// 输入框对象
		String number = etNumber.getText().toString();	// 将要拨打的号码转为数字
	
		// 2. 根据号码拨打电话
		intent = new Intent();		// 创建一个意图
		intent.setAction(Intent.ACTION_CALL);		// 指定其动作为拨打电话
		intent.setData(Uri.parse("tel:" + number));	// 指定将要拨出的号码
		startActivity(intent);	// 执行这个动作
		
		callTimeCnt = 0;
		callTimeCnt = 0;
		System.out.println("拨打电话. " + number);
	}
	
	public boolean endCallPhone()
	{
		boolean endCall = false;
		try 
		{
			endCall = iPhoney.endCall();
			if (endCall)
			{
				//callNums -= 1;
				//updateUI();
				curStep = 5;
				//callTimeCnt = 0;
			}
		}
		catch (Exception e)
	    {
			e.printStackTrace();
		}
		
		return endCall;
	}
	
	public void updateUI()
	{
		EditText etNumber = (EditText) findViewById(R.id.editText3); //呼叫次数
		etNumber.setText("" + callNums);
		
		int time = checkTime();
		
		if (time > 0 )
		{
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setText("("+time +"分钟后)" + "呼叫");
		}
		else
		{
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setText("呼叫中");
		}
	}
	
	public void showTips(String str)
	{
		showTips(str, true);
	}
	
	public void showTips(String str, boolean showBtn)
	{
		AlertDialog.Builder dlog = new AlertDialog.Builder(this).setTitle("提示");
		dlog.setMessage(str);
		if (showBtn)
		{
			dlog.setPositiveButton("确定", null);
		}
		dlog.show();
	}
	
	public int checkTime()
	{
		TimePicker timerPicker = (TimePicker) findViewById(R.id.timePicker1);
		int hour = timerPicker.getCurrentHour();
		int minute = timerPicker.getCurrentMinute();
		int timeNum = hour * 60 + minute;
		
		Calendar c = Calendar.getInstance(); 
		hour = c.get(Calendar.HOUR_OF_DAY);  
		minute = c.get(Calendar.MINUTE);
		
		int timeCur = hour * 60 + minute;

		return timeNum - timeCur;
	}
	
	public void startCount()
	{
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() 
			{
				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() 
					{
						if (runable)
						{
							updateUI();
							int time = checkTime();
							if (time <= 0 )
							{
								if (curStep == 3)
								{
									callTimeCnt += 1;
									if (callTimeCnt < callHoldTime)
									{
										return;
									}
								}
								else if (curStep == 5)
								{
									callTimeCnt += 1;
									if (callTimeCnt < callSpaceTime)
									{
										return;
									}
								}
								
								int state= tm.getCallState();
								if(state == TelephonyManager.CALL_STATE_IDLE)
								{
									startCallPhone();
									curStep = 3;
									callNums -= 1;
									updateUI();
									callTimeCnt = 0;
								}
								else if(state == TelephonyManager.CALL_STATE_OFFHOOK)
								{
									if (curStep == 3)
									{
										curStep = 4;
										callTimeCnt = 0;
									}
									else if(curStep == 4)
									{
										try 
										{
											endCall = endCallPhone();
											//System.out.println("是否成功挂断：" + endCall);
										}
										catch (Exception e)
									    {
											e.printStackTrace();
									    }
									}
								}
							}
						}
					}
				});
			}
		};
		timer1.schedule(timerTask, 0, 1000);
	}
	

	
	
	
	/**
	* 通过反射得到实例
	* @param context
	* @return
	*/
	private static ITelephony getITelephony(Context context) 
	{
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		
		try 
		{
			getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null); // 获取声明的方法
			getITelephonyMethod.setAccessible(true);
		} 
		catch (SecurityException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
		}
		ITelephony iTelephony=null;
		try 
		{
			iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null); // 获取实例
			return iTelephony;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return iTelephony;
	}
	
	
	private void initBanner() 
	{
		this.bv = new BannerView(this, ADSize.BANNER, Constants.APPID, Constants.BannerPosID2);
		// 注意：如果开发者的banner不是始终展示在屏幕中的话，请关闭自动刷新，否则将导致曝光率过低。
		// 并且应该自行处理：当banner广告区域出现在屏幕后，再手动loadAD。
		
		bv.setRefresh(15); 
		bv.setADListener(new AbstractBannerADListener() 
		{
			@Override
			public void onNoAD(int arg0) 
			{
				Log.i("AD_DEMO", "BannerNoAD，eCode=" + arg0);
			}

			@Override
			public void onADReceiv()
			{
				Log.i("AD_DEMO", "ONBannerReceive");
			}
		});
		bannerContainer.addView(bv);
	}
}

