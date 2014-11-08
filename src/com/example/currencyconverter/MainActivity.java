package com.example.currencyconverter;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MainActivity extends Activity {
	
	private Spinner fromSpinner;
	private Spinner toSpinner;
	private EditText fromInput;
	private EditText toInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.fromInput=(EditText) findViewById(R.id.editText1);
		this.toInput=(EditText) findViewById(R.id.editText2);
		
		this.fromSpinner=(Spinner)findViewById(R.id.spinner1);
		this.toSpinner=(Spinner)findViewById(R.id.spinner2);
		
		List<Currency> spinnerArray =  new ArrayList<Currency>();
		spinnerArray.add(new Currency("EURO", 123));
		spinnerArray.add(new Currency("USD", 5435));
		ArrayAdapter<Currency> adapter = new ArrayAdapter<Currency>(
			    this, android.R.layout.simple_spinner_item, spinnerArray);
		
		this.fromSpinner.setAdapter(adapter);
		this.toSpinner.setAdapter(adapter);

		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
