package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.github.pagehelper.PageInfo;

public interface PluginService {
	
	public PageInfo<Plugin> pluginList(Map<String, Object> condition,int pageNo, int pageSize);
	
	public List<Plugin> getPluginList(Map<String, Object> condition);
	
	public Set<String> getPluginTags(String product, String tag);
	
	//CRON、JOB实例任务页面用此插件数据
	public List<Plugin> getPluginParameter(Map<String, Object> condition);
	
	public List<Plugin> getPluginDefinition();

	public List<Plugin> queryPluList();
	
}
