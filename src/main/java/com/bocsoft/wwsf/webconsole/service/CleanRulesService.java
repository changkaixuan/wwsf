package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.CleanRules;
import com.github.pagehelper.PageInfo;

public interface CleanRulesService {
	
	public PageInfo<CleanRules> queryCleanRulesPage(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception;
	
	public List<CleanRules> queryCleanRulesList(Map<String, Object> condition) throws Exception;
	
	public CleanRules getCleanRules(String id) throws Exception;
	
	public int insertCleanRules(CleanRules cleanRules) throws Exception;
	
	public int updateCleanRules(CleanRules cleanRules) throws Exception;
	
	public int deleteCleanRules(String idArr[]) throws Exception;
	
	public void setJobNames(List<CleanRules> list) throws Exception;

	public void pWwsJobClear(Map<String, Object> condition) throws Exception;

	public void pWwsCronClear(Map<String, Object> condition) throws Exception;
	
	public void pWwsJobExeinfoClear(Map<String, Object> condition) throws Exception;

}
