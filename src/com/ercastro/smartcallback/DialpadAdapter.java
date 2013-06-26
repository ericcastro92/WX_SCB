package com.ercastro.smartcallback;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

public class DialpadAdapter extends BaseAdapter
{
	private Context context;
	private EditText field;
	
	public DialpadAdapter(EditText field, Context context)
	{
		this.context = context;	
		this.field = field;
	}
	
	public void setField(EditText field)
	{
		this.field = field;
	}
	
	@Override
	public int getCount() 
	{
		return 12;
	}

	@Override
	public Object getItem(int position) 
	{
		return null;
	}

	@Override
	public long getItemId(int position) 
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		Button button;
		
		if(convertView == null)
		{
			button = new Button(context);
			button.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,
															 GridView.LayoutParams.MATCH_PARENT));
		}
		else
		{
			button = (Button) convertView;
		}
		
		final String label;
		switch(position)
		{
			case 9: label = ""; break;
			case 10: label = "0"; break;
			case 11: label = "DEL"; break;
			default:label = Integer.toString(position + 1);break;
		}
		button.setText(label);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view) 
			{
				Log.e("Button", label);
				String curNum = field.getText().toString();
				curNum+=label;
				field.setText(curNum);
			}
		});
		
		if(position==11)
			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View view) 
				{
					Log.e("Button", label);
					String curNum = field.getText().toString();
					if(!curNum.isEmpty())
						field.setText(curNum.substring(0,curNum.length()-1));
				}
			});
		
		return button;
	}
}
