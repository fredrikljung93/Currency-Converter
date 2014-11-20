package com.currencyconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View.OnLongClickListener;

public class MainActivity extends Activity {

	private static final String XMLURL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

	private Spinner fromSpinner;
	private Spinner toSpinner;
	private EditText fromInput;
	private EditText resultEditText;

	private ClipboardManager clipboard;
	private DownloadXML downloadTask;

	private CurrencyAdapter adapter;

	private SharedPreferences sharedPrefs;

	@Override
	protected void onStop() {
		super.onStop();
		// downloadTask.cancel(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.fromInput = (EditText) findViewById(R.id.editText1);

		this.fromSpinner = (Spinner) findViewById(R.id.spinner1);
		this.toSpinner = (Spinner) findViewById(R.id.spinner2);

		this.resultEditText = (EditText) findViewById(R.id.editText2);
		
		this.fromInput.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				calculate();
				
			}
		});
		resultEditText.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("currency",
						resultEditText.getText().toString());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(getApplicationContext(), "Successfully copied",
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		List<Currency> spinnerArray = new ArrayList<Currency>();
		adapter = new CurrencyAdapter(getApplicationContext(), spinnerArray);

		OnItemSelectedListener oisl = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				calculate();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		};

		this.fromSpinner.setOnItemSelectedListener(oisl);
		this.toSpinner.setOnItemSelectedListener(oisl);

		this.fromSpinner.setAdapter(adapter);
		this.toSpinner.setAdapter(adapter);

		File file = new File(getFilesDir(), "currencies");
		long daysSinceUpdate = (System.currentTimeMillis() - file
				.lastModified()) / (1000 * 60 * 60 * 24);
		Log.d("Hours since update", daysSinceUpdate + "");
		String syncfreqstring = sharedPrefs.getString("prefSyncFrequency", "1");
		long syncfreq = Long.parseLong(syncfreqstring);
		if (syncfreq == -1) {
			syncfreq = Long.MAX_VALUE;
		}
		if (!file.exists() || daysSinceUpdate >= syncfreq) {
			Log.d("downloadTask.execute", "File didnt exists or file is old");
			downloadTask = new DownloadXML(XMLURL);
			downloadTask.execute();
		} else {
			Log.d("onStart", "Using old data");
				ReadCurrencies task = new ReadCurrencies(new File(getFilesDir(),"currencies"));
				task.execute();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	//	downloadTask.cancel(true);
	}

	public void updateAdapters() throws XmlPullParserException, IOException {
		File xmlfile = new File(getCacheDir(), "XML");
		File toFile = new File(getFilesDir(), "currencies");
		List<Currency> spinnerArray = XMLParser.parseCurrencies(xmlfile,toFile);
		adapter = new CurrencyAdapter(getApplicationContext(), spinnerArray);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
	}
	
	public void updateAdapters(ArrayList<Currency> currencies) {
		adapter = new CurrencyAdapter(getApplicationContext(), currencies);
		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
	}

	public void calculate() {
		Currency from = (Currency) fromSpinner.getSelectedItem();
		Currency to = (Currency) toSpinner.getSelectedItem();

		try {
			double fromInEURO = Double.parseDouble(fromInput.getText().toString())
					/ from.getRate();
			resultEditText.setText(fromInEURO * to.getRate() + "");
		} catch (NumberFormatException ne) {
			resultEditText.setText("");
		}
	}
	public void flipButtonClick(View view) {
		int fromPosition = fromSpinner.getSelectedItemPosition();
		int toPosition = toSpinner.getSelectedItemPosition();
		fromInput.setText(resultEditText.getText());
		fromSpinner.setSelection(toPosition);
		toSpinner.setSelection(fromPosition);
		calculate(); // Trigger new conversion
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
			String syncfreq = sharedPrefs.getString("prefSyncFrequency", "1");
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
				File file = new File(getCacheDir(), "XML");
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
								R.string.networkfailure, Toast.LENGTH_LONG)
								.show();

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
	
	private class ReadCurrencies extends AsyncTask<Void, Void, Void> {
		private File file;
		private ArrayList<Currency> currencies;

		public ReadCurrencies(File file) {
			this.file=file;
			this.currencies=new ArrayList<Currency>();

		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d("ReadCurrencies", "READ CURRENCIES IS RUNNING!");
			BufferedReader reader=null;
			try {
				reader = new BufferedReader(new FileReader(file));
				
				
				String line = reader.readLine();

		        while (line != null) {
		        	Log.d("READLINE", line);
		        	String[] data = line.split("@");
		        	Log.d("READLINE length", ""+data.length);
		        	Log.d("READLINE 0", data[0]);
		        	Log.d("READLINE 1", data[1]);
		        	currencies.add(new Currency(data[0],Double.parseDouble(data[1])));
		            line = reader.readLine();
		        }
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					if(reader!=null){
						reader.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			updateAdapters(currencies);
		}
	}
	
}
