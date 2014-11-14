package com.currencyconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class XMLParser {

	public static ArrayList<Currency> getCurrencies(File file)
			throws XmlPullParserException, IOException {
		ArrayList<Currency> currencies = new ArrayList<Currency>();
		XmlPullParser parser;
		parser = XmlPullParserFactory.newInstance().newPullParser();
		try {
			parser.setInput(new FileInputStream(file), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int parseEvent = parser.getEventType();
		while (parseEvent != XmlPullParser.END_DOCUMENT) {
			switch (parseEvent) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equalsIgnoreCase("cube")) {

					Currency currency = parseItem(parser);
					if (currency != null) {
						currencies.add(currency);
					}
				}
			default:
				break;
			}
			
			parseEvent = parser.next();
		}
		currencies.add(new Currency("EUR", 1));
		return currencies;
	}

	private static Currency parseItem(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		
		String name=parser.getName();
		if(name.equalsIgnoreCase("cube")){
			String currency = parser.getAttributeValue(null, "currency");
			String ratestring=parser.getAttributeValue(null, "rate");
			if(currency==null){return null;}
			if(ratestring==null){return null;}
			Double rate = Double.parseDouble(ratestring);
			Log.d("NEW CURRENCY", currency+", "+rate);
			return new Currency(currency, rate);
		}
		return null;
		
	}

}
