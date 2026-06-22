package com.bocsoft.wwsf.webconsole.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bocsoft.wwsf.webconsole.mapper.CalendarMapper;
import com.bocsoft.wwsf.webconsole.model.Calendar;
import com.bocsoft.wwsf.webconsole.service.CalendarService;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service("calendarService")
public class CalendarServiceImpl implements CalendarService {
	
	@Autowired
	private CalendarMapper calendarMapper;

	@Override
	public List<Calendar> queryCalendarList(Map<String, Object> condition) throws Exception {
		return calendarMapper.queryCalendarList(condition);
	}

	@Override
	public PageInfo<Calendar> queryCalendarList(Map<String, Object> condition, int pageNo, int pageSize)
			throws Exception {
		return PageHelper.startPage(pageNo, pageSize).doSelectPageInfo(new ISelect() {
			public void doSelect() {				
				calendarMapper.queryCalendarList(condition);
			}
		});
	}

	@Transactional
	@Override
	public int insertCalendar(Calendar calendar) throws Exception {
		if(queryCalendar(calendar) != null) {
			throw new Exception(String.format("[%s][%s]已存在!", calendar.getProduct(), calendar.getCalName()));
		}
		return calendarMapper.insertCalendar(calendar);
	}

	@Transactional
	@Override
	public int deleteCalendar(Map<String, Object> condition) throws Exception {
		String[] proArr = ((String)condition.get("proArr")).split(",");
		String[] calNameArr = ((String)condition.get("calNameArr")).split(",");
		if(proArr.length != calNameArr.length) {
			throw new Exception("产品,假日编排名称 不一致");
		}
		Map<String, Object> map = new HashMap<>();
		int result = 1;
		for(int i=0, len=proArr.length; i<len; i++) {
			map.put("product", proArr[i]);
			map.put("calName", calNameArr[i]);
			result = calendarMapper.deleteCalendar(map);
		}
		return result;
	}

	@Override
	public Calendar queryCalendar(Calendar calendar) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("product", calendar.getProduct());
		map.put("calName", calendar.getCalName());
		List<Calendar> calendarList = queryCalendarList(map);
		if (calendarList==null || calendarList.isEmpty()) {
			return null;
		}
		return calendarList.get(0);
	}

	@Transactional
	@Override
	public int updateCalendar(Calendar calendar) throws Exception {
		if(queryCalendar(calendar) == null) {
			throw new Exception(String.format("[%s][%s]不存在!", calendar.getProduct(), calendar.getCalName()));
		}
		Map<String, Object> condition = new HashMap<>();
		condition.put("product", calendar.getProduct());
		condition.put("calName", calendar.getCalName());
		condition.put("calDescription", calendar.getCalDescription());
		condition.put("includeValue", calendar.getIncludeValue());
		condition.put("exclusionValue", calendar.getExclusionValue());
		return calendarMapper.updateCalendar(condition);
	}

}
