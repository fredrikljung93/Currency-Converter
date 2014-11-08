package com.example.currencyconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class XMLParser {
	
	public static ArrayList<Currency> getCurrencies(File file){
		ArrayList<Currency> newCurrencies=new ArrayList<Currency>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line = null;
		try {
			line=reader.readLine();
			while(line!=null){
				if(line.toUpperCase().contains("CUBE CURRENCY")){
					String[] strings = line.split("'");
					newCurrencies.add(new Currency(strings[1],Float.parseFloat(strings[3])));
					Log.d("New Currency", strings[1]+", "+strings[3]);
				}
				else{
					Log.d("XML not wanted line", line);
				}
				line=reader.readLine();//Read next line
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		newCurrencies.add(new Currency("EUR", 1));
		return newCurrencies;
		
	}

}
