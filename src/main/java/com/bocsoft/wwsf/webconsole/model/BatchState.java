package com.bocsoft.wwsf.webconsole.model;

public class BatchState {
	private String state;
	private Integer order;
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	@Override
	public String toString() {
		return "BatchState [state=" + state + ", order=" + order + "]";
	}
}
