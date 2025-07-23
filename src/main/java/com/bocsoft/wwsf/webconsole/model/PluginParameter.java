package com.bocsoft.wwsf.webconsole.model;

public class PluginParameter {
	
	private String pluginName;
	private String paramName;
	private String dataType;
	private String paramDefValue;
	private int isRequired;
	private String paramDesc;
	private String dataRange;
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getPluginName() {
		return pluginName;
	}
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getParamDefValue() {
		return paramDefValue;
	}
	public void setParamDefValue(String paramDefValue) {
		this.paramDefValue = paramDefValue;
	}
	
	public String getParamDesc() {
		return paramDesc;
	}
	public void setParamDesc(String paramDesc) {
		this.paramDesc = paramDesc;
	}
	public String getDataRange() {
		return dataRange;
	}
	public void setDataRange(String dataRange) {
		this.dataRange = dataRange;
	}
	public int getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(int isRequired) {
		this.isRequired = isRequired;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
	
}
