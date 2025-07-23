package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.github.pagehelper.PageInfo;

public interface ParameterService {

	PageInfo<Parameter> queryParameterPage(Map<String, Object> condition, int pageNo, int pageSize) throws Exception;

	List<Parameter> queryPluParameterList(String pluginName) throws Exception;

	public List<Parameter> getParameterList(Map<String, Object> condition) throws Exception;

	public List<Parameter> getParameterList(String jobId, String sourceId) throws Exception;

	public int insertParameter(Parameter parameter) throws Exception;

	public int deleteParameter(Map<String, Object> dMap) throws Exception;

	public List<String> getProgramNames(Map<String, Object> condition) throws Exception;

}
