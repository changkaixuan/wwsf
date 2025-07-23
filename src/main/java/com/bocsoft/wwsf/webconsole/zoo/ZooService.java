package com.bocsoft.wwsf.webconsole.zoo;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.NodeInfo;

public interface ZooService {
	
	public List<ZooNode> getChildrenNodes(String parent) throws Exception;
	
	public ZooNode getData(String nodePath) throws Exception;
	
	public void save(ZooNode node) throws Exception;
	
	public String add(ZooNode node);
	
	public String delete(String nodePath);
	
	public void del(String nodePath, boolean delChildren) throws Exception;
	
	public default String tranformTime(Long seconds) {
		if(seconds != null){
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(seconds);
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
			return format.format(gc.getTime());
		}
		return null;
	}
	
	public List<String> getAgents(String plNodePath) throws Exception;
	
	public List<NodeInfo> getNodeInfoList(String product) throws Exception;

	public List<NodeInfo> getNodeInfoList() throws Exception;
	
	public Map<String,String> getAllNodeInfoKey() throws Exception;
	
	public Map<String,Boolean> getOnlineNodes() throws Exception;
	
	public NodeInfo getOnlineNode(String product, String nodeId) throws Exception;
	
}
