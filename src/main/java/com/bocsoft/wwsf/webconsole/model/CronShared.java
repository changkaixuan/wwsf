package com.bocsoft.wwsf.webconsole.model;

public class CronShared {
	private String product;
	private String agent;
	private String jobId;
	private String sharedJson;
	private String occuTime;
	
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getSharedJson() {
		return sharedJson;
	}
	public void setSharedJson(String sharedJson) {
		this.sharedJson = sharedJson;
	}
	public String getOccuTime() {
		return occuTime;
	}
	public void setOccuTime(String occuTime) {
		this.occuTime = occuTime;
	}
}
