package com.ercastro.smartcallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;

public class MainActivity extends Activity 
{	
	private final MainActivity ma = this;
	private TelephonyManager telManager;
	
	private boolean droppedCall = false;
	private boolean inCall = false;
	
	private String phoneNumber;
	private String recentPhoneURI;
	private int region;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		//Full screen setup
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		//Get layout buttons/views/etc
		final EditText meetingField = (EditText) findViewById(R.id.meetingNumberInput);
		final EditText attendeeField = (EditText) findViewById(R.id.attendeeIDInput);
		final Spinner regionSelector = (Spinner) findViewById(R.id.phoneInput);
		Button callButton = (Button) findViewById(R.id.callButton);
		GridView dialpad = (GridView) findViewById(R.id.dialpad);
		
		//Get shared preferences
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		region = settings.getInt("Region", Constants.US_WEST);
		
		//TelephonyManager setup
		initializeResources();
		final DialpadAdapter dialpadAdapter = new DialpadAdapter(meetingField, this);
		
		//Set up spinner
		regionSelector.setPrompt("Region");
		ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(this,
				R.array.countries, android.R.layout.simple_spinner_item);
		countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		regionSelector.setAdapter(countryAdapter);
		regionSelector.setSelection(region, false);
		regionSelector.setOnItemSelectedListener(new RegionSelectedListener(this));
		
		//Set up focus listeners
		meetingField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean hasFocus)
			{
				if(hasFocus)
					dialpadAdapter.setField(meetingField);
			}
		});
		attendeeField.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean hasFocus) 
			{
				if(hasFocus)
					dialpadAdapter.setField(attendeeField);
			}
		});
		
		//Initialize buttons
		callButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				String meetingNumber = meetingField.getText().toString();
				String attendeeID = attendeeField.getText().toString();
				String uri = "";
				//Each comma is a 2-second pause
				//Format: phoneNumber  Region number    Meeting number   Attendee ID
				if(attendeeID!=null)
					uri =  "tel:" + Uri.encode(phoneNumber + "," + meetingNumber + ",," + attendeeID + "#");
				else
					uri = "tel:" + Uri.encode(phoneNumber + "," + meetingNumber + ",," + "#");
				recentPhoneURI = uri;
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_CALL);
				intent.setData(Uri.parse(recentPhoneURI));
				startActivity(intent);
			}
			
		});
		
		//Disable keyboard on meeting & attendee fields
		meetingField.setInputType(InputType.TYPE_NULL);
		attendeeField.setInputType(InputType.TYPE_NULL);
		
		//PhoneStateListener & callback method set up
		PhoneStateListener phoneStateListener = new PhoneStateListener()
		{	
			public void onSignalStrengthsChanged(SignalStrength signalStrength)
			{
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
		
		//Get & set URI data
		Uri data = getIntent().getData();
		if(data!=null)
		{
			String meetingNumber = data.getHost();
			Log.e("Meeting Number", meetingNumber);
			meetingField.setText(meetingNumber);
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("Region", region);
		editor.commit();
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

	public void setPhoneNumber(int regionID)
	{
		region = regionID;
		
		switch(regionID){
			case Constants.US_WEST: phoneNumber = "14085256800";break;
			case Constants.US_EAST: phoneNumber = "19193923330";break;
			case Constants.ALGERIA_ALGIERS: phoneNumber = "21321989047";break;
			case Constants.ARGENTINA_BUENOS_AIRES: phoneNumber = "541143410101";break;
			case Constants.AUSTRALIA_CANBERRA: phoneNumber = "61262160643";break;
			case Constants.AUSTRALIA_MELBOURNE: phoneNumber = "61396594173";break;
			case Constants.AUSTRALIA_NORTH_SYDNEY: phoneNumber = "61284466660";break;
			case Constants.AUSTRIA_VIENNA: phoneNumber = "431240306022";break;
			case Constants.AZERBAIJAN_BAKU: phoneNumber = "994124374829";break;
			case Constants.BANGLADESH_DHAKA: phoneNumber = "8809610127888";break;
			case Constants.BELGIUM_BRUSSELS: phoneNumber = "3227045072";break;
			case Constants.BOSNIA_HERZEGOVINA_SARAJEVO: phoneNumber = "38733562898";break;
			default:phoneNumber="";break;
		}
		
		Log.e("Selected Country Number", phoneNumber);
	}

	public void displayHelp()
	{
		AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(this);
		helpDialogBuilder.setTitle("Webex Smart Callback help");
		helpDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}
		});
		TextView helpText = new TextView(this);
		helpText.setText(R.string.help_text);
		helpText.setTextColor(Color.WHITE);
		helpText.setMaxLines(250);
		helpText.setMovementMethod(new ScrollingMovementMethod());
		
		helpDialogBuilder.setView(helpText);
		AlertDialog helpDialog = helpDialogBuilder.create();
		helpDialog.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_help:
				displayHelp();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
