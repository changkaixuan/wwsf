package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.model.PluginParameter;

public interface PluginMapper {
	
	public List<Plugin> pluginListPage(Map<String, Object> conditions);
	
	public List<Plugin> pluginList(Map<String, Object> conditions);
	
	public List<Plugin> getPluginParameter(Map<String, Object> conditions);

	public List<Plugin> queryPluList();

	public List<Plugin> getPluginParameters(Map<String, Object> queryMap);

	public void insertPlugin(Plugin plugin);

	public List<PluginParameter> queryPluginParameter(Map<String, Object> hashMap);

	public void insertPluginParameter(PluginParameter pluginParameter);

	public void updatePlugin(Plugin plugin);

	public void updatePluginParameter(PluginParameter pluginParameter);
	
}
