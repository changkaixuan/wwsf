package com.bocsoft.wwsf.webconsole.model;

public class CleanRules {
	
	private String id;
	private String product;
	private String type;
	private String jobIds;
	private String jobNames;
	private String keepTime;
	private int clearRunningJob;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getJobNames() {
		return jobNames;
	}
	public void setJobNames(String jobNames) {
		this.jobNames = jobNames;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getJobIds() {
		return jobIds;
	}
	public void setJobIds(String jobIds) {
		this.jobIds = jobIds;
	}
	public String getKeepTime() {
		return keepTime;
	}
	public void setKeepTime(String keepTime) {
		this.keepTime = keepTime;
	}
	public int getClearRunningJob() {
		return clearRunningJob;
	}
	public void setClearRunningJob(int clearRunningJob) {
		this.clearRunningJob = clearRunningJob;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.getId().equals(((CleanRules)obj).getId());
	}
}
