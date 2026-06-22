package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.JobTemplateUtils;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UploadUtils;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.dag.DirectedGraph;
import com.bocsoft.wwsf.webconsole.dag.DirectedGraphConcise;
import com.bocsoft.wwsf.webconsole.dag.Edge;
import com.bocsoft.wwsf.webconsole.dag.EdgeConcise;
import com.bocsoft.wwsf.webconsole.dag.Vertex;
import com.bocsoft.wwsf.webconsole.dag.VertexConcise;
import com.bocsoft.wwsf.webconsole.model.Dimension;
import com.bocsoft.wwsf.webconsole.model.DimensionEntity;
import com.bocsoft.wwsf.webconsole.model.ExportJob;
import com.bocsoft.wwsf.webconsole.model.ImportJob;
import com.bocsoft.wwsf.webconsole.model.Job;
import com.bocsoft.wwsf.webconsole.model.JobExeInfo;
import com.bocsoft.wwsf.webconsole.model.JobPanelInfo;
import com.bocsoft.wwsf.webconsole.model.JobUpload;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskLean;
import com.bocsoft.wwsf.webconsole.model.TaskLoopback;
import com.bocsoft.wwsf.webconsole.model.TaskSpecialLean;
import com.bocsoft.wwsf.webconsole.service.DimensionEntityService;
import com.bocsoft.wwsf.webconsole.service.DimensionService;
import com.bocsoft.wwsf.webconsole.service.JobDimensionService;
import com.bocsoft.wwsf.webconsole.service.JobExeInfoService;
import com.bocsoft.wwsf.webconsole.service.JobService;
import com.bocsoft.wwsf.webconsole.service.JobUploadService;
import com.bocsoft.wwsf.webconsole.service.ParameterService;
import com.bocsoft.wwsf.webconsole.service.PluginService;
import com.bocsoft.wwsf.webconsole.service.TaskService;
import com.bocsoft.wwsf.webconsole.service.TaskSpecialLeanService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.PageInfo;

@Configuration
@RestController
public class JobTemplateController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	JobService jobService;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	DimensionService dimensionService;
	
	@Autowired
	DimensionEntityService dimensionEntityService;
	
	@Autowired
	PluginService pluginService;
	
	@Autowired
	ParameterService parameterService;
	
	@Autowired
	JobDimensionService jobDimensionService;
	
	@Autowired
	TaskSpecialLeanService taskSpecialLeanService;

	@Autowired
	WwseService wwseService;
	
	@Autowired
	JobExeInfoService jobExeInfoService;
	
	@Autowired
	JobUploadService jobUploadService;
	
	@Value("${file.releasePath}")
	public String fileReleasePath;
	
	@Value("${spring.datasource.driver-class-name}")
	public String driverClass;
	
	@Value("${spring.servlet.multipart.max-request-size}")
	public String uploadFileMaxSize;
	
	@PostMapping("/template/checkLoopback")
	@ResponseBody
	public RestResponse checkLoopback(@RequestBody TaskLoopback loopback){
		RestResponse response = new RestResponse();
		try {
			response = JobTemplateUtils.checkLoopback(response,loopback.getStartPort(),loopback.getConnects(),loopback.getCheckPort());
		} catch(Exception e) {
			logger.error("check loop back error ",e);
			response.setSuccess(false);
			response.setInfo("system running error!");
		}
		return response;
	}

	/*@PostMapping(value="/template/jobTemplateExport")
	public void jobTemplateExport(HttpServletRequest request,HttpServletResponse resp) {
		@SuppressWarnings("unchecked")
		List<Map<String,String>> infos = JsonUtil.toPojo(request.getParameter("info"),List.class);
		List<Job> jobs = new ArrayList<>();
		Map<String,List<Task>> tasks = new HashMap<>();
		try{
			if(infos != null){
				for(Map<String,String> info : infos){
					String product = info.get("product");
					String jobId = info.get("jobId");
					String key = RedisKey.getJobKey(product,jobId);
					String value = redisService.get(key);
					Job job = JsonUtil.toPojo(value,Job.class);
					if(null != job){
						tasks.put(job.getProduct() + "|" + job.getJobId(),new ArrayList<Task>());
						jobs.add(job);
						String taskKey = RedisKey.getTaskListKey(job);
						Map<String,String> taskValues = redisService.hgetAll(taskKey);
						if(taskValues != null){
							Set<String> taskIds = taskValues.keySet();
							if(taskIds != null){
								Iterator<String> iterator = taskIds.iterator();
								while(iterator.hasNext()){
									String taskId = iterator.next();
									Task task = new Task();
									task = JsonUtil.toPojo(taskValues.get(taskId),Task.class);
									if(null != task){
										tasks.get(job.getProduct() + "|" + job.getJobId()).add(task);
									} else {
										logger.error("get tasks failed, job:{}", jobId, "查询不到job下tasks信息");
										throw new Exception("查询不到job[" + jobId + "]下task信息");
									}
								}
							}
						}
					} else {
						logger.error("get failed, taskId:{}", jobId, "查询不到job信息");
						throw new Exception("查询不到job[" + jobId + "]信息");
					}
				}
			}
			jobTemplateService.exportJobAndTasksInfo(jobs, tasks, resp);
		} catch (Exception e){
			logger.error(e.getMessage());
		}
	}*/

	/*@PostMapping(value="/template/jobTemplateImport")
	public RestResponse jobTemplateImport(@RequestParam("uFile") MultipartFile multipartFile) {
		List<String> redisKeys = new ArrayList<>();
		RestResponse response = new RestResponse();
		List<String> products = new ArrayList<>();
		Map<String,List<String>> dmsns = new HashMap<>();
		Map<String,List<String>> jobs = new HashMap<>();
		Map<String,Map<String,List<String>>> titles = new HashMap<>();
		Map<String,Map<String,List<String>>> entities = new HashMap<>();
		Map<String,List<String>> pluginParameters = new HashMap<>();
		Map<String,List<String>> plugins = new HashMap<>();
		try{
			Map<String,String> productvs = redisService.hgetAll(RedisKey.getProductRootKey());
			if(productvs.size()>0){
				for (Entry<String,String> sp : productvs.entrySet()) {
					products.add(sp.getKey());
					dmsns.put(sp.getKey(), new ArrayList<String>());
					titles.put(sp.getKey(), new HashMap<String,List<String>>());
					entities.put(sp.getKey(), new HashMap<String,List<String>>());
				}
			}
			for(String product : products){
				plugins.put(product, new ArrayList<String>());
				String pluginKey = RedisKey.getPluginKey(product, "*");
				String keyComment = RedisKey.getPluginKey("wws", "*");
				Set<String> keySet = redisService.keys(pluginKey);
				if(keySet != null){
					Iterator<String> iterator = keySet.iterator();
					while(iterator.hasNext()){
						PojoPlugin plugin = new PojoPlugin();
						String tKey = iterator.next();
						String value = redisService.get(tKey);
						plugin = JsonUtil.toPojo(value,PojoPlugin.class);
						if(plugin != null){
							plugins.get(product).add(plugin.getName());
							List<PojoPluginParameter> params = plugin.getParameters();
							if(params != null){
								List<String> mustPlugins = new ArrayList<>();
								for(PojoPluginParameter param : params){
									if(param.getRequired()){
										mustPlugins.add(param.getName());
									}
								}
								pluginParameters.put(product + "|" + plugin.getName(), mustPlugins);
							}
						}
					}		
				}
				Set<String> keySetComment = redisService.keys(keyComment);
				if(keySetComment != null){
					Iterator<String> iterator = keySetComment.iterator();
					while(iterator.hasNext()){
						PojoPlugin plugin = new PojoPlugin();
						String tKey = iterator.next();
						String value = redisService.get(tKey);
						plugin = JsonUtil.toPojo(value,PojoPlugin.class);
						if(plugin != null){
							plugins.get(product).add(plugin.getName());
							List<PojoPluginParameter> params = plugin.getParameters();
							if(params != null){
								List<String> mustPlugins = new ArrayList<>();
								for(PojoPluginParameter param : params){
									if(param.getRequired()){
										mustPlugins.add(param.getName());
									}
								}
								pluginParameters.put(product + "|" + plugin.getName(), mustPlugins);
							}
							
						}
					}		
				}
				String jobKey = RedisKey.getJobKey(product, "*");
				Set<String> keys = redisService.keys(jobKey);
				jobs.put(product, new ArrayList<String>());
				if(keys != null && keys.size() > 0) {
					keys.forEach(v -> {
						Job jobCurrent = JsonUtil.toPojo(redisService.get(v), Job.class);
						if(jobCurrent != null)
							jobs.get(product).add(jobCurrent.getJobId());
					});
				}
				
				String key = RedisKey.getDimsnKey(product);
				Set<String> rs = redisService.hkeys(key);
				for(String dmsn : rs) {
					dmsns.get(product).add(dmsn);
					titles.get(product).put(dmsn, new ArrayList<String>());
					entities.get(product).put(dmsn, new ArrayList<String>());
					//拿到产品维度下的标签
					String pattern = RedisKey.getDimsnTitlePatternKey(product, dmsn);
					Set<String> tagKeys = redisService.keys(pattern);
					for(String tagKey: tagKeys) {
						String[] arr = tagKey.split(":");
						if(arr.length >= 5) {
							String tag = arr[4];
							titles.get(product).get(dmsn).add(tag);
						}
					}
					//获取产品维度下的实体
					String entityKey = RedisKey.getDimsnKey(product, dmsn);
					List<String> dmsnEntityList = redisService.lrange(entityKey, 0, -1);
					for (String dmsnEntity: dmsnEntityList){
						entities.get(product).get(dmsn).add(dmsnEntity);
					}
				}
			}
			jobTemplateService.importJobAndTasksInfo(multipartFile,products,jobs,dmsns,titles,entities,pluginParameters,plugins,redisKeys,redisService);
			response.setSuccess(true);
		} catch (Exception e){
			try{
				for(String redisKey : redisKeys){
					redisService.delete(redisKey);
				}
			} catch (Exception e1){
				e1.printStackTrace();
				response.setSuccess(false);
				response.setInfo(e.getMessage() + "并且删除redis上已加数据错误");
			}
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}*/

	/*@GetMapping(value="/template/jobTemplateDownloadTmpl")
	public void jobTemplateDownloadTmpl(HttpServletResponse response) {
		try{
			String downloadFileName = "temp_jobAndTaskInfo.xls";
			String tempPath = DimensionController.class.getClassLoader().getResource("").getPath()+downloadFileName;
			File f = new File(tempPath);
			if(f.exists()){
				response.setHeader("content-disposition", "attachment;filename=" + downloadFileName);
				InputStream is = null;
				OutputStream os = null;
				try{
					is = new FileInputStream(f);
					int len = 0;
					byte[] buffer = new byte[1024];
					os = response.getOutputStream();
					while((len = is.read(buffer)) > 0){
						os.write(buffer,0,len);
					}
					os.flush();
				}catch(Exception e2){
					e2.printStackTrace();
				}finally{
					if(null != is){
						is.close();
					}
					if(null != os){
						os.close();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/

	/*@GetMapping(value="/template/runJobPruneTemplate",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse runJobPruneTemplate(String product,String jobId,String params,String jobPruneInfo){
		RestResponse response = new RestResponse();
		List<String> list = new ArrayList<>();
		try{
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", jobId);
			List<Task> tasks = this.taskService.getTaskList(condition);
			if(null == tasks || tasks.size()<1) {
				throw new Exception("job has no tasks");
			}
			JobPruneInfo pruneInfo = JSONObject.parseObject(jobPruneInfo, JobPruneInfo.class);
			if(pruneInfo == null){
				throw new Exception("no jobPruneInfo find");
			}
			
			TemplateUtils.leansToBeleans(tasks,null);//务必拿到被依赖
			
			Map<String,Task> taskMap = new HashMap<>();
			for(Task task : tasks){
				taskMap.put(task.getTaskId(), task);
			}
			Set<String> taskIds = pruneInfo.getResult().keySet();
			if(taskIds != null){
				Iterator<String> iterator = taskIds.iterator();
				while(iterator.hasNext()){
					String taskId = iterator.next();
					boolean flag = pruneInfo.getResult().get(taskId);
					if(flag){
						getAllTask(taskMap,taskId,list);
					} else {
						Task task = taskMap.get(taskId);
						if(null != task){
							list.add(task.getTaskId());
						} else {
							throw new Exception("查询不到task[" + taskId + "]信息");
						}
					}
				}
			}
			Long jobInsId = wwseService.jobInit(product, jobId, params);
			response.put("jobInsId", jobInsId);
			response.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			//logger.error("run failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo("run failed, jobId:{" + jobId + "}");
		}
		return response;
	}*/

    public static void getAllTask(Map<String,Task> taskMap, String taskId,List<String> list) throws Exception{
		Task task = taskMap.get(taskId);
		if(null != task){
			list.add(task.getTaskId());
			List<String> beleans = task.getBeleans();
			if(beleans != null){
				for(String belean : beleans){
					getAllTask(taskMap,belean,list);
				}
			}
		} else {
			throw new Exception("查询不到task[" + taskId + "]信息");
		}
	}

	@GetMapping(value="/template/runJobTemplate",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse runJobTemplate(String product,String jobId,String params){
		RestResponse response = new RestResponse();
		try{
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", jobId);
			List<Task> tasks = this.taskService.getTaskList(condition);
			if(null == tasks || tasks.size()<1) {
				response.setSuccess(false);
				response.setInfo("job has no tasks");
				return response;
			}
			Long jobInsId = wwseService.jobInit(product, jobId, params);
			response.put("jobInsId", jobInsId);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("run failed, jobId:{}", jobId, e);
			StringBuffer info = new StringBuffer();
			info.append("running job '").append(jobId).append("' failed");
			response.setSuccess(false);
			response.setInfo(info.toString());
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping(value="/template/deltask",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse deltask(String product,String jobId,String taskId){
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product) || StringUtil.isNullOrEmpty(jobId) || StringUtil.isNullOrEmpty(taskId)){
			response.setSuccess(false);
			response.setInfo("参数无效[jobId]");
			return response;
		}
		try{
			taskService.deleteTask(jobId,new String[] {taskId});
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("delete failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@DeleteMapping(value="/template/deltasks",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse deltasks(String product,String jobId,String taskIds){
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product) || StringUtil.isNullOrEmpty(jobId) || StringUtil.isNullOrEmpty(taskIds)){
			response.setSuccess(false);
			response.setInfo("参数无效[jobId]");
			return response;
		}
		try{
			taskService.deleteTask(jobId,taskIds.split(","));
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("delete failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@DeleteMapping(value="/template/jobs/{jobsId}",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse delJobs(@PathVariable String jobsId) {
		RestResponse response = new RestResponse();
		String[] ids = jobsId.split("\\|");
		if(ids == null || ids.length <= 0){
			response.setSuccess(false);
			response.setInfo("请选择需要删除的jobs");
			return response;
		}
		try {
			jobService.deleteJob(ids);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete jobsId:{}",String.join(";", ids), e);
			response.setSuccess(false);
			response.setInfo("删除操作失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping(value="/template/getTask",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse getTask(String product,String jobId,String taskId){
		RestResponse response = new RestResponse();
		try{
			Task task = taskService.getTask(jobId, taskId);
			if(null != task){
				if(null == task.getCreateTime()) {
					task.setCreateTime(CalendarUtil.getNowTime("yyyy-MM-dd"));
				}else {
					task.setCreateTime(task.getCreateTime().substring(0,10));
				}
				/*response.put("task", task); 
				response.setSuccess(true);*/
			}/* else {
				response.setSuccess(false);
				response.setInfo("查询不到task[" + taskId + "]信息");
			}*/
			response.put("task", task); 
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("get failed, taskId:{}", taskId, e);
			response.setSuccess(false);
			response.setInfo("查询task信息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping(value="/template/getJob",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse getJob(String product,String jobId){
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(jobId)){
			response.setSuccess(false);
			response.setInfo("参数缺失,请传入Job模板编号");
			return response;
		}
		Job job = null;
		try{
			job = jobService.getJob(product, jobId);
			if(null != job){
				if(null == job.getCreateTime()) {
					job.setCreateTime(CalendarUtil.getNowTime("yyyy-MM-dd"));
				}else {
					job.setCreateTime(job.getCreateTime().substring(0,10));
				}
				response.put("job", job);
				response.setSuccess(true);
			} else {
				response.setSuccess(false);
				response.setInfo("查询不到Job[" + jobId + "]信息");
				logger.error("get failed, jobId:{}", jobId, "查询不到Job信息");
			}
		}catch(Exception e){
			logger.error("get failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo("[" + jobId + "] get job template failed");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	/*@GetMapping("/template/getJobPanelInfo")
	@ResponseBody
	public RestResponse getJobPanelInfo(String product,String jobId){
		RestResponse response = new RestResponse();
		JobPanelInfo jobPanelInfo = null;
		try{
			String key = RedisKey.getJobGraphKey(product, jobId);
			String tValue = redisService.get(key);
			jobPanelInfo = JsonUtil.toPojo(tValue,JobPanelInfo.class);
			if(null != jobPanelInfo){
				response.setSuccess(true);
				response.put("jobPanelInfo", jobPanelInfo);
			} else {
				logger.error("get job panel failed, jobId:{}", jobId);
				response.setSuccess(false);
				response.setInfo("get job panel failed, jobId:{" + jobId + "}");
			}
		}catch(Exception e){
			logger.error("get job panel failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}*/

	/*@GetMapping(value="/template/getJobPruneInfo",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getJobPruneInfo(String product,String jobId,String name){
		RestResponse response = new RestResponse();
		JobPruneInfo jobPruneInfo = null;
		try{
			String key = RedisKey.getJobPruneKey(product, jobId);
			List<String> hashKeys = new ArrayList<>();
			hashKeys.add(name);
			List<String> tValues = redisService.hmget(key, hashKeys);
			jobPruneInfo = JsonUtil.toPojo(tValues.get(0),JobPruneInfo.class);
			if(null != jobPruneInfo){
				response.put("jobPruneInfo", jobPruneInfo);
				response.setSuccess(true);
			} else {
				response.setSuccess(false);
				response.setInfo("查询不到剪裁方案[" + name + "]信息");
				logger.error("get failed, jobId：{},剪裁名:{}", jobId, name, "查询不到剪裁方案");
			}
		}catch(Exception e){
			logger.error("get failed, jobId：{},剪裁名:{}", jobId, name, e);
			response.setSuccess(false);
			response.setInfo(StringUtil.stringifyException(e));
		}
		return response;
	}*/

	/*	@GetMapping("/template/getJobPruneInfoList")
		public RestResponse getJobPruneInfo(String product,String jobId){
			RestResponse response = new RestResponse();
			//String key = RedisKey.getJobPruneKey(product, jobId);
			List<JobPruneInfo> jobPruneInfos = new ArrayList<>();
			try{	
				Map<String, String> hashInfo = redisService.hgetAll(key);
				Set<String> jobPruneInfoSet = hashInfo.keySet();
				if(jobPruneInfoSet != null){
					Iterator<String> iterator = jobPruneInfoSet.iterator();
					while(iterator.hasNext()){
						JobPruneInfo jobPruneInfo = null;
						String name = iterator.next();
						String value = hashInfo.get(name);
						jobPruneInfo = JsonUtil.toPojo(value,JobPruneInfo.class);
						if(jobPruneInfo == null){
							response.setSuccess(false);
							response.setInfo("查询不到job[" + jobId + "]下的剪裁名[" + name + "]的信息");
							return response;
						}
						
						jobPruneInfos.add(jobPruneInfo);	
					}		
				}
				response.put("jobPruneInfos", jobPruneInfos);
				response.setSuccess(true);
			}catch(Exception e){
				e.printStackTrace();
				response.setSuccess(false);
				response.setInfo(e.getMessage());
			}
			return response;
		}*/

	/*@PostMapping("/template/saveJobPruneInfo")
	public RestResponse saveJobPruneInfo(@RequestBody JobPruneInfo jobPruneInfo){
		RestResponse response = new RestResponse();
		try{
			if(jobPruneInfo == null){
				throw new Exception("JOB剪裁信息页面获取错误");
			}
			String key = RedisKey.getJobPruneKey(jobPruneInfo.getProduct(), jobPruneInfo.getJobId());
			redisService.hdel(key, jobPruneInfo.getName());
			redisService.hset(key, jobPruneInfo.getName(), JsonUtil.pojoToString(jobPruneInfo));
			response.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}*/

	@PostMapping("/template/saveJobPanelInfo")
	@ResponseBody
	public RestResponse saveJobPanelInfo(@RequestBody JobPanelInfo jobPanelInfo){
		RestResponse response = new RestResponse();
		//String key = RedisKey.getJobGraphKey(jobPanelInfo.getProduct(), jobPanelInfo.getJobId());
		try{
			/*String rowStr = JsonUtil.pojoToString(jobPanelInfo);
			redisService.set(key, rowStr);	
			Map<String, List<String>> leans = jobPanelInfo.getLeansRelationShip();
			Set<String> taskIds1 = leans.keySet();
			Iterator<String> iterator1 = taskIds1.iterator();
			String tasksKey = RedisKey.getTaskListKey(jobPanelInfo.getProduct(), jobPanelInfo.getJobId());
			Map<String,String> tasks = redisService.hgetAll(tasksKey);
			while(iterator1.hasNext()){//保存更新后的依赖关系
				String lean = iterator1.next();
				List<String> value = leans.get(lean);
				Task task = JsonUtil.toPojo(tasks.get(lean), Task.class);
				if(task == null){
					continue;
				}
				task.setLeans(value);
				String taskInfo = JsonUtil.pojoToString(task);
				tasks.put(lean, taskInfo);		
			}
			Set<String> taskIds2 = tasks.keySet();
			Iterator<String> iterator2 = taskIds2.iterator();
			while(iterator2.hasNext()){//删除掉原来数据中没用的依赖关系
				String lean = iterator2.next();
				if(!taskIds1.contains(lean)){
					Task task = JsonUtil.toPojo(tasks.get(lean), Task.class);
					task.setLeans(null);
					String taskInfo = JsonUtil.pojoToString(task);
					tasks.put(lean, taskInfo);	
				}		
			}
			redisService.hmset(tasksKey, tasks); //更新task信息完成
			*/		
			taskService.saveTaskRela(jobPanelInfo);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("save job panel info failed ", e);
			response.setSuccess(false);
			response.setInfo("保存画板信息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping("/template/isContainLoop")
	@ResponseBody
	public RestResponse isContainLoop(@RequestBody JobPanelInfo jobPanelInfo){
		RestResponse response = new RestResponse();
		try{
			String errorInfo = taskService.isContainLoop(jobPanelInfo);
			if(null == errorInfo) {
				response.setSuccess(true);
			}else {
				response.setInfo(errorInfo);
				response.setSuccess(false);
			}
		}catch(Exception e){
			logger.error("validate "+jobPanelInfo.getJobId()+" isContainLoop failed ", e);
			response.setSuccess(false);
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@PostMapping("/template/saveTasks")
	@ResponseBody
	public RestResponse saveTasks(@RequestBody Task task){
		RestResponse response = new RestResponse();
		//String[] info = task.getTaskId().split("#");
		//String key = RedisKey.getTaskListKey(info[0], task.getJobId());
		try{
			/*String rowStr;
			boolean keyExistsFlag = redisService.exists(key);
			List<String> keyList = new ArrayList<String>();
			keyList.add(info[1]);
			Task taskOri = null;
			if(keyExistsFlag){
				rowStr = redisService.hmget(key, keyList).get(0);
				if(rowStr != null ){
					taskOri = JsonUtil.toPojo(rowStr, Task.class);
				}
			}
			if(taskOri == null){
				task.setTaskId(info[1]);
				task.setCreateTime(getCurrentTime());
			} else {
				task.setTaskId(info[1]);
				task.setCreateTime(taskOri.getCreateTime());
			}
			if(task.getDimesionSchemeNo().equals("0")){
				task.setUseDefaultJobDimension(false);
			} else {
				task.setUseDefaultJobDimension(true);
			}
			String[] TASK_FILTER = new String[]{"mutexView","beleanView","leanView"};
			JsonSerializer<Task> taskSerializer = new JsonSerializer<Task>(TASK_FILTER);
			rowStr = taskSerializer.serializeJson(task, Task.class);
			redisService.hdel(key, info[1]);
			redisService.hset(key, info[1], rowStr);*/
			
			taskService.saveTask(task);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("save tasks failed ", e);
			response.setSuccess(false);
			response.setInfo("保存任务项信息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@PostMapping("/template/saveJob")
	public RestResponse saveJob(@RequestBody Job job){
		RestResponse response = new RestResponse();
		
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", job.getJobId());
			List<Job> jobList = jobService.getJobList(condition);
			if(null != jobList && jobList.size()>0) {
				response.setSuccess(false);
				response.setInfo("JOBID 已经存在！");
				return response;
			}
			
			job.setdCreateTime(CalendarUtil.parseDate(job.getCreateTime(), "yyyy-MM-dd"));
			jobService.saveJob("insert", job);
			response.setSuccess(true);
		}catch(Exception e) {
			logger.error("save job failed ", e);
			response.setSuccess(false);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@PostMapping("/template/updateJob")
	public RestResponse updateJob(@RequestBody Job job){
		RestResponse response = new RestResponse();
		try{
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", job.getJobId());
			List<Job> jobList = jobService.getJobList(condition);
			if(null == jobList || jobList.size()<1) {
				response.setSuccess(false);
				response.setInfo("JOBID 不存在！");
				return response;
			}
			Job tJob = jobList.get(0);
			if(null == tJob.getCreateTime()) {
				job.setdCreateTime(CalendarUtil.parseDate(job.getCreateTime(), "yyyy-MM-dd"));
			}
			//新增job模板特殊依赖关系
			jobService.saveJob("update", job);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("update job failed ", e);
			response.setSuccess(false);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping("/template/graphJob")
	public RestResponse graphJob(@RequestBody DirectedGraphConcise graph){
		RestResponse response = new RestResponse();
		
		if (graph != null && graph.getVertexs() != null && graph.getVertexs().size() > 0) {
			DirectedGraph dg = new DirectedGraph();
			
			for (VertexConcise vertex: graph.getVertexs()) {
				dg.getDirectedGraph().put(vertex.getName(), new Vertex(vertex.getName()));
			}
			if(null != graph.getEdges() && graph.getEdges().size() > 0) {
				for (EdgeConcise edge : graph.getEdges()) {
					dg.getDirectedGraph().get(edge.getTarget()).getInDegree().incrementAndGet();
					dg.getDirectedGraph().get(edge.getSource()).getEdges().add(new Edge(dg.getDirectedGraph().get(edge.getTarget())));
				}
			}
			
			try {
				dg.toplogicSortCeng();
				dg.dye();
			} catch (Exception e) {
				logger.error("任务流程图排序失败", e);
				response.setSuccess(false);
				response.setInfo("任务流程图排序失败");
				response.setDetailInfo(StringUtil.stringifyException(e));
				return response;
			}
			for (VertexConcise v : graph.getVertexs()) {
				v.setX(dg.getDirectedGraph().get(v.getName()).getXpoint());
				v.setY(dg.getDirectedGraph().get(v.getName()).getYpoint());
			}
			response.setSuccess(true);
			response.put("graph", graph);
		} else {
			response.setSuccess(true);
			response.put("graph", null);
		}
		return response;
	}

	@GetMapping("/template/getTasks")
	public RestResponse getTaskByProductAndJobId(String product, String jobId) {
		RestResponse response = new RestResponse();
		JobPanelInfo jobPanelInfo = null;
		try {
			jobPanelInfo = taskService.getJobPanelInfo(product,jobId);
			response.put("jobPanelInfo", jobPanelInfo);
			response.put("tasks", jobPanelInfo.getTasks());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get tasks failed ", e);
			response.setSuccess(false);
			response.setInfo("获取任务项信息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/getTaskIdsByProductAndJobId")
	public RestResponse getTaskIdsByProductAndJobId(String product, String jobId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", jobId);
			List<Task> taskList = this.taskService.getTaskList(condition);
			response.put("tasks", taskList);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get tasks failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/getTitleByProductAndDimension")
	public RestResponse getTitleByProductAndDimension(String product, String dimensionName) {
		RestResponse response = new RestResponse();
		if (StringUtil.hasText(product) && StringUtil.hasText(dimensionName)) {
			try {
				List<String> tags = this.dimensionEntityService.getDimensionEntityTags(product,dimensionName);
				response.put("entities", tags);
				response.setSuccess(true);
			} catch (Exception e) {
				if (StringUtil.hasText(dimensionName)) {
					logger.error("get dimension tags of product:{}, dimension:{}", product, dimensionName, e);
				} else {
					logger.error("get dimension tags of product:{}", product, e);
				}
				response.setSuccess(false);
				response.setInfo("查询标签失败");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
			response.setInfo("查询标签成功");
			return response;
		}
		response.setSuccess(true);
		response.setInfo("查询标签完成");
		return response;
	}

	@GetMapping("/template/getDimensionEntityByProductAndDimension")
	public RestResponse getDimensionEntityByProductAndDimension(String product, String dimensionName) {
		RestResponse response = new RestResponse();
		try {
			//获取维度实体
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", product);
			condition.put("dmsnName", dimensionName);
			List<DimensionEntity> dimensionEntityList = this.dimensionEntityService.getDimensionEntityList(condition);
			response.setSuccess(true);
			response.put("entities", dimensionEntityList);
		} catch (Exception e) {
			logger.error("get dimension entity failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/getDimensionByProduct")
	public RestResponse getDimensionByProduct(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", product);
			List<Dimension> dimensionList = this.dimensionService.getDimensionList(condition);
			response.setSuccess(true);
			response.put("dimensions", dimensionList);
		}catch(Exception e) {
			logger.error("get dimension failed ", e);
			response.setSuccess(false);
			response.setInfo("获取维度失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping(value = "/template/getPlugin", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getPlugin(String pluginName,String type,String id) {
		RestResponse response = new RestResponse();
		List<Parameter> params = new ArrayList<>();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("name", pluginName);
			List<Plugin> pluginList = this.pluginService.getPluginList(condition);
			if(null == pluginList || pluginList.size()<1) {
				response.setSuccess(false);
				return response;
			}
			params = pluginList.get(0).getParameters();
			if(null != type && type.equals("cron")) {
				if(null != id && !id.equals("") && !id.equals("null")) {
					Map<String,Parameter> filterMap = new LinkedHashMap<String,Parameter>();
					for(Parameter fParameter : params) {
						filterMap.put(fParameter.getName(), fParameter);
					}
					List<Parameter> parameterList = parameterService.getParameterList(id, id);
					if(null != parameterList && parameterList.size()>0) {
						for(Parameter fParameter : parameterList) {
							if(null == filterMap.get(fParameter.getName())) {
								filterMap.put(fParameter.getName(), fParameter);
							}else{
								if(fParameter.isRequired()) {
									filterMap.put(fParameter.getName(), fParameter);
								}else{
									Parameter tParameter = filterMap.get(fParameter.getName());
									if(tParameter.isRequired()) {
										fParameter.setRequired(1);
										filterMap.put(fParameter.getName(), fParameter);
									}
								}
							}
						}
					}
					params = new ArrayList<Parameter>();
					for(Map.Entry<String, Parameter> fMap : filterMap.entrySet()) {
						params.add(fMap.getValue());
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get plugin failed ", e);
			response.setSuccess(false);
			response.setInfo("获取插件失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		response.put("params", params);
		return response;
	}
	
	@GetMapping(value = "/template/getPluginParameter", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getPluginParameter(String jobId,String sourceId,String pluginName,String programName) {
		//1.cron (jobId=cronId,sourceId=cronId); 2.job/task (jobId=jobId,sourceId=taskId)
		RestResponse response = new RestResponse();
		List<Parameter> params = new ArrayList<>();
		try {
			if(null == programName || programName.equals("")) {
				response.setSuccess(true);
				return response;
			}
			//1.取插件参数 （jobId=插件名称、sourceId=插件名称） 这类参数可以改变值，属性名不可以改变，不是必须的参数可以删除。修改记录时，不是必须的参数被删除掉也需加载
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("name", pluginName);
			//List<Plugin> pluginList = this.pluginService.getPluginList(condition);
			List<Plugin> pluginList = this.pluginService.getPluginParameter(condition); //插件参数以表ww_plugin_parameter中数据为准
			if(null == pluginList || pluginList.size()<1) {
				response.setSuccess(false);
				response.setInfo("插件【"+pluginName+"】不存在");
				return response;
			}
			params = pluginList.get(0).getParameters();
			if(null == params || params.size() < 1) {
				response.setSuccess(false);
				response.setInfo("插件【"+pluginName+"】没有参数，需同步下插件参数。");
				return response;
			}
			if(null != pluginName && !pluginName.equals("") && null != programName && !programName.equals("") && !pluginName.equals(programName)) {//插件名称 != 程序名称
				//2.取插件关联程序参数 （jobId=插件名称、sourceId=程序名称）   这类参数不可改变
				Map<String,Parameter> filterMap = new LinkedHashMap<String,Parameter>();
				for(Parameter fParameter : params) {
					filterMap.put(fParameter.getName(), fParameter);
				}
				List<Parameter> parameterList = parameterService.getParameterList(pluginName, programName);
				if(null != parameterList && parameterList.size()>0) {
					for(Parameter fParameter : parameterList) {
						fParameter.setTempParamType(2);
						if(null == filterMap.get(fParameter.getName())) {
							filterMap.put(fParameter.getName(), fParameter);
						}else{
							fParameter.setParamType(filterMap.get(fParameter.getName()).getParamType());
							fParameter.setDataRange(filterMap.get(fParameter.getName()).getDataRange());//值以插件值为准
							if(!fParameter.isRequired()) {
								Parameter tParameter = filterMap.get(fParameter.getName());
								if(tParameter.isRequired()) {
									fParameter.setRequired(1);
								}
							}
							filterMap.put(fParameter.getName(), fParameter);
						}
					}
					params = new ArrayList<Parameter>();
					for(Map.Entry<String, Parameter> fMap : filterMap.entrySet()) {
						params.add(fMap.getValue());
					}
				}
			}
			if(null != jobId && !jobId.equals("") && sourceId != null && !sourceId.equals("")) {
				//3.取cron或job/task参数 （1.cron: jobId=cronId、sourceId=cronId; 2.job/task: jobId=jobId、sourceId=taskId） 这类参数可以随意更改或删除
				Map<String,Parameter> filterMap = new LinkedHashMap<String,Parameter>();
				for(Parameter fParameter : params) {
					filterMap.put(fParameter.getName(), fParameter);
				}
				List<Parameter> parameterList = parameterService.getParameterList(jobId, sourceId);
				if(null != parameterList && parameterList.size()>0) {
					for(Parameter fParameter : parameterList) {
						if(null == filterMap.get(fParameter.getName())) {
							fParameter.setTempParamType(3);
							fParameter.setRequired(0); //非插件参数，都不是必须的
							filterMap.put(fParameter.getName(), fParameter);
						}else{
							Parameter tParameter = filterMap.get(fParameter.getName());
							fParameter.setParamType(tParameter.getParamType());
							fParameter.setTempParamType(tParameter.getTempParamType());
							fParameter.setDataRange(tParameter.getDataRange());//值以插件值为准
							if(!fParameter.isRequired()) {
								if(tParameter.isRequired()) {
									fParameter.setRequired(1);
								}
							}
							if(tParameter.getTempParamType() == 2) {//参数类型值为2时，值以2为准
								fParameter.setValue(tParameter.getValue());
							}
							filterMap.put(fParameter.getName(), fParameter);
						}
					}
					params = new ArrayList<Parameter>();
					for(Map.Entry<String, Parameter> fMap : filterMap.entrySet()) {
						params.add(fMap.getValue());
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get plugin failed ", e);
			response.setSuccess(false);
			response.setInfo("获取插件参数失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		response.put("params", params);
		return response;
	}

	@GetMapping(value = "/template/getPluginByProduct", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getPluginByProduct(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			List<Plugin> plugins = this.pluginService.getPluginList(conditions); 
			response.put("plugins", plugins); //product 对应插件
			response.put("wws", plugins); //wws 对应插件          
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get plugins failed, product:{}", product, e);
			response.setSuccess(false);
			response.setInfo("获取插件失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value = "/template/getProgramNameByPlugin", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getProgramNameByPlugin(String plugin) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("pluginName", plugin);
			List<String> programNames = this.parameterService.getProgramNames(conditions); 
			response.put("programNames", programNames); //product 对应插件
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get ProgramNames failed, product:{}", plugin, e);
			response.setSuccess(false);
			response.setInfo("获取程序名称失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/jobList")
	//public RestResponse jobInsList(String product, String jobId, int mode, String isEnable, Integer pageNumber,Integer pageSize) {
	@RequiresPermissions("menu:jobTemplate-list")
	public RestResponse jobInsList(String product, String jobId, int mode, Integer pageNumber,Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != jobId && !jobId.equals("")) {
				conditions.put("lJobId", jobId);
			}
			if(mode != 0) {
				conditions.put("jobMode", mode);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			PageInfo<Job> pageList = jobService.queryJobPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取数据源列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}

	/*public String getCurrentTime() {
		return CalendarUtil.getNowTime(CalendarUtil.DEFAULT_DATE_PATTERN);
	}*/

	@GetMapping("/task/list")
	public RestResponse getTaskListByJobId(String jobId, String taskId, Integer pageNumber,Integer pageSize) {
		if (StringUtil.isNullOrEmpty(jobId)) {
			return new RestResponse();
		}
		RestResponse response = new RestResponse();
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("jobId", jobId);
		if(null != taskId && "".equals(taskId)) taskId = null;
		condition.put("lTaskId", taskId);
		try {
			PageInfo<Task> pageList = taskService.queryTaskPage(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get task list failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping("/template/jobListIgnoreMode")
	public RestResponse getJobListIgnoreMode() {
		RestResponse response = new RestResponse();
		try {
			List<Job> list = jobService.getJobListIgnoreMode();
			response.put("rows", list);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get job list failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping("/template/saveTaskLean")
	public RestResponse saveTaskLean(String jobId, String taskId, String leanJobId, String leanTaskIds, String operationType) {
		RestResponse response = new RestResponse();
		try {
			if ("add".equals(operationType)) {
				// 新增task依赖
				taskService.saveTaskLean(jobId, taskId, leanJobId, leanTaskIds);
			}else {
				// 删除task依赖
				taskService.deleteTaskLean(jobId, taskId, leanTaskIds.split(","));
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("save task lean failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/task/selectJob")
	public RestResponse selectJob(String jobId, String taskId) {
		RestResponse response = new RestResponse();
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("jobId", jobId);
		condition.put("notEqTaskId", taskId);
		try {
			List<Task> taskList = taskService.getTaskList(condition);
			response.put("rows", taskList);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("select job failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping("/template/task/selectTaskLeanList")
	public String selectTaskLeanList(
			String jobId,
			String notEqTaskId,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("notEqTaskId", notEqTaskId);
			conditions.put("taskIdNotExiTaskLean", notEqTaskId);
			conditions.put("orderByTaskId","orderByTaskId");
			PageInfo<Task> pageList = taskService.queryTaskPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取数据源列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping("/template/task/beSelectTaskLeanList")
	public String beSelectTaskLeanList(
			String jobId,
			String taskId,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("taskId",taskId);
			conditions.put("leanJobIdIsNull","leanJobIdIsNull");
			conditions.put("orderByTaskId","orderByTaskId");
			PageInfo<TaskLean> pageList = taskService.queryTaskLeanPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取数据源列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping("/template/task/taskLeanList")
	public String taskLeanList(
			String jobId,
			String taskId,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("taskId", taskId);
			PageInfo<TaskLean> pageList = taskService.taskLeanPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取任务型依赖列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/template/task/getdmsnName")
	public RestResponse getdmsnName(String jobId, String taskId) {
		RestResponse response = new RestResponse();
		try {
            List<String> dmsnNameList = taskService.getDmsnNameList(jobId, taskId);
            if(null != dmsnNameList && dmsnNameList.size() > 0) {
            	response.put("dmsnNameList", dmsnNameList);
            }
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/task/taskBeleanList")
	public String taskBeleanList(
			String jobId,
			String taskId,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("taskId", taskId);
			PageInfo<TaskLean> pageList = taskService.taskBeleanPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取任务型依赖列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping("/template/task/taskSpecialLeanList")
	public RestResponse taskSpecialLeanList(String jobId,String taskId,String leanTaskId,Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("taskId", taskId);
			if(null != leanTaskId && !leanTaskId.equals("")) {
				conditions.put("leanTaskId", leanTaskId);
			}
			PageInfo<TaskSpecialLean> pageList = taskService.taskSpecialLeanPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取任务型依赖列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@DeleteMapping(value="/template/task/deleteTaskSpecialLean",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse deleteTaskSpecialLean(String jobId, String taskId, String specialLeanTaskIds) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			taskService.deleteTaskLean(jobId, taskId, null, specialLeanTaskIds.split(","));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete task special lean failed ",e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/task/selectTaskLeans")
	public String selectTaskLeans(
			String jobId,
			String notEqTaskId,
			String qTaskId,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("notEqTaskId", notEqTaskId);
			conditions.put("taskIdNotExiTaskLean", notEqTaskId);
			conditions.put("taskIdNotExiSpeTaskLean", notEqTaskId);
			conditions.put("orderByTaskId","orderByTaskId");
			if(null != qTaskId && !qTaskId.equals("")) {
				conditions.put("lTaskId",qTaskId);
			}
			PageInfo<Task> pageList = taskService.queryTaskPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取完整依赖/前驱任务项列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/template/selectTaskSpecialLeans", produces = MediaType.APPLICATION_JSON_VALUE)
	public String selectTaskSpecialLeans(String jobId,String taskId,String inputBeleanTaskId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("jobId", jobId);
			conditions.put("notEqTaskId", taskId);
			conditions.put("taskIdNotExiTaskLean", taskId);
			//conditions.put("taskIdNotExiSpeTaskLean", taskId);
			conditions.put("lTaskId",inputBeleanTaskId); //下拉框输入值
			conditions.put("orderByTaskId","orderByTaskId");
			List<Task> taskList = taskService.getTaskList(conditions);
			if(null != taskList && taskList.size() > 0) {
				response.put("taskList", taskList);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("--- 获取特殊依赖/前驱任务项下拉数据异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/template/selectTaskExp", produces = MediaType.APPLICATION_JSON_VALUE)
	public String selectTaskExp(String jobId,String taskId,String inputEntityName) {
		RestResponse response = new RestResponse();
		try {
			List<String> entityNameList = taskService.getEntityNameList(jobId, taskId, inputEntityName);
            if(null != entityNameList && entityNameList.size() > 0) {
            	response.put("entityNameList", entityNameList);
            }
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("--- 获取特殊依赖/任务项表达式下拉数据异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@PutMapping("/template/saveSelectTaskLeans")
	public RestResponse saveSelectTaskLeans(@RequestParam String jobId, @RequestParam String taskId, @RequestParam String leanJobId, @RequestParam String leanTaskIds, @RequestParam String qTaskId) {
		RestResponse response = new RestResponse();
		try {
			String tLeanTaskIds = "";
			if(null == leanTaskIds || leanTaskIds.equals("")) {//未选中任务项
				Map<String,Object> conditions = new HashMap<String,Object>();
				conditions.put("jobId", jobId);
				conditions.put("notEqTaskId", taskId);
				conditions.put("taskIdNotExiTaskLean", taskId);
				conditions.put("taskIdNotExiSpeTaskLean", taskId);
				if(null != qTaskId && !qTaskId.equals("")){
					conditions.put("lTaskId",qTaskId);
				}
				List<Task> taskList = taskService.getTaskList(conditions);
				if(null != taskList && taskList.size() > 0) {
					for(Task fTask : taskList) {
						tLeanTaskIds += fTask.getTaskId()+",";
					}
					tLeanTaskIds = tLeanTaskIds.substring(0, tLeanTaskIds.length()-1);
				}
			}else {
				tLeanTaskIds = leanTaskIds;
			}
			if(!tLeanTaskIds.equals("")) {
				taskService.saveTaskLean(jobId, taskId, leanJobId, tLeanTaskIds);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PutMapping(value="/template/saveSelectTaskSpecialLeans",produces=MediaType.APPLICATION_JSON_VALUE)
	public String saveSelectTaskSpecialLeans(@RequestBody List<TaskSpecialLean> taskSpecialLeanList) {
		RestResponse response = new RestResponse();
		try {
			taskService.saveTaskSpecialLean(taskSpecialLeanList);
			response.setSuccess(true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@DeleteMapping(value="/template/task/delTaskLean",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String delTaskLean(String jobId,String taskId,String leanTaskIds,String specialLeanTaskIds) {
		RestResponse response = new RestResponse();
		try {
			taskService.deleteTaskLean(jobId,taskId,leanTaskIds.split(","),specialLeanTaskIds.split(","));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delTaskLean failed ",e);
			response.setSuccess(false);
			response.setInfo("删除失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping("/template/copyAddJob")
	public RestResponse copyAddJob(@RequestBody List<Job> list){
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		if (list.size() != 2) {
			response.setInfo("页面传递的参数异常");
			return response;
		}
		try{
			jobService.copyAddJob(list.get(0), list.get(1));
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("copy job failed, jobId:{}", list.get(0).getJobId(), e);
			StringBuffer info = new StringBuffer();
			info.append("copy job '").append(list.get(0).getJobId()).append("' failed");
			response.setSuccess(false);
			response.setInfo(info.toString());
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/queryJobExeInfo")
	public RestResponse queryJobExeInfo(String product, String jobId) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> condition = new HashMap<>();
			condition.put("product", product);
			condition.put("jobId", jobId);
			condition.put("status", 1);
			SysUser user = UserUtils.getCurLoginSysUser();
			condition.put("creator", user.getLoginName());
			PageInfo<JobExeInfo> pageList = jobExeInfoService.queryJobExeInfoPage(condition, 1, 10);
			response.put("rows", pageList.getList());
			response.put("total", pageList.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("获取job模板历史记录异常");
		}
		return response;
	}
	
	@GetMapping("/template/getJobExeInfoSelect")
	public RestResponse getJobExeInfoSelect(Integer pageNumber, Integer pageSize, String infoId, String jobId, String filterTaskId) {
		RestResponse response = new RestResponse();
		try {
			PageInfo<Task> list = jobExeInfoService.getJobExeInfoSelect(pageNumber, pageSize, infoId, jobId, filterTaskId);
			response.put("rows", list.getList());
			response.put("total", list.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("获取task模板历史记录异常");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/getJobExeInfoUnselect")
	public RestResponse getJobExeInfoUnselect(Integer pageNumber, Integer pageSize, String infoId, String jobId, String filterTaskId) {
		RestResponse response = new RestResponse();
		try {
			PageInfo<Task> list = jobExeInfoService.getJobExeInfoUnselect(pageNumber, pageSize, infoId, jobId, filterTaskId);
			response.put("rows", list.getList());
			response.put("total", list.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("获取task模板历史记录异常");
		}
		return response;
	}
	
	@GetMapping("/template/runJobTemplateHistory")
	public RestResponse runJobTemplateHistory(String infoParameter) {
		RestResponse response = new RestResponse();
		try {
			Long rs = jobExeInfoService.insertJobExeInfo(infoParameter);
			response.setSuccess(true);
			response.put("jobInsId", rs);
		} catch (Exception e) {
			e.printStackTrace();
			response.setSuccess(false);
			response.setInfo("作业运行失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/addTempJobExeInfo")
	public RestResponse addTempJobExeInfo(String infoId, String jobId) {
		RestResponse response = new RestResponse();
		try {
			jobExeInfoService.addTempJobExeInfo(infoId, jobId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			e.printStackTrace();
			response.setInfo("新增失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/addJobExeDetail")
	public RestResponse addJobExeDetail(String product, String jobId, String taskIds) {
		RestResponse response = new RestResponse();
		try {
			String infoId = jobExeInfoService.addJobExeDetail(product, jobId, taskIds);
			response.put("infoId", infoId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("新增运行task失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping("/template/removeJobExeDetail")
	public RestResponse removeJobExeDetail(String taskIds) {
		RestResponse response = new RestResponse();
		try {
			String infoId = jobExeInfoService.removeJobExeDetail(taskIds);
			response.put("infoId", infoId);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("删除运行task失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/topRecord")
	public RestResponse topRecord(String infoId, boolean flag) {
		RestResponse response = new RestResponse();
		try {
			jobExeInfoService.topRecord(infoId, flag);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("置顶/取消置顶失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/editJobExeInfo")
	public RestResponse editJobExeInfo(String infoId, String desc) {
		RestResponse response = new RestResponse();
		try {
			jobExeInfoService.editJobExeInfo(infoId, desc);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("修改描述失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/template/getAllJob")
	public RestResponse getAllJob(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<Job> list = jobService.getJobList(conditions);
			response.put("jobIds", list);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setInfo("获取Job模板列表出错");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value = "/template/selectWwJob", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse selectWwJob(String product,String exclusionJobIds) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != exclusionJobIds && !exclusionJobIds.equals("")) {
				conditions.put("exclusionJobIdArr", exclusionJobIds.split(","));
			}
			conditions.put("orderBy_productJobId_asc","orderBy_productJobId_asc");
			List<Job> jobs = this.jobService.getJobList(conditions);
			response.put("jobs", jobs); //product 对应JOB
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get plugins failed, product:{}", product, e);
			response.setSuccess(false);
			response.setInfo("获取任务下拉框数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value = "/template/getWwJob", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getWwJob(String product,String includeJobIds) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != includeJobIds && !includeJobIds.equals("")) {
				conditions.put("includeJobIdArr", includeJobIds.split(","));
			}
			conditions.put("orderBy_productJobId_asc","orderBy_productJobId_asc");
			List<Job> jobs = this.jobService.getJobList(conditions);
			response.put("jobs", jobs); //product 对应JOB
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get plugins failed, product:{}", product, e);
			response.setSuccess(false);
			response.setInfo("获取JOB失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/template/getJobParameter",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse getJobParameter(String jobId){
		RestResponse response = new RestResponse();
		try{
			Map<String, Object> condition = new HashMap<String,Object>();
			if(null != jobId && !jobId.equals("")) {
				condition.put("jobId", jobId);
				condition.put("sourceId", jobId);
				List<Parameter> jobParameters = this.parameterService.getParameterList(condition);
				if(null != jobParameters && jobParameters.size()>0) {
					response.put("jobParameters", jobParameters);
				}
			}
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("get job parameter failed, jobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo("[" + jobId + "] get job parameter failed");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/template/expotJobTemplate")
	public void expotJobTemplate(HttpServletResponse response,String exportFileName,String jobIds,String exportFileType,String exportFileCatalog,String qProduct,String qJobId,int qMode) {
		try{
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			if(null != jobIds && !jobIds.equals("")) {
				conditions.put("includeJobIdArr", jobIds.split(","));
			}else {
				conditions.put("product", qProduct);
				if(null != qJobId && !qJobId.equals("")) {
					conditions.put("lJobId", qJobId);
				}
				if(qMode != 0) {
					conditions.put("jobMode", qMode);
				}
			}
			conditions.put("orderBy_productJobId_asc", "orderBy_productJobId_asc");
			List<Job> jobList = jobService.getJobList(conditions);
			ExportJob exportJob = jobService.getExportJob(exportFileCatalog,jobList);
			response.setHeader("content-disposition", "attachment;filename="+exportFileName+exportFileType);
			if(exportFileType.equals(".xls")) {
				jobService.genExcelFile(response, exportJob, exportFileName);
			}else {
				jobService.genSqlFile(response, exportJob, exportFileName, driverClass);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@GetMapping(value="/template/getUploadFileMaxSize",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getUploadFileMaxSize() {
		RestResponse response = new RestResponse();
		try {
			response.put("uploadFileMaxSizeMB", uploadFileMaxSize); //前端提醒
			String tUploadFileMaxSize = uploadFileMaxSize.replace("MB", "");
			response.put("uploadFileMaxSize", Long.parseLong(tUploadFileMaxSize)*1024*1024);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get upload file max size failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return response;
	}
	
	@PostMapping(value="/template/importJobTemplate")
	public String importJobTemplate(@RequestParam("uProduct") String uProduct,@RequestParam("uFile") MultipartFile multipartFile) {
		RestResponse response = new RestResponse();
		ImportJob importJob = null;
		String uId = "";
		String startTime = "";
		String backEndTime = "";
		JobUpload jobUpload = null;
		String msg = "";
		try{
			startTime = CalendarUtil.fmtDate(new Date(), "HH:mm:ss");
			importJob = jobService.getImportJob(uProduct,multipartFile);
			//1.备份上传文件
			uId = UUID.randomUUID().toString().replaceAll("-", "");
			String uploadPath = UploadUtils.getUploadPath(fileReleasePath, UserUtils.getCurLoginSysUser().getLoginName(), uId);
			String uploadFileName = multipartFile.getOriginalFilename();
			UploadUtils.backUpExcelFile(uploadPath,uploadFileName,multipartFile);
			//2.备份数据库任务模板涉及数据
			List<Job> jobList = importJob.getJobList();
			String jobIdArr[] = new String[jobList.size()];
			StringBuffer sb = new StringBuffer();
			for(int x=0;x<jobList.size();x++) {
				jobIdArr[x] = jobList.get(x).getJobId();
				if (x == jobIdArr.length-1) {
					sb.append(jobIdArr[x]);
				}else {
					sb.append(jobIdArr[x]).append(",");
				}
			}
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("includeJobIdArr", jobIdArr);
			jobList = jobService.getJobList(conditions);
			ExportJob exportJob = jobService.getExportJob(ExportJob.EXPORT_FILE_CATALOG_ALL_VALUE,jobList);
			String backFileName = uploadFileName.substring(0, uploadFileName.lastIndexOf(".")) + ".sql";
			jobService.genSqlFile(uploadPath, exportJob, backFileName, driverClass);
			//记录上传日志
			backEndTime = CalendarUtil.fmtDate(new Date(), "HH:mm:ss");
			msg = "导入任务模板："+sb.toString()+"\r\n";
			msg += startTime+"-"+backEndTime+" 备份任务模板成功"+"\r\n";
			jobUpload = new JobUpload(uId,uProduct,uploadFileName,backFileName,msg,UserUtils.getCurLoginSysUser().getLoginName(),new Date(),JobUpload.JOBUPLOAD_STATUS_PROCESSING);
			jobUploadService.insertJobUpload(jobUpload);
			//3.插入任务模板涉及数据
		    jobService.insertImportJob(importJob);
		    String finishTime = CalendarUtil.fmtDate(new Date(), "HH:mm:ss");
		    msg += backEndTime +"-"+finishTime+" 插入任务模板成功";
		    jobUpload.setStatus(JobUpload.JOBUPLOAD_STATUS_SUCCESS);
		    jobUpload.setMsg(msg);
		    jobUploadService.updateJobUpload(jobUpload);
			response.setSuccess(true);
		}catch(Exception e){
			if(null != jobUpload) {
				String errorTime = CalendarUtil.fmtDate(new Date(), "HH:mm:ss");
			    msg += backEndTime +"-"+errorTime+" 插入任务模板失败"+"\r\n";
			    msg += e.getMessage()+importJob.getErrorInfo();
			    jobUpload.setStatus(JobUpload.JOBUPLOAD_STATUS_FAIL);
			    jobUpload.setMsg(msg);
			    jobUploadService.updateJobUpload(jobUpload);
			}
			response.setSuccess(false);
			response.setInfo(e.getMessage());
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
}
