package com.currencyconverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.example.currencyconverter.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String XMLURL="http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

	private Spinner fromSpinner;
	private Spinner toSpinner;
	private EditText fromInput;
	private TextView resultView;

	private DownloadXML downloadTask;

	private CurrencyAdapter adapter;

	private SharedPreferences sharedPrefs;
	@Override
	protected void onStop() {
		super.onStop();
		//downloadTask.cancel(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.fromInput = (EditText) findViewById(R.id.editText1);

		this.fromSpinner = (Spinner) findViewById(R.id.spinner1);
		this.toSpinner = (Spinner) findViewById(R.id.spinner2);

		this.resultView = (TextView) findViewById(R.id.textView3);

		List<Currency> spinnerArray = new ArrayList<Currency>();
		adapter = new CurrencyAdapter(getApplicationContext(), spinnerArray);
		
		OnItemSelectedListener oisl=new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				convertButtonClick(null);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		};
		
		this.fromSpinner.setOnItemSelectedListener(oisl);
		this.toSpinner.setOnItemSelectedListener(oisl);

		this.fromSpinner.setAdapter(adapter);
		this.toSpinner.setAdapter(adapter);

		File file = new File(getFilesDir(), "XML");
		long daysSinceUpdate = (System.currentTimeMillis() - file
				.lastModified()) / (1000 * 60 * 60*24);
		Log.d("Hours since update", daysSinceUpdate + "");
		String syncfreqstring=sharedPrefs.getString("prefSyncFrequency", "1");
		long syncfreq=Long.parseLong(syncfreqstring);
		if(syncfreq==-1){
			syncfreq=Long.MAX_VALUE;
		}
		if (!file.exists() || daysSinceUpdate >= syncfreq){
			Log.d("downloadTask.execute","File didnt exists or file is old");
			downloadTask = new DownloadXML(XMLURL);
			downloadTask.execute();
		} else {
			Log.d("onStart", "Using old data");
			try {
				updateAdapters();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void updateAdapters() throws XmlPullParserException, IOException {
		File file = new File(getFilesDir(), "XML");
		List<Currency> spinnerArray = XMLParser.getCurrencies(file);
		adapter = new CurrencyAdapter(getApplicationContext(), spinnerArray);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
	}

	public void convertButtonClick(View view) {
		Currency from = (Currency) fromSpinner.getSelectedItem();
		Currency to = (Currency) toSpinner.getSelectedItem();
		
		try{
			float fromInEURO = Float.parseFloat(fromInput.getText().toString())
					/ from.getRate();
			resultView.setText(fromInEURO * to.getRate() + "");
		}
		catch(NumberFormatException ne){
			resultView.setText("");
		}
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
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
		} else if (id == R.id.action_updaterates) {
			String syncfreq=sharedPrefs.getString("prefSyncFrequency", "1");
			Log.d("Update rates", syncfreq);
			downloadTask = new DownloadXML(XMLURL);
			downloadTask.execute();
		}
		return super.onOptionsItemSelected(item);
	}

	private class DownloadXML extends AsyncTask<Void, Void, Void> {
		private String urlstring;
		private ArrayList<Currency> newCurrencies;
		private ProgressDialog progress;

		public DownloadXML(String urlstring) {
			this.progress = new ProgressDialog(MainActivity.this);
			progress.setTitle(R.string.downloading);
			progress.show();
			this.urlstring = urlstring;
			newCurrencies = new ArrayList<Currency>();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(urlstring);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				InputStream is = connection.getInputStream();
				File file = new File(getFilesDir(), "XML");
				FileOutputStream fileOutput = new FileOutputStream(file);

				int totalSize = connection.getContentLength();
				int downloadedSize = 0;
				byte[] buffer = new byte[1024];
				int bufferLength = 0;

				while ((bufferLength = is.read(buffer)) > 0) {
					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					progress.setProgress(downloadedSize / totalSize);
				}
				fileOutput.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(
								MainActivity.this.getApplicationContext(),
								R.string.networkfailure,
								Toast.LENGTH_LONG).show();

					}
				});

			}
			Log.d("ASYNCTASK DONE", "ASYNCTASK DONE");
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			progress.dismiss();
			Log.d("ONPOSTEXECUTE", "ONPOSTEXECUTE");
			Log.d("XML", "Size: " + newCurrencies.size());
			try {
				updateAdapters();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
