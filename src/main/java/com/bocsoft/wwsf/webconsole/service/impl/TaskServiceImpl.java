package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.JobTemplateUtils;
import com.bocsoft.wwsf.webconsole.JobVerifyUtil;
import com.bocsoft.wwsf.webconsole.dag.DirectedGraph;
import com.bocsoft.wwsf.webconsole.dag.Edge;
import com.bocsoft.wwsf.webconsole.dag.Vertex;
import com.bocsoft.wwsf.webconsole.mapper.JobDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.ParameterMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskDimensionMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskLeanMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskSpecialLeanMapper;
import com.bocsoft.wwsf.webconsole.model.JobPanelInfo;
import com.bocsoft.wwsf.webconsole.model.Parameter;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.model.TaskDimension;
import com.bocsoft.wwsf.webconsole.model.TaskEdges;
import com.bocsoft.wwsf.webconsole.model.TaskGraph;
import com.bocsoft.wwsf.webconsole.model.TaskLean;
import com.bocsoft.wwsf.webconsole.model.TaskSpecialLean;
import com.bocsoft.wwsf.webconsole.service.TaskService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("taskService")
public class TaskServiceImpl implements TaskService{
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private TaskLeanMapper taskLeanMapper;
	
	@Autowired
	private TaskSpecialLeanMapper taskSpecialLeanMapper;
	
	@Autowired
	private TaskDimensionMapper taskDimensionMapper;
	
	@Autowired
	private ParameterMapper parameterMapper;
	
	@Autowired
	private JobDimensionMapper jobDimensionMapper;
	
	public PageInfo<Task> queryTaskPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskMapper.queryTaskPage(condition);
			}
		});
	}
	
	public List<Task> getTaskList(Map<String, Object> condition) throws Exception{
		return taskMapper.queryTaskPage(condition);
	}
	
	public Task getTask(String jobId,String taskId) throws Exception{
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("taskId", taskId);
		List<Task> taskList = taskMapper.queryTaskPage(condition);
		if(null == taskList || taskList.size()<1) {
			return null;
		}
		Task task = taskList.get(0);
		//查询TASK模板属性
		condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("sourceId", taskId);
		List<Parameter> parameterList = parameterMapper.queryParameterPage(condition);
		// 插件名称和程序名称不一致时多查询一次
		if (task.getPlugin()!=null && task.getProgramName()!=null && !task.getPlugin().equals(task.getProgramName())) {
			condition = new HashMap<String,Object>();
			condition.put("jobId", task.getPlugin());
			condition.put("sourceId", task.getProgramName());
			List<Parameter> list = parameterMapper.queryParameterPage(condition);
			if (parameterList != null) {
				parameterList.addAll(list);
			}
		}
		if(null != parameterList && parameterList.size()>0) {
			Map<String,String> parameters = new HashMap<String,String>();
			for(Parameter fParameter : parameterList) {
				parameters.put(fParameter.getName(), fParameter.getValue());
			}
			task.setParameters(parameters);
		}
		//查询TASK模板维度
		condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("taskId", taskId);
		List<TaskDimension> taskDimensionList = taskDimensionMapper.queryTaskDimensionPage(condition);
		if(null != taskDimensionList && taskDimensionList.size()>0) {
			TreeMap<String, Set<String>> dimensions = new TreeMap<String, Set<String>>(); //key=维度名称     Set<String>指：实体名称（实体名称值格式：实体名称）或标签（标签值格式：TAG:v_5）列表
			for(TaskDimension fTaskDimension : taskDimensionList) {
				Set<String> set = null;
				if(null == dimensions.get(fTaskDimension.getDmsnName())) {
					set = new TreeSet<String>();
				}else {
					set = dimensions.get(fTaskDimension.getDmsnName());
				}
				set.add(fTaskDimension.getEntityName());
				dimensions.put(fTaskDimension.getDmsnName(), set);
			}
			task.setDimensions(dimensions);
		}
		return task;
	}
	
	@Transactional
	public void saveTaskRela(JobPanelInfo jobPanelInfo) throws Exception{
		//删除JOB模板下所有TASK位置
		Map<String,Object> dMap = new HashMap<String,Object>();
		dMap.put("jobId",jobPanelInfo.getJobId());
		taskMapper.deleteTaskGraph(dMap);
		//删除JOB模板下所有TASK依赖关系
		dMap.put("leanJobIdIsNull", "leanJobIdIsNull");
		taskLeanMapper.deleteTaskLean(dMap);
		//保存JOB模板下所有TASK位置
		Map<String, Map<String, Integer>> locations = jobPanelInfo.getLocation(); //第一层key=taskId, 第二层key=x或y,第二层value=x轴对应值或y轴对应的值
		if(null != locations && locations.size()>0) {
			for(Map.Entry<String, Map<String, Integer>> fLocation : locations.entrySet()) {
				Map<String,Integer> xyMap = fLocation.getValue();
				TaskGraph taskGraph = new TaskGraph();
				taskGraph.setJobId(jobPanelInfo.getJobId());
				taskGraph.setTaskId(fLocation.getKey());
				taskGraph.setPointX(xyMap.get("pointX"));
				taskGraph.setPointY(xyMap.get("pointY"));
				taskMapper.insertTaskGraph(taskGraph);
			}
		}
		//保存JOB模板下所有TASK依赖关系
		Map<String, List<String>> leansRelationShip = jobPanelInfo.getLeansRelationShip();
		if(null != leansRelationShip && leansRelationShip.size()>0) {
			for(Map.Entry<String, List<String>> fLeansRelationShip : leansRelationShip.entrySet()) {
				List<String> leanTaskIdList = fLeansRelationShip.getValue();
				for(String fLeanTaskId : leanTaskIdList) {
					TaskLean taskLean =  new TaskLean();
					taskLean.setJobId(jobPanelInfo.getJobId());
					taskLean.setTaskId(fLeansRelationShip.getKey());
					taskLean.setLeanTaskId(fLeanTaskId);
					taskLeanMapper.insertTaskLean(taskLean);
				}
			}
		}
	}
	
	@Transactional
	public void saveTask(Task task) throws Exception{
		String[] info = task.getTaskId().split("#");
		String product = info[0];
		task.setTaskId(info[1]);
		Task taskOri = getTask(task.getJobId(), task.getTaskId());
		boolean fetchPoint = false;
		if(null == taskOri) {//新增
			task.setdCreateTime(CalendarUtil.parseDate(task.getCreateTime(),"yyyy-MM-dd"));
			if(task.getPointX() == 0 && task.getPointY() == 0) {
				fetchPoint = true;
			}
		}else {//更新
			if(null == taskOri.getCreateTime() || taskOri.getCreateTime().trim().equals("")) {
				task.setdCreateTime(CalendarUtil.parseDate(task.getCreateTime(),"yyyy-MM-dd"));
			}else{
				task.setdCreateTime(CalendarUtil.parseDate(taskOri.getCreateTime(),"yyyy-MM-dd"));
			}
			//删除Task模板、维度、属性
			Map<String,Object> dTaskMap = new HashMap<String,Object>();
			dTaskMap.put("jobId",task.getJobId());
			dTaskMap.put("taskId",task.getTaskId());
			//删除TASK模板维度
			taskDimensionMapper.deleteTaskDimension(dTaskMap);
			//删除TASK模板属性
			dTaskMap.put("sourceId", task.getTaskId());
			parameterMapper.deleteParameter(dTaskMap);
			//删除TASK模板
			if(task.getPointX() == 0 && task.getPointY() == 0) {
				if(taskOri.getPointX() == 0 && taskOri.getPointY() == 0) {//旧task坐标
					fetchPoint = true;
				}else {
					task.setPointX(taskOri.getPointX());
					task.setPointY(taskOri.getPointY());
				}
			}
			taskMapper.deleteTask(dTaskMap);
		}
		//保存TASK模板维度
		TreeMap<String, Set<String>> dimensions = task.getDimensions();
		if(null != dimensions && dimensions.size()>0) {
			for(Map.Entry<String, Set<String>> fDimension : dimensions.entrySet()) {//key=维度名称     Set<String>指：实体名称（实体名称值格式：实体名称）或标签（标签值格式：TAG:v_5）列表
				Set<String> sets = fDimension.getValue();
				Iterator<String> iterators = sets.iterator();
				while(iterators.hasNext()) {
					String entityNameOrTags = iterators.next(); //实体名称值格式：实体名称；标签值格式：tags:v_5 
					TaskDimension taskDimension = new TaskDimension();
					taskDimension.setDmsnProduct(product);
					taskDimension.setJobId(task.getJobId());
					taskDimension.setTaskId(task.getTaskId());
					taskDimension.setDmsnName(fDimension.getKey());
					taskDimension.setEntityName(entityNameOrTags);
					taskDimensionMapper.insertTaskDimension(taskDimension);
				}
			}
		}
		//保存TASK模板属性
		Map<String, String> parameters = task.getParameters();
		if(null != parameters && parameters.size()>0) {
			for(Map.Entry<String, String> fParameter : parameters.entrySet()) {
				Parameter parameter = new Parameter();
				parameter.setJobId(task.getJobId());
				parameter.setSourceId(task.getTaskId());
				parameter.setName(fParameter.getKey());
				parameter.setValue(fParameter.getValue());
				parameter.setdRequired(task.getParamterIsMust().get(fParameter.getKey()));
				parameterMapper.insertParameter(parameter);
			}
		}
		//保存TASK模板
		if(fetchPoint) {
			// 生成TASK坐标
			task.setPointX(1);
			task.setPointY(1);
			Map<String, Object> map = new HashMap<>();
			map.put("jobId", task.getJobId());
			int count = taskMapper.getCountByCondition(map);
			if (count > 1) {
				TaskGraph maxCorrdinateTaskGraph = taskMapper.getMaxCorrdinate(map);
				if (maxCorrdinateTaskGraph != null) task.setPointX(maxCorrdinateTaskGraph.getPointX() + 300);
			}
		}
		taskMapper.insertTask(task);
//		// 生成TASK坐标
//		Map<String, Object> map = new HashMap<>();
//		map.put("jobId", task.getJobId());
//		int count = taskMapper.getCountByCondition(map);
//		TaskGraph taskGraph = new TaskGraph();
//		taskGraph.setJobId(task.getJobId());
//		taskGraph.setTaskId(task.getTaskId());
//		taskGraph.setPointX(1);
//		taskGraph.setPointY(1);
//		if (count > 1) {
//			TaskGraph maxCorrdinateTaskGraph = taskMapper.getMaxCorrdinate(map);
//			if (maxCorrdinateTaskGraph != null) taskGraph.setPointX(maxCorrdinateTaskGraph.getPointX() + 300);
//		}
//		taskMapper.insertTaskGraph(taskGraph);
	}
	
	//删除Task模板所有相关表数据
	@Transactional
	public void deleteTask(String jobId,String taskIdArr[]) throws Exception{
		for(int i=0;i<taskIdArr.length;i++) {
			Map<String,Object> dTaskMap = new HashMap<String,Object>();
			dTaskMap.put("jobId",jobId);
			dTaskMap.put("taskId",taskIdArr[i]);
			//删除TASK模板依赖关系
			taskLeanMapper.deleteTaskLean(dTaskMap);
			//删除TASK模板维度
			taskDimensionMapper.deleteTaskDimension(dTaskMap);
			//删除TASK模板属性
			dTaskMap.put("sourceId", taskIdArr[i]);
			parameterMapper.deleteParameter(dTaskMap);
			//删除TASK模板
			taskMapper.deleteTask(dTaskMap);
		}
	}
	
	public JobPanelInfo graphJob(String product,String jobId) throws Exception{
		
		DirectedGraph dg = new DirectedGraph();
		
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		List<TaskGraph> taskGraphList = taskMapper.queryTaskGraph(condition);
		for (TaskGraph task: taskGraphList) {
			dg.getDirectedGraph().put(task.getTaskId(), new Vertex(task.getTaskId()));
		}
		condition.put("leanJobIdIsNull", "leanJobIdIsNull");
		List<TaskLean> taskLeanList = taskLeanMapper.queryTaskLeanPage(condition);
		if(null != taskLeanList && taskLeanList.size() > 0) {
			for (TaskLean tl : taskLeanList) {
				dg.getDirectedGraph().get(tl.getLeanTaskId()).getInDegree().incrementAndGet();
				dg.getDirectedGraph().get(tl.getLeanTaskId()).getEdges().add(new Edge(dg.getDirectedGraph().get(tl.getTaskId())));
			}
		}
		return null;
	}
	
	public JobPanelInfo getJobPanelInfo(String product,String jobId) throws Exception{
		JobPanelInfo jobPanelInfo = new JobPanelInfo();
		jobPanelInfo.setJobId(jobId);
		jobPanelInfo.setProduct(product);
		Map<String,List<String>> leansRelationShip = new HashMap<String,List<String>>(); //存job下所有task依赖   key=taskId,value=List<leanTaskId>
		Map<String,Map<String,Integer>> location = new HashMap<String,Map<String,Integer>>(); //存TASK模板位置
		List<Map<String, Object>> edges = new ArrayList<Map<String, Object>>();
		//TASK模板位置表有值说明TASK已经存在了
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		List<TaskGraph> taskGraphList = taskMapper.queryTaskGraph(condition);
		List<Task> tasks = null;
		if(null != taskGraphList && taskGraphList.size()>0) {//JOB模板下存在TASK模板
			for(TaskGraph fTaskGraph : taskGraphList) {
				Map<String,Integer> xyMap = new HashMap<String,Integer>();
				xyMap.put("pointX", fTaskGraph.getPointX());
				xyMap.put("pointY", fTaskGraph.getPointY());
				location.put(fTaskGraph.getTaskId(), xyMap);
			}
			//取job下所有task依赖关系
			condition.put("leanJobIdIsNull", "leanJobIdIsNull");
			List<TaskLean> taskLeanList = taskLeanMapper.queryTaskLeanPage(condition);
			if(null != taskLeanList && taskLeanList.size()>0) {
				for(TaskLean fTaskLean : taskLeanList) {
					List<String> leanTaskIdList = null;
					if(null == leansRelationShip.get(fTaskLean.getTaskId())) {
						leanTaskIdList = new ArrayList<String>();
					}else {
						leanTaskIdList = leansRelationShip.get(fTaskLean.getTaskId());
					}
					leanTaskIdList.add(fTaskLean.getLeanTaskId());
					leansRelationShip.put(fTaskLean.getTaskId(), leanTaskIdList);
				}
			}
			//lineType(1:task依赖 实线; 2:特殊依赖关系 点虚线)
			Map<String,Map<String,Object>> lineTypeMap = new HashMap<String,Map<String,Object>>(); //判断lineType值
			//取job下所有task关系  lineType=1
			List<TaskEdges> taskEdgesList = taskLeanMapper.queryTaskEdges(condition);
			if(null != taskEdgesList && taskEdgesList.size()>0) {
				for(TaskEdges fTaskEdges : taskEdgesList) {
					Map<String,Object> tMap = new HashMap<String,Object>();
					tMap.put("source", fTaskEdges.getSource());
					tMap.put("target", fTaskEdges.getTarget());
					tMap.put("data", JSONObject.parseObject(fTaskEdges.getData(), Map.class));
					//edges.add(tMap);
					lineTypeMap.put(fTaskEdges.getSource()+"###"+fTaskEdges.getTarget(), tMap);
				}
			}
			//取特殊依赖关系  lineType=2
			taskEdgesList = taskSpecialLeanMapper.queryTaskSpecialEdges(condition);
			if(null != taskEdgesList && taskEdgesList.size()>0) {
				for(TaskEdges fTaskEdges : taskEdgesList) {
					Map<String,Object> tMap = null;
					if(null == lineTypeMap.get(fTaskEdges.getSource()+"###"+fTaskEdges.getTarget())) {
					    tMap = new HashMap<String,Object>();
						tMap.put("source", fTaskEdges.getSource());
						tMap.put("target", fTaskEdges.getTarget());
						tMap.put("data", JSONObject.parseObject(fTaskEdges.getData(), Map.class));
					}else{//task依赖和特殊依赖关系
						//tMap = lineTypeMap.get(fTaskEdges.getSource()+"###"+fTaskEdges.getTarget());
						//tMap.put("data", JSONObject.parseObject("{'type':'s:e','lineType':'3'}", Map.class));
						throw new Exception("两个任务项("+fTaskEdges.getSource()+"和"+fTaskEdges.getTarget()+")之间,不能同时存在完整依赖和特殊依赖");
					}
					lineTypeMap.put(fTaskEdges.getSource()+"###"+fTaskEdges.getTarget(), tMap);
				}
			}
			for(Map.Entry<String, Map<String,Object>> fMap : lineTypeMap.entrySet()) {
				edges.add(fMap.getValue());
			}
			//取所有jobId取task列表
			tasks = taskMapper.queryTaskPage(condition);
		}else {
			tasks = new ArrayList<Task>();
		}
		jobPanelInfo.setLocation(location);
		jobPanelInfo.setLeansRelationShip(leansRelationShip);
		jobPanelInfo.setEdges(edges);
		jobPanelInfo.setTasks(tasks);
		return jobPanelInfo;
	}

	@Transactional
	public void saveTaskLean(String jobId, String taskId, String leanJobId, String leanTaskIds) throws Exception {
		// ******************************* 保存依赖关系 *******************************
		//完整依赖、特殊依赖 二选一
		Map<String,Object> queryMap = new HashMap<>();
		queryMap.put("jobId", jobId);
		queryMap.put("taskId", taskId);
		queryMap.put("leanJobIdIsNull", "leanJobIdIsNull");
		List<TaskLean> taskLeanList = taskSpecialLeanMapper.queryTaskLean(queryMap);
		Map<String,Object> leanTaskIdMap = new HashMap<>();
		if(null != taskLeanList && taskLeanList.size() > 0) {
			for(TaskLean fTaskLean : taskLeanList) {
				leanTaskIdMap.put(fTaskLean.getLeanTaskId(), taskId);
			}
		}
		taskLeanList = new ArrayList<TaskLean>();
		// 添加task新的依赖关系
		String[] leanTaskIdArr = leanTaskIds.split(",");
		TaskLean tTaskLean = null;
		for (String fLeanTaskId : leanTaskIdArr) {
			if(null != leanTaskIdMap.get(fLeanTaskId)) {
				throw new Exception(taskId+"特殊依赖"+fLeanTaskId+",不可以再设置完整依赖.");
			}
			tTaskLean = new TaskLean();
			tTaskLean.setJobId(jobId);
			tTaskLean.setTaskId(taskId);
			//tTaskLean.setLeanJobId(leanJobId);
			tTaskLean.setLeanTaskId(fLeanTaskId);
			taskLeanMapper.insertTaskLean(tTaskLean);
			taskLeanList.add(tTaskLean);
		}
		
		// 判断依赖关系是否闭环
		StringBuffer loopmsg = new StringBuffer();
		// 查询出这个job下的所有task
		Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", jobId);
		List<Task> taskList = getTaskList(condition);
		// 查询出这个job模板下所有task被依赖关系
		List<TaskLean> taskBeLeanList = taskLeanMapper.queryTaskBeLean(condition);//取完整依赖
		List<TaskLean> taskBeLeanList2 = this.taskSpecialLeanMapper.queryTaskLean(condition);//取特殊依赖
		if(null == taskBeLeanList || taskBeLeanList.size() < 1) {
			taskBeLeanList = taskBeLeanList2;
		}else {
			if(null != taskBeLeanList2 && taskBeLeanList2.size() > 0) {
				taskBeLeanList.addAll(taskBeLeanList2);
			}
		}
		//boolean cloop = isContainLoop(jobId, loopmsg);
		boolean cloop = JobVerifyUtil.isContainLoop(taskList, taskBeLeanList, loopmsg);
		if(cloop) {
			// 删除task新的依赖关系
			for(TaskLean fTaskLean : taskLeanList) {
				Map<String,Object> delMap = new HashMap<String,Object>();
				delMap.put("jobId", fTaskLean.getJobId());
				delMap.put("taskId", fTaskLean.getTaskId());
				delMap.put("leanTaskId", fTaskLean.getLeanTaskId());
				taskLeanMapper.deleteTaskLean2(delMap);
			}
			throw new Exception("job tasks exists contain loop. " + loopmsg.toString());
		}
		
		// ******************************* 生成位置信息 *******************************
		// 查询出这个job下的所有task
		/*Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", jobId);
		List<Task> taskList = getTaskList(condition);*/
		
		Map<String,Task> tasksMap = new HashMap<>();
		for (Task fTask : taskList) {
			tasksMap.put(fTask.getTaskId(), fTask);
		}
		// 查询出这个job下所有task依赖关系
		taskLeanList = taskLeanMapper.queryTaskBeLean(condition);
		// 添加依赖关系到task中
		if (taskLeanList != null && taskLeanList.size()>0) {
			List<String> leans = null;
			List<String> beLeans = null;
			Task tTask = null;
			Task tLeanTask = null;
			for (TaskLean fTaskLean : taskLeanList) {
				tLeanTask = tasksMap.get(fTaskLean.getLeanTaskId());
				beLeans = tLeanTask.getBeleans()==null ? new ArrayList<String>() : tLeanTask.getBeleans();
				beLeans.add(fTaskLean.getTaskId());
				tLeanTask.setBeleans(beLeans);
				tasksMap.put(tLeanTask.getTaskId(), tLeanTask);
				
				tTask = tasksMap.get(fTaskLean.getTaskId());
				leans = tTask.getLeans()==null ? new ArrayList<String>() : tTask.getLeans();
				leans.add(fTaskLean.getLeanTaskId());
				tTask.setLeans(leans);
				tasksMap.put(tTask.getTaskId(), tTask);
			}
			taskList = new ArrayList<>(tasksMap.values());
		}
		
		Map<Integer,List<String>> taskNode = new HashMap<>();
		JobTemplateUtils.taskPanelFormat(taskList, taskNode);
		Map<String,Map<String,Integer>> location = new HashMap<>();
		Set<Integer> nodes = taskNode.keySet();
		if(nodes != null){
			Iterator<Integer> nodeIterator = nodes.iterator();
			while(nodeIterator.hasNext()){
				Integer yKey = nodeIterator.next();
				Integer y = 33 + (yKey) * 160;
				List<String> nodesTask = taskNode.get(yKey);
				if(nodesTask != null){
					Iterator<String> nodeTaskIterator = nodesTask.iterator();
					int count = 0;
					while(nodeTaskIterator.hasNext()){
						Integer x = 17 + 300 * count;
						count ++;
						Map<String,Integer> position = new HashMap<>();
						position.put("pointX", x);
						position.put("pointY", y);
						location.put(nodeTaskIterator.next(), position);
					}
				}
			}
		}
		
		if(null != location && location.size()>0) {
			// 删除旧的位置信息
			taskMapper.deleteTaskGraph(condition);
			// 新增新的位置信息
			for(Map.Entry<String, Map<String, Integer>> fLocation : location.entrySet()) {
				Map<String,Integer> xyMap = fLocation.getValue();
				TaskGraph taskGraph = new TaskGraph();
				taskGraph.setJobId(jobId);
				taskGraph.setTaskId(fLocation.getKey());
				taskGraph.setPointX(xyMap.get("pointX"));
				taskGraph.setPointY(xyMap.get("pointY"));
				taskMapper.insertTaskGraph(taskGraph);
			}
		}
	}
	
	public PageInfo<TaskLean> queryTaskLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskLeanMapper.queryTaskLeanPage(condition);
			}
		});
	}
	
	@Transactional
	@Override
	public void deleteTaskLean(String jobId, String taskId, String leanTaskIdArr[])
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("jobId", jobId);
		map.put("taskId", taskId);
		map.put("leanTaskIdArr", leanTaskIdArr);
		// 删除task依赖
		taskLeanMapper.deleteTaskLean2(map);
	}
	
	public String isContainLoop(JobPanelInfo jobPanelInfo) throws Exception{
		// 判断依赖关系是否闭环
		StringBuffer loopmsg = new StringBuffer();
		// 查询出这个job下的所有task
		Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", jobPanelInfo.getJobId());
		List<Task> taskList = taskMapper.queryTaskPage(condition);
		// 查询出这个job模板下所有task被依赖关系
		List<TaskLean> taskLeanList = new ArrayList<TaskLean>();
		//保存JOB模板下所有TASK依赖关系
		Map<String, List<String>> leansRelationShip = jobPanelInfo.getLeansRelationShip();
		if(null != leansRelationShip && leansRelationShip.size()>0) {
			for(Map.Entry<String, List<String>> fLeansRelationShip : leansRelationShip.entrySet()) {
				List<String> leanTaskIdList = fLeansRelationShip.getValue();
				for(String fLeanTaskId : leanTaskIdList) {
					TaskLean taskLean =  new TaskLean();
					taskLean.setJobId(jobPanelInfo.getJobId());
					taskLean.setTaskId(fLeansRelationShip.getKey());
					taskLean.setLeanTaskId(fLeanTaskId);
					taskLeanList.add(taskLean);
				}
			}
		}
		boolean cloop = JobVerifyUtil.isContainLoop(taskList, taskLeanList, loopmsg);
		if(cloop) {
			// 删除task新的依赖关系
			//throw new Exception("job tasks exists contain loop. " + loopmsg.toString());
			return loopmsg.toString();
		}
		return null;
	}
	
	public PageInfo<TaskLean> taskLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskLeanMapper.taskLeanPage(condition);
			}
		});
	}
	
	public List<String> getDmsnNameList(String jobId,String taskId){
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("taskId", taskId);
		List<String> dmsnNameList = this.taskDimensionMapper.getTaskDmsnNameList(condition);
		if(null != dmsnNameList && dmsnNameList.size() > 0) {
			return dmsnNameList;
		}
		return this.jobDimensionMapper.getJobDmsnNameList(condition);
	}
	
	public PageInfo<TaskLean> taskBeleanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskLeanMapper.taskBeleanPage(condition);
			}
		});
	}
	
	public PageInfo<TaskSpecialLean> taskSpecialLeanPage(Map<String, Object> condition,int pageNo, int pageSize) throws Exception{
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				taskSpecialLeanMapper.getTaskSpecialLean(condition);
			}
		});
	}
	
	public List<String> getEntityNameList(String jobId,String taskId,String entityName){
		Map<String,Object> condition = new HashMap<String,Object>();
		condition.put("jobId", jobId);
		condition.put("taskId", taskId);
		if(null != entityName && !entityName.equals("")) {
			condition.put("lEntityName", entityName);
		}
		List<String> entityNameList = this.taskDimensionMapper.getTaskEntityNameList(condition);
		if(null != entityNameList && entityNameList.size() > 0) {
			return entityNameList;
		}
		return this.jobDimensionMapper.getJobEntityNameList(condition);
	}
	
	@Transactional
	public void deleteTaskLean(String jobId, String taskId, String leanTaskIdArr[], String specialLeanTaskIdArr[]) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("jobId", jobId);
		map.put("taskId", taskId);
		if(null != leanTaskIdArr) {// 删除task依赖
			map.put("leanTaskIdArr", leanTaskIdArr);
			taskLeanMapper.deleteTaskLean2(map);
		}
		if(null != specialLeanTaskIdArr) {// 删除task特殊依赖
			map.put("leanTaskIdArr", specialLeanTaskIdArr);
			taskSpecialLeanMapper.deleteTaskSpecialLean2(map);
		}
	}
	
	@Transactional
	public void saveTaskSpecialLean(List<TaskSpecialLean> taskSpecialLeanList) throws Exception{
		String jobId = taskSpecialLeanList.get(0).getJobId();
		// 判断依赖关系是否闭环
		StringBuffer loopmsg = new StringBuffer();
		// 查询出这个job下的所有task
		Map<String, Object> condition = new HashMap<>();
		condition.put("jobId", jobId);
		List<Task> taskList = getTaskList(condition);
		// 查询出这个job模板下所有task被依赖关系
		List<TaskLean> taskBeLeanList = taskLeanMapper.queryTaskBeLean(condition);//取完整依赖
		List<TaskLean> taskBeLeanList2 = this.taskSpecialLeanMapper.queryTaskLean(condition);//取特殊依赖
		if(null == taskBeLeanList || taskBeLeanList.size() < 1) {
			if(null != taskBeLeanList2 && taskBeLeanList2.size() > 0) {
				taskBeLeanList = taskBeLeanList2;
			}else {
				taskBeLeanList = new ArrayList<>();
			}
		}else {
			if(null != taskBeLeanList2 && taskBeLeanList2.size() > 0) {
				taskBeLeanList.addAll(taskBeLeanList2);
			}
		}
		Map<String,Object> filterMap = new HashMap<>();
		for(TaskSpecialLean fTaskSpecialLean : taskSpecialLeanList) {
			String tKey = fTaskSpecialLean.getJobId()+"###"+fTaskSpecialLean.getTaskId()+"###"+fTaskSpecialLean.getBeTaskId();
			if(null == filterMap.get(tKey)) {
				filterMap.put(tKey, tKey);
				TaskLean taskLean = new TaskLean();
				taskLean.setJobId(fTaskSpecialLean.getJobId());
				taskLean.setTaskId(fTaskSpecialLean.getTaskId());
				taskLean.setLeanTaskId(fTaskSpecialLean.getBeTaskId());
				taskBeLeanList.add(taskLean);
			}
		}
		//boolean cloop = isContainLoop(jobId, loopmsg);
		boolean cloop = JobVerifyUtil.isContainLoop(taskList, taskBeLeanList, loopmsg);
		if(cloop) {
			throw new Exception("job tasks exists contain loop. " + loopmsg.toString());
		}
		String taskId = taskSpecialLeanList.get(0).getTaskId();
		Map<String,Object> queryMap = new HashMap<>();
		queryMap.put("jobId", jobId);
		queryMap.put("taskId", taskId);
		queryMap.put("leanJobIdIsNull", "leanJobIdIsNull");
		List<TaskLean> taskLeanList = taskLeanMapper.queryTaskLeanPage(queryMap);
		Map<String,Object> leanTaskIdMap = new HashMap<>();
		if(null != taskLeanList && taskLeanList.size() > 0) {
			for(TaskLean fTaskLean : taskLeanList) {
				leanTaskIdMap.put(fTaskLean.getLeanTaskId(), taskId);
			}
		}
		for(TaskSpecialLean fTaskSpecialLean : taskSpecialLeanList) {
			if(null != leanTaskIdMap.get(fTaskSpecialLean.getBeTaskId())) {
				throw new Exception(taskId+"完整依赖"+fTaskSpecialLean.getBeTaskId()+",不可以再设置特殊依赖.");
			}
			queryMap = new HashMap<>();
			queryMap.put("jobId", fTaskSpecialLean.getJobId());
			queryMap.put("taskDimEntities",fTaskSpecialLean.getExp());
			queryMap.put("leanJobIdIsNull", "leanJobIdIsNull");
			queryMap.put("leanTaskId", fTaskSpecialLean.getBeTaskId());
			queryMap.put("leanTaskDimEntities", fTaskSpecialLean.getBeExp());
			List<TaskSpecialLean> tTaskSpecialLeanList = taskSpecialLeanMapper.getTaskSpecialLean(queryMap);
			if(null != tTaskSpecialLeanList && tTaskSpecialLeanList.size() > 0) {
				continue;
			}
			taskSpecialLeanMapper.insertTaskSpecialLean(fTaskSpecialLean);
		}
	}
	
}
