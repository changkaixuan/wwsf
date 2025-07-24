package com.bocsoft.wwsf.webconsole.websocket;

public class SshConnInfo {
	
	private String ipAddr;
	private int osSshPort;
	private String osSshUser;
	private String osSshPswd;
	private String charsetName;
	/**
	 * SHH 授权认证方式 0-密码认证； 1-公钥认证
	 */
	private String osSshAuth;

	/**
	 * SSH私钥路径
	 */
	private String privateKeyPath;

	/**
	 * SSH私钥密码（是用于保护私钥文件的密码,可为空）
	 */
	private String passphrase;

	/**
	 * SHH 授权默认1-公钥认证方式
	 */
	public final static String Ssh_Default_OsSshAuth = "1";
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

	public SshConnInfo(String ipAddr,int osSshPort,String osSshUser,String osSshPswd,String charsetName,String osSshAuth,String privateKeyPath,String passphrase){
		this.ipAddr = ipAddr;
		this.osSshPort = osSshPort;
		this.osSshUser = osSshUser;
		this.osSshPswd = osSshPswd;
		if(null == charsetName || charsetName.trim().equals("")) {
			this.charsetName = Ssh_Default_CharsetName;
		}else {
			this.charsetName = charsetName;
		}
		if(null == osSshAuth || osSshAuth.trim().equals("")) {
			this.osSshAuth = Ssh_Default_OsSshAuth;
		}else {
			this.osSshAuth = osSshAuth;
		}
        this.privateKeyPath = privateKeyPath;
		this.passphrase = passphrase;
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

	public String getOsSshAuth() {
		return osSshAuth;
	}

	public void setOsSshAuth(String osSshAuth) {
		this.osSshAuth = osSshAuth;
	}

	public String getPrivateKeyPath() {
		return privateKeyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
}
