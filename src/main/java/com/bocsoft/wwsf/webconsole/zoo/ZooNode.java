package com.bocsoft.wwsf.webconsole.zoo;

import java.util.LinkedList;
import java.util.List;

import org.apache.zookeeper.CreateMode;

public class ZooNode implements Comparable<ZooNode>{
	//节点名称
	private String name;
	//节点模式-->是永久节点还是临时节点
	private CreateMode mode;
	private String modeType;
	//创建时间
	private String createTime;
	private Long seconds;
	//对znode最近修改的zxid
	private Long mzxid;
	//创建节点的事务的zxid
	private Long czxid;
	//数据长度
	private Integer dataLength;
	//如果znode是临时节点，则指示节点的会话ID；如果不是临时节点则为零
	private Long ephemeralOwner;
	//最后一次修改时间
	private String lastModifiedTime;
	//znode数据的修改次数
	private Integer version;
	//znode的ACL修改次数
	private Integer aversion;
	//znode子节点修改次数
	private Integer cversion;
	//znode子节点个数
	private Integer numOfChildren;
	//znode的ID
	private Long znodeId;
	
	//数据
	private String nodeData;
	
	
	public Long getSeconds() {
		return seconds;
	}
	public void setSeconds(Long seconds) {
		this.seconds = seconds;
	}
	public String getModeType() {
		return modeType;
	}
	public void setModeType(String modeType) {
		this.modeType = modeType;
	}
	public String getNodeData() {
		return nodeData;
	}
	public void setNodeData(String nodeData) {
		this.nodeData = nodeData;
	}
	public Long getZnodeId() {
		return znodeId;
	}
	public void setZnodeId(Long znodeId) {
		this.znodeId = znodeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public CreateMode getMode() {
		return mode;
	}
	public void setMode(CreateMode mode) {
		this.mode = mode;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public Long getMzxid() {
		return mzxid;
	}
	public void setMzxid(Long mzxid) {
		this.mzxid = mzxid;
	}
	public Long getCzxid() {
		return czxid;
	}
	public void setCzxid(Long czxid) {
		this.czxid = czxid;
	}
	public Integer getDataLength() {
		return dataLength;
	}
	public void setDataLength(Integer dataLength) {
		this.dataLength = dataLength;
	}
	public Long getEphemeralOwner() {
		return ephemeralOwner;
	}
	public void setEphemeralOwner(Long ephemeralOwner) {
		this.ephemeralOwner = ephemeralOwner;
	}
	public String getLastModifiedTime() {
		return lastModifiedTime;
	}
	public void setLastModifiedTime(String lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getAversion() {
		return aversion;
	}
	public void setAversion(Integer aversion) {
		this.aversion = aversion;
	}
	public Integer getCversion() {
		return cversion;
	}
	public void setCversion(Integer cversion) {
		this.cversion = cversion;
	}
	public Integer getNumOfChildren() {
		return numOfChildren;
	}
	public void setNumOfChildren(Integer numOfChildren) {
		this.numOfChildren = numOfChildren;
	}
	public static void main(String[] args) {
		/*ArrayList<ZkNode> zds = new ArrayList<ZkNode>();
		ZkNode z1 = new ZkNode();
		z1.setSeconds(1444446l);
		ZkNode z2 = new ZkNode();
		z2.setSeconds(145446l);
		ZkNode z3 = new ZkNode();
		z3.setSeconds(1464l);
		zds.add(z1);
		zds.add(z2);
		zds.add(z3);
		for(ZkNode zn :zds){
			System.out.println(zn.getSeconds());
		}
		Collections.sort(zds);
		for(ZkNode zn :zds){
			System.out.println(zn.getSeconds());
		}*/
		List<Integer> iii =new LinkedList<Integer>();
		iii.add(1);
		iii.add(2);
		iii.add(3);
		iii.add(4);
		iii.add(5);
		iii.add(6);
		iii.add(7);
		iii.add(8);
		iii.add(9);
		iii.add(10);
		iii.add(11);
		Integer start = 1 * 5;
		for(int i= start;i < start+5;i++){
			System.out.println(iii.get(i));
		}
	}
	
	@Override
	public int compareTo(ZooNode o) {
		if(this.getSeconds()> o.getSeconds()){
			return 1;
		}else if(this.getSeconds() < o.getSeconds()){
			return -1;
		}else{
			return 0;
		}
	}
	
}
