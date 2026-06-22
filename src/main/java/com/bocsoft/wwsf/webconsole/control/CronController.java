package com.bocsoft.wwsf.webconsole.control;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.Cron;
import com.bocsoft.wwsf.webconsole.model.CronLog;
import com.bocsoft.wwsf.webconsole.model.CronShared;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.CronLogService;
import com.bocsoft.wwsf.webconsole.service.CronService;
import com.bocsoft.wwsf.webconsole.service.CronSharedService;
import com.github.pagehelper.PageInfo;

@RestController
public class CronController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CronLogService cronLogService;
	
	@Autowired
	private CronService cronService;
	
	@Autowired
	private CronSharedService cronSharedService;
	
	@GetMapping("/cron/cronspage")
	@RequiresPermissions("menu:cron-list")
	public String kvspage(String product, String agent,String jobId,Integer pageNumber,Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> conditions = new HashMap<String, Object>();
			conditions.put("nullOrProduct", product);
			conditions.put("nullOrAgent", agent);
			conditions.put("lJobId", jobId);
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<Cron> allCrons = null;
			PageInfo<Cron> pageList = cronService.kvspage(conditions, pageNumber, pageSize);
			allCrons = pageList.getList();
			response.put("total", pageList.getTotal());
			response.put("rows", allCrons);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("query cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("获取计划任务列表出错，"+e.getMessage());
		}
		return JSON.toJSONString(response);
	}

	@PostMapping("/cron/add")
	@ResponseBody
	public String cronAdd(@RequestBody Cron cron){
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(cron.getProduct())){
			response.setSuccess(false);
			response.setInfo("产品不能是空");
			return JSON.toJSONString(response);
		}
		try {
			// 用job_id 查询表ww_cron，判断job_id是否存在,存在返回错误
			Map<String, Object> conditions = new HashMap<String, Object>();
			conditions.put("jobId", cron.getJobId());
			List<Cron> cronList = cronService.cronList(conditions,false);
			if (cronList != null && cronList.size()>0) {
				response.setSuccess(false);
				response.setInfo(String.format("job_id %s 已存在", cron.getJobId()));
				return JSON.toJSONString(response);
			}
		
			// 不存在则 插入 ww_cron 表
			// 创建时间默认为当时
			cron.setCreateTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
			cronService.cronAdd(cron);
			
			// 判定是否为自动开始，若是自动开始，则调用start
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("add cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping("/cron/{jobsId}")
	@ResponseBody
	public String deleteCron(@PathVariable String jobsId) throws Exception {
		RestResponse response = new RestResponse();
		String[] ids = jobsId.split("\\|");
		if(ids == null || ids.length <= 0){
			throw new Exception("请选择需要删除的cron job。");
		}
		try {
			int flag = cronService.deleteCron(ids, true);
			if (flag==2) {
				response.setInfo("正在运行的任务无法删除");
				// TODO
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/cron/update")
	@ResponseBody
	public String update(@RequestBody Cron cron) throws Exception{
		RestResponse response = new RestResponse();
		try {
			if(null!=cron){
				if(StringUtil.isNullOrEmpty(cron.getJobId())){
					throw new Exception("任务ID不能是空。");
				}
				Map<String, Object> condition = new HashMap<String,Object>();
				condition.put("jobId", cron.getJobId());
				List<Cron> cronList = null;
				try {
					cronList = cronService.cronList(condition,true);
				}catch(Exception ee) {
					response.setInfo("修改计划任务失败（master nofound）");
					response.setSuccess(false);
					return JSONObject.toJSONString(response);
				}
				if(null == cronList || cronList.size()<1) {
					response.setInfo("修改计划任务失败（计划任务"+cron.getJobId()+"不存在）");
					response.setSuccess(false);
					return JSONObject.toJSONString(response);
				}
				Cron tCron = cronList.get(0);
				if(null != tCron.getStatus() && (tCron.getStatus().equals("STARTED") || tCron.getStatus().equals("STARTED_SHOULDNOT") || tCron.getStatus().equals("ERROR"))) {
					response.setInfo("修改计划任务失败（不能修改运行中、运行中(*)和错误的计划任务）");
					response.setSuccess(false);
					return JSONObject.toJSONString(response);
				}
				cronService.update(cron);
				response.setSuccess(true);
			}else{
				throw new Exception("请选择需要编辑的cron job");
			}
		} catch (Exception e) {
			logger.error("update cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("保存失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping("/cron/start")
	@ResponseBody
	public String cronStart(@RequestParam String jobsId, @RequestParam Boolean clearShared) throws Exception {
		RestResponse response = new RestResponse();
		try {
			if(StringUtil.isNullOrEmpty(jobsId)){
				throw new Exception("任务ID不能是空。");
			}
			String[] ids = jobsId.split("\\|");
			if(ids == null || ids.length <= 0){
				throw new Exception("请选择需要启动的cron job。");
			}
			cronService.cronStart(ids, clearShared);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("start cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("启动操作失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping("/cron/stop")
	@ResponseBody
	public String cronStop(@RequestParam String jobsId) {
		RestResponse response = new RestResponse();
		try {
			if(StringUtil.isNullOrEmpty(jobsId)){
				throw new Exception("任务ID不能是空。");
			}
			String[] ids = jobsId.split("\\|");
			if(ids == null || ids.length <= 0){
				throw new Exception("请选择需要停止的cron job。");
			}
			cronService.cronStop(ids);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("stop cronjob failed", e);
			response.setSuccess(false);
			response.setInfo("停止操作失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/cron/cronLog",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getCronLog(String product, String agent, String jobId, String startTime, String endTime, Integer result, Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			if(StringUtil.isNullOrEmpty(jobId)){
				throw new Exception("参数无效[任务ID不能是空]");
			}
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put("jobId", jobId);
			if(null != product && !product.equals("")) {
				condition.put("product", product);
			}
			if(null != agent && !agent.equals("")) {
				condition.put("lAgent", agent);
			}
			if(null != startTime && !startTime.equals("")) {
				condition.put("startTime", startTime);
			}
			if(null != endTime && !endTime.equals("")) {
				condition.put("endTime", endTime);
			}
			if(null != result && result != -1) {
				condition.put("result", result);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			condition.put("loginName", sysUser.getLoginName());
			PageInfo<CronLog> pageList = cronLogService.getCronLog(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get log failed, cronJobId:{}", jobId, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/cron/getCronShared",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getCronShared(String jobId, String product, String agent, Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			if(StringUtil.isNullOrEmpty(jobId)){
				throw new Exception("参数无效[任务ID不能是空]");
			}
			Map<String, Object> condition = new HashMap<String, Object>();
			condition.put("jobId", jobId);
			condition.put("product", product);
			condition.put("lAgent", agent);
			PageInfo<CronShared> pageList = cronSharedService.queryCronShared(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get cron shared failed ",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/cron/addCronShared")
	@ResponseBody
	public String addCronShared(@RequestBody CronShared cronShared){
		RestResponse response = new RestResponse();
		try {
			cronSharedService.saveCronShared("add", cronShared);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("add cron shared failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/cron/editCronShared")
	@ResponseBody
	public String editCronShared(@RequestBody CronShared cronShared){
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> condition = new HashMap<String,Object>();
			condition.put("jobId", cronShared.getJobId());
			List<Cron> cronList = cronService.cronList(condition,true);
			if(null == cronList || cronList.size()<1) {
				response.setInfo("修改运行时数据失败（计划任务"+cronShared.getJobId()+"不存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			Cron tCron = cronList.get(0);
			if(null != tCron.getStatus() && (tCron.getStatus().equals("STARTED") || tCron.getStatus().equals("STARTED_SHOULDNOT") || tCron.getStatus().equals("ERROR"))) {
				response.setInfo("修改运行时数据失败（计划任务状态为运行中、运行中(*)和错误时，不能修改运行时数据）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			cronSharedService.saveCronShared("update", cronShared);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("edit cron shared failed ", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping("/cron/deleteCronShared")
	@ResponseBody
	public String deleteCronShared(String jobId, String products, String agents) throws Exception {
		RestResponse response = new RestResponse();
		String[] productArr = products.split("\\|");
		String[] agentArr = agents.split("\\|");
		try {
			Map<String, Object> condition = new HashMap<String,Object>();
			condition.put("jobId", jobId);
			List<Cron> cronList = cronService.cronList(condition,true);
			if(null == cronList || cronList.size()<1) {
				response.setInfo("删除运行时数据失败（计划任务"+jobId+"不存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			Cron tCron = cronList.get(0);
			if(null != tCron.getStatus() && (tCron.getStatus().equals("STARTED") || tCron.getStatus().equals("STARTED_SHOULDNOT") || tCron.getStatus().equals("ERROR"))) {
				response.setInfo("删除运行时数据失败（计划任务状态为运行中、运行中(*)和错误时，不能删除运行时数据）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			cronSharedService.deleteCronShared(jobId, productArr, agentArr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete cron shared failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/cron/getAllCron")
	public RestResponse getAllCron(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> conditions = new HashMap<String, Object>();
			conditions.put("nullOrProduct", product);
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<Cron> list = cronService.cronList(conditions, false);
			response.put("jobIds", list);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("getAllCron failed ", e);
			response.setSuccess(false);
			response.setInfo("获取计划任务列表出错");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/cron/getCronLogInformation",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getCronLogInformation(String logId) {
		RestResponse response = new RestResponse();
		try {
            List<CronLog> cronLogList = cronLogService.getCronLogInformation(logId);
            if(null == cronLogList || cronLogList.size() < 1) {
            	response.setInfo("当前日志不存在");
                response.setSuccess(false);
                return response;
            }
            CronLog cronLog = cronLogList.get(0);
            response.put("cronLog", cronLog);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get cron log failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
}
