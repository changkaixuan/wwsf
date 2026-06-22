package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.DimensionEntityMapper;
import com.bocsoft.wwsf.webconsole.model.DimensionEntity;
import com.bocsoft.wwsf.webconsole.service.DimensionEntityService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("dimensionEntityService")
public class DimensionEntityServiceImpl implements DimensionEntityService{

	@Autowired
	private DimensionEntityMapper dimensionEntityMapper;
	
	public PageInfo<DimensionEntity> queryDimensionEntityPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				dimensionEntityMapper.queryDimensionEntityPage(condition);
			}
		});
	}
	
	public List<DimensionEntity> getDimensionEntityList(Map<String, Object> condition) throws Exception{
		return dimensionEntityMapper.queryDimensionEntityPage(condition);
	}
	
	public int insertDimensionEntity(DimensionEntity dimensionEntity) throws Exception{
		return dimensionEntityMapper.insertDimensionEntity(dimensionEntity);
	}
	
	public int updateDimensionEntity(Map<String, Object> uMap) throws Exception{
		return dimensionEntityMapper.updateDimensionEntity(uMap);
	}
	
	public int deleteDimensionEntity(Map<String, Object> dMap) throws Exception{
		return dimensionEntityMapper.deleteDimensionEntity(dMap);
	}
	
	public List<String> getDimensionEntityTags(String product,String dmsnName) throws Exception{
		List<String> tags = new ArrayList<String>();
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("product", product);
		condition.put("dmsnName", dmsnName);
		List<DimensionEntity> dimensionEntityList = this.getDimensionEntityList(condition);
		if(null != dimensionEntityList && dimensionEntityList.size()>0){
			Map<String,Object> filterRepeatTagMap = new HashMap<String,Object>();
			Map<String,Object> filterRepeatTitleMap = new HashMap<String,Object>();
			for(DimensionEntity fDimensionEntity : dimensionEntityList) {
				if(null != fDimensionEntity.getTags() && null == filterRepeatTitleMap.get(fDimensionEntity.getTags())) {
					filterRepeatTitleMap.put(fDimensionEntity.getTags(), "");
					String titleArr[] = fDimensionEntity.getTags().split(",");
					for(String fTitle : titleArr){
						if(!fTitle.equals("") && null == filterRepeatTagMap.get(fTitle)){
							filterRepeatTagMap.put(fTitle,fTitle);
							tags.add(fTitle);
						}
					}
				}
			}
			//标签排序
			Collections.sort(tags, new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
			});
		}
		return tags;
	}
	
}
