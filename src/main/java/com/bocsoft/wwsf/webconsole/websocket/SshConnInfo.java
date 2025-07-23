package com.bocsoft.wwsf.webconsole.websocket;

public class SshConnInfo {
	
	private String ipAddr;
	private int osSshPort;
	private String osSshUser;
	private String osSshPswd;
	private String charsetName;

	public final static String Springboot_Websocket_Name = "webssh";
	public final static String Ssh_Default_CharsetName = "UTF-8";

	public SshConnInfo(){}
	
    public SshConnInfo(String ipAddr,int osSshPort,String osSshUser,String osSshPswd,String charsetName){
		this.ipAddr = ipAddr;
		this.osSshPort = osSshPort;
		this.osSshUser = osSshUser;
		this.osSshPswd = osSshPswd;
		if(null == charsetName || charsetName.trim().equals("")) {
			this.charsetName = Ssh_Default_CharsetName;
		}else {
			this.charsetName = charsetName;
		}
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getOsSshPort() {
		return osSshPort;
	}

	public void setOsSshPort(int osSshPort) {
		this.osSshPort = osSshPort;
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

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
	
}
