package com.ercastro.smartcallback;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class RegionSelectedListener implements OnItemSelectedListener
{	
	MainActivity ma;
	
	public RegionSelectedListener(MainActivity ma){
		this.ma = ma;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
	{
		ma.setPhoneNumber(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) 
	{	
	}
}
