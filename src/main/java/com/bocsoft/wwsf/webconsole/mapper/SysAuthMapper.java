package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;
import com.bocsoft.wwsf.webconsole.model.SysAuth;

public interface SysAuthMapper {
	
	public List<SysAuth> querySysAuth(Map<String, Object> condition);	
	
	public List<SysAuth> getSysAuthByUserId(Map<String, Object> condition);
	
}
