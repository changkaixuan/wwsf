package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.model.Plugin;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.service.ParameterService;
import com.bocsoft.wwsf.webconsole.service.PluginService;
import com.bocsoft.wwsf.webconsole.service.ProductService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.PageInfo;

@RestController
public class PluginAndAgentController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PluginService pluginService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ParameterService parameterService;
	@Autowired
	private WwseService wwseService;
	
	@GetMapping("/pluginAndAgent/productList")
	public String allPrducts(){
		RestResponse response = new RestResponse();
		List<Product> products = new ArrayList<>();
		try {
			products = productService.selectProduct(null);
			
			Product pe_btn = new Product();
			pe_btn.setpId("全部");
			pe_btn.setpName("禁止删除");
			products.add(0, pe_btn);
			
			response.put("rows", products);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/pluginAndAgent/setPluginTags")
	public String setPluginTags(String product,String pluginName,String tagNames){
		RestResponse response = new RestResponse();

		return JSON.toJSONString(response);
	}
	
	@GetMapping("/pluginAndAgent/pluginParameters")
	public String pluginParameters(String pluginName){
		RestResponse response = new RestResponse();
		List<Parameter> parameterList = null;
		try{
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("jobId", pluginName);
			condition.put("sourceId", pluginName);
			parameterList = parameterService.getParameterList(condition);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		response.put("rows", parameterList);
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/plu/parameters")
	public String pluginParamDefinition(String pluginName){
		RestResponse response = new RestResponse();
		List<Parameter> parameterList = null;
		try{
			parameterList = parameterService.queryPluParameterList(pluginName);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		response.put("rows", parameterList);
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/plu/flushPluginInfo")
	public String flushPluginInfo(){
		RestResponse response = new RestResponse();
		try{
			return wwseService.flushPluginInfo();
		}catch(Exception e){
			String emsg = StringUtil.stringifyException(e);
			logger.error(emsg);
			response.setSuccess(false);
			response.setInfo("刷新插件信息失败");
			response.setDetailInfo(emsg);
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/pluginAndAgent/selectPlugin")
	public RestResponse selectPlugin(){
		RestResponse response = new RestResponse();
		List<Plugin> pluginList = null;
		try{
			pluginList = pluginService.queryPluList();
			if(null != pluginList && pluginList.size()>0) {
				response.put("plugins", pluginList);
			}
			response.setSuccess(true);
		}catch(Exception e){
			logger.error(e.toString());
			response.setSuccess(false);
			response.setInfo("获取插件数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/pluginAndAgent/pluginList")
	@RequiresPermissions("menu:pluginAndAgent-list")
	public String pluginList(String product,String pluginName,Integer pageNumber, Integer pageSize){
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != pluginName && !pluginName.equals("")) {
				conditions.put("lName", pluginName);
			}
			PageInfo<Plugin> pageList = pluginService.pluginList(conditions, pageNumber, pageSize);
			if(null != pageList) {
				response.put("total", pageList.getTotal());
				response.put("rows", pageList.getList());
			}
		} catch (Exception e) {
			logger.error("--- 获取插件列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}

}
