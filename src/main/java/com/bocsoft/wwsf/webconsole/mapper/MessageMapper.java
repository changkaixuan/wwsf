package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.Message;

public interface MessageMapper {

	public long countMessageByLoginName(Map<String,Object> condition);
	
	public List<Message> getMessageByLoginName(Map<String,Object> condition);
	
	public List<Message> queryMessage(Map<String,Object> condition);
	
	public int updateMessage(Map<String,Object> condition);
	
}
