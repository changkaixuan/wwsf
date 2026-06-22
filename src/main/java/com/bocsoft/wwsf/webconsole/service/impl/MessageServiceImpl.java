package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bocsoft.wwsf.webconsole.mapper.MessageMapper;
import com.bocsoft.wwsf.webconsole.model.Message;
import com.bocsoft.wwsf.webconsole.service.MessageService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("messageService")
public class MessageServiceImpl implements MessageService{

	@Autowired
	private MessageMapper messageMapper;
	
	public long countMessageByLoginName(Map<String,Object> condition) {
		return messageMapper.countMessageByLoginName(condition);
	}
	
	public List<Message> getMessageByLoginName(Map<String,Object> condition){
		return messageMapper.getMessageByLoginName(condition);
	}
	
	public PageInfo<Message> queryMessage(Map<String, Object> condition,int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				messageMapper.queryMessage(condition);
			}
		});
	}
	
	public Message getMessage(String id) throws Exception{
		Map<String, Object> condition = new HashMap<String, Object>();
		condition.put("id", id);
		List<Message> list = messageMapper.queryMessage(condition);
		if(null == list || list.size()<1) {
			return null;
		}
		return list.get(0);
	}
	
	public void updateMessage(String ids) throws Exception{
		if(null != ids && !ids.equals("")) {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("idArr", ids.split(","));
			condition.put("mRead", "1");
		    messageMapper.updateMessage(condition);
		}
	}
	
}
