package com.bocsoft.wwsf.webconsole.model;

import java.util.List;

public class NodeInfo {
	// 所属产品
	private String product;
	private String nodeId;
	private String identity;
	private String type;
	// 是否可用
	private boolean available = true;
	private String ip;
	private Integer sshdPort;
	private Integer rmiRegistryPort;
	private Integer rmiServerPort;
	// 当前状态
	private boolean isMasterStandby;
	private Integer online;
	
	List<String> pluginNames;
	
	private String registTime;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public String getRegistTime() {
		return registTime;
	}

	public void setRegistTime(String registTime) {
		this.registTime = registTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String toString() {
		return product + ":" + nodeId + "/" + ip + ":(sshd)" + sshdPort + ":(jmx)" + rmiRegistryPort + "/" + identity;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Integer getSshdPort() {
		return sshdPort;
	}

	public void setSshdPort(Integer sshdPort) {
		this.sshdPort = sshdPort;
	}

	public List<String> getPluginNames() {
		return pluginNames;
	}

	public void setPluginNames(List<String> pluginNames) {
		this.pluginNames = pluginNames;
	}

	public boolean isMasterStandby() {
		return isMasterStandby;
	}

	public void setMasterStandby(boolean isMasterStandby) {
		this.isMasterStandby = isMasterStandby;
	}

	public Integer getOnline() {
		return online;
	}

	public void setOnline(Integer online) {
		this.online = online;
	}

	public Integer getRmiRegistryPort() {
		return rmiRegistryPort;
	}

	public void setRmiRegistryPort(Integer rmiRegistryPort) {
		this.rmiRegistryPort = rmiRegistryPort;
	}

	public Integer getRmiServerPort() {
		return rmiServerPort;
	}

	public void setRmiServerPort(Integer rmiServerPort) {
		this.rmiServerPort = rmiServerPort;
	}

}
