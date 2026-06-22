package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.mapper.CronMapper;
import com.bocsoft.wwsf.webconsole.model.Cron;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.service.CronService;
import com.bocsoft.wwsf.webconsole.service.ParameterService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("cronService")
public class CronServiceImpl implements CronService {
	@Autowired
	private CronMapper cronMapper;
	@Autowired
	private ParameterService parameterService;
	
	@Autowired
	private WwseService wwseService;
	
	@Override
	public PageInfo<Cron> kvspage(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				try {
					cronList(condition,true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Cron> cronList(Map<String, Object> condition,boolean fetchState) throws Exception {
		List<Cron> cronList = cronMapper.kvspage(condition);
		Map<String,List<String>> agentsMap = null;
		if (fetchState) {
			String product = (String)condition.get("nullOrProduct");
			product = StringUtil.hasText(product) ? product : null;
			
			String agent = (String)condition.get("nullOrAgent");
			agent = StringUtil.hasText(agent) ? agent : null;
			
			String cronOnlineJson = wwseService.cronOnLine(product, agent);
			if (StringUtil.hasText(cronOnlineJson)) {
				agentsMap = JSONObject.parseObject(cronOnlineJson, Map.class);
			} else {
				agentsMap = new HashMap<String,List<String>>();
			}
		}
		
		Map<String, String> parameters = null;
		Map<String, Integer> paramterIsMust = null;
		List<Parameter> list = null;
		for (Cron cron : cronList) {
			list = parameterService.getParameterList(cron.getJobId(), cron.getJobId());
			parameters = new HashMap<>();
			paramterIsMust = new HashMap<>();
			for (Parameter parameter : list) {
				parameters.put(parameter.getName(), parameter.getValue());
				paramterIsMust.put(parameter.getName(), parameter.getdRequired());
			}
			cron.setParameter(parameters);
			cron.setParamterIsMust(paramterIsMust);
		
			if (fetchState) {
				cron.setStatus(Cron.NOTSTARTED);
				if (agentsMap != null && agentsMap.size() > 0) {
					if (agentsMap.containsKey(cron.getJobId())) {
						cron.setStatus(Cron.STARTED);
						cron.setRunningon(new HashSet<>(agentsMap.get(cron.getJobId())));
					}
				}
			} else {
				cron.setStatus(Cron.UNKNOWN);
			}
		}
		return cronList;
	}

	@Transactional
	@Override
	public int cronAdd(Cron cron) throws Exception {
		// 向 ww_cron 插入基本数据
		cronMapper.cronAdd(cron);
		// 向 ww_parampeter 插入插件参数
		addCronParameter(cron);
		return 1;
	}
	
	/**
	 * 将cron参数插入参数表，插入前先删除
	 * @param cron
	 * @throws Exception
	 */
	@Transactional
	public void addCronParameter(Cron cron) throws Exception{
		String jobId = cron.getJobId();
		Map<String, Object> map = new HashMap<>();
		map.put("jobId", jobId);
		map.put("sourceId", jobId);
		parameterService.deleteParameter(map);
		
		Map<String, String> parameters = cron.getParameter();
		Map<String, Integer> parameterIsMust = cron.getParamterIsMust();
		String key = "";
		int isMust = 0;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			key = entry.getKey();
			isMust = parameterIsMust.get(key);
			
			Parameter parameter = new Parameter();
			parameter.setJobId(jobId);
			parameter.setSourceId(jobId);
			parameter.setName(key);
			parameter.setValue(entry.getValue());
			parameter.setdRequired(isMust);
			parameterService.insertParameter(parameter);
		}
	}
	
	@Transactional
	@Override
	public int deleteCron(String[] cronIds, boolean skipRunning) throws Exception {
		int flag = 1;// 判断是否全部删除 1：全部删除 2：部分运行的任务没有删除
		Map<String, Object> conditions = null;
		//String[] arr = null;
		for (String id : cronIds) {
			// 正在运行的job跳过不删除
			/*
			arr = getCronStatus(id);
			if (skipRunning && null != arr && "STARTED".equals(arr[0])) {
				flag = 2;
				continue;
			}
			*/
			// 删除ww_cron 表中的数据
			wwseService.deleteCronJob(id);
			
			// 根据jobid 删除 ww_parameter 表中的数据
			conditions = new HashMap<String, Object>();
			conditions.put("jobId", id);
			conditions.put("sourceId", id);
			parameterService.deleteParameter(conditions);
		}
		return flag;
	}

	@Override
	public void cronStart(String[] cronIds, Boolean clearShared) throws Exception {
		for (String id : cronIds) {
			wwseService.cronStart(id, clearShared);
		}
	}

	@Override
	public void cronStop(String[] jobId) throws Exception {
		for (String id : jobId) {
			wwseService.cronStop(id);
		}
	}

	@Transactional
	@Override
	public int update(Cron cron) throws Exception {
		// 更新ww_cron表
		cronMapper.updateCron(cron);
		// 更新ww_parameter表
		addCronParameter(cron);
		return 1;
	}

	@Override
	public List<Map<String,Object>> getCronCountByProduct(Map<String, Object> condition) throws Exception {
		return cronMapper.getCronCountByProduct(condition);
	}

	@Override
	public List<String> getCronIdByProduct(Map<String, Object> condition) throws Exception {
		List<Cron> cronList = cronMapper.kvspage(condition);
		return cronList.stream().map(cron -> cron.getJobId()).collect(Collectors.toList());
	}
}
