package com.bocsoft.wwsf.webconsole.model;

public class SelectTwo {
	
	private String type;
	private String serverId;
	private String id;
	private String text;
	
	public final static String Type_Id = "id";
	
	public final static String Type_Tag_Start = "#";
	
	public SelectTwo() {}
	
	public SelectTwo(String id,String text, String type) {
		this.id = id;
		this.text = text;
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

}
