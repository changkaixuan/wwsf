package com.bocsoft.wwsf.webconsole.model;

public class Parameter {

	private String jobId;
	private String sourceId;
	private String name;
	private String value = "";
	private String desc;
	private boolean required; // select用 1true; 0false
	private int dRequired; // insert用 1true; 0false
	private String dataType;
	private String dataRange;

	// cron、job/task模板编辑用参数
	private int tempParamType = 1;
	
	private String paramType = "STRING";
	
	private int importType; //job模板导入取值：ImportJob.java

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(int required) {
		if (required == 1) {
			this.required = true;
		} else {
			this.required = false;
		}
	}

	public int getdRequired() {
		return dRequired;
	}

	public void setdRequired(int dRequired) {
		this.dRequired = dRequired;
	}

	public int getTempParamType() {
		return tempParamType;
	}

	public void setTempParamType(int tempParamType) {
		this.tempParamType = tempParamType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		if(null != paramType && !paramType.equals("")) {
			this.paramType = paramType;
		}else{
			this.paramType = "STRING";
		}
	}

	public String getDataRange() {
		return dataRange;
	}

	public void setDataRange(String dataRange) {
		this.dataRange = dataRange;
	}

	public int getImportType() {
		return importType;
	}

	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
