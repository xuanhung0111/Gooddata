package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.FilterAttribute;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;

import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog.OptionalExport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;

import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ExportDataToXLSXAndCSVDashboardsAdvancedTest extends AbstractDashboardTest {

    private final String INSIGHT_HAS_CONFIG_GLOBAL_FILTER = "Insight Config Global";
    private final String INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC = "Insight Under Metric";
    private final String INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE = "Local Attribute And Global Date";
    private final String INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE = "Global Attribute And Local Date";
    private final String INSIGHT_HAS_NO_OR_INVALID_DATA = "No or invalid data";
    private final String INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA = "Not computed data";
    private final String INSIGHT_HAS_CONTAINS_RESTRICTED_DATA = "Restricted data";
    private static final String INSIGHT_EXPORTED_CSV = "CSV";
    private static final String INSIGHT_EXPORTED_XLSX = "XLSX";

    private String sourceProjectId;
    private String targetProjectId;
    private IndigoRestRequest indigoRestRequest;
    private FactRestRequest factRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();

        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, false);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, false);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.EDITOR_AND_INVITATIONS);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
        createAndAddUserToProject(UserRoles.EXPLORER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createInsight(INSIGHT_HAS_CONFIG_GLOBAL_FILTER, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            singletonList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)), singletonList(ATTR_DEPARTMENT));

        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_CONFIG_GLOBAL_FILTER).addDate().waitForReportComputing().getFilterBuckets()
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales").configDateFilter("01/01/2012", "01/01/2013");

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();
        analysisPage.saveInsight().waitForReportComputing();

        createInsight(INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            singletonList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)), singletonList(ATTR_DEPARTMENT));

        initAnalysePage().openInsight(INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC).addDate().waitForReportComputing()
            .getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilter(ATTR_DEPARTMENT, "Direct Sales")
            .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2012", "01/01/2013");
        analysisPage.saveInsight().waitForReportComputing();

        createInsight(INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            singletonList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)), singletonList(ATTR_DEPARTMENT));

        initAnalysePage().openInsight(INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE).addDate()
            .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration().addFilter(ATTR_DEPARTMENT, "Direct Sales");

        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter()).configTimeFilter("01/01/2012", "01/01/2013");
        analysisPage.saveInsight().waitForReportComputing();

        createInsight(INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE, ReportType.TABLE, singletonList(METRIC_AMOUNT),
            singletonList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)), singletonList(ATTR_DEPARTMENT));

        initAnalysePage().openInsight(INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE).addDate()
            .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, "01/01/2012", "01/01/2013");

        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.saveInsight().waitForReportComputing();

        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)));
        final String metricNullValue = "EMPTY_SHOW_NULL_STRING";
        createMetric(metricNullValue, metricExpression, "#'##0,00 formatted; [=null] null value!");

        createInsightHasOnlyMetric(INSIGHT_HAS_NO_OR_INVALID_DATA, ReportType.TABLE, singletonList(metricNullValue));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void exportInsightsIntoXLSXFormatWithGlobalFilterBar() throws IOException {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONFIG_GLOBAL_FILTER).waitForWidgetsLoading()
            .saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectStaticPeriod("01/01/2012", "01/01/2013")
            .apply();
        indigoDashboardsPage.waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(OptionalExportMenu.File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(OptionalExport.FILTERS_CONTEXT).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_HAS_CONFIG_GLOBAL_FILTER + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info(INSIGHT_HAS_CONFIG_GLOBAL_FILTER + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(asList("Applied filters:", "Date (Closed) BETWEEN 01/01/2012 AND 01/01/2013"),
                asList("Department IN (Direct Sales)"),
                asList("Department", "Year (Closed)", "Amount - SP year ago", "Amount"),
                asList("Direct Sales", "2012", "4.010598396E7", "2.517027098E7"),
                asList("2013", "10584.41")));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void exportInsightsIntoXLSXFormatWithFilterUnderMetric() throws IOException {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC).waitForWidgetsLoading()
            .saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectStaticPeriod("01/01/2012", "01/01/2013")
            .apply();
        indigoDashboardsPage.waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(OptionalExportMenu.File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(OptionalExport.FILTERS_CONTEXT).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info(INSIGHT_HAS_CONFIG_FILTER_UNDER_METRIC + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(
                asList("Department", "Year (Closed)", "Amount, Closed: Jan 1, 2012 - Jan 1, 2013 " +
                    "(Department: Direct Sales)"),
                asList("Direct Sales", "2012", "2.517027098E7")));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void exportInsightsIntoXLSXBetweenLocalAttributeAndGlobalDateFilter() throws IOException {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE).waitForWidgetsLoading()
            .saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectStaticPeriod("01/01/2012", "01/01/2013")
            .apply();
        indigoDashboardsPage.waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(OptionalExportMenu.File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(OptionalExport.FILTERS_CONTEXT).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info(INSIGHT_HAS_CONFIG_LOCAL_ATTRIBUTE_AND_GLOBAL_DATE + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(asList("Applied filters:", "Date (Closed) BETWEEN 01/01/2012 AND 01/01/2013"),
                asList(ATTR_DEPARTMENT, "Year (Closed)", "Amount (Department: Direct Sales)"),
                asList("Direct Sales", "2012", "2.517027098E7")));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void exportInsightsIntoXLSXBetweenLocalDateMetricAndGlobalAttributeFilter() throws IOException {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE).waitForWidgetsLoading()
            .saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectStaticPeriod("01/01/2012", "01/01/2013")
            .apply();
        indigoDashboardsPage.waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(OptionalExportMenu.File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(OptionalExport.FILTERS_CONTEXT).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info(INSIGHT_HAS_CONFIG_GLOBAL_ATTRIBUTE_AND_LOCAL_DATE + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(
                asList("Applied filters:", "Department IN (Direct Sales)"),
                asList("Department", "Year (Closed)", "Amount, Closed: Jan 1, 2012 - Jan 1, 2013"),
                asList("Direct Sales", "2012", "2.517027098E7")));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void disableExportInsightForNoDataOrInvalidData() {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_NO_OR_INVALID_DATA)
            .saveEditModeWithWidgets().selectDateFilterByName("All time").waitForWidgetsLoading();
        OptionalExportMenu optionalExport = indigoDashboardsPage.selectFirstWidget(Insight.class).openOptionsMenu();

        assertFalse(optionalExport.isExportToButtonEnabled(OptionalExportMenu.File.CSV),
            "Export to CSV options should be disabled");
        assertFalse(optionalExport.isExportToButtonEnabled(OptionalExportMenu.File.XLSX),
            "Export to XLSX options should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightContainsNotComputeData() {
        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
            MetricFormatterDialog.Formatter.DEFAULT.toString());

        createInsightHasOnlyMetric(INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA, ReportType.TABLE,
            asList(metricNegativeValue, metricNegativeValue));

        Stream.of(ReportType.PIE_CHART, ReportType.DONUT_CHART, ReportType.TREE_MAP)
            .forEach(type -> {
                try {
                    initAnalysePage().openInsight(INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA)
                        .changeReportType(type).saveInsight().waitForReportComputing();

                    initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA)
                        .saveEditModeWithWidgets().selectDateFilterByName("All time").waitForWidgetsLoading();
                    indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.XLSX);
                    ExportXLSXDialog.getInstance(browser).confirmExport();

                    final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                        + INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA + "." + ExportFormat.EXCEL_XLSX.getName());

                    waitForExporting(exportFile);

                    log.info(INSIGHT_HAS_CONTAINS_NOT_COMPUTED_DATA + ":"
                        + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
                    assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                        asList(asList("", metricNegativeValue, metricNegativeValue),
                            asList("Values", "-400000.0", "-400000.0")));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void exportInsightContainsRestrictedData() {
        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        try {
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
                .saveInsight(INSIGHT_HAS_CONTAINS_RESTRICTED_DATA).waitForReportComputing();

            initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONTAINS_RESTRICTED_DATA)
                .saveEditModeWithWidgets().selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).confirmExport();

            assertEquals(ElementUtils.getErrorMessage(browser),
                "You cannot export this insight because it contains restricted data.");
        } finally {
            factRestRequest.unsetFactRestricted(factUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportInsightContainsProtectedDataWithEditorRole() throws IOException {
        String attributeUri = factRestRequest.getAttributeByTitle(ATTR_STAGE_NAME).getUri();
        addUsersWithOtherRolesToProject();
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            factRestRequest.setFactProtected(attributeUri);
            initAnalysePage().changeReportType(ReportType.TABLE)
                .addAttribute(ATTR_STAGE_NAME).saveInsight(INSIGHT_HAS_CONTAINS_RESTRICTED_DATA)
                .waitForReportComputing();

            initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_CONTAINS_RESTRICTED_DATA)
                .saveEditModeWithWidgets().selectDateFilterByName("All time").waitForWidgetsLoading();
            OptionalExportMenu optionalExport = indigoDashboardsPage.selectFirstWidget(Insight.class).openOptionsMenu();

            assertFalse(optionalExport.isExportToButtonEnabled(OptionalExportMenu.File.XLSX),
                "Export to XLSX options should be disabled");
            assertFalse(optionalExport.isExportToButtonEnabled(OptionalExportMenu.File.CSV),
                "Export to CSV options should be disabled");
        } finally {
            factRestRequest.unsetFactProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getRolesUserAndPrepareInsight")
    public void exportCSVInsightWithAllRoles(UserRoles userRole) throws IOException {
        try {
            logoutAndLoginAs(true, userRole);

            initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_EXPORTED_CSV).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.CSV);

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder()
                + testParams.getFolderSeparator() + INSIGHT_EXPORTED_CSV + "." + ExportFormat.CSV.getName());

            waitForExporting(exportFile);
            log.info(INSIGHT_EXPORTED_CSV + ":" + CSVUtils.readCsvFile(exportFile));
            assertEquals(CSVUtils.readCsvFile(exportFile), asList(
                asList(null, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP),
                asList("Values", "116625456.54", "20286.2161315011", "5134397.65")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(INSIGHT_EXPORTED_CSV + "."
                + ExportFormat.CSV.getName())));
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getRolesUserAndPrepareInsight")
    public void exportXLSXInsightWithAllRoles(UserRoles userRole) throws IOException {
        try {
            logoutAndLoginAs(true, userRole);

            initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_EXPORTED_XLSX).saveEditModeWithWidgets()
                .selectDateFilterByName("All time").waitForWidgetsLoading();
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.uncheckOption(OptionalExport.CELL_MERGED).uncheckOption(OptionalExport.FILTERS_CONTEXT)
                .confirmExport();

            final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder()
                + testParams.getFolderSeparator() + INSIGHT_EXPORTED_XLSX + "." + ExportFormat.EXCEL_XLSX.getName());

            waitForExporting(exportFile);
            log.info(INSIGHT_EXPORTED_XLSX + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
                asList(asList("", METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP),
                    asList("Values", "1.1662545654E8", "20286.2161315011", "5134397.65")));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(INSIGHT_EXPORTED_XLSX + "."
                + ExportFormat.EXCEL_XLSX.getName())));
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "getRolesUserAndPrepareInsight")
    private Object[][] getRolesUserAndPrepareInsight() {
        createInsightHasOnlyMetric(INSIGHT_EXPORTED_CSV, ReportType.TABLE, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT,
            METRIC_AMOUNT_BOP));
        createInsightHasOnlyMetric(INSIGHT_EXPORTED_XLSX, ReportType.TABLE, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT,
            METRIC_AMOUNT_BOP));

        return new Object[][]{
            {UserRoles.EDITOR},
            {UserRoles.EDITOR_AND_INVITATIONS},
            {UserRoles.EDITOR_AND_USER_ADMIN},
            {UserRoles.EXPLORER},
        };
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType
        reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList())));
    }

    private String createInsight(String insightTitle, ReportType reportType, List<String> metricsTitle,
                                 List<Pair<String, CategoryBucket.Type>> attributeConfigurations,
                                 List<String> filterAttributes) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList()))
                .setFilter(filterAttributes.stream()
                    .map(filter -> FilterAttribute.createFilter(getAttributeByTitle(filter)))
                    .collect(toList())));
    }
}
