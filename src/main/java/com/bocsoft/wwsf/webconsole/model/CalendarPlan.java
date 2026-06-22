package com.bocsoft.wwsf.webconsole.model;

import java.util.List;

public class CalendarPlan {
	private String startDate;
	private String endDate;
	private String type;//0为区域，1为单选
	private List<String> singleDates;
	private String creator;
	private String desc;
	private String createTime;
	private String planName;
	private String product;
	//维度信息
	private String dimession;

	public String getDimession() {
		return dimession;
	}

	public void setDimession(String dimession) {
		this.dimession = dimession;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public List<String> getSingleDates() {
		return singleDates;
	}


	public void setSingleDates(List<String> singleDates) {
		this.singleDates = singleDates;
	}


	public String getCreator() {
		return creator;
	}


	public void setCreator(String creator) {
		this.creator = creator;
	}


	public String getDesc() {
		return desc;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	public String getCreateTime() {
		return createTime;
	}


	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}


	public String getPlanName() {
		return planName;
	}


	public void setPlanName(String planName) {
		this.planName = planName;
	}


	public String getProduct() {
		return product;
	}


	public void setProduct(String product) {
		this.product = product;
	}
	
	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getEndDate() {
		return endDate;
	}


	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


	
}
