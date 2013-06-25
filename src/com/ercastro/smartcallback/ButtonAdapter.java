package com.ercastro.smartcallback;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;

public class ButtonAdapter extends BaseAdapter
{
	private Context context;
	private Button[] dialpad;
	
	@Override
	public int getCount() 
	{
		return dialpad.length;
	}

	@Override
	public Object getItem(int position) 
	{
		return dialpad[position];
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		Button button = (Button) convertView;
		return button;
	}
}
