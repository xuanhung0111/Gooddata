package com.gooddata.qa.utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XlsxUtils {

    private XlsxUtils() {
    }

    public static List<List<String>> excelFileToRead(String name, int sheetNumber) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook(name);

        XSSFSheet sheet = wb.getSheetAt(sheetNumber);
        XSSFRow row;
        XSSFCell cell;

        Iterator rows = sheet.rowIterator();
        List<List<String>> content = new ArrayList<>();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            List<String> rowContent = new ArrayList<>();
            while (cells.hasNext()) {
                cell = (XSSFCell) cells.next();
                if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                    rowContent.add(cell.getStringCellValue());
                } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                    rowContent.add(String.valueOf(cell.getNumericCellValue()));
                }
            }
            content.add(rowContent);
        }
        return content;
    }
}
