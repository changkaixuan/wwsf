package com.bocsoft.wwsf.webconsole;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.CronExpression;

public class CalendarUtil {

	private final long period;
	private final AtomicLong now;
	public static final String DEFAULT_DATE_PATTERN = "yyyyMMdd HH:mm:ss";
	public static final String DEFAULT_DAY_PATTERN = "yyyyMMdd";

	private static class InstanceHolder {
		public static final CalendarUtil INSTANCE = new CalendarUtil(1);
	}

	private static CalendarUtil instance() {
		return InstanceHolder.INSTANCE;
	}

	private CalendarUtil(long period) {
		this.period = period;
		now = new AtomicLong(System.currentTimeMillis());
		scheduleClockUpdating();
	}
	
	public static String formatSpent(long ms) {
		long days = ms/(1000*60*60*24);
		long hours = (ms%(1000*60*60*24))/(1000*60*60);
		long min = (ms%(1000*60*60))/(1000*60);
		long scd = (ms%(1000*60))/(1000);
		StringBuffer buf = new StringBuffer();
		if (days > 0) {
			buf.append(days).append(" ").append(getRepairZeroLeft(hours,2)).append(":").append(getRepairZeroLeft(min,2)).append(":").append(getRepairZeroLeft(scd,2));
			return buf.toString();
		}
		buf.append(getRepairZeroLeft(hours,2)).append(":").append(getRepairZeroLeft(min,2)).append(":").append(getRepairZeroLeft(scd,2));
		return buf.toString();
	}
	
	private static String getRepairZeroLeft(long x, Integer pixelsLenth) {
		String xs = String.valueOf(x);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pixelsLenth-xs.length(); i++) {
			sb.append("0");
		}
		sb.append(xs);
		return sb.toString();

	}

	private void scheduleClockUpdating() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(
				new Runnable() {
					public void run() {
						now.set(System.currentTimeMillis());
					}
				}, period, period, TimeUnit.MILLISECONDS);
	}

	private long currentTimeMillis() {
		return now.get();
	}

	public static long now() {
		return instance().currentTimeMillis();
	}

	public static Date add(Date time, int field, int count) {
		return addForCalendar(time, field, count).getTime();
	}

	public static Calendar addForCalendar(Date time, int field, int count) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(time);
		ca.add(field, count);
		return ca;
	}

	public static boolean isAfterPoint(Date end, int addition, Date point) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(end);
		ca.add(Calendar.SECOND, addition);

		Calendar nowc = Calendar.getInstance();
		nowc.setTime(point);
		return ca.after(nowc);
	}

	public static boolean isAfterNow(Date end, int addition) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(end);
		ca.add(Calendar.SECOND, addition);
		Calendar nowc = Calendar.getInstance();
		nowc.setTime(new Date());
		return ca.after(nowc);
	}

	/**
	 * 要求传入的参数必须为正确的参数，完成计划任务执行判断
	 * 
	 * @param date
	 *            需比较的时间
	 * @param timeTab
	 *            格式时间表time_window minutes hours days_of_month months
	 *            days_of_week
	 * @return Map<String,Object> MINUS_TO_NEXT 离下个执行周期 MINUS_FORM_PREP 离上个执行周期
	 */
	public static Map<String, Object> timeCompCronTab(Date ckDate, String timeTab) throws Exception {

		Map<String, Object> results = new HashMap<String, Object>();
		results.put("CHECK_POINT", ckDate.getTime());
		// 需要校验时间分析： 时 分
		Calendar ca = Calendar.getInstance();
		ca.setTime(ckDate);
		int ck_hour = ca.get(Calendar.HOUR_OF_DAY);
		int ck_minutes = ca.get(Calendar.MINUTE);
		// 用于记录比较前后一天的计算结果
		int[] flags = new int[3];

		try {
			// 切割cron_tab字符串：XX 分 时 日 月 周
			String[] periodSplit = timeTab.trim().split(" ");
			String tab_minutes = periodSplit[1];
			String tab_hours = periodSplit[2];
			String tab_day_of_month = periodSplit[3];
			String tab_month = periodSplit[4];
			String tab_day_of_week = periodSplit[5];

			/* 确定当前日期的上一天，当前天，下一天是否在周期时间表中 */
			for (int j = -1; j < 2; j++) {
				Calendar Jdate = addForCalendar(ckDate, Calendar.DAY_OF_MONTH, j);
				int Jmonth = Jdate.get(Calendar.MONTH) + 1;// Calendar月份为0到11,需加1处理
				int Jday = Jdate.get(Calendar.DAY_OF_MONTH);
				int JweekDay = Jdate.get(Calendar.DAY_OF_WEEK);// Calendar周天为:
																// 1-周日; 2-周;一
																// 3-周二;...7-周六
				flags[j + 1] = 0;

				/* 比较月份,是否当前日期在计划执行月份 */
				if (!tab_month.equals("*")) {
					String[] cron_months = tab_month.split(",");
					int i = 0;
					for (i = 0; i < cron_months.length; i++) {
						if (isNullOrEmpty(cron_months[i]))
							continue;
						String[] rg = cron_months[i].split("-");
						int bg = Integer.parseInt(rg[0]), end = -1;
						short retM;
						if (rg.length > 1) {
							end = Integer.parseInt(rg[1]);
							if (end < bg) {
								retM = -1;
							} else {
								retM = 2;
							}
						} else {
							retM = 1;
						}
						/* 当计划字串格式不对9-8,或者月份在执行计划内时,跳出 */
						if ((retM < 0) || (retM == 1 && bg == Jmonth) || (retM > 1 && bg <= Jmonth && end >= Jmonth)) {
							break;
						}
					}
					// 月不满足,直接进入下一日期计算
					if (i == cron_months.length)
						continue;
				}

				/* 比较周天,是否当前日期在计划执行周天 */
				if (!tab_day_of_week.equals("*")) {
					String[] cron_week_day = tab_day_of_week.split(",");
					boolean flag = false;
					for (int i = 0; i < cron_week_day.length; i++) {
						// 为空时进入下个表达式判断
						if (isNullOrEmpty(cron_week_day[i]))
							continue;
						String[] rg = cron_week_day[i].split("-");
						int bg = Integer.parseInt(rg[0]), end = -1;
						short retM;
						if (rg.length > 1) {
							end = Integer.parseInt(rg[1]);
							if (end < bg) {
								retM = -1;
							} else {
								retM = 2;
							}
						} else {
							retM = 1;
						}
						// 当表达式错误，周天在计划周天内时，跳出
						if ((retM < 0) || (retM == 1 && bg == JweekDay)
								|| (retM > 1 && bg <= JweekDay && end >= JweekDay)) {
							flags[j + 1] = 1;
							flag = true;
							break;
						}
					}
					if (flag)
						continue;
				}

				/* 比较天,*为每天,#为不按天计算按周天计算,是否当前日期在计划执行天 */
				if (tab_day_of_month.equals("*")) {
					flags[j + 1] = 1;
				} else if (tab_day_of_month.equals("?")) {
					/* 不执行计算 */
				} else {
					String[] cron_day_of_month = tab_day_of_month.split(",");
					int monthEnd = Jdate.getActualMaximum(Calendar.DATE);
					for (int i = 0; i < cron_day_of_month.length; i++) {
						if (isNullOrEmpty(cron_day_of_month[i]))
							continue;
						String[] rg = cron_day_of_month[i].split("-");
						int bg = Integer.parseInt(rg[0]), end = -1;
						short retM;
						if (rg.length > 1) {
							end = Integer.parseInt(rg[1]);
							if (end < bg) {
								retM = -1;
							} else {
								retM = 2;
							}
						} else {
							retM = 1;
						}
						if ((retM < 0) || (retM == 1 && bg == 0 && monthEnd == Jday) || (retM == 1 && bg == Jday)
								|| (retM > 1 && bg <= Jday && end >= Jday)) {
							flags[j + 1] = 1;
							break;
						}
					}
				}
			}

			long minus_afterPerPerid = -1000, minus_nextPeridMinus = -1000;

			long diffMin = (ck_hour * 60 + ck_minutes)
					- (Integer.parseInt(tab_hours) * 60 + Integer.parseInt(tab_minutes));
			if (diffMin == 0) {
				if (flags[1] == 1)
					minus_afterPerPerid = 0;
			} else if (diffMin > 0) {
				if (flags[1] == 1)
					minus_afterPerPerid = diffMin;
				if (flags[2] == 1)
					minus_nextPeridMinus = 24 * 60 - diffMin;
			} else {
				if (flags[0] == 1)
					minus_afterPerPerid = 24 * 60 + diffMin;
				if (flags[1] == 1)
					minus_nextPeridMinus = Math.abs(diffMin);
			}
			results.put("MINUS_AFTER_PERPERID", minus_afterPerPerid);
			results.put("MINUS_TO_NEXTPERID", minus_nextPeridMinus);
			return results;
		} catch (Exception e) {
			throw new Exception("Check TimeTab(" + timeTab + ") Fail,Check Date:" + ckDate.getTime(), e);
		}
	}
	
	/**
	 * 只校验日期是否满足表达式条件
	 * 
	 * @param date 日期
	 * @param cron 日 月 周 年
	 * @return
	 * @throws Exception
	 */
	public static boolean isSatisfiedCronByDate(Date date, String cron) throws Exception {
		if(date == null){
			return true;
		}
		if(StringUtil.isNullOrEmpty(cron)){
			return true;
		}
		CronExpression exp = null;
		try {
			exp = new CronExpression("0 0 0 " + cron.trim());
		} catch (Exception e) {
			throw new Exception("cron expression " + cron + " invalid, format[日 月 周天 年]. 例:每个月末/(L * ? *);每周的周一至周五/(? * 2-6 *)");
		}
		return exp.isSatisfiedBy(date);
	}
	
	public static Date getTimeAfter(Date date, String cron) throws Exception {
		return new CronExpression("0 0 0 " + cron.trim()).getTimeAfter(date);
	}
	
	/**
	 * 只校验日期是否满足表达式条件,满足返回0,不满足返回下个触发时间
	 * 
	 * @param date 日期
	 * @param cron 时 日 月 周 年
	 * @return long
	 *        0 满足表达式
	 *      > 0 不满足，返回下次触发时间
	 * @throws Exception
	 */
	public static long satisfiedCronByDateTime(Date date, String cron) throws Exception {
		CronExpression exp = null;
		try {
			exp = new CronExpression("* * " + cron.trim());
		} catch (Exception e) {
			throw new Exception("cron expression " + cron + " invalid, format[时 日 月 周天 年]. 例:(9-17 * * ? * / 在每天的上班时间内执行)(0-8,18-23 * * ? * / 在每天的下班时间内执行)");
		}
		if (exp.isSatisfiedBy(date)) {
			return 0;
		} else {
			Date nextTime = exp.getNextValidTimeAfter(date);
			if (nextTime == null) {
				throw new Exception("according to the expression " + cron + ", the next valid time cannot be found after " + CalendarUtil.fmtDate(date, DEFAULT_DATE_PATTERN));
			} else {
				return nextTime.getTime();
			}
		}
	}

	/**
	 * 8-9 数字区间校验
	 */
	public static short checkValueRange(String rangstr, int ck) {

		if (isNullOrEmpty(rangstr))
			return 0;
		String[] rg = rangstr.split("-");
		int bg = Integer.parseInt(rg[0]), end;
		if (rg.length > 1) {
			end = Integer.parseInt(rg[1]);
			if (end < bg)
				return -1;
			return 2;
		} else {
			return 1;
		}
	}

	/**
	 * 获取月最后一天31 30 28 29
	 */
	public static int getMonthEnd(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		if (ca.get(Calendar.YEAR) < 1900)
			return -1;
		return ca.getActualMaximum(Calendar.DATE);
	}

	/**
	 * 获取月最后一天31 30 28 29
	 */
	public static Date getMonthEndDate(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DATE));
		return ca.getTime();
	}

	public static String getFormateDate(Date date) {
		return fmtDate(date, "yyyyMMdd");
	}

	public static String fmtDate(Date date, String pat) {
		return new SimpleDateFormat(pat).format(date);
	}

	public static Date parseDate(String date, String pat) throws Exception {
		if(null == date || date.equals("")) {
			return null;
		}
		return new SimpleDateFormat(pat).parse(date);
	}

	public static String getNowTime() {
		return getNowTime("yyyy-MM-dd HH:mm:ss,SSS");
	}

	public static Date getNowDate() {
		return new Date(now());
	}

	public static String getNowTime(String pat) {
		return fmtDate(new Date(now()), pat);
	}

	public static String getTime(long mill, String pat) {
		return fmtDate(new Date(mill), pat);
	}

	public static void main(String[] args) throws Exception {
		/*
		System.out.println(CalendarUtil.checkValueRange("9-8", 9));
		System.out.println(CalendarUtil.getMonthEndDate(new Date()));
		System.out.println(
				CalendarUtil.fmtDate(CalendarUtil.add(new Date(), Calendar.SECOND, 20), "yyyy-MM-dd HH:mm:ss,SSS"));
		// System.out.println(CalendarUtils.fmtDate(CalendarUtils.getMonthEndDate(new
		// Date()),"yyyy-MM-dd HH:mm:ss,SSS"));

		System.out.println(System.currentTimeMillis());
		System.out.println(CalendarUtil.getNowDate().getTime());
		
		System.out.println(System.lineSeparator().trim());
		System.out.println("'" + System.getProperty("line.separator") + "'");
		
		
		Map<String,Object> ooo = CalendarUtil.timeCompCronTab(new Date(), "100 61 5 * * *");
		System.out.println(ooo);
		System.out.println(
		CalendarUtil.fmtDate(new Date((Long)ooo.get("CHECK_POINT") + (Long)ooo.get("MINUS_TO_NEXTPERID")*60*1000), "yyyy-MM-dd HH:mm:ss,SSS")
		);
		Date testDate = CalendarUtil.getMonthEndDate(CalendarUtil.getNowDate());
		Calendar ca = Calendar.getInstance();
		ca.setTime(testDate);
		ca.add(Calendar.DAY_OF_MONTH, -1);
		System.out.println(CalendarUtil.dateInCronTab(ca.getTime(), "? * ? 2018-2019"));
		*/
		System.out.println(StringUtil.formatTimeDiff(System.currentTimeMillis(), System.currentTimeMillis()-177412300000l));
	}

	public static boolean isNullOrEmpty(String param) {
		if (param == null) {
			return true;
		}
		if ("".equals(param.trim())) {
			return true;
		}
		return false;
	}

}
