package com.bocsoft.wwsf.webconsole.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BatchStatisticVo implements Comparable<BatchStatisticVo> {
	private String product;
	private String batch;
	private long beginTime;
	private String spentTime = "-";
	private long endTime;
	private String batchKey;
	private List<String> jobInsIds;
	private int jobInsTotal;
	private int state;
	private String stateDesc;
	
	private int taskInsTotal;
	private int taskInitTotal; //任务初始化
	private int taskReadyTotal; //等待执行
	private int taskRunningTotal; //正在执行
	private int taskErrorDelayTotal; //任务出错延迟中
	private int taskFeedbackTotal; //任务结果返回
	private int taskMaualTotal; //失败
	private int taskCycleDelayTotal; //需人工干预处理
	private int taskFailedTotal; //任务执行窗口延迟中
	private int taskSuccessTotal; //成功
	
	private BatchState batchState;
	
	public BatchStatisticVo() {
		jobInsTotal = 0;
		jobInsIds = new ArrayList<String>();
		taskInsTotal = 0;
		taskInitTotal = 0;
		taskReadyTotal = 0;
		taskRunningTotal = 0;
		taskErrorDelayTotal = 0;
		taskFeedbackTotal = 0;
		taskMaualTotal = 0;
		taskCycleDelayTotal = 0 ;
		taskFailedTotal = 0;
		taskSuccessTotal = 0;
		stateDesc = "";
	}

	public BatchState getBatchState() {
		return batchState;
	}

	public void setBatchState(BatchState batchState) {
		this.batchState = batchState;
	}

	public String getBatchKey() {
		return batchKey;
	}

	public void setBatchKey(String batchKey) {
		this.batchKey = batchKey;
	}

	public int getJobInsTotal() {
		return jobInsTotal;
	}

	public void setJobInsTotal(int jobInsTotal) {
		this.jobInsTotal = jobInsTotal;
	}

	public int getTaskInsTotal() {
		return taskInsTotal;
	}

	public void setTaskInsTotal(int taskInsTotal) {
		this.taskInsTotal = taskInsTotal;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public String getStateDesc() {
		return stateDesc;
	}

	public void setStateDesc(String stateDesc) {
		this.stateDesc = stateDesc;
	}

	@Override
	public int compareTo(BatchStatisticVo o) {
		if (this.getState() > o.getState()) {
			return 1;
		} else if (this.getState() < o.getState()) {
			return -1;
		} else {
			return -1 * (this.getBatch().compareTo(o.getBatch()));
		}
	}

	@Override
	public String toString() {
		return "BatchStatisticVo [batchInfo=" + batchKey + ", jobInsTotal=" + jobInsTotal + ", taskInsTotal="
				+ taskInsTotal + ", state=" + state + ", batchState="
				+ batchState + "]";
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getBatch() {
		return batch;
	}

	public void setBatch(String batch) {
		this.batch = batch;
	}

	public String getFinishedPercent() {
		if(taskInsTotal == 0 || taskSuccessTotal == 0) {
			return "0%";
		}
		double f0 = taskSuccessTotal*1.0 / taskInsTotal;
		BigDecimal f1 = new BigDecimal(f0*100)
				.setScale(1, BigDecimal.ROUND_FLOOR);
		return f1.floatValue() + "%";
	}

	public int getCode() {
		if(taskSuccessTotal==taskInsTotal)
			return 8;
		if(taskErrorDelayTotal>0)
			return 9;
		if((taskSuccessTotal<<1) > taskInsTotal)
			return 1;
		if((taskSuccessTotal<<1) <= taskInsTotal)
			return 2;
		return 0;
	}

	public List<String> getJobInsIds() {
		return jobInsIds;
	}

	public void setJobInsIds(List<String> jobInsIds) {
		this.jobInsIds = jobInsIds;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getSpentTime() {
		return spentTime;
	}

	public void setSpentTime(String spentTime) {
		this.spentTime = spentTime;
	}

	public int getTaskSuccessTotal() {
		return taskSuccessTotal;
	}

	public void setTaskSuccessTotal(int taskSuccessTotal) {
		this.taskSuccessTotal = taskSuccessTotal;
	}

	public int getTaskInitTotal() {
		return taskInitTotal;
	}

	public void setTaskInitTotal(int taskInitTotal) {
		this.taskInitTotal = taskInitTotal;
	}

	public int getTaskReadyTotal() {
		return taskReadyTotal;
	}

	public void setTaskReadyTotal(int taskReadyTotal) {
		this.taskReadyTotal = taskReadyTotal;
	}

	public int getTaskRunningTotal() {
		return taskRunningTotal;
	}

	public void setTaskRunningTotal(int taskRunningTotal) {
		this.taskRunningTotal = taskRunningTotal;
	}

	public int getTaskErrorDelayTotal() {
		return taskErrorDelayTotal;
	}

	public void setTaskErrorDelayTotal(int taskErrorDelayTotal) {
		this.taskErrorDelayTotal = taskErrorDelayTotal;
	}

	public int getTaskMaualTotal() {
		return taskMaualTotal;
	}

	public void setTaskMaualTotal(int taskMaualTotal) {
		this.taskMaualTotal = taskMaualTotal;
	}

	public int getTaskCycleDelayTotal() {
		return taskCycleDelayTotal;
	}

	public void setTaskCycleDelayTotal(int taskCycleDelayTotal) {
		this.taskCycleDelayTotal = taskCycleDelayTotal;
	}

	public int getTaskFailedTotal() {
		return taskFailedTotal;
	}

	public void setTaskFailedTotal(int taskFailedTotal) {
		this.taskFailedTotal = taskFailedTotal;
	}

	public int getTaskFeedbackTotal() {
		return taskFeedbackTotal;
	}

	public void setTaskFeedbackTotal(int taskFeedbackTotal) {
		this.taskFeedbackTotal = taskFeedbackTotal;
	}

}
