package com.bocsoft.wwsf.webconsole.control;

import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.bean.shiro.WwsfSessionFilter;
import com.bocsoft.wwsf.webconsole.model.CleanRules;
import com.bocsoft.wwsf.webconsole.service.CleanRulesService;
import com.github.pagehelper.PageInfo;

@RestController
public class CleanRulesController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CleanRulesService cleanRulesService;
	
	@GetMapping("/cleanRules/list")
	@RequiresPermissions("menu:cleanRules-list")
	public RestResponse list(String product, String type, Integer pageSize,Integer pageNumber){
		RestResponse response = new RestResponse();
		try{
			Map<String, Object> condition = new HashMap<>();
			if (product != null && !"".equals(product)) {
				condition.put("product", product);
			}
			if (type != null && !"".equals(type)) {
				condition.put("type", type);
			}
			PageInfo<CleanRules> pageList = cleanRulesService.queryCleanRulesPage(condition, pageNumber, pageSize);
			cleanRulesService.setJobNames(pageList.getList());
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("get clean rule list failed ",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping(value="/cleanRules/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse get(String id) {
		RestResponse response = new RestResponse();
		try {
			CleanRules tCleanRules = cleanRulesService.getCleanRules(id);
			if(null == tCleanRules) {
				response.setInfo("不存在清理策略"+id);
				response.setSuccess(false);
				return response;
			}
			response.put("cleanRules", tCleanRules);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get clean rule failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping(value="/cleanRules/insert",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse insert(@RequestBody CleanRules cleanRules, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
//			cleanRules.setId(CalendarUtil.getNowTime("yyyyMMddHHmmssSSS"));
			cleanRulesService.insertCleanRules(cleanRules);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("insert ststem role failed ",e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping(value="/cleanRules/update",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse update(@RequestBody CleanRules cleanRules, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			CleanRules tCleanRules = cleanRulesService.getCleanRules(cleanRules.getId());
			if(null == tCleanRules) {
				response.setInfo("不存在清理策略"+cleanRules.getId());
				response.setSuccess(false);
				return response;
			}
			cleanRulesService.updateCleanRules(cleanRules);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update system role failed ",e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@DeleteMapping(value="/cleanRules/delete",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RestResponse delete(String ids, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			cleanRulesService.deleteCleanRules(ids.split(","));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete clean rule failed ",e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/cleanRules/checkCRId",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse checkCRId(String id) {
		RestResponse response = new RestResponse();
		try {
			CleanRules tCleanRules = cleanRulesService.getCleanRules(id);
			response.setSuccess(null != tCleanRules);
		} catch (Exception e) {
			logger.error("get clean rule failed ",e);
			response.setSuccess(false);
		}
		return response;
	}
	
	@GetMapping(value="/cleanRules/getBtnAuth",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getBtnAuth() {
		RestResponse response = new RestResponse();
		try {
			response.put("sysAuthList", SecurityUtils.getSubject().getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUserMenu));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get clean rule failed ",e);
			response.setSuccess(false);
			response.setInfo("加载按钮权限失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/cleanRules/execuRule",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse execuRule(String product,String type,String ruleId) {
		RestResponse response = new RestResponse();
		Map<String, Object> condition = new HashMap<>();
		if (product==null && ruleId==null) {
			logger.error("execut clean rule failed ");
			response.setSuccess(false);
			response.setInfo("数据错误");
		}
		try {
			condition.put("o_result", 0);
			condition.put("o_log", "");
			if(null != type && type.equals("JOB")){
				condition.put("p_clean_rules_id", ruleId);
				cleanRulesService.pWwsJobClear(condition);
			}else if(null != type && type.equals("CRON")){
				condition.put("p_clean_rules_id", ruleId);
				cleanRulesService.pWwsCronClear(condition);
			}else{
				condition.put("p_product", product);
				cleanRulesService.pWwsJobExeinfoClear(condition);
			}
			if(null != condition.get("o_result")){
				if(condition.get("o_result").toString().equals("0")){
					response.setSuccess(true);
				}else{
					response.setSuccess(false);
				}
				if(null != condition.get("o_log")) {
					if(condition.get("o_log") instanceof Clob){
						response.setDetailInfo(StringUtil.clobToString((Clob)condition.get("o_log")));
					}else{
						response.setDetailInfo(condition.get("o_log").toString());
					}
				}else {
					response.setSuccess(false);
					response.setDetailInfo("执行异常");
				}
			}else{
				response.setSuccess(false);
				response.setInfo("执行异常");
			}
		} catch (Exception e) {
			logger.error("execute clean rule failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
}
