package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Setting;

public interface SettingMapper {
	public List<Setting> querySettingList(Map<String, Object> condition);
	
	public void updateSetting(Setting setting);
}
