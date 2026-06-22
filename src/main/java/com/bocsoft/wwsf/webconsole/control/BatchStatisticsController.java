package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.BatchStatisticVo;
import com.bocsoft.wwsf.webconsole.model.ChartNameValue;
import com.bocsoft.wwsf.webconsole.model.JobInstance;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.model.TaskInstance;
import com.bocsoft.wwsf.webconsole.service.JobInstanceService;
import com.github.pagehelper.PageInfo;

@RestController
public class BatchStatisticsController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JobInstanceService jobInstanceService;
	
	@DeleteMapping("/batch/info")
    public String clearBatch(@RequestParam String product, @RequestParam String batch) {
		RestResponse response = new RestResponse();
		try{
			if(StringUtil.isNullOrEmpty(product)){
				throw new Exception("Parameter 'product' is null");
			}
			if(StringUtil.isNullOrEmpty(batch)){
				throw new Exception("Parameter 'batch' is null");
			}
			jobInstanceService.clearBatch(product, batch);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping("/batch/infos")
    public String delBatches(String productArr, String batchArr) {
		RestResponse response = new RestResponse();
		try{
			if(StringUtil.isNullOrEmpty(productArr)){
				throw new Exception("Parameter 'product' is null");
			}
			if(StringUtil.isNullOrEmpty(batchArr)){
				throw new Exception("Parameter 'batch' is null");
			}
			jobInstanceService.delBatches(productArr, batchArr);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("删除失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/batch/info")
    public String refreshBatch(@RequestParam String product, @RequestParam String batch) {
		RestResponse response = new RestResponse();
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("product", product);
		conditions.put("batch", batch);
		try{
			BatchStatisticVo bsv = jobInstanceService.getBatchStatisticVo(conditions);
			response.put("batch_info", bsv);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/batch/list")
	@RequiresPermissions("menu:batchStatistics-list")
    public String redisBatchs(String product,String batchKey,Integer pageNumber,Integer pageSize){
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> conditions = new HashMap<String, Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != batchKey && !batchKey.equals("")) {
				conditions.put("lBatch", batchKey);
			}
			
			if (pageSize == null || pageSize <= 0) pageSize = 20;
			if (pageNumber == null || pageNumber <= 0) pageNumber = 1;
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			PageInfo<BatchStatisticVo> pageList = jobInstanceService.redisBatchs(conditions, pageNumber, pageSize);
			response.put("rows", pageList.getList());
			response.put("total", pageList.getTotal());
			response.put("pageNumber", pageNumber);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//单批量统计/Job实例表格
	@GetMapping("batch/infowithproduct")
	public RestResponse getProductAndBatch() {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("orderBy_initTimeDesc", "orderBy_initTimeDesc");
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			List<JobInstance> jobInstanceList = jobInstanceService.queryJobInstance(conditions);
			if(null != jobInstanceList && jobInstanceList.size()>0) {
				response.put("product", jobInstanceList.get(0).getProduct());
				response.put("batch", jobInstanceList.get(0).getBatch());
				response.setSuccess(true);
			}else {
				response.setSuccess(false);
			}
		} catch (Exception e) {
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo("获取查询条件（产品和批量）默认值失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	//单批量统计/Task实例统计
	@GetMapping("/batch/chart/series")
	public RestResponse getSeriesTaskState(@RequestParam String product, @RequestParam String batch) {
		RestResponse response = new RestResponse();
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("product", product);
		conditions.put("batch", batch);
		try{
			BatchStatisticVo bsv = jobInstanceService.getBatchStatisticVo(conditions);
			List<ChartNameValue> chartNameValueList = new ArrayList<ChartNameValue>();
			ChartNameValue chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_INIT);
			chartNameValue.setValue(bsv.getTaskInitTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_READY);
			chartNameValue.setValue(bsv.getTaskReadyTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_RUNNING);
			chartNameValue.setValue(bsv.getTaskRunningTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_ERROR_DELAY);
			chartNameValue.setValue(bsv.getTaskErrorDelayTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_FEEDBACK);
			chartNameValue.setValue(bsv.getTaskFeedbackTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_FAIL);
			chartNameValue.setValue(bsv.getTaskFailedTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_MANUAL);
			chartNameValue.setValue(bsv.getTaskMaualTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_PERIOD_DELAY);
			chartNameValue.setValue(bsv.getTaskCycleDelayTotal());
			chartNameValueList.add(chartNameValue);
			
			chartNameValue = new ChartNameValue();
			chartNameValue.setName(TaskInstance.STATE_SUCCESS);
			chartNameValue.setValue(bsv.getTaskSuccessTotal());
			chartNameValueList.add(chartNameValue);
			
			response.put("chartNameValueList", chartNameValueList);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("获取任务项实例状态统计数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	//单批量统计/Job实例表格
	@GetMapping("/batch/chart/jobs")
	@RequiresPermissions("menu:singleBatchStatistics-list")
	public String queryJobInstance(
			String product,
			String batch,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != batch && !batch.equals("")) {
				conditions.put("batch", batch);
			}
			conditions.put("orderBy_initTimeDesc", "orderBy_initTimeDesc");
			if(conditions.size() < 3) {
				conditions.put("getNull", "getNull");
			}
			PageInfo<JobInstance> pageList = jobInstanceService.queryJobInstance(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取Job实例列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping("/batch/select")
	public RestResponse selectBatch(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", product);
			response.put("batchs", jobInstanceService.getJobInstanceBatch(condition));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/batch/select", e);
			response.setSuccess(false);
			response.setInfo("获取批量下拉框数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

}
