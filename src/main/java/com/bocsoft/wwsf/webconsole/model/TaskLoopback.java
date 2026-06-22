package com.bocsoft.wwsf.webconsole.model;

import java.util.List;
import java.util.Map;

public class TaskLoopback {

	private String startPort;
	private String checkPort;
	Map<String,List<String>> connects;
	
	public String getStartPort() {
		return startPort;
	}
	public void setStartPort(String startPort) {
		this.startPort = startPort;
	}
	public String getCheckPort() {
		return checkPort;
	}
	public void setCheckPort(String checkPort) {
		this.checkPort = checkPort;
	}
	public Map<String, List<String>> getConnects() {
		return connects;
	}
	public void setConnects(Map<String, List<String>> connects) {
		this.connects = connects;
	}
	
}
