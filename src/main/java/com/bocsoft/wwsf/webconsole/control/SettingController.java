package com.bocsoft.wwsf.webconsole.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.Setting;
import com.bocsoft.wwsf.webconsole.service.SettingService;

@RestController
public class SettingController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private SettingService settingService;
	
	@GetMapping("/setting/querySettingList")
	public RestResponse querySettingList() {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			List<Setting> settingList = settingService.querySettingList(conditions);
			response.setSuccess(true);
			response.put("settingList", settingList);
		} catch (Exception e) {
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo("获取预警参数失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping("/setting/updateSettingList")
	public RestResponse updateSettingList(@RequestBody List<Setting> list) {
		RestResponse response = new RestResponse();
		try {
			settingService.updateSettingList(list);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping("/setting/updateSetting")
	public RestResponse updateSetting(@RequestBody Setting setting) {
		RestResponse response = new RestResponse();
		try {
			settingService.updateSetting(setting);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return response;
	}
}
