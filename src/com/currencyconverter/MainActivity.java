package com.currencyconverter;

import java.io.BufferedReader;
import java.io.File;
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
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View.OnLongClickListener;

/**
 * The main activity for the Currency converter android application XML data for
 * currencys quoted against euro (base currency) can be downloaded from
 * http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml
 * 
 * This application uses this data to enable the user to convert between the
 * different currencies
 * 
 * @author Fredrik Ljung
 * 
 */
public class MainActivity extends Activity {

	private static final String XMLURL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

	/**
	 * Spinner with currency user wants to convert from
	 */
	private Spinner fromSpinner;

	/**
	 * Spinner with currency user wants to convert to
	 */
	private Spinner toSpinner;

	/**
	 * Display and allow user to change value of the currency the user wants to
	 * convert from
	 */
	private EditText fromInput;

	/**
	 * Displays converted value
	 */
	private EditText resultEditText;

	/**
	 * Android clip board
	 */
	private ClipboardManager clipboard;

	/**
	 * AsyncTask to download XMl file
	 */
	private DownloadXML downloadTask;
	
	/**
	 * AsyncTask to parse downloaded currency data
	 */
	private ReadCurrencies readCurrenciesTask;

	/**
	 * Spinner adapter containing all currencies and how to graphically
	 * represent them
	 */
	private CurrencyAdapter adapter;

	/**
	 * Shared preferences
	 */
	private SharedPreferences sharedPrefs;

	@Override
	protected void onStop() {
		super.onStop();
		// downloadTask.cancel(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		LinearLayout bgElement = (LinearLayout) findViewById(R.id.MyLinearLayout);
		if (sharedPrefs.getString("choosebackground", "0").equals("1")) {
			bgElement.setBackgroundColor(Color.RED);
		} else {
			bgElement.setBackgroundColor(Color.LTGRAY);

		}
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
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
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
		readCurrenciesTask = new ReadCurrencies(new File(getFilesDir(),
					"currencies"));
			readCurrenciesTask.execute();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		downloadTask.cancel(true);
		
	}

	/**
	 * Updates adapters using downloaded XML stored in cacheDir
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void updateAdapters() throws XmlPullParserException, IOException {
		File xmlfile = new File(getCacheDir(), "XML");
		File toFile = new File(getFilesDir(), "currencies");
		List<Currency> spinnerArray = XMLParser
				.parseCurrencies(xmlfile, toFile);
		adapter = new CurrencyAdapter(getApplicationContext(), spinnerArray);

		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
	}

	/**
	 * Updates adapter
	 * 
	 * @param currencies
	 *            currencies sent to adapters
	 */
	public void updateAdapters(ArrayList<Currency> currencies) {
		adapter = new CurrencyAdapter(getApplicationContext(), currencies);
		fromSpinner.setAdapter(adapter);
		toSpinner.setAdapter(adapter);
	}

	/**
	 * Calculates currency conversion and displays result
	 */
	public void calculate() {
		Currency from = (Currency) fromSpinner.getSelectedItem();
		Currency to = (Currency) toSpinner.getSelectedItem();

		try {
			double fromInEURO = Double.parseDouble(fromInput.getText()
					.toString()) / from.getRate();
			resultEditText.setText(fromInEURO * to.getRate() + "");
		} catch (NumberFormatException ne) {
			resultEditText.setText("");
		}
	}

	/**
	 * Called when user clicks the flip button. Flips the TO and FROM currencies
	 * with each other
	 * 
	 * @param view
	 */
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

	/**
	 * AsyncTask to download XML data to cache dir
	 * 
	 * @author Fredrik
	 * 
	 */
	private class DownloadXML extends AsyncTask<Void, Void, String> {
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

		@SuppressWarnings("finally")
		@Override
		protected String doInBackground(Void... params) {
			InputStream is = null;
			FileOutputStream fileOutput = null;
			try {
				URL url = new URL(urlstring);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				is = connection.getInputStream();
				File cacheFile = new File(getCacheDir(), "XML");
				fileOutput = new FileOutputStream(cacheFile);

				int totalSize = connection.getContentLength();
				int downloadedSize = 0;
				byte[] buffer = new byte[1024];
				int bufferLength = 0;

				while ((bufferLength = is.read(buffer)) > 0) {
					if (isCancelled()) { // Stop downloading and kill thread if
											// application has been destroyed
						is.close();
						fileOutput.close();
						return null;
					}
					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					progress.setProgress(downloadedSize / totalSize);
				}
				fileOutput.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				try {
					if (is != null) {
						is.close();
					}
					if (fileOutput != null) {
						fileOutput.close();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					return "NETWORKFAILURE";
				}
			}
			Log.d("ASYNCTASK DONE", "ASYNCTASK DONE");
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return "SUCCESS";

		}

		@Override
		protected void onPostExecute(String result) {
			progress.dismiss();
			if (result.equalsIgnoreCase("NETWORKFAILURE")) {
				Toast.makeText(MainActivity.this.getApplicationContext(),
						R.string.networkfailure, Toast.LENGTH_LONG).show();
			}
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

		@Override
		protected void onCancelled(String result) {
			if (progress != null) {
				progress.dismiss();
			}
		}
	}

	/**
	 * AsyncTask to read currencies from text file.
	 * 
	 * @author Fredrik
	 * 
	 */
	private class ReadCurrencies extends AsyncTask<Void, Void, Void> {
		private File file;
		private ArrayList<Currency> currencies;

		public ReadCurrencies(File file) {
			this.file = file;
			this.currencies = new ArrayList<Currency>();

		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d("ReadCurrencies", "READ CURRENCIES IS RUNNING!");
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));

				String line = reader.readLine();

				while (line != null) {
					if(isCancelled()){
						try {
							if (reader != null) {
								reader.close();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
					Log.d("READLINE", line);
					String[] data = line.split("@");
					Log.d("READLINE length", "" + data.length);
					Log.d("READLINE 0", data[0]);
					Log.d("READLINE 1", data[1]);
					currencies.add(new Currency(data[0], Double
							.parseDouble(data[1])));
					line = reader.readLine();
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (reader != null) {
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
