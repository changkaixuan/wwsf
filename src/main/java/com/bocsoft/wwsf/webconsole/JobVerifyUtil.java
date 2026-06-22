package com.bocsoft.wwsf.webconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskLean;

public class JobVerifyUtil {

	//public static boolean isContainLoop(String jobId, StringBuffer sb) throws Exception {
	public static boolean isContainLoop(List<Task> taskList,List<TaskLean> taskLeanList, StringBuffer sb) throws Exception {
		// 查询出这个job下的所有task
		/*Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", jobId);
		List<Task> taskList = getTaskList(condition);*/
		if (null == taskList || taskList.size()<1) 
			return false;
		
		if (null == taskLeanList || taskLeanList.size()<1)
			return false;
		
		Map<String,Task> tasksMap = new HashMap<>();
		for (Task fTask : taskList) {
			tasksMap.put(fTask.getTaskId(), fTask);
		}
	
		// 查询出这个job模板下所有task被依赖关系
		//List<TaskLean> taskBeLeanList = taskLeanMapper.queryTaskBeLean(condition);
	
		// 把被依赖关系添加到对应的task模板
		if (taskLeanList != null && taskLeanList.size()>0) {
			List<String> leans = null;
			Task tTask = null;
			for (TaskLean fTaskLean : taskLeanList) {
				tTask = tasksMap.get(fTaskLean.getLeanTaskId());
				leans = tTask.getLeans()==null ? new ArrayList<String>() : tTask.getLeans();
				leans.add(fTaskLean.getTaskId());
				tTask.setLeans(leans);
				tasksMap.put(tTask.getTaskId(), tTask);
			}
		}
		
		//存相邻的两个任务，用来判断相邻两个任务是否相互依赖      key=",开始任务ID,结束任务ID", value=""
		Map<String,String> brotherTasksMap = new HashMap<String,String>();
		for(Map.Entry<String, Task> fTaskMap : tasksMap.entrySet()){
			boolean noLoop = loopTask(fTaskMap.getKey(),fTaskMap.getValue(),tasksMap,brotherTasksMap,sb);
			if(!noLoop){
				return true;
			}
		}
		return false;
	}
	
	public static boolean loopTask(
			String lineTasks,
			Task startTask,
			Map<String,Task> tasksMap,
			Map<String,String> brotherTasksMap,StringBuffer msg) throws Exception{
		String startTaskId = startTask.getTaskId();
		List<String> leansList = startTask.getLeans();
		if(null != leansList && leansList.size() > 0){
			for(String endTaskId : leansList){
				//相邻的两个任务不能相互依赖
				if(null != brotherTasksMap.get("," + endTaskId + "," + startTaskId + ",")){
					msg.append(startTaskId + " 和 " + endTaskId + ",相邻的两个任务不能相互依赖");
					return false;
				}
				brotherTasksMap.put("," + startTaskId + "," + endTaskId + ",", "");
				//一个线性任务流程，不能有闭环
				if((","+lineTasks+",").indexOf(","+endTaskId+",") != -1){
					msg.append("任务流程出现闭环: " + lineTasks);
					return false;
				}
				String tempLineTasks = lineTasks + "," + endTaskId;
				boolean hasRing = loopTask(
						tempLineTasks,
						tasksMap.get(endTaskId),
						tasksMap,
						brotherTasksMap,
						msg);
				if(!hasRing){
					return false;
				}
			}
		}else{
			//end of lean-line, do nothing
		}
		return true;
	}
	
}
