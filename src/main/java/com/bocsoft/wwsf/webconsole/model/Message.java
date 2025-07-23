package com.bocsoft.wwsf.webconsole.model;

public class Message {
	
	private String id;
	private String product;
	private String createTime;
	private String mLevel;
	private String mRead;
	private String mInfo;
	
	private String mTimeSec; //消息时间（单位秒）
	public final static int Message_Fetch_RowNum = 3; //取前3条消息
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getmLevel() {
		return mLevel;
	}
	public void setmLevel(String mLevel) {
		this.mLevel = mLevel;
	}
	public String getmRead() {
		return mRead;
	}
	public void setmRead(String mRead) {
		this.mRead = mRead;
	}
	public String getmInfo() {
		return mInfo;
	}
	public void setmInfo(String mInfo) {
		this.mInfo = mInfo;
	}
	public String getmTimeSec() {
		return mTimeSec;
	}
	public void setmTimeSec(String mTimeSec) {
		this.mTimeSec = mTimeSec;
	}
	
}
