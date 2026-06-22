package com.bocsoft.wwsf.webconsole.model;

import java.util.List;
import java.util.Map;

public class JobPanelInfo {

	private String jobId;
	private String product;
	private Map<String,List<String>> leansRelationShip;
	private Map<String,List<String>> beLeansRelationShip;
	private Map<String,Map<String,Integer>> location;
	private List<Map<String,Object>> edges;
	
	private List<Task> tasks; //taskService.setJobPanelInfoAndTasks周转用
	
	public List<Map<String, Object>> getEdges() {
		return edges;
	}
	public void setEdges(List<Map<String, Object>> edges) {
		this.edges = edges;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getProduct() {
		return product;
	}
	public Map<String, List<String>> getBeLeansRelationShip() {
		return beLeansRelationShip;
	}
	public void setBeLeansRelationShip(Map<String, List<String>> beLeansRelationShip) {
		this.beLeansRelationShip = beLeansRelationShip;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public Map<String, List<String>> getLeansRelationShip() {
		return leansRelationShip;
	}
	public void setLeansRelationShip(Map<String, List<String>> leansRelationShip) {
		this.leansRelationShip = leansRelationShip;
	}
	public Map<String, Map<String, Integer>> getLocation() {
		return location;
	}
	public void setLocation(Map<String, Map<String, Integer>> location) {
		this.location = location;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}
	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
}
