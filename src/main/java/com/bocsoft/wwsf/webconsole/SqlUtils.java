package com.bocsoft.wwsf.webconsole;


public class SqlUtils {
	
	public static String insert(String tabName,String... colNameArr) {
		String colNames = "insert into "+tabName+"(";
		for(String fString : colNameArr) {
			colNames += fString + ",";
		}
		return colNames.substring(0, colNames.length()-1)+") ";
	}
	
	public static String values(Object... colValueArr) {
		String colValues = "values(";
		for(Object fObject : colValueArr) {
			if(null != fObject) {
				if(fObject.toString().startsWith("to_date(") || fObject.toString().startsWith("date_format(")) {//日期型值
					colValues += fObject + ",";
				}else {
					colValues += "'" + fObject + "',";
				}
			}else {
				colValues += null + ",";
			}
		}
		return colValues.substring(0, colValues.length()-1)+");\r\n";
	}
	
	public static String processDate(boolean isOracle, String value) {
		if (StringUtil.hasText(value)) {
			return isOracle ? "to_date('" + value + "','yyyy-MM-dd')" : "date_format('" + value + "','%Y-%m-%d')";
		} else {
			return "null";
		}
	}
	
	public static String processDateTime(boolean isOracle, String value) {
		if (StringUtil.hasText(value)) {
			return isOracle ? "to_date('"+value+"','yyyy-MM-dd HH24:mi:ss')" : "date_format('" + value + "','%Y-%m-%d %H:%i:%S')";
		} else {
			return "null";
		}
	}
	
}
