package com.example.currencyconverter;


public class Currency {
	
	private float value;
	private String name;
	
	public Currency(String name, float value){
		this.name=name;
		this.value=value;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
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
