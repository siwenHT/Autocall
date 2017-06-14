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
 * ��������о���ʾ�Ľ���
 */
public class phoneActivity extends Activity {
	private Timer timer1;
	private int   curStep;  // 1������ʱ��׼����绰. 2������ 3�������С� 4�����н�����. 
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
	 * ������ձ�����ʱ�ص��˷���
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		// ����ִ�д˾����. ִ�и���ĳ�ʼ������.
		
		setContentView(R.layout.activity_main);		// ���õ�ǰ������ʾ�Ĳ���:���ز��ֵ�xml�ļ�.	
		
		timer1 = new Timer();
		curStep = 1;
		endCall = false;
		
		tm=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		iPhoney = getITelephony(this);
		
		startCount();
	}
	
	public void call(View v) 
	{
		System.out.println("����绰.");
		runable = true;
		
		EditText etNumber = (EditText) findViewById(R.id.editText3); //���д���
		String str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callNums = Integer.parseInt(str_data);
		
		
		etNumber = (EditText) findViewById(R.id.editText4); //���ʱ��
		etNumber.setEnabled(false);
		str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callSpaceTime = Integer.parseInt(str_data);
		
		etNumber = (EditText) findViewById(R.id.editText1); //���г���ʱ��
		etNumber.setEnabled(false);
		str_data = etNumber.getText().toString();
		if("".equals(str_data))
		{
		    str_data = "0";
		}
		callHoldTime = Integer.parseInt(str_data);
		
		System.out.println("��ʼ���ݣ� ����" + callNums + ",���ʱ��" + callSpaceTime + ",���г���ʱ��" + callHoldTime);
	}
	
	public void stop(View v) 
	{
		runable = false;
		endCall = endCallPhone();
		
		EditText etNumber = (EditText) findViewById(R.id.editText3); //���д���
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText4); //���ʱ��
		etNumber.setEnabled(true);
		
		etNumber = (EditText) findViewById(R.id.editText1); //���г���ʱ��
		etNumber.setEnabled(true);
	}
	
	public void startCallPhone()
	{
		// 1. ȡ��������еĺ���
		EditText etNumber = (EditText) findViewById(R.id.phoneNum);	// ��������
		String number = etNumber.getText().toString();	// ��Ҫ����ĺ���תΪ����
	
		// 2. ���ݺ��벦��绰
		intent = new Intent();		// ����һ����ͼ
		intent.setAction(Intent.ACTION_CALL);		// ָ���䶯��Ϊ����绰
		intent.setData(Uri.parse("tel:" + number));	// ָ����Ҫ�����ĺ���
		startActivity(intent);	// ִ���������
		
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
		EditText etNumber = (EditText) findViewById(R.id.editText3); //���д���
		etNumber.setText("" + callNums);
		
		int time = checkTime();
		
		if (time > 0 )
		{
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setText("("+time +"���Ӻ�)" +R.string.call);
		}
		else
		{
			Button btn = (Button) findViewById(R.id.Button01);
			btn.setText(R.string.call);
		}
	}
	
	public int checkTime()
	{
		TimePicker timerPicker = (TimePicker) findViewById(R.id.timePicker1);	// ��������
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
										//System.out.println("�Ƿ�ɹ��Ҷϣ�"+endCall);
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
	* ͨ������õ�ʵ��
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
			getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null); // ��ȡ�����ķ���
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
			iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null); // ��ȡʵ��
			return iTelephony;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return iTelephony;
	}
}
