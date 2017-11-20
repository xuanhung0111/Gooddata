package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TimeFormattingTest extends AbstractProjectTest {

    @Override
    public void initProperties() {
        projectTitle = "Time-formatting-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + UPLOAD_CSV + "/customer.csv"));

        createMetric("PhoneTime", "SELECT MIN([" + getMdService().getObjUri(getProject(),
                Fact.class, identifier("fact.csv_customer.phone_time")) + "])", "#,##0.00");

        String timeFormatted = new StringBuilder("{{{86400||[>1]# days ;[>0]# day ;#}}}")
                .append("{{{3600|24|[>1]# hours ;[>0]# hour ;#}}}")
                .append("{{{60|60|[>1]# minutes ;[>0]# minute ;#}}}")
                .append("{{{|60|[>1]# seconds;[>0]# second;#}}}")
                .toString();
        createMetric("PhoneTimeFormatted", "SELECT MIN([" + getMdService().getObjUri(getProject(),
                Fact.class, identifier("fact.csv_customer.phone_time_formatted")) + "])", timeFormatted);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTimeFormatting() throws InvalidFormatException, IOException {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName("Time_format")
            .withWhats("PhoneTimeFormatted", "PhoneTime")
            .withHows("Name");
        createReport(reportDefinition, "Time_format");
        checkRedBar(browser);

        List<String> metrics = reportPage.getTableReport().getRawMetricValues();
        List<String> timeFormattedMetrics = metrics.subList(0, 5);
        List<String> timeMetrics = metrics.subList(5, metrics.size());

        List<String> timeFormatExpected = asList("1 day 3 hours 46 minutes 40 seconds",
                "2 hours 46 minutes 40 seconds", "1 day", "16 minutes 40 seconds", "50 seconds");
        List<String> timeExpected = asList("100,000.00", "10,000.00", "86,400.00", "1,000.00", "50.00");
        assertEquals(timeFormattedMetrics, timeFormatExpected);
        assertEquals(timeMetrics, timeExpected);
        verifyExcelFile(new File(testParams.getDownloadFolder(),
                reportPage.exportReport(ExportFormat.EXCEL_XLSX) + ".xlsx"), timeFormatExpected);
        verifyPdfFile(new File(testParams.getDownloadFolder(),
                reportPage.exportReport(ExportFormat.PDF) + ".pdf"), timeFormatExpected, timeExpected);
    }

    private void verifyExcelFile(File excelFile, List<String> formatExpected)
            throws InvalidFormatException, IOException {
        List<String> timeFormatActual = Lists.newArrayList();
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
        try {
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rows = sheet.iterator();
            rows.next(); // skip the first row
            while (rows.hasNext()) {
                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = rows.next().cellIterator();
                cellIterator.next(); // skip the first column
                timeFormatActual.add(cellIterator.next().toString().trim());
            }
            assertEquals(timeFormatActual, formatExpected);
        } finally {
            workbook.close();
        }
    }

    private void verifyPdfFile(File pdfFile, List<String> formatExpected, List<String> expected) {
        final String pdfContent = PdfUtils.getTextContentFrom(pdfFile);
        assertTrue(Stream.concat(formatExpected.stream(), expected.stream())
            .allMatch(text -> pdfContent.contains(text)));
    }
}
