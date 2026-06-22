package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.DimensionEntity;

public interface DimensionEntityMapper {

	public List<DimensionEntity> queryDimensionEntityPage(Map<String,Object> condition);
	
	public int insertDimensionEntity(DimensionEntity dimensionEntity);
	
	public int updateDimensionEntity(Map<String,Object> uMap);
	
	public int deleteDimensionEntity(Map<String,Object> dMap);
	
	public int deleteDimensionEntityByProduct(@Param("product") String product);

	public void updateDmsnEntity(DimensionEntity dimensionEntity);
	
	public int batchInsertDimensionEntity(List<DimensionEntity> dimensionEntityList);

	public void batchUpdateDimensionEntity(List<DimensionEntity> uDimensionEntityList);
	
}
