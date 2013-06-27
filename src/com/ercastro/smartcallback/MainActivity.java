package com.ercastro.smartcallback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity 
{	
	private TelephonyManager telManager;
	private final MainActivity ma = this;
	
	private static boolean callDropped = false;
	
	private String phoneNumber;
	private String recentPhoneURI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Get layout buttons/views/etc
		final TextView statusLabel = (TextView) findViewById(R.id.statusLabel);
		final TextView cellLevelLabel = (TextView) findViewById(R.id.cellLevelLabel);
		final EditText meetingField = (EditText) findViewById(R.id.meetingNumberInput);
		final EditText passwordField = (EditText) findViewById(R.id.meetingPasswordInput);
		final Spinner locationSelector = (Spinner) findViewById(R.id.phoneInput);
		Button callButton = (Button) findViewById(R.id.callButton);
		GridView dialpad = (GridView) findViewById(R.id.dialpad);
		
		//TelephonyManager setup
		initializeResources();
		final DialpadAdapter dialpadAdapter = new DialpadAdapter(meetingField, this);
		
		//Set up spinner
		locationSelector.setPrompt("Location");
		ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
				R.array.countries, android.R.layout.simple_spinner_item);
		countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locationSelector.setAdapter(countryAdapter);
		locationSelector.setOnItemSelectedListener(new CountrySelectedListener(this));
		
		//Set up Focus listeners
		locationSelector.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				EditText phoneField = (EditText) view;
				dialpadAdapter.setField(phoneField);
				Log.e("phoneField", "hasFocus: " + hasFocus);
			}
		});
		meetingField.setOnFocusChangeListener(new View.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				EditText meetingField = (EditText) view;
				dialpadAdapter.setField(meetingField);
				Log.e("meetingField", "hasFocus: " + hasFocus);
			}
		});
		
		//Initialize buttons
		callButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				String meetingNumber = meetingField.getText().toString();
				String url = "tel:" + phoneNumber + "," + meetingNumber;
				recentPhoneURI = url;
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
				startActivity(intent);
			}
			
		});

		//Disable keyboard on phone & meeting fields
		meetingField.setInputType(InputType.TYPE_NULL);
		
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
			
			/**
			 * Attempt to return back to SCB after disconnected call
			 */
			public void onCallStateChanged(int state, String incomingNumber){
				switch(state){
					case TelephonyManager.CALL_STATE_RINGING:break;
					case TelephonyManager.CALL_STATE_OFFHOOK:break;
					case TelephonyManager.CALL_STATE_IDLE:break;
					default:break;	
				}
			}
		};
		
		telManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE |
				PhoneStateListener.LISTEN_SERVICE_STATE |
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	

		dialpad.setAdapter(dialpadAdapter);
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
	
	private void initializeResources()
	{
		telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setPhoneNumber(int countryID)
	{
		switch(countryID){
			case Constants.US_WEST: phoneNumber = "14085256800";break;
			case Constants.US_EAST: phoneNumber = "19193923330";break;
			case Constants.ALGERIA_ALGIERS: phoneNumber = "21321989047";break;
			
		}
		
		Log.e("Selected Country Number", phoneNumber);
	}
}
