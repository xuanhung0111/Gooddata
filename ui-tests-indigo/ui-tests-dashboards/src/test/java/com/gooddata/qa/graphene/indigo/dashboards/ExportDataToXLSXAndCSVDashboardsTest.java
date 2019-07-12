package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu.File;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog.OptionalExport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.ElementUtils;

import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ExportDataToXLSXAndCSVDashboardsTest extends AbstractDashboardTest {

    private final String INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY = "Insight1" + generateHashString();
    private final String INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY = "Insight2" + generateHashString();
    private final String INSIGHT_HAS_SOME_METRICS = "Insight3" + generateHashString();
    private final String INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK = "Insight4" + generateHashString();
    private final String INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY = "Insight5" + generateHashString();
    private final String INSIGHT_HAS_CONFIG_SETTING = "Insight6" + generateHashString();
    private final String INSIGHT_HAS_HEAD_LINE_REPORT = "Insight7" + generateHashString();
    private final String INSIGHT_CONFIG_FILTER_ON_KPI = "Insight Filter Attribute";
    private final String INSIGHT_EMBEDDED = "Embedded Insight";

    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createInsightHasOnlyMetric(INSIGHT_HAS_SOME_METRICS, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        createInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT),
            asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)));

        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY).addFilter(ATTR_DEPARTMENT)
            .saveInsight();

        createInsight(INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.STACK)));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY).addFilter(ATTR_DEPARTMENT)
            .saveInsight();

        createInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, CategoryBucket.Type.STACK)));

        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK).addFilter(ATTR_DEPARTMENT)
            .addFilter(ATTR_FORECAST_CATEGORY).saveInsight();

        MetricsBucket metricsBucket = initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
            .addAttribute(ATTR_DEPARTMENT).addDate().getMetricsBucket();
        metricsBucket.setTitleItemBucket("M1\n" + METRIC_AMOUNT, "New-" + METRIC_AMOUNT);
        metricsBucket.getMetricConfiguration("New-" + METRIC_AMOUNT).expandConfiguration().showPercents();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter())
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();
        filterBucket.configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_CONFIG_SETTING);

        takeScreenshot(browser, INSIGHT_HAS_CONFIG_SETTING, getClass());

        createInsight(INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.STACK)));

        initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY)
            .getPivotTableReport().addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.addFilter(ATTR_DEPARTMENT).addFilter(ATTR_FORECAST_CATEGORY).saveInsight()
            .waitForReportComputing();
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getDataCheckMergeCellsAndUncheckFiltersContext")
    public void exportXLSXWith_CheckMergeCellsAnd_UncheckFiltersContext(String insight, List<List<String>> expectedResult)
        throws IOException {
        try {
            initIndigoDashboardsPage().addDashboard().addInsight(insight).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(File.XLSX);

            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.uncheckOption(OptionalExport.FILTERS_CONTEXT).confirmExport();

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), expectedResult);
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getDataUnCheckMergeCellsAndFiltersContext")
    public void exportXLSXWith_UnCheckMergeCellsAndFiltersContext(String insight, List<List<String>> expectedResult)
        throws IOException {
        try {
            initIndigoDashboardsPage().addDashboard().addInsight(insight).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(File.XLSX);
            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.uncheckOption(OptionalExport.CELL_MERGED).uncheckOption(OptionalExport.FILTERS_CONTEXT)
                .confirmExport();

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), expectedResult);
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getDataCheckMergeCellsAndFiltersContext")
    public void exportXLSXWith_CheckMergeCellsAndFiltersContext(String insight, List<List<String>> expectedResult)
        throws IOException {
        try {
            initIndigoDashboardsPage().addDashboard().addInsight(insight).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(File.XLSX);

            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.checkOption(OptionalExport.CELL_MERGED).checkOption(OptionalExport.FILTERS_CONTEXT)
                .confirmExport();

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), expectedResult);
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getDataUnCheckMergeCellsAndCheckFiltersContext")
    public void exportXLSXWith_UnCheckMergeCellsAnd_CheckFiltersContext(String insight, List<List<String>> expectedResult)
        throws IOException {
        try {
            initIndigoDashboardsPage().addDashboard().addInsight(insight).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(File.XLSX);

            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.uncheckOption(OptionalExport.CELL_MERGED).checkOption(OptionalExport.FILTERS_CONTEXT)
                .confirmExport();

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), expectedResult);
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getSavedInsightCSV")
    public void exportInsightsIntoCSVFormat(String insight, List<List<String>> expectedResult) throws IOException {
        try {
            initIndigoDashboardsPage().addDashboard().addInsight(insight).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(File.CSV);

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.CSV.getName());

            waitForExporting(exportFile);
            log.info(insight + ":" + CSVUtils.readCsvFile(exportFile));
            assertEquals(CSVUtils.readCsvFile(exportFile), expectedResult);
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.CSV.getName())));
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void cannotExportKPIWidgetAndHeadLineInsight() {
        createInsightHasOnlyMetric(INSIGHT_HAS_HEAD_LINE_REPORT, ReportType.HEAD_LINE, singletonList(METRIC_AMOUNT));
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_HEAD_LINE_REPORT)
            .addKpi(new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CLOSED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build()).saveEditModeWithWidgets()
            .selectDateFilterByName("All time")
            .waitForWidgetsLoading();

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_HAS_HEAD_LINE_REPORT).hoverOnOptionsButton();
        assertEquals(ElementUtils.getBubbleMessage(browser), "Export is not supported");

        indigoDashboardsPage.selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT).hoverOnOptionsButton();
        assertEquals(ElementUtils.getBubbleMessage(browser), "Export is not supported");
    }

    @Test(dependsOnGroups = "createProject")
    public void exportInsightIsRenderedWithEmbedding() throws IOException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
            .addColumnsAttribute(ATTR_FORECAST_CATEGORY).saveInsight(INSIGHT_EMBEDDED).waitForReportComputing();

        IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL)
            .waitForDashboardLoad().waitForWidgetsLoading().switchToEditMode().addInsight(INSIGHT_EMBEDDED)
            .waitForWidgetsLoading().saveEditModeWithWidgets().selectDateFilterByName("All time")
            .waitForWidgetsLoading();

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_EMBEDDED).exportTo(File.XLSX);
        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);

        exportXLSXDialog.checkOption(OptionalExport.CELL_MERGED).checkOption(OptionalExport.FILTERS_CONTEXT)
            .confirmExport();

        final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_EMBEDDED + "." + ExportFormat.EXCEL_XLSX.getName());
        waitForExporting(exportFile);
        log.info(INSIGHT_EMBEDDED + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include"),
                asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AMOUNT),
                asList("Direct Sales", "3.356248251E7", "4.684384245E7"),
                asList("Inside Sales", "1.537015708E7", "2.08489745E7")));
    }

    @Test(dependsOnGroups = "createProject")
    public void exportInsightAfterChangingFilterOnKPI() throws IOException {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
            .addColumnsAttribute(ATTR_FORECAST_CATEGORY).saveInsight(INSIGHT_CONFIG_FILTER_ON_KPI)
            .waitForReportComputing();
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_CONFIG_FILTER_ON_KPI)
            .addAttributeFilter(ATTR_DEPARTMENT, "Inside Sales").saveEditModeWithWidgets()
            .selectDateFilterByName("All time").waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(OptionalExport.CELL_MERGED).checkOption(OptionalExport.FILTERS_CONTEXT)
            .confirmExport();

        final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_CONFIG_FILTER_ON_KPI + "." + ExportFormat.EXCEL_XLSX.getName());
        waitForExporting(exportFile);
        log.info(INSIGHT_CONFIG_FILTER_ON_KPI + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(asList("Applied filters:", "Department IN (Inside Sales)"),
                asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include"),
                asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AMOUNT),
                asList("Inside Sales", "1.537015708E7", "2.08489745E7")));
    }

    @DataProvider
    private Object[][] getSavedInsightCSV() {
        return new Object[][]{
            {INSIGHT_HAS_SOME_METRICS,
                asList(asList(null, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP),
                    asList("Values", "116625456.54", "20286.2161315011", "5134397.65"))},
            {INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY,
                asList(asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Direct Sales", "80406324.96", "21310.979316194"),
                    asList("Inside Sales", "36219131.58", "18329.5200303644"))},
            {INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY,
                asList(asList(ATTR_DEPARTMENT, "Direct Sales", "Direct Sales", "Inside Sales", "Inside Sales"),
                    asList(null, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Values", "80406324.96", "21310.979316194", "36219131.58", "18329.5200303644"))},
            {INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK,
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include"),
                    asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AMOUNT),
                    asList("Direct Sales", "33562482.51", "46843842.45"),
                    asList("Inside Sales", "15370157.08", "20848974.5"))},
            {INSIGHT_HAS_CONFIG_SETTING,
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)", "New-Amount - SP year ago", "New-Amount"),
                    asList("Direct Sales", "2010", null, "0.150187619767568"),
                    asList("Direct Sales", "2011", "0.150187619767568", "0.498791407018685"),
                    asList("Direct Sales", "2012", "0.498791407018685", "0.313038445576533"),
                    asList("Direct Sales", "2013", "0.313038445576533", "0.0379825276372139"),
                    asList("Direct Sales", "2014", "0.0379825276372139", null),
                    asList("Direct Sales", "2015", null, null),
                    asList("Direct Sales", "2016", null, null),
                    asList("Direct Sales", "2017", null, null))},
            {INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY,
                asList(asList(null, ATTR_DEPARTMENT, "Direct Sales", "Inside Sales"),
                    asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AMOUNT),
                    asList("Direct Sales", "Exclude", "33562482.51", null),
                    asList("Direct Sales", "Include", "46843842.45", null),
                    asList("Inside Sales", "Exclude", null, "15370157.08"),
                    asList("Inside Sales", "Include", null, "20848974.5"),
                    asList("Sum", "Sum", "80406324.96", "36219131.58"))}};
    }

    @DataProvider(name = "getDataCheckMergeCellsAndUncheckFiltersContext")
    private Object[][] getSavedInsightMergeCell() {
        return new Object[][]{
            {INSIGHT_HAS_SOME_METRICS, expectedResultsInsightSomeMetric},
            {INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY, expectedResultsInsightAnAttributeViewBy},
            {INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY, expectedResultsInsightAnAttributeStackBy},
            {INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK, expectedResultsInsightAnAttributeStackAndViewBy},
            {INSIGHT_HAS_CONFIG_SETTING, expectedResultsInsightConfigSetting},
            {INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY, expectedResultsInsightSameAttribute}};
    }

    @DataProvider(name = "getDataUnCheckMergeCellsAndFiltersContext")
    private Object[][] getSavedInsightUnCheckBoth() {
        return new Object[][]{
            {INSIGHT_HAS_SOME_METRICS, expectedResultsInsightSomeMetric},
            {INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY, expectedResultsInsightAnAttributeViewBy},
            {INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY,
                asList(asList(ATTR_DEPARTMENT, "Direct Sales", "Direct Sales", "Inside Sales", "Inside Sales"),
                    asList("", METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Values", "8.040632496E7", "21310.979316194", "3.621913158E7", "18329.5200303644"))},
            {INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK, expectedResultsInsightAnAttributeStackAndViewBy},
            {INSIGHT_HAS_CONFIG_SETTING,
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)", "New-Amount - SP year ago", "New-Amount"),
                    asList("Direct Sales", "2010", "0.150187619767568"),
                    asList("Direct Sales", "2011", "0.150187619767568", "0.498791407018685"),
                    asList("Direct Sales", "2012", "0.498791407018685", "0.313038445576533"),
                    asList("Direct Sales", "2013", "0.313038445576533", "0.0379825276372139"),
                    asList("Direct Sales", "2014", "0.0379825276372139"),
                    asList("Direct Sales", "2015"),
                    asList("Direct Sales", "2016"),
                    asList("Direct Sales", "2017"))},
            {INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY,
                asList(asList("", ATTR_DEPARTMENT, "Direct Sales", "Inside Sales"),
                    asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AMOUNT),
                    asList("Direct Sales", "Exclude", "3.356248251E7"),
                    asList("Direct Sales", "Include", "4.684384245E7"),
                    asList("Inside Sales", "Exclude", "1.537015708E7"),
                    asList("Inside Sales", "Include", "2.08489745E7"),
                    asList("Sum", "8.040632496E7", "3.621913158E7"))}};
    }

    @DataProvider(name = "getDataCheckMergeCellsAndFiltersContext")
    private Object[][] getSavedInsightCheckBoth() {
        return new Object[][]{
            {INSIGHT_HAS_SOME_METRICS, expectedResultsInsightSomeMetric},
            {INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY, expectedResultsInsightAnAttributeViewBy},
            {INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY, expectedResultsInsightAnAttributeStackBy},
            {INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK, expectedResultsInsightAnAttributeStackAndViewBy},
            {INSIGHT_HAS_CONFIG_SETTING,
                asList(asList("Applied filters:", "Department IN (Direct Sales)"),
                    asList(ATTR_DEPARTMENT, "Year (Closed)", "New-Amount - SP year ago", "New-Amount"),
                    asList("Direct Sales", "2010", "0.150187619767568"),
                    asList("2011", "0.150187619767568", "0.498791407018685"),
                    asList("2012", "0.498791407018685", "0.313038445576533"),
                    asList("2013", "0.313038445576533", "0.0379825276372139"),
                    asList("2014", "0.0379825276372139"),
                    singletonList("2015"),
                    singletonList("2016"),
                    singletonList("2017"))},
            {INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY, expectedResultsInsightSameAttribute}};
    }

    @DataProvider(name = "getDataUnCheckMergeCellsAndCheckFiltersContext")
    private Object[][] getSavedInsightFiltersContext() {
        return new Object[][]{
            {INSIGHT_HAS_SOME_METRICS, expectedResultsInsightSomeMetric},
            {INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE_VIEW_BY, expectedResultsInsightAnAttributeViewBy},
            {INSIGHT_HAS_SOME_MEASURE_AND_AN_ATTRIBUTE_STACK_BY,
                asList(asList(ATTR_DEPARTMENT, "Direct Sales", "Direct Sales", "Inside Sales", "Inside Sales"),
                    asList("", METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Values", "8.040632496E7", "21310.979316194", "3.621913158E7", "18329.5200303644"))},
            {INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK, expectedResultsInsightAnAttributeStackAndViewBy},
            {INSIGHT_HAS_CONFIG_SETTING,
                asList(asList("Applied filters:", "Department IN (Direct Sales)"),
                    asList(ATTR_DEPARTMENT, "Year (Closed)", "New-Amount - SP year ago", "New-Amount"),
                    asList("Direct Sales", "2010", "0.150187619767568"),
                    asList("Direct Sales", "2011", "0.150187619767568", "0.498791407018685"),
                    asList("Direct Sales", "2012", "0.498791407018685", "0.313038445576533"),
                    asList("Direct Sales", "2013", "0.313038445576533", "0.0379825276372139"),
                    asList("Direct Sales", "2014", "0.0379825276372139"),
                    asList("Direct Sales", "2015"),
                    asList("Direct Sales", "2016"),
                    asList("Direct Sales", "2017"))},
            {INSIGHT_HAS_SAME_ATTRIBUTE_ON_VIEW_BY_AND_STACK_BY,
                asList(asList("", ATTR_DEPARTMENT, "Direct Sales", "Inside Sales"),
                    asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AMOUNT),
                    asList("Direct Sales", "Exclude", "3.356248251E7"),
                    asList("Direct Sales", "Include", "4.684384245E7"),
                    asList("Inside Sales", "Exclude", "1.537015708E7"),
                    asList("Inside Sales", "Include", "2.08489745E7"),
                    asList("Sum", "8.040632496E7", "3.621913158E7"))}};
    }

    private List<List<String>> expectedResultsInsightSomeMetric = asList(
        asList("", METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP),
        asList("Values", "1.1662545654E8", "20286.2161315011", "5134397.65"));

    private List<List<String>> expectedResultsInsightAnAttributeViewBy = asList(
        asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
        asList("Direct Sales", "8.040632496E7", "21310.979316194"),
        asList("Inside Sales", "3.621913158E7", "18329.5200303644"));

    private List<List<String>> expectedResultsInsightAnAttributeStackBy = asList(
        asList(ATTR_DEPARTMENT, "Direct Sales", "Inside Sales"),
        asList("", METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
        asList("Values", "8.040632496E7", "21310.979316194", "3.621913158E7", "18329.5200303644"));

    private List<List<String>> expectedResultsInsightAnAttributeStackAndViewBy = asList(
        asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include"),
        asList(ATTR_DEPARTMENT, METRIC_AMOUNT, METRIC_AMOUNT),
        asList("Direct Sales", "3.356248251E7", "4.684384245E7"),
        asList("Inside Sales", "1.537015708E7", "2.08489745E7"));

    private List<List<String>> expectedResultsInsightConfigSetting = asList(
        asList(ATTR_DEPARTMENT, "Year (Closed)", "New-Amount - SP year ago", "New-Amount"),
        asList("Direct Sales", "2010", "0.150187619767568"),
        asList("2011", "0.150187619767568", "0.498791407018685"),
        asList("2012", "0.498791407018685", "0.313038445576533"),
        asList("2013", "0.313038445576533", "0.0379825276372139"),
        asList("2014", "0.0379825276372139"),
        singletonList("2015"),
        singletonList("2016"),
        singletonList("2017"));

    private List<List<String>> expectedResultsInsightSameAttribute = asList(
        asList("", ATTR_DEPARTMENT, "Direct Sales", "Inside Sales"),
        asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AMOUNT),
        asList("Direct Sales", "Exclude", "3.356248251E7"),
        asList("Include", "4.684384245E7"),
        asList("Inside Sales", "Exclude", "1.537015708E7"),
        asList("Include", "2.08489745E7"),
        asList("Sum", "8.040632496E7", "3.621913158E7"));

    private String createInsight(String insightTitle, ReportType reportType, List<String> metricsTitle,
                                 List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList())));
    }
}
