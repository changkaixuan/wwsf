package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.PluginMapper;
import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.service.PluginService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("pluginService")
public class PluginServiceImpl implements PluginService {
	
	@Autowired
	private PluginMapper pluginMapper;

	@Override
	public PageInfo<Plugin> pluginList(Map<String, Object> condition, int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				pluginMapper.pluginListPage(condition);
			}
		});
	}
	
	public List<Plugin> getPluginList(Map<String, Object> condition){
		return pluginMapper.pluginList(condition);
	}

	@Override
	public Set<String> getPluginTags(String product, String tag) {
		Set<String> set = new HashSet<String>();
		
		return set;
	}
	
	//CRON、JOB实例任务页面用此插件数据
	public List<Plugin> getPluginParameter(Map<String, Object> condition){
		return pluginMapper.getPluginParameter(condition);
	}

	@Override
	public List<Plugin> queryPluList() {
		return pluginMapper.queryPluList();
	}

	public List<Plugin> getPluginDefinition() {
		return pluginMapper.getPluginParameters(null);
	}
	
}
