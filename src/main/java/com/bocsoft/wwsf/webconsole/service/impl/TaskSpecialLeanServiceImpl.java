package com.bocsoft.wwsf.webconsole.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.TaskSpecialLeanMapper;
import com.bocsoft.wwsf.webconsole.service.TaskSpecialLeanService;

@Service("taskSpecialLeanService")
public class TaskSpecialLeanServiceImpl implements TaskSpecialLeanService {
	
	@Autowired
	private TaskSpecialLeanMapper taskSpecialLeanMapper;
	
}
