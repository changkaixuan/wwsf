package com.bocsoft.wwsf.webconsole;

import java.util.HashMap;
import java.util.Map;

public class RestResponse {

	private boolean success;
	private Boolean result;
	private String info;
	private Map<String, Object> data;
	private String detailInfo;
	
	public RestResponse() {
		data = new HashMap<String,Object>();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void put(String key, Object value) {
		data.put(key, value);
	}

	public void remove(String key) {
		data.remove(key);
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public String getDetailInfo() {
		return detailInfo;
	}

	public void setDetailInfo(String detailInfo) {
		this.detailInfo = detailInfo;
	}
}
