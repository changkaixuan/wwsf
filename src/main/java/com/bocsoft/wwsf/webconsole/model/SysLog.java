package com.bocsoft.wwsf.webconsole.model;

import com.alibaba.fastjson.JSON;

public class SysLog {
	
	private String id;
	private String logDesc;
	private String logType;
	private String modName;
	private String optDate;
	private String optName;
	private String userId;
	private String optRst;
	private String userUip;
	private String targetTab;

	// 模块名称
	public final static String Mod_Name_Server = "服务器";

	// 操作名称
	public final static String Opt_Name_Insert = "新增";
	public final static String Opt_Name_Delete = "删除";
	public final static String Opt_Name_Update = "修改";
	public final static String Opt_Name_Install = "安装";
	public final static String Opt_Name_Uninstall = "卸载";

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getOptDate() {
		return optDate;
	}

	public void setOptDate(String optDate) {
		this.optDate = optDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLogDesc() {
		return logDesc;
	}

	public void setLogDesc(String logDesc) {
		this.logDesc = logDesc;
	}

	public String getModName() {
		return modName;
	}

	public void setModName(String modName) {
		this.modName = modName;
	}

	public String getOptName() {
		return optName;
	}

	public void setOptName(String optName) {
		this.optName = optName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOptRst() {
		return optRst;
	}

	public void setOptRst(String optRst) {
		this.optRst = optRst;
	}

	public String getUserUip() {
		return userUip;
	}

	public void setUserUip(String userUip) {
		this.userUip = userUip;
	}

	public String getTargetTab() {
		return targetTab;
	}

	public void setTargetTab(String targetTab) {
		this.targetTab = targetTab;
	}

	public String toString() {
		return JSON.toJSONString(this);
	}
}
