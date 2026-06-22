package com.bocsoft.wwsf.webconsole.model;

import java.util.Date;

public class Server {

	private String product;
	private String serverId;
	private String serverDesc;
	private String ipAddr;
	private int sshPort;
	private int rmiRegistryPort;
	private String serverType;
	private String tags;
	private String latestOnlineTime;
	private Date dLatestOnlineTime;
	private String osSshUser;
	private String osSshPswd;
	private String zooConnectStr;
	private String zooNamespace;
	private String homeDir;
	private int osSshPort;
	private int rmiServerPort;
	private String ignorePlugins;
	private int status;
	private String versionNo;
	private Long cfgVersion;
	private Long installCfgVersion;
	private String installLog;

	public final static int ServerInstallLog_Status_NotInstall = 0;
	public final static int ServerInstallLog_Status_InstallFail = 1;
	public final static int ServerInstallLog_Status_InstallSuccess = 2;
	public final static int ServerInstallLog_Status_Start = 3;

	// public final static String Server_Default_Install_File_Path =
	// "installFilePath";
	public final static String Server_Default_Install_Name = "wwsf";
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getServerDesc() {
		return serverDesc;
	}

	public void setServerDesc(String serverDesc) {
		this.serverDesc = serverDesc;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getLatestOnlineTime() {
		return latestOnlineTime;
	}

	public void setLatestOnlineTime(String latestOnlineTime) {
		if (null != latestOnlineTime && latestOnlineTime.length() > 19) {
			this.latestOnlineTime = latestOnlineTime.substring(0, 19);
		} else {
			this.latestOnlineTime = latestOnlineTime;
		}
	}
	
	public Date getdLatestOnlineTime() {
		return dLatestOnlineTime;
	}

	public void setdLatestOnlineTime(Date dLatestOnlineTime) {
		this.dLatestOnlineTime = dLatestOnlineTime;
	}

	public String getOsSshUser() {
		return osSshUser;
	}

	public void setOsSshUser(String osSshUser) {
		this.osSshUser = osSshUser;
	}

	public String getOsSshPswd() {
		return osSshPswd;
	}

	public void setOsSshPswd(String osSshPswd) {
		this.osSshPswd = osSshPswd;
	}

	public String getZooConnectStr() {
		return zooConnectStr;
	}

	public void setZooConnectStr(String zooConnectStr) {
		this.zooConnectStr = zooConnectStr;
	}
	
	public String getZooNamespace() {
		return zooNamespace;
	}

	public void setZooNamespace(String zooNamespace) {
		this.zooNamespace = zooNamespace;
	}

	public String getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	public int getOsSshPort() {
		return osSshPort;
	}

	public void setOsSshPort(int osSshPort) {
		this.osSshPort = osSshPort;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getRmiRegistryPort() {
		return rmiRegistryPort;
	}

	public void setRmiRegistryPort(int rmiRegistryPort) {
		this.rmiRegistryPort = rmiRegistryPort;
	}

	public int getRmiServerPort() {
		return rmiServerPort;
	}

	public void setRmiServerPort(int rmiServerPort) {
		this.rmiServerPort = rmiServerPort;
	}
	
	public String getIgnorePlugins() {
		return ignorePlugins;
	}

	public void setIgnorePlugins(String ignorePlugins) {
		this.ignorePlugins = ignorePlugins;
	}

	public String getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(String versionNo) {
		this.versionNo = versionNo;
	}
	
	public Long getCfgVersion() {
		return cfgVersion;
	}

	public void setCfgVersion(Long cfgVersion) {
		this.cfgVersion = cfgVersion;
	}

	public Long getInstallCfgVersion() {
		return installCfgVersion;
	}

	public void setInstallCfgVersion(Long installCfgVersion) {
		this.installCfgVersion = installCfgVersion;
	}

	public String getInstallLog() {
		return installLog;
	}

	public void setInstallLog(String installLog) {
		this.installLog = installLog;
	}

	public int getImportType() {
		return importType;
	}

	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
