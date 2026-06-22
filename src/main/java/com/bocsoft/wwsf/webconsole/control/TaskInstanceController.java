package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.JobState;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.model.TaskInstance;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.bocsoft.wwsf.webconsole.service.TaskInstanceService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.PageInfo;

@RestController
public class TaskInstanceController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TaskInstanceService taskInstanceService;
	
	@Autowired
	private WwseService wwseService;
	
	@Autowired
	private SysLogService sysLogService;
	
	@GetMapping("/taskIns/list")
	public String taskInsList(
			String product,
			Long jobInsId,
			String taskId,
			String taskInsId,
			Integer state, 
			Integer pageNumber, Integer pageSize) {
		
		RestResponse response = new RestResponse();
		
		if(StringUtil.isNullOrEmpty(product)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		
		Map<String, Object> conditons = new HashMap<>();
		conditons.put("jobInsId", jobInsId);
		conditons.put("state", state);
		conditons.put("lTaskId", taskId);
		conditons.put("lTaskInsId", taskInsId);
		
		try {
			PageInfo<TaskInstance> taskInsList = taskInstanceService.taskInsList(conditons, pageNumber, pageSize);
			response.put("rows", taskInsList.getList());
			response.put("total", taskInsList.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/taskIns",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getTaskIns(String product, String jobInsId, String taskInsId) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(jobInsId)){
			response.setSuccess(false);
			response.setInfo("参数无效[job实例编号]");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(taskInsId)){
			response.setSuccess(false);
			response.setInfo("参数无效[task实例编号]");
			return JSON.toJSONString(response);
		}

		try {
			TaskInstance task = taskInstanceService.selectByPrimaryKey(Long.valueOf(jobInsId), taskInsId);
			if (task != null) {
				task = taskInstanceService.setTaskInsLean(task);
				response.put("task", task);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get task instance failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/taskIns/saveTaskIns",produces=MediaType.APPLICATION_JSON_VALUE)
	public String saveTaskIns(String formJson, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		
		Map<String, Object> formMap = JSONObject.parseObject(formJson, java.util.Map.class);
	
		if(StringUtil.isNullOrEmpty(formMap.get("product"))){
			response.setSuccess(false);
			response.setInfo("产品号不能为空");
			return JSON.toJSONString(response);
		}
		
		if(StringUtil.isNullOrEmpty(formMap.get("jobInsId"))){
			response.setSuccess(false);
			response.setInfo("job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		
		if(StringUtil.isNullOrEmpty(formMap.get("taskInsId"))){
			response.setSuccess(false);
			response.setInfo("task实例编号不能为空");
			return JSON.toJSONString(response);
		}
		
		long jobInsId = 0L;
		try{
			jobInsId =  Long.parseLong((String)formMap.get("jobInsId"));
		}catch(Exception e){
			logger.error("save task instance failed ",e);
			response.setSuccess(false);
			response.setInfo("job实例编号不是LONG类型");
			response.setDetailInfo(StringUtil.stringifyException(e));
			return JSON.toJSONString(response);
		}
		
		String taskInsId = (String)formMap.get("taskInsId");
		try {
			Map<String, Object> conditions = new HashMap<>();
			conditions.put("jobInsId", jobInsId);
			conditions.put("taskInsId", taskInsId);
			conditions.put("errorDelay", Integer.parseInt((String)formMap.get("errorDelay")));
			conditions.put("maxNumOfExeErrors", Integer.parseInt((String)formMap.get("maxNumOfExeErrors")));
			conditions.put("currentErrorExeCount", Integer.parseInt((String)formMap.get("currentErrorExeCount")));
			conditions.put("agentScope", (String)formMap.get("agentScope"));
			conditions.put("period", (String)formMap.get("period"));
			conditions.put("nextExecuteTime", (String)formMap.get("nextExecuteTime"));
			conditions.put("workingDay", (String)formMap.get("workingDay"));
			
			Map<String, Object> map = (Map<String, Object>) formMap.get("parameters");
			conditions.put("parameters", map.toString());
			
			taskInstanceService.saveTaskIns(conditions);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("save task instance failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("保存");
			sysLog.setLogDesc(String.format("修改job实例[%s]下的task实例：[%s]:errorDelay=%d, maxNumOfExeErrors=%d, currentErrorExeCount=%d,"
					+ "agentScope=%s, period=%s, nextExecuteTime=%s, workingDay=%s, parameters=%s", 
					jobInsId,
					taskInsId,
					Integer.parseInt((String)formMap.get("errorDelay")),
					Integer.parseInt((String)formMap.get("maxNumOfExeErrors")),
					Integer.parseInt((String)formMap.get("currentErrorExeCount")),
					(String)formMap.get("agentScope"),
					(String)formMap.get("period"),
					(String)formMap.get("nextExecuteTime"),
					(String)formMap.get("workingDay"),
					((Map<String, Object>)formMap.get("parameters")).toString()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/jobInsAndTaskInsPage")
	@RequiresPermissions("menu:batchTask-list")
	public String jobInsAndTaskInsPage(String product,String batchNo,String jobId,String jobInsId,String taskId,String taskInsId,Integer score,String taskTitle,Integer pageSize,Integer pageNumber){
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != batchNo && !batchNo.equals("")) {
				conditions.put("batch", batchNo);
			}
			if(null != jobId && !jobId.equals("")) {
				conditions.put("lJobId", jobId);
			}
			if(null != jobInsId && !jobInsId.equals("")) {
				conditions.put("lJobInsId", jobInsId);
			}
			if(null != taskTitle && !taskTitle.equals("")) {
				conditions.put("lTaskTitle", taskTitle);
			}
			if(null != taskId && !taskId.equals("")) {
				conditions.put("lTaskId", taskId);
			}
			if(null != taskInsId && !taskInsId.equals("")) {
				conditions.put("lTaskInsId", taskInsId);
			}
			if(score > 0) {
				conditions.put("taskState", score);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			PageInfo<Map<String,Object>> pageList = taskInstanceService.jobInsAndTaskInsList(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取查询列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	
	@PutMapping(value="/taskIns/kill",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobKill(@RequestParam Long jobInsId, @RequestParam String taskInsId, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(taskInsId)){
			response.setSuccess(false);
			response.setInfo("Task实例编号不能为空");
			return JSON.toJSONString(response);
		}
		try {
			wwseService.jobKill(jobInsId, taskInsId);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("{} kill task error {}", jobInsId, taskInsId, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		SysLog sysLog = null;
		try {
			sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("中断");
			sysLog.setLogDesc("中断job实例["+jobInsId+"]下的task实例：["+taskInsId+"]");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("记录用户日志异常：" + (sysLog == null ? "" : sysLog.toString()),e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/taskIns/taskRerun",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskRerun(@RequestParam String product, @RequestParam Long jobInsId, @RequestParam String taskInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		try {
			if(StringUtil.isNullOrEmpty(taskInsIds)){
				response.setSuccess(false);
				response.setInfo("Task实例编号不能为空");
				return JSON.toJSONString(response);
			}else{
				String[] taskInsIdArr = taskInsIds.split("\\|");
				//recursion: 值为false时,只重新运行当前任务；值为true时，递归重新运行当前任务
				//exceptRunning: 值为false时，排除正在运行的task
				wwseService.jobRerun(jobInsId, taskInsIdArr, false, false);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("taskRerun product:{}, jobInsId:{}, taskInsIds:{}", product,String.valueOf(jobInsId),taskInsIds, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("重跑");
			sysLog.setLogDesc("重跑的job实例["+jobInsId+"]下的task实例：["+taskInsIds+"]");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/taskIns/taskRecursionRerun",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskRecursionRerun(@RequestParam String product, @RequestParam Long jobInsId, @RequestParam String taskInsIds) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		try {
			if(StringUtil.isNullOrEmpty(taskInsIds)){
				response.setSuccess(false);
				response.setInfo("Task实例编号不能为空");
				return JSON.toJSONString(response);
			}else{
				String[] taskInsIdArr = taskInsIds.split("\\|");
				//recursion: 值为false时,只重新运行当前任务；值为true时，递归重新运行当前任务
				//exceptRunning: 值为false时，排除正在运行的task
				wwseService.jobRerun(jobInsId, taskInsIdArr, true, false);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("taskRecursionRerun product:{}, jobInsId:{}, taskInsIds:{}", product,String.valueOf(jobInsId),taskInsIds, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/taskIns/taskDisabled",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskDisabled(@RequestParam String product, @RequestParam Long jobInsId, @RequestParam String taskInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		try {
			if(StringUtil.isNullOrEmpty(taskInsIds)){
				response.setSuccess(false);
				response.setInfo("Task实例编号不能为空");
				return JSON.toJSONString(response);
			}else{
				String[]  taskInsIdArr = taskInsIds.split("\\|");
				wwseService.jobDisabled(jobInsId, taskInsIdArr);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("taskDisabled product:{}, jobInsId:{}, taskInsIds:{}", product,String.valueOf(jobInsId),taskInsIds, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("设为通过");
			sysLog.setLogDesc("将job实例["+jobInsId+"]下的 task实例：["+taskInsIds+"] 设为通过");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	//--------------菜单：Batch start-------------------
	//选中记录taskRerun
	@PutMapping(value="/taskIns/taskRerunBatchSelect",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskRerunBatchSelect(@RequestParam String pjts) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Map<String,List<String>>> productMap = new HashMap<String,Map<String,List<String>>>(); //第一个key=product,第二个key=jobInsId,List<String>=taskInsId列表
			String[] pjtArr = pjts.split("\\|");   //pjts值格式：rowData|rowData|rowData
			if(null != pjtArr && pjtArr.length>0) {
				for(String fPjt : pjtArr) {
					String fpjRowArr[] = fPjt.split(";");  //fPjt值格式：产品:jobInsId:taskInsId
					String product = fpjRowArr[0];
					String jobInsId = fpjRowArr[1];
					String taskInsId = fpjRowArr[2];
					Map<String,List<String>> jobInsMap = null;
					if(null == productMap.get(product)) {
						jobInsMap = new HashMap<String,List<String>>();
					}else {
						jobInsMap = productMap.get(product);
					}
					List<String> taskInsIdList = null;
					if(null == jobInsMap.get(jobInsId)) {
						taskInsIdList = new ArrayList<String>();
					}else {
						taskInsIdList = jobInsMap.get(jobInsId);
					}
					taskInsIdList.add(taskInsId);
					jobInsMap.put(jobInsId, taskInsIdList);
					productMap.put(product,jobInsMap);
				}
				for(Map.Entry<String, Map<String,List<String>>> fProductMap : productMap.entrySet()) {
					Map<String,List<String>> jobInsMap = fProductMap.getValue();
					for(Map.Entry<String, List<String>> fJobInsMap : jobInsMap.entrySet()){
						List<String> taskInsIdList = fJobInsMap.getValue();
						String taskInsIdArr[] = new String[taskInsIdList.size()];
						taskInsIdArr = taskInsIdList.toArray(taskInsIdArr);
						wwseService.jobRerun(Long.parseLong(fJobInsMap.getKey()), taskInsIdArr, false, false);
						//logger.info("taskRerunBatchSelect: "+fProductMap.getKey()+" "+fJobInsMap.getKey()+" "+Arrays.toString(taskInsIdArr));
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("rerun select batch task failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//未选中记录taskRerun（根据查询条件taskRerun）
	@PutMapping(value="/taskIns/taskRerunBatchQuery",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskRerunBatchQuery(@RequestParam String product,@RequestParam String batchNo,@RequestParam String jobId,@RequestParam Long jobInsId,@RequestParam String taskId,@RequestParam String taskInsId,@RequestParam Integer score,@RequestParam String taskTitle) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != batchNo && !batchNo.equals("")) {
				conditions.put("batch", batchNo);
			}
			if(null != jobId && !jobId.equals("")) {
				conditions.put("lJobId", jobId);
			}
			if(null != jobInsId && jobInsId > 0) {
				conditions.put("lJobInsId", jobInsId);
			}
			if(null != taskId && !taskId.equals("")) {
				conditions.put("lTaskId", taskId);
			}
			if(null != taskInsId && !taskInsId.equals("")) {
				conditions.put("lTaskInsId", taskInsId);
			}
			if(null != score && score > 0) {
				conditions.put("taskState", score);
			}
			if(null != taskTitle && !taskTitle.equals("")) {
				conditions.put("lTaskTitle", taskTitle);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<Map<String,Object>> taskInsList = taskInstanceService.jobInsAndTaskInsList(conditions);
			if(null != taskInsList && taskInsList.size()>0) {
				Map<String,Map<String,List<String>>> productMap = new HashMap<String,Map<String,List<String>>>(); //第一个key=product,第二个key=jobInsId,List<String>=taskInsId列表
				for(Map<String,Object> fTaskInsMap : taskInsList) {
					String tProduct = fTaskInsMap.get("product").toString();
					String tJobInsId = fTaskInsMap.get("job_ins_id").toString();
					String tTaskInsId = fTaskInsMap.get("task_ins_id").toString();
					Map<String,List<String>> jobInsMap = null;
					if(null == productMap.get(tProduct)) {
						jobInsMap = new HashMap<String,List<String>>();
					}else {
						jobInsMap = productMap.get(tProduct);
					}
					List<String> taskInsIdList = null;
					if(null == jobInsMap.get(tJobInsId)) {
						taskInsIdList = new ArrayList<String>();
					}else {
						taskInsIdList = jobInsMap.get(tJobInsId);
					}
					taskInsIdList.add(tTaskInsId);
					jobInsMap.put(tJobInsId, taskInsIdList);
					productMap.put(tProduct,jobInsMap);
				}
				for(Map.Entry<String, Map<String,List<String>>> fProductMap : productMap.entrySet()) {
					Map<String,List<String>> jobInsMap = fProductMap.getValue();
					for(Map.Entry<String, List<String>> fJobInsMap : jobInsMap.entrySet()){
						List<String> taskInsIdList = fJobInsMap.getValue();
						String taskInsIdArr[] = new String[taskInsIdList.size()];
						taskInsIdArr = taskInsIdList.toArray(taskInsIdArr);
						wwseService.jobRerun(Long.parseLong(fJobInsMap.getKey()), taskInsIdArr, false, false);
						//logger.info("taskRerunBatchQuery: "+fProductMap.getKey()+" "+fJobInsMap.getKey()+" "+Arrays.toString(taskInsIdArr));
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("rerun query batch task failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//选中记录taskDisabled
	@PutMapping(value="/taskIns/taskDisabledBatchSelect",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskDisabledBatchSelect(@RequestParam String pjts) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Map<String,List<String>>> productMap = new HashMap<String,Map<String,List<String>>>(); //第一个key=product,第二个key=jobInsId,List<String>=taskInsId列表
			String[] pjtArr = pjts.split("\\|");   //pjts值格式：rowData|rowData|rowData
			if(null != pjtArr && pjtArr.length>0) {
				for(String fPjt : pjtArr) {
					String fpjRowArr[] = fPjt.split(";");  //fPjt值格式：产品:jobInsId:taskInsId
					String product = fpjRowArr[0];
					String jobInsId = fpjRowArr[1];
					String taskInsId = fpjRowArr[2];
					Map<String,List<String>> jobInsMap = null;
					if(null == productMap.get(product)) {
						jobInsMap = new HashMap<String,List<String>>();
					}else {
						jobInsMap = productMap.get(product);
					}
					List<String> taskInsIdList = null;
					if(null == jobInsMap.get(jobInsId)) {
						taskInsIdList = new ArrayList<String>();
					}else {
						taskInsIdList = jobInsMap.get(jobInsId);
					}
					taskInsIdList.add(taskInsId);
					jobInsMap.put(jobInsId, taskInsIdList);
					productMap.put(product,jobInsMap);
				}
				for(Map.Entry<String, Map<String,List<String>>> fProductMap : productMap.entrySet()) {
					Map<String,List<String>> jobInsMap = fProductMap.getValue();
					for(Map.Entry<String, List<String>> fJobInsMap : jobInsMap.entrySet()){
						List<String> taskInsIdList = fJobInsMap.getValue();
						String taskInsIdArr[] = new String[taskInsIdList.size()];
						taskInsIdArr = taskInsIdList.toArray(taskInsIdArr);
						wwseService.jobDisabled(Long.parseLong(fJobInsMap.getKey()), taskInsIdArr);
						//logger.info("taskDisabledBatchSelect: "+fProductMap.getKey()+" "+fJobInsMap.getKey()+" "+Arrays.toString(taskInsIdArr));
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("disabled select batch task failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//未选中记录taskDisabled（根据查询条件taskRerun）
	@PutMapping(value="/taskIns/taskDisabledBatchQuery",produces=MediaType.APPLICATION_JSON_VALUE)
	public String taskDisabledBatchQuery(@RequestParam String product,@RequestParam String batchNo,@RequestParam String jobId,@RequestParam Long jobInsId,@RequestParam String taskId,@RequestParam String taskInsId,@RequestParam String taskTitle) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != batchNo && !batchNo.equals("")) {
				conditions.put("batch", batchNo);
			}
			if(null != jobId && !jobId.equals("")) {
				conditions.put("lJobId", jobId);
			}
			if(null != jobInsId && jobInsId > 0) {
				conditions.put("lJobInsId", jobInsId);
			}
			if(null != taskId && !taskId.equals("")) {
				conditions.put("lTaskId", taskId);
			}
			if(null != taskInsId && !taskInsId.equals("")) {
				conditions.put("lTaskInsId", taskInsId);
			}
			conditions.put("taskStateNotPass", 1);	
			if(null != taskTitle && !taskTitle.equals("")) {
				conditions.put("lTaskTitle", taskTitle);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<Map<String,Object>> taskInsList = taskInstanceService.jobInsAndTaskInsList(conditions);
			if(null != taskInsList && taskInsList.size()>0) {
				Map<String,Map<String,List<String>>> productMap = new HashMap<String,Map<String,List<String>>>(); //第一个key=product,第二个key=jobInsId,List<String>=taskInsId列表
				for(Map<String,Object> fTaskInsMap : taskInsList) {
					String tProduct = fTaskInsMap.get("product").toString();
					String tJobInsId = fTaskInsMap.get("job_ins_id").toString();
					String tTaskInsId = fTaskInsMap.get("task_ins_id").toString();
					Map<String,List<String>> jobInsMap = null;
					if(null == productMap.get(tProduct)) {
						jobInsMap = new HashMap<String,List<String>>();
					}else {
						jobInsMap = productMap.get(tProduct);
					}
					List<String> taskInsIdList = null;
					if(null == jobInsMap.get(tJobInsId)) {
						taskInsIdList = new ArrayList<String>();
					}else {
						taskInsIdList = jobInsMap.get(tJobInsId);
					}
					taskInsIdList.add(tTaskInsId);
					jobInsMap.put(tJobInsId, taskInsIdList);
					productMap.put(tProduct,jobInsMap);
				}
				for(Map.Entry<String, Map<String,List<String>>> fProductMap : productMap.entrySet()) {
					Map<String,List<String>> jobInsMap = fProductMap.getValue();
					for(Map.Entry<String, List<String>> fJobInsMap : jobInsMap.entrySet()){
						List<String> taskInsIdList = fJobInsMap.getValue();
						String taskInsIdArr[] = new String[taskInsIdList.size()];
						taskInsIdArr = taskInsIdList.toArray(taskInsIdArr);
						wwseService.jobDisabled(Long.parseLong(fJobInsMap.getKey()), taskInsIdArr);
						//logger.info("taskDisabledBatchQuery: "+fProductMap.getKey()+" "+fJobInsMap.getKey()+" "+Arrays.toString(taskInsIdArr));
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("disabled query batch task failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//--------------菜单：Batch end-------------------
	
	@GetMapping("/taskIns/getTaskByBatchAndProduct")
	public RestResponse getTaskByBatchAndProduct(String product, String batch) {
		RestResponse response = new RestResponse();
		try {
			// 查询这个批次下的所有Task实例
			Map<String, Object> condition = new HashMap<>();
			condition.put("product", product);
			condition.put("batch", batch);
			List<Map<String, Object>> list = taskInstanceService.getTaskByBatchAndProduct(condition);
			// 根据标签统计出各个Task实例状态
			Map<String, Object> map = taskInstanceService.countTaskStateByTitle(list);
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				response.put(entry.getKey(), entry.getValue());
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get task failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/getTaskInsInfo",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getTaskInsInfo(Long jobInsId, String taskInsId) {
		RestResponse response = new RestResponse();
		if(jobInsId == null || jobInsId <= 0){
			response.setSuccess(false);
			response.setInfo("参数无效[job实例编号]");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(taskInsId)){
			response.setSuccess(false);
			response.setInfo("参数无效[task实例编号]");
			return JSON.toJSONString(response);
		}
		
		try {
			TaskInstance task = taskInstanceService.selectByPrimaryKey(jobInsId, taskInsId);
			if (task != null) {
				if (StringUtil.hasText(task.getInformation())) {
					response.put("information", task.getInformation());
				} else if (task.getState() == JobState.T_STATE_RUNNING) {
					String information = wwseService.showTaskRuntimeLog(jobInsId, taskInsId);
					if (StringUtil.hasText(information)) {
						response.put("information", information);
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get task information failed ",e);
			response.setSuccess(false);
			response.setInfo("获取日志失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
}
