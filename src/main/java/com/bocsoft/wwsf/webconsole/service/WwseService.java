package com.bocsoft.wwsf.webconsole.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

import com.bocsoft.wwsf.webconsole.DesEncrypt;
import com.bocsoft.wwsf.webconsole.StringUtil;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.zoo.ZooUtil;

public interface WwseService {
	
	final String PL_NODE = "/pl_nodes/{0}";
	final String PL_MASTER_ROOT = "/pl_master";
	final String JMX_URL_PATTERN = "service:jmx:rmi://{0}:{1}/jndi/rmi://{2}:{3}/karaf-root";
	final String OBJECT_NAME_CRON = "org.apache.karaf:type=wws,name=cron";
	final String OBJECT_NAME_DS = "org.apache.karaf:type=wws,name=ds";
	final String OBJECT_NAME_MANAGER = "org.apache.karaf:type=wws,name=manager";
	
	CuratorFramework getCuratorFramework();
	
	public default NodeInfo getMaster() throws Exception {
		List<String> masters = getCuratorFramework().getChildren().forPath(PL_MASTER_ROOT);
		if(masters != null && masters.size() > 0) {
			String plPath = MessageFormat.format(PL_NODE, masters.get(0));
			Stat stat = getCuratorFramework().checkExists().forPath(plPath);
			if(stat != null) {
				return ZooUtil.parseObject(getCuratorFramework().getData().forPath(plPath), NodeInfo.class);
			}
		}
		return null;
	}
	
	public default String getJmxUrl() throws Exception {
		List<String> masters = getCuratorFramework().getChildren().forPath(PL_MASTER_ROOT);
		if(masters != null && masters.size() > 0) {
			String plPath = MessageFormat.format(PL_NODE, masters.get(0));
			Stat stat = getCuratorFramework().checkExists().forPath(plPath);
			if(stat != null) {
				NodeInfo nodeInfo = ZooUtil.parseObject(getCuratorFramework().getData().forPath(plPath), NodeInfo.class);
				if(null != nodeInfo) {
					if(!StringUtil.hasText(nodeInfo.getIp())){
						nodeInfo.setIp("127.0.0.1");
					}
					return MessageFormat.format(
							JMX_URL_PATTERN, 
							nodeInfo.getIp(), 
							String.valueOf(nodeInfo.getRmiServerPort()), 
							nodeInfo.getIp(), 
							String.valueOf(nodeInfo.getRmiRegistryPort()));
				}
			}
		}
		return null;
	}
	
	public default JMXConnector getJmxClient(String user, String password) throws IOException {
		String url = null;
		try {
			url = getJmxUrl();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("get master jmx url failed", e);
		}
		if(StringUtil.isNullOrEmpty(url)) throw new IOException("master nofound");
		Map<String, Object> env = new HashMap<String, Object>();
		env.put(JMXConnector.CREDENTIALS, new String[]{ user, password });
		return JMXConnectorFactory.connect(new JMXServiceURL(url), env);
	}
	
	public default Object jmxCall(
			String OBJECT_NAME,String user, String password, 
			String method, Object[] params, String[] signature) throws IOException, MalformedObjectNameException,
			InstanceNotFoundException, MBeanException, ReflectionException {
		JMXConnector client = null;
		try {
			client = getJmxClient(user, DesEncrypt.decrypt(password));
			MBeanServerConnection mbsc = client.getMBeanServerConnection();
			ObjectName objectName = new ObjectName(OBJECT_NAME);;
			return mbsc.invoke(objectName, method, params, signature);
		} finally {
			try {
				if (client != null) client.close();
			} catch (Exception e) { }
		}
	}
	
	/*********Job Operations(wwsf.controller)*****************************/
	
	String pluginExecutorInfo(String product, String nodeId, String plugin) throws Exception;
	
	String pluginExecutorInfo(String product, String nodeId) throws Exception;
	
	String changePluginExecutorSize(String product, String nodeId, String plugin, Integer core) throws Exception;
	
	List<String> pluginExecutorQueueList(String product, String nodeId, String plugin) throws Exception;
	
	/*********Manager Operations(wwsf.shell)****************************/
	void jobGraph(String product, String jobId) throws Exception;
	
	Long jobInitRegional(String infoId) throws Exception;
	
    Long jobInit(String product, String jobId, String params) throws Exception;
	
	void jobRerunUnpassed(Long jobInsId) throws Exception;

	void jobRerunFailed(Long jobInsId, String[] taskInsIds) throws Exception;
	/**
	 * 杀死只在执行的Task实例，暂支持shell类型的作业
	 * @param jobInsId
	 * @param taskInsIds
	 * @throws Exception
	 */
	void jobKill(Long jobInsId, String taskInsId) throws Exception;

	void jobRerun(
			Long jobInsId, 
			String[] taskInsIds, Boolean recursion, Boolean exceptRunning) throws Exception;
	
	void jobRenovate(Long jobInsId) throws Exception;
	
	void jobContinue(Long jobInsId) throws Exception;
	
	void jobPause(Long jobInsId) throws Exception;
	
	void jobDisabled(Long jobInsId,String[] taskInsIds) throws Exception;
	
	void jobDriver(Long[] jobInsId) throws Exception;
	/**
	 *  查看处于Running状态Task实例的日志
	 * @param jobInsId
	 * @param taskInsId
	 * @return
	 * @throws Exception
	 */
	String showTaskRuntimeLog(Long jobInsId,String taskInsId) throws Exception;
	
	/**CronJob interface***************************************/
	void deleteCronJob(String cronId) throws Exception;
	
	void cronStart(String cronId, Boolean clearShared) throws Exception;
	
	void cronStop(String cronId) throws Exception;
	
	Boolean hasCron(String cronId, String product, String agent) throws Exception;
	
	String cronOnLine(String product, String agent) throws Exception;
	
	/**DataSource interface***************************************/
	
	void deleteDataSource(String name) throws Exception;
	
	void destroyDataSource(String name) throws Exception;
	
	void createDataSource(String name) throws Exception;
	
	Boolean hasDataSource(String product, String agent, String name) throws Exception;
	 
	String getDataSourceInfo(String product, String agent, String name) throws Exception;
	
	String getDataSourcesOnLine(String product,String agent) throws Exception;
	
	String encryption(String text, String confirm) throws Exception;
	
	String decryption(String ciphertext) throws Exception;
	
	/** Schedule Resources ****************************************/
	
	void changeScheduleResourceParallel(String rid, Integer parallel) throws Exception;
	
	String destroyScheduleResource(String rid) throws Exception;
	
	String scheduleResourceInfo() throws Exception;
	
	String scheduleInitQueueResourceInfo() throws Exception;
	
	String flushPluginInfo() throws Exception;
	
}
