package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.google.common.collect.Lists;

public class TimeFormattingTest extends AbstractProjectTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Time-formatting-test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void setupProject() {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + UPLOAD_CSV + "/customer.csv"));
    }

    @Test(dependsOnMethods = {"setupProject"})
    public void createMetrics() {
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

    @Test(dependsOnMethods = {"createMetrics"})
    public void testTimeFormatting() throws BiffException, IOException {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName("Time_format")
            .withWhats("PhoneTimeFormatted", "PhoneTime")
            .withHows("Name");
        createReport(reportDefinition, "Time_format");
        checkRedBar(browser);

        List<String> metrics = reportPage.getTableReport().getRawMetricElements();
        List<String> timeFormattedMetrics = metrics.subList(0, 5);
        List<String> timeMetrics = metrics.subList(5, metrics.size());

        List<String> timeFormatExpected = asList("1 day 3 hours 46 minutes 40 seconds",
                "2 hours 46 minutes 40 seconds", "1 day ", "16 minutes 40 seconds", "50 seconds");
        List<String> timeExpected = asList("100,000.00", "10,000.00", "86,400.00", "1,000.00", "50.00");
        assertEquals(timeFormattedMetrics, timeFormatExpected);
        assertEquals(timeMetrics, timeExpected);

        verifyPdfFile(new File(testParams.getDownloadFolder(),
                reportPage.exportReport(ExportFormat.PDF) + ".pdf"), timeFormatExpected, timeExpected);
    }

    private void verifyExcelFile(File excelFile, List<String> formatExpected, List<String> expected)
            throws BiffException, IOException {
        Workbook workbook =  Workbook.getWorkbook(excelFile);
        Sheet sheet = workbook.getSheet(0);

        List<String> actual = Lists.newArrayList();
        for (int i = 1, n = formatExpected.size(); i <= n; i++) {
            actual.add(sheet.getCell(1, i).getContents());
        }
        assertEquals(actual, formatExpected);

        actual.clear();
        for (int i = 1, n = expected.size(); i <= n; i++) {
            actual.add(sheet.getCell(2, i).getContents());
        }
        assertEquals(actual, expected);
    }

    private void verifyPdfFile(File pdfFile, List<String> formatExpected, List<String> expected) {
        final String pdfContent = PdfUtils.getTextContentFrom(pdfFile);
        assertTrue(Stream.concat(formatExpected.stream(), expected.stream())
            .allMatch(text -> pdfContent.contains(text)));
    }
}
