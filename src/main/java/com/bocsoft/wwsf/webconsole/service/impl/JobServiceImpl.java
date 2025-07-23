package com.bocsoft.wwsf.webconsole.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.ExcelUtils;
import com.bocsoft.wwsf.webconsole.JobVerifyUtil;
import com.bocsoft.wwsf.webconsole.SqlUtils;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.mapper.CalendarMapper;
import com.bocsoft.wwsf.webconsole.mapper.CronMapper;
import com.bocsoft.wwsf.webconsole.mapper.DataSourceMapper;
import com.bocsoft.wwsf.webconsole.mapper.DimensionEntityMapper;
import com.bocsoft.wwsf.webconsole.mapper.DimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.JobDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.JobMapper;
import com.bocsoft.wwsf.webconsole.mapper.ParameterMapper;
import com.bocsoft.wwsf.webconsole.mapper.PluginMapper;
import com.bocsoft.wwsf.webconsole.mapper.ProductMapper;
import com.bocsoft.wwsf.webconsole.mapper.ServerMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskLeanMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskSpecialLeanMapper;
import com.bocsoft.wwsf.webconsole.model.Batch;
import com.bocsoft.wwsf.webconsole.model.BatchJob;
import com.bocsoft.wwsf.webconsole.model.Calendar;
import com.bocsoft.wwsf.webconsole.model.Cron;
import com.bocsoft.wwsf.webconsole.model.CronShared;
import com.bocsoft.wwsf.webconsole.model.DataSource;
import com.bocsoft.wwsf.webconsole.model.Dimension;
import com.bocsoft.wwsf.webconsole.model.DimensionEntity;
import com.bocsoft.wwsf.webconsole.model.ExportJob;
import com.bocsoft.wwsf.webconsole.model.ImportJob;
import com.bocsoft.wwsf.webconsole.model.Job;
import com.bocsoft.wwsf.webconsole.model.JobDimension;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.model.PluginParameter;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.ProductCertification;
import com.bocsoft.wwsf.webconsole.model.Server;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskDimension;
import com.bocsoft.wwsf.webconsole.model.TaskGraph;
import com.bocsoft.wwsf.webconsole.model.TaskLean;
import com.bocsoft.wwsf.webconsole.model.TaskSpecialLean;
import com.bocsoft.wwsf.webconsole.service.JobService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("jobService")
public class JobServiceImpl implements JobService{
	
	@Autowired
	private JobMapper jobMapper;
	
	@Autowired
	private JobDimensionMapper jobDimensionMapper;
	
	@Autowired
	private ParameterMapper parameterMapper;
	
	@Autowired
	private TaskSpecialLeanMapper taskSpecialLeanMapper;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private TaskLeanMapper taskLeanMapper;
	
	@Autowired
	private TaskDimensionMapper taskDimensionMapper;
	
	@Autowired
	private ProductMapper productMapper;
	
	@Autowired
	private ServerMapper serverMapper;
	
	@Autowired
	private DimensionMapper dimensionMapper;
	
	@Autowired
	private DimensionEntityMapper dimensionEntityMapper;
	
	@Autowired
	private PluginMapper pluginMapper;
	
	@Autowired
	private CalendarMapper calendarMapper;
	
	@Autowired
	private DataSourceMapper dataSourceMapper;
	
	@Autowired
	private CronMapper cronMapper;
	
	public PageInfo<Job> queryJobPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobMapper.queryJobPage(condition);
			}
		});
	}
	
	public List<Job> getJobList(Map<String, Object> condition) throws Exception{
		return jobMapper.queryJobPage(condition);
	}
	
	public Job getJob(String product,String jobId) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("product", product);
		condition.put("jobId", jobId);
		List<Job> jobList = jobMapper.queryJobPage(condition);
		if(null == jobList || jobList.size()<1) {
			return null;
		}
		Job job = jobList.get(0); 
		//job.setMutexGroups(mutexGroups); //互斥组暂时忽略
		//设置 job对象属性properties  和  job对象属性paramterIsMust
		condition = new HashMap<String,Object>();
		condition.put("jobId", job.getJobId());
		condition.put("sourceId", job.getJobId());
		List<Parameter> parameterList = parameterMapper.queryParameterPage(condition);
		if(null != parameterList && parameterList.size()>0) {
			Map<String,String> properties = new HashMap<>();
			Map<String,Integer> jobParamterIsMust = new HashMap<>();
			for(Parameter fParameter : parameterList) {
				properties.put(fParameter.getName(), fParameter.getValue());
				if(fParameter.isRequired()) {
					jobParamterIsMust.put(fParameter.getName(), 1);
				}
			}
			job.setProperties(properties);
			job.setJobParamterIsMust(jobParamterIsMust);
		}
		//设置 job对象属性specialLeans
		Map<String,List<String>> specialLeans = null;
		condition = new HashMap<>();
		condition.put("jobId", job.getJobId());
		List<TaskSpecialLean> taskSpecialLeanList = taskSpecialLeanMapper.getTaskSpecialLean(condition);
	    if(null != taskSpecialLeanList && taskSpecialLeanList.size()>0) {
	    	specialLeans = new HashMap<String,List<String>>();
	    	for(TaskSpecialLean fTaskSpecialLean : taskSpecialLeanList) {
	    		String tKey = fTaskSpecialLean.getTaskId()+"|"+fTaskSpecialLean.getExp();
	    		List<String> tValueList = null;
	    		if(null == specialLeans.get(tKey)) {
	    			tValueList = new ArrayList<>();
	    		}else {
	    			tValueList = specialLeans.get(tKey);
	    		}
	    		tValueList.add(fTaskSpecialLean.getBeTaskId()+"|"+fTaskSpecialLean.getBeExp());
	    		specialLeans.put(tKey, tValueList);
	    	}
	    	job.setSpecialLeans(specialLeans);
	    }
		//设置 job对象属性DefaultDimensions
		Map<String,TreeMap<String, Set<String>>> defaultDimensions = null;
		condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		List<JobDimension> jobDimensionList = this.jobDimensionMapper.queryJobDimensionPage(condition);
		if(null != jobDimensionList && jobDimensionList.size()>0) {
			defaultDimensions = new HashMap<String,TreeMap<String, Set<String>>>();
			for(JobDimension fJobDimension : jobDimensionList) {
				TreeMap<String, Set<String>> treeMap = null;
				if(null == defaultDimensions.get(fJobDimension.getModelNo())) {
					treeMap = new TreeMap<String, Set<String>>();
				}else {
					treeMap = defaultDimensions.get(fJobDimension.getModelNo()); 
				}
				Set<String> set = null;
				if(null == treeMap.get(fJobDimension.getDmsnName())) {
					set = new TreeSet<String>();
				}else {
					set = treeMap.get(fJobDimension.getDmsnName());
				}
				set.add(fJobDimension.getEntityName());
				treeMap.put(fJobDimension.getDmsnName(), set);
				defaultDimensions.put(fJobDimension.getModelNo(), treeMap);
			}
			job.setDefaultDimensions(defaultDimensions);
		}
		return job;
	}
	
	@Transactional
	public void saveJob(String type,Job job) throws Exception{
		if(type.equals("update")) {
			//删除job模板特殊依赖关系
			Map<String,Object> dMap = new HashMap<String,Object>();
			dMap.put("jobId", job.getJobId());
			taskSpecialLeanMapper.deleteTaskSpecialLean(dMap);
			//删除job模板参数
			dMap = new HashMap<String,Object>();
			dMap.put("jobId", job.getJobId());
			dMap.put("sourceId", job.getJobId());
			parameterMapper.deleteParameter(dMap);
			//删除job模板维度信息
			dMap = new HashMap<String,Object>();
			dMap.put("jobId", job.getJobId());
			jobDimensionMapper.deleteJobDimension(dMap);
		}
		//新增job模板特殊依赖关系
		Map<String, List<String>> specialLeans = job.getSpecialLeans();
		if(null != specialLeans && specialLeans.size()>0) {
			for(Map.Entry<String, List<String>> fSpecialLean : specialLeans.entrySet()) {
				List<String> tList = fSpecialLean.getValue();
				for(String fString : tList) {
					TaskSpecialLean taskSpecialLean = new TaskSpecialLean();
					taskSpecialLean.setJobId(job.getJobId());
					taskSpecialLean.setTaskId(fSpecialLean.getKey().split("\\|")[0]);
					if(fSpecialLean.getKey().split("\\|").length > 1) {
						taskSpecialLean.setExp(fSpecialLean.getKey().split("\\|")[1]);
					}
					taskSpecialLean.setBeTaskId(fString.split("\\|")[0]);
					if(fString.split("\\|").length > 1) {
						taskSpecialLean.setBeExp(fString.split("\\|")[1]);
					}
					taskSpecialLeanMapper.insertTaskSpecialLean(taskSpecialLean);
				}
			}
		}
		//新增job模板参数
		Map<String,String> properties = job.getProperties();
		if(null != properties && properties.size()>0) {
			Map<String,Integer> jobParamterIsMust = job.getJobParamterIsMust();
			for(Map.Entry<String, String> fPropertie : properties.entrySet()) {
				Parameter parameter = new Parameter();
				parameter.setJobId(job.getJobId());
				parameter.setSourceId(job.getJobId());
				parameter.setName(fPropertie.getKey());
				parameter.setValue(fPropertie.getValue());
				if(null != jobParamterIsMust && jobParamterIsMust.get(fPropertie.getKey()) != null){
					parameter.setdRequired(jobParamterIsMust.get(fPropertie.getKey()));
				}else {
					parameter.setdRequired(0);
				}
				parameterMapper.insertParameter(parameter);
			}
		}
		//新增job模板维度信息
		Map<String,TreeMap<String, Set<String>>> defaultDimensions = job.getDefaultDimensions();
		if(null != defaultDimensions && defaultDimensions.size()>0) {
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
						jobDimensionMapper.insertJobDimension(jobDimension);
					}
				}
			}
		}
		//保存job模板
		if(type.equals("update")) {//更新job模板基本信息
			jobMapper.updateJob(job);
		}else {//新增job模板基本信息
			jobMapper.insertJob(job);
		}
	}
	
	@Transactional
	public void deleteJob(String[] jobIdArr) throws Exception{
		for(int i=0;i<jobIdArr.length;i++){
			String[] idsuse = jobIdArr[i].split("\\+"); //idsuse[1] = product
			String jobId = idsuse[0];
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", jobId);
			List<Task> taskList = taskMapper.queryTaskPage(condition);
			if(null != taskList && taskList.size()>0) {
				for(Task fTask : taskList) {
					Map<String,Object> dTaskMap = new HashMap<String,Object>();
					dTaskMap.put("jobId",jobId);
					dTaskMap.put("taskId", fTask.getTaskId());
					//删除TASK模板依赖关系
					taskLeanMapper.deleteTaskLean(dTaskMap);
					//删除TASK模板维度
					taskDimensionMapper.deleteTaskDimension(dTaskMap);
					//删除TASK模板属性
					dTaskMap.put("sourceId", fTask.getTaskId());
					parameterMapper.deleteParameter(dTaskMap);
					//删除TASK模板
					taskMapper.deleteTask(dTaskMap);
				}
			}
			//删除JOB模板参数
			Map<String,Object> dJobMap = new HashMap<String,Object>();
			dJobMap.put("jobId", jobId);
			dJobMap.put("sourceId", jobId);
			parameterMapper.deleteParameter(dJobMap);
			//删除JOB模板维度
			dJobMap = new HashMap<String,Object>();
			dJobMap.put("jobId", jobId);
			jobDimensionMapper.deleteJobDimension(dJobMap);
			//删除JOB模板特殊依赖
			taskSpecialLeanMapper.deleteTaskSpecialLean(dJobMap);
			//删除JOB模板
			jobMapper.deleteJob(dJobMap);    
    	}
	}

	@Override
	public List<Job> getJobListIgnoreMode() throws Exception{
		return jobMapper.getJobListIgnoreMode();
	}

	@Override
	public List<Map<String,Object>> getJobCountByProduct(Map<String, Object> condition) throws Exception {
		return jobMapper.getCountByProduct(condition);
	}

	@Override
	@Transactional
	public int copyAddJob(Job source, Job target) throws Exception {
		String sourceJobId = source.getJobId();
		String targetJobId = target.getJobId();
		Job old = getJob(source.getProduct(), sourceJobId);
		if (old == null) {
			throw new Exception("查询不到job["+sourceJobId+"]信息");
		}
		if (getJob(target.getProduct(), targetJobId) != null) {
			throw new Exception("job["+sourceJobId+"]已存在");
		}
		// 判断依赖关系是否闭环
		StringBuffer loopmsg = new StringBuffer();
		// 查询出这个job下的所有task
		Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", sourceJobId);
		List<Task> taskList = taskMapper.queryTaskPage(condition);
		// 查询出这个job模板下所有task被依赖关系
		List<TaskLean> taskBeLeanList = taskLeanMapper.queryTaskBeLean(condition);
		boolean cloop = JobVerifyUtil.isContainLoop(taskList, taskBeLeanList, loopmsg);
		if(cloop) {
			// 删除task新的依赖关系
			throw new Exception("job tasks exists contain loop. " + loopmsg.toString());
		}
		// ************ 复制job相关信息 *************
		// 复制参数
		target.setProperties(old.getProperties());
		// 复制参数是否必填
		target.setJobParamterIsMust(old.getJobParamterIsMust());
		// 复制维度
		target.setDefaultDimensions(old.getDefaultDimensions());
		// 复制特殊依赖
		target.setSpecialLeans(old.getSpecialLeans());
		// 互斥组忽略
		// target.setMutexGroups(old.getMutexGroups());
		target.setdCreateTime(CalendarUtil.parseDate(target.getCreateTime(), "yyyy-MM-dd"));
		saveJob("add", target);
		
		// ************ 复制task相关信息 *************
		// 复制task
		/*Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", sourceJobId);
		List<Task> taskList = taskMapper.queryTaskPage(condition);*/
		TaskGraph taskGraph = null;
		Map<String, Object> map = new HashMap<>();
		map.put("jobId", sourceJobId);
		for (Task task : taskList) {
			task.setJobId(targetJobId);
			task.setdCreateTime(CalendarUtil.parseDate(task.getCreateTime(),"yyyy-MM-dd"));
			taskMapper.insertTask(task);
			// 加入坐标信息
			map.put("taskId", task.getTaskId());
			List<TaskGraph> taskGraphList = taskMapper.queryTaskGraph(map);
			if (taskGraphList != null && taskGraphList.size()>0) {
				taskGraph = taskGraphList.get(0);
				taskGraph.setJobId(targetJobId);
				taskMapper.insertTaskGraph(taskGraph);
			}
		}
		// 复制task依赖
		List<TaskLean> taskLeanList = taskLeanMapper.queryTaskLeanPage(condition);
		for (TaskLean taskLean : taskLeanList) {
			taskLean.setJobId(targetJobId);
			taskLeanMapper.insertTaskLean(taskLean);
		}
		// 复制task维度信息
		List<TaskDimension> taskDimensionList = taskDimensionMapper.queryTaskDimensionPage(condition);
		for (TaskDimension taskDimension : taskDimensionList) {
			taskDimension.setJobId(targetJobId);
			taskDimensionMapper.insertTaskDimension(taskDimension);
		}
		// 复制task参数信息
		List<Parameter> parameterList = parameterMapper.queryParameterPage(condition);
		for (Parameter parameter : parameterList) {
			if (!sourceJobId.equals(parameter.getSourceId())) {
				parameter.setJobId(targetJobId);
				parameterMapper.insertParameter(parameter);
			}
		}
		return 0;
	}
	
	public ExportJob getExportJob(String exportFileCatalog,List<Job> jobList){
		ExportJob exportJob = new ExportJob();  //封装数据
		if(null == jobList || jobList.size() < 1) {
			exportJob.getExportErrorInfoList().add("没有符合导出条件的任务模板");
		}else {
			//PRODUCT
			String productId = jobList.get(0).getProduct();
			Map<String,Object> queryMap = new HashMap<>();
			queryMap.put("pId", productId);
			if((","+exportFileCatalog+",").indexOf(",PRODUCT,") != -1) {
				List<Product> productList = productMapper.selectProductByName(queryMap);
				if(null == productList || productList.size() < 1) {
					exportJob.getExportErrorInfoList().add("WW_PRODUCT： 不存在产品"+productId);
				}else {
				    exportJob.setProduct(productList.get(0));
				}
			}
			//CRON 取产品下所有cron涉及job
			Map<String,Map<String,Object>> jobIdAndCronIdsMap = new HashMap<>(); //存job涉及cron   备注：Map<jobId,Map<cronId,''>>
			Map<String,Object> cronIdMap = new HashMap<>(); //存需要导出的cron 备注：Map<cronId,''>
			if((","+exportFileCatalog+",").indexOf(",CRON,") != -1) {
				queryMap = new HashMap<String,Object>();
				queryMap.put("product", productId);
				queryMap.put("dataType", "JOB");
				List<Map<String,Object>> tList = parameterMapper.getCronIdAndPluParValue(queryMap);
				if (null != tList && tList.size()>0) {
					for(Map<String,Object> fMap : tList) {
						String cronId = fMap.get("job_id").toString();
						String dataType = fMap.get("data_type").toString();
						String jobValue = fMap.get("value").toString();
						if ("JOB".equals(dataType)) {
							BatchJob batchJob = JSONObject.parseObject(jobValue, BatchJob.class);
							if (!StringUtil.hasText(batchJob.getJobId())) continue;
							Map<String,Object> tempMap = null;
							if (jobIdAndCronIdsMap.containsKey(batchJob.getJobId())) {
								tempMap = jobIdAndCronIdsMap.get(batchJob.getJobId());
							} else {
								tempMap = new HashMap<>();
								jobIdAndCronIdsMap.put(batchJob.getJobId(), tempMap);
							}
							tempMap.put(cronId, "");
						} else {
							Batch batch = JSONObject.parseObject(jobValue, Batch.class);
							List<BatchJob> batchJobList = batch.getJobs();
							if(null != batchJobList && batchJobList.size() > 0) {
								for(BatchJob batchJob : batchJobList) {
									if (!StringUtil.hasText(batchJob.getJobId())) continue;
									Map<String,Object> tempMap = null;
									if (jobIdAndCronIdsMap.containsKey(batchJob.getJobId())) {
										tempMap = jobIdAndCronIdsMap.get(batchJob.getJobId());
									} else {
										tempMap = new HashMap<>();
										jobIdAndCronIdsMap.put(batchJob.getJobId(), tempMap);
									}
									tempMap.put(cronId, "");
								}
							}
						}
					}
				}
			}
			String jobIdArr[] = new String[jobList.size()];
			for(int a=0;a<jobList.size();a++) {
				Job fJob = jobList.get(a);
				jobIdArr[a] = fJob.getJobId();
				//CRON
				if (null != jobIdAndCronIdsMap.get(fJob.getJobId())) {
					Map<String,Object> tCronIdMap = jobIdAndCronIdsMap.get(fJob.getJobId());
					for(Map.Entry<String, Object> fMap : tCronIdMap.entrySet()) {
						cronIdMap.put(fMap.getKey(), "");
					}
				}
			}
			//PRODUCT_CERTIFICATION
			//取产品下所有认证信息
			Map<String,ProductCertification> tabPcAuthIdMap = new HashMap<>();
			List<ProductCertification> pcList = productMapper.selectCertificationByProduct(productId);
			if(null != pcList && pcList.size() > 0) {
				for(ProductCertification fProductCertification : pcList) {
					tabPcAuthIdMap.put(fProductCertification.getAuthId(), fProductCertification);
				}
			}
			//取导出的所有job下所有task插件涉及到的产品认证信息
			queryMap = new HashMap<String,Object>();
			queryMap.put("jobIdArr", jobIdArr);
			queryMap.put("dataType", "CERTIFICATION");
			List<Map<String,Object>> taskIdAndPcAuthIdList = parameterMapper.getTaskIdAndPluParValue(queryMap);
			Map<String,ProductCertification> expPcAuthIdMap = new HashMap<>();
			if (null != taskIdAndPcAuthIdList && taskIdAndPcAuthIdList.size()>0) {
				for(Map<String,Object> fMap : taskIdAndPcAuthIdList) {
					if(null == tabPcAuthIdMap.get(fMap.get("value").toString())) {
						exportJob.getExportErrorInfoList().add("WW_TASK：产品认证信息不存在"+fMap.get("value").toString());
					}else {
						expPcAuthIdMap.put(fMap.get("value").toString(),tabPcAuthIdMap.get(fMap.get("value").toString()));
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",PRODUCT_CERTIFICATION,") != -1) {
				if(expPcAuthIdMap.size() > 0) { 
					for(Map.Entry<String, ProductCertification> fMap : expPcAuthIdMap.entrySet()) {
						exportJob.getProductCertificationList().add(fMap.getValue()); 
					}
				}
			}
			//SERVER
			//1.取产品下所有节点
			Map<String,Server> tabServerIdMap = new HashMap<>();
			Map<String,List<Server>> tabServerTagMap = new HashMap<>();
			queryMap = new HashMap<>();
			queryMap.put("product", productId);
			List<Server> serverList = serverMapper.queryServer(queryMap);
			if(null != serverList && serverList.size() > 0) {
				for(Server fServer : serverList) {
					tabServerIdMap.put(fServer.getServerId(), fServer);
					if (StringUtil.hasText(fServer.getTags())) {
						String[] tags = fServer.getTags().split(",");
						for (String tag : tags) {
							if (!tabServerTagMap.containsKey("#"+tag)) {
								tabServerTagMap.put("#"+tag, new ArrayList<Server>());
							}
							tabServerTagMap.get("#"+tag).add(fServer);
						}
					}
				}
			}
			//2.取导出的所有job下所有task插件涉及到的服务节点
			Map<String,Server> taskUseServerIdMap = new HashMap<>();
			queryMap = new HashMap<String,Object>();
			queryMap.put("jobIdArr", jobIdArr);
			queryMap.put("dataType", "SERVER");
			List<Map<String,Object>> taskIdAndServerIdList = parameterMapper.getTaskIdAndPluParValue(queryMap);
			if(null != taskIdAndServerIdList && taskIdAndServerIdList.size() > 0) {
				//判断使用的服务是否存在
				for(Map<String,Object> fMap : taskIdAndServerIdList) {
					String paramServer = fMap.get("VALUE").toString();
					if (tabServerTagMap.containsKey(paramServer))
					if(null == tabServerIdMap.get(fMap.get("VALUE").toString())) {
						exportJob.getExportErrorInfoList().add("WW_TASK：任务参数节点编号不存在"+fMap.get("VALUE").toString());
					}else {
						taskUseServerIdMap.put(paramServer, tabServerIdMap.get(paramServer));
					}
				}
			}
			//3.取导出的所有job下所有task使用的可执行节点范围
			List<Map<String,Object>> agentScopList = taskMapper.getAgentScopeByJobIds(queryMap);
			if (null != agentScopList && agentScopList.size() > 0) {
				Set<String> idAndTags = new HashSet<String>();
				for (Map<String,Object> fMap : agentScopList) {
					if(null == fMap) {
						taskUseServerIdMap.put("-1", new Server());
					}else {
						String agentScop = (String)fMap.get("AGENT_SCOPE");
						if (!StringUtil.hasText(agentScop)) {
							taskUseServerIdMap.put("-1", new Server());
						}else {
							idAndTags.addAll(StringUtil.commaDelimitedListToSet(agentScop));
						}
					}
				}
				if(!idAndTags.isEmpty()) {
					for (String as : idAndTags) {
						if (tabServerIdMap.containsKey(as) || tabServerTagMap.containsKey(as)) {
							if (tabServerIdMap.get(as) != null) {
								taskUseServerIdMap.put(as, tabServerIdMap.get(as));
							} else {
								for (Server s :tabServerTagMap.get(as)) {
									taskUseServerIdMap.put(s.getServerId(), s);
								}
							}
						} else {
							exportJob.getExportErrorInfoList().add("WW_TASK：节点编号或标签不存在" + as);
						}
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",SERVER,") != -1) {
				if (null != taskUseServerIdMap.get("-1")) { //取所有服务
					exportJob.setServerList(serverList);
				}else {
					for(Map.Entry<String, Server> fMap : taskUseServerIdMap.entrySet()) {
						exportJob.getServerList().add(fMap.getValue());
					}
				}
			}
			//DS
			Map<String,DataSource> tabDsNameMap = new HashMap<>();
			queryMap = new HashMap<>();
			queryMap.put("nullAndProduct", productId);
			List<DataSource> dsList = dataSourceMapper.queryDataSourcePage(queryMap);
			if (null != dsList && dsList.size() > 0) {
				for(DataSource fDataSource : dsList) {
					tabDsNameMap.put(fDataSource.getName(), fDataSource);
				}
			}
			//取job下所有task插件涉及到的数据源信息  忽略验证：数据源在哪个节点上
			queryMap = new HashMap<>();
			queryMap.put("jobIdArr", jobIdArr);
			queryMap.put("dataType", "DATASOURCE");
			List<Map<String,Object>> taskIdAndDsNameList = parameterMapper.getTaskIdAndPluParValue(queryMap);
			Map<String,DataSource> expDsNameMap = new HashMap<>();
			if (null != taskIdAndDsNameList && taskIdAndDsNameList.size()>0) {
				for(Map<String,Object> fMap : taskIdAndDsNameList) {
					if(null == tabDsNameMap.get(fMap.get("VALUE").toString())) {
						exportJob.getExportErrorInfoList().add("WW_TASK：数据源不存在"+fMap.get("VALUE").toString());
					}else {
						expDsNameMap.put(fMap.get("VALUE").toString(),tabDsNameMap.get(fMap.get("VALUE").toString()));
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",DS,") != -1) {
				if(expDsNameMap.size() > 0) { 
					for(Map.Entry<String, DataSource> fMap : expDsNameMap.entrySet()) {
						exportJob.getDataSourceList().add(fMap.getValue());
					}
				}
			}
			//dimension
			Map<String,Map<String,Object>> dmsnEntityMap = new HashMap<>(); //存所有维度实体,用来判断task或job维度实体是否存在
			Map<String,Map<String,Object>> dmsnTagMap = new HashMap<>(); //存所有维度标签,用来判断task或job维度标签是否存在
			Map<String,Dimension> tabDimensionMap = new HashMap<>();
			Map<String,DimensionEntity> tabDimensionEntityMap = new HashMap<>();
			queryMap = new HashMap<String,Object>();
			queryMap.put("product", productId);
			List<Dimension> dimensionList = dimensionMapper.queryDimensionPage(queryMap);
			if(null != dimensionList && dimensionList.size() > 0) {
				for(Dimension fDimension : dimensionList) {
					tabDimensionMap.put(fDimension.getName(), fDimension);
				}
			}
			List<DimensionEntity> dimensionEntityList = dimensionEntityMapper.queryDimensionEntityPage(queryMap);
			if(null != dimensionEntityList && dimensionEntityList.size() > 0) {
				for(DimensionEntity fDimensionEntity : dimensionEntityList) {
					Map<String,Object> tempEntityMap = new HashMap<>();
					if(null == dmsnEntityMap.get(fDimensionEntity.getName())) {
						tempEntityMap = new HashMap<>();
					}else {
						tempEntityMap = dmsnEntityMap.get(fDimensionEntity.getName());
					}
					tempEntityMap.put(fDimensionEntity.getEntity(), "");
					dmsnEntityMap.put(fDimensionEntity.getName(), tempEntityMap);
					Map<String,Object> tempTagMap = new HashMap<>();
					if(null == dmsnTagMap.get(fDimensionEntity.getName())) {
						tempTagMap = new HashMap<>();
					}else {
						tempTagMap = dmsnTagMap.get(fDimensionEntity.getName());
					}
					String tags = fDimensionEntity.getTags();
					if(null != tags && !tags.equals("")) {
						tags = tags.substring(1,tags.length()-1); //去掉首尾,
						String tagArr [] = tags.split(",");
						for(String fTag : tagArr) {
							tempTagMap.put("TAG:"+fTag, "");
						}
					}
					if(tempTagMap.size() > 0) {
						dmsnTagMap.put(fDimensionEntity.getName(), tempTagMap);
					}
					tabDimensionEntityMap.put(fDimensionEntity.getName()+"###"+fDimensionEntity.getEntity(),fDimensionEntity);
				}
			}
			//WW_JOB_DIMENSION
			queryMap = new HashMap<String,Object>();
			queryMap.put("dmsnProduct",productId);
			queryMap.put("jobIdArr", jobIdArr);
			List<JobDimension> jobDimensionList = jobDimensionMapper.queryJobDimensionPage(queryMap);
			Map<String,Dimension> expDimensionMap = new HashMap<>();
			Map<String,DimensionEntity> expDimensionEntityMap = new HashMap<>();
			if(null != jobDimensionList && jobDimensionList.size() > 0) {
				for(JobDimension fJobDimension : jobDimensionList) {
					if(fJobDimension.getEntityName().startsWith("TAG:")) {
						Map<String,Object> tempTagMap = dmsnTagMap.get(fJobDimension.getDmsnName());
						if(null == tempTagMap || null == tempTagMap.get(fJobDimension.getEntityName())) {
							exportJob.getExportErrorInfoList().add("WW_JOB_DIMENSION：不存在维度/标签("+fJobDimension.getDmsnName()+"/"+fJobDimension.getEntityName()+")");
						}
						List<DimensionEntity> tDimensionEntityList = getDimensionEntity(fJobDimension.getDmsnName(),fJobDimension.getEntityName(),dimensionEntityList);
						if(null != tDimensionEntityList && tDimensionEntityList.size() > 0) {
							for(DimensionEntity fDimensionEntity : tDimensionEntityList) {
								expDimensionEntityMap.put(fDimensionEntity.getName()+"###"+fDimensionEntity.getEntity(), fDimensionEntity);
							}
						}
					}else {
						Map<String,Object> tempEntityMap = dmsnEntityMap.get(fJobDimension.getDmsnName());
						if(null == tempEntityMap || null == tempEntityMap.get(fJobDimension.getEntityName())) {
							exportJob.getExportErrorInfoList().add("WW_JOB_DIMENSION：不存在维度/实体("+fJobDimension.getDmsnName()+"/"+fJobDimension.getEntityName()+")");
						}
						if(null != tabDimensionEntityMap.get(fJobDimension.getDmsnName()+"###"+fJobDimension.getEntityName())) {
							expDimensionEntityMap.put(fJobDimension.getDmsnName()+"###"+fJobDimension.getEntityName(), tabDimensionEntityMap.get(fJobDimension.getDmsnName()+"###"+fJobDimension.getEntityName()));
						}
					}
					exportJob.getJobDimensionList().add(fJobDimension);
					if(null != tabDimensionMap.get(fJobDimension.getDmsnName())) {
						expDimensionMap.put(fJobDimension.getDmsnName(), tabDimensionMap.get(fJobDimension.getDmsnName()));
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",DIMENSION,") != -1) {
				if(expDimensionMap.size() > 0) { 
					for(Map.Entry<String, Dimension> fMap : expDimensionMap.entrySet()) {
						exportJob.getDimensionList().add(fMap.getValue());
					}
				}
				if(dimensionEntityList.size() > 0) { 
					for(Map.Entry<String, DimensionEntity> fMap : expDimensionEntityMap.entrySet()) {
						exportJob.getDimensionEntityList().add(fMap.getValue());
					}
                }
			}
			//PLUGIN 忽略：task没有用到的插件，插件没参数，就不导入参数也不报错
			Map<String,Plugin> tabPluginNameMap = new HashMap<>(); //存表中的插件
			List<Plugin> pluginList = pluginMapper.getPluginParameters(new HashMap<String,Object>());
			if(null != pluginList && pluginList.size() > 0) {
				for(Plugin fPlugin : pluginList) {
					tabPluginNameMap.put(fPlugin.getName(),fPlugin);
				}
			}
			queryMap.put("jobIdArr", jobIdArr);
			List<Map<String,Object>> taskPluginList = taskMapper.getTaskPluginByJobIds(queryMap);
			Map<String,Plugin> expPluginNameMap = new HashMap<>(); //存需要导出的插件
			if (null != taskPluginList && taskPluginList.size() > 0) {
				for (Map<String,Object> fMap : taskPluginList) {
					if(null == tabPluginNameMap.get(fMap.get("TASK_PLUGIN").toString())) {
						exportJob.getExportErrorInfoList().add("WW_TASK：插件不存在"+fMap.get("TASK_PLUGIN").toString());
					}else {
						expPluginNameMap.put(fMap.get("TASK_PLUGIN").toString(),tabPluginNameMap.get(fMap.get("TASK_PLUGIN").toString()));
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",PLUGIN,") != -1) {
				if(expPluginNameMap.size() > 0) { 
					for(Map.Entry<String, Plugin> fMap : expPluginNameMap.entrySet()) {
						Plugin plugin = fMap.getValue();
						if(null == plugin.getPluginParameters() || plugin.getPluginParameters().size() < 1) {
							exportJob.getExportErrorInfoList().add("WW_TASK：插件没有参数"+plugin.getName());
						}
						exportJob.getPluginList().add(plugin);
					}
				}
			}
			//CALENDAR
			Map<String,Calendar> tabCalNameMap = new HashMap<>(); //存表中工作日设定
			queryMap = new HashMap<String,Object>();
			queryMap.put("product", productId);
			List<Calendar> calendarList = calendarMapper.queryCalendarList(queryMap);
			if(null != calendarList && calendarList.size() > 0) {
				for(Calendar fCalendar : calendarList) {
					tabCalNameMap.put(fCalendar.getCalName(), fCalendar);
				}
			}
			//取导出的所有job下所有task使用的工作日设定
			queryMap.put("jobIdArr", jobIdArr);
			List<Map<String,Object>> workingDayList = taskMapper.getWorkingDayByJobIds(queryMap);
			Map<String,Calendar> expCalNameMap = new HashMap<>(); //存需导出的工作日设定
			if (null != workingDayList && workingDayList.size() > 0) {
				for (Map<String,Object> fMap : workingDayList) {
					if(null != fMap && null != fMap.get("WORKING_DAY") && !fMap.get("WORKING_DAY").toString().equals("")) {
						if(null == tabCalNameMap.get(fMap.get("WORKING_DAY").toString())) {
							exportJob.getExportErrorInfoList().add("WW_TASK：工作日设定不存在"+fMap.get("WORKING_DAY").toString());
						}else {
							expCalNameMap.put(fMap.get("WORKING_DAY").toString(), tabCalNameMap.get(fMap.get("WORKING_DAY").toString()));
						}
					}
				}
			}
			if((","+exportFileCatalog+",").indexOf(",CALENDAR,") != -1) {
				if(expCalNameMap.size() > 0) { 
					for(Map.Entry<String, Calendar> fMap : expCalNameMap.entrySet()) {
						exportJob.getCalendarList().add(fMap.getValue());
					}
				}
			}
			for(Job fJob : jobList) {
				queryMap = new HashMap<String,Object>();
				queryMap.put("jobId", fJob.getJobId());
				List<Task> taskList = taskMapper.queryTaskPage(queryMap);//获取task列表
				if (null != taskList && taskList.size() > 0) {
					Map<String,Object> taskIdMap = new HashMap<>();
					for (Task fTask : taskList) {
						taskIdMap.put(fTask.getTaskId(), "");
					}
					//JOB
					exportJob.getJobList().add(fJob);
					//传入参数 and 任务项传入参数
					queryMap = new HashMap<String,Object>();
					queryMap.put("jobId", fJob.getJobId());
					exportJob.getParameterList().addAll(parameterMapper.queryParameterPage(queryMap));
					//特殊依赖
					List<TaskSpecialLean> taskSpecialLeanList = taskSpecialLeanMapper.getTaskSpecialLean(queryMap);
					if(null != taskSpecialLeanList && taskSpecialLeanList.size() > 0) {
						for(TaskSpecialLean fTaskSpecialLean : taskSpecialLeanList) {
							if(null == taskIdMap.get(fTaskSpecialLean.getTaskId())) {
								exportJob.getExportErrorInfoList().add("WW_TASK_SPECIAL_LEAN：TASK编号（"+fTaskSpecialLean.getTaskId() + "）不在任务项列表中");
							}
							if((fTaskSpecialLean.getLeanJobId() == null || fTaskSpecialLean.getLeanJobId().equals("") || fTaskSpecialLean.getJobId().equals(fTaskSpecialLean.getLeanJobId())) && null == taskIdMap.get(fTaskSpecialLean.getBeTaskId())) {
								exportJob.getExportErrorInfoList().add("WW_TASK_SPECIAL_LEAN：被依赖TASK编号（"+fTaskSpecialLean.getBeTaskId() + "）不在任务项列表中");
							}
						}
						exportJob.getTaskSpecialLeanList().addAll(taskSpecialLeanList);
					}
					//任务项依赖
					List<TaskLean> taskLeanList = taskLeanMapper.queryTaskLeanPage(queryMap);
					if(null != taskLeanList && taskLeanList.size() > 0) {
						for(TaskLean fTaskLean : taskLeanList) {
							if(null == taskIdMap.get(fTaskLean.getTaskId())) {
								exportJob.getExportErrorInfoList().add("WW_TASK_LEAN：TASK编号（"+fTaskLean.getTaskId() + "）不在任务项列表中");
							}
							if((fTaskLean.getLeanJobId() == null || fTaskLean.getLeanJobId().equals("") || fTaskLean.getJobId().equals(fTaskLean.getLeanJobId())) && null == taskIdMap.get(fTaskLean.getLeanTaskId())) {
								exportJob.getExportErrorInfoList().add("WW_TASK_LEAN：被依赖TASK编号（"+fTaskLean.getLeanTaskId() + "）不在任务项列表中");
							}
						}
						exportJob.getTaskLeanList().addAll(taskLeanList);
					}
					//任务项
					exportJob.getTaskList().addAll(taskList);
				}else {
					exportJob.getExportErrorInfoList().add("WW_TASK： 任务模板（"+fJob.getJobId()+"）不存在任务项");
				}
			}
			if((","+exportFileCatalog+",").indexOf(",CRON,") != -1 && cronIdMap.size() > 0) {
				String cronIds = "";
				for(Map.Entry<String, Object> fMap : cronIdMap.entrySet()) {
					cronIds += fMap.getKey() + ",";
				}
				queryMap = new HashMap<>();
				queryMap.put("jobIdArr", cronIds.substring(0, cronIds.length()-1).split(","));
				List<Cron> cronList = cronMapper.kvspage(queryMap);
				if(null != cronList && cronList.size() > 0) {
					exportJob.getCronList().addAll(cronList);
				}
				//计划任务参数
				queryMap.put("jobIdEqSourceId", "jobIdEqSourceId");
				exportJob.getParameterList().addAll(parameterMapper.queryParameterPage(queryMap));
			}
		}
		return exportJob;
	}
	
	private List<DimensionEntity> getDimensionEntity(String dmsnName,String title,List<DimensionEntity> dimensionEntityList){
		List<DimensionEntity> rDimensionEntityList = new ArrayList<>();
		if(null != dimensionEntityList && dimensionEntityList.size() > 0) {
			if(title.length() > 4) {
				title = title.substring(4,title.length());
			}
			title = ","+title+",";
			for(DimensionEntity fDimensionEntity : dimensionEntityList) {
				if(fDimensionEntity.getName().equals(dmsnName)) {
					String tags = fDimensionEntity.getTags();
					if(null != tags && tags.indexOf(title) != -1) {
						rDimensionEntityList.add(fDimensionEntity);
					}
				}
			}
		}
		return rDimensionEntityList;
	}
	
	public void genExcelFile(HttpServletResponse response, ExportJob exportJob, String exportFileName) {
		Workbook workbook = null;
		OutputStream os = null;
		try {
			os = response.getOutputStream();
			workbook = new HSSFWorkbook();
			Sheet errorSheet = null;
			if(exportJob.getExportErrorInfoList().size() > 0) {
				errorSheet = workbook.createSheet("ERROR");
				ExcelUtils.createTitle(workbook, errorSheet, new String[]{ "错误信息" }, new int[]{160*256}, 20);
				//生成出错数据
				int errorRowNum = 1;
				List<String> exportErrorInfoList = exportJob.getExportErrorInfoList();
				for(String fString : exportErrorInfoList) {
					errorRowNum = ExcelUtils.createRow(errorSheet, errorRowNum, fString);
				}
				workbook.write(os);
			}else {
				Sheet tSheet = null;
				int tRowNum = 1;
				//产品
				Product product = exportJob.getProduct();
				if(null != product) {
					tSheet = workbook.createSheet("WW_PRODUCT");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{ "P_ID", "P_NAME", "P_DESC" }, new int[]{30*256, 60*256, 80*256}, 20);
					tRowNum = ExcelUtils.createRow(tSheet, tRowNum, product.getpId(), product.getpName(), product.getpDesc());
				}
				//产品认证信息
				List<ProductCertification> productCertificationlist = exportJob.getProductCertificationList();
				if(null != productCertificationlist && productCertificationlist.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_PRODUCT_CERTIFICATION");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{ "P_ID", "AUTH_ID", "PROTOCAL" , "USER_ID", "PUBLIC_KEY"}, new int[]{30*256, 30*256, 30*256, 30*256, 80*256}, 20);
					for (ProductCertification productCertification : productCertificationlist) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,productCertification.getpId(),productCertification.getAuthId(),productCertification.getProtocal(),productCertification.getUserId(),"******");
					}
				}
				//节点
				List<Server> serverList = exportJob.getServerList();
				if(null != serverList && serverList.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_SERVER");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{ "PRODUCT", "SERVER_ID", "SERVER_DESC" , "IP_ADDR", "SSH_PORT", "RMI_REGISTRY_PORT", "RMI_SERVER_PORT", "SERVER_TYPE", "TAGS", "LATEST_ONLINE_TIME", "OS_SSH_PORT", "OS_SSH_USER", "OS_SSH_PSWD", "ZOO_CONNECT_STR","ZOO_NAMESPACE", "HOME_DIR", "IGNORE_PLUGINS", "STATUS", "VERSION_NO"}, 
																  new int[]{20*256, 20*256, 20*256, 20*256, 20*256, 30*256, 20*256, 20*256, 20*256, 30*256, 20*256, 20*256, 40*256, 20*256, 40*256, 40*256, 20*256, 20*256, 20*256}, 20);
					for (Server server : serverList) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,server.getProduct(),server.getServerId(),server.getServerDesc(),server.getIpAddr(),server.getSshPort()+"",server.getRmiRegistryPort()+"",server.getRmiServerPort()+"",server.getServerType(),server.getTags(),server.getLatestOnlineTime(),server.getOsSshPort()+"",
								server.getOsSshUser(),"******",server.getZooConnectStr(),server.getZooNamespace(),server.getHomeDir(),server.getIgnorePlugins(),server.getStatus()+"",server.getVersionNo());
					}
				}
				//数据源
				List<DataSource> dataSourceList = exportJob.getDataSourceList();
				if(null != dataSourceList && dataSourceList.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_DS");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{ "PRODUCT", "AGENT", "NAME" , "DRIVER_NAME", "DRIVER_CLASS", "DATABASE_NAME", "DATABASE_TYPE", "JDBC_URL", "JDBC_USER", "JDBC_PASSWORD", "AUTO_START", "POOL", "XA", "POOL_PROPERTIES"}, 
																  new int[]{20*256, 20*256, 20*256, 20*256, 40*256, 20*256, 20*256, 40*256, 20*256, 20*256, 20*256, 20*256, 20*256, 20*256}, 20);
					for (DataSource dataSource : dataSourceList) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,dataSource.getProduct(),dataSource.getAgent(),dataSource.getName(),dataSource.getDriverName(),dataSource.getDriverClass(),dataSource.getDatabaseName(),
								dataSource.getDatabaseType(),dataSource.getJdbcUrl(),dataSource.getJdbcUser(),"******",String.valueOf(dataSource.getAutoStart()),dataSource.getPool(),dataSource.getXa(),dataSource.getPoolProperties());
					}
				}
				//维度信息
				List<Dimension> dimensions = exportJob.getDimensionList();
				if(null != dimensions && dimensions.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_DIMENSION");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"PRODUCT","DMSN_NAME","DMSN_DESC"},new int[]{20*256,20*256,50*256}, 20);
					for (Dimension dimension : dimensions) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,dimension.getProduct(),dimension.getName(),dimension.getDescription());
					}
				}
				List<DimensionEntity> dimensionEntities = exportJob.getDimensionEntityList();
				if(null != dimensionEntities && dimensionEntities.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_DIMENSION_ENTITY");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"PRODUCT","DMSN_NAME","ENTITY_NAME","ENTITY_DESC","TITLE"},new int[]{20*256,20*256,30*256,40*256,20*256}, 20);
					//int sheetNum = ExportJob.EXPORT_EXCEL_SHEET_NAME_START_NUM;
					for (DimensionEntity dimensionEntity : dimensionEntities) {
						if(tRowNum > ExcelUtils.excelMaxRow){
							//tSheet = workbook.createSheet("WW_DIMENSION_ENTITY"+sheetNum);
							//ExcelUtils.createTitle(workbook,tSheet,new String[]{"PRODUCT","DMSN_NAME","ENTITY_NAME","ENTITY_DESC","TITLE"},new int[]{20*256,20*256,30*256,40*256,20*256}, 20);
							//tRowNum = 1;
							//sheetNum++;
							break;
						}
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,dimensionEntity.getProduct(),dimensionEntity.getName(),dimensionEntity.getEntity(),dimensionEntity.getEntityDesc(),dimensionEntity.getTags());
					}
				}
				//插件
				List<Plugin> plugins = exportJob.getPluginList();
				if (null != plugins && plugins.size() > 0) {
					Sheet tSheet2 = null;
					int tRowNum2 = 1;
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_PLUGIN");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"NAME","PLUGIN_DESC","FEATURE_NAME","VERSION"},new int[]{30*256,60*256,60*256,20*256}, 20);
					for (Plugin plugin : plugins) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,plugin.getName(),plugin.getDesc(),plugin.getFeature(),plugin.getVersion());
						//插件参数
						List<PluginParameter> pluginParameters = plugin.getPluginParameters();
						for (PluginParameter pluginParameter : pluginParameters) {
							if (null == tSheet2) {
								tSheet2 = workbook.createSheet("WW_PLUGIN_PARAMETER");
								ExcelUtils.createTitle(workbook, tSheet2, new String[]{"PLUGIN_NAME","PARAM_NAME","DATA_TYPE","PARAM_DEF_VALUE","IS_REQUIRED","PARAM_DESC","DATA_RANGE"}, new int[]{20*256,20*256,20*256,20*256,20*256,80*256,20*256}, 20);
							}
							tRowNum2 = ExcelUtils.createRow(tSheet2, tRowNum2,pluginParameter.getPluginName(),pluginParameter.getParamName(),pluginParameter.getDataType(),pluginParameter.getParamDefValue(),String.valueOf(pluginParameter.getIsRequired()),pluginParameter.getParamDesc(),pluginParameter.getDataRange());
						}
					}
				}
				//假日编排
				List<Calendar> calendars = exportJob.getCalendarList();
				if (null != calendars && calendars.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_CALENDAR");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"PRODUCT","CAL_NAME","CAL_DESCRIPTION","INCLUDE_VALUE","EXCLUSION_VALUE"},new int[]{20*256,20*256,50*256,50*256,50*256}, 20);
					for (Calendar calendar : calendars) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,calendar.getProduct(),calendar.getCalName(),calendar.getCalDescription(),calendar.getIncludeValue(),calendar.getExclusionValue());
					}
				}
				//计划任务
				List<Cron> cronList = exportJob.getCronList();
				if (null != cronList && cronList.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_CRON");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{"JOB_ID","MODEL","PRODUCT","AGENT","JOB_NAME","CRON_EXPRESSION","PLUGIN","PROGRAM_NAME","AUTO_START","CREATE_TIME","CREATOR"}, new int[]{20*256,20*256,20*256,20*256,40*256,40*256,20*256,20*256,20*256,20*256,20*256}, 20);
					for (Cron cron : cronList) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,cron.getJobId(),cron.getModel(),cron.getProduct(),cron.getAgent(),cron.getJobName(),cron.getCronExpression(),cron.getPlugin(),cron.getProgramName(),cron.getAutoStart()+"",cron.getCreateTime(),cron.getCreator());
					}
					List<CronShared> cronSharedList = exportJob.getCronSharedList();
					if(null != cronSharedList && cronSharedList.size() >0) {
						tRowNum = 1;
						tSheet = workbook.createSheet("WW_CRON_SHARED");
						ExcelUtils.createTitle(workbook, tSheet, new String[]{"PRODUCT","AGENT","JOB_ID","SHARED_JSON","OCCU_TIME"}, new int[]{20*256,20*256,20*256,20*256,40*256}, 20);
					}
					for (CronShared cronShared : cronSharedList) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,cronShared.getProduct(),cronShared.getAgent(),cronShared.getJobId(),cronShared.getSharedJson(),cronShared.getOccuTime());
					}
				}
				//任务模板JOB
				List<Job> jobList = exportJob.getJobList();
				if(null != jobList && jobList.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_JOB");
					ExcelUtils.createTitle(workbook, tSheet, new String[]{"JOB_ID","PRODUCT","JOB_NAME","JOB_MODE","SCHEDULE_RID","TITLE","VERSION","CREATE_TIME","CREATOR"}, new int[]{20*256,20*256,40*256,20*256,20*256,20*256,20*256,20*256,20*256}, 20);
					for(Job fJob : jobList) {
						tRowNum = ExcelUtils.createRow(tSheet, tRowNum,fJob.getJobId(),fJob.getProduct(),fJob.getName(),String.valueOf(fJob.getMode()),fJob.getScheduleRid(),fJob.getTitle(),fJob.getVersion(),fJob.getCreateTime(),fJob.getCreator());
					}
				}
				//维度方案
				List<JobDimension> jobDimensions = exportJob.getJobDimensionList();
				if(null != jobDimensions && jobDimensions.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_JOB_DIMENSION");
					ExcelUtils.createTitle(workbook, tSheet,new String[]{"JOB_ID","MODEL_NO","DMSN_NAME","DMSN_PRODUCT","ENTITY_NAME"},new int[]{20*256,20*256,30*256,20*256,30*256}, 20);
					for (JobDimension jobDimension : jobDimensions) {
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,jobDimension.getJobId(),jobDimension.getModelNo(),jobDimension.getDmsnName(),jobDimension.getDmsnProduct(),jobDimension.getEntityName());
					}
				}
				//特殊依赖关系
				List<TaskSpecialLean> taskSpecialLeans = exportJob.getTaskSpecialLeanList();
				if (null != taskSpecialLeans && taskSpecialLeans.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_TASK_SPECIAL_LEAN");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","TASK_DIM_ENTITIES","LEAN_JOB_ID","LEAN_TASK_ID","LEAN_TASK_DIM_ENTITIES"}, new int[]{20*256,20*256,30*256,20*256,20*256,30*256}, 20);
					//int sheetNum = ExportJob.EXPORT_EXCEL_SHEET_NAME_START_NUM;
					for (TaskSpecialLean taskSpecialLean : taskSpecialLeans) {
						if(tRowNum > ExcelUtils.excelMaxRow){
							//tSheet = workbook.createSheet("WW_TASK_SPECIAL_LEAN"+sheetNum);
							//ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","TASK_DIM_ENTITIES","LEAN_JOB_ID","LEAN_TASK_ID","LEAN_TASK_DIM_ENTITIES"}, new int[]{20*256,20*256,30*256,20*256,20*256,30*256}, 20);
							//tRowNum = 1;
							//sheetNum++;
							break;
						}
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,taskSpecialLean.getJobId(),taskSpecialLean.getTaskId(),taskSpecialLean.getExp(),taskSpecialLean.getLeanJobId(),taskSpecialLean.getBeTaskId(),taskSpecialLean.getBeExp());
					}
				}
				//传入参数 and 任务项传入参数
				List<Parameter> parameters = exportJob.getParameterList();
				if (null != parameters && parameters.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_PARAMETER");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","SOURCE_ID","NAME","VALUE","REQUIRED"},new int[]{20*256,20*256,30*256,60*256,20*256}, 20);
					//int sheetNum = ExportJob.EXPORT_EXCEL_SHEET_NAME_START_NUM;
					for (Parameter parameter : parameters) {
						if(tRowNum > ExcelUtils.excelMaxRow){
							//tSheet = workbook.createSheet("WW_PARAMETER"+sheetNum);
							//ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","SOURCE_ID","NAME","VALUE","REQUIRED"},new int[]{20*256,20*256,30*256,60*256,20*256}, 20);
							//tRowNum = 1;
							//sheetNum++;
							break;
						}
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,parameter.getJobId(),parameter.getSourceId(),parameter.getName(),parameter.getValue(),String.valueOf(parameter.getdRequired()));
					}
				}
				//任务项
				List<Task> tasks = exportJob.getTaskList();
				if(null != tasks && tasks.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_TASK");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","TASK_NAME","TITLE","TASK_PLUGIN","PROGRAM_NAME","ALLOWED_RERUN","PERIOD","WORKING_DAY","ERROR_DELAY","ERROR_IGNORE","ERROR_MAX_NUM","USE_JOB_DIMENSION","AGENT_SCOPE","CREATE_TIME","CREATOR","POINT_X","POINT_Y"}, 
							new int[]{20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,30*256,20*256,20*256,20*256,20*256,20*256}, 20);
					//int sheetNum = ExportJob.EXPORT_EXCEL_SHEET_NAME_START_NUM;
					for (Task task : tasks) {
						if(tRowNum > ExcelUtils.excelMaxRow){
							//tSheet = workbook.createSheet("WW_TASK"+sheetNum);
							//ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","TASK_NAME","TITLE","TASK_PLUGIN","PROGRAM_NAME","ALLOWED_RERUN","PERIOD","WORKING_DAY","ERROR_DELAY","ERROR_IGNORE","ERROR_MAX_NUM","USE_JOB_DIMENSION","AGENT_SCOPE","CREATE_TIME","CREATOR","POINT_X","POINT_Y"}, 
							//		new int[]{20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,20*256,30*256,20*256,20*256,20*256,20*256,20*256}, 20);
							//tRowNum = 1;
							//sheetNum++;
							break;
						}
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,task.getJobId(),task.getTaskId(),task.getName(),task.getTitle(),task.getPlugin(),task.getProgramName(),String.valueOf(task.getAllowedRerun()),task.getPeriod(),task.getWorkingDay(),String.valueOf(task.getErrorDelay()),
								String.valueOf(task.getErrorIgnore()),String.valueOf(task.getMaxNumOfExeErrors()),task.getUseJobDimension(),task.getAgentScope(),task.getCreateTime(),task.getCreator(),String.valueOf(task.getPointX()),String.valueOf(task.getPointY()));
					}
				}
				//任务项依赖
				List<TaskLean> taskLeans = exportJob.getTaskLeanList();
				if (null != taskLeans && taskLeans.size() > 0) {
					tRowNum = 1;
					tSheet = workbook.createSheet("WW_TASK_LEAN");
					ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","LEAN_JOB_ID","LEAN_TASK_ID"},new int[]{20*256,20*256,20*256,20*256}, 20);
					//int sheetNum = ExportJob.EXPORT_EXCEL_SHEET_NAME_START_NUM;
					for (TaskLean taskLean : taskLeans) {
						if(tRowNum > ExcelUtils.excelMaxRow){
							//tSheet = workbook.createSheet("WW_TASK_LEAN"+sheetNum);
							//ExcelUtils.createTitle(workbook,tSheet,new String[]{"JOB_ID","TASK_ID","LEAN_JOB_ID","LEAN_TASK_ID"},new int[]{20*256,20*256,20*256,20*256}, 20);
							//tRowNum = 1;
							//sheetNum++;
							break;
						}
						tRowNum = ExcelUtils.createRow(tSheet,tRowNum,taskLean.getJobId(),taskLean.getTaskId(),taskLean.getLeanJobId(),taskLean.getLeanTaskId());
					}
				}
			}
			workbook.write(os);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(null != workbook) { try { workbook.close(); }catch(Exception e) {} };
			if(null != os) { try { os.close(); }catch(Exception e) {} };
		}
	}

	@Override
	public ImportJob getImportJob(String productId,MultipartFile multipartFile) throws Exception{
		ImportJob importJob = new ImportJob();
		Workbook tWorkbook = null;
		try{
	    	if (multipartFile.getOriginalFilename().lastIndexOf("xlsx") != -1) {
	    		tWorkbook = new XSSFWorkbook(multipartFile.getInputStream());
			}else if (multipartFile.getOriginalFilename().lastIndexOf("xls") != -1) {
				tWorkbook = new HSSFWorkbook(multipartFile.getInputStream());
			}
	    	Map<String, Object> queryMap = new HashMap<>();
    		queryMap.put("pId", productId);
	    	if((null == productId || productId.equals("")) && null == tWorkbook.getSheet("WW_PRODUCT")) {
	    		throw new Exception("WW_PRODUCT: 导入条件中产品和导入文件中产品，两个值不能同时为空");
	    	}
	    	//WW_PRODUCT
	    	if (null != tWorkbook.getSheet("WW_PRODUCT")) {
	    		Sheet tSheet = tWorkbook.getSheet("WW_PRODUCT");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			Product product = new Product();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_PRODUCT", (rowNum+1), "P_ID", true, colValue, 50);
    				product.setpId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT", (rowNum+1), "P_NAME", true, colValue, 500);
    				product.setpName(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT", (rowNum+1), "P_DESC", false, colValue, 500);
    				product.setpDesc(colValue);
    				if(null != importJob.getProduct()) {
    					throw new Exception("WW_PRODUCT: 每次只能导入一个产品");
    				}
    				if((null == productId || productId.equals("")) || productId.equals(product.getpId())) {
    					importJob.setProduct(product);
	    				productId = product.getpId();
    				}else {
    					throw new Exception("WW_PRODUCT: 导入条件中产品和导入文件中产品，两个值一定要相同");
    				}
	    		}
	    		if(null == importJob.getProduct()) {
	    			throw new Exception("WW_PRODUCT: 无数据");
	    		}
				List<Product> products = productMapper.selectProductByName(queryMap);
				if (null != products && products.size() > 0) {
					importJob.getProduct().setImportType(ImportJob.IMPORT_TYPE_UPDATE);
				}else {
					importJob.getProduct().setImportType(ImportJob.IMPORT_TYPE_INSERT);
				}
			}
	    	//WW_PRODUCT_CERTIFICATION
	    	if (null != tWorkbook.getSheet("WW_PRODUCT_CERTIFICATION")) {
	    		Map<String,Object> tabProductCertificationMap = new HashMap<>(); //存表中的认证信息
	    		queryMap = new HashMap<>();
	    		queryMap.put("pId", productId);
		    	List<ProductCertification> productCertifications = productMapper.queryProductCertification(queryMap);
	    		if (null != productCertifications && productCertifications.size() > 0) {
	    			for (ProductCertification productCertification : productCertifications) {
	    				tabProductCertificationMap.put(productCertification.getAuthId(), "");
	    			}
	    		}
	    		Map<String,Object> fileProductCertificationMap = new HashMap<>(); //存文件中的认证信息
	    		Sheet tSheet = tWorkbook.getSheet("WW_PRODUCT_CERTIFICATION");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			ProductCertification productCertification = new ProductCertification();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_PRODUCT_CERTIFICATION", (rowNum+1), "P_ID", true, colValue, 50);
	    			if (!productId.equals(colValue)) {
	    				throw new Exception("WW_PRODUCT_CERTIFICATION: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			productCertification.setpId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT_CERTIFICATION", (rowNum+1), "AUTH_ID", true, colValue, 50);
    				if(null != fileProductCertificationMap.get(colValue)) {
    					throw new Exception("WW_PRODUCT_CERTIFICATION: 第"+(rowNum+1)+"行,存在重复的认证编号"+colValue);
    				}
    				fileProductCertificationMap.put(colValue, "");
    				if(null != tabProductCertificationMap.get(colValue)) {
    					productCertification.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
    				}else {
    					productCertification.setImportType(ImportJob.IMPORT_TYPE_INSERT);
    				}
    				productCertification.setAuthId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT_CERTIFICATION", (rowNum+1), "PROTOCAL", true, colValue, 50);
    				productCertification.setProtocal(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT_CERTIFICATION", (rowNum+1), "USER_ID", false, colValue, 20);
    				productCertification.setUserId(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_PRODUCT_CERTIFICATION", (rowNum+1), "PUBLIC_KEY", false, colValue, 2000);
    				productCertification.setPublicKey(colValue);
    				importJob.getProductCertificationList().add(productCertification);
	    		}
			}
	    	//WW_DIMENSION
	    	if (null != tWorkbook.getSheet("WW_DIMENSION")) {
	    		Map<String, Object> tabDimensionNameMap = new HashMap<>(); //存表中的维度名称
	    		queryMap = new HashMap<>();
	    		queryMap.put("product", productId);
	    		List<Dimension> dimensionList = dimensionMapper.queryDimensionPage(queryMap);
	    		if (null != dimensionList && dimensionList.size() > 0) {
	    			for (Dimension fDimension : dimensionList) {
	    				tabDimensionNameMap.put(fDimension.getName(),"");
	    			}
	    		}
	    		Map<String,Object> fileDimensionNameMap = new HashMap<>(); //存文件中的维度名称
	    		Sheet tSheet = tWorkbook.getSheet("WW_DIMENSION");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			Dimension dimension = new Dimension();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_DIMENSION", (rowNum+1), "PRODUCT", true, colValue, 50);
	    			if (!productId.equals(colValue)) {
	    				throw new Exception("WW_DIMENSION: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			dimension.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION", (rowNum+1), "DMSN_NAME", true, colValue, 50);
    				if(null != fileDimensionNameMap.get(colValue)) {
    					throw new Exception("WW_DIMENSION: 第"+(rowNum+1)+"行,存在重复的维度名称"+colValue);
    				}
    				fileDimensionNameMap.put(colValue, "");
    				if (null != tabDimensionNameMap.get(colValue)) { 
    					dimension.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						dimension.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				dimension.setName(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION", (rowNum+1), "DMSN_DESC", false, colValue, 200);
    				dimension.setDescription(colValue);
    				importJob.getDimensionList().add(dimension);
	    		}
			}
	    	//WW_DIMENSION_ENTITY
	    	if (null != tWorkbook.getSheet("WW_DIMENSION_ENTITY")) {
	    		Map<String,Object> tabDimensionEntityMap = new HashMap<>(); //存表中的维度实体
	    		queryMap = new HashMap<>();
	    		queryMap.put("product", productId);
	    		List<DimensionEntity> dimensionEntityList = dimensionEntityMapper.queryDimensionEntityPage(queryMap);
	    		if (null != dimensionEntityList && dimensionEntityList.size() > 0) {
					for (DimensionEntity fDimensionEntity : dimensionEntityList) {
						tabDimensionEntityMap.put(fDimensionEntity.getName()+"###"+fDimensionEntity.getEntity(), "");
					}
				}
	    		Map<String,Object> fileDimensionEntityMap = new HashMap<>(); //存文件中的维度实体
	    		Sheet tSheet = tWorkbook.getSheet("WW_DIMENSION_ENTITY");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装维度实体
	    			DimensionEntity dimensionEntity = new DimensionEntity();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_DIMENSION_ENTITY", (rowNum+1), "PRODUCT", true, colValue, 50);
	    			if (!productId.equals(colValue)) {
	    				throw new Exception("WW_DIMENSION_ENTITY: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			dimensionEntity.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION_ENTITY", (rowNum+1), "DMSN_NAME", true, colValue, 50);
    				dimensionEntity.setName(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION_ENTITY", (rowNum+1), "ENTITY_NAME", true, colValue, 50);
    				if(null != fileDimensionEntityMap.get(dimensionEntity.getName()+"###"+colValue)) {
    					throw new Exception("WW_DIMENSION_ENTITY: 第"+(rowNum+1)+"行,存在重复的维度实体"+dimensionEntity.getName()+"###"+colValue);
    				}
    				fileDimensionEntityMap.put(dimensionEntity.getName()+"###"+colValue, "");
    				if(null != tabDimensionEntityMap.get(dimensionEntity.getName()+"###"+colValue)) {
    					dimensionEntity.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
    				}else {
    					dimensionEntity.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				dimensionEntity.setEntity(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION_ENTITY", (rowNum+1), "ENTITY_DESC", false, colValue, 200);
    				dimensionEntity.setEntityDesc(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_DIMENSION_ENTITY", (rowNum+1), "TITLE", false, colValue, 500);
    				dimensionEntity.setTags(colValue);
    				importJob.getDimensionEntityList().add(dimensionEntity);
	    		}
			}
	    	//WW_SERVER
	    	if (null != tWorkbook.getSheet("WW_SERVER")) {
	    		Map<String, Object> tabServerIdMap = new HashMap<>(); //存表中的节点编号
	    		queryMap = new HashMap<>();
	    		queryMap.put("product", productId);
	    		List<Server> serverList = serverMapper.queryServer(queryMap);
	    		if (null != serverList && serverList.size() > 0) {
	    			for (Server fServer : serverList) {
	    				tabServerIdMap.put(fServer.getServerId(), "");
	    			}
	    		}
	    		Map<String, Object> fileServerIdMap = new HashMap<>(); //存文件中的节点编号
	    		Sheet tSheet = tWorkbook.getSheet("WW_SERVER");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装服务
	    			Server server = new Server();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "PRODUCT", true, colValue, 50);
	    			if (!productId.equals(colValue)) {
	    				throw new Exception("WW_SERVER: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			server.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "SERVER_ID", true, colValue, 50);
    				if(null != fileServerIdMap.get(colValue)) {
    					throw new Exception("WW_SERVER: 第"+(rowNum+1)+"行,存在重复的节点编号"+colValue);
    				}
    				fileServerIdMap.put(colValue, "");
    				if (null != tabServerIdMap.get(colValue)) {
						continue;
    					//server.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						server.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				server.setServerId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "SERVER_DESC", false, colValue, 500);
    				server.setServerDesc(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "IP_ADDR", true, colValue, 100);
    				server.setIpAddr(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "SSH_PORT", true, colValue, 11);
    				server.setSshPort(StringUtil.getIntValue("WW_SERVER", (rowNum+1), "SSH_PORT",colValue));
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "RMI_REGISTY_PORT", true, colValue, 11);
    				server.setRmiRegistryPort(StringUtil.getIntValue("WW_SERVER", (rowNum+1), "RMI_REGISTY_PORT",colValue));
    				colValue = getColValue(tRow.getCell(6));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "RMI_SERVER_PORT", true, colValue, 11);
    				server.setRmiServerPort(StringUtil.getIntValue("WW_SERVER", (rowNum+1), "RMI_SERVER_PORT",colValue));
    				colValue = getColValue(tRow.getCell(7));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "SERVER_TYPE", true, colValue, 10);
    				server.setServerType(colValue);
    				colValue = getColValue(tRow.getCell(8));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "TAGS", false, colValue, 500);
    				server.setTags(colValue);
    				colValue = getColValue(tRow.getCell(9));
    				server.setdLatestOnlineTime(StringUtil.getDateValue("WW_SERVER",(rowNum+1),"LATEST_ONLINE_TIME",colValue));
    				colValue = getColValue(tRow.getCell(10));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "OS_SSH_PORT", true, colValue, 11);
    				server.setOsSshPort(StringUtil.getIntValue("WW_SERVER", (rowNum+1), "OS_SSH_PORT",colValue));
    				colValue = getColValue(tRow.getCell(11));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "OS_SSH_USER", true, colValue, 100);
    				server.setOsSshUser(colValue);
    				colValue = getColValue(tRow.getCell(12));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "OS_SSH_PSWD", true, colValue, 100);
    				server.setOsSshPswd(colValue);
    				colValue = getColValue(tRow.getCell(13));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "ZOO_CONNECT_STR", true, colValue, 200);
    				server.setZooConnectStr(colValue);
    				colValue = getColValue(tRow.getCell(14));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "ZOO_NAMESPACE", true, colValue, 200);
    				server.setZooNamespace(colValue);
    				colValue = getColValue(tRow.getCell(15));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "HOME_DIR", true, colValue, 200);
    				server.setHomeDir(colValue);
    				colValue = getColValue(tRow.getCell(16));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "IGNORE_PLUGINS", false, colValue, 500);
    				server.setIgnorePlugins(colValue);
    				colValue = getColValue(tRow.getCell(17));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "STATUS", true, colValue, 11);
    				server.setStatus(StringUtil.getIntValue("WW_SERVER", (rowNum+1), "STATUS",colValue));
    				colValue = getColValue(tRow.getCell(18));
    				StringUtil.validateTabColNullAndLength("WW_SERVER", (rowNum+1), "VERSION_NO", false, colValue, 100);
    				server.setVersionNo(colValue);;
    				importJob.getServerList().add(server);
	    		}
			}
	    	//WW_DS
	    	if (null != tWorkbook.getSheet("WW_DS")) {
	    		Map<String, Object> tabDsNameMap = new HashMap<>(); //存表中的数据源名称
	    		queryMap = new HashMap<>();
	    		queryMap.put("nullAndProduct", productId);
	    		List<DataSource> dataSourceList = dataSourceMapper.queryDataSourcePage(queryMap);
	    		if (null != dataSourceList && dataSourceList.size() > 0) {
	    			for (DataSource fDataSource : dataSourceList) {
	    				tabDsNameMap.put(fDataSource.getName(),"");
	    			}
	    		}
	    		Map<String, Object> fileDsNameMap = new HashMap<>(); //存文件中的数据源名称
	    		Sheet tSheet = tWorkbook.getSheet("WW_DS");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			DataSource dataSource = new DataSource();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "PRODUCT", false, colValue, 50);
	    			if (null != colValue && !colValue.equals("") && !productId.equals(colValue)) {
	    				throw new Exception("WW_DS: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			dataSource.setProduct(colValue);
	    			colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "AGENT", false, colValue, 50);
    				dataSource.setAgent(colValue);
	    			colValue = getColValue(tRow.getCell(2));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "NAME", true, colValue, 50);
	    			if(null != fileDsNameMap.get(colValue)) {
    					throw new Exception("WW_DS: 第"+(rowNum+1)+"行,存在重复的数据源名称"+colValue);
    				}
	    			fileDsNameMap.put(colValue, "");
    				if (null != tabDsNameMap.get(colValue)) {
    					dataSource.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						dataSource.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				dataSource.setName(colValue);
	    			colValue = getColValue(tRow.getCell(3));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "DRIVER_NAME", true, colValue, 200);
	    			dataSource.setDriverName(colValue);
	    			colValue = getColValue(tRow.getCell(4));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "DRIVER_CLASS", true, colValue, 200);
	    			dataSource.setDriverClass(colValue);
	    			colValue = getColValue(tRow.getCell(5));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "DATABASE_NAME", false, colValue, 50);
	    			dataSource.setDatabaseName(colValue);
	    			colValue = getColValue(tRow.getCell(6));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "DATABASE_TYPE", true, colValue, 200);
	    			dataSource.setDatabaseType(colValue);
	    			colValue = getColValue(tRow.getCell(7));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "JDBC_URL", true, colValue, 500);
	    			dataSource.setJdbcUrl(colValue);
	    			colValue = getColValue(tRow.getCell(8));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "JDBC_USER", true, colValue, 50);
	    			dataSource.setJdbcUser(colValue);
	    			colValue = getColValue(tRow.getCell(9));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "JDBC_PASSWORD", true, colValue, 50);
	    			dataSource.setJdbcPassword(colValue);
    				colValue = getColValue(tRow.getCell(10));
    				StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "AUTO_START", true, colValue, 11);
    				dataSource.setAutoStart(StringUtil.getIntValue("WW_DS", (rowNum+1), "AUTO_START",colValue));
    				colValue = getColValue(tRow.getCell(11));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "POOL", true, colValue, 45);
	    			dataSource.setPool(colValue);
	    			colValue = getColValue(tRow.getCell(12));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "XA", true, colValue, 45);
	    			dataSource.setXa(colValue);
	    			colValue = getColValue(tRow.getCell(13));
	    			StringUtil.validateTabColNullAndLength("WW_DS", (rowNum+1), "POOL_PROPERTIES", false, colValue, 500);
	    			dataSource.setPoolProperties(colValue);
    				importJob.getDataSourceList().add(dataSource);
	    		}
	    	}
	    	//WW_PLUGIN
	    	Map<String, Object> tabPluginNameMap = new HashMap<>(); //存表中的插件名称
    		List<Plugin> plugins = pluginMapper.queryPluList();
    		if (null != plugins && plugins.size() > 0) {
    			for (Plugin plugin : plugins) {
    				tabPluginNameMap.put(plugin.getName(),"");
    			}
    		}
    		Map<String, Object> filePluginNameMap = new HashMap<>(); //存文件中的插件名称
	    	if (null != tWorkbook.getSheet("WW_PLUGIN")) {
	    		Sheet tSheet = tWorkbook.getSheet("WW_PLUGIN");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装插件
	    			Plugin plugin = new Plugin();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_PLUGIN", (rowNum+1), "NAME", true, colValue, 50);
	    			if(null != filePluginNameMap.get(colValue)) {
    					throw new Exception("WW_PLUGIN: 第"+(rowNum+1)+"行,存在重复的插件名称"+colValue);
    				}
	    			filePluginNameMap.put(plugin.getName(), plugin.getName());
	    			if (null != tabPluginNameMap.get(colValue)) { //判断维度是否存在,存在则跳过
	    				plugin.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						plugin.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
	    			plugin.setName(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN", (rowNum+1), "PLUGIN_DESC", false, colValue, 500);
    				plugin.setDesc(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN", (rowNum+1), "FEATURE_NAME", false, colValue, 500);
    				plugin.setFeature(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN", (rowNum+1), "VERSION", false, colValue, 20);
    				plugin.setVersion(colValue);
    				importJob.getPluginList().add(plugin);
	    		}
			}
	    	//WW_PLUGIN_PARAMETER
	    	if (null != tWorkbook.getSheet("WW_PLUGIN_PARAMETER")) {
	    		Map<String,Object> tabPluginParameterMap = new HashMap<>(); //存表中的插件名称+插件参数名称
	    		List<PluginParameter> pluginParameterList = pluginMapper.queryPluginParameter(new HashMap<>());
	    		if (null != pluginParameterList && pluginParameterList.size() > 0) {
	    			for (PluginParameter fPluginParameter : pluginParameterList) {
	    				tabPluginParameterMap.put(fPluginParameter.getPluginName()+"###"+fPluginParameter.getParamName(), "");
					}
				}
	    		Map<String,Object> filePluginParameterMap = new HashMap<>(); //存文件中的插件名称+插件参数名称
	    		Sheet tSheet = tWorkbook.getSheet("WW_PLUGIN_PARAMETER");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装插件参数
	    			PluginParameter pluginParameter = new PluginParameter();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "PLUGIN_NAME", true, colValue, 50);
	    			pluginParameter.setPluginName(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "PARAM_NAME", true, colValue, 200);
    				if(null != filePluginParameterMap.get(pluginParameter.getPluginName()+"###"+colValue)) {
    					throw new Exception("WW_PLUGIN_PARAMETER: 第"+(rowNum+1)+"行,存在重复的插件参数名称"+pluginParameter.getPluginName()+"/"+colValue);
    				}
    				filePluginParameterMap.put(pluginParameter.getPluginName()+"###"+colValue, "");
    				if(null != tabPluginParameterMap.get(pluginParameter.getPluginName()+"###"+colValue)) {
    					pluginParameter.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
    				}else {
    					pluginParameter.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				pluginParameter.setParamName(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "DATA_TYPE", true, colValue, 50);
    				pluginParameter.setDataType(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "PARAM_DEF_VALUE", false, colValue, 200);
    				pluginParameter.setParamDefValue(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "IS_REQUIRED", false, colValue, 2);
    				pluginParameter.setIsRequired(StringUtil.getIntValue("WW_PLUGIN_PARAMETER", (rowNum+1), "IS_REQUIRED",colValue));
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "PARAM_DESC", false, colValue, 500);
    				pluginParameter.setParamDesc(colValue);
    				colValue = getColValue(tRow.getCell(6));
    				StringUtil.validateTabColNullAndLength("WW_PLUGIN_PARAMETER", (rowNum+1), "DATA_RANGE", false, colValue, 1000);
    				pluginParameter.setDataRange(colValue);
    				importJob.getPluginParameterList().add(pluginParameter);
	    		}
			}
	    	//WW_CALENDAR
	    	if (null != tWorkbook.getSheet("WW_CALENDAR")) {
	    		Map<String, Object> tabCalendarNameMap = new HashMap<>(); //存表中的假日名称
	    		queryMap = new HashMap<>();
	    		queryMap.put("product", productId);
	    		List<Calendar> calendarList = calendarMapper.queryCalendarList(queryMap);
	    		if (null != calendarList && calendarList.size() > 0) {
	    			for (Calendar fCalendar : calendarList) {
	    				tabCalendarNameMap.put(fCalendar.getCalName(),"");
	    			}
	    		}
	    		Map<String, Object> fileCalendarNameMap = new HashMap<>(); //存文件中的假日名称
	    		Sheet tSheet = tWorkbook.getSheet("WW_CALENDAR");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			Calendar calendar = new Calendar();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_CALENDAR", (rowNum+1), "PRODUCT", true, colValue, 50);
	    			if (!productId.equals(colValue)) {
	    				throw new Exception("WW_CALENDAR: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			calendar.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_CALENDAR", (rowNum+1), "CAL_NAME", true, colValue, 50);
    				if(null != fileCalendarNameMap.get(colValue)) {
    					throw new Exception("WW_CALENDAR: 第"+(rowNum+1)+"行,存在重复的假日名称"+colValue);
    				}
    				fileCalendarNameMap.put(colValue, "");
    				if (null != tabCalendarNameMap.get(colValue)) {
    					calendar.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						calendar.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
    				calendar.setCalName(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_CALENDAR", (rowNum+1), "CAL_DESCRIPTION", false, colValue, 200);
    				calendar.setCalDescription(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_CALENDAR", (rowNum+1), "INCLUDE_VALUE", false, colValue, 4000);
    				calendar.setIncludeValue(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_CALENDAR", (rowNum+1), "EXCLUSION_VALUE", false, colValue, 4000);
    				calendar.setExclusionValue(colValue);
    				importJob.getCalendarList().add(calendar);
	    		}
			}
	    	//WW_CRON
	    	Map<String,Object> importCronIdMap = new HashMap<>();
	    	if (null != tWorkbook.getSheet("WW_CRON")) {
	    		Map<String, Object> tabCronJobIdMap = new HashMap<>(); //存表中的计划任务编号
	    		queryMap = new HashMap<>();
	    		queryMap.put("nullOrProduct", productId);
	    		List<Cron> cronList = cronMapper.kvspage(queryMap);
	    		if (null != cronList && cronList.size() > 0) {
	    			for (Cron fCron : cronList) {
	    				tabCronJobIdMap.put(fCron.getJobId(),"");
	    			}
	    		}
	    		Map<String, Object> fileCronJobIdMap = new HashMap<>(); //存文件中的计划任务编号
	    		Sheet tSheet = tWorkbook.getSheet("WW_CRON");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			Cron cron = new Cron();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if(null != fileCronJobIdMap.get(colValue)) {
    					throw new Exception("WW_CALENDAR: 第"+(rowNum+1)+"行,存在重复的计划任务编号"+colValue);
    				}
	    			fileCronJobIdMap.put(colValue, "");
    				if (null != tabCronJobIdMap.get(colValue)) {
    					cron.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						cron.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
	    			cron.setJobId(colValue);
	    			colValue = getColValue(tRow.getCell(1));
	    			StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "MODEL", true, colValue, 20);
	    			cron.setModel(colValue);
	    			colValue = getColValue(tRow.getCell(2));
	    			StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "PRODUCT", false, colValue, 50);
	    			if (null != colValue && !colValue.equals("") && !productId.equals(colValue)) {
	    				throw new Exception("WW_CRON: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
	    			cron.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "AGENT", false, colValue, 50);
    				cron.setAgent(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "JOB_NAME", true, colValue, 200);
    				cron.setJobName(colValue);
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "CRON_EXPRESSION", true, colValue, 100);
    				cron.setCronExpression(colValue);
    				colValue = getColValue(tRow.getCell(6));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "PLUGIN", true, colValue, 200);
    				cron.setPlugin(colValue);
    				colValue = getColValue(tRow.getCell(7));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "PROGRAM_NAME", true, colValue, 200);
    				cron.setProgramName(colValue);
    				colValue = getColValue(tRow.getCell(8));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "AUTO_START", true, colValue, 11);
    				cron.setAutoStart(StringUtil.getIntValue("WW_CRON", (rowNum+1), "AUTO_START",colValue));
    				colValue = getColValue(tRow.getCell(9));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "CREATE_TIME", false, colValue, 50);
    				cron.setCreateTime(colValue);
    				colValue = getColValue(tRow.getCell(10));
    				StringUtil.validateTabColNullAndLength("WW_CRON", (rowNum+1), "CREATOR", false, colValue, 50);
    				cron.setCreator(colValue);
    				importJob.getCronList().add(cron);
    				importCronIdMap.put(cron.getJobId(), "");
	    		}
	    	}
	    	/*//WW_CRON_SHARED
			if (null != tWorkbook.getSheet("WW_CRON_SHARED")) {
				Map<String, Object> fileCronSharedAgentJobIdMap = new HashMap<>(); //存文件中的计划任务共享参数
				Sheet tSheet = tWorkbook.getSheet("WW_CRON_SHARED");
				for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
					Row tRow = tSheet.getRow(rowNum);
					if (null == tRow) {
						continue;
					}
					CronShared cronShared = new CronShared();
					String colValue = getColValue(tRow.getCell(0));
					StringUtil.validateTabColNullAndLength("WW_CRON_SHARED", (rowNum+1), "PRODUCT", true, colValue, 50);
					if(!productId.equals(colValue)) {
						throw new Exception("WW_CRON_SHARED: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
					cronShared.setProduct(colValue);
					colValue = getColValue(tRow.getCell(1));
					StringUtil.validateTabColNullAndLength("WW_CRON_SHARED", (rowNum+1), "AGENT", true, colValue, 50);
					cronShared.setAgent(colValue);
					colValue = getColValue(tRow.getCell(2));
					StringUtil.validateTabColNullAndLength("WW_CRON_SHARED", (rowNum+1), "JOB_ID", true, colValue, 50);
					if(null != fileCronSharedAgentJobIdMap.get(cronShared.getAgent()+"###"+colValue)) {
						throw new Exception("WW_CRON_SHARED: 第"+(rowNum+1)+"行,存在重复的计划任务参数"+cronShared.getAgent()+"/"+colValue);
					}
					fileCronSharedAgentJobIdMap.put(cronShared.getAgent()+"###"+colValue, "");
					cronShared.setJobId(colValue);
					colValue = getColValue(tRow.getCell(3));
					StringUtil.validateTabColNullAndLength("WW_CRON_SHARED", (rowNum+1), "SHARED_JSON", false, colValue, 2000);
					cronShared.setSharedJson(colValue);
					colValue = getColValue(tRow.getCell(4));
					StringUtil.validateTabColNullAndLength("WW_CRON_SHARED", (rowNum+1), "OCCU_TIME", false, colValue, 30);
					cronShared.setOccuTime(colValue);
					importJob.getCronSharedList().add(cronShared);
				}
			}*/
    		//WW_JOB
    		Map<String,Map<String,TaskLean>> importJobIdMap = new HashMap<>(); //判断JOB闭环
    		if(null == tWorkbook.getSheet("WW_JOB")) {
    			throw new Exception("导入的文件中不存在SHEET(WW_JOB)");
    		}else{
	    		Map<String, Object> tabJobIdMap = new HashMap<>(); //存表中任务模板编号
	    		queryMap = new HashMap<>();
	    		queryMap.put("product", productId);
	    		List<Job> jobList = jobMapper.queryJobPage(queryMap);
	    		if (null != jobList && jobList.size() > 0) {
	    			for (Job fJob : jobList) {
	    				tabJobIdMap.put(fJob.getJobId(),"");
	    			}
	    		}
	    		Map<String, Object> fileJobIdMap = new HashMap<>(); //存文件中任务模板编号
	    		Sheet tSheet = tWorkbook.getSheet("WW_JOB");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装job
	    			Job job = new Job();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if(null != fileJobIdMap.get(job.getJobId())) {
    					throw new Exception("WW_JOB: 第"+(rowNum+1)+"行,存在重复的任务模板编号"+colValue);
    				}
	    			fileJobIdMap.put(job.getJobId(), "");
	    			if (null != tabJobIdMap.get(colValue)) { //判断job是否存在，存在则跳过
	    				job.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
					}else {
						job.setImportType(ImportJob.IMPORT_TYPE_INSERT);
					}
	    			job.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "PRODUCT", true, colValue, 50);
    				if (!productId.equals(colValue)) {
	    				throw new Exception("WW_JOB: 第"+(rowNum+1)+"行,产品号应是"+productId);
					}
    				job.setProduct(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "JOB_NAME", true, colValue, 200);
    				job.setName(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "JOB_MODE", true, colValue, 1);
    				job.setMode(StringUtil.getIntValue("WW_JOB", (rowNum+1), "JOB_MODE",colValue));
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "SCHEDULE_RID", false, colValue, 100);
    				job.setScheduleRid(colValue);
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "TITLE", false, colValue, 500);
    				job.setTitle(colValue);
    				colValue = getColValue(tRow.getCell(6));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "VERSION", false, colValue, 30);
    				job.setVersion(colValue);
    				colValue = getColValue(tRow.getCell(7));
    				job.setdCreateTime(StringUtil.getDateValue("WW_JOB", (rowNum+1), "CREATE_TIME", colValue));
    				colValue = getColValue(tRow.getCell(8));
    				StringUtil.validateTabColNullAndLength("WW_JOB", (rowNum+1), "CREATOR", false, colValue, 50);
    				job.setCreator(colValue);
    				importJob.getJobList().add(job);
    				importJobIdMap.put(job.getJobId(), new HashMap<>());
	    		}
	    		if(importJobIdMap.size() == 0) {
	    			throw new Exception("任务模板（WW_JOB）不能为空");
	    		}
			}
	    	if (null != tWorkbook.getSheet("WW_JOB_DIMENSION")) {
	    		Map<String, Object> fileJobDimensionKeyMap = new HashMap<>(); //存文件中JOB维度
	    		//Map<String, List<String>> fileJobDimensionModelNoMap = new HashMap<>(); //MODEL_NO相同 <= 2个维度 todo...
	    		Sheet tSheet = tWorkbook.getSheet("WW_JOB_DIMENSION");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			JobDimension  jobDimension = new JobDimension();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_JOB_DIMENSION", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if (null == importJobIdMap.get(colValue)) {
	    				throw new Exception("WW_JOB_DIMENSION: 第"+(rowNum+1)+"行,任务模板中不存在任务模板编号"+colValue);
					}
	    			jobDimension.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_JOB_DIMENSION", (rowNum+1), "MODEL_NO", true, colValue, 50);
    				jobDimension.setModelNo(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_JOB_DIMENSION", (rowNum+1), "DMSN_NAME", true, colValue, 50);
    				jobDimension.setDmsnName(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_JOB_DIMENSION", (rowNum+1), "DMSN_PRODUCT", true, colValue, 50);
    				if(!productId.equals(colValue)) {
    					throw new Exception("WW_JOB_DIMENSION: 第"+(rowNum+1)+"行,产品号应是"+productId);
    				}
    				jobDimension.setDmsnProduct(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_JOB_DIMENSION", (rowNum+1), "ENTITY_NAME", true, colValue, 50);
    				jobDimension.setEntityName(colValue);
    				String key = jobDimension.getJobId()+"###"+jobDimension.getModelNo()+"###"+jobDimension.getDmsnName()+"###"+jobDimension.getEntityName();
    				if(null != fileJobDimensionKeyMap.get(key)) {
    					throw new Exception("WW_JOB_DIMENSION: 第"+(rowNum+1)+"行,存在重复的JOB维度"+jobDimension.getJobId()+"/"+jobDimension.getModelNo()+"/"+jobDimension.getDmsnName()+"/"+jobDimension.getEntityName());
    				}
    				fileJobDimensionKeyMap.put(key, "");
    				importJob.getJobDimensionList().add(jobDimension);
	    		}
			}
	    	if (null != tWorkbook.getSheet("WW_TASK_SPECIAL_LEAN")) {
	    		Map<String, Object> fileTaskSpecialLeanKeyMap = new HashMap<>(); //存文件中TASK特殊依赖
	    		Sheet tSheet = tWorkbook.getSheet("WW_TASK_SPECIAL_LEAN");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装特殊依赖关系
	    			TaskSpecialLean taskSpecialLean = new TaskSpecialLean();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if (null == importJobIdMap.get(colValue)) {
	    				throw new Exception("WW_TASK_SPECIAL_LEAN: 第"+(rowNum+1)+"行,任务模板中不存在任务模板编号"+colValue);
					}
	    			taskSpecialLean.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "TASK_ID", true, colValue, 50);
    				//todo...
					/*if (null == taskMap.get(taskSpecialLean.getJobId()+"###"+colValue)) {
						throw new Exception("WW_TASK_SPECIAL_LEAN: 第"+(rowNum+1)+"行,"+taskSpecialLean.getJobId()+"+"+colValue+"的任务项不存在");
					}*/
    				taskSpecialLean.setTaskId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "TASK_DIM_ENTITIES", false, colValue, 500);
    				taskSpecialLean.setExp(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "LEAN_JOB_ID", false, colValue, 50);
    				taskSpecialLean.setLeanJobId(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "LEAN_TASK_ID", true, colValue, 50);
    				//todo...
					/*if (null == taskIdMap.get(colValue)) {
						throw new Exception("WW_TASK_SPECIAL_LEAN: 第"+(rowNum+1)+"行,LEAN_TASK_ID为"+colValue+"的任务项不存在");
					}*/
    				taskSpecialLean.setBeTaskId(colValue);
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_TASK_SPECIAL_LEAN", (rowNum+1), "LEAN_TASK_DIM_ENTITIES", false, colValue, 500);
    				taskSpecialLean.setBeExp(colValue);
    				String key = taskSpecialLean.getJobId()+"###"+taskSpecialLean.getTaskId()+"###"+taskSpecialLean.getExp()+"###"+taskSpecialLean.getLeanJobId()+"###"+taskSpecialLean.getBeTaskId()+"###"+taskSpecialLean.getBeExp();
    				if(null != fileTaskSpecialLeanKeyMap.get(key)) {
    					throw new Exception("WW_TASK_SPECIAL_LEAN: 第"+(rowNum+1)+"行,存在重复的TASK特殊依赖"+taskSpecialLean.getJobId()+"/"+taskSpecialLean.getTaskId()+"/"+taskSpecialLean.getExp()+"/"+taskSpecialLean.getLeanJobId()+"/"+taskSpecialLean.getBeTaskId()+"/"+taskSpecialLean.getBeExp());
    				}
    				fileTaskSpecialLeanKeyMap.put(key, "");
    				importJob.getTaskSpecialLeanList().add(taskSpecialLean);
    				//判断JOB闭环
    				if((null == taskSpecialLean.getLeanJobId() || taskSpecialLean.getLeanJobId().equals("")) || taskSpecialLean.getJobId().equals(taskSpecialLean.getLeanJobId())) {
	    				Map<String,TaskLean> tempTaskLeanMap = importJobIdMap.get(taskSpecialLean.getJobId());
	    				String tempTaskLeanKey = taskSpecialLean.getJobId()+"###"+taskSpecialLean.getTaskId()+"###"+taskSpecialLean.getBeTaskId();
	    				if(null == tempTaskLeanMap.get(tempTaskLeanKey)) {
	    					TaskLean taskLean = new TaskLean();
	    					taskLean.setJobId(taskSpecialLean.getJobId());
	    					taskLean.setTaskId(taskSpecialLean.getTaskId());
	    					taskLean.setLeanTaskId(taskSpecialLean.getBeTaskId());
	    					tempTaskLeanMap.put(tempTaskLeanKey, taskLean);
	    					importJobIdMap.put(taskLean.getJobId(), tempTaskLeanMap);
	    				}
    				}
	    		}
			}
	    	if (null != tWorkbook.getSheet("WW_PARAMETER")) {
	    		Map<String, Object> tabCronParameterKeyMap = new HashMap<>(); //存表中参数主键
	    		if(null != importCronIdMap && importCronIdMap.size() > 0) {
	    			String cronIdArr [] = new String[importCronIdMap.size()];
	    			int l=0;
	    			for(Map.Entry<String, Object> fMap : importCronIdMap.entrySet()) {
	    				cronIdArr[l] = fMap.getKey();
	    				l++;
	    			}
	    			queryMap = new HashMap<>();
		    		queryMap.put("jobIdArr", cronIdArr);
		    		queryMap.put("jobIdEqSourceId","jobIdEqSourceId");
		    		List<Parameter> parameterList = parameterMapper.queryParameterPage(queryMap);
		    		if (null != parameterList && parameterList.size() > 0) {
		    			for (Parameter fParameter : parameterList) {
		    				tabCronParameterKeyMap.put(fParameter.getJobId()+"###"+fParameter.getName(),"");
		    			}
		    		}
	    		}
	    		Map<String,Object> fileParameterKeyMap = new HashMap<>(); //存文件中参数主键
	    		Sheet tSheet = tWorkbook.getSheet("WW_PARAMETER");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装参数
	    			Parameter  parameter = new Parameter();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_PARAMETER", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if (null == importCronIdMap.get(colValue) && null == importJobIdMap.get(colValue)) {
	    				throw new Exception("WW_PARAMETER: 第"+(rowNum+1)+"行,计划任务和任务模板中都不存在"+colValue);
					}
	    			parameter.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_PARAMETER", (rowNum+1), "SOURCE_ID", true, colValue, 50);
    				parameter.setSourceId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_PARAMETER", (rowNum+1), "NAME", true, colValue, 200);
    				String key = parameter.getJobId()+"###"+parameter.getSourceId()+"###"+colValue;
    				if(null != fileParameterKeyMap.get(key)) {
    					throw new Exception("WW_PARAMETER: 第"+(rowNum+1)+"行,存在重复的参数"+parameter.getJobId()+"/"+parameter.getSourceId()+"/"+colValue);
    				}
    				fileParameterKeyMap.put(key, "");
    				parameter.setName(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_PARAMETER", (rowNum+1), "VALUE", false, colValue, 2000);
    				parameter.setValue(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_PARAMETER", (rowNum+1), "REQUIRED", false, colValue, 11);
    				parameter.setdRequired(StringUtil.getIntValue("WW_PARAMETER", (rowNum+1), "REQUIRED",colValue));
    				if(null != parameter.getJobId() && null != parameter.getSourceId() && parameter.getJobId().equals(parameter.getSourceId()) && null != importCronIdMap.get(parameter.getJobId())) {//CRON参数
    					if(null != tabCronParameterKeyMap.get(parameter.getJobId()+"###"+parameter.getName())) {
    						parameter.setImportType(ImportJob.IMPORT_TYPE_UPDATE);
    					}else {
    						parameter.setImportType(ImportJob.IMPORT_TYPE_INSERT);
    					}
	    			}else {//JOB参数
	    				parameter.setImportType(ImportJob.IMPORT_TYPE_INSERT);
	    			}
    				importJob.getParameterList().add(parameter);
	    		}
			}
	    	Map<String,List<Task>> fileTaskJobIdMap = new HashMap<>(); //存文件中JOB编号 判断JOB有task和判断JOB闭环
    		Map<String,Object> fileTaskKeyMap = new HashMap<>(); //存文件中JOB编号+TASK编号 过滤重复
	    	if(null == tWorkbook.getSheet("WW_TASK")) {
	    		throw new Exception("导入的文件中不存在SHEET(WW_TASK)");
	    	}else {
	    		Sheet tSheet = tWorkbook.getSheet("WW_TASK");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装任务项
	    			Task task = new Task();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			if (null == importJobIdMap.get(colValue)) {
	    				throw new Exception("WW_TASK: 第"+(rowNum+1)+"行,任务模板中不存在任务模板编号"+colValue);
					}
	    			task.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "TASK_ID", true, colValue, 50);
    				if(null != fileTaskKeyMap.get(task.getJobId()+"###"+colValue)) {
    					throw new Exception("WW_TASK: 第"+(rowNum+1)+"行,存在重复的任务项"+task.getJobId()+"/"+colValue);
    				}
    				fileTaskKeyMap.put(task.getJobId()+"###"+colValue, "");
    				task.setTaskId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "TASK_NAME", true, colValue, 200);
    				task.setName(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "TITLE", false, colValue, 500);
    				task.setTitle(colValue);
    				colValue = getColValue(tRow.getCell(4));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "TASK_PLUGIN", true, colValue, 50);
    				if (null == tabPluginNameMap.get(colValue) && null == filePluginNameMap.get(colValue)) {
	    				throw new Exception("WW_TASK: 第"+(rowNum+1)+"行,插件中不存在"+colValue);
					}
    				task.setPlugin(colValue);
    				colValue = getColValue(tRow.getCell(5));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "PROGRAM_NAME", true, colValue, 500);
    				task.setProgramName(colValue);
    				colValue = getColValue(tRow.getCell(6));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "ALLOWED_RERUN", true, colValue, 11);
    				task.setAllowedRerun(StringUtil.getIntValue("WW_TASK", (rowNum+1), "ALLOWED_RERUN",colValue));
    				colValue = getColValue(tRow.getCell(7));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "PERIOD", false, colValue, 100);
    				task.setPeriod(colValue);
    				colValue = getColValue(tRow.getCell(8));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "WORKING_DAY", false, colValue, 50);
    				task.setWorkingDay(colValue);
    				colValue = getColValue(tRow.getCell(9));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "ERROR_DELAY", false, colValue, 11);
    				task.setErrorDelay(StringUtil.getIntValue("WW_TASK", (rowNum+1), "ERROR_DELAY",colValue));
    				colValue = getColValue(tRow.getCell(10));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "ERROR_IGNORE", true, colValue, 11);
    				task.setErrorIgnore(StringUtil.getIntValue("WW_TASK", (rowNum+1), "ERROR_IGNORE",colValue));
    				colValue = getColValue(tRow.getCell(11));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "ERROR_MAX_NUM", false, colValue, 11);
    				task.setMaxNumOfExeErrors(StringUtil.getIntValue("WW_TASK", (rowNum+1), "ERROR_MAX_NUM",colValue));
    				colValue = getColValue(tRow.getCell(12));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "USE_JOB_DIMENSION", true, colValue, 50);
    				task.setUseJobDimension(colValue);
    				colValue = getColValue(tRow.getCell(13));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "AGENT_SCOPE", false, colValue, 500);
    				task.setAgentScope(colValue);
    				colValue = getColValue(tRow.getCell(14));
    				task.setdCreateTime(StringUtil.getDateValue("WW_TASK", (rowNum+1), "CREATE_TIME", colValue));
    				colValue = getColValue(tRow.getCell(15));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "CREATOR", false, colValue, 50);
    				task.setCreator(colValue);
    				colValue = getColValue(tRow.getCell(16));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "POINT_X", false, colValue, 11);
    				task.setPointX(StringUtil.getIntValue("WW_TASK", (rowNum+1), "POINT_X",colValue));
    				colValue = getColValue(tRow.getCell(17));
    				StringUtil.validateTabColNullAndLength("WW_TASK", (rowNum+1), "POINT_Y", false, colValue, 11);
    				task.setPointY(StringUtil.getIntValue("WW_TASK", (rowNum+1), "POINT_Y",colValue));
    				importJob.getTaskList().add(task);
    				//判断JOB闭环
    				List<Task> tempTaskList = new ArrayList<>();
	    			if(null != fileTaskJobIdMap.get(task.getJobId())) {
	    				tempTaskList = fileTaskJobIdMap.get(task.getJobId());
	    			}
	    			tempTaskList.add(task);
	    			fileTaskJobIdMap.put(task.getJobId(), tempTaskList);
	    		}
	    		//判断WW_JOB中每个JOB都存在TASK
	    		for(Map.Entry<String, Map<String,TaskLean>> fMap : importJobIdMap.entrySet()) {
	    			boolean inFlag = false;
	    			for(Map.Entry<String, List<Task>> fMap2 : fileTaskJobIdMap.entrySet()) {
		    			if(fMap.getKey().equals(fMap2.getKey())) {
		    				inFlag = true;
		    				break;
		    			}
		    		}
	    			if(!inFlag) {
	    				throw new Exception("任务模板（"+fMap.getKey()+"）不存在任务项");
	    			}
	    		}
			}
	    	if (null != tWorkbook.getSheet("WW_TASK_LEAN")) {
	    		Map<String,Object> fileTaskLeanKeyMap = new HashMap<>(); //存文件中TASK依赖
	    		Sheet tSheet = tWorkbook.getSheet("WW_TASK_LEAN");
	    		for (int rowNum = 1; rowNum < tSheet.getLastRowNum()+1; rowNum++) {
	    			Row tRow = tSheet.getRow(rowNum);
	    			if (null == tRow) {
						continue;
					}
	    			//封装任务项依赖
	    			TaskLean taskLean = new TaskLean();
	    			String colValue = getColValue(tRow.getCell(0));
	    			StringUtil.validateTabColNullAndLength("WW_TASK_LEAN", (rowNum+1), "JOB_ID", true, colValue, 50);
	    			taskLean.setJobId(colValue);
    				colValue = getColValue(tRow.getCell(1));
    				StringUtil.validateTabColNullAndLength("WW_TASK_LEAN", (rowNum+1), "TASK_ID", true, colValue, 50);
    				if (null == fileTaskKeyMap.get(taskLean.getJobId()+"###"+colValue)) { 
						throw new Exception("WW_TASK_LEAN: 第"+(rowNum+1)+"行,"+taskLean.getJobId()+"+"+colValue+"的任务项不存在");
					}
    				taskLean.setTaskId(colValue);
    				colValue = getColValue(tRow.getCell(2));
    				StringUtil.validateTabColNullAndLength("WW_TASK_LEAN", (rowNum+1), "LEAN_JOB_ID", false, colValue, 50);
    				taskLean.setLeanJobId(colValue);
    				colValue = getColValue(tRow.getCell(3));
    				StringUtil.validateTabColNullAndLength("WW_TASK_LEAN", (rowNum+1), "LEAN_TASK_ID", true, colValue, 50);
    				if ((null == taskLean.getLeanJobId() || taskLean.getLeanJobId().equals("")) && null == fileTaskKeyMap.get(taskLean.getJobId()+"###"+colValue)) {
	    				throw new Exception("WW_TASK_LEAN: 第"+(rowNum+1)+"行,LEAN_TASK_ID为"+colValue+"的任务项不存在");
					}
    				taskLean.setLeanTaskId(colValue);
    				String key = taskLean.getJobId()+"###"+taskLean.getTaskId()+"###"+taskLean.getLeanTaskId();
    				if(null != fileTaskLeanKeyMap.get(key)) {
    					throw new Exception("WW_TASK_LEAN: 第"+(rowNum+1)+"行,存在重复的TASK依赖"+taskLean.getJobId()+"/"+taskLean.getTaskId()+"/"+taskLean.getLeanTaskId());
    				}
    				fileTaskLeanKeyMap.put(key, "");
    				importJob.getTaskLeanList().add(taskLean);
    				//判断JOB闭环
    				if((null == taskLean.getLeanJobId() || taskLean.getLeanJobId().equals("")) || taskLean.getJobId().equals(taskLean.getLeanJobId())) {
	    				Map<String,TaskLean> tempTaskLeanMap = importJobIdMap.get(taskLean.getJobId());
	    				String tempTaskLeanKey = taskLean.getJobId()+"###"+taskLean.getTaskId()+"###"+taskLean.getLeanTaskId();
	    				if(null != tempTaskLeanMap.get(tempTaskLeanKey)) {
	    					throw new Exception("WW_TASK_LEAN: 第"+(rowNum+1)+"行,任务项做过特殊依赖不能再做完整依赖");
	    				}
	    				tempTaskLeanMap.put(tempTaskLeanKey, taskLean);
	    				importJobIdMap.put(taskLean.getJobId(), tempTaskLeanMap);
    				}
	    		}
			}
	    	// 判断依赖关系是否闭环
	    	for(Map.Entry<String, List<Task>> fMap : fileTaskJobIdMap.entrySet()) {
	    		StringBuffer loopmsg = new StringBuffer();
				// 查询出这个job下的所有task
				List<Task> taskList = fMap.getValue();
				// 查询出这个job模板下所有task被依赖关系
				List<TaskLean> taskBeLeanList = null;
				Map<String,TaskLean> tempTaskLeanMap = importJobIdMap.get(fMap.getKey());
				if(null != tempTaskLeanMap && tempTaskLeanMap.size() > 0) {
					taskBeLeanList = new ArrayList<>();
					for(Map.Entry<String, TaskLean> fMap2 : tempTaskLeanMap.entrySet()){
						taskBeLeanList.add(fMap2.getValue());
					}
				}
				boolean cloop = JobVerifyUtil.isContainLoop(taskList, taskBeLeanList, loopmsg);
				if(cloop) {
					throw new Exception("job("+fMap.getKey()+") tasks exists contain loop. " + loopmsg.toString());
				}
	    	}
	    	return importJob;
		}catch(Exception e){
			throw e;
		}finally {
			if(null != tWorkbook) { try { tWorkbook.close(); }catch(Exception e) {} };
		}
	}

	private String getColValue(Cell tCell) {
		if(tCell != null){
		String	colValue = ExcelUtils.getCellValue(tCell,tCell.getCellType());
			if(null != colValue){
				colValue = colValue.trim();
			}
			return colValue;
		}
		return null;
	}
	
	public void insertImportJob(ImportJob importJob) throws Exception{
		//导入任务模板关联的基础数据(产品、产品认证信息、节点、数据源、维度、插件及参数、节假日、计划任务)
		try {
			insertJobBase(importJob);
		}catch(Exception e) {
			e.printStackTrace();
			importJob.setErrorInfo("导入产品对象（产品、产品认证信息、节点、数据源、维度、插件及参数、节假日、计划任务）异常", e.getMessage());
		}
		//导入任务模板关联的基础数据(维度实体)
		try {
			insertJobBase2(importJob);
		}catch(Exception e) {
			e.printStackTrace();
			importJob.setErrorInfo("导入产品对象（维度实体）异常", e.getMessage());
		}
		//删除JOB及相关表（WW_JOB、WW_JOB_DIMENSION、WW_TASK_SPECIAL_LEAN、WW_PARAMETER、WW_TASK、WW_TASK_LEAN）数据
		try {
			for (Job fJob : importJob.getJobList()) {
				if (fJob.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					deleteJobRela(fJob.getJobId());
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			importJob.setErrorInfo("删除任务模板数据异常", e.getMessage());
		}
		//插入JOB及相关表（WW_JOB、WW_JOB_DIMENSION、WW_TASK_SPECIAL_LEAN、WW_PARAMETER、WW_TASK、WW_TASK_LEAN）数据
		try {
			for (Job fJob : importJob.getJobList()) {
				insertJobRela(fJob,importJob);
			}
		}catch(Exception e) {
			e.printStackTrace();
			importJob.setErrorInfo("插入任务模板数据异常", e.getMessage());
		}
	}
	
	@Transactional
	private void insertJobBase(ImportJob importJob) throws Exception{
		if (null != importJob.getProduct()) {
			if(importJob.getProduct().getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
				productMapper.insertProduct(importJob.getProduct());
			}else if(importJob.getProduct().getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
				productMapper.updateProduct(importJob.getProduct());
			}
		}
		if (null != importJob.getProductCertificationList() && importJob.getProductCertificationList().size() > 0) {
			for (ProductCertification fProductCertification : importJob.getProductCertificationList()) {
				if(fProductCertification.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					productMapper.insertCertification(fProductCertification);
				}else if(fProductCertification.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					productMapper.updateCertification(fProductCertification);
				}
			}
		}
		if (null != importJob.getServerList() && importJob.getServerList().size() > 0) {
			for (Server fServer : importJob.getServerList()) {
				if (fServer.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					serverMapper.insertServer(fServer);
				}else if (fServer.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					serverMapper.updateServer(fServer);
				}
			}
		}
		if (null != importJob.getDataSourceList() && importJob.getDataSourceList().size() > 0) {
			for (DataSource fDataSource : importJob.getDataSourceList()) {
				if (fDataSource.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					dataSourceMapper.insertDataSource(fDataSource);
				}else if (fDataSource.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					dataSourceMapper.updateDataSourceAllValue(fDataSource);
				}
			}
		}
		if (null != importJob.getDimensionList() && importJob.getDimensionList().size() > 0) {
			for (Dimension fDimension : importJob.getDimensionList()) {
				if (fDimension.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					dimensionMapper.insertDimension(fDimension);
				}else if (fDimension.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					dimensionMapper.updateDimension(fDimension);
				}
			}
		}
		if (null != importJob.getPluginList() && importJob.getPluginList().size() > 0) {
			for (Plugin fPlugin : importJob.getPluginList()) {
				if (fPlugin.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					pluginMapper.insertPlugin(fPlugin);
				}else if (fPlugin.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					pluginMapper.updatePlugin(fPlugin);
				}
			}
		}
		if (null != importJob.getPluginParameterList() && importJob.getPluginParameterList().size() > 0) {
			for (PluginParameter fPluginParameter : importJob.getPluginParameterList()) {
				if (fPluginParameter.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					pluginMapper.insertPluginParameter(fPluginParameter);
				}else if (fPluginParameter.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					pluginMapper.updatePluginParameter(fPluginParameter);
				}
			}
		}
		if (null != importJob.getCalendarList() && importJob.getCalendarList().size() > 0) {
			for (Calendar fCalendar : importJob.getCalendarList()) {
				if (fCalendar.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					calendarMapper.insertCalendar(fCalendar);
				}else if (fCalendar.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					calendarMapper.updateCalend(fCalendar);
				}
			}
		}
	}
	
	private void insertJobBase2(ImportJob importJob) {
		if (null != importJob.getDimensionEntityList() && importJob.getDimensionEntityList().size() > 0) {
			List<DimensionEntity> iDimensionEntityList = new ArrayList<>();//批量插入
			List<DimensionEntity> uDimensionEntityList = new ArrayList<>();//批量更新
			for (DimensionEntity fDimensionEntity : importJob.getDimensionEntityList()) {
				if (fDimensionEntity.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					if(iDimensionEntityList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						dimensionEntityMapper.batchInsertDimensionEntity(iDimensionEntityList);
						iDimensionEntityList = new ArrayList<>();
					}
					iDimensionEntityList.add(fDimensionEntity);
				}else if (fDimensionEntity.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					if(uDimensionEntityList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						dimensionEntityMapper.batchUpdateDimensionEntity(uDimensionEntityList);
						uDimensionEntityList = new ArrayList<>();
					}
					uDimensionEntityList.add(fDimensionEntity);
				}
			}
			if(iDimensionEntityList.size() > 0) {
				dimensionEntityMapper.batchInsertDimensionEntity(iDimensionEntityList);
			}
			if(uDimensionEntityList.size() > 0) {
				dimensionEntityMapper.batchUpdateDimensionEntity(uDimensionEntityList);
			}
		}
	}
	
	@Transactional
	public void deleteJobRela(String jobId) throws Exception{
		Map<String,Object> delMap = new HashMap<String,Object>();
		delMap.put("jobId",jobId);
		//删除TASK模板依赖关系
		taskLeanMapper.deleteTaskLean(delMap);
		//删除TASK模板
		taskMapper.deleteTask(delMap);
		//删除JOB和TASK模板参数
		parameterMapper.deleteParameter(delMap);
		//删除JOB模板特殊依赖
		taskSpecialLeanMapper.deleteTaskSpecialLean(delMap);
		//删除JOB模板维度
		jobDimensionMapper.deleteJobDimension(delMap);
		//删除JOB模板
		jobMapper.deleteJob(delMap);
	}
	
	private void insertJobRela(Job job,ImportJob importJob) throws Exception{
		jobMapper.insertJob(job);
		List<JobDimension> jobDimensionList = importJob.getJobDimensionList();
		if(null != jobDimensionList && jobDimensionList.size() > 0) {
			List<JobDimension> tJobDimensionList = new ArrayList<>();
			for(JobDimension fJobDimension : jobDimensionList) {
				if(tJobDimensionList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
					jobDimensionMapper.batchInsertJobDimension(tJobDimensionList);
					tJobDimensionList = new ArrayList<>();
				}
				tJobDimensionList.add(fJobDimension);
			}
			if(tJobDimensionList.size() > 0) {
				jobDimensionMapper.batchInsertJobDimension(tJobDimensionList);
			}
		}
		List<TaskSpecialLean> taskSpecialLeanList = importJob.getTaskSpecialLeanList();
		if(null != taskSpecialLeanList && taskSpecialLeanList.size() > 0) {
			List<TaskSpecialLean> tTaskSpecialLeanList = new ArrayList<>();
			for(TaskSpecialLean fTaskSpecialLean : taskSpecialLeanList) {
				if(tTaskSpecialLeanList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
					taskSpecialLeanMapper.batchInsertTaskSpecialLean(tTaskSpecialLeanList);
					tTaskSpecialLeanList = new ArrayList<>();
				}
				tTaskSpecialLeanList.add(fTaskSpecialLean);
			}
			if(tTaskSpecialLeanList.size() > 0) {
				taskSpecialLeanMapper.batchInsertTaskSpecialLean(tTaskSpecialLeanList);
			}
		}
		List<Parameter> parameterList = importJob.getParameterList();
		if(null != parameterList && parameterList.size() > 0) {
			List<Parameter> iParameterList = new ArrayList<>(); //批量插入
			List<Parameter> uParameterList = new ArrayList<>(); //批量更新
			for(Parameter fParameter : parameterList) {
				if(fParameter.getImportType() == ImportJob.IMPORT_TYPE_INSERT) {
					if(iParameterList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						parameterMapper.batchInsertParameter(iParameterList);
						iParameterList = new ArrayList<>();
					}
					iParameterList.add(fParameter);
				}else if(fParameter.getImportType() == ImportJob.IMPORT_TYPE_UPDATE) {
					if(uParameterList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						parameterMapper.batchUpdateParameter(uParameterList);
						uParameterList = new ArrayList<>();
					}
					uParameterList.add(fParameter);
				}
			}
			if(iParameterList.size() > 0) {
				parameterMapper.batchInsertParameter(iParameterList);
			}
			if(uParameterList.size() > 0) {
				parameterMapper.batchUpdateParameter(uParameterList);
			}
		}
		List<Task> taskList = importJob.getTaskList();
		if(null != taskList && taskList.size() > 0) {
			List<Task> tTaskList = new ArrayList<>();
			for(Task fTask : taskList) {
				if(tTaskList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
					taskMapper.batchInsertTask(tTaskList);
					tTaskList = new ArrayList<>();
				}
				tTaskList.add(fTask);
			}
			if(tTaskList.size() > 0) {
				taskMapper.batchInsertTask(tTaskList);
			}
		}
		List<TaskLean> taskLeanList = importJob.getTaskLeanList();
		if(null != taskLeanList && taskLeanList.size() > 0) {
			List<TaskLean> tTaskLeanList = new ArrayList<>();
			for(TaskLean fTaskLean : taskLeanList) {
				if(tTaskLeanList.size() == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
					taskLeanMapper.batchInsertTaskLean(tTaskLeanList);
					tTaskLeanList = new ArrayList<>();
				}
				tTaskLeanList.add(fTaskLean);
			}
			if(tTaskLeanList.size() > 0) {
				taskLeanMapper.batchInsertTaskLean(tTaskLeanList);
			}
		}
	}

	public void genSqlFile(Object object, ExportJob exportJob, String exportFileName, String driverClass) {
		BufferedWriter bufferedWriter = null;
		File f = null;
		try {
			if(object instanceof HttpServletResponse) {
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(((HttpServletResponse)object).getOutputStream()));
			}else {
				f = new File((String)object+exportFileName);
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
			}
			//产品
			Product product = exportJob.getProduct();
			if(null != product) {
				String sql = SqlUtils.insert("ww_product", "p_id","p_name","p_desc");
				sql += SqlUtils.values(product.getpId(),product.getpName(),product.getpDesc());
				bufferedWriter.write(sql);
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//产品认证信息
			List<ProductCertification> pcList = exportJob.getProductCertificationList();
			if (null != pcList && pcList.size() >0) {
				for (ProductCertification pc : pcList) {
					String sql = SqlUtils.insert("ww_product_certification", "p_id","auth_id","protocal","user_id","public_key");
					sql += SqlUtils.values(pc.getpId(),pc.getAuthId(),pc.getProtocal(),pc.getUserId(),pc.getPublicKey());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//节点
			List<Server> serverList = exportJob.getServerList();
			if (null != serverList && serverList.size() >0) {
				for (Server server : serverList) {
					String sql = SqlUtils.insert("ww_server", "product", "server_id", "server_desc" , "ip_addr", "ssh_port", "rmi_registry_port", "rmi_server_port", "server_type", "tags", "latest_online_time", "os_ssh_port", "os_ssh_user", "os_ssh_pswd", "zoo_connect_str","zoo_namespace", "home_dir", "ignore_plugins", "status", "version_no");
					if (driverClass.equals(ORACLE_DRIVER_CLASS)) {
						sql += SqlUtils.values(server.getProduct(),server.getServerId(),server.getServerDesc(),server.getIpAddr(),server.getSshPort()+"",server.getRmiRegistryPort()+"",server.getRmiServerPort()+"",server.getServerType(),server.getTags(),
								SqlUtils.processDateTime(true, server.getLatestOnlineTime()),server.getOsSshPort()+"",
								server.getOsSshUser(),server.getOsSshPswd(),server.getZooConnectStr(),server.getZooNamespace(),server.getHomeDir(),server.getIgnorePlugins(),server.getStatus()+"",server.getVersionNo());
					}else {
						sql += SqlUtils.values(server.getProduct(),server.getServerId(),server.getServerDesc(),server.getIpAddr(),server.getSshPort()+"",server.getRmiRegistryPort()+"",server.getRmiServerPort()+"",server.getServerType(),server.getTags(),
								SqlUtils.processDateTime(false, server.getLatestOnlineTime()),server.getOsSshPort()+"",
								server.getOsSshUser(),server.getOsSshPswd(),server.getZooConnectStr(),server.getZooNamespace(),server.getHomeDir(),server.getIgnorePlugins(),server.getStatus()+"",server.getVersionNo());
					}
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//数据源
			List<DataSource> dataSourceList = exportJob.getDataSourceList();
			if(null != dataSourceList && dataSourceList.size() > 0) {
				for (DataSource dataSource : dataSourceList) {
					String sql = SqlUtils.insert("ww_ds", "product", "agent", "name" , "driver_name", "driver_class", "database_name", "database_type", "jdbc_url", "jdbc_user", "jdbc_password", "auto_start", "pool", "xa", "pool_properties");
					sql += SqlUtils.values(dataSource.getProduct(),dataSource.getAgent(),dataSource.getName(),dataSource.getDriverName(),dataSource.getDriverClass(),dataSource.getDatabaseName(),
							dataSource.getDatabaseType(),dataSource.getJdbcUrl(),dataSource.getJdbcUser(),dataSource.getJdbcPassword(),dataSource.getAutoStart(),dataSource.getPool(),dataSource.getXa(),dataSource.getPoolProperties());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//维度信息
			List<Dimension> dimensions = exportJob.getDimensionList();
			if(null != dimensions && dimensions.size() > 0) {
				for (Dimension dimension : dimensions) {
					String sql = SqlUtils.insert("ww_dimension", "product","dmsn_name","dmsn_desc");
					sql += SqlUtils.values(dimension.getProduct(),dimension.getName(),dimension.getDescription());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			List<DimensionEntity> dimensionEntities = exportJob.getDimensionEntityList();
			if(null != dimensionEntities && dimensionEntities.size() > 0) {
				int startRowNum = 1;
				for(DimensionEntity dimensionEntity : dimensionEntities) {
					String sql = SqlUtils.insert("ww_dimension_entity", "product","dmsn_name","entity_name","entity_desc","title");
					sql += SqlUtils.values(dimensionEntity.getProduct(),dimensionEntity.getName(),dimensionEntity.getEntity(),dimensionEntity.getEntityDesc(),dimensionEntity.getTags());
					bufferedWriter.write(sql);
					if(startRowNum == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						bufferedWriter.write("commit;\r\n");
						startRowNum = 1;
						continue;
					}
					startRowNum++;
				}
				if (startRowNum == 1) {
					bufferedWriter.newLine();
				}else {
					bufferedWriter.write("commit;\r\n");
					bufferedWriter.newLine();
				}
			}
			//插件
			List<Plugin> plugins = exportJob.getPluginList();
			if (null != plugins && plugins.size()>0) {
				for (Plugin plugin : plugins) {
					String sql = SqlUtils.insert("ww_plugin", "name","plugin_desc","feature_name","version");
					sql += SqlUtils.values(plugin.getName(),plugin.getDesc(),plugin.getFeature(),plugin.getVersion());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
				//插件参数
				for (Plugin plugin : plugins) {
					List<PluginParameter> pluginParameters = plugin.getPluginParameters();
					for(PluginParameter pluginParameter : pluginParameters) {
						String sql = SqlUtils.insert("ww_plugin_parameter", "plugin_name","param_name","data_type","param_def_value","is_required","param_desc","data_range");
						sql += SqlUtils.values(pluginParameter.getPluginName(),pluginParameter.getParamName(),pluginParameter.getDataType(),pluginParameter.getParamDefValue(),String.valueOf(pluginParameter.getIsRequired()),pluginParameter.getParamDesc(),pluginParameter.getDataRange());
						bufferedWriter.write(sql);
					}
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//假日编排
			List<Calendar> calendars = exportJob.getCalendarList();
			if (null != calendars && calendars.size()>0) {
				for (Calendar calendar : calendars) {
					String sql = SqlUtils.insert("ww_calendar", "product","cal_name","cal_description","include_value","exclusion_value");
					sql += SqlUtils.values(calendar.getProduct(),calendar.getCalName(),calendar.getCalDescription(),calendar.getIncludeValue(),calendar.getExclusionValue());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//计划任务
			List<Cron> cronList = exportJob.getCronList();
			if (null != cronList && cronList.size() >0) {
				for (Cron cron : cronList) {
					String sql = SqlUtils.insert("ww_cron", "job_id","model","product","agent","job_name","cron_expression","plugin","program_name","auto_start","create_time","creator");
					sql += SqlUtils.values(cron.getJobId(),cron.getModel(),cron.getProduct(),cron.getAgent(),cron.getJobName(),cron.getCronExpression(),cron.getPlugin(),cron.getProgramName(),cron.getAutoStart()+"",cron.getCreateTime(),cron.getCreator());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//JOB
			//任务模板
			List<Job> jobList = exportJob.getJobList();
			if(null != jobList && jobList.size() > 0) {
				for(Job fJob : jobList) {
					String sql = SqlUtils.insert("ww_job", "job_id","product","job_name","job_mode","schedule_rid","title","version","create_time","creator");
					if (driverClass.equals(ORACLE_DRIVER_CLASS)) {
						sql += SqlUtils.values(fJob.getJobId(),fJob.getProduct(),fJob.getName(),String.valueOf(fJob.getMode()),fJob.getScheduleRid(),fJob.getTitle(),fJob.getVersion(),
								SqlUtils.processDate(true, fJob.getCreateTime()),fJob.getCreator());
					}else {
						sql += SqlUtils.values(fJob.getJobId(),fJob.getProduct(),fJob.getName(),String.valueOf(fJob.getMode()),fJob.getScheduleRid(),fJob.getTitle(),fJob.getVersion(),
								SqlUtils.processDate(false, fJob.getCreateTime()),fJob.getCreator());
					}
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//维度方案
			List<JobDimension> jobDimensions = exportJob.getJobDimensionList();
			if(null != jobDimensions && jobDimensions.size() > 0) {
				for (JobDimension jobDimension : jobDimensions) {
					String sql = SqlUtils.insert("ww_job_dimension", "job_id","model_no","dmsn_name","dmsn_product","entity_name");
					sql += SqlUtils.values(jobDimension.getJobId(),jobDimension.getModelNo(),jobDimension.getDmsnName(),jobDimension.getDmsnProduct(),jobDimension.getEntityName());
					bufferedWriter.write(sql);
				}
				bufferedWriter.write("commit;\r\n");
				bufferedWriter.newLine();
			}
			//特殊依赖关系
			List<TaskSpecialLean> taskSpecialLeans = exportJob.getTaskSpecialLeanList();
			if (null != taskSpecialLeans && taskSpecialLeans.size()>0) {
				int startRowNum = 1;
				for(TaskSpecialLean taskSpecialLean : taskSpecialLeans) {
					String sql = SqlUtils.insert("ww_task_special_lean", "job_id","task_id","task_dim_entities","lean_job_id","lean_task_id","lean_task_dim_entities");
					sql += SqlUtils.values(taskSpecialLean.getJobId(),taskSpecialLean.getTaskId(),taskSpecialLean.getExp(),taskSpecialLean.getLeanJobId(),taskSpecialLean.getBeTaskId(),taskSpecialLean.getBeExp());
					bufferedWriter.write(sql);
					if(startRowNum == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						bufferedWriter.write("commit;\r\n");
						startRowNum = 1;
						continue;
					}
					startRowNum++;
				}
				if (startRowNum == 1) {
					bufferedWriter.newLine();
				}else {
					bufferedWriter.write("commit;\r\n");
					bufferedWriter.newLine();
				}
			}
			//传入参数 and 任务项传入参数
			List<Parameter> parameters = exportJob.getParameterList();
			if (null != parameters && parameters.size()>0) {
				int startRowNum = 1;
				for(Parameter parameter : parameters) {
					String sql = SqlUtils.insert("ww_parameter", "job_id","source_id","name","value","required");
					sql += SqlUtils.values(parameter.getJobId(),parameter.getSourceId(),parameter.getName(),parameter.getValue().replaceAll("'", "''"),parameter.getdRequired());
					bufferedWriter.write(sql);
					if(startRowNum == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						bufferedWriter.write("commit;\r\n");
						startRowNum = 1;
						continue;
					}
					startRowNum++;
				}
				if (startRowNum == 1) {
					bufferedWriter.newLine();
				}else {
					bufferedWriter.write("commit;\r\n");
					bufferedWriter.newLine();
				}
			}
			//任务项
			List<Task> tasks = exportJob.getTaskList();
			if(null != tasks && tasks.size()>0) {
				int startRowNum = 1;
				for(Task task : tasks) {
					String sql = SqlUtils.insert("ww_task", "job_id","task_id","task_name","title","task_plugin","program_name","allowed_rerun","period","working_day","error_delay","error_ignore","error_max_num","use_job_dimension","agent_scope","create_time","creator","point_x","point_y");
					if (driverClass.equals(ORACLE_DRIVER_CLASS)) {
						sql += SqlUtils.values(task.getJobId(),task.getTaskId(),task.getName(),task.getTitle(),task.getPlugin(),task.getProgramName(),task.getAllowedRerun(),task.getPeriod(),task.getWorkingDay(),task.getErrorDelay(),
							task.getErrorIgnore(),task.getMaxNumOfExeErrors(),task.getUseJobDimension(),task.getAgentScope(),
							SqlUtils.processDate(true, task.getCreateTime()),task.getCreator(),task.getPointX(),task.getPointY());
					}else {
						sql += SqlUtils.values(task.getJobId(),task.getTaskId(),task.getName(),task.getTitle(),task.getPlugin(),task.getProgramName(),task.getAllowedRerun(),task.getPeriod(),task.getWorkingDay(),task.getErrorDelay(),
								task.getErrorIgnore(),task.getMaxNumOfExeErrors(),task.getUseJobDimension(),task.getAgentScope(),
								SqlUtils.processDate(false, task.getCreateTime()), task.getCreator(),task.getPointX(),task.getPointY());
					}
					bufferedWriter.write(sql);
					if(startRowNum == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						bufferedWriter.write("commit;\r\n");
						startRowNum = 1;
						continue;
					}
					startRowNum++;
				}
				if (startRowNum == 1) {
					bufferedWriter.newLine();
				}else {
					bufferedWriter.write("commit;\r\n");
					bufferedWriter.newLine();
				}
			}
			//任务项依赖
			List<TaskLean> taskLeans = exportJob.getTaskLeanList();
			if (null != taskLeans && taskLeans.size()>0) {
				int startRowNum = 1;
				for(TaskLean taskLean : taskLeans) {
					String sql = SqlUtils.insert("ww_task_lean", "job_id","task_id","lean_job_id","lean_task_id");
					sql += SqlUtils.values(taskLean.getJobId(),taskLean.getTaskId(),taskLean.getLeanJobId(),taskLean.getLeanTaskId());
					bufferedWriter.write(sql);
					if(startRowNum == ExportJob.BATCH_COMMIT_MAX_RECORD_NUM) {
						bufferedWriter.write("commit;\r\n");
						startRowNum = 1;
						continue;
					}
					startRowNum++;
				}
				if (startRowNum == 1) {
					bufferedWriter.newLine();
				}else {
					bufferedWriter.write("commit;\r\n");
					bufferedWriter.newLine();
				}
			}
			bufferedWriter.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(null != bufferedWriter) { 
				try { bufferedWriter.close(); }catch(Exception e2) {}
			}
		}
		
	}
	
}
