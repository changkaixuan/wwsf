package com.bocsoft.wwsf.webconsole.zoo;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bocsoft.wwsf.webconsole.RestResponse;
import com.bocsoft.wwsf.webconsole.model.NodeInfo;

@RestController
public class ZooController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ZooService zooService;
	
	@GetMapping("/zoo/children")
	@RequiresPermissions("menu:zoo-list")
	public String childrenPage(String path, Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		if (pageSize == null || pageSize <= 0) pageSize = 20;
		if (pageNumber == null || pageNumber <= 0) pageNumber = 1;
		try {
			List<ZooNode> nodes = zooService.getChildrenNodes(path);
			response.put("total", nodes.size());
			response.put("pageSize", pageSize);
			if (!nodes.isEmpty()) {
				int beginIndex = 0;
				int endIndex = nodes.size();
				int realPageCount = (nodes.size()%pageSize > 0 ? (nodes.size()/pageSize)+1 : nodes.size()/pageSize);
				if(pageNumber > realPageCount) pageNumber = realPageCount;
				if(pageNumber == 1) {
					endIndex = nodes.size() <= pageSize ? nodes.size() : pageSize;
				} else if (pageNumber == realPageCount) {
					beginIndex = (pageNumber-1) * pageSize;
				} else {
					beginIndex = (pageNumber-1) * pageSize;
					endIndex = beginIndex + pageSize;
				}
				response.put("pageNumber", pageNumber);
				response.put("rows", nodes.subList(beginIndex, endIndex));				
			} else {
				response.put("pageNumber", 1);
				response.put("rows", new ArrayList<ZooNode>());	
			}
			
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get children {}", path, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/zoo/nodeInfo")
	public String node(@RequestParam String path) {
		RestResponse response = new RestResponse();
		try {
			ZooNode node = zooService.getData(path);
			response.put("node", node);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("get node info {}", path, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@PutMapping("/zoo/nodeModify")
	public String modify(@RequestBody String path) {
		RestResponse response = new RestResponse();
		try {
			ZooNode node = JSON.parseObject(path, ZooNode.class);
			zooService.save(node);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("modify node info {}", path, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@DeleteMapping("/zoo/nodeDel")
	public String delete(@RequestParam String path, boolean delChildren) {
		RestResponse response = new RestResponse();
		try {
			zooService.del(path, delChildren);
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("remove node info {}", path, e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return JSON.toJSONString(response);
	}
	
	@GetMapping("/zoo/selectAgent")
	public RestResponse selectAgent(String product) {
		RestResponse response = new RestResponse();
		try {
			response.put("agents", zooService.getAgents(Naming.getPlNodeRoot()));
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/zoo/selectAgent", e);
			response.setSuccess(false);
			//response.setInfo(e.getMessage());
			response.setInfo("没有在线节点");
		}
		return response;
	}
	
	@GetMapping("/zoo/getNodeInfo")
	public RestResponse getNodeInfo(String product,Integer pageNumber, Integer pageSize) {
		RestResponse response = new RestResponse();
		try {
			List<NodeInfo> result = zooService.getNodeInfoList(product);
			if (null != result && result.size() > 0) {
				int beginIndex = 0;
				int endIndex = result.size();
				int realPageCount = (result.size()%pageSize > 0 ? (result.size()/pageSize)+1 : result.size()/pageSize);
				if(pageNumber > realPageCount) pageNumber = realPageCount;
				if(pageNumber == 1) {
					endIndex = result.size() <= pageSize ? result.size() : pageSize;
				} else if (pageNumber == realPageCount) {
					beginIndex = (pageNumber-1) * pageSize;
				} else {
					beginIndex = (pageNumber-1) * pageSize;
					endIndex = beginIndex + pageSize;
				}
				response.put("rows", result.subList(beginIndex, endIndex));
			} else {
				response.put("rows", result);
			}
			response.put("total", result.size());
			response.setSuccess(true);
		} catch (Exception e) {
			logger.error("/zoo/getNodeInfo", e);
			response.setSuccess(false);
			response.setInfo(e.getMessage());
		}
		return response;
	}

}
