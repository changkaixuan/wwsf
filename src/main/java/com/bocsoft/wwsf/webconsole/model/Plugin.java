package com.bocsoft.wwsf.webconsole.model;

import java.util.List;

public class Plugin {
	
	private String name;
	private String desc;
	private String feature;
	private String version;
	private List<Parameter> parameters;
	private List<PluginParameter> pluginParameters;
	
	private int importType; //job模板导入取值：ImportJob.java
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	public List<PluginParameter> getPluginParameters() {
		return pluginParameters;
	}
	public void setPluginParameters(List<PluginParameter> pluginParameters) {
		this.pluginParameters = pluginParameters;
	}
	public int getImportType() {
		return importType;
	}
	public void setImportType(int importType) {
		this.importType = importType;
	}
	
}
