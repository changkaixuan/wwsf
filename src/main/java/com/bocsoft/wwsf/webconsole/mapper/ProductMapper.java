package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.ProductCertification;

public interface ProductMapper {
	public List<Product> selectProductByName(Map<String, Object> condition);
	
	public int insertProduct(Product product);
	
	public int deleteProduct(Map<String, Object> condition);
	
	public int updateProduct(Product product);
	
	public List<Product> getProductByCondition(Map<String, Object> condition);
	
	public int insertSysUserProductRef(Map<String, Object> condition);
	
	public int deleteSysUserProductRef(Map<String, Object> condition);

	public int insertCertification(ProductCertification productCertification);

	public int updateCertification(ProductCertification productCertification) ;

    public int deleteCertification(Map<String,Object> condition);

	public List<ProductCertification> selectCertificationByProduct(String pId);

	public ProductCertification selectCertificationByKey(ProductCertification productCertification) ;

	public List<ProductCertification>  selectCertificationByAuthId(Map<String,Object> condition);

	public List<ProductCertification> queryProductCertification(Map<String,Object> condition);

	public int deleteCertificationByProduct(Map<String, Object> condition);
	
}
