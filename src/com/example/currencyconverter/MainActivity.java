package com.example.currencyconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Spinner fromSpinner;
	private Spinner toSpinner;
	private EditText fromInput;
	private TextView resultView;

	private ArrayAdapter<Currency> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.fromInput = (EditText) findViewById(R.id.editText1);

		this.fromSpinner = (Spinner) findViewById(R.id.spinner1);
		this.toSpinner = (Spinner) findViewById(R.id.spinner2);

		this.resultView = (TextView) findViewById(R.id.textView3);

		List<Currency> spinnerArray = new ArrayList<Currency>();
		adapter = new ArrayAdapter<Currency>(this,
				android.R.layout.simple_spinner_item, spinnerArray);

		this.fromSpinner.setAdapter(adapter);
		this.toSpinner.setAdapter(adapter);

		DownloadXML task = new DownloadXML(
				"http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
		task.execute();
	}

	public void convertButtonClick(View view) {
		Currency from = (Currency) fromSpinner.getSelectedItem();
		Currency to = (Currency) toSpinner.getSelectedItem();
		float fromInEURO = Float.parseFloat(fromInput.getText().toString())
				/ from.getRate();
		resultView.setText(fromInEURO * to.getRate() + "");
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

	private class DownloadXML extends AsyncTask<Void, Void, Void> {
		private String urlstring;
		private ArrayList<Currency> newCurrencies;
		private ProgressDialog progress;

		public DownloadXML(String urlstring) {
			this.progress = new ProgressDialog(MainActivity.this);
			progress.setTitle("Downloading currencies");
			progress.show();
			this.urlstring = urlstring;
			newCurrencies = new ArrayList<Currency>();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				File file = new File(getFilesDir(), "XML");
				FileOutputStream fileOutput = new FileOutputStream(file);
				URL url = new URL(urlstring);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				InputStream is =connection.getInputStream();

				int totalSize = connection.getContentLength();
				int downloadedSize = 0;
				byte[] buffer = new byte[1024];
				int bufferLength = 0;

				while ((bufferLength = is.read(buffer)) > 0) {
					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					progress.setProgress(bufferLength/totalSize);
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
								"Network failure", Toast.LENGTH_LONG).show();

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
			File file = new File(getFilesDir(), "XML");
			List<Currency> spinnerArray = XMLParser.getCurrencies(file);
			adapter = new ArrayAdapter<Currency>(MainActivity.this,
					android.R.layout.simple_spinner_item, spinnerArray);

			fromSpinner.setAdapter(adapter);
			toSpinner.setAdapter(adapter);
		}
	}
}
