package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.model.SelectTwo;
import com.bocsoft.wwsf.webconsole.model.Server;

public interface ServerMapper {

	public List<Server> queryServer(Map<String,Object> condition);
	
	public int insertServer(Server sysUser);
	
	public int updateServer(Server sysUser);
	
	public int updateServerStatus(Server sysUser);
	
	public int updateServerOsSshPswd(Server sysUser);
	
	public int deleteServers(Map<String,Object> dMap);
	
	public NodeInfo selectByPrimaryKey(@Param("product") String product, @Param("serverId") String serverId);
	
	public List<Server> getServerInstallLog(Map<String,Object> condition);
	
	public List<SelectTwo> getServerIdAndTags(Map<String,Object> condition);
	
}
