package com.gooddata.qa.graphene.reports;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.greypages.exporter.ReportExporterExecutorFragment;
import com.gooddata.qa.graphene.fragments.greypages.exporter.Xtab2Executor3Fragment;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.report.ReportEmbedDialog;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.utils.XlsxUtils;
import org.testng.annotations.Test;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class ExportReportUngroupedXLSXTest extends AbstractEmbeddedModeTest {

    private static final String EMBEDDED_REPORT = "Embedded Report";
    private static final String DRILLING_REPORT = "Drilling Report";
    private static final String GREY_PAGE_REPORT = "Grey Page Report";
    private static final String TWO_DISPLAY_IN_LEFT_REPORT = "Two Display In Left Report";
    private static final String IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT = "Two Display In Left And Top Report";
    private static final String METRIC_FORMATTING_REPORT = "Metric Formatting Report";
    private static final String METRIC_REGION = "metricRegion";
    private static final String METRIC_BARS = "metricBars";
    private static final String METRIC_TRUNCATE_NUMBER = "metricTruncateNumber";
    private static final String METRIC_COLORS = "metricColors";
    private static final String METRIC_COLORS_FORMAT = "metricColorsFormat";
    private static final String METRIC_BACKGROUND_FORMAT = "metricBackgroundColorsFormat";
    private static final String METRIC_UTF = "metricUTF";
    private static final String METRIC_UNIT_CONVERSION = "metricUnitConversion";
    private static final String METRIC_NULL_VALUE = "metricNullValue";
    private static final List<Pair<String, String>> METRIC_FORMATTINGS =
        asList(Pair.of(Formatter.REGION.toString(), METRIC_REGION),
            Pair.of(Formatter.COLORS.toString(), METRIC_COLORS),
            Pair.of(Formatter.BACKGROUND_COLOR_FORMAT.toString(), METRIC_BACKGROUND_FORMAT),
            Pair.of(Formatter.TRUNCATE_NUMBERS.toString(), METRIC_TRUNCATE_NUMBER),
            Pair.of(Formatter.BARS.toString(), METRIC_BARS),
            Pair.of(Formatter.COLORS_FORMAT.toString(), METRIC_COLORS_FORMAT),
            Pair.of(Formatter.UTF_8.toString(), METRIC_UTF),
            Pair.of(Formatter.UNIT_CONVERSION.toString(), METRIC_UNIT_CONVERSION));

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createNumberOfWonOppsMetric();
        metrics.createNumberOfLostOppsMetric();
        metrics.createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportXLSXWithUnMergedCellSomeLabelAttribute() throws IOException {
        initAttributePage()
            .initAttribute(ATTR_IS_WON)
            .getLabel(ATTR_IS_WON)
            .selectType(AttributeLabelTypes.IMAGE);

        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(TWO_DISPLAY_IN_LEFT_REPORT)
            .withWhats(METRIC_AMOUNT)
            .withHows(new HowItem(ATTR_PRODUCT, HowItem.Position.LEFT),
                new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT)), TWO_DISPLAY_IN_LEFT_REPORT);

        exportReportToXLSXWithUnMergedCell(TWO_DISPLAY_IN_LEFT_REPORT);
        List<String> xlsxContent;
        verifyReportExport(ExportFormat.EXCEL_XLSX, TWO_DISPLAY_IN_LEFT_REPORT);

        xlsxContent = XlsxUtils.excelFileToRead(testParams.getDownloadFolder() +
            testParams.getFolderSeparator() + TWO_DISPLAY_IN_LEFT_REPORT + "." + ExportFormat.EXCEL_XLSX.getName(), 0)
            .stream().flatMap(List::stream).collect(Collectors.toList());

        takeScreenshot(browser, TWO_DISPLAY_IN_LEFT_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "CompuSci"), 2);

        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT)
            .withWhats(METRIC_AMOUNT)
            .withHows(new HowItem(ATTR_IS_WON, HowItem.Position.LEFT),
                new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT),
                new HowItem(ATTR_STATUS, HowItem.Position.TOP)), IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT);

        exportReportToXLSXWithUnMergedCell(IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT);
        verifyReportExport(ExportFormat.EXCEL_XLSX, IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT);

        xlsxContent = XlsxUtils.excelFileToRead(testParams.getDownloadFolder() +
            testParams.getFolderSeparator() + IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT + "."
            + ExportFormat.EXCEL_XLSX.getName(), 0)
            .stream().flatMap(List::stream).collect(Collectors.toList());

        takeScreenshot(browser, IMAGE_DISPLAY_IN_LEFT_AND_TOP_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "false"), 2);
        assertEquals(Collections.frequency(xlsxContent, "true"), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportXLSXWithUnMergedCellMetricFormatting() throws IOException {
        METRIC_FORMATTINGS.forEach(e -> createMetric(e.getValue(), format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))), e.getKey()));

        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)));
        createMetric(METRIC_NULL_VALUE, metricExpression, Formatter.CONDITION_NULL.toString());

        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(METRIC_FORMATTING_REPORT)
            .withWhats(METRIC_REGION, METRIC_COLORS, METRIC_BACKGROUND_FORMAT, METRIC_TRUNCATE_NUMBER,
                METRIC_NULL_VALUE, METRIC_BARS, METRIC_COLORS_FORMAT, METRIC_UTF, METRIC_UNIT_CONVERSION)
            .withHows(new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT),
                new HowItem(ATTR_STATUS, HowItem.Position.LEFT)), METRIC_FORMATTING_REPORT);

        exportReportToXLSXWithUnMergedCell(METRIC_FORMATTING_REPORT);
        verifyReportExport(ExportFormat.EXCEL_XLSX, METRIC_FORMATTING_REPORT);

        List<String> xlsxContent = XlsxUtils.excelFileToRead(testParams.getDownloadFolder() +
            testParams.getFolderSeparator() + METRIC_FORMATTING_REPORT + "." + ExportFormat.EXCEL_XLSX.getName(), 0)
            .stream().flatMap(List::stream).collect(Collectors.toList());

        takeScreenshot(browser, METRIC_FORMATTING_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "Direct Sales"), 3);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportDrillReportXLSXWithUnMergedCellFromDashboard() throws IOException {
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(DRILLING_REPORT)
            .withWhats(new WhatItem(METRIC_AMOUNT, ATTR_ACCOUNT))
            .withHows(new HowItem(ATTR_PRODUCT, HowItem.Position.LEFT),
                new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT)), DRILLING_REPORT);

        ReportPage.getInstance(browser).saveAsReport();
        DashboardsPage dashboardsPage = initDashboardsPage().addReportToDashboard(DRILLING_REPORT)
            .saveDashboard();

        final File exportFile = new File(testParams.getDownloadFolder(),
            dashboardsPage.exportDashboardToXLSXWithUnMergedCell());
        waitForExporting(exportFile);

        List<String> xlsxContent = XlsxUtils.excelFileToRead(exportFile.getPath(), 0).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        takeScreenshot(browser, DRILLING_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "CompuSci"), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportXLSXWithUnMergedCellThroughEmbeddedReport() throws IOException{
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(EMBEDDED_REPORT)
            .withWhats(new WhatItem(METRIC_AMOUNT, ATTR_ACCOUNT))
            .withHows(new HowItem(ATTR_PRODUCT, HowItem.Position.LEFT),
                new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT)), EMBEDDED_REPORT);

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        embeddedReportUri = embedDialog.getEmbedUri();

        initEmbeddedReport().openReportInfoViewPanel().downloadXLSXReportWithUnMergeCell();
        String xlsxUrl = testParams.getExportFilePath(EMBEDDED_REPORT + "." + ExportFormat.EXCEL_XLSX.getName());

        verifyReportExport(ExportFormat.EXCEL_XLSX, EMBEDDED_REPORT);

        List<String> xlsxContent = XlsxUtils.excelFileToRead(xlsxUrl, 0).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        takeScreenshot(browser, EMBEDDED_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "CompuSci"), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportXLSXWithUnMergedCellThroughGreyPage() throws IOException{
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
            .withName(GREY_PAGE_REPORT)
            .withWhats(new WhatItem(METRIC_AMOUNT, ATTR_ACCOUNT))
            .withHows(new HowItem(ATTR_PRODUCT, HowItem.Position.LEFT),
                new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT)), GREY_PAGE_REPORT);

        String reportUri = getReportByTitle(GREY_PAGE_REPORT).getUri();

        openUrl(XTAB2_EXECUTOR3);
        Xtab2Executor3Fragment xtab2Executor3Fragment = waitForFragmentVisible(xtab2Executor3);
        xtab2Executor3Fragment.fillReportUri(reportUri).submit();

        String result = xtab2Executor3Fragment.getResult();

        openUrl(EXPORTER_EXECUTOR);

        ReportExporterExecutorFragment reportExporterExecutorFragment = waitForFragmentVisible(reportExporterExecutor);
        reportExporterExecutorFragment.unCheckMergeHeadersCheckbox();
        reportExporterExecutorFragment.chooseXLSXRadio();
        reportExporterExecutorFragment.submit(result);

        String xlsxUrl = testParams.getExportFilePath(GREY_PAGE_REPORT + "." + ExportFormat.EXCEL_XLSX.getName());

        verifyReportExport(ExportFormat.EXCEL_XLSX, GREY_PAGE_REPORT);

        List<String> xlsxContent = XlsxUtils.excelFileToRead(xlsxUrl, 0).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        takeScreenshot(browser, GREY_PAGE_REPORT, getClass());

        assertEquals(Collections.frequency(xlsxContent, "CompuSci"), 2);
    }

    private ReportPage initReportPage(String reportName) {
        return initReportsPage()
            .openFolder("My Reports")
            .openReport(reportName);
    }

    private void exportReportToXLSXWithUnMergedCell(String reportName) {
        initReportPage(reportName)
            .exportReportToXLSXWithUnMergedCell();
    }
}
