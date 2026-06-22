package com.bocsoft.wwsf.webconsole.zoo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;

@Service
public class ZooServiceImpl implements ZooService{

	@Autowired
	private CuratorFramework curator;
	
	public List<ZooNode> getChildrenNodes(String parentPath) throws Exception{
		List<String> childrenNodes;
		List<ZooNode> zooNodes = new LinkedList<ZooNode>();
		if(parentPath.contains("_|_")){
			parentPath = parentPath.replace("_|_", "##").replace("$", "]");
		}
		Stat stat = curator.checkExists().forPath(parentPath);
		if(stat == null){
			throw new Exception("node nofound");
		}else{
			childrenNodes = curator.getChildren().forPath(parentPath);
			Iterator<String> nodesIterator = childrenNodes.iterator();
			while(nodesIterator.hasNext()){
				String nodeName = nodesIterator.next();
				String childPath = parentPath;
				if("/".equals(parentPath)){
					childPath = childPath + nodeName;
				}else{
					childPath = childPath + "/" + nodeName;
				}
				Stat st = curator.checkExists().forPath(childPath);
				ZooNode zooNode = new ZooNode();
				zooNode.setName(nodeName);
				zooNode.setMode(st.getEphemeralOwner() > 0 ? CreateMode.EPHEMERAL : CreateMode.PERSISTENT);
				zooNode.setSeconds(st.getCtime());
				zooNode.setNumOfChildren(st.getNumChildren());
				zooNodes.add(zooNode);
			}
			Collections.sort(zooNodes);
			return zooNodes;
		}
	}
	
	/**
	 * 判断节点类型
	 * @param client
	 * @param nodePath
	 * @return
	 */
	public CreateMode getNodeType(CuratorFramework client,String nodePath){
		try {
			Stat stat = client.checkExists().forPath(nodePath);
			if(stat == null){
				return null;
			}
			if(stat.getEphemeralOwner()>0){
				return CreateMode.EPHEMERAL;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CreateMode.PERSISTENT;
	}

	public ZooNode getData(String nodePath) throws Exception {
		String nodeData;
		if(nodePath.contains("_|_")){
			nodePath = nodePath.replace("_|_", "##").replace("$", "]");
		}
		Stat stat = curator.checkExists().forPath(nodePath);
		if(stat == null){
			throw new Exception("node nofound");
		}else{
			ZooNode zkNode = new ZooNode();
			byte[] data = curator.getData().forPath(nodePath);
			String nodeName = nodePath;
			if(data == null){
				nodeData = null;
			}else{
				nodeData = new String(data,"utf-8");
			}
			zkNode.setName(nodeName);
			zkNode.setMode(getNodeType(curator,nodePath));
			zkNode.setAversion(stat.getAversion());
			zkNode.setCreateTime(tranformTime((stat.getCtime())));
			zkNode.setCversion(stat.getCversion());
			zkNode.setCzxid(stat.getCzxid());
			zkNode.setDataLength(stat.getDataLength());
			zkNode.setEphemeralOwner(stat.getEphemeralOwner());
			zkNode.setLastModifiedTime(tranformTime((stat.getCtime())));
			zkNode.setMzxid(stat.getMzxid());
			zkNode.setNumOfChildren(stat.getNumChildren());
			zkNode.setVersion(stat.getVersion());
			zkNode.setZnodeId(stat.getPzxid());
			zkNode.setNodeData(nodeData);
			return zkNode;
		}
	}

	public void save(ZooNode node) throws Exception{
		Stat stat = curator.checkExists().forPath(node.getName());
		if(stat == null){
			throw new Exception("node not exists");
		}else{
			curator.setData().forPath(node.getName(),(node.getNodeData() == null ? "":node.getNodeData()).getBytes("utf-8"));
		}
	}

	public String add(ZooNode node) {
		try{
			switch(node.getModeType()){
			case "4":
				node.setMode(CreateMode.EPHEMERAL_SEQUENTIAL);
				createNodeWithMode(curator,node);
				break;
			case "3":
				node.setMode(CreateMode.EPHEMERAL);
				createNodeWithMode(curator,node);
				break;
			case "2":
				node.setMode(CreateMode.PERSISTENT_SEQUENTIAL);
				createNodeWithMode(curator,node);
				break;
			default:
				node.setMode(CreateMode.PERSISTENT);
				createNodeWithMode(curator,node);
			}
		}catch(Exception e){
			return "{\"success\":" + false +"}";
		}
		return "{\"success\":" + true +"}";
	}
	
	private void createNodeWithMode(CuratorFramework curator,ZooNode node) throws Exception{
		curator.create()
						.withMode(node.getMode())
						.forPath(node.getName(),
								node.getNodeData().getBytes("utf-8"));
	}
	
	public String delete(String nodePath) {
		Stat stat = null;
		if(nodePath.contains("_|_")){
			nodePath = nodePath.replace("_|_", "##").replace("$", "]");
		}
		try {
			stat = curator.checkExists().forPath(nodePath);
		} catch (Exception e) {
			return "{\"success\":"+ false +"}";
		}
		if(stat == null){
			return "{\"success\":"+ false +"}";
		}
		try {
			curator.delete().deletingChildrenIfNeeded().forPath(nodePath);
		} catch (Exception e) {
			return "{\"success\":"+ false +"}";
		}
		return "{\"success\":"+ true +"}";
	}
	
	public void del(String nodePath, boolean delChildren) throws Exception {
		if(nodePath.contains("_|_")){
			nodePath = nodePath.replace("_|_", "##").replace("$", "]");
		}
		Stat stat = curator.checkExists().forPath(nodePath);
		if(stat == null){
			return;
		}
		if (delChildren) {
			curator.delete().deletingChildrenIfNeeded().forPath(nodePath);
		} else {
			curator.delete().forPath(nodePath);
		}
	}
	
	public List<String> getAgents(String plNodePath) throws Exception {
		List<String> rList = new ArrayList<String>();
		List<String> nodes = curator.getChildren().forPath(plNodePath);
		if(nodes != null && nodes.size() > 0) {
			for(String fNode : nodes) {//fNode值格式：产品::节点
				rList.add(fNode.split("::")[1]);
			}
		}
		return rList;
	}

	public List<NodeInfo> getNodeInfoList(String product) throws Exception{
		List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		//取master节点路径
		List<String> mNodeList = curator.getChildren().forPath(Naming.getPlMasterRoot());
		String mNodeName = ""; //值格式：产品::节点号        例如：wws::50000
		if(null != mNodeList && mNodeList.size() > 0) {
			mNodeName = mNodeList.get(0);
		}
		//取所有节点路径
		List<String> sNodeList = curator.getChildren().forPath(Naming.getPlNodeRoot());
		if(sNodeList != null && sNodeList.size() > 0) {
			for(String fSNodeName : sNodeList) {
				String tProduct = fSNodeName.split("::")[0];
				if(null != product && product.equals(tProduct)) {
					String nodePath = Naming.getPlNodeRoot() + "/" + fSNodeName;
					NodeInfo nodeInfo = JSON.parseObject(new String(curator.getData().forPath(nodePath)), NodeInfo.class);
					if(fSNodeName.equals(mNodeName)) {
						nodeInfo.setOnline(1);
					}else {
						nodeInfo.setOnline(-1);
					}
					nodeInfoList.add(nodeInfo);
				}
			}
		}
		return nodeInfoList;
	}
	
	public Map<String,String> getAllNodeInfoKey() throws Exception{
		Map<String,String> nodeInfoMap = new HashMap<String,String>();
		//取所有节点路径
		List<String> sNodeList = curator.getChildren().forPath(Naming.getPlNodeRoot());
		if(sNodeList != null && sNodeList.size() > 0) {
			for(String fSNodeName : sNodeList) {
				nodeInfoMap.put(fSNodeName.split("::")[0]+","+fSNodeName.split("::")[1], fSNodeName.split("::")[0]+","+fSNodeName.split("::")[1]);
			}
		}
		return nodeInfoMap;
	}

	@Override
	public List<NodeInfo> getNodeInfoList() throws Exception {
		List<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		//取master节点路径
		List<String> mNodeList = curator.getChildren().forPath(Naming.getPlMasterRoot());
		String mNodeName = ""; //值格式：产品::节点号        例如：wws::50000
		if(null != mNodeList && mNodeList.size() > 0) {
			mNodeName = mNodeList.get(0);
		}
		//取所有节点路径
		List<String> sNodeList = curator.getChildren().forPath(Naming.getPlNodeRoot());
		if(sNodeList != null && sNodeList.size() > 0) {
			for(String fSNodeName : sNodeList) {
				String nodePath = Naming.getPlNodeRoot() + "/" + fSNodeName;
				NodeInfo nodeInfo = JSON.parseObject(new String(curator.getData().forPath(nodePath)), NodeInfo.class);
				if(fSNodeName.equals(mNodeName)) {
					nodeInfo.setOnline(1);
				}else {
					nodeInfo.setOnline(-1);
				}
				nodeInfoList.add(nodeInfo);
			}
		}
		return nodeInfoList;
	}

	@Override
	public Map<String, Boolean> getOnlineNodes() throws Exception {
		Map<String, Boolean> nodes = new HashMap<String, Boolean>();
		//取master节点
		List<String> mNodeList = curator.getChildren().forPath(Naming.getPlMasterRoot());
		if(null != mNodeList && mNodeList.size() > 0) {
			nodes.put(mNodeList.get(0), true);
		}
		
		List<String> nodeList = curator.getChildren().forPath(Naming.getPlNodeRoot());
		if(nodeList != null && nodeList.size() > 0) {
			for(String name : nodeList) {
				if (!nodes.containsKey(name)) {
					nodes.put(name, false);
				} 
			}
		}
		return nodes;
	}
	
	public NodeInfo getOnlineNode(String product, String nodeId) throws Exception {
		Stat stat = curator.checkExists().forPath(Naming.getPlNode(product, nodeId));
		if (stat == null) {
			return null;
		} else {
			return JSON.parseObject(new String(curator.getData().forPath(Naming.getPlNode(product, nodeId))), NodeInfo.class);
		}
	}
	
	
}
