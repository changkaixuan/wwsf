package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.JobDimensionMapper;
import com.bocsoft.wwsf.webconsole.model.Job;
import com.bocsoft.wwsf.webconsole.model.JobDimension;
import com.bocsoft.wwsf.webconsole.service.JobDimensionService;

@Service("jobDimensionService")
public class JobDimensionServiceImpl implements JobDimensionService{
	
	@Autowired
	private JobDimensionMapper jobDimensionMapper;
	
	public List<JobDimension> getJobDimensionList(Map<String, Object> condition) throws Exception{
		return jobDimensionMapper.queryJobDimensionPage(condition);
	}
	
	public int insertJobDimension(JobDimension jobDimension) throws Exception{
		return jobDimensionMapper.insertJobDimension(jobDimension);
	}
	
	public int deleteJobDimension(Map<String, Object> dMap) throws Exception{
		return jobDimensionMapper.deleteJobDimension(dMap);
	}
	
	public List<JobDimension> getJobDimension(Job job) throws Exception{
		Map<String,TreeMap<String, Set<String>>> defaultDimensions = job.getDefaultDimensions();
		if(null == defaultDimensions || defaultDimensions.size()<1) {
			return null;
		}
		List<JobDimension> jobDimensionList = new ArrayList<JobDimension>();
		for(Map.Entry<String, TreeMap<String, Set<String>>> fDefaultDimension : defaultDimensions.entrySet()) {//fDefaultDimension.getKey(); //编号
			TreeMap<String, Set<String>> treeMaps = fDefaultDimension.getValue();
			for(Map.Entry<String, Set<String>> fTreeMap : treeMaps.entrySet()) { //fTreeMap.getKey(); //维度名称
				Set<String> sets = fTreeMap.getValue();
				Iterator<String> iterators = sets.iterator();
				while(iterators.hasNext()) {
					String entityNameOrTags = iterators.next(); //实体名称值格式：实体名称；标签值格式：tags:v_5
					JobDimension jobDimension = new JobDimension();
					jobDimension.setJobId(job.getJobId());
					jobDimension.setModelNo(fDefaultDimension.getKey());
					jobDimension.setDmsnName(fTreeMap.getKey());
					jobDimension.setDmsnProduct(job.getProduct());
					jobDimension.setEntityName(entityNameOrTags);
					jobDimensionList.add(jobDimension);
				}
			}
		}
		return jobDimensionList;
	}
	
	public boolean batchInsertJobDimension(String type,Job job) throws Exception{
		if(type.equals("update")) {
			Map<String,Object> dMap = new HashMap<String,Object>();
			dMap.put("jobId", job.getJobId());
			jobDimensionMapper.deleteJobDimension(dMap);
		}
		List<JobDimension> jobDimensionList = this.getJobDimension(job);
		if(null != jobDimensionList && jobDimensionList.size()>0) {
			for(JobDimension fJobDimension : jobDimensionList) {
				jobDimensionMapper.insertJobDimension(fJobDimension);
			}
		}
		return true;
	}
	
}
