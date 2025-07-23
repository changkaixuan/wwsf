package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.mapper.ServerMapper;
import com.bocsoft.wwsf.webconsole.mapper.SysLogMapper;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.model.SelectTwo;
import com.bocsoft.wwsf.webconsole.model.Server;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.service.ServerService;
import com.bocsoft.wwsf.webconsole.zoo.ZooService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("serverService")
public class ServerServiceImpl implements ServerService{

	@Autowired
	private ServerMapper serverMapper;
	
	@Autowired
	private SysLogMapper sysLogMapper;
	
	@Autowired
	private ZooService zooService;
	
	public PageInfo<Server> queryServer(Map<String, Object> condition,int pageNo, int pageSize) {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				getServerListPage(condition);
			}
		});
	}

	public List<Server> getServerListPage(Map<String, Object> condition) {
		List<Server> serverList = serverMapper.queryServer(condition);
		if(null != serverList && serverList.size()>0) {
			Map<String,String> nodeInfoMap = null;
			try{
				nodeInfoMap = zooService.getAllNodeInfoKey();
			}catch(Exception e) {
				e.printStackTrace();
				nodeInfoMap = new HashMap<String,String>();
			}
			for(Server fServer : serverList) {
				if(fServer.getStatus() == 2) {//安装成功或脱机
					if(null != nodeInfoMap.get(fServer.getProduct()+","+fServer.getServerId())) {
						fServer.setStatus(Server.ServerInstallLog_Status_Start);
					}
				}
			}
		}
		return serverList;
	}
	
	public List<Server> getServerList(Map<String, Object> condition) {
		return serverMapper.queryServer(condition);
	}
	
	public NodeInfo getServer(String product, String serverId) {
		return serverMapper.selectByPrimaryKey(product, serverId);
	}
	
	public int insertServer(Server sysUser) throws Exception{
		return serverMapper.insertServer(sysUser);
	}
	
	public int updateServer(Server sysUser) throws Exception{
		return serverMapper.updateServer(sysUser);
	}
	
	@Transactional
	public void deleteServers(List<Server> servers,SysLog sysLog) throws Exception{
		List<Server> serverList = new ArrayList<>();
		Map<String,Object> delMap = new HashMap<String,Object>();
		String prodAndServerIdArr[] = new String[servers.size()];
		for(int i=0;i<servers.size();i++) {
			Server fServer = servers.get(i);
			prodAndServerIdArr[i] = fServer.getProduct()+","+fServer.getServerId();
			serverList.add(fServer);
		}
		delMap.put("prodAndServerIdArr", prodAndServerIdArr);
		serverMapper.deleteServers(delMap);
		if(serverList.size() > 0) {
			for(Server fServer : serverList) {
				sysLog.setId(String.valueOf(System.currentTimeMillis()));
				sysLog.setLogDesc(JSONObject.toJSONString(fServer));
				sysLogMapper.insertSysLog(sysLog);
			}
		}
	}
	
	public int updateServerOsSshPswd(Server sysUser) throws Exception{
		return serverMapper.updateServerOsSshPswd(sysUser);
	}
	
	public List<Server> getServerInstallLog(Map<String, Object> condition) throws Exception{
		return serverMapper.getServerInstallLog(condition);
	}
	
	public Map<String,Object> canInstall(String product,String serverId,Long installCfgVersion) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("product", product);
		condition.put("serverId", serverId);
		List<Server> serverList = serverMapper.queryServer(condition);
		String result = "";
		Server server = null;
		if(null == serverList || serverList.size() < 1) {
			result = "安装失败，服务器不存在";
		}else{
			server = serverList.get(0);
			if(server.getStatus() == Server.ServerInstallLog_Status_InstallSuccess) {
				result = "安装失败，服务器已安装";
			}
			//验证安装配置版本号
			if(installCfgVersion.longValue() != server.getInstallCfgVersion().longValue()) {
				result = "安装失败，服务器已安装";
			}
		}
		Map<String,Object> retMap = new HashMap<String,Object>();
		retMap.put("result", result);
		if(result.equals("")) {
			condition = new HashMap<String,Object>();
			condition.put("product", product);
			condition.put("serverId", serverId);
			serverList = serverMapper.queryServer(condition);
			retMap.put("server", server);
		}
		return retMap;
	}
	
//	protected synchronized String canInstall(List<Server> serverList) throws Exception{
//		if(null != serverList && serverList.size()>0) {
//			return "安装失败，服务器不存在或已安装";
//		}
//		return "";
//	}
	
	@Transactional
	public void installServer(Server server,SysLog sysLog) throws Exception{
		serverMapper.updateServerStatus(server);
		sysLog.setOptRst((server.getStatus() == Server.ServerInstallLog_Status_InstallSuccess)?"成功":"失败");
		sysLogMapper.insertSysLog(sysLog);
	}
	
	@Transactional
	public void uninstallServer(Server server,SysLog sysLog) throws Exception{
		serverMapper.updateServerStatus(server);
		sysLog.setOptRst((server.getStatus() == Server.ServerInstallLog_Status_NotInstall)?"成功":"失败");
		sysLogMapper.insertSysLog(sysLog);
	}
	
	public List<SelectTwo> getServerIdAndTags(String product,String agentScope) throws Exception{
		List<SelectTwo> rList = new ArrayList<>();
		Map<String,Object> qMap = new HashMap<>();
		qMap.put("product", product);
		if(null != agentScope && !agentScope.equals("")) {
			qMap.put("agentScope", agentScope);
		}
		List<SelectTwo> tList = serverMapper.getServerIdAndTags(qMap);
		if(null != tList && tList.size() > 0) {
			Map<String,Object> fTagMap = new HashMap<>(); //过滤标签重复
			for(SelectTwo fSelectTwo : tList) {
				String serverId = fSelectTwo.getServerId();
				if(fSelectTwo.getType().equals(SelectTwo.Type_Id)) {//ww_server/server_id
					rList.add(new SelectTwo(serverId,serverId,fSelectTwo.getType()));  // 服务器ID，服务器ID，类型
				}else {//ww_server/tags
					String tagArr[] = serverId.split(",");
					for(String fTag : tagArr) {
						if(null == agentScope || agentScope.equals("") || fTag.indexOf(agentScope) != -1) {
							String id = SelectTwo.Type_Tag_Start + fTag; // # + 服务器标签
							if(null == fTagMap.get(id)) {
								rList.add(new SelectTwo(id,fTag,fSelectTwo.getType()));  // # + 服务器标签，服务器标签，类型
								fTagMap.put(id, id);
							}
						}
					}
				}
			}
		}
		return rList;
	}
	
}
