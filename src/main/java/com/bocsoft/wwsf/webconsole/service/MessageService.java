package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.Message;
import com.github.pagehelper.PageInfo;

public interface MessageService {
	
	public long countMessageByLoginName(Map<String,Object> condition);
	
	public List<Message> getMessageByLoginName(Map<String,Object> condition);

	public PageInfo<Message> queryMessage(Map<String, Object> condition,int pageNo, int pageSize);
	
	public Message getMessage(String id) throws Exception;
	
	public void updateMessage(String ids) throws Exception;
	
}
