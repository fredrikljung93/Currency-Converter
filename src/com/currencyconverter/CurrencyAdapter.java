package com.currencyconverter;

import java.util.List;

import com.example.currencyconverter.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Currency adapter for spinners. Instances of this class contains currency data
 * and tells view how to graphically represent the currency in a spinner
 * 
 * @author Fredrik Ljung
 * 
 */
public class CurrencyAdapter extends ArrayAdapter<Currency> {

	public CurrencyAdapter(Context context, List<Currency> currencies) {
		super(context, R.layout.customspinner, R.id.textview1337, currencies);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.customspinner, parent, false);
		TextView textview = (TextView) rowView.findViewById(R.id.textview1337);
		Currency currency = getItem(position);
		textview.setText(currency.getName());
		return rowView;
	}
}
