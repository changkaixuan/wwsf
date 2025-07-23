package com.bocsoft.wwsf.webconsole.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JobInstance extends Job {
	private long jobInsId;
	private int state;
	private String batch;
	private Map<String, Object> parameters;
	private String accutDate;
	private int pause;
	private String initTime;
	private String beginTime;
	private String endTime;
	private int countFailTask;
	
	// 这两个字段用于获取 数据库json格式的字符串 然后再转换成Java的map字段
	private String parameterJson;
	private String propertieJson;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public String getPropertieJson() {
		return propertieJson;
	}
	public void setPropertieJson(String propertieJson) {
		this.propertieJson = propertieJson;
		if(propertieJson != null && !"".equals(propertieJson)) {
			try {
				JSONArray arr = JSONArray.parseArray(propertieJson);
				if (arr != null && arr.size() > 0) {
					Iterator it = arr.iterator();
					Map<String, String> properties = new HashMap<>();
					Map<String, Integer> jobParamterIsMust = new HashMap<>();
					while (it.hasNext()) {
						JSONObject jo = (JSONObject)it.next();
						properties.put(jo.getString("name"),jo.getString("value"));
						jobParamterIsMust.put(jo.getString("name"),jo.getInteger("required"));
					}
					setProperties(properties);
					setJobParamterIsMust(jobParamterIsMust);
				}
			} catch (Exception e) {
				logger.error("{}解析propertieJson异常：",getJobInsId(),e);
			}
			//setProperties(JSONObject.parseObject(propertieJson, java.util.Map.class));
		}
	}
	public String getParameterJson() {
		return parameterJson;
	}
	public void setParameterJson(String parameterJson) {
		this.parameterJson = parameterJson;
		if(parameterJson != null && !"".equals(parameterJson))
			setParameters(JSONObject.parseObject(parameterJson, java.util.Map.class));
	}
	public void setJobInsId(long jobInsId) {
		this.jobInsId = jobInsId;
	}
	public long getJobInsId() {
		return jobInsId;
	}
	public void setJobInsId(int jobInsId) {
		this.jobInsId = jobInsId;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	public String getAccutDate() {
		return accutDate;
	}
	public void setAccutDate(String accutDate) {
		this.accutDate = accutDate;
	}
	public int getPause() {
		return pause;
	}
	public void setPause(int pause) {
		this.pause = pause;
	}
	public String getInitTime() {
		return initTime;
	}
	public void setInitTime(String initTime) {
		this.initTime = initTime;
	}
	public String getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public int getCountFailTask() {
		return countFailTask;
	}
	public void setCountFailTask(int countFailTask) {
		this.countFailTask = countFailTask;
	}
}
