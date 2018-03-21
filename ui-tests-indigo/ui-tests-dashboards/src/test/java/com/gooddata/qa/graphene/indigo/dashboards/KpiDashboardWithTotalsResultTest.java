package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.entity.visualization.TotalsBucket;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationItem;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KpiDashboardWithTotalsResultTest extends AbstractDashboardTest {

    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Insight has attribute and measure";
    private static final String INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE = "Insight has date attribute and measure";
    private static final String INSIGHT_IS_UPLOADED_DATA = "Insight is uploaded data";
    private static final String INSIGHT_IS_ON_MOBILE = "Insight is on mobile";
    private static final String KPI_DASHBOARD = "KPI dashboard";
    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";

    private String insight;
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Totals-Result-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = "createProject", groups = {"desktop", "mobile"})
    public void prepareInsights() throws IOException {
        insight = createSimpleInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE,
            METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT, AggregationItem.MAX);
        createSimpleInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE,
            METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY, AggregationItem.MAX);

        MeasureBucket measureBucket = MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES));
        CategoryBucket categoryBucket = CategoryBucket.createAttributeByBucket(getAttribute(ATTR_DEPARTMENT));
        TotalsBucket totalsBucket = TotalsBucket.createTotals(measureBucket, categoryBucket, AggregationItem.MAX);
        String insightWidget = createInsightWidget(new InsightMDConfiguration(INSIGHT_IS_ON_MOBILE, ReportType.TABLE)
            .setMeasureBucket(singletonList(measureBucket))
            .setCategoryBucket(singletonList(categoryBucket))
            .setTotalsBucket(singletonList(totalsBucket)));
        indigoRestRequest.addTotalResults(INSIGHT_IS_ON_MOBILE, singletonList(totalsBucket));

        indigoRestRequest.createAnalyticalDashboard(singletonList(insightWidget), "KPI Dashboard Mobile");
    }

    @Test(dependsOnMethods = "prepareInsights", groups = "desktop")
    public void placeTableHasTotalsOnKD() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage()
            .addDashboard()
            .addInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
            .changeDashboardTitle(KPI_DASHBOARD);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.addInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE).getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading().addAttributeFilter(ATTR_DEPARTMENT).getAttributeFiltersPanel()
            .getLastFilter().clearAllCheckedValues().selectByNames("Direct Sales");

        CheckUtils.checkRedBar(browser);

        TableReport tableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getTableReport();
        Screenshots.takeScreenshot(browser, "place tables have totals on KD", getClass());

        assertTrue(tableReport.hasTotalsResult(), "Totals result should be rendered as well");
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");

        tableReport = indigoDashboardsPage.getLastWidget(Insight.class).getTableReport();
        assertTrue(tableReport.hasTotalsResult(), "Totals result should be rendered as well");
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "47,459");
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"placeTableHasTotalsOnKD"}, groups = "desktop")
    public void exportAndImportProject() throws Throwable {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(true, true, true, statusPollingCheckIterations);
        String workingProjectId = testParams.getProjectId();
        String targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE");

        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard(KPI_DASHBOARD).waitForWidgetsLoading();
            Screenshots.takeScreenshot(browser, "export import project has totals result in " + KPI_DASHBOARD, getClass());
            assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getTableReport()
                .getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
            assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getTableReport()
                .getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "47,459");
        } finally {
            testParams.setProjectId(workingProjectId);
        }
    }

    @Test(dependsOnMethods = {"prepareInsights"}, groups = "desktop")
    public void partialExportAndImportInsight() throws Throwable {
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
        String workingProjectId = testParams.getProjectId();
        String targetProjectId = createProjectUsingFixture("Copy of " + projectTitle, ResourceManagement.ResourceTemplate.GOODSALES);

        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
            Screenshots.takeScreenshot(browser, "partial export import project has totals result in " +
                INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, getClass());
            assertEquals(analysisPage.getTableReport().getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
        } finally {
            testParams.setProjectId(workingProjectId);
        }
    }

    @Test(dependsOnGroups = "createProject", groups = "desktop")
    public void uploadDataTest() throws IOException {
        CsvFile payroll = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));

        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        getMdService().createObj(getProject(),
            new Metric(METRIC_AMOUNT, format("SELECT SUM([%s])", getFactByIdentifier("fact.csv_payroll.amount").getUri()),
            DEFAULT_CURRENCY_METRIC_FORMAT));
        createSimpleInsight(INSIGHT_IS_UPLOADED_DATA, METRIC_AMOUNT, "Education", AggregationItem.MIN);

        //Edit Payroll data and upload
        payroll
            .rows("Wood", "Alfredo", "Partial College", "Store", "Ranch Foodz", "Texas", "Austin", "2007-12-01", "10000")
            .saveToDisc(testParams.getCsvFolder());

        initDataUploadPage()
            .getMyDatasetsTable()
            .getDataset(payroll.getDatasetNameOfFirstUpload())
            .clickUpdateButton()
            .pickCsvFile(payroll.getFilePath())
            .clickUploadButton();
        DataPreviewPage.getInstance(browser).triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        //Check updated
        TableReport tableReport = initAnalysePage().openInsight(INSIGHT_IS_UPLOADED_DATA).getTableReport();
        takeScreenshot(browser, "uploaded payroll dataset", getClass());
        assertEquals(tableReport.getTotalsValue(AggregationItem.MIN, METRIC_AMOUNT), "$10,000.00");
    }

    @Test(dependsOnMethods = "prepareInsights", groups = "mobile")
    public void checkKpiDashboardHasTotalsResultOnMobile() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().selectKpiDashboard("KPI Dashboard Mobile");

        TableReport tableReport = indigoDashboardsPage.waitForAlertsLoaded().getLastWidget(Insight.class).getTableReport();
        Screenshots.takeScreenshot(browser, "check Kpi dashboard has totals results on mobile", getClass());
        assertTrue(tableReport.hasTotalsResult(), "Totals Result should be displayed");
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");
    }

    private String createSimpleInsight(String title, String metric, String attribute, AggregationItem type) throws IOException {
        MeasureBucket measureBucket = MeasureBucket.createSimpleMeasureBucket(getMetric(metric));
        CategoryBucket categoryBucket = CategoryBucket.createAttributeByBucket(getAttribute(attribute));
        TotalsBucket totalsBucket = TotalsBucket.createTotals(measureBucket, categoryBucket, type);
        indigoRestRequest.createInsight(new InsightMDConfiguration(title, ReportType.TABLE)
                .setMeasureBucket(singletonList(measureBucket))
                .setCategoryBucket(singletonList(categoryBucket))
                .setTotalsBucket(singletonList(totalsBucket)));
        return indigoRestRequest.addTotalResults(title, singletonList(totalsBucket));
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    private Attribute getAttribute(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title));
    }
}
