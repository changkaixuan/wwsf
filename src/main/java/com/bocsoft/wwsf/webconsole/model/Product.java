package com.bocsoft.wwsf.webconsole.model;

public class Product {
	
	private String pId; //产品英文名
	private String pName; //产品中文名
	private String pDesc; //产品描述
	/*private String busiSort; //业务分类
	private String devMode; //开发方式
	private String memo; //产品备注
	private String testManager; //测试经理
	private String productManager; //产品经理*/	
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getpId() {
		return pId;
	}
	public void setpId(String pId) {
		this.pId = pId;
	}
	public String getpName() {
		return pName;
	}
	public void setpName(String pName) {
		this.pName = pName;
	}
	public String getpDesc() {
		return pDesc;
	}
	public void setpDesc(String pDesc) {
		this.pDesc = pDesc;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
