package com.bocsoft.wwsf.webconsole.model;

import java.util.List;
import java.util.Map;

public class Batch {

	public List<BatchJob> jobs;
	public Map<String, String> commonParameters;
	
	public List<BatchJob> getJobs() {
		return jobs;
	}
	public void setJobs(List<BatchJob> jobs) {
		this.jobs = jobs;
	}
	public Map<String, String> getCommonParameters() {
		return commonParameters;
	}
	public void setCommonParameters(Map<String, String> commonParameters) {
		this.commonParameters = commonParameters;
	}
	
}
