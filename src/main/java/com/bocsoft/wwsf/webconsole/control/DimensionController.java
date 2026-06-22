package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
/*import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.Dimension;
import com.bocsoft.wwsf.webconsole.model.DimensionEntity;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.DimensionEntityService;
import com.bocsoft.wwsf.webconsole.service.DimensionService;
import com.github.pagehelper.PageInfo;

@RestController
public class DimensionController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	DimensionService dimensionService;
	
	@Autowired
	DimensionEntityService dimensionEntityService;
	
	//查询产品维度列表
	@GetMapping("/dmsn/list")
	@RequiresPermissions("menu:dimension-list")
	public RestResponse dmsnList(
			String product,
			String dmsn,
			Integer pageNumber, 
			Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != dmsn && !dmsn.equals("")) {
				conditions.put("lDmsnName", dmsn);
			}
			SysUser sysUser = UserUtils.getCurLoginSysUser();
			conditions.put("loginName", sysUser.getLoginName());
			PageInfo<Dimension> pageList = dimensionService.queryDimensionPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取维度列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	//删除维度
	@DeleteMapping(value="/dmsn/{dmsns}",produces=MediaType.APPLICATION_JSON_VALUE)
	public String delDimension(@PathVariable String dmsns) {
		RestResponse response = new RestResponse();
		String[] ds = dmsns.split("\\|");
		if(ds == null || ds.length <= 0){
			response.setSuccess(false);
			response.setInfo("请选择需要删除的维度!");
			return JSON.toJSONString(response);
		}
		try {
			//对选中的多个维度循环执行删除
			dimensionService.deleteDimensions(ds);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete dimensions:{}",String.join(";", dmsns), e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/dmsn/existsDmsn",produces=MediaType.APPLICATION_JSON_VALUE)
	public String existsDmsn(String product, String dmsn) {
		RestResponse response = new RestResponse();
		String isExistsDmsn = "N";
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", product);
			condition.put("dmsnName", dmsn);
			List<Dimension> dimensionList = this.dimensionService.getDimensionList(condition);
			if(null != dimensionList && dimensionList.size()>0){
				isExistsDmsn = "Y";
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get dmsn list failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		response.put("isExistsDmsn", isExistsDmsn);
		return JSON.toJSONString(response);
	}
	
	//查询维度实体
	@GetMapping(value="/dmsn/entity",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse DmsnEntity(
			String product, 
			String dmsn,
			String p_entity,
			String p_tag,
			Integer pageNumber, 
			Integer pageSize) {
		RestResponse response = new RestResponse();
		if(StringUtil.isNullOrEmpty(product)){
			response.setSuccess(false);
			response.setInfo("参数无效[product]");
			return response;
		}
		if(StringUtil.isNullOrEmpty(dmsn)){
			response.setSuccess(false);
			response.setInfo("参数无效[dimension]");
			return response;
		}
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("product", product);
			conditions.put("dmsnName", dmsn);
			if(null != p_entity && !p_entity.equals("")) {
				conditions.put("lEntityName", p_entity);
			}
			if(null != p_tag && !p_tag.equals("")) {
				conditions.put("title", ","+p_tag+",");
			}
			PageInfo<DimensionEntity> pageList = dimensionEntityService.queryDimensionEntityPage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取维度列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	

	//删除维度实体
	@DeleteMapping(value="/dmsn/delEntity/{entities}",produces=MediaType.APPLICATION_JSON_VALUE)
	public String delEntities(@PathVariable String entities) {
		RestResponse response = new RestResponse();
		if(!StringUtil.hasText(entities)){
			response.setSuccess(false);
			response.setInfo("请选择需要删除的维度实体!");
			return JSON.toJSONString(response);
		}
		
		String[] entityArray = entities.split("\\|");
		try {
			for (String entity: entityArray) {
				String[] es = entity.split(":");
				if(es.length < 4) {
					continue;
				}else {
					String prod = es[0];
					String dmsn = es[1];
					String enty = es[2];
					//String tags = es[3];
					Map<String,Object> dMap = new HashMap<String,Object>();
					dMap.put("product", prod);
					dMap.put("dmsnName", dmsn);
					dMap.put("entityName", enty);
					dimensionEntityService.deleteDimensionEntity(dMap);
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete dimensions entities:{}",String.join(";", entities), e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}

	//查询标签
	@GetMapping(value="/dmsn/tags", produces=MediaType.APPLICATION_JSON_VALUE)
	public String getTags(@RequestParam String product, @RequestParam String dimension) {
		RestResponse response = new RestResponse();
		List<String> tags = new ArrayList<String>();
		if(StringUtil.hasText(product) && StringUtil.hasText(dimension)) {
			try {
				tags = dimensionEntityService.getDimensionEntityTags(product,dimension);
			}catch (Exception e) {
				logger.error("get dmsn entity tags failed ", e);
				response.setSuccess(false);
				response.setInfo("查询标签失败");
				response.setDetailInfo(StringUtil.stringifyException(e));
				return JSON.toJSONString(response);
			}
		}
		response.put("tags", tags);
		response.setSuccess(true);
		return JSON.toJSONString(response);
	}
	
	//保存维度
	@PostMapping(value="/dmsn/save", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String dmsnSave(@RequestBody ArrayList<Map<String,String>> params) {
		RestResponse response = new RestResponse();
		if( params != null && !params.isEmpty() ) {
			try {
				for(Map<String,String> p: params) {
					String product = p.get("product").trim();
					String name = p.get("dimension").trim();
					String description = p.get("description").trim();
					Map<String,Object> condition = new HashMap<String,Object>();
					condition.put("product", product);
					condition.put("dmsnName", name);
					List<Dimension> dimensionList = this.dimensionService.getDimensionList(condition);
					if(null != dimensionList && dimensionList.size()>0){
						response.setSuccess(false);
						response.setInfo("维度已存在," + product + "/" + name);
						break;
					}
					Dimension dimension = new Dimension();
					dimension.setProduct(product);
					dimension.setName(name);
					dimension.setDescription(description);
					this.dimensionService.insertDimension(dimension);
					response.setSuccess(true);
				}
			}catch(Exception e) {
				logger.error("保存维度失败", e);
				response.setSuccess(false);
				response.setInfo("保存维度失败");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
		}else {
			response.setSuccess(false);
			response.setInfo("Parameter is Empty");
		}
		
		return JSON.toJSONString(response);
	}
	
	//保存维度实体
	@PostMapping(value="/dmsn/entity/save",  produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String entitySave(@RequestBody ArrayList<Map<String,String>> params) {
		RestResponse response = new RestResponse();
		if( params != null && !params.isEmpty() ) {
			try {
				for(Map<String,String> p: params) {
					String product = p.get("product");
					String dimension = p.get("dimension");
					String entity = p.get("entity");
					String tags = p.get("tags");
					Map<String,Object> condition = new HashMap<String,Object>();
					condition.put("product", product);
					condition.put("dmsnName", dimension);
					condition.put("entityName", entity);
					List<DimensionEntity> dimensionEntityList = this.dimensionEntityService.getDimensionEntityList(condition);
					if(null != dimensionEntityList && dimensionEntityList.size()>0){//维度实体已存在，只更新标签
						if(null != tags && !tags.equals("")){//需要更新下标签
							DimensionEntity dimensionEntity = dimensionEntityList.get(0);
							String dTags = dimensionEntity.getTags(); //值格式：,tag,
							String uTags = "";
							if(null == dTags || dTags.equals("")) {
								uTags = ","+tags+",";
								condition.put("title", uTags);
								this.dimensionEntityService.updateDimensionEntity(condition);
							}else {
								uTags = dTags;
								for (String tag: tags.split(",")) {
									if(uTags.indexOf(","+tag+",") == -1) {
										uTags = uTags + tag + ",";
									}
								}
								if(uTags.length() != dTags.length()) {
									condition.put("title", uTags);
									this.dimensionEntityService.updateDimensionEntity(condition);
								}
							}
						}
					}else{
						DimensionEntity dimensionEntity = new DimensionEntity();
						dimensionEntity.setProduct(product);
						dimensionEntity.setName(dimension);
						dimensionEntity.setEntity(entity);
						if(null != tags && !tags.equals("")){
							dimensionEntity.setTags(","+tags+",");
						}
						this.dimensionEntityService.insertDimensionEntity(dimensionEntity);
					}
				}
				response.setSuccess(true);
			}catch(Exception e) {
				logger.error("save entity failed ", e);
				response.setSuccess(false);
				response.setInfo("保存维度实体失败");
				response.setDetailInfo(StringUtil.stringifyException(e));
			}
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/dmsn/entity/getDmsnEntity",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getDmsnEntity(String product, String dimension, String entity, String tag,String selectEntity) {
		RestResponse response = new RestResponse();
		String dmsnEntitys = "";
		try {
			if(null != selectEntity && !selectEntity.trim().equals("")){//选中的实体
				dmsnEntitys = selectEntity;
			}else{//未选中实体
				Map<String,Object> condition = new HashMap<String,Object>();
				condition.put("product", product);
				condition.put("dmsnName", dimension);
				if(null != entity && !entity.trim().equals("")){
					condition.put("lEntityName", entity);
				}
				if(null != tag && !tag.trim().equals("")){
					condition.put("title", ","+tag+",");
				}
				List<DimensionEntity> dimensionEntityList = this.dimensionEntityService.getDimensionEntityList(condition);
				if(null != dimensionEntityList && dimensionEntityList.size()>0){
					for(DimensionEntity fDimensionEntity : dimensionEntityList){
						dmsnEntitys = dmsnEntitys + fDimensionEntity.getEntity() + ",";
					}
					dmsnEntitys = dmsnEntitys.substring(0,dmsnEntitys.length()-1);
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get dmsn entity failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		response.put("dmsnEntitys", dmsnEntitys);
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping(value="/dmsn/entity/tags/{type}/{product}/{dimension}/{entitys}/{tags}",produces=MediaType.APPLICATION_JSON_VALUE)
	public String batchSaveTags(@PathVariable String type,@PathVariable String product,@PathVariable String dimension,@PathVariable String entitys,@PathVariable String tags) {
		RestResponse response = new RestResponse();
		try {
			String entityArr[] = entitys.split(",");
			for(String fEntity : entityArr){
				Map<String,Object> condition = new HashMap<String,Object>();
				condition.put("product", product);
				condition.put("dmsnName", dimension);
				condition.put("entityName",fEntity);
				List<DimensionEntity> dimensionEntityList = this.dimensionEntityService.getDimensionEntityList(condition);
				if(null == dimensionEntityList || dimensionEntityList.size() < 1){
					response.setSuccess(false);
					response.setInfo("操作失败，产品["+product+"]维度["+dimension+"]无实体["+fEntity+"]");
					return JSON.toJSONString(response);
				}
				
				DimensionEntity dimensionEntity = dimensionEntityList.get(0);
				String dTags = dimensionEntity.getTags(); //值格式：,tag,
				String uTags = "";
				if(null == dTags || dTags.equals("")) {
					if(type.equals("add")){
						uTags = ","+tags+",";
						int tagsLength = StringUtil.getStringLength(uTags);
						if(tagsLength > 200) {
							response.setSuccess(false);
							response.setInfo("操作失败，产品["+product+"]维度["+dimension+"]实体["+fEntity+"]，标签的长度不能超过200");
							return JSON.toJSONString(response);
						}
						condition.put("title", uTags);
						this.dimensionEntityService.updateDimensionEntity(condition);
					}
				}else {
					uTags = dTags;
					if(type.equals("add")){
						for(String tag: tags.split(",")) {
							if(uTags.indexOf(","+tag+",") == -1) {
								uTags = uTags + tag + ",";
							}
						}
					}else{
						for(String tag: tags.split(",")) {
							if(uTags.indexOf(","+tag+",") != -1) {
								uTags = uTags.replaceAll(","+tag+",", ",");
							}
						}
						if(uTags.equals(",")){
							uTags = "";
						}
					}
					if(uTags.length() != dTags.length()) {
						int tagsLength = StringUtil.getStringLength(uTags);
						if(tagsLength > 200) {
							response.setSuccess(false);
							response.setInfo("操作失败，产品["+product+"]维度["+dimension+"]实体["+fEntity+"]，标签的长度不能超过200");
							return JSON.toJSONString(response);
						}
						condition.put("title", uTags);
						this.dimensionEntityService.updateDimensionEntity(condition);
					}
				}
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("batch save tags failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	//查询产品维度列表
	/*@GetMapping(value="/dmsn/dmsnDownloadTmpl")
	public void dmsnDownloadTmpl(HttpServletResponse response) {
		try{
			String downloadFileName = "temp_dimension.xls";
			String tempPath = DimensionController.class.getClassLoader().getResource("").getPath()+downloadFileName;
			File f = new File(tempPath);
			if(f.exists()){
				response.setHeader("content-disposition", "attachment;filename="+downloadFileName);
				InputStream is = null;
				OutputStream os = null;
				try{
					is = new FileInputStream(f);
					int len = 0;
					byte[] buffer = new byte[1024];
					os = response.getOutputStream();
					while((len = is.read(buffer)) > 0){
						os.write(buffer,0,len);
					}
					os.flush();
				}catch(Exception e2){
					e2.printStackTrace();
				}finally{
					if(null != is){
						is.close();
					}
					if(null != os){
						os.close();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
	
	//保存维度
	/*@PostMapping(value="/dmsn/dmsnSaveImport")
	public String dmsnSaveImport(@RequestParam("uFile") MultipartFile multipartFile) {
		RestResponse response = new RestResponse();
		String errInfos = "";
		try{
			Workbook tWorkbook;
	    	if (multipartFile.getOriginalFilename().lastIndexOf("xlsx") != -1) { // 2007
	    		tWorkbook = new XSSFWorkbook(multipartFile.getInputStream());
			} else { // 2003
				tWorkbook = new HSSFWorkbook(multipartFile.getInputStream());
			}
	    	Sheet tSheet = tWorkbook.getSheet("维度实体");
	    	if(null == tSheet){
	    		response.setSuccess(false);
	    		response.setInfo("上传的文件中不存在SHEET名称为维度实体");
	    		return JSON.toJSONString(response);
	    	}
	    	List<String> pdList = new ArrayList<String>(); //存：产品+维度   过滤重复
			List<DmsnEntity> deList = new ArrayList<DmsnEntity>();
			Map<String,Object> filterRepeatRowMap = new HashMap<String,Object>(); //过滤重复行（“产品+维度+实体”重复，认为是重复行）
			for(int r=1;r<tSheet.getLastRowNum() + 1; r++){
				Row tRow = tSheet.getRow(r);
	    		if(null != tRow){
	    			String product = "";
		    		String dimension = "";
		    		String entity = "";
		    		String tags = "";
	    			boolean rowIsNull = true; //判断是不是空行（空行忽略）
	    			String rowErrInfo = "第"+(r+1)+"行,"; //判断当前行有没有出错信息
	    			for(int c=0;c<tRow.getLastCellNum();c++){
		    			Cell tCell = tRow.getCell(c);
		    			String colValue = null;
		    			if(tCell != null){
		    				colValue = ExcelUtils.getCellValue(tCell,tCell.getCellType());
		    				if(null != colValue){
		    					colValue = colValue.trim();
		    				}
		    			}
		    			if(null != colValue && !colValue.equals("")){
		    				rowIsNull = false;
	    				}
		    			if(c == 0){//产品
		    				if(null != colValue && !colValue.equals("")){
		    					product = colValue;
		    				}
		    			}if(c == 1){//维度
		    				if(null != colValue && !colValue.equals("")){
		    					dimension = colValue;
		    				}
		    			}if(c == 2){//实体
		    				if(null != colValue && !colValue.equals("")){
		    					entity = colValue;
		    				}
		    			}if(c == 3){//标签
		    				if(null != colValue && !colValue.equals("")){
		    					tags = colValue;
		    				}
		    			}
	    			}
	    			if(product.equals("")){
		    			rowErrInfo += "产品不能为空,";
		    		}
	    			if(dimension.equals("")){
		    			rowErrInfo += "维度不能为空,";
		    		}
	    			if(!rowIsNull){//不是空行
	    				if(!rowErrInfo.equals("第"+(r+1)+"行,")){
	    					errInfos = errInfos + rowErrInfo.substring(0,rowErrInfo.length()-1) + ";";
			    		}else{
			    			if(null == filterRepeatRowMap.get(product+"^^^"+dimension+"^^^"+entity)){
			    				if(!pdList.contains(product+"^^^"+dimension)){
			    					pdList.add(product+"^^^"+dimension);
			    				}
			    				filterRepeatRowMap.put(product+"^^^"+dimension+"^^^"+entity, "");
			    				if(errInfos.equals("")){
			    					DmsnEntity de = new DmsnEntity();
			    					de.setProduct(product);
			    					de.setName(dimension);
			    					de.setEntity(entity);
			    					de.setTags(tags);
			    					deList.add(de);
				    			}
			    			}else{
			    				errInfos = errInfos + rowErrInfo + "有重复[" + (product+"^^^"+dimension+"^^^"+entity) + "];";
			    			}
			    		}
		    	    }
	    		}
			}
			if(!errInfos.equals("")){
				response.setSuccess(false);
				errInfos = errInfos.substring(0,errInfos.length()-1);
	    		response.setInfo("上传失败："+errInfos);
	    		return JSON.toJSONString(response);
			}
			if(pdList.size() > 0 || deList.size() > 0){
				//1.（1）产品维度存在：删除产品维度下所有实体、标签；      （2）产品维度不存在：新增产品维度，不管产品维度下的实体标签
				for(String fStr : pdList){
					String strArr[] = fStr.split("\\^\\^\\^");
					String tProduct = strArr[0];
					String tDimension = strArr[1];
					String tProductKey = tProduct+":dmsn";
					if(redisService.exists(tProductKey)){//存在产品key(wws:dmsn)
						Set<String> tDimensionSet = redisService.smembers(tProductKey);
						if(tDimensionSet.contains(tDimension)){//当前产品包含当前的维度
							//删除{product}:dmsn:{dmsn}下的维度实体
							String tDimensionKey = tProductKey + ":" + tDimension;
							if(redisService.exists(tDimensionKey)){//存在产品/维度key(wws:dmsn)
								//取维度下所有实体
								List<String> tEntityList = redisService.lrange(tDimensionKey, 0, -1); //取维度下所有实体
								//删除{product}:dmsn:{dmsn}:titile:{tag}中所有tag中涉及该维度的实体
	    						String tTagKey = tDimensionKey + ":title:*"; //取维度下所有标签key
	    						Set<String> tagKeySet = redisService.keys(tTagKey);
	    						for(String fTagKey: tagKeySet) {//循环标签key
	    							List<String> tEntityList2 = redisService.lrange(fTagKey, 0, -1); //取标签下所有实体
	    							for(String fEntity2 : tEntityList2){
	    								for(String fEntity : tEntityList){
	    									if(fEntity2.equals(fEntity)){
	    										redisService.lrem(fTagKey, 0, fEntity2);
	    										break;
	    									}
	    								}
	    							}
	    						}
								redisService.delete(tDimensionKey);
							}
						}else{//当前产品不包含当前的维度
							redisService.sadd(tProductKey, tDimension);
						}
					}else{
						redisService.sadd(tProductKey, tDimension);
					}
				}
				//2.直接新增产品维度/实体标签
				for(DmsnEntity fDe : deList){
					if(null == fDe.getEntity() || fDe.getEntity().equals("")){
						continue;
					}
					//产品key = {product}:dmsn
					String tProductKey = fDe.getProduct()+":dmsn";
					//维度key = {product}:dmsn:{dmsn}
					String tDimensionKey = tProductKey + ":" + fDe.getName();
					//向维度key值中插入实体
					redisService.lrightPush(tDimensionKey, fDe.getEntity());
					if(null != fDe.getTags() && !fDe.getTags().equals("")){
						String tTagArr[] = fDe.getTags().split(",");
						for(String fTag : tTagArr){
							//标签key = {product}:dmsn:{dmsn}:title:{tag}
							String tTagKey = tDimensionKey + ":title:"+fTag;
							//向标签key值中插入实体
							redisService.lrightPush(tTagKey, fDe.getEntity());
						}
					}
				}
			}
			response.setSuccess(true);
		}catch(Exception e){
			response.setSuccess(false);
			response.setInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}*/
	
	//查询产品维度列表
	/*@GetMapping(value="/dmsn/dmsnExport")
	public void dmsnExport(HttpServletResponse response,String qProduct,String qDimension,String pAndDs) {
		FileOutputStream fos = null;
		File expFile = null;
		try{
			if(null != qProduct && !qProduct.trim().equals("")){
				qProduct = URLDecoder.decode(qProduct,"UTF-8");
			}
			if(null != qDimension && !qDimension.trim().equals("")){
				qDimension = URLDecoder.decode(qDimension,"UTF-8");
			}
			if(null != pAndDs && !pAndDs.equals("")){
				pAndDs = URLDecoder.decode(pAndDs,"UTF-8");
			}
			//System.out.println("************: "+pAndDs);
			List<DmsnEntity> deList = new ArrayList<DmsnEntity>();
			if(null != pAndDs && !pAndDs.equals("")){
				//System.out.println("pAndDs: ");
				String pAndDArr[] = pAndDs.split("\\|");
				for(String pAndD : pAndDArr){
					addDimension(pAndD.split(":")[0],pAndD.split(":")[1],deList);
				}
			}else{
				//System.out.println("qProduct: "+qProduct+", qDimension: "+qDimension);
				String tProductKeys = "*:dmsn";
				Set<String> tProductKeySet = redisService.keys(tProductKeys);
				// 为满足“产品”字段模糊查询，将所有可能满足的key都进行查询
				for(String fProductKey: tProductKeySet) {
					String[] prod_dmsn = fProductKey.split(":");
					if(prod_dmsn.length == 2) {
						String fProduct = prod_dmsn[0];
						if(null != qProduct && !qProduct.trim().equals("")){
							if(fProduct.indexOf(qProduct) == -1){
								continue;
							}
						}
						List<String> tDimensionList = new ArrayList<String>();
						Set<String> dimensionSet = redisService.smembers(fProductKey);
						if(null != qDimension && !qDimension.trim().equals("")){
							for(String fDimension : dimensionSet){
								if(fDimension.indexOf(qDimension) != -1){
									tDimensionList.add(fDimension);
								}
							}
						}else{
							for(String fDimension : dimensionSet){
								tDimensionList.add(fDimension);
							}
						}
						if(tDimensionList.size() > 0){
							for(String fDimenSion : tDimensionList){
								addDimension(fProduct,fDimenSion,deList);
							}
						}
					}
				}
			}
			String tempFileName = "temp_dimension.xls";
			String tempPath = DimensionController.class.getClassLoader().getResource("").getPath()+tempFileName;
			File tempFile = new File(tempPath);
			if(tempFile.exists()){
				expFile = new File(DimensionController.class.getClassLoader().getResource("").getPath()+"/temp/export_dimension_"+System.currentTimeMillis()+".xls");
				//System.out.println("************: " + expFile.getAbsolutePath());
				
				FileUtils.copyFile(tempFile, expFile);
				
				Workbook wb = new HSSFWorkbook(new FileInputStream(expFile));
				// 找到目标SHEET页
				Sheet sheet = wb.getSheet("维度实体");
				if (sheet == null) {
					throw new Exception("Excel导出模板不合法,没有找到sheet页:'维度实体'");
				}
				int rowNo = 1;
				if(deList.size() > 0){
					for(DmsnEntity fDimensionEntity : deList){
						if(rowNo >= ExcelUtils.excelMaxRow) break;  //excel最大行数65535
						Row row = sheet.createRow(rowNo);
						row.createCell(0).setCellValue(fDimensionEntity.getProduct());
						row.createCell(1).setCellValue(fDimensionEntity.getName());
						row.createCell(2).setCellValue(fDimensionEntity.getEntity());
						row.createCell(3).setCellValue(fDimensionEntity.getTags());
						rowNo++;
					}
				}
				fos = new FileOutputStream(expFile);
				wb.write(fos);
				
				response.setHeader("content-disposition", "attachment;filename=export_dimension.xls");
				InputStream is = null;
				OutputStream os = null;
				try{
					is = new FileInputStream(expFile);
					int len = 0;
					byte[] buffer = new byte[1024];
					os = response.getOutputStream();
					while((len = is.read(buffer)) > 0){
						os.write(buffer,0,len);
					}
					os.flush();
				}catch(Exception e2){
					throw e2;
				}finally{
					if(null != is){
						is.close();
					}
					if(null != os){
						os.close();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{ if (fos != null) fos.close(); }catch(Exception e2){ }
			try{
				if (null != expFile ) expFile.delete();
			}catch(Exception e3){
				e3.printStackTrace();
			}
		}
	}*/
	
	/*private void addDimension(String product,String dimension,List<DmsnEntity> deList){
		String dimensionKey = RedisKey.getDimsnKey(product, dimension);
		if(redisService.exists(dimensionKey)){
			//取维度下所有实体
			List<String> dmsnEntities = redisService.lrange(dimensionKey, 0, -1); //取维度下所有实体
			for(String dmsnEntity : dmsnEntities){
				DmsnEntity de = new DmsnEntity();
				de.setProduct(product);
				de.setName(dimension);
				de.setEntity(dmsnEntity);
				String tags = "";
				String tagPatternKey = RedisKey.getDimsnTitlePatternKey(product, dimension); //取维度下所有标签key
				Set<String> tagKeySet = redisService.keys(tagPatternKey); 
				for(String tagKey: tagKeySet) {//循环标签key
					List<String> entitiesInTag = redisService.lrange(tagKey, 0, -1); //取标签下所有实体
					if(entitiesInTag.contains(dmsnEntity)){
						tags += tagKey.split(":")[4] + ",";
					}
				}
				if(StringUtil.hasText(tags)){
					tags = tags.substring(0, tags.length()-1);
					de.setTags(tags);
				}
				deList.add(de);
			}
		}else{
			DmsnEntity de = new DmsnEntity();
			de.setProduct(product);
			de.setName(dimension);
			deList.add(de);
		}
	}*/
	
}
