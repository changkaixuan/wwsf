package com.bocsoft.wwsf.webconsole.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Task {
	
	public String jobId;
	public String taskId;
	//public int nodeType; // 1. 顶点; 2. 中间节点; 0. 基点
	public String name;
	public String title;
	//public int enabled = 1; // 0.disabled ;1.enabled (default value)
	public int allowedRerun = 0; // 0.不可重跑  (default value);1.可重跑
	public String agentScope;
	
	public String plugin;
	public String programName; // 插件时，plugin = programName
	
	// dimension job specific
	//public boolean useDefaultJobDimension = false;
	//public String dimesionSchemeNo; //默认使用的模板
	public String useJobDimension;
	public TreeMap<String, Set<String>> dimensions;  //key=维度名称     Set<String>指：实体名称（实体名称值格式：实体名称）或标签（标签值格式：TAG:v_5）列表

	//错误重复执行容错
	public int errorDelay;
	public int errorIgnore;
	public int maxNumOfExeErrors;
	//成功重复执行
	//public int cycle;
	//public int cycleInterval;
	public String period;
	public String workingDay;
	public String createTime;
	public String creator;
	public int pointX;
	public int pointY;
	
	public List<String> conditions = null; // 自定义条件，只支持表达式
	public List<String> leansInBatch = null; // 依赖job间task
	public List<String> leans = null; // task依赖关系，当依赖关系为空时，为起点
	public List<String> beleans = null; // task被依赖关系，当被依赖关系为空时，为顶点
	public List<String> beleansInBatch = null; // task被依赖关系，当被依赖关系为空时，为顶点
	public Map<String, String> parameters;
	public Map<String, Integer> paramterIsMust; // 必须的参数Map<String,Integer>中Integer存1否则存0
	
	public Date dCreateTime; //保存task周转用一次
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getAllowedRerun() {
		return allowedRerun;
	}
	public void setAllowedRerun(int allowedRerun) {
		this.allowedRerun = allowedRerun;
	}
	public String getAgentScope() {
		return agentScope;
	}
	public void setAgentScope(String agentScope) {
		this.agentScope = agentScope;
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
	public String getUseJobDimension() {
		return useJobDimension;
	}
	public void setUseJobDimension(String useJobDimension) {
		this.useJobDimension = useJobDimension;
	}
	public TreeMap<String, Set<String>> getDimensions() {
		return dimensions;
	}
	public void setDimensions(TreeMap<String, Set<String>> dimensions) {
		this.dimensions = dimensions;
	}
	public int getErrorDelay() {
		return errorDelay;
	}
	public void setErrorDelay(int errorDelay) {
		this.errorDelay = errorDelay;
	}
	public int getErrorIgnore() {
		return errorIgnore;
	}
	public void setErrorIgnore(int errorIgnore) {
		this.errorIgnore = errorIgnore;
	}
	public int getMaxNumOfExeErrors() {
		return maxNumOfExeErrors;
	}
	public void setMaxNumOfExeErrors(int maxNumOfExeErrors) {
		this.maxNumOfExeErrors = maxNumOfExeErrors;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getWorkingDay() {
		return workingDay;
	}
	public void setWorkingDay(String workingDay) {
		this.workingDay = workingDay;
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
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public List<String> getConditions() {
		return conditions;
	}
	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
	}
	public List<String> getLeansInBatch() {
		return leansInBatch;
	}
	public void setLeansInBatch(List<String> leansInBatch) {
		this.leansInBatch = leansInBatch;
	}
	public List<String> getLeans() {
		return leans;
	}
	public void setLeans(List<String> leans) {
		this.leans = leans;
	}
	public List<String> getBeleans() {
		return beleans;
	}
	public void setBeleans(List<String> beleans) {
		this.beleans = beleans;
	}
	public List<String> getBeleansInBatch() {
		return beleansInBatch;
	}
	public void setBeleansInBatch(List<String> beleansInBatch) {
		this.beleansInBatch = beleansInBatch;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	public Date getdCreateTime() {
		return dCreateTime;
	}
	public void setdCreateTime(Date dCreateTime) {
		this.dCreateTime = dCreateTime;
	}
	public Map<String, Integer> getParamterIsMust() {
		return paramterIsMust;
	}
	public void setParamterIsMust(Map<String, Integer> paramterIsMust) {
		this.paramterIsMust = paramterIsMust;
	}
	public int getPointX() {
		return pointX;
	}
	public void setPointX(int pointX) {
		this.pointX = pointX;
	}
	public int getPointY() {
		return pointY;
	}
	public void setPointY(int pointY) {
		this.pointY = pointY;
	}
	
}
