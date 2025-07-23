package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.JobUpload;

public interface JobUploadMapper {

	public List<JobUpload> queryJobUpload(Map<String,Object> condition);
	
	public int insertJobUpload(JobUpload job);
	
	public int updateJobUpload(JobUpload job);
	
	public int deleteJobUpload(Map<String,Object> dMap);
	
}
