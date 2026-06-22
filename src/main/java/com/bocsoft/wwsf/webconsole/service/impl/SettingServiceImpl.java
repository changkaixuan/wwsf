package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.SettingMapper;
import com.bocsoft.wwsf.webconsole.model.Setting;
import com.bocsoft.wwsf.webconsole.service.SettingService;

@Service("settingService")
public class SettingServiceImpl implements SettingService {
	@Autowired
	private SettingMapper settingMapper;

	@Override
	public List<Setting> querySettingList(Map<String, Object> condition) throws Exception {
		return settingMapper.querySettingList(condition);
	}

	@Override
	@Transactional
	public void updateSetting(Setting setting) throws Exception {
		if (setting.getModal()==null || "".equals(setting.getModal())) {
			throw new Exception("参数的类型不能为空");
		}
		if (setting.getName()==null || "".equals(setting.getName())) {
			throw new Exception("参数的名称不能为空");
		}
		settingMapper.updateSetting(setting);
	}

	@Override
	public void updateSettingList(List<Setting> list) throws Exception {
		for (Setting setting : list) {
			updateSetting(setting);
		}
	}
}
