package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.model.SelectTwo;
import com.bocsoft.wwsf.webconsole.model.Server;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.github.pagehelper.PageInfo;

public interface ServerService {

	public PageInfo<Server> queryServer(Map<String, Object> condition,int pageNo, int pageSize);
	
	public List<Server> getServerList(Map<String, Object> condition);

	public int insertServer(Server server) throws Exception;
	
	public int updateServer(Server server) throws Exception;
	
	public int updateServerOsSshPswd(Server sysUser) throws Exception;
	
	public void deleteServers(List<Server> servers,SysLog sysLog) throws Exception;
	
	public NodeInfo getServer(String product, String serverId) throws Exception;
	
	public List<Server> getServerInstallLog(Map<String, Object> condition) throws Exception;
	
	public Map<String,Object> canInstall(String product,String serverId,Long installCfgVersion) throws Exception;
	
	public void installServer(Server server,SysLog sysLog) throws Exception;
	
	public void uninstallServer(Server server,SysLog sysLog) throws Exception;
	
	public List<SelectTwo> getServerIdAndTags(String product,String agentScope) throws Exception;
	
}
