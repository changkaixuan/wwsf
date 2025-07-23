package com.bocsoft.wwsf.webconsole.model;

public class ProductCertification {

	private String pId; // 产品
	private String authId; // 认证信息编号
	private String protocal; // 协议
	private String userId; // 用户名
	private String publicKey; // 密钥
	
	private int importType; //job模板导入取值：1insert;2delete;3update

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public String getProtocal() {
		return protocal;
	}

	public void setProtocal(String protocal) {
		this.protocal = protocal;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public int getImportType() {
		return importType;
	}
	
	public void setImportType(int importType) {
		this.importType = importType;
	}

}
