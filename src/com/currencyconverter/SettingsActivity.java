package com.currencyconverter;

import com.example.currencyconverter.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
{
             
             @Override
             public void onCreate(Bundle savedInstanceState) 
             {
                     super.onCreate(savedInstanceState);
                     addPreferencesFromResource(R.xml.pref_general);
                     

             }

} 