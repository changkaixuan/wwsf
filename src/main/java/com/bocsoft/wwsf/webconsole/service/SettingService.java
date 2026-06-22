package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Setting;

public interface SettingService {
	public List<Setting> querySettingList(Map<String, Object> condition) throws Exception;
	
	public void updateSetting(Setting setting) throws Exception;
	
	public void updateSettingList(List<Setting> list) throws Exception;
}
