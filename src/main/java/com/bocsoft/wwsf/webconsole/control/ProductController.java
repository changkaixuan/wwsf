package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.bocsoft.wwsf.webconsole.cryptor.EncryptService;
import com.bocsoft.wwsf.webconsole.encoder.PasswordEncoder;
import com.bocsoft.wwsf.webconsole.model.ProductCertification;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.bean.shiro.WwsfSessionFilter;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.service.ProductService;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.github.pagehelper.PageInfo;

@RestController
public class ProductController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ProductService productService;
	
	@Autowired
	private SysLogService sysLogService;

	@Autowired
    PasswordEncoder passwordEncoder;

	@Autowired
	EncryptService encryptService;



	@GetMapping("/product/certification")
	public RestResponse selectProductCertification(String product, String authId, Integer pageNumber, Integer pageSize){
		RestResponse response = new RestResponse();
        try {
        	Map<String,Object> condition = new HashMap<String,Object>();
        	condition.put("pId", product);
        	if(null != authId && !authId.equals("")) {
        		condition.put("lAuthId", authId);
        	}
            PageInfo<ProductCertification> p=productService.selectCertificationByAuthId(condition, pageNumber,pageSize);
            response.setSuccess(true);
            response.put("rows",p.getList());
            response.put("total",p.getTotal());
        }
        catch (Exception e){
            response.setSuccess(false);
            response.setInfo(e.getMessage());
        }
		return response;
	}

	@PostMapping(value="/product/addcertification" ,produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
	public String addProductCertification(@RequestBody ProductCertification productCertification, HttpServletRequest request){
		RestResponse response = new RestResponse();
		String pass=productCertification.getPublicKey();

		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("pId", productCertification.getpId());
			conditions.put("authId", productCertification.getAuthId());
			List<ProductCertification> productCertificationList = productService.queryProductCertification(conditions);
			if(null != productCertificationList && productCertificationList.size()>0) {
				response.setInfo("新增失败。产品【"+productCertification.getpId()+"】、认证信息编号【"+productCertification.getAuthId()+"】，已存在。");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
            productCertification.setPublicKey(encryptService.encryptInfo(pass));
            productService.insertProductCertification(productCertification);
            response.setSuccess(true);

        }catch (Exception e){
		    logger.error("/product/addcertification",e);
		    response.setSuccess(false);
		    response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
        }
		return JSONObject.toJSONString(response);
	}

    @PostMapping(value="/product/editcertification" ,produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String editProductCertification(@RequestBody ProductCertification productCertification){
        RestResponse response = new RestResponse();
        String pass=productCertification.getPublicKey();

        try {
            ProductCertification var_productCertification=productService.selectCertificationByKey(productCertification);
            if (!var_productCertification.getPublicKey().equals(pass)) {
                productCertification.setPublicKey(encryptService.encryptInfo(pass));
            }
            productService.updateProductCertification(productCertification);
            response.setSuccess(true);

        }catch (Exception e){
            logger.error("/product/editcertification",e);
            response.setSuccess(false);
            response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
        }
        return JSONObject.toJSONString(response);
    }


	@PostMapping(value="/product/deletecertification" ,produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String deleteProductCertification(@RequestBody ArrayList<ProductCertification> productCertification){
		HashMap<String,Object> condition=new HashMap<String,Object>();
		ArrayList<String> authIds=new ArrayList<String>();
		String pId="";
		for (ProductCertification p:productCertification){
			authIds.add(p.getAuthId());
			pId=p.getpId();
		}
		condition.put("pId",pId);
		condition.put("authIds",authIds);
		RestResponse response = new RestResponse();
		try {
//			productService.deleteProductCertification(productCertification);
			productService.deleteProductCertification(condition);
			logger.debug(productCertification.toString());
			response.setSuccess(true);

		}catch (Exception e){
			logger.error("/product/editcertification",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}


	@GetMapping(value="/product/certificationByAuthId")
	@ResponseBody
	public String selectProductCertificationByAuthId(String pId,String authId, Integer pageNumber, Integer pageSize){
		HashMap<String,Object> condition=new HashMap<String,Object>();
		condition.put("pId",pId);
		condition.put("authId",authId);
		RestResponse response = new RestResponse();
		try {
			PageInfo<ProductCertification> p=productService.selectCertificationByAuthId(condition, pageNumber, pageSize);
			response.put("rows",p.getList());
			response.put("total",p.getTotal());
			response.setSuccess(true);

		}catch (Exception e){
			logger.error("/product/editcertification",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}



	@GetMapping("/product/selectProduct")
	public RestResponse selectProduct() {
		RestResponse response = new RestResponse();
		try {
			SysUser sysUser = (SysUser)SecurityUtils.getSubject().getSession().getAttribute(WwsfSessionFilter.Session_CurLoginSysUser);
			response.put("products", productService.getProductByUserId(sysUser.getLoginName()));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/product/selectProduct", e);
			response.setSuccess(false);
			response.setInfo("获取产品列表失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/product/selectProductByName")
	@RequiresPermissions("menu:product-list")
	public RestResponse selectProductByName(String product, Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("lName", product);
			PageInfo<Product> productPage = productService.selectProductPage(condition, pageNumber, pageSize);
			response.put("rows", productPage.getList());
			response.put("total", productPage.getTotal());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/product/selectProduct", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping(value="/product/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public String get(String pId) {
		RestResponse response = new RestResponse();
		try {
			Product tProduct = productService.getProduct(pId);
			if (null != tProduct) {
				response.put("product", tProduct);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get product failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/product/insertProduct",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String insertProduct(@RequestBody Product product, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Product tProduct = productService.getProduct(product.getpId());
			if(null != tProduct){
				response.setInfo("保存失败.产品已存在");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			productService.insertProduct(product);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("insert product failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("产品管理");
			sysLog.setOptName("新增");
			sysLog.setLogDesc(String.format("用户[%s]新增了产品[%s]", sysLog.getUserId(), product.getpId()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@PostMapping(value="/product/updateProduct",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String updateProduct(@RequestBody Product product, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Product tProduct = productService.getProduct(product.getpId());
			if(null == tProduct){
				response.setInfo("保存失败,当前产品不存在");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			productService.updateProduct(product);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update product failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("产品管理");
			sysLog.setOptName("修改");
			sysLog.setLogDesc(String.format("用户[%s]修改了产品[%s]信息：pName=%s, pDesc=%s", 
							sysLog.getUserId(), product.getpId(), product.getpName(), product.getpDesc()));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping(value="/product/deleteProduct",produces=MediaType.APPLICATION_JSON_VALUE)
	public String deleteProduct(String pids, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> condition = new HashMap<>();
			condition.put("pids", pids);
			// 查询产品下面是否有job模板，维度，定时任务等
			// 若有，询问是否确定删除
			// 若没有，直接删除
			boolean containsJDC = productService.confirmDelete(condition);
			if (containsJDC) {
				response.setInfo("confrimDel");
			}else {
				condition.put("directDel", "directDel");
				productService.deleteProduct(condition);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("delete product failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("产品管理");
			sysLog.setOptName("删除");
			sysLog.setLogDesc(String.format("用户[%s]删除了产品[%s]", sysLog.getUserId(), pids));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping(value="/product/confirmDeleteProduct",produces=MediaType.APPLICATION_JSON_VALUE)
	public String confirmDeleteProduct(String pids, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> condition = new HashMap<>();
			condition.put("pids", pids);
			productService.deleteProduct(condition);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("confirm delete product failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		// 记录用户操作日志
		try {
			SysLog sysLog = sysLogService.getSysLogTemplate(request);
			sysLog.setModName("产品管理");
			sysLog.setOptName("删除");
			sysLog.setLogDesc(String.format("用户[%s]删除了产品[%s]", sysLog.getUserId(), pids));
			sysLog.setOptRst(response.isSuccess()?"成功":"失败");
			sysLogService.insertSysLog(sysLog);
		} catch (Exception e) {
			logger.error("插入用户日志异常：",e);
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping(value="/product/getProductCertification",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getProductCertification(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				condition.put("pId", product);
			}
			condition.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			List<ProductCertification> productCertificationList = productService.queryProductCertification(condition);
			if (null != productCertificationList) {
				response.put("productCertificationList", productCertificationList);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get productCertification failed ", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
}
