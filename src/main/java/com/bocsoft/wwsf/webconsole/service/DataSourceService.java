package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.DataSource;
import com.github.pagehelper.PageInfo;

public interface DataSourceService {

	public PageInfo<DataSource> queryDataSourcePage(Map<String, Object> condition,int pageNo, int pageSize);
	
	public List<DataSource> getDataSourceList(Map<String, Object> condition);

	public int insertDataSource(DataSource dataSource) throws Exception;
	
	public int updateDataSource(DataSource dataSource) throws Exception;
	
	public void batchDeleteDataSource(List<DataSource> dataSourceList) throws Exception;
	
	public void batchCreateDataSource(List<DataSource> dataSourceList) throws Exception;
	
	public void batchDestroyDataSource(List<DataSource> dataSourceList) throws Exception;
	
	public List<DataSource> setIsOnline(String product,String agent,List<DataSource> dataSourceList) throws Exception;
	
	public DataSource getDataSource(String name) throws Exception;
	
	public List<Map<String,Object>> getDataSourceCountByProduct(Map<String, Object> condition) throws Exception;
	
}
