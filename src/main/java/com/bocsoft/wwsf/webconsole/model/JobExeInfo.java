package com.bocsoft.wwsf.webconsole.model;

public class JobExeInfo {
	private String infoId;
	private String jobId;
	private String product;
	private String infoDesc;
	private String executeTime;
	private String creator;
	private String infoParameter;
	private int top;
	private int status;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getExecuteTime() {
		return executeTime;
	}
	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}
	public int getTop() {
		return top;
	}
	public void setTop(int top) {
		this.top = top;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getInfoId() {
		return infoId;
	}
	public void setInfoId(String infoId) {
		this.infoId = infoId;
	}
	public String getInfoDesc() {
		return infoDesc;
	}
	public void setInfoDesc(String infoDesc) {
		this.infoDesc = infoDesc;
	}
	public String getInfoParameter() {
		return infoParameter;
	}
	public void setInfoParameter(String infoParameter) {
		this.infoParameter = infoParameter;
	}
}
