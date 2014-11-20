package com.currencyconverter;

import android.os.Parcel;
import android.os.Parcelable;


public class Currency implements Parcelable {
	
	private double rate;
	private String name;
	
	public Currency(String name, double rate){
		this.name=name;
		this.rate=rate;
	}
	
	  public Currency(Parcel in){
          String[] data = new String[2];
          in.readStringArray(data);
          this.rate = Double.parseDouble(data[0]);
          this.name = data[1];
      }
	
	public double getRate() {
		return rate;
	}
	public void setrate(double rate) {
		this.rate = rate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		 dest.writeStringArray(new String[] {this.rate+"",
                 this.name});
	}

}
