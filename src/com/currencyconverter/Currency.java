package com.currencyconverter;


public class Currency {
	
	private float rate;
	private String name;
	
	public Currency(String name, float rate){
		this.name=name;
		this.rate=rate;
	}
	public float getRate() {
		return rate;
	}
	public void setrate(float rate) {
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

}
