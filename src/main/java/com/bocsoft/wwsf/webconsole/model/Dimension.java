package com.bocsoft.wwsf.webconsole.model;

public class Dimension {

	private String product;
	private String name;
	private String description;
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
