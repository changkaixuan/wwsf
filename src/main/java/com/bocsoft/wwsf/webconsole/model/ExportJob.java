package com.bocsoft.wwsf.webconsole.model;

import java.util.ArrayList;
import java.util.List;

public class ExportJob {

	private List<String> exportErrorInfoList = new ArrayList<String>();
	private Product product;
	private List<ProductCertification> productCertificationList = new ArrayList<>();
	private List<Server> serverList = new ArrayList<>();
	private List<DataSource> dataSourceList = new ArrayList<>();
	private List<Dimension> dimensionList = new ArrayList<>();
	private List<DimensionEntity> dimensionEntityList = new ArrayList<>();
	private List<Calendar> calendarList = new ArrayList<>();
	private List<Cron> cronList = new ArrayList<>();
	private List<CronShared> cronSharedList = new ArrayList<>();
	private List<Plugin> pluginList = new ArrayList<>();
	private List<Job> jobList = new ArrayList<>();
	private List<JobDimension> jobDimensionList = new ArrayList<>();
	private List<TaskSpecialLean> taskSpecialLeanList = new ArrayList<>();
	private List<Parameter> parameterList = new ArrayList<>();
	private List<Task> taskList = new ArrayList<>();
	private List<TaskLean> taskLeanList = new ArrayList<>();
	
	public static final String EXPORT_FILE_CATALOG_ALL_VALUE = "PRODUCT,PRODUCT_CERTIFICATION,SERVER,DS,DIMENSION,PLUGIN,CALENDAR,CRON";
	
	public static final int BATCH_COMMIT_MAX_RECORD_NUM = 500; 
	public static final int EXPORT_EXCEL_SHEET_NAME_START_NUM = 1; 
	
	public List<Cron> getCronList() {
		return cronList;
	}
	public void setCronList(List<Cron> cronList) {
		this.cronList = cronList;
	}
	public List<String> getExportErrorInfoList() {
		return exportErrorInfoList;
	}
	public void setExportErrorInfoList(List<String> exportErrorInfoList) {
		this.exportErrorInfoList = exportErrorInfoList;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public List<ProductCertification> getProductCertificationList() {
		return productCertificationList;
	}
	public void setProductCertificationList(List<ProductCertification> productCertificationList) {
		this.productCertificationList = productCertificationList;
	}
	public List<Server> getServerList() {
		return serverList;
	}
	public void setServerList(List<Server> serverList) {
		this.serverList = serverList;
	}
	public List<Dimension> getDimensionList() {
		return dimensionList;
	}
	public void setDimensionList(List<Dimension> dimensionList) {
		this.dimensionList = dimensionList;
	}
	public List<DimensionEntity> getDimensionEntityList() {
		return dimensionEntityList;
	}
	public void setDimensionEntityList(List<DimensionEntity> dimensionEntityList) {
		this.dimensionEntityList = dimensionEntityList;
	}
	public List<Plugin> getPluginList() {
		return pluginList;
	}
	public void setPluginList(List<Plugin> pluginList) {
		this.pluginList = pluginList;
	}
	public List<Job> getJobList() {
		return jobList;
	}
	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}
	public List<JobDimension> getJobDimensionList() {
		return jobDimensionList;
	}
	public void setJobDimensionList(List<JobDimension> jobDimensionList) {
		this.jobDimensionList = jobDimensionList;
	}
	public List<Parameter> getParameterList() {
		return parameterList;
	}
	public void setParameterList(List<Parameter> parameterList) {
		this.parameterList = parameterList;
	}
	public List<Calendar> getCalendarList() {
		return calendarList;
	}
	public void setCalendarList(List<Calendar> calendarList) {
		this.calendarList = calendarList;
	}
	public List<TaskSpecialLean> getTaskSpecialLeanList() {
		return taskSpecialLeanList;
	}
	public void setTaskSpecialLeanList(List<TaskSpecialLean> taskSpecialLeanList) {
		this.taskSpecialLeanList = taskSpecialLeanList;
	}
	public List<TaskLean> getTaskLeanList() {
		return taskLeanList;
	}
	public void setTaskLeanList(List<TaskLean> taskLeanList) {
		this.taskLeanList = taskLeanList;
	}
	public List<Task> getTaskList() {
		return taskList;
	}
	public void setTaskList(List<Task> taskList) {
		this.taskList = taskList;
	}
	public List<DataSource> getDataSourceList() {
		return dataSourceList;
	}
	public void setDataSourceList(List<DataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}
	public List<CronShared> getCronSharedList() {
		return cronSharedList;
	}
	public void setCronSharedList(List<CronShared> cronSharedList) {
		this.cronSharedList = cronSharedList;
	}
	
}
