package com.bocsoft.wwsf.webconsole.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.Calendar;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.CalendarService;
import com.github.pagehelper.PageInfo;

@RestController
public class CalendarController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	CalendarService calendarService;
	
	@GetMapping("/calendar/calendarPlanListPage")
	@RequiresPermissions("menu:calendar-list")
	public String calendarPlanListPage(String product,String calName,Integer pageSize,Integer pageNumber){
		RestResponse response = new RestResponse();
		try{
			Map<String, Object> condition = new HashMap<>();
			condition.put("product", product);
			condition.put("lCalName", calName);
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			condition.put("loginName", sysUser.getLoginName());
			PageInfo<Calendar> pageList = calendarService.queryCalendarList(condition, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		}catch(Exception e){
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value = "/calendar/getCalendarList", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getCalendarList(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			conditions.put("orderBy_calName_asc","orderBy_calName_asc");
			List<Calendar> calendars = this.calendarService.queryCalendarList(conditions);
			if(null != calendars && calendars.size()>0) {
				response.put("calendars", calendars); 
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo("获取工作日下拉框数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/calendar/queryCalendar")
	public String queryCalendar(String product,String calName){
		RestResponse response = new RestResponse();
		try{
			Calendar calendar = new Calendar();
			calendar.setProduct(product);
			calendar.setCalName(calName);
			Calendar result = calendarService.queryCalendar(calendar);
			response.put("calendar", result);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping("/calendar/calendarPlanAdd")
	public RestResponse calendarPlanAdd(@RequestBody Calendar calendar){
		RestResponse response = new RestResponse();
		try {
			calendarService.insertCalendar(calendar);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@DeleteMapping("/calendar/calendarPlanDel")
	public RestResponse calendarPlanDel(String proArr, String calNameArr){
		RestResponse response = new RestResponse();
		Map<String, Object> condition = new HashMap<>();
		condition.put("proArr", proArr);
		condition.put("calNameArr", calNameArr);
		try {
			calendarService.deleteCalendar(condition);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping("/calendar/calendarPlanEdit")
	public RestResponse calendarPlanEdit(@RequestBody Calendar calendar){
		RestResponse response = new RestResponse();
		try {
			calendarService.updateCalendar(calendar);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
}
