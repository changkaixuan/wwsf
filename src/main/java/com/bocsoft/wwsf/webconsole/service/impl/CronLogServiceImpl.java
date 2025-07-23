package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.CronLogMapper;
import com.bocsoft.wwsf.webconsole.model.CronLog;
import com.bocsoft.wwsf.webconsole.service.CronLogService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("cronLogService")
public class CronLogServiceImpl implements CronLogService {

	@Autowired
	private CronLogMapper cronLogMapper;
	
	@Override
	public List<CronLog> getCronLog(Map<String, Object> condition) throws Exception {
		return cronLogMapper.getCronLog(condition);
	}
	
	@Override
	public PageInfo<CronLog> getCronLog(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				try {
					cronLogMapper.getCronLog(condition);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public List<CronLog> getCronLogInformation(String logId) throws Exception{
		return cronLogMapper.getCronLogInformation(logId);
	}

}
