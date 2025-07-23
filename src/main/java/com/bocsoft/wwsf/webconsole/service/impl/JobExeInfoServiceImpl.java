package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.CalendarUtil;
import com.bocsoft.wwsf.webconsole.UserUtils;
import com.bocsoft.wwsf.webconsole.mapper.JobExeInfoMapper;
import com.bocsoft.wwsf.webconsole.mapper.TaskMapper;
import com.bocsoft.wwsf.webconsole.model.Cron;
import com.bocsoft.wwsf.webconsole.model.JobExeInfo;
import com.bocsoft.wwsf.webconsole.model.SysUser;
import com.bocsoft.wwsf.webconsole.model.Task;
import com.bocsoft.wwsf.webconsole.service.JobExeInfoService;
import com.bocsoft.wwsf.webconsole.service.WwseService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("jobExeInfoService")
public class JobExeInfoServiceImpl implements JobExeInfoService {
	
	@Autowired
	private JobExeInfoMapper jobExeInfoMapper;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private WwseService wwseService;

	@Override
	public List<JobExeInfo> queryJobExeInfo(Map<String, Object> condition) throws Exception {
		return jobExeInfoMapper.queryJobExeInfo(condition);
	}
	
	@Override
	public PageInfo<JobExeInfo> queryJobExeInfoPage(Map<String, Object> condition, int pageNo, int pageSize) throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				jobExeInfoMapper.queryJobExeInfo(condition);
			}
		});
	}

	@Override
	public List<Task> queryJobExeDetail(Map<String, Object> condition) throws Exception {
		return jobExeInfoMapper.queryJobExeDetail(condition);
	}

	@Override
	public void insertJobExeDetail(Task task) throws Exception {
		jobExeInfoMapper.insertJobExeDetail(task);
	}

	@Override
	public PageInfo<Task> getJobExeInfoSelect(Integer pageNumber, Integer pageSize, String infoId, String jobId, String filterTaskId) throws Exception {
		return PageHelper.startPage(pageNumber, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				try {
					getJobExeInfoSelect(infoId, filterTaskId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public List<Task> getJobExeInfoSelect(String infoId, String filterTaskId) throws Exception {
		List<Task> list = new ArrayList<>();
		if (infoId==null || "".equals(infoId)) {
			return list;
		}
		SysUser user = UserUtils.getCurLoginSysUser();
		Map<String, Object> map = new HashMap<>();
		map.put("tempVersion", "tempVersion");
		map.put("creator", user.getLoginName());
		if (filterTaskId!=null && !"".equals(filterTaskId)) {
			//map.put("filterTaskId", filterTaskId); //过滤taskId
			map.put("lTaskId",filterTaskId); //根据taskId模糊查询
		}
		list = queryJobExeDetail(map);
		return list;
	}

	@Override
	public PageInfo<Task> getJobExeInfoUnselect(Integer pageNumber, Integer pageSize, String infoId, String jobId, String filterTaskId) throws Exception {
		return PageHelper.startPage(pageNumber, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				try {
					getJobExeInfoUnselect(infoId, jobId, filterTaskId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public List<Task> getJobExeInfoUnselect(String infoId, String jobId, String filterTaskId) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("jobId", jobId);
		map.put("filterSelect", infoId);
		if (filterTaskId!=null && !"".equals(filterTaskId)) {
			//map.put("filterTaskId", filterTaskId);  //过滤taskId
			map.put("lTaskId",filterTaskId); //根据taskId模糊查询
		}
		List<Task> list = taskMapper.queryTaskPage(map);
		return list;
	}

	@Override
	public void addTempJobExeInfo(String infoId, String jobId) throws Exception {
		SysUser user = UserUtils.getCurLoginSysUser();
		// 删除旧的临时数据
		Map<String, Object> map = new HashMap<>();
		map.put("status", 0);
		map.put("creator", user.getLoginName());
		List<JobExeInfo> list = queryJobExeInfo(map);
		if (list!=null && list.size()==1) {
			JobExeInfo jei = list.get(0);
			map = new HashMap<>();
			map.put("infoId", jei.getInfoId());
			jobExeInfoMapper.deleteJobExeDetail(map);
			jobExeInfoMapper.deleteJobExeInfo(map);
		}
		// 新增临时数据
		if (infoId!=null && !"".equals(infoId)) {
			Map<String, Object> condition = new HashMap<>();
			condition.put("infoId", infoId);
			List<JobExeInfo> jeiList = queryJobExeInfo(condition);
			if (jeiList!=null && jeiList.size()==1) {
				// 临时job模板历史记录
				JobExeInfo jh = jeiList.get(0);
				JobExeInfo tJh = new JobExeInfo();
				tJh.setProduct(jh.getProduct());
				tJh.setInfoDesc("");
				tJh.setJobId(jh.getJobId());
				tJh.setStatus(0);
				tJh.setTop(0);
				tJh.setInfoId(String.valueOf(System.currentTimeMillis()));
				tJh.setCreator(user.getLoginName());
				tJh.setInfoParameter(jh.getInfoParameter());
				jobExeInfoMapper.insertJobExeInfo(tJh);
				// 临时已选择task记录
				List<Task> taskList = queryJobExeDetail(condition);
				for (Task task : taskList) {
					Task tTask = new Task();
					tTask.setJobId(tJh.getInfoId());
					tTask.setTaskId(task.getTaskId());
					insertJobExeDetail(tTask);
				}
			}
		}
	}

	@Override
	public String addJobExeDetail(String product, String jobId, String taskIds) throws Exception {
		SysUser user = UserUtils.getCurLoginSysUser();
		String infoId = "";
		Map<String, Object> map = new HashMap<>();
		map.put("status", 0);
		map.put("creator", user.getLoginName());
		JobExeInfo jh = null;
		List<JobExeInfo> tjhList = queryJobExeInfo(map);
		if (tjhList!=null && tjhList.size()==1) {
			jh = tjhList.get(0);
			infoId = jh.getInfoId();
		}else {
			infoId = String.valueOf(System.currentTimeMillis());
			jh = new JobExeInfo();
			jh.setJobId(jobId);
			jh.setProduct(product);
			jh.setStatus(0);
			jh.setTop(0);
			jh.setInfoId(infoId);
			jh.setCreator(user.getLoginName());
			jobExeInfoMapper.insertJobExeInfo(jh);
		}
		String[] taskIdArr = taskIds.split(",");
		for (int i = 0; i < taskIdArr.length; i++) {
			Task t = new Task();
			t.setJobId(infoId);
			t.setTaskId(taskIdArr[i]);
			jobExeInfoMapper.insertJobExeDetail(t);
		}
		return infoId;
	}

	@Override
	public String removeJobExeDetail(String taskIds) throws Exception {
		SysUser user = UserUtils.getCurLoginSysUser();
		String infoId = "";
		Map<String, Object> map = new HashMap<>();
		map.put("status", 0);
		map.put("creator", user.getLoginName());
		List<JobExeInfo> tjhList = queryJobExeInfo(map);
		if (tjhList!=null && tjhList.size()==1) {
			JobExeInfo jh = tjhList.get(0);
			infoId = jh.getInfoId();
			map = new HashMap<>();
			map.put("infoId", infoId);
			String[] taskIdArr = taskIds.split(",");
			for (int i = 0; i < taskIdArr.length; i++) {
				map.put("taskId", taskIdArr[i]);
				jobExeInfoMapper.deleteJobExeDetail(map);
			}
		}
		return infoId;
	}

	@Override
	@Transactional
	public void topRecord(String infoId, boolean flag) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("infoId", infoId);
		List<JobExeInfo> jhList = queryJobExeInfo(map);
		if (jhList!=null && jhList.size()==1) {
			JobExeInfo jh = jhList.get(0);
			jh.setTop(flag?1:0);
			jobExeInfoMapper.deleteJobExeInfo(map);
			jobExeInfoMapper.insertJobExeInfo(jh);
		}
	}

	@Override
	@Transactional
	public void editJobExeInfo(String infoId, String desc) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("infoId", infoId);
		List<JobExeInfo> jhList = queryJobExeInfo(map);
		if (jhList!=null && jhList.size()==1) {
			JobExeInfo jh = jhList.get(0);
			jh.setInfoDesc(desc);
			jobExeInfoMapper.deleteJobExeInfo(map);
			jobExeInfoMapper.insertJobExeInfo(jh);
		}
	}

	@Override
	public Long insertJobExeInfo(String infoParameter) throws Exception {
		SysUser user = UserUtils.getCurLoginSysUser();
		Map<String, Object> map = new HashMap<>();
		map.put("status", 0);
		map.put("creator", user.getLoginName());
		List<JobExeInfo> list = queryJobExeInfo(map);
		if (list!=null && list.size()==1) {
			JobExeInfo jei = list.get(0);
			/*jei.setExecuteTime(CalendarUtil.fmtDate(new Date(), "yyyyMMddHHmmss"));
			jei.setInfoDesc("");
			jei.setStatus(1);
			if(!"{}".equals(infoParameter)) {
				jei.setInfoParameter(infoParameter);
			}else {
				jei.setInfoParameter("");
			}
			jobExeInfoMapper.deleteJobExeInfo(map);
			jobExeInfoMapper.insertJobExeInfo(jei);*/
			
			map.put("status", 1);
			map.put("executeTime", CalendarUtil.fmtDate(new Date(), "yyyyMMddHHmmss"));
			if(!"{}".equals(infoParameter)) {
				map.put("infoParameter", infoParameter);
			}
			map.put("infoId", jei.getInfoId());
			jobExeInfoMapper.updateJobExeInfo(map);
			return wwseService.jobInitRegional(jei.getInfoId());
		}
		return 0L;
	}
}
