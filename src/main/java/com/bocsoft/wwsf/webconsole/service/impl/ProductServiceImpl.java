package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.ProductCertification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.CronLogMapper;
import com.bocsoft.wwsf.webconsole.mapper.CronMapper;
import com.bocsoft.wwsf.webconsole.mapper.DataSourceMapper;
import com.bocsoft.wwsf.webconsole.mapper.DimensionEntityMapper;
import com.bocsoft.wwsf.webconsole.mapper.DimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.JobDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.JobInstanceMapper;
import com.bocsoft.wwsf.webconsole.mapper.JobMapper;
import com.bocsoft.wwsf.webconsole.mapper.ParameterMapper;
import com.bocsoft.wwsf.webconsole.mapper.ProductMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskInstanceMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskLeanMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskSpecialLeanMapper;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.service.CronService;
import com.bocsoft.wwsf.webconsole.service.DataSourceService;
import com.bocsoft.wwsf.webconsole.service.DimensionService;
import com.bocsoft.wwsf.webconsole.service.JobInstanceService;
import com.bocsoft.wwsf.webconsole.service.JobService;
import com.bocsoft.wwsf.webconsole.service.ProductService;
import com.bocsoft.wwsf.webconsole.service.TaskInstanceService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("productService")
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	private ProductMapper productMapper;
	@Autowired
	private TaskInstanceMapper taskInstanceMapper;
	@Autowired
	private JobInstanceMapper jobInstanceMapper;
	@Autowired
	private JobDimensionMapper jobDimensionMapper;
	@Autowired
	private DimensionMapper dimensionMapper;
	@Autowired
	private DimensionEntityMapper dimensionEntityMapper;
	@Autowired
	private ParameterMapper parameterMapper;
	@Autowired
	private TaskSpecialLeanMapper taskSpecialLeanMapper;
	@Autowired
	private TaskLeanMapper taskLeanMapper;
	@Autowired
	private TaskMapper taskMapper;
	@Autowired
	private TaskDimensionMapper taskDimensionMapper;
	@Autowired
	private JobMapper jobMapper;
	@Autowired
	private CronMapper cronMapper;
	@Autowired
	private CronLogMapper cronLogMapper;
	@Autowired
	private DataSourceMapper dataSourceMapper;
	@Autowired
	JobService jobService;
	
	@Autowired
	JobInstanceService jobInstanceService;
	
	@Autowired
	TaskInstanceService taskInstanceService;
	
	@Autowired
	DimensionService dimensionService;
	
	@Autowired
	CronService cronService;
	
	@Autowired
	DataSourceService dataSourceService;
	
	/*@Autowired
	private JobMapper jobMapper;
	
	public PageInfo<Job> queryJobPage(Map<String, Object> condition,int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobMapper.queryJobPage(condition);
			}
		});
	}*/

	public List<Product> selectProduct(Map<String, Object> condition) throws Exception{
//		List<Product> productsList = new ArrayList<Product>();
//		Product pe = new Product();
//		pe.setName("wws");
//		pe.setDescription("wws");
//		productsList.add(pe);
//		return productsList;
		return productMapper.selectProductByName(condition);
	}

	public PageInfo<Product> selectProductPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				productMapper.selectProductByName(condition);
			}
		});
	}
		
	@Override
	public List<Product> selectProductByName(Map<String, Object> condition) throws Exception {
		return productMapper.selectProductByName(condition);
	}
	
	@Override
	public Product getProduct(String pId) throws Exception {
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("pId", pId);
		List<Product> pList = productMapper.selectProductByName(condition);
		if(null != pList && pList.size()>0) {
			return pList.get(0);
		}
		return null;
	}
	
	@Override
	@Transactional
	public int insertProduct(Product product) throws Exception {
		return productMapper.insertProduct(product);
	};
	
	@Override
	public int deleteProduct(Map<String, Object> condition) throws Exception {
		String[] pids = ((String)condition.get("pids")).split(",");
		int result = -1;
		
		if (!condition.containsKey("directDel")) {
			for (String pid : pids) {
				/*Map<String, Object> map = new HashMap<>();
				map.put("product", pid);
				// 删除产品相关 实例
				taskInstanceMapper.delTaskInsLeanByProduct(pid);
				taskInstanceMapper.delTaskInsByProduct(pid);
				jobInstanceMapper.delJobInsByProduct(pid);
				
				// 删除产品相关 维度
				dimensionEntityMapper.deleteDimensionEntityByProduct(pid);
				dimensionMapper.deleteDimensionByProduct(pid);
				parameterMapper.deleteParameterByProduct(pid);
				jobDimensionMapper.deleteJobDimensionByProduct(pid);
				// 删除产品相关 计划任务
				List<String> cronIdList = cronService.getCronIdByProduct(map);
				if(cronIdList!=null && cronIdList.size()>0) {
					String[] cronIdArr = new String[cronIdList.size()];
					cronIdList.toArray(cronIdArr);
					cronService.deleteCron(cronIdArr, false);
				}
				
				// 删除产品相关 数据源
				List<DataSource> dsList = dataSourceService.getDataSourceList(map);
				if (dsList!=null && dsList.size()>0) {
					dataSourceService.batchDeleteDataSource(dsList);
				}*/
				
				// 删除 9张码表信息
				deleteProductFirst(pid);
				// 删除cron cron_log datasource
				deleteProductSecond(pid);
				// 删除实例
				deleteProductThird(pid);
				//删除产品和用户关系
				this.deleteSysUserProductRef(null, pid);
			}
		}
		
		// 删除 产品
		for (String pid : pids) {
			condition.put("name", pid);
			result = productMapper.deleteProduct(condition);
			result = productMapper.deleteCertificationByProduct(condition);
		}
		return result;
	}

	@Override
	public int updateProduct(Product product) throws Exception {
		return productMapper.updateProduct(product);
	}

	@Override
	public boolean confirmDelete(Map<String, Object> condition) throws Exception {
		String pids = (String) condition.get("pids");
		String[] pidArr = pids.split(",");
		Map<String, Object> map = new HashMap<>();
		map.put("pidArr", pidArr);
		/*return jobService.getJobCountByProduct(map)>=1 ? true :  // 查询产品下是否有job模板
			   (dimensionService.getDimensionCountByProduct(map)>=1 ? true : // 查询产品下是否有维度
			    	(cronService.getCronCountByProduct(map)>=1 ? true : // 查询产品下是否有计划任务
			    		dataSourceService.getDataSourceCountByProduct(map)>=1)); // 查询产品下是否有数据源
			};*/
		List<Map<String,Object>> tList = jobService.getJobCountByProduct(map);
		if(null != tList && tList.size()>0){
			return true;
		}
		tList = jobInstanceService.getJobInstanceCountByProduct(map);
		if(null != tList && tList.size()>0){
			return true;
		}
		tList = dimensionService.getDimensionCountByProduct(map);
		if(null != tList && tList.size()>0){
			return true;
		}
		tList = cronService.getCronCountByProduct(map);
		if(null != tList && tList.size()>0){
			return true;
		}
		tList = dataSourceService.getDataSourceCountByProduct(map);
		if(null != tList && tList.size()>0){
			return true;
		}
		List<Product> pList = this.getProductByCondition(map);
		if(null != pList && pList.size()>0){
			return true;
		}
		return false;
	}
	
	public List<Product> getProductByCondition(Map<String, Object> condition) throws Exception{
		return productMapper.getProductByCondition(condition);
	}
	
	public List<Product> getProductByUserId(String userId) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("userId", userId);
		return productMapper.getProductByCondition(condition);
	}
	
	public int insertSysUserProductRef(Map<String, Object> condition) {
		return productMapper.insertSysUserProductRef(condition);
	}
	
	public int deleteSysUserProductRef(String userId,String productId) {
		Map<String, Object> condition = new HashMap<String, Object>();
		if(null != userId && !userId.equals("")){
			condition.put("userId", userId);
		}
		if(null != productId && !productId.equals("")){
			condition.put("productId", productId);
		}
		return productMapper.deleteSysUserProductRef(condition);
	}

	@Override
	public List<ProductCertification> selectProductCertification(Map<String, Object> condition) throws Exception {
		return null;
	}

	@Override
	public int insertProductCertification(ProductCertification productCertification) throws Exception {
		return productMapper.insertCertification(productCertification);
	}

	@Override
	public int updateProductCertification(ProductCertification productCertification) throws Exception {
		return productMapper.updateCertification(productCertification);
	}

	@Override
	public int deleteProductCertification(Map<String,Object> condition) throws Exception {
		return productMapper.deleteCertification(condition);
	}

	@Override
	public PageInfo<ProductCertification> selectCertificationByProduct(String pId, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {
				productMapper.selectCertificationByProduct(pId);
			}
		});
	}


	@Override
	public ProductCertification selectCertificationByKey(ProductCertification productCertification) throws Exception {
		return productMapper.selectCertificationByKey(productCertification);
	}

	@Override
	public PageInfo<ProductCertification> selectCertificationByAuthId(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {
				productMapper.selectCertificationByAuthId(condition);
			}
		});
	}


	@Transactional
	private void deleteProductFirst(String product) {
		if (product==null || "".equals(product)) {
			return;
		}
		// delete from ww_parameter where job_id in (select job_id from ww_job where product='wws');
		parameterMapper.deleteParameterByProduct(product);
		// delete from ww_task_special_lean where job_id in (select job_id from ww_job where product='wws');
		taskSpecialLeanMapper.deleteTaskSpecialLeanByProduct(product);
		// delete from ww_task_lean where job_id in (select job_id from ww_job where product='wws');
		taskLeanMapper.deleteTaskLeanByProduct(product);
		// delete from ww_task where job_id in (select job_id from ww_job where product='wws');
		taskMapper.deleteTaskByProduct(product);
		// delete from ww_dimension_entity where dmsn_name in (select d.dmsn_name from ww_dimension d where d.product = 'wws')
		dimensionEntityMapper.deleteDimensionEntityByProduct(product);
		// delete from ww_job_dimension where job_id in (select j.job_id from ww_job j where j.product = 'wws');
		jobDimensionMapper.deleteJobDimensionByProduct(product);
		// delete from ww_task_dimension  where job_id in (select job_id from ww_job where product='wws');
		taskDimensionMapper.deleteTaskDimensionByProduct(product);
		// delete  from ww_dimension where product='wws';
		dimensionMapper.deleteDimensionByProduct(product);
		// delete from ww_job where product='wws';
		jobMapper.deleteJobByProduct(product);
	}
	
	@Transactional
	private void deleteProductSecond(String product) {
		if (product==null || "".equals(product)) {
			return;
		}
		// delete from ww_cron where product='wws';
		cronMapper.deleteCronByProduct(product);
		// delete from ww_cron_log where product='wws';
		cronLogMapper.deleteCronLogByProduct(product);
		// from ww_ds where product='wws';
		dataSourceMapper.deleteDataSourceByProduct(product);
	}
	
	@Transactional
	private void deleteProductThird(String product) {
		if (product==null || "".equals(product)) {
			return;
		}
		// delete from ww_task_instance_lean where job_ins_id in (select job_ins_id from ww_job_instance where product='wws');
		taskInstanceMapper.delTaskInsLeanByProduct(product);
		// delete from from ww_task_instance where job_ins_id in (select job_ins_id from ww_job_instance where product='wws');
		taskInstanceMapper.delTaskInsByProduct(product);
		// delete from ww_job_instance where product='wws';
		jobInstanceMapper.delJobInsByProduct(product);
	}
	
	public List<ProductCertification> queryProductCertification(Map<String,Object> condition){
		return productMapper.queryProductCertification(condition);
	}
	
}
