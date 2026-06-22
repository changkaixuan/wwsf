package com.bocsoft.wwsf.webconsole.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.JobState;
import com.bocsoft.wwsf.webconsole.mapper.JobInstanceMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskInstanceMapper;
import com.bocsoft.wwsf.webconsole.model.BatchStatisticVo;
import com.bocsoft.wwsf.webconsole.model.JobInstance;
import com.bocsoft.wwsf.webconsole.service.JobInstanceService;
import com.bocsoft.wwsf.webconsole.service.TaskInstanceService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("jobInstanceService")
@EnableAsync(proxyTargetClass=true)
@EnableCaching(proxyTargetClass=true)
public class JobInstanceServiceImpl implements JobInstanceService {
	@Autowired
	private JobInstanceMapper jobInstanceMapper;
	@Autowired
	private TaskInstanceMapper taskInstanceMapper;
	@Autowired
	private TaskInstanceService taskInstanceService;
	@Autowired
	private WwseService wwseService;
	
	public PageInfo<JobInstance> jobInsList(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobInstanceMapper.jobInsList(condition);
			}
		});
	}
	
	public JobInstance getJobInstance(Map<String, Object> condition) throws Exception {
		List<JobInstance> jobInstanceList = jobInstanceMapper.getJobInstance(condition);
		if(null != jobInstanceList && jobInstanceList.size()>0) {
			return jobInstanceList.get(0);
		}
		return null;
	}
	
	public int delJobIns(Map<String, Object> conditions) throws Exception {
		return jobInstanceMapper.delJobIns(conditions);
	}
	
	public int delTaskIns(Map<String, Object> conditions) throws Exception {
		return taskInstanceMapper.delTaskInsByJobInsId(conditions);
	}
	
	public int delTaskInsLean(Map<String, Object> conditions) throws Exception {
		return taskInstanceMapper.delTaskInsLeanJobInsId(conditions);
	}

	public int saveJobIns(Map<String, Object> conditions) throws Exception {
		return jobInstanceMapper.saveJobIns(conditions);
	}

	@Transactional
	public int delJobIns(String products, String jobInsIds) throws Exception {
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		
		Map<String, Object> conditions = new HashMap<>();
		String product = "";
		String jobInsId = "";
		
		for(int i=0, len = productArr.length; i<len; i++){
			product = productArr[i];
			jobInsId = jobInsIdArr[i];
			if(product==null || "".equals(product) || jobInsId==null || "".equals(jobInsId))
				continue;
			conditions.put("product", product);
			conditions.put("jobInsId", jobInsId);
			delJobIns(conditions);
			delTaskIns(conditions);
			delTaskInsLean(conditions);
		}
		return 1;
	}

	public BatchStatisticVo getBatchStatisticVo(Map<String, Object> conditions) throws Exception {
		BatchStatisticVo bsv = new BatchStatisticVo();
		bsv.setProduct((String)conditions.get("product"));
		bsv.setBatch((String)conditions.get("batch"));
		List<BatchStatisticVo> dBatchStatisticVoList = new ArrayList<BatchStatisticVo>();
		dBatchStatisticVoList.add(bsv);
		setBatchStatisticVo(dBatchStatisticVoList);
		return bsv;
	}

	@Transactional
	public int clearBatch(String product, String batch) throws Exception {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("batch", batch);
		conditions.put("product", product);
		taskInstanceService.deleteTaskInsLeanByBatch(conditions);
		taskInstanceService.deleteTaskInsByBatch(conditions);
		jobInstanceMapper.delJobInsByBatch(conditions);
		
		return 1;
	}
	
	@Transactional
	public void delBatches(String productArr, String batchArr) throws Exception {
		String[] proArr = productArr.split(",");
		String[] bthArr = batchArr.split(",");
		if (proArr.length != bthArr.length) {
			throw new Exception("产品和批量不一致");
		}
		for(int i=0, len=proArr.length; i<len; i++) {
			clearBatch(proArr[i], bthArr[i]);
		}
	}
	
	public PageInfo<BatchStatisticVo> redisBatchs(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobInstanceMapper.redisBatchs(condition);
			}
		});
	}
	
	public List<BatchStatisticVo> setBatchStatisticVo(List<BatchStatisticVo> dBatchStatisticVoList) throws Exception{
		List<BatchStatisticVo> rBatchStatisticVoList = new ArrayList<BatchStatisticVo>();
		if(null != dBatchStatisticVoList && dBatchStatisticVoList.size() > 0) {
			for(BatchStatisticVo fBatchStatisticVo : dBatchStatisticVoList) {
				Map<String,Object> conditions = new HashMap<String,Object>();
				conditions.put("product", fBatchStatisticVo.getProduct());
				conditions.put("batch", fBatchStatisticVo.getBatch());

				//job实例
				List<JobInstance> jobInstanceList = jobInstanceMapper.jobInsList(conditions); //根据产品、批次，取job实例
				if(null != jobInstanceList && jobInstanceList.size()>0) {
					List<String> jobInsIds = new ArrayList<String>();
					int count = 0;
					for(JobInstance fJobInstance : jobInstanceList) {
						jobInsIds.add(fJobInstance.getJobId()+"|"+fJobInstance.getJobInsId());
						count++;
						if(count==10) {
							jobInsIds.add("......");
							break;
						}
					}
					fBatchStatisticVo.setJobInsIds(jobInsIds);
					fBatchStatisticVo.setJobInsTotal(jobInstanceList.size());
				}
				
				//task实例
				List<Map<String, Object>> taskStateMapList = (List<Map<String, Object>>) taskInstanceService.getTaskState(conditions);
				if(taskStateMapList != null && taskStateMapList.size()>0) {
					Map<String, Object> taskStateMap = taskStateMapList.get(0);
					
					int taskInsTotal = Integer.valueOf(taskStateMap.get("total_count").toString());
					int taskInitTotal = Integer.valueOf(taskStateMap.get("init_count").toString());
					int taskReadyTotal = Integer.valueOf(taskStateMap.get("ready_count").toString());
					int taskRunningTotal = Integer.valueOf(taskStateMap.get("running_count").toString());
					int taskErrorDelayTotal = Integer.valueOf(taskStateMap.get("error_delay_count").toString());
					int taskFeedbackTotal = Integer.valueOf(taskStateMap.get("feedback_count").toString());
					int taskMaualTotal = Integer.valueOf(taskStateMap.get("manual_count").toString());
					int taskCycleDelayTotal = Integer.valueOf(taskStateMap.get("period_delay_count").toString());
					int taskFailedTotal = Integer.valueOf(taskStateMap.get("failed_count").toString());
					int taskSuccessTotal = Integer.valueOf(taskStateMap.get("passed_count").toString());
					
					fBatchStatisticVo.setTaskInsTotal(taskInsTotal);
					fBatchStatisticVo.setTaskInitTotal(taskInitTotal);
					fBatchStatisticVo.setTaskReadyTotal(taskReadyTotal);
					fBatchStatisticVo.setTaskRunningTotal(taskRunningTotal);
					fBatchStatisticVo.setTaskErrorDelayTotal(taskErrorDelayTotal);
					fBatchStatisticVo.setTaskFeedbackTotal(taskFeedbackTotal);
					fBatchStatisticVo.setTaskMaualTotal(taskMaualTotal);
					fBatchStatisticVo.setTaskCycleDelayTotal(taskCycleDelayTotal);
					fBatchStatisticVo.setTaskFailedTotal(taskFailedTotal);
					fBatchStatisticVo.setTaskSuccessTotal(taskSuccessTotal);
					
					Long beginTime = 0L;
					Long endTime = 0L ;
					String beginTimeStr = (String) taskStateMap.get("begin_time");
					String endTimeStr = (String) taskStateMap.get("end_time");
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
					if (null != beginTimeStr && beginTimeStr.matches("\\d{8} \\d{2}:\\d{2}:\\d{2}")) {
						beginTime = sdf.parse(beginTimeStr).getTime();
					}
					if (null != endTimeStr && endTimeStr.matches("\\d{8} \\d{2}:\\d{2}:\\d{2}")) {
						endTime = sdf.parse(endTimeStr).getTime();
					}
					if(taskInsTotal == taskInitTotal) {
						fBatchStatisticVo.setState(JobState.STATE_INIT);
						fBatchStatisticVo.setStateDesc("初始化");
						if(beginTime > 0) {
							fBatchStatisticVo.setSpentTime(CalendarUtil.formatSpent(System.currentTimeMillis() - beginTime));
						}
				    }else if(taskInsTotal == taskSuccessTotal) {
						fBatchStatisticVo.setState(JobState.STATE_QUIT);
						fBatchStatisticVo.setStateDesc("执行结束");
						if (beginTime > 0 && endTime > 0) {
							fBatchStatisticVo.setSpentTime(CalendarUtil.formatSpent(endTime - beginTime));
						}
					}else{
						fBatchStatisticVo.setState(JobState.STATE_RUNNING);
						fBatchStatisticVo.setStateDesc("正在运行");
						if(beginTime > 0) {
							fBatchStatisticVo.setSpentTime(CalendarUtil.formatSpent(System.currentTimeMillis() - beginTime));
						}
					}
				}
				
				rBatchStatisticVoList.add(fBatchStatisticVo);
			}
		}
		return rBatchStatisticVoList;
	}

	@Override
	public void jobRerunfailed(String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<jobInsIdArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobRerunFailed(jobInsId, null);
		}
	}

	@Override
	public void jobRerunUnpassed(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobRerunUnpassed(jobInsId);
		}
	}

	@Override
	public void jobRerun(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			//recursion: 值为false时,只重新运行当前任务；值为true时，递归重新运行当前任务
			//exceptRunning: 值为false时，排除正在运行的task
			wwseService.jobRerun(jobInsId, null, false, false);
		}
	}
	
	@Override
	public void jobRenovate(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobRenovate(jobInsId);
		}
	}

	@Override
	public void jobDisabled(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobDisabled(jobInsId, null);
		}
	}

	@Override
	public void jobPause(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobPause(jobInsId);
		}
	}

	@Override
	public void jobContinue(String[] productArr, String[] jobInsIdArr) throws Exception {
		long jobInsId = 0l;
		for(int i=0;i<productArr.length;i++){
			jobInsId = Long.parseLong(jobInsIdArr[i]);
			wwseService.jobContinue(jobInsId);
		}
	}
	
	public void jobDriver(Long[] jobInsId) throws Exception {
		wwseService.jobDriver(jobInsId);
	}
	
	//单批量统计
	public List<JobInstance> queryJobInstance(Map<String, Object> condition) throws Exception{
		return jobInstanceMapper.queryJobInstance(condition);
	}
	
	public PageInfo<JobInstance> queryJobInstance(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobInstanceMapper.queryJobInstance(condition);
			}
		});
	}
	
	public List<Map<String,Object>> getJobInstanceBatch(Map<String, Object> condition) throws Exception{
		return jobInstanceMapper.getJobInstanceBatch(condition);
	}
	
	public List<Map<String,Object>> getJobInstanceCountByProduct(Map<String, Object> condition) throws Exception{
		return jobInstanceMapper.getJobInstanceCountByProduct(condition);
	}

}
