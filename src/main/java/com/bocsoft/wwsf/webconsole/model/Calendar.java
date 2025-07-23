package com.bocsoft.wwsf.webconsole.model;

public class Calendar {
	private String product;
	private String calName;
	private String calDescription;
	private String includeValue;
	private String exclusionValue;
	
	private int importType; //job模板导入取值：ImportJob.java

	public String getIncludeValue() {
		return includeValue;
	}
	public void setIncludeValue(String includeValue) {
		this.includeValue = includeValue;
	}
	public String getExclusionValue() {
		return exclusionValue;
	}
	public void setExclusionValue(String exclusionValue) {
		this.exclusionValue = exclusionValue;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getCalName() {
		return calName;
	}
	public void setCalName(String calName) {
		this.calName = calName;
	}
	public String getCalDescription() {
		return calDescription;
	}
	public void setCalDescription(String calDescription) {
		this.calDescription = calDescription;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
