package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.ParameterMapper;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.service.ParameterService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("parameterService")
public class ParameterServiceImpl implements ParameterService{

	@Autowired
	private ParameterMapper parameterMapper;
	
	public PageInfo<Parameter> queryParameterPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				parameterMapper.queryParameterPage(condition);
			}
		});
	}
	
	public List<Parameter> queryPluParameterList(String pluginName) throws Exception{
		return parameterMapper.queryPluParameterDefinition(pluginName);
	}
	
	public List<Parameter> getParameterList(Map<String, Object> condition) throws Exception{
		return parameterMapper.queryParameterPage(condition);
	}
	
	public List<Parameter> getParameterList(String jobId,String sourceId) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("sourceId", sourceId);
		return parameterMapper.queryParameterPage(condition);
	}
	
	public int insertParameter(Parameter parameter) throws Exception{
		return parameterMapper.insertParameter(parameter);
	}
	
	public int deleteParameter(Map<String, Object> dMap) throws Exception{
		return parameterMapper.deleteParameter(dMap);
	}
	
	public List<String> getProgramNames(Map<String,Object> condition) throws Exception{
		return parameterMapper.getProgramNames(condition);
	}
	
}
