package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.CleanRules;

public interface CleanRulesMapper {
	
	public List<CleanRules> queryCleanRulesPage(Map<String, Object> condition);
	
	public int insertCleanRules(CleanRules cleanRules);
	
	public int updateCleanRules(CleanRules cleanRules);
	
	public List<CleanRules> getCleanRulesByCondition(Map<String, Object> condition);
	
	public int deleteCleanRules(Map<String,Object> dMap);
	
	public List<CleanRules> queryCleanRulesJobNames(Map<String, Object> condition);

	public void pWwsJobClear(Map<String, Object> condition);

	public void pWwsCronClear(Map<String, Object> condition);

	public void pWwsJobExeinfoClear(Map<String, Object> condition);
}
