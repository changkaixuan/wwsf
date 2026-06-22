package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.DimensionEntity;
import com.github.pagehelper.PageInfo;

public interface DimensionEntityService{
	
	public PageInfo<DimensionEntity> queryDimensionEntityPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;

	public List<DimensionEntity> getDimensionEntityList(Map<String, Object> condition) throws Exception;
	
	public int insertDimensionEntity(DimensionEntity dimensionEntity) throws Exception;
	
	public int updateDimensionEntity(Map<String, Object> uMap) throws Exception;
	
	public int deleteDimensionEntity(Map<String, Object> dMap) throws Exception;
	
	public List<String> getDimensionEntityTags(String product,String dmsnName) throws Exception;
	
}
