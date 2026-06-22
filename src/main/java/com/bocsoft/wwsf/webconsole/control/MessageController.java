package com.bocsoft.wwsf.webconsole.control;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.model.Message;
import com.bocsoft.wwsf.webconsole.service.MessageService;
import com.github.pagehelper.PageInfo;

@RestController
public class MessageController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	MessageService messageService;
	
	@GetMapping("/message/load")
	public RestResponse load() {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			conditions.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			conditions.put("fetchRowNum", Message.Message_Fetch_RowNum);
			long newMessageCount = messageService.countMessageByLoginName(conditions);
			List<Message> messageList = messageService.getMessageByLoginName(conditions);
			if(null != messageList && messageList.size()>0) {
				response.put("messageList", messageList);
			}
			response.put("newMessageCount", newMessageCount);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("--- 获取消息列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo("加载消息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/message/list")
	public RestResponse list(
			String product,String mLevel,String mRead,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != mLevel && !mLevel.equals("")) {
				conditions.put("lMLevel", mLevel);
			}
			if(null != mRead && !mRead.equals("")) {
				conditions.put("mRead", mRead);
			}
			conditions.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			PageInfo<Message> pageList = messageService.queryMessage(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取消息列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}
	
	@GetMapping(value="/message/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public String get(String id) {
		RestResponse response = new RestResponse();
		try {
			Message message = messageService.getMessage(id);
			if (null == message) {
				response.setInfo("消息["+id+"]不存在");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			response.put("message", message);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get message failed ",e);
			response.setSuccess(false);
			response.setInfo("查看消息失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@DeleteMapping(value="/message/read/{ids}",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String delete(@PathVariable String ids, HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			messageService.updateMessage(ids);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("message read failed ",e);
			response.setSuccess(false);
			response.setInfo("已阅失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
}
