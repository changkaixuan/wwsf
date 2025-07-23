package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.bocsoft.wwsf.webconsole.model.ExportJob;
import com.bocsoft.wwsf.webconsole.model.ImportJob;
import com.bocsoft.wwsf.webconsole.model.Job;
import com.github.pagehelper.PageInfo;

public interface JobService {
	
	public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";
	
	public PageInfo<Job> queryJobPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception;
	
	public List<Job> getJobList(Map<String, Object> condition) throws Exception;
	
	public Job getJob(String product,String jobId) throws Exception;
	
	public void saveJob(String type,Job job) throws Exception;
	
	public void deleteJob(String[] jobIdArr) throws Exception;
	
	public List<Job> getJobListIgnoreMode() throws Exception;
	
	public List<Map<String,Object>> getJobCountByProduct(Map<String, Object> condition) throws Exception;
	
	public int copyAddJob(Job source, Job target) throws Exception;
	
	public ExportJob getExportJob(String exportFileCatalog,List<Job> jobList);
	
	public void genExcelFile(HttpServletResponse response, ExportJob exportJob, String exportFileName);

	public ImportJob getImportJob(String productId,MultipartFile multipartFile) throws Exception;

	public void insertImportJob(ImportJob importJob) throws Exception;

	public void genSqlFile(Object object, ExportJob exportJob, String exportFileName, String driverClass);
	
}
