package com.currencyconverter;


public class Currency {
	
	private double rate;
	private String name;
	
	public Currency(String name, double rate){
		this.name=name;
		this.rate=rate;
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

}
