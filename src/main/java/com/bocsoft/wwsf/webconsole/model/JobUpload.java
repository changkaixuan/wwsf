package com.bocsoft.wwsf.webconsole.model;

import java.util.Date;

public class JobUpload {

	private String uId;
	private String product;
	private String uploadFile;
	private String backFile;
	private String msg;
	private String creator;
	private String createTime;
	private Date dCreateTime;
	private String status;
	private String triggerTime;
	private Date dTriggerTime;
	
	public static final String JOBUPLOAD_STATUS_INITIALIZE = "0"; //初始化
	public static final String JOBUPLOAD_STATUS_PROCESSING = "1"; //正在处理
	public static final String JOBUPLOAD_STATUS_FAIL = "2"; //失败
	public static final String JOBUPLOAD_STATUS_SUCCESS = "3"; //成功
	
	
	public JobUpload() {
		
	}
	
	public JobUpload(String uId, String msg, String status) {
		this.uId = uId;
		this.msg = msg;
		this.status = status;
	}
	
	public JobUpload(String uId, String product, String uploadFile, String backFile, String msg, String creator,
			Date dCreateTime, String status) {
		this.uId = uId;
		this.product = product;
		this.uploadFile = uploadFile;
		this.backFile = backFile;
		this.msg = msg;
		this.creator = creator;
		this.dCreateTime = dCreateTime;
		this.status = status;
	}
	
	public String getuId() {
		return uId;
	}
	public void setuId(String uId) {
		this.uId = uId;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getUploadFile() {
		return uploadFile;
	}
	public void setUploadFile(String uploadFile) {
		this.uploadFile = uploadFile;
	}
	public String getBackFile() {
		return backFile;
	}
	public void setBackFile(String backFile) {
		this.backFile = backFile;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		if (null != createTime && createTime.length() > 19) {
			this.createTime = createTime.substring(0, 19);
		} else {
			this.createTime = createTime;
		}
	}
	public Date getdCreateTime() {
		return dCreateTime;
	}
	public void setdCreateTime(Date dCreateTime) {
		this.dCreateTime = dCreateTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTriggerTime() {
		return triggerTime;
	}
	public void setTriggerTime(String triggerTime) {
		if (null != triggerTime && triggerTime.length() > 19) {
			this.triggerTime = triggerTime.substring(0, 19);
		} else {
			this.triggerTime = triggerTime;
		}
	}
	public Date getdTriggerTime() {
		return dTriggerTime;
	}
	public void setdTriggerTime(Date dTriggerTime) {
		this.dTriggerTime = dTriggerTime;
	}
	
}
