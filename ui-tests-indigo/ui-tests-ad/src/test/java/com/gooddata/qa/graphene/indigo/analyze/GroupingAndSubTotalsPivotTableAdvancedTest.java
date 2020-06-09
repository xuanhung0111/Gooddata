package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.util.List;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.json.JSONException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class GroupingAndSubTotalsPivotTableAdvancedTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW = "Measures and attributes, rows";
    private static final String INSIGHT_HAS_THREE_ATTRIBUTES_ROW_AND_A_COLUMN = "Measures and three rows, a column";
    private static final String INSIGHT_HAS_A_MEASURE_AND_ATTRIBUTES_ROW_AND_COLUMN = "One measure and rows, columns";
    private static final String INSIGHT_TEST_EXPORT_AND_IMPORT_PROJECT = "Export, import project";
    private static final String INSIGHT_EXPORTED = "Insight exported";

    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private String insightJsonObject;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Table Grouping & Sub-Totals (Advanced)";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createAmountBOPMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_TABLE_COLUMN_AUTO_RESIZING, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareTable() {
        createInsight(INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE),
                Pair.of(ATTR_REGION, Type.ATTRIBUTE), Pair.of(ATTR_DEPARTMENT, Type.COLUMNS),
                Pair.of(ATTR_FORECAST_CATEGORY, Type.COLUMNS)));

        createInsight(INSIGHT_HAS_THREE_ATTRIBUTES_ROW_AND_A_COLUMN, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE),
                Pair.of(ATTR_REGION, Type.ATTRIBUTE), Pair.of(ATTR_FORECAST_CATEGORY, Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, Type.COLUMNS)));

        createInsight(INSIGHT_HAS_A_MEASURE_AND_ATTRIBUTES_ROW_AND_COLUMN, ReportType.TABLE,
            singletonList(METRIC_AMOUNT), asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE),
                Pair.of(ATTR_REGION, Type.ATTRIBUTE), Pair.of(ATTR_FORECAST_CATEGORY, Type.ATTRIBUTE),
                Pair.of(ATTR_IS_WON, Type.COLUMNS)));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void sortOnGroupedPivotTable() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW).getPivotTableReport();
        analysisPage.waitForReportComputing();
        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Inside Sales", "East Coast", "West Coast",
            "Direct Sales", "East Coast", "West Coast"));

        pivotTableReport.sortBaseOnHeader(ATTR_REGION);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Direct Sales", "East Coast", "Inside Sales",
            "East Coast", "Direct Sales", "West Coast", "Inside Sales", "West Coast"));

        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Direct Sales", "East Coast", "West Coast",
            "Inside Sales", "East Coast", "West Coast"));

        analysisPage.addDate().waitForReportComputing();
        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();

        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Direct Sales", "East Coast", "2010", "2011",
            "2012", "2013", "2014", "2016", "West Coast", "2010", "2011", "2012", "2013", "Inside Sales", "East Coast",
            "2010", "2011", "2012", "2013", "West Coast", "2010", "2011", "2012", "2013", "2014", "2017"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void grandAndSubTotalAreAutoAdded() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_ROW_AND_A_COLUMN).getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.SUM, "Exclude", 0);
        pivotTableReport.openAggregationPopup("Exclude", 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.addMetric(METRIC_AVG_AMOUNT).waitForReportComputing();

        assertThat(pivotTableReport.getSubTotalsContent(), hasItems("Sum", "$35,393.26", "$29,123.14"));

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(List::stream)
            .collect(toList()), hasItems("Sum", "$48,932,639.59", "$2,245,586.38", "$64,516.40"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void grandAndSubTotalAreNotAutoAdded() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_ROW_AND_A_COLUMN).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.addMetric(METRIC_AVG_AMOUNT).waitForReportComputing();

        assertEquals(pivotTableReport.getSubTotalsContent(), asList(
            "Sum", EMPTY, "$33,562,482.51", EMPTY, EMPTY, "$46,843,842.45",
            "Sum", EMPTY, "$15,370,157.08", EMPTY, EMPTY, "$20,848,974.50"));

        assertTrue(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(List::stream)
            .collect(toList()).toString().contains("Sum, , , $48,932,639.59, , "));

        analysisPage.addAttribute(ATTR_IS_WON).waitForReportComputing();
        assertThat(pivotTableReport.getSubTotalsContent(), hasItems("Sum", "$33,562,482.51", "$15,370,157.08"));

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(List::stream)
            .collect(toList()), hasItems("Sum", "$48,932,639.59"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void removeAttributeAndMeasureFromBucket() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_ROW_AND_A_COLUMN).getPivotTableReport();

        pivotTableReport.addTotal(AggregationItem.SUM, "Exclude", 0);
        pivotTableReport.openAggregationPopup("Exclude", 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.removeAttribute(ATTR_DEPARTMENT).waitForReportComputing();

        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertEquals(pivotTableReport.getGrandTotalsContent(), asList(asList("Sum", EMPTY, "$48,932,639.59",
            "$2,245,586.38", "$67,692,816.95", "$2,888,811.27")));

        analysisPage.addAttribute(ATTR_DEPARTMENT).reorderAttribute(ATTR_REGION, ATTR_DEPARTMENT)
            .reorderAttribute(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT).waitForReportComputing();

        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            asList(asList("Sum", EMPTY, EMPTY, "$48,932,639.59", "$2,245,586.38", "$67,692,816.95", EMPTY)));

        analysisPage.removeMetric(METRIC_AMOUNT).waitForReportComputing();

        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            asList(asList("Sum", EMPTY, EMPTY, "$2,245,586.38", "$2,888,811.27")));

        analysisPage.removeMetric(METRIC_AMOUNT_BOP).waitForReportComputing();
        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertFalse(pivotTableReport.containsGrandTotals(), "Grand Total should be not showed");
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void reorderAttributesInBucket() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_A_MEASURE_AND_ATTRIBUTES_ROW_AND_COLUMN).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.reorderAttribute(ATTR_DEPARTMENT, ATTR_REGION).waitForReportComputing();

        assertEquals(pivotTableReport.getSubTotalsContent(), asList("Sum", "$13,594,424.44", "$8,384,271.02",
            "Sum", "$3,879,912.33", "$2,158,488.63", "Sum", "$40,307,040.44", "$18,120,589.06",
            "Sum", "$20,533,325.88", "$9,647,404.74"));
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            singletonList(asList("Sum", EMPTY, EMPTY, "$78,314,703.09", "$38,310,753.45")));

        analysisPage.reorderAttribute(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY).waitForReportComputing();

        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            singletonList(asList("Sum", EMPTY, EMPTY, "$78,314,703.09", "$38,310,753.45")));

        Stream.of(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_REGION).forEach(
            e -> analysisPage.moveAttributeFromRowsToColumnsBucket(e));

        assertFalse(pivotTableReport.hasSubTotals(), "Subtotals should be not showed");
        assertFalse(pivotTableReport.containsGrandTotals(), "Grand Total should be not showed");
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void configFilterAttributeGlobalOnFilterBucket() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_A_MEASURE_AND_ATTRIBUTES_ROW_AND_COLUMN).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);
        analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets()
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getSubTotalsContent(), asList("Sum", EMPTY, "$53,901,464.88", "$26,504,860.08"));
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            singletonList(asList("Sum", EMPTY, EMPTY, "$53,901,464.88", "$26,504,860.08")));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void undoOrRedoOrClearActions() {
        String insight = "Insight :" + generateHashString();
        createInsight(insight, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE), Pair.of(ATTR_REGION, Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, Type.COLUMNS)));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(insight).getPivotTableReport();
        analysisPage.waitForReportComputing();
        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Inside Sales", "East Coast", "West Coast",
            "Direct Sales", "East Coast", "West Coast"));

        analysisPage.undo().waitForReportComputing();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Direct Sales", "East Coast", "West Coast",
            "Inside Sales", "East Coast", "West Coast"));

        pivotTableReport.addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.undo().redo().waitForReportComputing();

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(List::stream)
            .collect(toList()), hasItems("Sum", "$48,932,639.59", "$67,692,816.95"));

        analysisPage.addMetric(METRIC_AVG_AMOUNT).undo().redo().waitForReportComputing();

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(List::stream)
            .collect(toList()), hasItems("Sum", "$48,932,639.59", "$64,516.40", "$67,692,816.95"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportPivotIntoXLSXWithGroupingAndSubtotals() throws IOException {
        createInsight(INSIGHT_EXPORTED, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE), Pair.of(ATTR_REGION, Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, Type.COLUMNS)));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_EXPORTED).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog.getInstance(browser).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_EXPORTED + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        takeScreenshot(browser, INSIGHT_EXPORTED + "GroupingAndSubtotal", getClass());
        log.info(INSIGHT_EXPORTED + "GroupingAndSubtotal" + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertThat(XlsxUtils.excelFileToRead(exportFile.getPath(), 0).stream()
            .flatMap(List::stream)
            .collect(toList()), hasItems("Sum", "3.356248251E7", "4.684384245E7", "1.537015708E7",
            "2.08489745E7", "4.893263959E7", "6.769281695E7"));
    }

    @Test(dependsOnMethods = {"exportPivotIntoXLSXWithGroupingAndSubtotals"})
    public void exportPivotIntoCSVWithGroupingAndSubtotals() throws IOException {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_EXPORTED).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        analysisPage.exportTo(OptionalExportMenu.File.CSV);

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_EXPORTED + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        takeScreenshot(browser, INSIGHT_EXPORTED + "CSV", getClass());
        log.info(INSIGHT_EXPORTED + "CSV" + ":" + CSVUtils.readCsvFile(exportFile));

        assertThat(CSVUtils.readCsvFile(exportFile).stream().flatMap(List::stream).collect(toList()),
            hasItems("Sum", "33562482.51", "46843842.45", "15370157.08", "20848974.5", "48932639.59", "67692816.95"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testExportAndImportProjectWithInsight() {
        insightJsonObject = createInsight(INSIGHT_TEST_EXPORT_AND_IMPORT_PROJECT, ReportType.TABLE,
            singletonList(METRIC_AMOUNT), asList(Pair.of(ATTR_DEPARTMENT, Type.ATTRIBUTE),
                Pair.of(ATTR_REGION, Type.ATTRIBUTE), Pair.of(ATTR_FORECAST_CATEGORY, Type.COLUMNS)));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_TEST_EXPORT_AND_IMPORT_PROJECT)
            .waitForReportComputing().getPivotTableReport().addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);
        analysisPage.saveInsight().waitForReportComputing();

        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
            true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            initAnalysePage().openInsight(INSIGHT_TEST_EXPORT_AND_IMPORT_PROJECT).waitForReportComputing()
                .getPivotTableReport();

            assertEquals(pivotTableReport.getSubTotalsContent(),
                asList("Sum", "$33,562,482.51", "$46,843,842.45", "Sum", "$15,370,157.08", "$20,848,974.50"));
            assertEquals(pivotTableReport.getGrandTotalsContent(),
                singletonList(asList("Sum", EMPTY, "$48,932,639.59", "$67,692,816.95")));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testExportAndImportProjectWithInsight"})
    public void testPartialExportAndImportProject() {
        String exportToken = exportPartialProject(insightJsonObject, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_TEST_EXPORT_AND_IMPORT_PROJECT)
                .waitForReportComputing().getPivotTableReport();

            assertEquals(pivotTableReport.getSubTotalsContent(),
                asList("Sum", "$33,562,482.51", "$46,843,842.45", "Sum", "$15,370,157.08", "$20,848,974.50"));
            assertEquals(pivotTableReport.getGrandTotalsContent(),
                singletonList(asList("Sum", EMPTY, "$48,932,639.59", "$67,692,816.95")));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

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
}
