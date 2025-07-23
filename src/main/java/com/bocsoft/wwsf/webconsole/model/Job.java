package com.bocsoft.wwsf.webconsole.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Job {

	private String jobId;
	private String product;
	private String name;
	private int mode;
	private String title;
	private String version;
	private String createTime;
	private Date dCreateTime;
	private String creator;
	private String scheduleRid;
	
	public Map<String,TreeMap<String, Set<String>>> defaultDimensions; //第一层key指：编号   第二层key指：维度名称     Set<String>指：实体名称（实体名称值格式：实体名称）或标签（标签值格式：TAG:v_5）列表
	public Map<String,List<String>> specialLeans = null; // task实例特殊依赖关系列表
	public Map<String, Integer> jobParamterIsMust = null; //job必须的参数Map<String,Integer>中Integer存1否则存0
	public Map<String, String> properties = null;
	public Map<String,Map<String,Map<String,Set<String>>>> mutexGroups = null; //暂时忽略互斥组
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getProduct() {
		return product;
	}
	public String getScheduleRid() {
		return scheduleRid;
	}
	public void setScheduleRid(String scheduleRid) {
		this.scheduleRid = scheduleRid;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		if(null != createTime && createTime.length() > 10) {
			this.createTime = createTime.substring(0,10);
		}else {
			this.createTime = createTime;
		}
	}
	public Date getdCreateTime() {
		return dCreateTime;
	}
	public void setdCreateTime(Date dCreateTime) {
		this.dCreateTime = dCreateTime;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public Map<String, TreeMap<String, Set<String>>> getDefaultDimensions() {
		return defaultDimensions;
	}
	public void setDefaultDimensions(Map<String, TreeMap<String, Set<String>>> defaultDimensions) {
		this.defaultDimensions = defaultDimensions;
	}
	public Map<String, List<String>> getSpecialLeans() {
		return specialLeans;
	}
	public void setSpecialLeans(Map<String, List<String>> specialLeans) {
		this.specialLeans = specialLeans;
	}
	public Map<String, Integer> getJobParamterIsMust() {
		return jobParamterIsMust;
	}
	public void setJobParamterIsMust(Map<String, Integer> jobParamterIsMust) {
		this.jobParamterIsMust = jobParamterIsMust;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public Map<String, Map<String, Map<String, Set<String>>>> getMutexGroups() {
		return mutexGroups;
	}
	public void setMutexGroups(Map<String, Map<String, Map<String, Set<String>>>> mutexGroups) {
		this.mutexGroups = mutexGroups;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
