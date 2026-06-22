package com.bocsoft.wwsf.webconsole.model;

public class TaskLean {

	private String jobId;
	private String taskId;
	private String leanJobId;
	private String leanTaskId;
	
	private Integer type;
	private Integer includeDimension;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getLeanJobId() {
		return leanJobId;
	}
	public void setLeanJobId(String leanJobId) {
		this.leanJobId = leanJobId;
	}
	public String getLeanTaskId() {
		return leanTaskId;
	}
	public void setLeanTaskId(String leanTaskId) {
		this.leanTaskId = leanTaskId;
	}
	
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getIncludeDimension() {
		return includeDimension;
	}
	public void setIncludeDimension(Integer includeDimension) {
		this.includeDimension = includeDimension;
	}
	
}
