package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bocsoft.wwsf.webconsole.mapper.CleanRulesMapper;
import com.bocsoft.wwsf.webconsole.model.CleanRules;
import com.bocsoft.wwsf.webconsole.service.CleanRulesService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("cleanRulesService")
public class CleanRulesServiceImpl implements CleanRulesService {
	
	@Autowired
	private CleanRulesMapper cleanRulesMapper;

	public PageInfo<CleanRules> queryCleanRulesPage(Map<String, Object> condition, Integer pageNo, Integer pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				cleanRulesMapper.queryCleanRulesPage(condition);
			}
		});
	}

	public List<CleanRules> queryCleanRulesList(Map<String, Object> condition) throws Exception{
		return cleanRulesMapper.queryCleanRulesPage(condition);
	}
	
	public CleanRules getCleanRules(String id) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("id", id);
		List<CleanRules> tList = cleanRulesMapper.queryCleanRulesPage(condition);
		if(null != tList && tList.size()>0) {
			return tList.get(0);
		}
		return null;
	}
	
	public int insertCleanRules(CleanRules cleanRules) throws Exception{
		return cleanRulesMapper.insertCleanRules(cleanRules);
	}
	
	public int updateCleanRules(CleanRules cleanRules) throws Exception{
		return cleanRulesMapper.updateCleanRules(cleanRules);
	}
	
	public int deleteCleanRules(String idArr[]) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("idArr", idArr);
		return cleanRulesMapper.deleteCleanRules(condition);
	}

	@Override
	public void setJobNames(List<CleanRules> list) throws Exception {
		if (list==null || list.size()<1) {
			return;
		}
		List<String> idList = list.stream().map(cr -> cr.getId()).collect(Collectors.toList());
		String[] idArr = new String[idList.size()];
		idList.toArray(idArr);
		Map<String, Object> map = new HashMap<>();
		map.put("idArr", idArr);
		List<CleanRules> tList = cleanRulesMapper.queryCleanRulesJobNames(map);
		for(CleanRules fCleanRules : list) {
			fCleanRules.setJobNames(tList.get(tList.indexOf(fCleanRules)).getJobNames());
		}
	}

	public void pWwsJobClear(Map<String, Object> condition) throws Exception {
		cleanRulesMapper.pWwsJobClear(condition);
	}

	public void pWwsCronClear(Map<String, Object> condition) throws Exception {
		cleanRulesMapper.pWwsCronClear(condition);
	}

	public void pWwsJobExeinfoClear(Map<String, Object> condition) throws Exception {
		cleanRulesMapper.pWwsJobExeinfoClear(condition);
	}

}
