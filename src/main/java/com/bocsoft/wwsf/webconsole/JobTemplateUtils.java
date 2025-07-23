package com.bocsoft.wwsf.webconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bocsoft.wwsf.webconsole.model.JobPanelInfo;
import com.bocsoft.wwsf.webconsole.model.Task;

public class JobTemplateUtils {

	public static RestResponse checkLoopback(RestResponse restResponse,String startPort,Map<String,List<String>> connects,String checkPort){
		Set<String> allPaths = new HashSet<>();
		Map<String,List<String>> endPortPaths = new HashMap<>();
		checkLoopback(endPortPaths, allPaths, connects, startPort, startPort, connects.get(startPort));
		List<String> LoopbackPath = endPortPaths.get(checkPort);
		if(LoopbackPath != null){
			restResponse.setSuccess(false);
			restResponse.put("LoopbackPath", LoopbackPath);
		} else {
			restResponse.setSuccess(true);
		}
		return restResponse;
	}
	
	/**
	 * 
	 * @param allPaths : 某个个点出发的路线图集合
	 * @param connects ： 两个点的链接集合
	 * @param interimPath ：临时路线
	 * @param path ： 最终路线
	 * @param nextPorts ：某个点下一个到达点集合
	 */
	public static void checkLoopback(Map<String,List<String>> endPortPaths,Set<String> allPaths, Map<String,List<String>> connects, String interimPath, String path, List<String> nextPorts){
		if(nextPorts != null){
			for(int i = 0; i<nextPorts.size(); i++){
				path = path + "->" + nextPorts.get(i);//拿到路线分支
				List<String> childNextPorts = connects.get(nextPorts.get(i));
				checkLoopback(endPortPaths,allPaths,connects,path,path,childNextPorts);//拿到这条路线分支下的所有路线
				path = interimPath;
			}
		} else {
			String[] ports = path.split("->");
			String endPort = ports[ports.length -1];
			List<String> oneEndPortPaths = endPortPaths.get(endPort);
			if(oneEndPortPaths != null){
				oneEndPortPaths.add(path);
			} else {
				endPortPaths.put(endPort, new ArrayList<String>());
				endPortPaths.get(endPort).add(path);
			}
			allPaths.add(path);//如果是终点，就把路线图加进去
		}
	}
	
	/**
	 * 通过leans关系，得到tasks的beleans关系,保存到jobPanelInfo中
	 * @param tasks
	 * @param jobPanelInfo
	 */
	public static void leansToBeleans(List<Task> tasks,JobPanelInfo jobPanelInfo){
		Iterator<Task> iterator = tasks.iterator();
		Map<String,List<String>> allBeLeans = new HashMap<>();
		while(iterator.hasNext()){
			Task task = iterator.next();		
			List<String> beLans = task.getLeans();
			if(beLans != null){
				Iterator<String> iteratorBeLans= beLans.iterator();
				while(iteratorBeLans.hasNext()){
					String beLeanTaskId = iteratorBeLans.next();
					List<String> beLeansUse = allBeLeans.get(beLeanTaskId);
					if(beLeansUse != null){
						beLeansUse.add(task.getTaskId());
					} else {
						beLeansUse = new ArrayList<>();
						beLeansUse.add(task.getTaskId());
						allBeLeans.put(beLeanTaskId, beLeansUse);
					}
				}
			}
		}
		if(jobPanelInfo != null){
			jobPanelInfo.setBeLeansRelationShip(allBeLeans);
		}
		Iterator<Task> iterator2 = tasks.iterator();
		while(iterator2.hasNext()){
			Task task = iterator2.next();
			task.setBeleans(allBeLeans.get(task.getTaskId()));
		}
	}
	
	
	/**
	 * 对JOB下的task进行排序，node从0开始
	 * @param tasks
	 */
	public static void taskPanelFormat(List<Task> tasks,Map<Integer,List<String>> taskNodesValue){
		List<String> determinedNodeTasks = new ArrayList<>(); //已经确定好node的tasks
		List<String> parentNodes = new ArrayList<>();//父节点的所有tasks
		List<Task> undeterminedNodeTasks = new ArrayList<>(tasks); //未确定node的tasks
		taskNodesValue.put(0, new ArrayList<String>());
		Iterator<Task> iterator = tasks.iterator();
		while(iterator.hasNext()){
			Task task = iterator.next();
			if(task.getLeans() == null || task.getLeans().size() == 0){
				String taskId = task.getTaskId();
				taskNodesValue.get(0).add(taskId);
				determinedNodeTasks.add(taskId);
				parentNodes.add(taskId);//
				undeterminedNodeTasks.remove(task);//去除掉根节点
			}
		}
		int node = 1; //node数值
		int endCount = undeterminedNodeTasks.size();
		while(undeterminedNodeTasks.size() > 0){
			taskNodesValue.put(node, new ArrayList<String>());
			taskPanelFormat(undeterminedNodeTasks,taskNodesValue,parentNodes,determinedNodeTasks,node);
			if(undeterminedNodeTasks.size() == endCount){
				List<String> tasksEnd = new ArrayList<>();
				for(Task x : undeterminedNodeTasks){
					tasksEnd.add(x.getTaskId());
				}
				taskNodesValue.put(node, tasksEnd);
				break;
			} else {
				endCount = undeterminedNodeTasks.size();
			}
			node++;
		}
	}
	
	public static void taskPanelFormat(List<Task> undeterminedNodeTasks,Map<Integer,List<String>> taskNodesValue,List<String> parentNodes,List<String> determinedNodeTasks,int node){
		List<Task> taskRemove = new ArrayList<>();//此次循环确定好node的tasks
		List<String> taskRemoveId = new ArrayList<>();//此次循环确定好node的tasks
		Iterator<Task> iterator = undeterminedNodeTasks.iterator();
		while(iterator.hasNext()){
			Task task = iterator.next();
			String taskId = task.getTaskId();
			if(!determinedNodeTasks.contains(taskId)){
				List<String> beLeans = task.getLeans();
				List<String> beLeansCopy = new ArrayList<>(beLeans);
				if(beLeans != null){
					List<String> parentNodesCopy = new ArrayList<>(parentNodes);
					parentNodesCopy.retainAll(beLeans);
					beLeansCopy.retainAll(taskRemoveId);
					//此task的依赖完全要包含于已经确定好节点的tasks,且他的依赖tasks不能处于这个node节点
					if(parentNodesCopy.size() > 0 && parentNodesCopy.size() == beLeans.size() && beLeansCopy.size() < 1){//属于节点下面的直接子节点
						taskNodesValue.get(node).add(taskId);
						parentNodes.add(taskId);
						taskRemoveId.add(taskId);
						determinedNodeTasks.add(taskId);
						taskRemove.add(task);
					}
				}
			}
		}
		Iterator<Task> iteratorRemove = taskRemove.iterator();
		while(iteratorRemove.hasNext()){
			Task task = iteratorRemove.next();
			undeterminedNodeTasks.remove(task);
		}
		
	}
	
}
