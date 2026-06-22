package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Dimension;
import com.github.pagehelper.PageInfo;

public interface DimensionService{
	
	public PageInfo<Dimension> queryDimensionPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;

	public List<Dimension> getDimensionList(Map<String, Object> condition) throws Exception;
	
	public int insertDimension(Dimension dimension) throws Exception;
	
	public void deleteDimensions(String[] ds) throws Exception;
	
	public List<Map<String,Object>> getDimensionCountByProduct(Map<String, Object> condition) throws Exception;
	
	public int deleteDimensions(List<Dimension> list) throws Exception;
	
}
