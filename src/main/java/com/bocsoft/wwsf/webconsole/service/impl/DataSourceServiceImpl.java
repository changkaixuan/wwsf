package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.mapper.DataSourceMapper;
import com.bocsoft.wwsf.webconsole.model.DataSource;
import com.bocsoft.wwsf.webconsole.service.DataSourceService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("dataSourceService")
public class DataSourceServiceImpl implements DataSourceService{

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private DataSourceMapper dataSourceMapper;
	
	@Autowired
	private WwseService wwseService; 
	
	public PageInfo<DataSource> queryDataSourcePage(Map<String, Object> condition,int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				dataSourceMapper.queryDataSourcePage(condition);
			}
		});
	}
	
	public List<DataSource> getDataSourceList(Map<String, Object> condition) {
		return dataSourceMapper.queryDataSourcePage(condition);
	}
	
	public int insertDataSource(DataSource dataSource) throws Exception{
		return dataSourceMapper.insertDataSource(dataSource);
	}
	
	public int updateDataSource(DataSource dataSource) throws Exception{
		return dataSourceMapper.updateDataSource(dataSource);
	}
	
	public void batchDeleteDataSource(List<DataSource> dataSourceList) throws Exception{
		if(null != dataSourceList && dataSourceList.size()>0) {
			for(DataSource fDataSource : dataSourceList) {
				wwseService.destroyDataSource(fDataSource.getName());
				Map<String,Object> dMap = new HashMap<String,Object>();
				dMap.put("name", fDataSource.getName());
				dataSourceMapper.deleteDataSource(dMap);
			}
		}
	}
	
    public void batchCreateDataSource(List<DataSource> dataSourceList) throws Exception{
    	if(null != dataSourceList && dataSourceList.size()>0) {
			for(DataSource fDataSource : dataSourceList) {
				wwseService.createDataSource(fDataSource.getName());
			}
		}
    }
	
	public void batchDestroyDataSource(List<DataSource> dataSourceList) throws Exception{
		if(null != dataSourceList && dataSourceList.size()>0) {
			for(DataSource fDataSource : dataSourceList) {
				wwseService.destroyDataSource(fDataSource.getName());
			}
		}
	}
	
	public List<DataSource> setIsOnline(String product,String agent,List<DataSource> dataSourceList) throws Exception{
		if(null != dataSourceList && dataSourceList.size()>0) {
			Map<String,List<String>> dsMap = getOnlineDataSources(product, agent);
			for(DataSource fDataSource : dataSourceList) {
				if(null != dsMap.get(fDataSource.getName())) {
					fDataSource.setOnLineDesc("Y1");
					fDataSource.setRunningon(dsMap.get(fDataSource.getName()));
				}
			}
		}
		return dataSourceList;
	}
	
	//key=在线的数据源名称, value=在线的product agent列表
	public Map<String,List<String>> getOnlineDataSources(String product,String agent) throws Exception{
		String agentInfoJson = "";
		String tProduct = null;
		if(null != product && !product.equals("")) {
			tProduct = product;
		}
		String tAgent = null;
		if(null != agent && !agent.equals("")) {
			tAgent = agent;
		}
		try {
			agentInfoJson = wwseService.getDataSourcesOnLine(tProduct,tAgent);
		}catch(Exception e) {
			agentInfoJson = "";
		}
		Map<String,List<String>> agentsMap = null; //key=product,agent, value=在线的数据源名称列表
		if(agentInfoJson.equals("")) {
			agentsMap = new HashMap<String,List<String>>();
		}else {
			agentsMap = JSONObject.parseObject(agentInfoJson, Map.class);
		}
		return agentsMap;
	}
	
	public DataSource getDataSource(String name) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("name", name);
		List<DataSource> dataSourceList = dataSourceMapper.queryDataSourcePage(condition);
		if(null == dataSourceList || dataSourceList.size()<1) {
			return null;
		}
		DataSource dataSource = dataSourceList.get(0);
		/*Map<String,String> onLineDsMap = getOnLineDataSource();
		if(null != onLineDsMap.get(name)) {
			String state = onLineDsMap.get(name);
			if(state.equals("OK")) {
				dataSource.setOnLineDesc("Y1");
			}else {
				dataSource.setOnLineDesc("Y2");
			}
		}*/
		Map<String,List<String>> dsMap = getOnlineDataSources(dataSource.getProduct(), dataSource.getAgent());
		if(null != dsMap.get(dataSource.getName())) {
			dataSource.setOnLineDesc("Y1");
		}
		return dataSource;
	}

	@Override
	public List<Map<String,Object>> getDataSourceCountByProduct(Map<String, Object> condition) throws Exception {
		return dataSourceMapper.getDataSourceCountByProduct(condition);
	}
	
}
