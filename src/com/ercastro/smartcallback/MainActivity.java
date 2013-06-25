package com.ercastro.smartcallback;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.view.Menu;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity 
{	
	private WifiManager wifiManager;
	private TelephonyManager telManager;
	private WifiInfo wifiInfo;
	
	private final MainActivity ma = this;
	
	private static boolean callDropped = false;
	
	private String recentPhoneURI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Set up layout buttons/toggles/etc
		final TextView statusLabel = (TextView) findViewById(R.id.statusLabel);
		final TextView cellLevelLabel = (TextView) findViewById(R.id.cellLevelLabel);
		final TextView wifiLevelLabel = (TextView) findViewById(R.id.wifiLevelLabel);
		final EditText phoneInput = (EditText) findViewById(R.id.phoneInput);
		
		Button callButton = (Button) findViewById(R.id.callButton);
		
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
				recentPhoneURI = url;
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
				startActivity(intent);
			}
			
		});

		int rssi = wifiInfo.getRssi();
		boolean wifiEnabled = wifiManager.isWifiEnabled();
		Log.e("WifiEnabled", ""+wifiEnabled);
		
		//Calculates wifi signal strength on a scale of 0-99
		int wifiStrength = WifiManager.calculateSignalLevel(rssi, 100);
		
		//PhoneStateListener & callback method set up
		PhoneStateListener phoneStateListener = new PhoneStateListener()
		{	
			public void onSignalStrengthsChanged(SignalStrength signalStrength)
			{
				int strength = signalStrength.getGsmSignalStrength();
				cellLevelLabel.setText("Cell Strength: " + strength);
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
						ma.callBack();
						break;
					case ServiceState.STATE_OUT_OF_SERVICE:
						callDropped = true;
						cellLevelLabel.setText("Cell disabled");
						state = "Out of service";
						break;
					case ServiceState.STATE_POWER_OFF:
						callDropped = true;
						cellLevelLabel.setText("Cell disabled");
						state = "Power off";
						break;
					default: state = "Invalid State ID"; break;
				}
				
				Log.e("Service State", state);
				statusLabel.setText(state);
			}
			
		};
		
		telManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE |
				PhoneStateListener.LISTEN_SERVICE_STATE |
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	
		
		Button[] buttons = new Button[1];
		buttons[0] = new Button(this);
		buttons[0].setText(""+1);
		
		//ArrayAdapter<Button> dialPadButtons = new ArrayAdapter<Button>(this, R.layout.activity_main, buttons);
		
		//GridView dialpad = (GridView) findViewById(R.id.dialpad);
		//dialpad.setAdapter(dialPadButtons);
	}

	/*
	 * Redials most recent number
	 */
	public void callBack()
	{
		if(!callDropped || recentPhoneURI==null || recentPhoneURI.isEmpty())
			return;
		
		callDropped = false;
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(recentPhoneURI));
		startActivity(intent);
	}
	
	/*private Button[] generateButtons()
	{
		Button[] dialpad = new Button[12];
		for(int i = 0;i<dialpad.length;i++)
		{
			dialpad[i] = new Button(this);
			dialpad[i].setText(""+(i+1));
		}
		
		return dialpad;
	}*/
	
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
