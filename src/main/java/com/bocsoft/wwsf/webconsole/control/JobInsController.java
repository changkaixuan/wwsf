package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.JobInstance;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.JobInstanceService;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.PageInfo;

@RestController
public class JobInsController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JobInstanceService jobInstanceService;
	@Autowired
	private WwseService wwseService;
	@Autowired
	private SysLogService sysLogService;
	
	@GetMapping("/jobIns/list")
	@RequiresPermissions("menu:jobIns-list")
	public String jobInsList(
			String product,
			String jobId,
			String jobInsId,
			String batch,
			Integer state, 
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> conditions = new HashMap<String, Object>();
			conditions.put("product", product);
			conditions.put("lJobId", jobId);
			conditions.put("lJobInsId", jobInsId);
			conditions.put("lBatch", batch);
			conditions.put("state", state);
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			
			PageInfo<JobInstance> pageList = jobInstanceService.jobInsList(conditions, pageNumber, pageSize);
			response.put("rows", pageList.getList());
			response.put("total", pageList.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobRerunfailed",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobRerunfailed(@RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] jobInsIdArr = jobInsIds.split("\\|");
		
		try {
			jobInstanceService.jobRerunfailed(jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("{} rerun failed tasks error", jobInsIds, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("失败重运行");
			sysLog.setLogDesc("运行job实例["+jobInsIds+"]下的所有失败task实例");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobRerunUnpassed",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobRerunUnpassed(@RequestParam String products, @RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.jobRerunUnpassed(productArr, jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("job Rerun Unpassed failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("未通过重运行");
			sysLog.setLogDesc("运行job实例["+jobInsIds+"]下的所有未通过task实例");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobRerun",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobRerun(@RequestParam String products, @RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.jobRenovate(productArr, jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("job rerun failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("重运行");
			sysLog.setLogDesc("重运行job实例["+jobInsIds+"]下所有task实例");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobDisabled",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobDisabled(@RequestParam String products, @RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.jobDisabled(productArr, jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("job disabled failed", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("设为通过");
			sysLog.setLogDesc("将job实例["+jobInsIds+"]下所有task实例设为通过");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobPause",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobPause(@RequestParam String products, @RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.jobPause(productArr, jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("job pause failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("暂停");
			sysLog.setLogDesc("暂停job实例["+jobInsIds+"]下所有运行的task实例");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/jobContinue",produces=MediaType.APPLICATION_JSON_VALUE)
	public String jobContinue(@RequestParam String products, @RequestParam String jobInsIds, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.jobContinue(productArr, jobInsIdArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("job continue failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("继续运行");
			sysLog.setLogDesc("继续运行job实例["+jobInsIds+"]下的所有暂停的task实例");
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/jobIns/jobDriver")
	public String jobDriver(@RequestBody JSONArray jobs, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		String product = null;
		Long jobInsId = 0l;
		try {
			if (jobs != null && jobs.size() > 0) {
				List<Long> ids = new ArrayList<Long>();
				Iterator<Object> oit = jobs.iterator();
				while (oit.hasNext()) {
					Object o = oit.next();
					if (o instanceof Map) {
						Map<?,?> oj = (Map<?,?>)o;
						ids.add(Long.valueOf(String.valueOf(oj.get("jobInsId"))));
					}
				}
				jobInstanceService.jobDriver(ids.toArray(new Long[0]));
			} else {
				response.setSuccess(false);
				response.setInfo("参数错误");
				return JSON.toJSONString(response);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("jobDriver product:{}, jobInsId:{}", product, String.valueOf(jobInsId), e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping(value="/jobIns/{products}/{jobInsIds}",produces=MediaType.APPLICATION_JSON_VALUE)
	public String delJobIns(@PathVariable String products,@PathVariable String jobInsIds) {
		RestResponse response = new RestResponse();
		
		if(StringUtil.isNullOrEmpty(products)){
			response.setSuccess(false);
			response.setInfo("产品不能为空");
			return JSON.toJSONString(response);
		}
		if(StringUtil.isNullOrEmpty(jobInsIds)){
			response.setSuccess(false);
			response.setInfo("Job实例编号不能为空");
			return JSON.toJSONString(response);
		}
		String[] productArr = products.split("\\|");
		String[] jobInsIdArr = jobInsIds.split("\\|");
		if(productArr.length != jobInsIdArr.length){
			response.setSuccess(false);
			response.setInfo("产品与job实例号不对应");
			return JSON.toJSONString(response);
		}
		try {
			jobInstanceService.delJobIns(products, jobInsIds);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete job instance failed", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/jobIns",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getJobIns(String product, String jobInsId) {
		RestResponse response = new RestResponse();
		if (StringUtil.isNullOrEmpty(jobInsId)) {
			response.setSuccess(false);
			response.setInfo("参数无效[job实例编号]");
			return JSON.toJSONString(response);
		}
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("product", product);
		conditions.put("jobInsId", jobInsId);
		try {
			response.put("job", jobInstanceService.getJobInstance(conditions));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get failed, jobInsId:{}", jobInsId, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping(value="/jobIns/saveJobIns",produces=MediaType.APPLICATION_JSON_VALUE)
	public String saveJobIns(String product, String jobInsId, String state, String pause, String properties, String parameters, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(jobInsId)){
			response.setSuccess(false);
			response.setInfo("参数无效[job实例编号]");
			return JSON.toJSONString(response);
		}
		
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("jobInsId", jobInsId);
		conditions.put("properties", properties);
		conditions.put("parameters", parameters);
		conditions.put("state", state);
		conditions.put("pause", pause);
		
		try {
			jobInstanceService.saveJobIns(conditions);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get failed, jobInsId:{}", jobInsId, e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("任务实例");
			sysLog.setOptName("保存");
			sysLog.setLogDesc(String.format("修改了的job实例[%s], properties=%s, parameters=%s, state=%s, pause=%s", jobInsId, properties, parameters, state, pause));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
}
