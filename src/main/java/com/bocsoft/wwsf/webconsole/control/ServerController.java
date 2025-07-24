package com.bocsoft.wwsf.webconsole.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.DesEncrypt;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.SshClientUtils;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.cryptor.EncryptService;
import com.bocsoft.wwsf.webconsole.model.DbConn;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.model.Product;
import com.bocsoft.wwsf.webconsole.model.SelectTwo;
import com.bocsoft.wwsf.webconsole.model.Server;
import com.bocsoft.wwsf.webconsole.model.SysLog;
import com.bocsoft.wwsf.webconsole.service.ProductService;
import com.bocsoft.wwsf.webconsole.service.ServerService;
import com.bocsoft.wwsf.webconsole.service.SysLogService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.bocsoft.wwsf.webconsole.websocket.SshConnInfo;
import com.bocsoft.wwsf.webconsole.zoo.ZooService;
import com.github.pagehelper.PageInfo;

@Configuration
@RestController
public class ServerController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ServerService serverService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	private WwseService wwseService;
	
	@Autowired
	ZooService zooService;

	@Value("${zoo.connect.string}")
	private String zooConnectUrl;
	
	@Value("${zoo.namespace}")
	private String zooNamespace;
	
	@Value("${zoo.acl.string:}") //默认值为"". 不设默认值,application.properties中不配置zoo.acl.string,会报错.
	private String zooAclString;
	
	@Value("${file.releasePath}")
	private String fileReleasePath;
	
	@Value("${wws.jmx.user}")
	private String wwsJmxUser;
	
	@Value("${wws.jmx.password}")
	private String wwsJmxPassword;

	/**
	 * SHH 授权认证方式 0-密码认证； 1-公钥认证
	 */
	@Value("${wws.ssh.auth}")
	private String sshAuth;

	/**
	 * SSH私钥路径
	 */
	@Value("${wws.ssh.privateKeyPath}")
	private String privateKeyPath;

	/**
	 * SSH私钥密码（是用于保护私钥文件的密码,可为空）
	 */
	@Value("${www.ssh.passphrase}")
	private String passphrase;

	@Autowired
	private DbConn dbConn;
	
	@Autowired
	EncryptService encryptService;
	
	@Autowired
	private SysLogService sysLogService;
	
	@GetMapping("/server/list")
	@RequiresPermissions("menu:server-list")
	public RestResponse list(
			String product,
			String serverId,  
			String tags,
			Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> conditions = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				conditions.put("product", product);
			}
			if(null != serverId && !serverId.equals("")) {
				conditions.put("lServerId", serverId);
			}
			if(null != tags && !tags.equals("")) {
				conditions.put("lTags", tags);
			}
			conditions.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			PageInfo<Server> pageList = serverService.queryServer(conditions, pageNumber, pageSize);
			response.put("total", pageList.getTotal());
			response.put("rows", pageList.getList());
		} catch (Exception e) {
			logger.error("--- 获取服务列表异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}

	@GetMapping(value="/server/get",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse get(String product, String serverId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> qMap = new HashMap<String,Object>();
			qMap.put("product", product);
            qMap.put("serverId", serverId);
            List<Server> serverList = serverService.getServerList(qMap);
            if(null == serverList || serverList.size() < 1) {
            	response.setInfo("当前服务不存在");
                response.setSuccess(false);
                return response;
            }
            Server server = serverList.get(0);
            if(null == server.getOsSshPswd()) {
            	server.setOsSshPswd("");
            }else {
            	try {
            		server.setOsSshPswd(encryptService.decryptInfo(server.getOsSshPswd()));
            	}catch(Exception e2) {
            		server.setOsSshPswd("");
            	}
            }
            response.put("server", server);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping(value="/server/save",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String save(@RequestBody Server server,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", server.getProduct());
			condition.put("serverId", server.getServerId());
			List<Server> serverList = serverService.getServerList(condition);
			if(null != serverList && serverList.size()>0) {
				response.setInfo("新增失败（新增的服务器已存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			server.setStatus(Server.ServerInstallLog_Status_NotInstall);
			server.setOsSshPswd(encryptService.encryptInfo(server.getOsSshPswd()));
			serverService.insertServer(server);
			SysLog sysLog = sysLogService.getDefaultSysLog(request,SysLog.Mod_Name_Server,SysLog.Opt_Name_Insert,"成功",JSONObject.toJSONString(server));
			sysLogService.insertSysLog(sysLog);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("save server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return JSONObject.toJSONString(response);
	}
	
	@PostMapping(value="/server/update",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String update(@RequestBody Server server,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", server.getProduct());
			condition.put("serverId", server.getServerId());
			List<Server> serverList = serverService.getServerList(condition);
			if(null == serverList || serverList.size() < 1) {
				response.setInfo("修改失败，修改的服务器不存在");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			Server tServer = serverList.get(0);
//			if(tServer.getStatus() > 1) {
//				response.setInfo("修改服务器失败（只能修改未安装或安装失败的服务器）");
//				response.setSuccess(false);
//				return JSONObject.toJSONString(response);
//			}
			//验证配置版本号
			if(server.getCfgVersion().longValue() != tServer.getCfgVersion().longValue()) {
				response.setInfo("修改失败，别人在此之前有修改，请刷新服务器页面，再修改");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			server.setStatus(tServer.getStatus());
			server.setOsSshPswd(encryptService.encryptInfo(server.getOsSshPswd()));
			server.setCfgVersion(System.currentTimeMillis());
			server.setVersionNo(tServer.getVersionNo());
			server.setdLatestOnlineTime(tServer.getdLatestOnlineTime());
			serverService.updateServer(server);
			SysLog sysLog = sysLogService.getDefaultSysLog(request,SysLog.Mod_Name_Server,SysLog.Opt_Name_Update,"成功",JSONObject.toJSONString(server));
			sysLogService.insertSysLog(sysLog);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("update server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return JSONObject.toJSONString(response);
	}
	
	@DeleteMapping(value="/server/delete",produces=MediaType.APPLICATION_JSON_VALUE)
	public String delete(@RequestBody List<Server> servers,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		if (servers != null && servers.size() > 0) {
			try {
				SysLog sysLog = sysLogService.getDefaultSysLog(request,SysLog.Mod_Name_Server,SysLog.Opt_Name_Delete,"成功",null);
				serverService.deleteServers(servers,sysLog);
				response.setSuccess(true);
			} catch (Exception e) {
				logger.error("delete server failed ",e);
				response.setSuccess(false);
				response.setInfo("");
				response.setDetailInfo(StringUtil.stringifyException(e)); 
			}
		} else {
			response.setSuccess(false);
			response.setInfo("未选中服务器");
			return JSONObject.toJSONString(response);
		}
		return JSONObject.toJSONString(response);
	}

	@GetMapping(value="/server/getDefaultValue",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getDefaultValue() {
		RestResponse response = new RestResponse();
		try {
			Server server = new Server();
			server.setZooNamespace(zooNamespace);
			server.setZooConnectStr(zooConnectUrl);
			server.setOsSshPort(22);
			server.setRmiServerPort(44444);
			server.setRmiRegistryPort(1099);
			server.setSshPort(8101);
			response.put("server", server);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return response;
	}

	@GetMapping("/server/getInstallFileName")
	public RestResponse getInstallFileName() {
		RestResponse response = new RestResponse();
		try {
			TreeMap<String,String> releaseInfos = new TreeMap<String,String>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return - o1.compareTo(o2);
				}
			});
			File releaseDirFile = new File(fileReleasePath);
			if(releaseDirFile.isDirectory()) {
				File[] rfiles= releaseDirFile.listFiles();
				if(null != rfiles && rfiles.length > 0) {
					for(File rfile : rfiles) {
						String rfilename = rfile.getName();
						if(rfilename.startsWith("wwsf-release") && rfilename.endsWith(".html")) {
							String[] rla = rfilename.split("-");
							String batchFileName = rla[0] + "-" + rla[2] + "-" + rla[3].substring(0, rla[3].lastIndexOf(".")) + ".zip";
							File batchFile = new File(releaseDirFile, batchFileName);
							if (batchFile.exists()) {
								releaseInfos.put(rfilename, batchFileName);
							}
						}
					}
				}
			}
			if(releaseInfos.size() > 0) {
				response.put("newInstallFileName", releaseInfos.firstEntry().getValue());
			}
			response.put("ifnList", releaseInfos.values());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get install file name failed ",e);
			response.setSuccess(false);
			response.setInfo("获取安装文件列表失败");
			response.setDetailInfo(StringUtil.stringifyException(e)); 
		}
		return response;
	}
	
	@GetMapping(value="/server/install",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse install(String product, String serverId, String installFileName, Long installCfgVersion,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		StringBuffer installLog = new StringBuffer("");
		Server tServer = null; //更新用
		Server server = null; //记录系统日志用
		int status = Server.ServerInstallLog_Status_NotInstall;
		try {
			Map<String,Object> resultMap = null;
            try {
            	resultMap = serverService.canInstall(product, serverId, installCfgVersion);
            	String result = (String)resultMap.get("result");
            	if(!result.equals("")) {
            		response.setSuccess(false);
    				response.setInfo(result);
    				return response;
            	}
            }catch(Exception e2) {
            	logger.error("install server failed ",e2);
            	response.setSuccess(false);
				response.setInfo("安装失败，判断是否可以安装服务器出错");
				response.setDetailInfo(StringUtil.stringifyException(e2)); 
				return response;
            }
			Map<String,Object> paraMap = new HashMap<String,Object>();
			server = (Server)resultMap.get("server");
			server.setOsSshPswd(encryptService.decryptInfo(server.getOsSshPswd()));
			paraMap.put("Server", server);

			// 把SSH认证方式,默认密钥认证、密钥路径、密钥密码放入paraMap
			paraMap.put("sshAuth",sshAuth);
			paraMap.put("privateKeyPath",privateKeyPath);
			paraMap.put("passphrase",passphrase);

			Product tProduct = productService.getProduct(product);
			if(null == tProduct) {
				response.setSuccess(false);
				response.setInfo("安装失败，不存在产品: " + product);
				return response;
			}
			paraMap.put("product", tProduct);
			paraMap.put("dbConn", dbConn);
			String classPath = fileReleasePath;
			List<String> uploadFilePathList = new ArrayList<String>();
			uploadFilePathList.add(classPath + "/" + installFileName);
			tServer = new Server();
			tServer.setProduct(product);
			tServer.setServerId(serverId);
			paraMap.put("zooAclString", zooAclString);
			SshClientUtils.install(paraMap,uploadFilePathList,installLog);
			status = Server.ServerInstallLog_Status_InstallSuccess;
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("",e);
			status = Server.ServerInstallLog_Status_InstallFail;
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		} finally {
			try {
				if(null != tServer) {
					tServer.setInstallLog(installLog.toString());
					tServer.setStatus(status);
					tServer.setVersionNo(installFileName.replace("wwsf-v", "").replace(".zip", ""));
					tServer.setInstallCfgVersion(server.getCfgVersion());
					SysLog sysLog = sysLogService.getDefaultSysLog(request,SysLog.Mod_Name_Server,SysLog.Opt_Name_Install,null,installLog.toString());
					serverService.installServer(tServer,sysLog);
				}
			}catch(Exception e3) {
				logger.error("",e3);
				response.setSuccess(false);
				response.setInfo("保存服务器安装日志出错");
			}
		}
		return response;
	}
	
	@GetMapping(value="/server/uninstall",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse uninstall(String product, String serverId,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		StringBuffer uninstallLog = new StringBuffer("");
		Server tServer = null; //更新用
		Server server = null; //记录系统日志用
		int status = Server.ServerInstallLog_Status_InstallSuccess;
		try {
            Map<String,Object> queryMap = new HashMap<String,Object>();
            queryMap.put("product", product);
            queryMap.put("serverId", serverId);
            queryMap.put("status", status);
            List<Server> serverList = this.serverService.getServerList(queryMap);
            if(null == serverList || serverList.size() < 1) {
            	response.setSuccess(false);
				response.setInfo("卸载失败，服务器("+serverId+")已被别人卸载");
				return response;
            }
			Map<String,Object> paraMap = new HashMap<String,Object>();
			server = (Server)serverList.get(0);
			server.setOsSshPswd(encryptService.decryptInfo(server.getOsSshPswd()));
			paraMap.put("Server", server);

			// 把SSH认证方式,默认密钥认证、密钥路径、密钥密码放入paraMap
			paraMap.put("sshAuth",sshAuth);
			paraMap.put("privateKeyPath",privateKeyPath);
			paraMap.put("passphrase",passphrase);

			tServer = new Server();
			tServer.setProduct(product);
			tServer.setServerId(serverId);
			SshClientUtils.uninstall(paraMap,uninstallLog);
			status = Server.ServerInstallLog_Status_NotInstall;
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		} finally {
			try {
				if(null != tServer) {
					tServer.setInstallLog(uninstallLog.toString());
					tServer.setStatus(status);
					tServer.setInstallCfgVersion(server.getInstallCfgVersion());
					SysLog sysLog = sysLogService.getDefaultSysLog(request,SysLog.Mod_Name_Server,SysLog.Opt_Name_Uninstall,null,uninstallLog.toString());
					serverService.uninstallServer(tServer,sysLog);
				}
			}catch(Exception e3) {
				logger.error("",e3);
				response.setSuccess(false);
				response.setInfo("保存服务器卸载日志出错");
			}
		}
		return response;
	}
	
	@GetMapping(value="/server/start",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse start(String product,String serverId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> qMap = new HashMap<String,Object>();
			qMap.put("product", product);
            qMap.put("serverId", serverId);
            List<Server> serverList = serverService.getServerList(qMap);
            if(null == serverList || serverList.size()<1) {
            	response.setSuccess(false);
				response.setInfo("联机失败，不存在服务器: " + product+"/"+serverId);
				return response;
            }
			Map<String,Object> paraMap = new HashMap<String,Object>();
			Server server = serverList.get(0);
			server.setOsSshPswd(encryptService.decryptInfo(server.getOsSshPswd()));
			paraMap.put("Server", server);

			// 把SSH认证方式,默认密钥认证、密钥路径、密钥密码放入paraMap
			paraMap.put("sshAuth",sshAuth);
			paraMap.put("privateKeyPath",privateKeyPath);
			paraMap.put("passphrase",passphrase);

			//Map<String,String> karafMap = PropertiesUtils.getKarafProperties(); //加载文件(karaf.properties)内容
			//paraMap.put("karafMap", karafMap);
			SshClientUtils.start(paraMap);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("start server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}

	@GetMapping(value="/server/stop",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse stop(String product,String serverId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> qMap = new HashMap<String,Object>();
			qMap.put("product", product);
            qMap.put("serverId", serverId);
            List<Server> serverList = serverService.getServerList(qMap);
            if(null == serverList || serverList.size()<1) {
            	response.setSuccess(false);
				response.setInfo("脱机失败，不存在服务器: " + product+"/"+serverId);
				return response;
            }
			Map<String,Object> paraMap = new HashMap<String,Object>();
			Server server = serverList.get(0);
			server.setOsSshPswd(encryptService.decryptInfo(server.getOsSshPswd()));
			paraMap.put("Server", server);

			// 把SSH认证方式,默认密钥认证、密钥路径、密钥密码放入paraMap
			paraMap.put("sshAuth",sshAuth);
			paraMap.put("privateKeyPath",privateKeyPath);
			paraMap.put("passphrase",passphrase);

			//Map<String,String> karafMap = PropertiesUtils.getKarafProperties(); //加载文件(karaf.properties)内容
			//paraMap.put("karafMap", karafMap);
			SshClientUtils.stop(paraMap);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("stop server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/server/getNodeInfos")
	public RestResponse getNodeInfos() {
		RestResponse response = new RestResponse();
		Map<String,Object> conditions = new HashMap<String,Object>();
		Server onlineMaster = null;
		Server aMaster = null;
		List<Map<String,Object>> visNodes = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> visEdges = new ArrayList<Map<String,Object>>();
		Map<String,Object> borderDashes = new HashMap<String,Object>();
		borderDashes.put("borderDashes", true);
		Map<String,Object> borderColors = new HashMap<String,Object>();
		borderColors.put("border", "red");
		borderColors.put("background", "#868e96");
		try {
			conditions.put("loginName", UserUtils.getCurLoginSysUser().getLoginName());
			List<Server> servers = serverService.getServerList(conditions);
			Map<String,Boolean> onlines = zooService.getOnlineNodes();
			Set<String> products = new HashSet<String>();
			for (Server server : servers) {
				String visNodeId = server.getProduct() + "|" + server.getIpAddr() + "|" + server.getServerId();
				String onlineKey = server.getProduct() + "::" + server.getServerId();
				Map<String,Object> visNode = new HashMap<String,Object>();
				visNode.put("id", visNodeId);
				visNode.put("product", server.getProduct());
				visNode.put("label", server.getServerId());
				if ("master".equals(server.getServerType())) {
					visNode.put("group", "master");
					visNode.put("level", 1);
					if (onlines.containsKey(onlineKey) && onlines.get(onlineKey)) {
						onlineMaster = server;
					}
					aMaster = server;
				} else {
					visNode.put("group", "slave");
					visNode.put("level", 3);
				}
				//未在线的节点边框虚线标识
				if (!onlines.containsKey(onlineKey)) {
					if (server.getStatus() == 0) {
						
					} else {
						visNode.put("shapeProperties", borderDashes);
					}
					visNode.put("color", borderColors);
					visNode.put("borderWidth", 2);
				}
				
				visNodes.add(visNode);
				if (!products.contains(server.getProduct())) {
					Map<String,Object> visNodeProduct = new HashMap<String,Object>();
					visNodeProduct.put("id", server.getProduct());
					visNodeProduct.put("label", server.getProduct());
					visNodeProduct.put("group", "product");
					if ("master".equals(server.getServerType())) {
						visNodeProduct.put("level", 0);
					} else {
						visNodeProduct.put("level", 2);
					}
					visNodes.add(visNodeProduct);
					products.add(server.getProduct());
				}
				
				Map<String,Object> visEdge = new HashMap<String,Object>();
				visEdge.put("from", server.getProduct());
				visEdge.put("to", visNodeId);
				visEdge.put("length", 20);
				visEdges.add(visEdge);
			}
			
			if (onlineMaster != null || aMaster != null) {
				Server master = onlineMaster != null ? onlineMaster : aMaster;
				for (String prod : products) {
					String visNodeId = (master.getProduct() + "|" + master.getIpAddr() + "|" + master.getServerId());
					if (!master.getProduct().equals(prod)) {
						Map<String,Object> visEdge = new HashMap<String,Object>();
						visEdge.put("from", visNodeId);
						visEdge.put("to", prod);
						visEdge.put("length", 20);
						if (onlineMaster == null) visEdge.put("dashes", true);
						visEdges.add(visEdge);
					}
				}
			}
			
			response.put("visNodes", visNodes);
			response.put("visEdges", visEdges);
			List<NodeInfo> result = zooService.getNodeInfoList();
			List<NodeInfo> slaves = new ArrayList<>(result.size());
			for (NodeInfo nodeInfo : result) {
				if (nodeInfo.getOnline()==1) {
					response.put("master", nodeInfo);
					continue;
				}
				slaves.add(nodeInfo);
			}
			response.put("slaves", slaves);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/zoo/getNodeInfos", e);
			response.setSuccess(false);
			response.setInfo("获取节点信息出错");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/server/getNodeInfo")
	public RestResponse getNodeInfo(String product, String nodeId) {
		RestResponse response = new RestResponse();
		try {
			NodeInfo nodeInfo = zooService.getOnlineNode(product, nodeId);
			if (nodeInfo == null) {
				nodeInfo = serverService.getServer(product,nodeId);
				nodeInfo.setAvailable(false);
			}
			response.put("server", nodeInfo);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/server/getNodeInfo", e);
			response.setSuccess(false);
			response.setInfo("未找到节点信息");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/server/getServerInstallLogInfo",produces=MediaType.APPLICATION_JSON_VALUE)
	public String getServerInstallLogInfo(String product, String serverId) {
		RestResponse response = new RestResponse();
		try {
			Map<String, Object> conditions = new HashMap<>();
			conditions.put("product", product);
			conditions.put("serverId", serverId);
            List<Server> serverList = serverService.getServerInstallLog(conditions);
            if(null != serverList && serverList.size() > 0) {
            	Server server = serverList.get(0);
            	if(null != server.getInstallLog() && !server.getInstallLog().equals("")) {
            		response.put("installLog", server.getInstallLog());
            	}
            }
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get server install log info failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/server/getPluginQueueInfo")
	public RestResponse getPluginQueueInfo(String product, String nodeId) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Map<String, Object>> list = new TreeMap<String,Map<String, Object>>();
			String result = wwseService.pluginExecutorInfo(product, nodeId);
			JSONObject jsonObject = JSONObject.parseObject(result);
			if (jsonObject.containsKey("data")) {
				JSONObject data = jsonObject.getJSONObject("data");
				JSONObject plugin = null;
				Map<String, Object> map = null;
				for (String key : data.keySet()) {
					map = new HashMap<>();
					map.put("name", key);
					plugin = data.getJSONObject(key);
					map.put("queueSize", plugin.getInteger("QueueSize"));
					map.put("active", plugin.getInteger("Active"));
					map.put("coreSize", plugin.getInteger("CoreSize"));
					map.put("maxSize", plugin.getInteger("MaxSize"));
					map.put("shutdown", false);
					list.put(key, map);
				}
			}
			response.put("rows", list.values());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/zoo/getPluginQueueInfo", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));  
		}
		return response;
	}
	
	@GetMapping("/server/updatePluginPoolSize")
	public RestResponse updatePluginPoolSize(String product, String nodeId, String names, String coreSizes) {
		RestResponse response = new RestResponse();
		response.setSuccess(false);
		try {
			if (names == null || "".equals(names)) {
				response.setInfo("插件名不能为空");
				return response;
			}
			if (coreSizes == null || "".equals(coreSizes)) {
				response.setInfo("插件线程数不能为空");
				return response;
			}
			String[] nameArr = names.split(",");
			String[] coreSizeArr = coreSizes.split(",");
			if (nameArr.length != coreSizeArr.length) {
				response.setInfo("插件，插件线程，数量不匹配");
				return response;
			}
			for(int i=0, len=nameArr.length; i<len; i++) {
				String out = wwseService.changePluginExecutorSize(product, nodeId, nameArr[i], Integer.valueOf(coreSizeArr[i]));
				logger.info(out);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/server/updatePluginPoolSize", e);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));  
		}
		return response;
	}
	
	@GetMapping("/server/getScheduleResourceInfo")
	public RestResponse getScheduleResourceInfo() {
		RestResponse response = new RestResponse();
		try {
			List<Map<String, Object>> list = new ArrayList<>(10);
			String result = wwseService.scheduleResourceInfo();
			JSONObject jsonObject = JSONObject.parseObject(result);
			if (jsonObject.containsKey("data")) {
				JSONObject data = jsonObject.getJSONObject("data");
				JSONObject plugin = null;
				Map<String, Object> map = null;
				for (String key : data.keySet()) {
					map = new HashMap<>();
					map.put("name", key);
					plugin = data.getJSONObject(key);
					map.put("rcv_size", plugin.getInteger("rcv_size"));
					map.put("rcv_active", plugin.getInteger("rcv_active"));
					map.put("rcv_parallel", plugin.getInteger("rcv_parallel"));
					map.put("drv_size", plugin.getInteger("drv_size"));
					map.put("drv_parallel", plugin.getInteger("drv_parallel"));
					map.put("drv_active", plugin.getInteger("drv_active"));
					map.put("res_size", plugin.getInteger("res_size"));
					map.put("res_parallel", plugin.getInteger("res_parallel"));
					map.put("res_active", plugin.getInteger("res_active"));
					list.add(map);
				}
			}
			response.put("rows", list);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/server/getScheduleResourceInfo", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));  
		}
		return response;
	}
	
	@GetMapping("/server/getScheduleInitQueueResourceInfo")
	public RestResponse getScheduleInitQueueResourceInfo() {
		RestResponse response = new RestResponse();
		try {
			List<Map<String, Object>> list = new ArrayList<>(10);
			String result = wwseService.scheduleInitQueueResourceInfo();
			JSONObject jsonObject = JSONObject.parseObject(result);
			if (jsonObject.containsKey("data")) {
				JSONObject data = jsonObject.getJSONObject("data");
				JSONObject plugin = null;
				Map<String, Object> map = null;
				for (String key : data.keySet()) {
					map = new HashMap<>();
					map.put("name", key);
					plugin = data.getJSONObject(key);
					map.put("init_size", plugin.getInteger("init_size"));
					map.put("ended_size", plugin.getInteger("ended_size"));
					map.put("init_parallel", plugin.getInteger("init_parallel"));
					map.put("ended_parallel", plugin.getInteger("ended_parallel"));
					map.put("init_active", plugin.getInteger("init_active"));
					map.put("ended_active", plugin.getInteger("ended_active"));
					list.add(map);
				}
			}
			response.put("rows", list);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/server/getScheduleInitQueueResourceInfo", e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping("/server/selectServer")
	public RestResponse selectServer(String product) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> qMap = new HashMap<String,Object>();
			if(null != product && !product.equals("")) {
				qMap.put("product", product);
			}
			qMap.put("orderByServerId", "orderByServerId");
            List<Server> servers = serverService.getServerList(qMap);
            if(null != servers && servers.size() > 0) {
            	response.put("servers", servers);
            }
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/server/selectServer", e);
			response.setSuccess(false);
			response.setInfo("获取节点数据失败");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@PostMapping(value="/server/saveOsSshPswd",produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String saveOsSshPswd(@RequestBody Server server) {
		RestResponse response = new RestResponse();
		try {
			Map<String,Object> condition = new HashMap<String,Object>();
			condition.put("product", server.getProduct());
			condition.put("serverId", server.getServerId());
			List<Server> serverList = serverService.getServerList(condition);
			if(null == serverList || serverList.size()<1) {
				response.setInfo("保存失败（服务器不存在）");
				response.setSuccess(false);
				return JSONObject.toJSONString(response);
			}
			server.setOsSshPswd(encryptService.encryptInfo(server.getOsSshPswd()));
			serverService.updateServerOsSshPswd(server);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("save server failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return JSONObject.toJSONString(response);
	}
	
	@GetMapping(value="/server/destroyScheduleResource",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse destroyScheduleResource(String resourceId) {
		RestResponse response = new RestResponse();
		try {
			wwseService.destroyScheduleResource(resourceId);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("destroyScheduleResource ["+resourceId+"] fail. ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	@GetMapping(value="/server/getWebsocketAddr",produces=MediaType.APPLICATION_JSON_VALUE)
	public RestResponse getWebsocketAddr(String product,String serverId,String connType,HttpServletRequest request) {
		RestResponse response = new RestResponse();
		try {
            Map<String,Object> qMap = new HashMap<String,Object>();
			qMap.put("product", product);
            qMap.put("serverId", serverId);
            List<Server> serverList = serverService.getServerList(qMap);
            if(null == serverList || serverList.size() < 1) {
            	response.setInfo("当前服务不存在");
                response.setSuccess(false);
                return response;
            }
            Server server = serverList.get(0);
            SshConnInfo sshConnInfo = null;
            if(connType.equals("karaf")) {
            	sshConnInfo = new SshConnInfo(server.getIpAddr(),server.getSshPort(),wwsJmxUser,DesEncrypt.decrypt(wwsJmxPassword),"UTF-8");
            }else {
            	sshConnInfo = new SshConnInfo(server.getIpAddr(),
						                       server.getOsSshPort(),
						                       server.getOsSshUser(),
												encryptService.decryptInfo(server.getOsSshPswd()),
						                       null,
						                        sshAuth,privateKeyPath,passphrase);
            }
            response.put("sshConnInfo", sshConnInfo);
            //拼接websockeAddr地址
            String tIp = request.getServerName();
			if(tIp.equals("0:0:0:0:0:0:0:1")) {
				tIp = "localhost";
			}
			String websocketAddr = tIp+":"+request.getServerPort()+"/"+SshConnInfo.Springboot_Websocket_Name+"/";
            response.put("websocketAddr", websocketAddr);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get websocket addr failed ",e);
			response.setSuccess(false);
			response.setInfo("");
			response.setDetailInfo(StringUtil.stringifyException(e));
		}
		return response;
	}
	
	//任务模板/任务项/可执行节点范围
	@GetMapping(value="/server/selectAgentScope", produces = MediaType.APPLICATION_JSON_VALUE)
	public String selectAgentScope(String product,String inputAgentScope) {
		RestResponse response = new RestResponse();
		try {
			List<SelectTwo> selectTwoList = serverService.getServerIdAndTags(product,inputAgentScope);
			if(null != selectTwoList && selectTwoList.size() > 0) {
				response.put("selectTwos", selectTwoList);
			}
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("--- 获取可执行节点范围下拉数据异常 ---",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSONObject.toJSONString(response);
	}
	
}
