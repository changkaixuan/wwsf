package com.bocsoft.wwsf.webconsole.model;

public class ReleaseInfo {

	private String appName;
	private String time;
	private String version;
	private String batchZipUrl;
	private String batchZipSize;
	private String consoleZipUrl;
	private String consoleZipSize;
	private String docUrl;
	private String docSize;
	private String textUrl;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getBatchZipUrl() {
		return batchZipUrl;
	}

	public void setBatchZipUrl(String batchZipUrl) {
		this.batchZipUrl = batchZipUrl;
	}

	public String getConsoleZipUrl() {
		return consoleZipUrl;
	}

	public void setConsoleZipUrl(String consoleZipUrl) {
		this.consoleZipUrl = consoleZipUrl;
	}

	public String getDocUrl() {
		return docUrl;
	}

	public void setDocUrl(String docUrl) {
		this.docUrl = docUrl;
	}

	public String getTextUrl() {
		return textUrl;
	}

	public void setTextUrl(String textUrl) {
		this.textUrl = textUrl;
	}

	public String getBatchZipSize() {
		return batchZipSize;
	}

	public void setBatchZipSize(String batchZipSize) {
		this.batchZipSize = batchZipSize;
	}

	public String getConsoleZipSize() {
		return consoleZipSize;
	}

	public void setConsoleZipSize(String consoleZipSize) {
		this.consoleZipSize = consoleZipSize;
	}

	public String getDocSize() {
		return docSize;
	}

	public void setDocSize(String docSize) {
		this.docSize = docSize;
	}

}
