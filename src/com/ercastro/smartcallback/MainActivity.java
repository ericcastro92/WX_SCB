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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;

public class MainActivity extends Activity 
{	
	private TelephonyManager telManager;
	private final MainActivity ma = this;
	
	private boolean droppedCall = false;
	private boolean inCall = false;
	
	private String phoneNumber;
	private String recentPhoneURI;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Full screen setup
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		//Get layout buttons/views/etc
		final EditText meetingField = (EditText) findViewById(R.id.meetingNumberInput);
		//final EditText passwordField = (EditText) findViewById(R.id.meetingPasswordInput);
		final Spinner regionSelector = (Spinner) findViewById(R.id.phoneInput);
		Button callButton = (Button) findViewById(R.id.callButton);
		GridView dialpad = (GridView) findViewById(R.id.dialpad);
		
		//TelephonyManager setup
		initializeResources();
		final DialpadAdapter dialpadAdapter = new DialpadAdapter(meetingField, this);
		
		//Set up spinner
		regionSelector.setPrompt("Region");
		ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
				R.array.countries, android.R.layout.simple_spinner_item);
		countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		regionSelector.setAdapter(countryAdapter);
		regionSelector.setOnItemSelectedListener(new RegionSelectedListener(this));
		
		//Initialize buttons
		callButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				String meetingNumber = meetingField.getText().toString();
				//Each comma is a 2-second pause
				//                   			 Region number      Meeting number      Attendee ID
				String uri = "tel:" + Uri.encode(phoneNumber + "," + meetingNumber + ",," + "#");
				recentPhoneURI = uri;
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_CALL);
				intent.setData(Uri.parse(recentPhoneURI));
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
				//Log.e("Signal Strength", ""+strength);
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
						droppedCall = true;
						state = "Out of service";
						break;
					case ServiceState.STATE_POWER_OFF:
						droppedCall = true;
						state = "Power off";
						break;
					default: 
						state = "Invalid State ID"; 
						break;
				}
				
				Log.e("Service State", state);
			}
			
			/**
			 * Attempt to return back to SCB after disconnected call
			 */
			public void onCallStateChanged(int state, String incomingNumber){
				switch(state){
					case TelephonyManager.CALL_STATE_RINGING:
						Log.e("CALL_STATE", "Ringing");
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						Log.e("CALL_STATE", "Offhook");
						inCall = true;
						//Return to SCB once call is connected
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						Log.e("CALL_STATE", "Idle");
						Log.e("Dropped Call (TELE)", "" + droppedCall);
						if(!droppedCall)
							inCall = false;
						break;
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

	/**
	 * Redials most recent number
	 */
	public void callBack()
	{
		Log.e("DroppedCall & InCall", droppedCall + " " + inCall);
		if(!droppedCall || !inCall)
			return;
		
		droppedCall = false;
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_CALL);
		intent.setData(Uri.parse(recentPhoneURI));
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
			case Constants.ARGENTINA_BUENOS_AIRES: phoneNumber = "541143410101";break;
			case Constants.AUSTRALIA_CANBERRA: phoneNumber = "61262160643";break;
			default:phoneNumber="";break;
		}
		
		Log.e("Selected Country Number", phoneNumber);
	}
}
