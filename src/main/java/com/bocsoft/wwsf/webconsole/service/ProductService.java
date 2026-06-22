package com.bocsoft.wwsf.webconsole.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.ProductCertification;
import com.github.pagehelper.PageInfo;

public interface ProductService {

	//取产品表中产品
	public List<Product> selectProduct(Map<String, Object> condition) throws Exception;
	
	public PageInfo<Product> selectProductPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<Product> selectProductByName(Map<String, Object> condition) throws Exception;
	
	public Product getProduct(String pId) throws Exception;
	
	public int insertProduct(Product product) throws Exception;
	
	public int deleteProduct(Map<String, Object> condition) throws Exception;
	
	public int updateProduct(Product product) throws Exception;
	
	public boolean confirmDelete(Map<String, Object> condition) throws Exception;
	
	//取用户关联产品
	public List<Product> getProductByCondition(Map<String, Object> condition) throws Exception;
	public List<Product> getProductByUserId(String userId) throws Exception;
	
	public int insertSysUserProductRef(Map<String, Object> condition);
	
	public int deleteSysUserProductRef(String userId,String productId);



	public List<ProductCertification> selectProductCertification(Map<String, Object> condition) throws Exception;

	public int insertProductCertification(ProductCertification productCertification) throws Exception;

	public int updateProductCertification(ProductCertification productCertification) throws Exception;

	public int deleteProductCertification(Map<String,Object> condition) throws Exception;

	public PageInfo<ProductCertification> selectCertificationByProduct(String pId,int pageNo, int pageSize) throws Exception;

	public ProductCertification selectCertificationByKey(ProductCertification productCertification) throws  Exception;

	public PageInfo<ProductCertification>  selectCertificationByAuthId(Map<String,Object> condition,int pageNo, int pageSize) throws  Exception;

	public List<ProductCertification> queryProductCertification(Map<String,Object> condition);

}
