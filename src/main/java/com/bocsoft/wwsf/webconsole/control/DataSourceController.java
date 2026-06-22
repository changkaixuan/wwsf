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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.DataSourceFactoryConfig;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.DataSource;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.DataSourceService;
import com.github.pagehelper.PageInfo;

@RestController
public class DataSourceController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	DataSourceService dsService;
	
	@Autowired
	DataSourceFactoryConfig dataSourceFactoryConfig;
		
	@GetMapping("/dataSource/dsFactories")
	public RestResponse dsFactories() {
		RestResponse response = new RestResponse();
		Map<String,String> dsFactories = dataSourceFactoryConfig.getFactories();
		try {
			if (dsFactories != null && dsFactories.size() > 0) {
				response.put("dsFactories", dsFactories);
			} else {
				response.put("dsFactories", new HashMap<String,String>());
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("", e);
			response.setSuccess(false);
			response.setInfo("获取驱动下拉框数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/dataSource/list")
	@RequiresPermissions("menu:dataSource-list")
	public String list(
			String product,
			String agent,
			String name,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("nullAndProduct", product);
			}
			if(null != agent && !agent.equals("")) {
				conditions.put("nullAndAgent", agent);
			}
			if(null != name && !name.equals("")) {
				conditions.put("lName", name);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			PageInfo<DataSource> pageList = dsService.queryDataSourcePage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", dsService.setIsOnline(product,agent,pageList.getList()));
		} catch (Exception e) {
			logger.error("--- 获取数据源列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
	@DeleteMapping(value="/dataSource",produces=MediaType.APPLICATION_JSON_VALUE)
	public String delete(@RequestBody List<DataSource> configs) {
		RestResponse response = new RestResponse();
		if (configs != null && configs.size() > 0) {
			try {
				dsService.batchDeleteDataSource(configs);
				response.setSuccess(true);
			} catch (Exception e) {
				logger.error("delete datasource failed ", e);
				response.setSuccess(false);
				response.setInfo("");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
		} else {
			response.setSuccess(false);
			response.setInfo("未选中数据源");
			return JSONObject.toJSONString(response);
		}
		return JSONObject.toJSONString(response);
	}
	
	@PutMapping(value="/dataSource/create",produces=MediaType.APPLICATION_JSON_VALUE)
	public String create(@RequestBody List<DataSource> configs) {
		RestResponse response = new RestResponse();
		if (configs != null && configs.size() > 0) {
			try {
				dsService.batchCreateDataSource(configs);
				response.setSuccess(true);
			} catch (Exception e) {
				logger.error("create datasource failed ", e);
				response.setSuccess(false);
				response.setInfo("");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
		} else {
			response.setSuccess(false);
			response.setInfo("未选中数据源");
			return JSONObject.toJSONString(response);
		}
		return JSONObject.toJSONString(response);
	}
	
	@PutMapping(value="/dataSource/destroy",produces=MediaType.APPLICATION_JSON_VALUE)
	public String destroy(@RequestBody List<DataSource> configs) {
		RestResponse response = new RestResponse();
		
		if (configs != null && configs.size() > 0) {
			try {
				dsService.batchDestroyDataSource(configs);
				response.setSuccess(true);
			} catch (Exception e) {
				logger.error("destory dataSource failed ", e);
				response.setSuccess(false);
				response.setInfo("");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
		} else {
			response.setSuccess(false);
			response.setInfo("未选中数据源");
			return JSONObject.toJSONString(response);
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/dataSource",produces=MediaType.APPLICATION_JSON_VALUE)
	public String get(String product, String agent, String name) {
		RestResponse response = new RestResponse();
		try {
			DataSource dataSource = dsService.getDataSource(name);
			if (null != dataSource) {
				response.put("dataSourceConfig", dataSource);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get dataSource failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/dataSource/save",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String save(@RequestBody DataSource dsConfig) {
		RestResponse response = new RestResponse();
		try {
			/*if (StringUtil.hasText(dsConfig.getXa()) && Boolean.parseBoolean(dsConfig.getXa())) {
				dsConfig.setDatabaseType("XADataSource");
			} else if (StringUtil.hasText(dsConfig.getPool())) {
				dsConfig.setDatabaseType("ConnectionPoolDataSource");
			} else {
				dsConfig.setDatabaseType("DataSource");
			}*/
			//dsConfig.setDatabaseType("DataSource");
			if (StringUtil.hasText(dsConfig.getDriverName())) {
				dsConfig.setDriverClass(dataSourceFactoryConfig.getFactories().get(dsConfig.getDriverName()));
			}
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("name", dsConfig.getName());
			List<DataSource> dataSourceList = dsService.getDataSourceList(condition);
			if(null != dataSourceList && dataSourceList.size()>0) {
				response.setInfo("新增数据源失败（数据源"+dsConfig.getName()+"已存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			dsService.insertDataSource(dsConfig);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("save dataSource failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/dataSource/update",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String update(@RequestBody DataSource dsConfig) {
		RestResponse response = new RestResponse();
		try {
			/*if (StringUtil.hasText(dsConfig.getXa()) && Boolean.parseBoolean(dsConfig.getXa())) {
				dsConfig.setDatabaseType("XADataSource");
			} else if (StringUtil.hasText(dsConfig.getPool())) {
				dsConfig.setDatabaseType("ConnectionPoolDataSource");
			} else {
				dsConfig.setDatabaseType("DataSource");
			}*/
			//dsConfig.setDatabaseType("DataSource");
			if (StringUtil.hasText(dsConfig.getDriverName())) {
				dsConfig.setDriverClass(dataSourceFactoryConfig.getFactories().get(dsConfig.getDriverName()));
			}
			//dsService.update(dsConfig);
			DataSource dataSource = dsService.getDataSource(dsConfig.getName());
			if(null == dataSource) {
				response.setInfo("修改数据源失败（数据源"+dsConfig.getName()+"不存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			if(null != dataSource.getOnLineDesc() && dataSource.getOnLineDesc().equals("Y1")) {
				response.setInfo("修改数据源失败（只能修改未在线数据源）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			dsService.updateDataSource(dsConfig);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update dataSource failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/dataSource/dataSourceNameList",produces=MediaType.APPLICATION_JSON_VALUE)
	public String dataSourceNameList(String product, String agent) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				condition.put("product", product);
			}
			if(null != agent && !agent.equals("")) {
				condition.put("agent", agent);
			}
			condition.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			List<DataSource> dataSourceList = dsService.getDataSourceList(condition);
			if (null != dataSourceList) {
				response.put("dataSourceList", dataSourceList);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get dataSource failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
}
