package com.blackMonster.webkiosk.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.blackMonster.webkiosk.SharedPrefs.MainPrefs;
import com.blackMonster.webkiosk.SharedPrefs.RefreshDBPrefs;
import com.blackMonster.webkiosk.controller.RefreshFullDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Sets alarms for auto refreshing database.
 */

public class AutoRefreshAlarmService extends IntentService {
	public static final String TAG = "serviceAlarm";
	public static final String PREF_AUTO_UPDATE_OVER = "pref_auto_update_over";
	public static final String CALLER_TYPE = "callerType";
	

	//Points at which this service is called.
	public static final int BOOT_COMPLETE = 1;
	public static final int CONNECTIVITY_CHANGE = 2;	//Network connectivity changed
	public static final int INSTALLATION_DONE = 3;
	public static final int TIME_JUNCTION = 4;
	
	public static final double LAST_SERVER_UPDATE_IN_DAY = 19;  ///i.e 7pm, time after which no teacher updates attendance. i.e time after which server will not be updated.
	public static final double WAIT_WIFI_UPTO = 22.5;
	

	public AutoRefreshAlarmService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		int callerType = intent.getExtras().getInt(CALLER_TYPE);

		switch (callerType) {
		
		case CONNECTIVITY_CHANGE:
			connectivityChange();
			break;
		case TIME_JUNCTION:
			timeJunciton();
			break;
		case BOOT_COMPLETE:	
			createRepeatingAlarm(WAIT_WIFI_UPTO);
			break;
		case INSTALLATION_DONE:
			createRepeatingAlarm(WAIT_WIFI_UPTO);
			break;
		}
	}

	private void timeJunciton() {
		if (RefreshDBPrefs.canAutoRefresh(this)) {
			
			if ( ! isRefreshedAfterServerUpdate() ) {
				if (isNetAvailableForApp()) {
					startRefreshService();
				}
			}
		
		}
		
	}

	private void connectivityChange() {
		
		if (RefreshDBPrefs.canAutoRefresh(this)) {
			if (isWifiAvailable()) {
				startRefreshService();
				//log ("refresh started");
			}
			else
			{
				if ( ! isWifiOnlyZone()) 
				{
					//log("not in wifiOnly zone");
					timeJunciton();
				}
			}
			
		}
	}
	
	private boolean isWifiOnlyZone() {
	//	log("last server updated time : " + printTime(getLastServerUpdatedTime()) + " wifi zone end time : " 
	//+ printTime(getWifiZoneEndTime()) );
		return System.currentTimeMillis() > getLastServerUpdatedTime() &&
				System.currentTimeMillis() < getWifiZoneEndTime() ; 
		
	}

	private void createRepeatingAlarm(double time) {
		
		//Log.d(TAG, "create repeating alarm");
		//log("creating repeatin alarm");
		Calendar calender = Calendar.getInstance();
		long randomize = calender.get(Calendar.MILLISECOND) +  calender.get(Calendar.SECOND) *1000 + calender.get(Calendar.MINUTE) * 60000;
		long timeJunction = getFutureTimeInMillisec(time) + randomize;
		RefreshDBPrefs.setWifiZoneEndRandomizeTime(randomize, this);
		//log ("current time " + printTime(System.currentTimeMillis()) + "  timejunction : " + printTime(timeJunction) + " random : " + randomize);
		
		
		setAlarm(timeJunction);
		
		
	}
	//beware using this funcion
	private void setAlarm(long timeJunction) {
		Intent intent = new Intent(this,AutoRefreshAlarmService.class);
		intent.putExtra("callerType", AutoRefreshAlarmService.TIME_JUNCTION);
		PendingIntent operation = PendingIntent.getService(this, -1,
				intent,PendingIntent.FLAG_CANCEL_CURRENT);
		

		AlarmManager am = ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE));
		am.cancel(operation);
		am.setRepeating(AlarmManager.RTC, timeJunction
				, AlarmManager.INTERVAL_DAY, operation);		
	}
	
	public static  void cancelAlarm(Context context) {
		Intent intent = new Intent(context,AutoRefreshAlarmService.class);
		intent.putExtra("callerType", AutoRefreshAlarmService.TIME_JUNCTION);
		PendingIntent operation = PendingIntent.getService(context, -1,
				intent,PendingIntent.FLAG_CANCEL_CURRENT);
		
		
		AlarmManager am = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
		am.cancel(operation);
				
	}

	private boolean isNetAvailableForApp() {
		boolean result = false;
		if (isWifiAvailable()) result = true;
		else 
		{
			if (isDataPackAvailable() && canUpdateOverDataPack()) result = true;
			else result = false;
		}
		
		return result;
	}

	private boolean canUpdateOverDataPack() {
		return getSharedPreferences(MainPrefs.PREFS_NAME, 0).getString(PREF_AUTO_UPDATE_OVER, "anyNetwork").equals("anyNetwork");
	}

	private boolean isDataPackAvailable() {
		NetworkInfo networkInfo = ( (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE) ).getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	private boolean isWifiAvailable() {
		NetworkInfo networkInfo = ( (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE) ).getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
		}
	
	
	private boolean isRefreshedAfterServerUpdate() {
		//Log.d(TAG, "isRefreshedAfterServerUpdate ");
		//log("isrefreshedAfterServerUpdate");
		
		
		long lastServerUpdatedTime = getLastServerUpdatedTime();
		
		printTime( System.currentTimeMillis());
		printTime ( lastServerUpdatedTime);
		
		//Log.d(TAG, "isRefreshedAfterServerUpdate : " + DateUtils.formatDateTime(this, lastServerUpdatedTime, DateUtils.FORMAT_SHOW_TIME) + 
	//			"  " + DateUtils.formatDateTime(this, lastServerUpdatedTime, DateUtils.FORMAT_SHOW_DATE));
		
		//log ( "last server updated till (ie 7pm) : " + printTime(lastServerUpdatedTime) );
		
		return RefreshDBPrefs.getRefreshEndTimeStamp(this) >  lastServerUpdatedTime ;
	}

	
	
		
	
	private long getLastServerUpdatedTime() {
		
		return getPastTimeInMillisec(LAST_SERVER_UPDATE_IN_DAY) ;		
	}
	
	private long getWifiZoneEndTime() {
		return getLastServerUpdatedTime() +( (long) ( ( WAIT_WIFI_UPTO - LAST_SERVER_UPDATE_IN_DAY ) * AlarmManager.INTERVAL_HOUR ) )+ 
				RefreshDBPrefs.getWifiZoneEndRandomizeTime(this);
	}
	
	private long getPastTimeInMillisec(double time) {
		Calendar calender = Calendar.getInstance();
		long  timeDiff=  ( calender.get(Calendar.HOUR_OF_DAY) * 60 + calender.get(Calendar.MINUTE) - 
				(long) (time *60) ) * 60 * 1000;
		if (timeDiff <0 ) timeDiff = AlarmManager.INTERVAL_DAY + timeDiff;
		return System.currentTimeMillis() - timeDiff;
	}
	
	private long getFutureTimeInMillisec(double time) {
		Calendar calender = Calendar.getInstance();
		long tm = (long) (time*60) ;
	//	Log.d(TAG, "tt " + tm);
		long  timeDiff= ( tm  - 
				calender.get(Calendar.HOUR_OF_DAY) * 60 - calender.get(Calendar.MINUTE) ) * 60 * 1000;
		if (timeDiff <0 ) timeDiff = AlarmManager.INTERVAL_DAY + timeDiff;
		return System.currentTimeMillis() + timeDiff ; 
		
	}

	private String printTime(long currentTimeMillis) {
		 SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");

	        Date resultdate = new Date(currentTimeMillis);
	        String res = sdf.format(resultdate);
	     //   Log.d(TAG,  res );
	        return res;
	}

	private void startRefreshService() {
		Intent intent = ServiceRefreshAll.getIntent(RefreshFullDB.AUTO_REFRESH, this);

		startService(intent);
		
	}
	
	
	
}
