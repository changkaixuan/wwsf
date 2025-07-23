package com.bocsoft.wwsf.webconsole;

import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
//import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
//import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelUtils {
	
	public static final int excelMaxRow=65535;
	
	public static void createTitle(Workbook wb, Sheet sheet, String[] titles, int[] colWidth, float height) {
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(height);
		CellStyle titleStyle = wb.createCellStyle();
		titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		titleStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		titleStyle.setWrapText(true);
		
		titleStyle.setBorderBottom(CellStyle.BORDER_THIN);
		titleStyle.setBorderTop(CellStyle.BORDER_THIN);
		titleStyle.setBorderLeft(CellStyle.BORDER_THIN);
		titleStyle.setBorderRight(CellStyle.BORDER_THIN);
		titleStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		titleStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		titleStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		titleStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		
		Font font = wb.createFont();
		font.setBoldweight((short) 1000);
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 10);
		titleStyle.setFont(font);
		for (int k = 0; k < titles.length; k++) {								
			Cell cell = titleRow.createCell(k);
			cell.setCellStyle(titleStyle);
			cell.setCellValue(titles[k]);
		}
		for (int i = 0; i < colWidth.length; i++) {
			sheet.setColumnWidth(i, colWidth[i]);
		}
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5, String colValue6) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5, String colValue6,String colValue7) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		Cell cell7 = row.createCell(6);
		cell7.setCellValue(colValue7);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5
			, String colValue6, String colValue7, String colValue8, String colValue9) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		Cell cell7 = row.createCell(6);
		cell7.setCellValue(colValue7);
		Cell cell8 = row.createCell(7);
		cell8.setCellValue(colValue8);
		Cell cell9 = row.createCell(8);
		cell9.setCellValue(colValue9);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5
			, String colValue6, String colValue7, String colValue8, String colValue9, String colValue10, String colValue11, String colValue12, String colValue13) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		Cell cell7 = row.createCell(6);
		cell7.setCellValue(colValue7);
		Cell cell8 = row.createCell(7);
		cell8.setCellValue(colValue8);
		Cell cell9 = row.createCell(8);
		cell9.setCellValue(colValue9);
		Cell cell10 = row.createCell(9);
		cell10.setCellValue(colValue10);
		Cell cell11 = row.createCell(10);
		cell11.setCellValue(colValue11);
		Cell cell12 = row.createCell(11);
		cell12.setCellValue(colValue12);
		Cell cell13 = row.createCell(12);
		cell13.setCellValue(colValue13);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet,int rowNum,String... colValueArr) {
		Row row = sheet.createRow(rowNum);
        for(int a=0;a<colValueArr.length;a++) {
        	Cell cell = row.createCell(a);
    		cell.setCellValue(colValueArr[a]);
        }
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, int colValue5
			, int colValue6, int colValue7, String colValue8, String colValue9, String colValue10, int colValue11, String colValue12, String colValue13
			, String colValue14, String colValue15, String colValue16, String colValue17, int colValue18, String colValue19) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		Cell cell7 = row.createCell(6);
		cell7.setCellValue(colValue7);
		Cell cell8 = row.createCell(7);
		cell8.setCellValue(colValue8);
		Cell cell9 = row.createCell(8);
		cell9.setCellValue(colValue9);
		Cell cell10 = row.createCell(9);
		cell10.setCellValue(colValue10);
		Cell cell11 = row.createCell(10);
		cell11.setCellValue(colValue11);
		Cell cell12 = row.createCell(11);
		cell12.setCellValue(colValue12);
		Cell cell13 = row.createCell(12);
		cell13.setCellValue(colValue13);
		Cell cell14 = row.createCell(13);
		cell14.setCellValue(colValue14);
		Cell cell15 = row.createCell(14);
		cell15.setCellValue(colValue15);
		Cell cell16 = row.createCell(15);
		cell16.setCellValue(colValue16);
		Cell cell17 = row.createCell(16);
		cell17.setCellValue(colValue17);
		Cell cell18 = row.createCell(17);
		cell18.setCellValue(colValue18);
		Cell cell19 = row.createCell(18);
		cell19.setCellValue(colValue19);
		rowNum++;
		return rowNum;
	}
	
	public static int createRow(Sheet sheet, int rowNum, String colValue1, String colValue2, String colValue3, String colValue4, String colValue5
			, String colValue6, String colValue7, String colValue8, String colValue9, String colValue10, String colValue11, String colValue12, String colValue13
			, String colValue14, String colValue15, String colValue16, String colValue17, String colValue18) {
		Row row = sheet.createRow(rowNum);
		Cell cell1 = row.createCell(0);
		cell1.setCellValue(colValue1);
		Cell cell2 = row.createCell(1);
		cell2.setCellValue(colValue2);
		Cell cell3 = row.createCell(2);
		cell3.setCellValue(colValue3);
		Cell cell4 = row.createCell(3);
		cell4.setCellValue(colValue4);
		Cell cell5 = row.createCell(4);
		cell5.setCellValue(colValue5);
		Cell cell6 = row.createCell(5);
		cell6.setCellValue(colValue6);
		Cell cell7 = row.createCell(6);
		cell7.setCellValue(colValue7);
		Cell cell8 = row.createCell(7);
		cell8.setCellValue(colValue8);
		Cell cell9 = row.createCell(8);
		cell9.setCellValue(colValue9);
		Cell cell10 = row.createCell(9);
		cell10.setCellValue(colValue10);
		Cell cell11 = row.createCell(10);
		cell11.setCellValue(colValue11);
		Cell cell12 = row.createCell(11);
		cell12.setCellValue(colValue12);
		Cell cell13 = row.createCell(12);
		cell13.setCellValue(colValue13);
		Cell cell14 = row.createCell(13);
		cell14.setCellValue(colValue14);
		Cell cell15 = row.createCell(14);
		cell15.setCellValue(colValue15);
		Cell cell16 = row.createCell(15);
		cell16.setCellValue(colValue16);
		Cell cell17 = row.createCell(16);
		cell17.setCellValue(colValue17);
		Cell cell18 = row.createCell(17);
		cell18.setCellValue(colValue18);
		rowNum++;
		return rowNum;
	}
	
	public static String getCellValue(Cell cell,int cellType){
        String cellValue = null;
        if (cell != null) {
            // 单元格不同的类型，用不同方法取值
            switch (cellType) {
                case Cell.CELL_TYPE_STRING:
                    cellValue = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                    } else {
                        cellValue = replaceDouble(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    cellValue = String.valueOf(cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
	                    case Cell.CELL_TYPE_STRING:
	                        cellValue = cell.getStringCellValue();
	                        break;
	                    case Cell.CELL_TYPE_NUMERIC:
	                        cellValue = replaceDouble(cell.getNumericCellValue());
	                        break;
	                    default:
	                        break;
                    }
                    break;
                default:
                    break;
            }
            // 对字符串进行处理
            if (cellValue != null) {
                cellValue = replace(cellValue);
            }
        }
        return cellValue;
    }
	
	public static String replaceDouble(double dou) {
		String str = String.valueOf(dou);
		String result;

		String[] results = str.split("\\.");
		if (results.length == 2) {
			boolean isAllz = true;
			for (int i = 0; i < results[1].length(); i++) {
				String subs = results[1].substring(i, i + 1);
				if (!subs.equals("0")) {
					isAllz = false;
				}
			}
			if (isAllz) {
				result = results[0];
			} else {
				result = str;
			}
		} else {
			result = str;
		}
		return result;
	}
	
	public static String replace(String str) {
		String result = str.trim();
		result = result.replace("\r", "");
		result = result.replace("\n", "");
		return result;
	}

}
