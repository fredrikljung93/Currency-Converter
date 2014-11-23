package com.currencyconverter;

/**
 * Instances of this class represents a currency
 * @author Fredrik Ljung
 *
 */
public class Currency {
	/**
	 * Rate of the currency
	 */
	private double rate;
	
	/**
	 * Name of the currency
	 */
	private String name;
	
	public Currency(String name, double rate){
		this.name=name;
		this.rate=rate;
	}
	
	/**
	 * @return Rate of the currency
	 */
	public double getRate() {
		return rate;
	}
	/**
	 * 
	 * @param rate New rate
	 */
	public void setrate(double rate) {
		this.rate = rate;
	}
	/**
	 * 
	 * @return Name of the currency
	 */
	public String getName() {
		return name;
	}
	/**
	 * 
	 * @param name New name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}

}
