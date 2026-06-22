package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.service.WwseService;

@Service
public class WwseServiceImpl implements WwseService {

	@Autowired
	CuratorFramework curatorFramework;
	
	@Value("${wws.jmx.user}")
	private String wwsJmxUser;
	@Value("${wws.jmx.password}")
	private String wwsJmxPassword;
	
    public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}
	
	/*********Job Operations(wwsf.shell)*****************************/
    public String pluginExecutorInfo(String product, String nodeId, String plugin) throws Exception{
    	return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"pluginExecutorInfo", 
				new Object[] { product, nodeId, plugin },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
	}
    public String pluginExecutorInfo(String product, String nodeId) throws Exception{
    	return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"pluginExecutorInfo", 
				new Object[] { product, nodeId },
				new String[] { "java.lang.String", "java.lang.String" });
	}
	
	public String changePluginExecutorSize(String product, String nodeId, String plugin, Integer core) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"changePluginExecutorSize", 
				new Object[] { product, nodeId, plugin, core },
				new String[] { "java.lang.String","java.lang.String","java.lang.String","java.lang.Integer"});
	}
	
	@SuppressWarnings("unchecked")
	public List<String> pluginExecutorQueueList(String product, String nodeId, String plugin) throws Exception{
		return (List<String>)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"pluginExecutorQueueList", 
				new Object[] { product, nodeId, plugin },
				new String[] { "java.lang.String","java.lang.String","java.lang.String" });
	}
	
	/*********Manager Operations****************************/

	public void jobGraph(String product, String jobId) throws Exception {
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobGraph", 
				new Object[] { product,jobId },
				new String[] { "java.lang.String","java.lang.String" });
	}
	
	public Long jobInit(String product, String jobId, String params) throws Exception{
		return (Long)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobInit", 
				new Object[] { product,jobId,params },
				new String[] { "java.lang.String","java.lang.String","java.lang.String" });
	}

	public Long jobInitRegional(String infoId) throws Exception{
		return (Long)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobInitRegional", 
				new Object[] { infoId },
				new String[] { "java.lang.String"});
	}
	
	public void jobRerunUnpassed(Long jobInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobRerunUnpassed", 
				new Object[] { jobInsId },
				new String[] { "java.lang.Long" });
	}

	public void jobRerunFailed(Long jobInsId, String[] taskInsIds) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobRerunFailed", 
				new Object[] { jobInsId,taskInsIds },
				new String[] { "java.lang.Long", "[Ljava.lang.String;" });
	}
	
	public void jobKill(Long jobInsId, String taskInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobKill", 
				new Object[] { jobInsId, taskInsId },
				new String[] { "java.lang.Long", "java.lang.String" });
	}

	public String showTaskRuntimeLog(Long jobInsId, String taskInsId) throws Exception {
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"showTaskRuntimeLog", 
				new Object[] { jobInsId,taskInsId },
				new String[] { "java.lang.Long","java.lang.String" });
	}

	public void jobRerun(
			Long jobInsId, 
			String[] taskInsIds, Boolean recursion, Boolean exceptRunning) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobRerun", 
				new Object[] { jobInsId,taskInsIds,recursion,exceptRunning },
				new String[] { "java.lang.Long","[Ljava.lang.String;","java.lang.Boolean","java.lang.Boolean" });
		
	}
	
	public void jobRenovate(Long jobInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobRenovate", 
				new Object[] { jobInsId },
				new String[] { "java.lang.Long" });
	}
	
	public void jobContinue(Long jobInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobContinue", 
				new Object[] { jobInsId },
				new String[] { "java.lang.Long" });
	}
	
	public void jobPause(Long jobInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobPause", 
				new Object[] { jobInsId },
				new String[] { "java.lang.Long" });
	}
	
	public void jobDisabled(Long jobInsId,String[] taskInsIds) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobDisabled", 
				new Object[] { jobInsId,taskInsIds },
				new String[] { "java.lang.Long","[Ljava.lang.String;" });
	}
	
	public void jobDriver(Long[] jobInsId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"jobDriver", 
				new Object[] { jobInsId},
				new String[] { "[Ljava.lang.Long;"});
	}
	
	/**CronJob interface***************************************/
	public void deleteCronJob(String cronId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"deleteCronJob", 
				new Object[] { cronId },
				new String[] { "java.lang.String" });
	}
	
	public void cronStart(String cronId, Boolean clearShared) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"cronStart", 
				new Object[] { cronId,clearShared },
				new String[] { "java.lang.String","java.lang.Boolean" });
	}
	
	public void cronStop(String cronId) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"cronStop", 
				new Object[] { cronId },
				new String[] { "java.lang.String" });
	}
	
	public Boolean hasCron(String cronId, String product, String agent) throws Exception{
		return (Boolean)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"hasCron", 
				new Object[] { cronId,product,agent },
				new String[] { "java.lang.String","java.lang.String","java.lang.String" });
	}
	
	public String cronOnLine(String product, String agent) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"cronOnLine", 
				new Object[] { product,agent },
				new String[] { "java.lang.String","java.lang.String" });
	}
	
	/**DataSource interface***************************************/
	
	public void deleteDataSource(String name) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"deleteDataSource", 
				new Object[] { name },
				new String[] { "java.lang.String" });
	}
	
	public void destroyDataSource(String name) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"destroyDataSource", 
				new Object[] { name },
				new String[] { "java.lang.String" });
	}
	
	public void createDataSource(String name) throws Exception{
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"createDataSource", 
				new Object[] { name },
				new String[] { "java.lang.String" });
	}
	
	public Boolean hasDataSource(String product, String agent, String name) throws Exception{
		return (Boolean)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"hasDataSource", 
				new Object[] { product,agent,name },
				new String[] { "java.lang.String","java.lang.String","java.lang.String" });
	}
	
	public String getDataSourceInfo(String product, String agent, String name) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"getDataSourceInfo", 
				new Object[] { product,agent,name },
				new String[] { "java.lang.String","java.lang.String","java.lang.String" });
	}
	
	public String getDataSourcesOnLine(String product,String agent) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"getDataSourcesOnLine", 
				new Object[] { product,agent },
				new String[] { "java.lang.String","java.lang.String" });
	}
	
	public String encryption(String text, String confirm) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"encryption", 
				new Object[] { text,confirm },
				new String[] { "java.lang.String","java.lang.String" });
	}
	
	public String decryption(String ciphertext) throws Exception{
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"decryption", 
				new Object[] { ciphertext },
				new String[] { "java.lang.String" });
	}
	
	/** Schedule Resources ****************************************/
	
	public void changeScheduleResourceParallel(String rid, Integer parallel) throws Exception {
		jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"changeScheduleResourceParallel", 
				new Object[] { rid,parallel },
				new String[] { "java.lang.String","java.lang.Integer" });
	}
	
	public String destroyScheduleResource(String rid) throws Exception {
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, 
				"destroyScheduleResource", 
				new Object[] { rid },
				new String[] { "java.lang.String" });
	}
	
	public String scheduleResourceInfo() throws Exception {
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, "scheduleResourceInfo", new Object[]{}, new String[]{});
	}

	public String scheduleInitQueueResourceInfo() throws Exception {
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, "scheduleInitQueueResourceInfo", new Object[]{}, new String[]{});
	}

	public String flushPluginInfo() throws Exception {
		return (String)jmxCall(OBJECT_NAME_MANAGER, wwsJmxUser, wwsJmxPassword, "flushPluginInfo", new Object[]{}, new String[]{});
	}
	
}
