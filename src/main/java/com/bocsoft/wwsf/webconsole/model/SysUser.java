package com.bocsoft.wwsf.webconsole.model;

import java.util.Date;
import java.util.List;

public class SysUser {
	
	private String loginTime;
	private String lastTime;
	private String userName;
	private String loginName;
	private String userPwd;
	private String userStat;
	private String userMobile;
	private String userEmail;
	private String lockFlag;
	private String lockTime;
	private String invalidDate;
	private String validDate;
	private int delFlag;
	private String createTime;
	private Date dCreateTime;
	private Date dInvalidDate;
	private Date dValidDate;
	
	private String oldUserPwd;
	
	private List<String> productList;
	private List<String> sysRoleList;
	
	public String getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getUserPwd() {
		return userPwd;
	}
	public void setUserPwd(String userPwd) {
		this.userPwd = userPwd;
	}
	public String getUserStat() {
		return userStat;
	}
	public void setUserStat(String userStat) {
		this.userStat = userStat;
	}
	public String getUserMobile() {
		return userMobile;
	}
	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getLockFlag() {
		return lockFlag;
	}
	public void setLockFlag(String lockFlag) {
		this.lockFlag = lockFlag;
	}
	public String getLockTime() {
		return lockTime;
	}
	public void setLockTime(String lockTime) {
		this.lockTime = lockTime;
	}
	public String getInvalidDate() {
		return invalidDate;
	}
	public void setInvalidDate(String invalidDate) {
		if(null != invalidDate && invalidDate.length() > 10) {
			this.invalidDate = invalidDate.substring(0,10);
		}else{
			this.invalidDate = invalidDate;
		}
	}
	public String getValidDate() {
		return validDate;
	}
	public void setValidDate(String validDate) {
		if(null != validDate && validDate.length() > 10) {
			this.validDate = validDate.substring(0,10);
		}else{
			this.validDate = validDate;
		}
	}
	public int getDelFlag() {
		return delFlag;
	}
	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public Date getdCreateTime() {
		return dCreateTime;
	}
	public void setdCreateTime(Date dCreateTime) {
		this.dCreateTime = dCreateTime;
	}
	public Date getdInvalidDate() {
		return dInvalidDate;
	}
	public void setdInvalidDate(Date dInvalidDate) {
		this.dInvalidDate = dInvalidDate;
	}
	public Date getdValidDate() {
		return dValidDate;
	}
	public void setdValidDate(Date dValidDate) {
		this.dValidDate = dValidDate;
	}
	public List<String> getProductList() {
		return productList;
	}
	public void setProductList(List<String> productList) {
		this.productList = productList;
	}
	public List<String> getSysRoleList() {
		return sysRoleList;
	}
	public void setSysRoleList(List<String> sysRoleList) {
		this.sysRoleList = sysRoleList;
	}
	public String getOldUserPwd() {
		return oldUserPwd;
	}
	public void setOldUserPwd(String oldUserPwd) {
		this.oldUserPwd = oldUserPwd;
	}
	
}
