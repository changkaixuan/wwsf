package com.bocsoft.wwsf.webconsole.model;

import java.util.Map;
import java.util.Set;

public class Cron implements Comparable<Cron>{
	public static final String STARTED = "STARTED";
	public static final String STARTED_SHOULDNOT = "STARTED_SHOULDNOT";
	public static final String ERROR = "ERROR";
	public static final String UNKNOWN = "UNKNOWN";
	public static final String NOTSTARTED = "NOTSTARTED";
	
	private String jobId;
	private String model;
	private String product;
	private String agent;
	private String jobName;
	private String cronExpression;
	private String plugin; 
	private String programName;
	private int autoStart;
	private String createTime;
	private String creator;
	
	private Map<String,String> parameter; // 插件参数
	public Map<String, Integer> paramterIsMust; // 必须的参数Map<String,Integer>中Integer存1否则存0
	private Map<String,String> shared; // 临时数据共享
	
	private String status; // 状态
	private Set<String> runningon;
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public Map<String, Integer> getParamterIsMust() {
		return paramterIsMust;
	}
	public void setParamterIsMust(Map<String, Integer> paramterIsMust) {
		this.paramterIsMust = paramterIsMust;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Set<String> getRunningon() {
		return runningon;
	}
	public void setRunningon(Set<String> runningon) {
		this.runningon = runningon;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public int getAutoStart() {
		return autoStart;
	}
	public void setAutoStart(int autoStart) {
		this.autoStart = autoStart;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	public String getPlugin() {
		return plugin;
	}
	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Map<String, String> getParameter() {
		return parameter;
	}
	public void setParameter(Map<String, String> parameter) {
		this.parameter = parameter;
	}
	public Map<String, String> getShared() {
		return shared;
	}
	public void setShared(Map<String, String> shared) {
		this.shared = shared;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	@Override
	public int compareTo(Cron o) {
		return this.getJobId().compareTo(o.getJobId());
	}
	
}
