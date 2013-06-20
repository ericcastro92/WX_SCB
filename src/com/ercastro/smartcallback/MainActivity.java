package com.ercastro.smartcallback;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity 
{
	private WifiManager wifiManager;
	private TelephonyManager telManager;
	private WifiInfo wifiInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Set up layout buttons/toggles/etc
		final TextView statusLabel = (TextView) findViewById(R.id.statusLabel);
		final EditText phoneInput = (EditText) findViewById(R.id.phoneInput);
		Button callButton = (Button) findViewById(R.id.callButton);
		ToggleButton dropToggle = (ToggleButton) findViewById(R.id.dropToggle);
		
		//WifiManager & TelephonyManager setup
		initializeResources();
		
		//Initialize buttons
		callButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				String phoneNumber = phoneInput.getText().toString();
				String url = "tel:" + phoneNumber;
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
				startActivity(intent);
			}
			
		});

		int rssi = wifiInfo.getRssi();
		boolean wifiEnabled = wifiManager.isWifiEnabled();
		Log.e("WifiEnabled", ""+wifiEnabled);
		
		//Calculates wifi signal strength on a scale of 0-99
		int wifiStrength = WifiManager.calculateSignalLevel(rssi, 100);
		if(wifiEnabled)
			Log.e("Wifi Strength", ""+wifiStrength);
		
		//PhoneStateListener & callback method set up
		PhoneStateListener phoneStateListener = new PhoneStateListener()
		{
			public void onSignalStrengthsChanged(SignalStrength signalStrength)
			{
				int strength = signalStrength.getGsmSignalStrength();
				Log.e("Signal Strength", ""+strength);
			}
			
			public void onServiceStateChanged(ServiceState serviceState)
			{
				int stateID = serviceState.getState();
				String state;
				switch(stateID)
				{
					case ServiceState.STATE_IN_SERVICE:
						state = "In Service";
						break;
					case ServiceState.STATE_OUT_OF_SERVICE:
						state = "Out of service";
						break;
					case ServiceState.STATE_POWER_OFF:
						state = "Power off";
						break;
					default: state = "Invalid State ID"; break;
				}
				
				Log.e("Service State", state);
			}
			
			public ServiceState getServiceState()
			{
				return null;
			}
		};
		
		telManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE |
				PhoneStateListener.LISTEN_SERVICE_STATE |
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	
	}

	private void initializeResources()
	{
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		wifiInfo = wifiManager.getConnectionInfo();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
