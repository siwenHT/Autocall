package com.siwen.autocallphone;

import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.telephony.ITelephony;

import android.app.Activity;  

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

/**
 * @author andong
 * 程序刚运行就显示的界面
 */
public class phoneActivity extends Activity {
	private Timer timer1;
	private int   curStep;  // 1、计算时间准备打电话. 2、拨打 3、呼叫中。 4、呼叫结束中. 
	private int   callTimeCnt;
	private Intent intent;
	ITelephony iPhoney = null;
	TelephonyManager tm = null;
	private boolean  endCall = false;
	private boolean  runable = false;
	
	private int  callNums = 50;
	private int  callSpaceTime = 5;
	private int  callHoldTime = 10;
	
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
	}
	
	public void call(View v) 
	{
		System.out.println("拨打电话.");
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
		
		System.out.println("起始数据： 次数" + callNums + ",间隔时间" + callSpaceTime + ",呼叫持续时间" + callHoldTime);
	}
	
	public void stop(View v) 
	{
		runable = false;
		endCall = endCallPhone();
		
		EditText etNumber = (EditText) findViewById(R.id.editText3); //呼叫次数
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText4); //间隔时间
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText1); //呼叫持续时间
		etNumber.setEnabled(true);
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
	}
	
	public boolean endCallPhone()
	{
		boolean endCall = false;
		try 
		{
			endCall = iPhoney.endCall();
			if (endCall)
			{
				callNums -= 1;
				updateUI();
				Thread.sleep(callSpaceTime * 1000);
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
			btn.setText("("+time +"分钟后)" +R.string.call);
		}
		else
		{
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setText(R.string.call);
		}
	}
	
	public int checkTime()
	{
		TimePicker timerPicker = (TimePicker) findViewById(R.id.timePicker1);	// 输入框对象
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
								int state= tm.getCallState();
								if(state == TelephonyManager.CALL_STATE_IDLE)
								{
									startCallPhone();
									try 
									{
										Thread.sleep(2000);
									}
									catch (Exception e)
								    {
										e.printStackTrace();
								    }
									curStep = 3;
								}
								if(curStep != 4 && state == TelephonyManager.CALL_STATE_OFFHOOK)
								{
									try 
									{
										Thread.sleep(callHoldTime * 1000 - 2000);
										endCall = endCallPhone();
										//System.out.println("是否成功挂断："+endCall);
									}
									catch (Exception e)
								    {
										e.printStackTrace();
										curStep = 4;
								    }
								}
								 
								if (curStep == 4)
								{
									try 
									{
										endCall = endCallPhone();
									}
									catch (Exception e)
								    {
										e.printStackTrace();
										curStep = 4;
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
}

