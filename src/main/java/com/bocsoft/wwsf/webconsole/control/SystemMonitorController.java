package com.bocsoft.wwsf.webconsole.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;
import com.bocsoft.wwsf.webconsole.zoo.Naming;

@RestController
public class SystemMonitorController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CuratorFramework curator;
	
	@GetMapping("/monitor/machines")
	public String machines() {
		return JSON.toJSONString(getNodeInfo());
	}
	
	/*@GetMapping("/monitor/info")
	public String monitorInfo(String product,String nodeId) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			String fs_key = RedisKey.getDiskLogKey(product, nodeId);
			Set<String> fdata = redisService.zrange(fs_key, -1, -1);
			FileSystemInfomation fsi = null;
			if (fdata != null && fdata.size() > 0) {
				fsi = JSON.parseObject(fdata.toArray(new String[0])[0], FileSystemInfomation.class);
			}
			
			String cpu_key = RedisKey.getCpuLogKey(product, nodeId);
			CpuInfomation cpuInfo = null;
			Set<String> cdata = redisService.zrange(cpu_key, -1, -1);
			if (cdata != null && cdata.size() > 0) {
				cpuInfo = JSON.parseObject(cdata.toArray(new String[0])[0], CpuInfomation.class);
			}
			
			String memo_key = RedisKey.getMemoLogKey(product, nodeId);
			NodeMemInfo memInfo = null;
			Set<String> mdata = redisService.zrange(memo_key, -1, -1);
			if (mdata != null && mdata.size() > 0) {
				memInfo = JSON.parseObject(mdata.toArray(new String[0])[0], NodeMemInfo.class);
			}
			
			String pool_key = RedisKey.getPluginPoolLogKey(product, nodeId);
			NodeQueueInfo queInfo = null;
			Set<String> pdata = redisService.zrange(pool_key, -1, -1);
			if (pdata != null && pdata.size() > 0) {
				queInfo = JSON.parseObject(pdata.toArray(new String[0])[0], NodeQueueInfo.class);
			}
			
			//处理“-”符号，转化为“_”
			handleSymbol(queInfo);
			response.put("fsi", fsi);
			response.put("cpuInfo", cpuInfo);
			response.put("memInfo", memInfo);
			response.put("queueInfo", queInfo);
			response.setRes(true);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setRes(false);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/*@GetMapping("/monitor/infos")
	public String monitorInfos(String product, String nodeId, Date time) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			
			String fs_key = RedisKey.getDiskLogKey(product, nodeId);
			Set<String> fdata = redisService.zrange(fs_key, -1, -1);
			FileSystemInfomation fsi = null;
			if (fdata != null && fdata.size() > 0) {
				fsi = JSON.parseObject(fdata.toArray(new String[0])[0], FileSystemInfomation.class);
			}
			
			String cpu_key = RedisKey.getCpuLogKey(product, nodeId);
			CpuInfomation cpuInfo = null;
			Set<String> cdata = redisService.zrange(cpu_key, -1, -1);
			if (cdata != null && cdata.size() > 0) {
				cpuInfo = JSON.parseObject(cdata.toArray(new String[0])[0], CpuInfomation.class);
			}
			
			String memo_key = RedisKey.getMemoLogKey(product, nodeId);
			NodeMemInfo memInfo = null;
			Set<String> mdata = redisService.zrange(memo_key, -1, -1);
			if (mdata != null && mdata.size() > 0) {
				memInfo = JSON.parseObject(mdata.toArray(new String[0])[0], NodeMemInfo.class);
			}
			
			String pool_key = RedisKey.getPluginPoolLogKey(product, nodeId);
			NodeQueueInfo queInfo = null;
			Set<String> pdata = redisService.zrange(pool_key, -1, -1);
			if (pdata != null && pdata.size() > 0) {
				queInfo = JSON.parseObject(pdata.toArray(new String[0])[0], NodeQueueInfo.class);
			}
			
			//处理“-”符号，转化为“_”
			handleSymbol(queInfo);
			response.put("fsi", fsi);
			response.put("cpuInfo", cpuInfo);
			response.put("memInfo", memInfo);
			response.put("queueInfo", queInfo);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/*@GetMapping("/monitor/queueinfo")
	public String queueInfo(String product, String nodeId) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			String pool_key = RedisKey.getPluginPoolLogKey(product, nodeId);
			Set<Tuple> poolInfos = redisService.zrangeWithScores(pool_key, -1, -1);
			if(poolInfos.size() == 1) {
				Tuple tt = poolInfos.iterator().next();
				double score = tt.getScore();
				NodeQueueInfo nqi = JSON.parseObject(tt.getElement(), NodeQueueInfo.class);
				Map<String,Map<String,Integer>> psmap = nqi.getQueue();
				Iterator<String> itfs = psmap.keySet().iterator();
				List<String> queues = new ArrayList<String>();
				List<Integer> coreSizes = new ArrayList<Integer>();
				List<Integer> maxSizes = new ArrayList<Integer>();
				List<Integer> activeSizes = new ArrayList<Integer>();
				List<Integer> queueSizes = new ArrayList<Integer>();
				while(itfs.hasNext()) {
					String queue = itfs.next();
					queues.add(queue);
					Map<String,Integer> qsif = psmap.get(queue);
					coreSizes.add(qsif.get("CoreSize"));
					maxSizes.add(qsif.get("MaxSize"));
					activeSizes.add(qsif.get("Active"));
					queueSizes.add(qsif.get("QueueSize"));
				}
				response.put("score", score);
				response.put("coreSizes", coreSizes);
				response.put("maxSizes", maxSizes);
				response.put("activeSizes", activeSizes);
				response.put("queues", queues);
				response.put("queueSizes", queueSizes);
				response.setRes(true);
			} else {
				response.setRes(false);
			}
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/*@GetMapping("/monitor/fsinfo")
	public String fsInfo(String product, String nodeId) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			String disk_key = RedisKey.getDiskLogKey(product, nodeId);
			Set<Tuple> fsInfos = redisService.zrangeWithScores(disk_key, -1, -1);
			if(fsInfos.size() == 1) {
				Tuple tt = fsInfos.iterator().next();
				double score = tt.getScore();
				FileSystemInfomation fsi = JSON.parseObject(tt.getElement(), FileSystemInfomation.class);
				Iterator<NodeFsInfo> itfs = fsi.getFss().iterator();
				List<String> dirs = new ArrayList<String>();
				List<Long> sizes = new ArrayList<Long>();
				List<Long> usedSizes = new ArrayList<Long>();
				while(itfs.hasNext()) {
					NodeFsInfo nfi = itfs.next();
					sizes.add(nfi.getSize());
					dirs.add(nfi.getDevDir());
					usedSizes.add(nfi.getUsed());
				}
				response.put("score", score);
				response.put("dirs", dirs);
				response.put("sizes", sizes);
				response.put("usedSizes", usedSizes);
				response.setRes(true);
			} else {
				response.setRes(false);
			}
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/*@GetMapping("/monitor/memoinfo")
	public String memoInfo(String product, String nodeId, Long point) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			String memo_key = RedisKey.getMemoLogKey(product, nodeId);
			Set<Tuple> cpuInfos = redisService.zrangeByscoreWithScores(memo_key, point, Long.MAX_VALUE);
			List<Double[]> memos = new ArrayList<Double[]>();
			double max = point;
			long total = 0;
			for (Tuple info : cpuInfos) {
				Double[] xy = new Double[2];
				xy[0] = info.getScore();
				NodeMemInfo cif = JSON.parseObject(info.getElement(), NodeMemInfo.class);
				if (total == 0) total = cif.getTotal();
				double combined = cif.getUsedPercent();
				BigDecimal b = new BigDecimal(combined);
				xy[1] = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				memos.add(xy);
				if (max < info.getScore()) {
					max = info.getScore();
				}
			}
			response.put("total", total);
			response.put("memos", memos);
			response.put("point", max);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/*@GetMapping("/monitor/cpuinfo")
	public String cpuInfo(String product, String nodeId, Long point) {
		RestResponse response = new RestResponse();
		try{
			NodeInfo nodeInfo = getNodeInfo(product, nodeId);
			if (nodeInfo == null) {
				response.setRes(false);
				response.setSuccess(true);
				response.setInfo("节点已处于离线状态");
				return JSON.toJSONString(response);
			}
			String cpu_key = RedisKey.getCpuLogKey(product, nodeId);
			Set<Tuple> cpuInfos = redisService.zrangeByscoreWithScores(cpu_key, point, Long.MAX_VALUE);
			List<Double[]> cpus = new ArrayList<Double[]>();
			double max = point;
			int core = 0;
			long mhz = 0;
			String vendor = null;
			for (Tuple info : cpuInfos) {
				Double[] xy = new Double[2];
				xy[0] = info.getScore();
				CpuInfomation cif = JSON.parseObject(info.getElement(), CpuInfomation.class);
				List<NodeCpuInfo> ci = cif.getCpus();
				core = ci.size();
				double combined = 0;
				for (NodeCpuInfo nci : ci) {
					if (mhz == 0) mhz = nci.getMhz();
					if (vendor == null) vendor = nci.getVendor();
					combined += nci.getCombined();
				}
				combined = (combined*100)/ci.size();
				BigDecimal b = new BigDecimal(combined);
				xy[1] = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				cpus.add(xy);
				if (max < info.getScore()) {
					max = info.getScore();
				}
			}
			response.put("core", core);
			response.put("mhz", mhz);
			response.put("vendor", vendor);
			response.put("cpus", cpus);
			response.put("point", max);
			response.setSuccess(true);
		}catch(Exception e){
			logger.error("",e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}*/
	
	/**
	 * 对数据中的特殊符号做处理，以正确在前台显示
	 * @param nodeQueueInfo
	 */
	/*private void handleSymbol(NodeQueueInfo nodeQueueInfo) {
		Map<String, Map<String, Integer>> newQueue = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> queue = nodeQueueInfo.getQueue();
		Set<String> keySet = queue.keySet();
		Iterator<String> strs = keySet.iterator();
		while(strs.hasNext()){
			String str = strs.next();
			String oldStr = "-",newStr = "_";
			if(str.contains(oldStr)){
				String newKey = str.replace(oldStr, newStr);
				newQueue.put(newKey, queue.get(str));
			}else{
				newQueue.put(str, queue.get(str));
			}
		}
		nodeQueueInfo.getQueue().clear();
		nodeQueueInfo.setQueue(newQueue);
	}*/
	
	private Map<String, List<NodeInfo>> getNodeInfo() {
		String plNodeRoot = Naming.getPlNodeRoot();
		Map<String, List<NodeInfo>> maps = new HashMap<String, List<NodeInfo>>();
		try {
			List<String> nodeNames = curator.getChildren().forPath(plNodeRoot);
			if (nodeNames != null && nodeNames.size() > 0) {
				for(String nodeName : nodeNames){
					String nodePath = plNodeRoot + "/" + nodeName;
					byte[] data = curator.getData().forPath(nodePath);
					String nodeData = new String(data,"utf-8");
					NodeInfo nif = JSON.parseObject(nodeData,NodeInfo.class);
					String product = nif.getProduct();
					if (!maps.containsKey(product)) {
						maps.put(product, new ArrayList<NodeInfo>());
					}
					maps.get(product).add(nif);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return maps;
	}
	
	private NodeInfo getNodeInfo(String product, String nodeId) {
		String plNode = Naming.getPlNode(product, nodeId);
		try {
			Stat stat = curator.checkExists().forPath(plNode);
			if (stat != null) {
				byte[] data = curator.getData().forPath(plNode);
				String nodeData = new String(data,"utf-8");
				return JSON.parseObject(nodeData,NodeInfo.class);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return null;
	}

}
