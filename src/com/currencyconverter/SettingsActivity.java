package com.currencyconverter;

import com.example.currencyconverter.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Activity for letting the user set its preferences
 * @author Fredrik
 *
 */
public class SettingsActivity extends PreferenceActivity 
{
             
             @SuppressWarnings("deprecation")
			@Override
             public void onCreate(Bundle savedInstanceState) 
             {
                     super.onCreate(savedInstanceState);
                     addPreferencesFromResource(R.xml.pref_general);
                     

             }

} 