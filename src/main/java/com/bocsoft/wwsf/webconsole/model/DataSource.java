package com.bocsoft.wwsf.webconsole.model;

import java.util.List;

public class DataSource {
	
	private String product;
	private String agent;
	private String name;
	private String driverName;
	private String driverClass;
	private String databaseName;
	private String databaseType;
	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPassword;
	private int autoStart;
	private String pool;
	private String xa;
	private String poolProperties;
	
	private String onLineDesc;//is online
	private List<String> runningon;
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public DataSource(){ }
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getDriverClass() {
		return driverClass;
	}
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getDatabaseType() {
		return databaseType;
	}
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getJdbcUser() {
		return jdbcUser;
	}
	public void setJdbcUser(String jdbcUser) {
		this.jdbcUser = jdbcUser;
	}
	public String getJdbcPassword() {
		return jdbcPassword;
	}
	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}
	public int getAutoStart() {
		return autoStart;
	}
	public void setAutoStart(int autoStart) {
		this.autoStart = autoStart;
	}
	public String getPool() {
		return pool;
	}
	public void setPool(String pool) {
		this.pool = pool;
	}
	public String getXa() {
		return xa;
	}
	public void setXa(String xa) {
		this.xa = xa;
	}
	public String getPoolProperties() {
		return poolProperties;
	}
	public void setPoolProperties(String poolProperties) {
		this.poolProperties = poolProperties;
	}
	public String getOnLineDesc() {
		return onLineDesc;
	}
	public void setOnLineDesc(String onLineDesc) {
		this.onLineDesc = onLineDesc;
	}
	public List<String> getRunningon() {
		return runningon;
	}
	public void setRunningon(List<String> runningon) {
		this.runningon = runningon;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
