package com.bocsoft.wwsf.webconsole.mapper;

import java.util.List;
import java.util.Map;

import com.bocsoft.wwsf.webconsole.model.Calendar;

public interface CalendarMapper {
	public List<Calendar> queryCalendarList(Map<String, Object> condition);
	
	public int insertCalendar(Calendar calendar);
	
	public int deleteCalendar(Map<String, Object> condition);
	
	public int updateCalendar(Map<String, Object> condition);

	public void updateCalend(Calendar calendar);
}
