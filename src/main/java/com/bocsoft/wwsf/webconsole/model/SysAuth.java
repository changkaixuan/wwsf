package com.bocsoft.wwsf.webconsole.model;

public class SysAuth {
	private String id;
	private String name;
	private String pid;
	private String dataModule;
	private String sort;
	private String iconClass;
	private String dataFilterTags; //多个用空格隔开
	private String requiresPermissions; //shiro菜单权限控制(Controller层url过滤)
	
	public final static String SysAuth_Root = "nav.wws.root";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getDataModule() {
		return dataModule;
	}
	public void setDataModule(String dataModule) {
		this.dataModule = dataModule;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getIconClass() {
		return iconClass;
	}
	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}
	public String getDataFilterTags() {
		return dataFilterTags;
	}
	public void setDataFilterTags(String dataFilterTags) {
		this.dataFilterTags = dataFilterTags;
	}
	public String getRequiresPermissions() {
		return requiresPermissions;
	}
	public void setRequiresPermissions(String requiresPermissions) {
		this.requiresPermissions = requiresPermissions;
	}
	
}
