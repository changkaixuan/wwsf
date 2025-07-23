package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.Dimension;

public interface DimensionMapper {

	public List<Dimension> queryDimensionPage(Map<String,Object> condition);
	
	public int insertDimension(Dimension dimension);
	
	public int deleteDimension(Map<String,Object> dMap);
	
	public List<Map<String,Object>> getDimensionCountByProduct(Map<String,Object> condition);
	
	public int deleteDimensionByProduct(@Param("product") String product);

	public void updateDimension(Dimension dimension);
	
}
