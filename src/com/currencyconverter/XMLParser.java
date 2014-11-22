package com.currencyconverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * Class to extract relevant currency data from XML file to text file.
 * 
 * Based on RSSParser by Anders Lindström
 * @author Fredrik Ljung
 *
 */

public class XMLParser {

	/**
	 * Parses XML data to Currency objects.
	 * 
	 * @param sourceXMLFile XML file containing currencies
	 * @param toTextFile Destination for relevant data from XML file
	 * @return Parsed currencies as ArrayList of Currencies.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static ArrayList<Currency> parseCurrencies(File sourceXMLFile, File toTextFile)
			throws XmlPullParserException, IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(toTextFile,false)));
		
		ArrayList<Currency> currencies = new ArrayList<Currency>();
		XmlPullParser parser;
		parser = XmlPullParserFactory.newInstance().newPullParser();
		try {
			parser.setInput(new FileInputStream(sourceXMLFile), null);
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
						out.println(currency.getName()+"@"+currency.getRate());
					}
				}
			default:
				break;
			}
			
			parseEvent = parser.next();
		}
		out.println("EUR@1");
		out.close();
		currencies.add(new Currency("EUR", 1));
		return currencies;
	}
/**
 * Parses an XML item.
 * @param parser
 * @return A currency object if success and null if failure
 * @throws XmlPullParserException
 * @throws IOException
 */
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
