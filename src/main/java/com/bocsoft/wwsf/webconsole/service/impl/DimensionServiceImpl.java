package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.DimensionEntityMapper;
import com.bocsoft.wwsf.webconsole.mapper.DimensionMapper;
import com.bocsoft.wwsf.webconsole.model.Dimension;
import com.bocsoft.wwsf.webconsole.service.DimensionService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("dimensionService")
public class DimensionServiceImpl implements DimensionService{

	@Autowired
	private DimensionMapper dimensionMapper;
	
	@Autowired
	private DimensionEntityMapper dimensionEntityMapper;
	
	public PageInfo<Dimension> queryDimensionPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				dimensionMapper.queryDimensionPage(condition);
			}
		});
	}
	
	public List<Dimension> getDimensionList(Map<String, Object> condition) throws Exception{
		return dimensionMapper.queryDimensionPage(condition);
	}
	
	public int insertDimension(Dimension dimension) throws Exception{
		return dimensionMapper.insertDimension(dimension);
	}
	
	@Transactional
	public void deleteDimensions(String[] ds) throws Exception{
		for (String d: ds) {
			String[] pds = d.split(":");
			if(pds.length<2) {
				continue;
			}else {
				String product = pds[0];
				String dmsn = pds[1];
				Map<String,Object> dMap = new HashMap<String,Object>();
				dMap.put("product", product);
				dMap.put("dmsnName", dmsn);
				dimensionEntityMapper.deleteDimensionEntity(dMap);
				dimensionMapper.deleteDimension(dMap);
			}
		}
	}

	@Override
	public List<Map<String,Object>> getDimensionCountByProduct(Map<String, Object> condition) throws Exception {
		return dimensionMapper.getDimensionCountByProduct(condition);
	}

	@Override
	@Transactional
	public int deleteDimensions(List<Dimension> list) throws Exception {
		Map<String,Object> dMap = new HashMap<String,Object>();
		for (Dimension dimension : list) {
			dMap.put("product", dimension.getProduct());
			dMap.put("dmsnName", dimension.getName());
			dimensionEntityMapper.deleteDimensionEntity(dMap);
			dimensionMapper.deleteDimension(dMap);
		}
		return 1;
	}
	
}
