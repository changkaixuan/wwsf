package com.bocsoft.wwsf.webconsole.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bocsoft.wwsf.webconsole.mapper.JobUploadMapper;
import com.bocsoft.wwsf.webconsole.model.JobUpload;
import com.bocsoft.wwsf.webconsole.service.JobUploadService;

@Service
public class JobUploadServiceImpl implements JobUploadService {
	
	@Autowired
	private JobUploadMapper jobUploadMapper;

	@Transactional
	public void insertJobUpload(JobUpload jobUpload) {
		try {
			jobUploadMapper.insertJobUpload(jobUpload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Transactional
	public void updateJobUpload(JobUpload jobUpload) {
		try {
			jobUploadMapper.updateJobUpload(jobUpload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
