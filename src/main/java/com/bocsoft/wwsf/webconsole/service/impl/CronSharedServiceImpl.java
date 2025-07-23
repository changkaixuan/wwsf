package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.CronSharedMapper;
import com.bocsoft.wwsf.webconsole.model.CronShared;
import com.bocsoft.wwsf.webconsole.service.CronSharedService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("cronSharedService")
public class CronSharedServiceImpl implements CronSharedService {
	
	@Autowired
	private CronSharedMapper cronSharedMapper;
	
	public PageInfo<CronShared> queryCronShared(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				cronSharedMapper.queryCronShared(condition);
			}
		});
	}

	@Override
	public CronShared getCronShared(String jobId,String product,String agent) throws Exception{
		CronShared cronShared = null;
		Map<String, Object> condition = new HashMap<>();
		condition.put("product", product);
		condition.put("agent", agent);
		condition.put("jobId", jobId);
		List<CronShared> cronSharedList = cronSharedMapper.queryCronShared(condition);
		if(null != cronSharedList && cronSharedList.size()>0) {
			cronShared = cronSharedList.get(0);
		}
		return cronShared;
	}

	@Transactional
	public void saveCronShared(String operationType,CronShared cronShared) throws Exception{
		CronShared dCronShared = this.getCronShared(cronShared.getJobId(),cronShared.getProduct(),cronShared.getAgent());
		if(null != operationType && operationType.equals("update")) {
			if(null == dCronShared) {
				throw new Exception("更新失败，临时共享数据不存在");
			}
			cronSharedMapper.updateCronShared(cronShared);
		}else {
			if(null != dCronShared) {
				throw new Exception("新增失败，临时共享数据已存在");
			}
			cronSharedMapper.insertCronShared(cronShared);
		}
	}
	
	@Transactional
	public void deleteCronShared(String jobId,String productArr[],String agentArr[]) throws Exception{
		for(int i=0;i<productArr.length;i++) {
			Map<String, Object> dMap = new HashMap<String, Object>();
			dMap.put("jobId", jobId);
			dMap.put("product", productArr[i]);
			dMap.put("agent", agentArr[i]);
			cronSharedMapper.deleteCronShared(dMap);
		}
	}
	
}
