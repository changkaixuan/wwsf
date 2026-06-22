package com.bocsoft.wwsf.webconsole.model;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class TaskInstance extends Task {
	private long jobInsId;
	private String taskInsId;
	private int state;
	private String batch;
	private int currentErrorExeCount;
	private int result;
	private String information;
	private String nextExecuteTime;
	private String agent;
	private String beginTime;
	private String endTime;
	private Map<String, Integer> leansRuntime;
	
	// 存储数据库 json 格式的字符串， 转成Java map类型
	private String parameterJson;
//	private String leansRuntimeJson;
	
	// 存储 数据库 数组 格式的字符串， 转成Java list 类型
//	private String beleansArray;
//	private String leansArray;
	
	public final static String STATE_INIT = "初始化"; // 任务初始化
	public final static String STATE_READY = "准备执行"; // 正在执行
	public final static String STATE_RUNNING = "正在执行"; // 正在执行
	public final static String STATE_ERROR_DELAY = "出错延迟中"; // 任务出错延迟中
	public final static String STATE_FAIL = "失败"; // 失败
	public final static String STATE_FEEDBACK = "执行结果返回"; // 执行结果返回
	public final static String STATE_MANUAL = "需人工干预"; // 需人工干预处理
	public final static String STATE_PERIOD_DELAY = "执行延迟中"; // 任务执行窗口延迟中
	public final static String STATE_SUCCESS = "成功"; // 成功

//	public String getLeansRuntimeJson() {
//		return leansRuntimeJson;
//	}
//	public void setLeansRuntimeJson(String leansRuntimeJson) {
//		if (null != leansRuntimeJson && !"".equals(leansRuntimeJson)) 
//			setLeansRuntime(JSONObject.parseObject(leansRuntimeJson, java.util.Map.class));
//		else
//			leansRuntimeJson = "{}";
//		this.leansRuntimeJson = leansRuntimeJson;
//	}

	public Map<String, Integer> getLeansRuntime() {
		return leansRuntime;
	}
	public void setLeansRuntime(Map<String, Integer> leansRuntime) {
		this.leansRuntime = leansRuntime;
	}
	
//	public String getLeansArray() {
//		return leansArray;
//	}
//	public void setLeansArray(String leansArray) {
//		this.leansArray = leansArray;
//		if(null != leansArray && !"".equals(leansArray)) {
//			leansArray = leansArray.substring(1, leansArray.length()-1);
//			String[] arr = leansArray.split(",(?=\")");
//			List<String> list = Arrays.stream(arr).map(v -> v = v.replace("\\", "")).collect(Collectors.toList());
//			setLeans(list);
//		}
//	}
//	public String getBeleansArray() {
//		return beleansArray;
//	}
//	public void setBeleansArray(String beleansArray) {
//		this.beleansArray = beleansArray;
//		if(null != beleansArray && !"".equals(beleansArray)) {
//			beleansArray = beleansArray.substring(1, beleansArray.length()-1);
//			String[] arr = beleansArray.split(",(?=\")");
//			List<String> list = Arrays.stream(arr).map(v -> v = v.replace("\\", "")).collect(Collectors.toList());
//			setBeleans(list);
//		}
//	}
	public String getParameterJson() {
		return parameterJson;
	}
	public void setParameterJson(String parameterJson) {
		if (null != parameterJson && !"".equals(parameterJson)) 
			setParameters(JSONObject.parseObject(parameterJson, java.util.Map.class));
		else
			parameterJson = "{}";
		this.parameterJson = parameterJson;
	}
	public long getJobInsId() {
		return jobInsId;
	}
	public void setJobInsId(long jobInsId) {
		this.jobInsId = jobInsId;
	}
	public String getTaskInsId() {
		return taskInsId;
	}
	public void setTaskInsId(String taskInsId) {
		this.taskInsId = taskInsId;
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
	public int getCurrentErrorExeCount() {
		return currentErrorExeCount;
	}
	public void setCurrentErrorExeCount(int currentErrorExeCount) {
		this.currentErrorExeCount = currentErrorExeCount;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getInformation() {
		return information;
	}
	public void setInformation(String information) {
		this.information = information;
	}
	public String getNextExecuteTime() {
		return nextExecuteTime;
	}
	public void setNextExecuteTime(String nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
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
}
