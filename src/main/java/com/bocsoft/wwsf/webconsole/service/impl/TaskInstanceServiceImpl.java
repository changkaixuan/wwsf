package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bocsoft.wwsf.webconsole.mapper.TaskInstanceMapper;
import com.bocsoft.wwsf.webconsole.model.TaskInstance;
import com.bocsoft.wwsf.webconsole.service.TaskInstanceService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("taskInstanceService")
public class TaskInstanceServiceImpl implements TaskInstanceService {
	
	@Autowired
	private TaskInstanceMapper taskInstanceMapper;

	@Override
	public PageInfo<TaskInstance> taskInsList(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskInstanceMapper.taskInsList(condition);
			}
		});
	}
	
	public PageInfo<Map<String,Object>> jobInsAndTaskInsList(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskInstanceMapper.jobInsAndTaskInsList(condition);
			}
		});
	}
	public List<Map<String,Object>> jobInsAndTaskInsList(Map<String, Object> condition) throws Exception{
	    return taskInstanceMapper.jobInsAndTaskInsList(condition);
	}

	@Override
	public int saveTaskIns(Map<String, Object> conditions) throws Exception{
		return taskInstanceMapper.saveTaskIns(conditions);
	}

	@Override
	public List<TaskInstance> getTaskInstanceByJobInsId(long jobInsId) throws Exception {
		Map<String, Object> condition = new HashMap<>();
		condition.put("jobInsId", jobInsId);
		return taskInstanceMapper.taskInsList(condition);
	}

	public int deleteTaskInsByBatch(Map<String, Object> conditions) throws Exception {
		return taskInstanceMapper.delTaskInsByBatch(conditions);
	}
	
	public int deleteTaskInsLeanByBatch(Map<String, Object> conditions) throws Exception {
		return taskInstanceMapper.delTaskInsLeanByBatch(conditions);
	}
	
	public int deleteTaskInsByProduct(String product) throws Exception {
		return taskInstanceMapper.delTaskInsByProduct(product);
	}
	
	public int deleteTaskInsLeanByProduct(String product) throws Exception {
		return taskInstanceMapper.delTaskInsLeanByProduct(product);
	}

	@Override
	public List<Map<String, Object>> getTaskState(Map<String, Object> condition) throws Exception {
		return taskInstanceMapper.getTaskState(condition);
	}

	@Override
	public TaskInstance setTaskInsLean(TaskInstance task) throws Exception {
		List<TaskInstance> leanList = null;
		Map<String, Object> map = null;
		Map<String, Integer> leansRuntime = null;
		List<String> beleans = null;
		
			// 查出这个task实例 依赖的task的实例及状态
			map = new HashMap<String, Object>();
			map.put("jobInsId", task.getJobInsId());
			map.put("taskInsId", task.getTaskInsId());
			leanList = taskInstanceMapper.getTaskInsLean(map);
			
			leansRuntime = new HashMap<String, Integer>();
			for (TaskInstance t : leanList) {
				leansRuntime.put(t.getTaskInsId(), (t.getState() < 20 ? 0 : 1));
			}
			task.setLeansRuntime(leansRuntime);
			
			// 查出这个task实例被哪些实例所依赖
			beleans = new ArrayList<String>();
			beleans = taskInstanceMapper.getTaskInsBeLean(map);
			task.setBeleans(beleans);
		return task;
	}

	@Override
	public List<Map<String, Object>> getTaskByBatchAndProduct(Map<String, Object> conditions) throws Exception {
		return taskInstanceMapper.getTaskByBatchAndProduct(conditions);
	}

	@Override
	public Map<String, Object> countTaskStateByTitle(List<Map<String, Object>> list) throws Exception {
		Map<String, Object> result = new HashMap<>();
		/**
		 * 长度为3的数组，用来存储每个标签下Task状态的总数，成功，失败
		 * index:0 存储总数
		 * index:1 存储成功
		 * index:2 存储失败
		 */
		Integer[] stateArr = null;
		List<String> titles = new ArrayList<String>();
		for (Map<String,Object> fMap : list) {
			if(null == fMap.get("TITLE") || fMap.get("TITLE").toString().trim().equals("")) {
				continue;
			}
			int taskInsTotal = Integer.valueOf(fMap.get("TOTAL_COUNT").toString());
			int taskFailedTotal = Integer.valueOf(fMap.get("FAILED_COUNT").toString());
			int taskSuccessTotal = Integer.valueOf(fMap.get("PASSED_COUNT").toString());
			String[] tags = fMap.get("TITLE").toString().split(",");
			for (String tag : tags) {
				// 找到这个标签对应的存储Task状态的数组，没有则新建
				if (result.containsKey(tag)) 
					stateArr = (Integer[]) result.get(tag);
				else {
					titles.add(tag);
					stateArr = new Integer[]{0,0,0};
				}
				// Task总数+1
				stateArr[0] += taskInsTotal;
				// Task失败状态+1
				stateArr[2] += taskFailedTotal;
				// Task成功状态+1
				stateArr[1] += taskSuccessTotal;
				result.put(tag, stateArr);
			}
		}
		result.put("titleList", titles);
		return result;
	}

	@Override
	public List<TaskInstance> getTaskInsInfo(Map<String, Object> condition) throws Exception {
		return taskInstanceMapper.getTaskInsInfo(condition);
	}

	@Override
	public List<TaskInstance> getTaskIns(Map<String, Object> condition) throws Exception {
		return taskInstanceMapper.taskInsList(condition);
	}
	
	public TaskInstance selectByPrimaryKey(long jobInsId, String taskInsId) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jobInsId", jobInsId);
		map.put("taskInsId", taskInsId);
		return taskInstanceMapper.selectByPrimaryKey(map);
	}
}
