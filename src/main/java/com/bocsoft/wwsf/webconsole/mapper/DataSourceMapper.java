package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.DataSource;

public interface DataSourceMapper {

	public List<DataSource> queryDataSourcePage(Map<String,Object> condition);
	
	public int insertDataSource(DataSource dataSource);
	
	public int updateDataSource(DataSource dataSource);
	
	public int updateDataSourceAllValue(DataSource dataSource);
	
	public int deleteDataSource(Map<String,Object> dMap);
	
	public List<Map<String,Object>> getDataSourceCountByProduct(Map<String,Object> condition);
	
	public int deleteDataSourceByProduct(@Param("product") String product);

	public DataSource selectDsByNm(String dsName);
}
