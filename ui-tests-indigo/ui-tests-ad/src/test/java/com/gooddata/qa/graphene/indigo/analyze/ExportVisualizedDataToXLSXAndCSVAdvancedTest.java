package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ExportVisualizedDataToXLSXAndCSVAdvancedTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_TOTAL_VALUE = "Total value";

    private String sourceProjectId;
    private String targetProjectId;
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private FactRestRequest factRestRequest;
    private String insightJsonObject;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "SD-136 M1 Export visualized data in AD and UI SDK to XLSX and CSV (Advanced)";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createAmountBOPMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ACTIVE_FILTER_CONTEXT, false);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightsIntoXLSXFormatWithGlobalFilterBar() throws IOException {
        String insight = "Insight " + generateHashString();
        try {
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .addDate().waitForReportComputing().setInsightTitle(insight).getFilterBuckets()
                .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales")
                .configDateFilter("01/01/2012", "01/01/2013");

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
            dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();

            analysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, insight, getClass());
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)", "Amount - SP year ago", METRIC_AMOUNT),
                    asList("Direct Sales", "2012", "4.010598396E7", "2.517027098E7"),
                    asList("2013", "10584.41")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightsIntoXLSXFormatWithFilterUnderMetric() throws IOException {
        String insight = "Insight " + generateHashString();
        try {
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .addDate().waitForReportComputing().setInsightTitle(insight).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilter(ATTR_DEPARTMENT, "Direct Sales")
                .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2012", "01/01/2013");

            analysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, insight, getClass());
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)",
                    "Amount, Closed: Jan 1, 2012 - Jan 1, 2013 (Department: Direct Sales)"),
                    asList("Direct Sales", "2012", "2.517027098E7")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightsIntoXLSXBetweenLocalAttributeAndGlobalDateFilter() throws IOException {
        String insight = "Insight " + generateHashString();
        try {
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .addDate().waitForReportComputing().setInsightTitle(insight).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilter(ATTR_DEPARTMENT, "Direct Sales");

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).configTimeFilter("01/01/2012", "01/01/2013");

            analysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, insight, getClass());
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)", "Amount (Department: Direct Sales)"),
                    asList("Direct Sales", "2012", "2.517027098E7")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightsIntoXLSXBetweenLocalDateMetricAndGlobalAttributeFilter() throws IOException {
        String insight = "Insight " + generateHashString();
        try {
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .addDate().waitForReportComputing().setInsightTitle(insight).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2012", "01/01/2013");

            analysisPage.getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
            analysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, insight, getClass());
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_DEPARTMENT, "Year (Closed)", METRIC_AMOUNT + ", Closed: Jan 1, 2012 - Jan 1, 2013"),
                    asList("Direct Sales", "2012", "2.517027098E7")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightsIntoXLSXFormatWithAttributeFilterNotRelatedDate() throws IOException {
        String insight = "Insight " + generateHashString();
        try {
            MetricConfiguration metricConfiguration = initAnalysePage().addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE)
                .waitForReportComputing().setInsightTitle(insight).getMetricsBucket()
                .getMetricConfiguration("Count of " + ATTR_DEPARTMENT).expandConfiguration();

            assertEquals(metricConfiguration.expandFilterByDate().getRoot().getText(),
                "No data available\n5 unrelated dates hidden");
            analysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog()
                .confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + insight + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, insight, getClass());
            log.info(insight + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList("", "Count of " + ATTR_DEPARTMENT), asList("Count of " + ATTR_DEPARTMENT, "2.0")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportTableContainTotalValueIntoXLSX() throws IOException {
        try {
            insightJsonObject = indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_TOTAL_VALUE, ReportType.TABLE)
                    .setMeasureBucket(
                        asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                            MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                    .setCategoryBucket(singletonList(
                        CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                            CategoryBucket.Type.ATTRIBUTE))));

            PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_TOTAL_VALUE)
                .waitForReportComputing().getPivotTableReport();
            pivotTableReport.addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
            analysisPage.saveInsight().waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog()
                .confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + INSIGHT_HAS_TOTAL_VALUE + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, INSIGHT_HAS_TOTAL_VALUE, getClass());
            log.info(INSIGHT_HAS_TOTAL_VALUE + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Exclude", "4.893263959E7", "16972.8198369754"),
                    asList("Include", "6.769281695E7", "23619.2662072575"),
                    asList("Sum", "1.1662545654E8")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(INSIGHT_HAS_TOTAL_VALUE + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableExportInsightForNoDataOrInvalidData() {
        String insight = "Insight " + generateHashString();

        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)));
        final String metricNullValue = "EMPTY_SHOW_NULL_STRING";
        createMetric(metricNullValue, metricExpression, "#'##0,00 formatted; [=null] null value!");
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.TABLE)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNullValue)))));

        OptionalExportMenu optionalExportMenu = initAnalysePage().openInsight(insight).waitForReportComputing().
            getPageHeader().clickOptionsButton();
        assertFalse(optionalExportMenu.isExportToButtonEnabled(OptionalExportMenu.File.CSV),
            "Export to CSV options should be disabled");
        assertFalse(optionalExportMenu.isExportToButtonEnabled(OptionalExportMenu.File.XLSX),
            "Export to XLSX options should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightContainsNotComputeData() {
        String insight = "Insight " + generateHashString();

        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
            MetricFormatterDialog.Formatter.DEFAULT.toString());

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.TABLE)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNegativeValue)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNegativeValue)))));

        initAnalysePage().openInsight(insight).waitForReportComputing();
        Stream.of(ReportType.PIE_CHART, ReportType.DONUT_CHART, ReportType.TREE_MAP)
            .forEach(type -> {
                try {
                    exportInsightsAndCheckDataFile(type, insight, metricNegativeValue);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
    }

    @Test(dependsOnGroups = {"createProject"}, enabled = false)
    public void exportInsightContainsRestrictedData() {
        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        try {
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
                .waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            assertEquals(ElementUtils.getErrorMessage(browser), "You cannot export this insight because it contains restricted data.");
        } finally {
            factRestRequest.unsetFactRestricted(factUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightContainsProtectedDataWithEditorRole() throws IOException {
        String attributeUri = factRestRequest.getAttributeByTitle(ATTR_STAGE_NAME).getUri();
        try {
            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            factRestRequest.setFactProtected(attributeUri);
            OptionalExportMenu optionalExportMenu = initAnalysePage().changeReportType(ReportType.TABLE)
                .addAttribute(ATTR_STAGE_NAME).waitForReportComputing().clickOptionsButton();
            assertFalse(optionalExportMenu.isExportToButtonEnabled(OptionalExportMenu.File.XLSX),
                "Export to XLSX options should be disabled");
            assertFalse(optionalExportMenu.isExportToButtonEnabled(OptionalExportMenu.File.CSV),
                "Export to CSV options should be disabled");
        } finally {
            factRestRequest.unsetFactProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"exportTableContainTotalValueIntoXLSX"})
    public void testExportAndImportProjectWithInsight() throws IOException {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
            true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            initAnalysePage().openInsight(INSIGHT_HAS_TOTAL_VALUE).waitForReportComputing()
                .exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + INSIGHT_HAS_TOTAL_VALUE + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, INSIGHT_HAS_TOTAL_VALUE + " ExportAndImport", getClass());
            log.info(INSIGHT_HAS_TOTAL_VALUE+ " ExportAndImport:" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Exclude", "4.893263959E7", "16972.8198369754"),
                    asList("Include", "6.769281695E7", "23619.2662072575"),
                    asList("Sum", "1.1662545654E8")));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testExportAndImportProjectWithInsight"})
    public void testPartialExportAndImportProject() throws IOException {
        String exportToken = exportPartialProject(insightJsonObject, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initAnalysePage().openInsight(INSIGHT_HAS_TOTAL_VALUE).waitForReportComputing()
                .exportTo(OptionalExportMenu.File.XLSX).getExportXLSXDialog().confirmExport();

            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + INSIGHT_HAS_TOTAL_VALUE + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            takeScreenshot(browser, INSIGHT_HAS_TOTAL_VALUE + " PartialExportAndImport", getClass());
            log.info(INSIGHT_HAS_TOTAL_VALUE + " PartialExportAndImport:"
                    + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList(ATTR_FORECAST_CATEGORY, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
                    asList("Exclude", "4.893263959E7", "16972.8198369754"),
                    asList("Include", "6.769281695E7", "23619.2662072575"),
                    asList("Sum", "1.1662545654E8")));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

    private void exportInsightsAndCheckDataFile(ReportType type, String insight, String metricNegativeValue)
            throws IOException {
        analysisPage.changeReportType(type).waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX)
            .getExportXLSXDialog().confirmExport();
        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + insight + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        takeScreenshot(browser, insight + type, getClass());
        try {
            log.info(insight + type + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList("", metricNegativeValue, metricNegativeValue),
                    asList("Values", "-400000.0", "-400000.0")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(
                insight + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }
}
