package com.bocsoft.wwsf.webconsole.service;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Calendar;
import com.github.pagehelper.PageInfo;

public interface CalendarService {
	public List<Calendar> queryCalendarList(Map<String, Object> condition) throws Exception;
	
	public PageInfo<Calendar> queryCalendarList(Map<String, Object> condition, int pageNo, int pageSize) throws Exception;

	public int insertCalendar(Calendar calendar) throws Exception;
	
	public int deleteCalendar(Map<String, Object> condition) throws Exception;
	
	public Calendar queryCalendar(Calendar calendar) throws Exception;
	
	public int updateCalendar(Calendar calendar) throws Exception;
}
