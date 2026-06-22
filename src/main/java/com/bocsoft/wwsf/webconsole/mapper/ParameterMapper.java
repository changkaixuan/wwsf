package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.Parameter;

public interface ParameterMapper {

	public List<Parameter> queryParameterPage(Map<String,Object> condition);
	
	public List<Parameter> queryPluParameterDefinition(@Param("pluginName") String pluginName);
	
	public int insertParameter(Parameter parameter);
	
	public int deleteParameter(Map<String,Object> dMap);
	
	public int deleteParameterByProduct(@Param("product") String product);
	
	public List<String> getProgramNames(Map<String,Object> condition);

	public List<Map<String,Object>> getTaskIdAndPluParValue(Map<String,Object> condition);

	public void updateParameter(Parameter parameter);

	public List<Map<String, Object>> getCronIdAndPluParValue(Map<String, Object> queryMap);
	
	public int batchInsertParameter(List<Parameter> parameterList);
	
	public void batchUpdateParameter(List<Parameter> parameterList);
	
}
