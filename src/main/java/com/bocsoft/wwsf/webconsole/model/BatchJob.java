package com.bocsoft.wwsf.webconsole.model;

import java.util.Map;

public class BatchJob {

	public String jobId;
	public Map<String, String> parameters;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
}
