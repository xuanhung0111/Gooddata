package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ScheduleEmailDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertEquals;

public class InternationalDateFormatTest extends AbstractAnalyseTest {

    private final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private final String NEW_PROJECT_NAME = "Default-Date-Format-Project" + generateHashString();
    private final String INSIGHT_NAME_HAVE_FILTER_BY_DATE = "Insight" + generateHashString();
    private final String INSIGHT_NAME = "Insight" + generateHashString();
    private final String DRILL_INSIGHT_NAME = "Drill-Insight" + generateHashString();

    private String sourceProjectId;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "International Date Format";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();

        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        // TODO: BB-1675 enableNewADFilterBar FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_MEASURE_VALUE_FILTERS, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, false);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);

        sourceProjectId = testParams.getProjectId();
        createProjectUsingFixture(NEW_PROJECT_NAME, GOODSALES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareInsights() {
        projectRestRequest.createProjectConfiguration(
            ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), DEFAULT_DATE_FORMAT);

        // Creating insight which have filter by date of metric
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CREATED, "01/02/2008", "03/04/2008");

        FiltersBucket filterBucket = analysisPage.addDateFilter().getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper("01/02/2008", "03/20/2008").apply();

        AttributesBucket attributesBucket = analysisPage.addDate().getAttributesBucket();
        attributesBucket.changeGranularity(DateGranularity.DAY);
        attributesBucket.changeDateDimension(DATE_DATASET_CREATED);
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_NAME_HAVE_FILTER_BY_DATE).saveInsightAs(DRILL_INSIGHT_NAME);

        // Creating insight which doesn't have filter by date of metric
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT);
        filterBucket = analysisPage.addDateFilter().getFilterBuckets();
        dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper("01/02/2008", "03/20/2008").apply();

        attributesBucket = analysisPage.addDate().getAttributesBucket();
        attributesBucket.changeGranularity(DateGranularity.DAY);
        attributesBucket.changeDateDimension(DATE_DATASET_CREATED);
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_NAME);
    }

    @DataProvider(name = "getDateFormats")
    public Object[][] getDateFormats() {
        return new Object[][]{
            {"MM/dd/yyyy", new String[]{"02/01/2008", "03/04/2008"}},
            {"dd.MM.yyyy", new String[]{"01.02.2008", "04.03.2008"}},
            {"dd/MM/yyyy", new String[]{"01/02/2008", "04/03/2008"}},
            {"dd-MM-yyyy", new String[]{"01-02-2008", "04-03-2008"}},
            {"yyyy-MM-dd", new String[]{"2008-02-01", "2008-03-04"}},
            {"M/d/yy",  new String[]{"2/1/08", "3/4/08"}},
        };
    }

    @Test(dependsOnMethods = {"prepareInsights"}, dataProvider = "getDateFormats")
    public void checkInternationalDateFormatsOnADPage(String dateFormats, String[] expectedResult) throws IOException {
        try {
            projectRestRequest.updateProjectConfiguration(
                ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), dateFormats);
            PivotTableReport pivotTableReport = initAnalysePage().changeReportType(ReportType.TABLE)
                .openInsight(INSIGHT_NAME_HAVE_FILTER_BY_DATE).waitForReportComputing().getPivotTableReport();
            assertThat(pivotTableReport.getAttributeValuePresent(), hasItems(expectedResult));

            AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
            attributesBucket.changeGranularity(DateGranularity.MONTH);
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getAttributeValuePresent(), not(hasItems(expectedResult)));

            attributesBucket.changeGranularity(DateGranularity.DAY);
            analysisPage.waitForReportComputing();

            asList(ReportType.BAR_CHART, ReportType.LINE_CHART, ReportType.STACKED_AREA_CHART, ReportType.COMBO_CHART,
                ReportType.BULLET_CHART).stream().forEach(reportType -> {
                log.info(dateFormats + " Date Formats With " + reportType);
                assertThat(analysisPage.changeReportType(reportType).getChartReport()
                    .getXaxisLabels(), hasItems(expectedResult));
            });
        } finally {
            projectRestRequest.updateProjectConfiguration(
                ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), DEFAULT_DATE_FORMAT);
        }
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void exportDateFormatsOnKDPage() throws IOException {
        try {
            final String dashboard = generateDashboardName();
            String currentDate = getCurrentDate("yyyy-MM-dd");
            projectRestRequest.updateProjectConfiguration(ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), "yyyy-MM-dd");

            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .changeDashboardTitle(dashboard).addInsight(INSIGHT_NAME);
            indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("2008-02-01", "2008-03-04")
                .apply();
            indigoDashboardsPage.saveEditModeWithWidgets();

            indigoDashboardsPage.exportDashboardToPDF();
            List<String> contents = asList(getContentFrom(dashboard).split("\n"));
            assertThat(contents, hasItems("2008-02-01" + " $4,000.00", "2008-03-04" + " $20,000.00"));

            ScheduleEmailDialog scheduleEmailDialog = indigoDashboardsPage.scheduleEmailing();
            assertEquals(scheduleEmailDialog.getDateText(), currentDate);

            String dateAttachment = currentDate.replaceAll("/", "-");
            assertThat(scheduleEmailDialog.getSubjectDefaultPlaceHolder(), containsString(dateAttachment));
            assertThat(scheduleEmailDialog.getDefaultAttachmentNameText(), containsString(dateAttachment));
        } finally {
            projectRestRequest.updateProjectConfiguration(
                ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), DEFAULT_DATE_FORMAT);
        }
    }

    @Test(dependsOnMethods = {"exportDateFormatsOnKDPage"})
    public void drillToInsightOnKDPage() throws IOException {
        try {
            projectRestRequest.updateProjectConfiguration(ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), "M/d/yy");

            final String dashboard = generateDashboardName();
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .changeDashboardTitle(dashboard).addInsight(DRILL_INSIGHT_NAME).addInsightNext(DRILL_INSIGHT_NAME);
            indigoDashboardsPage.selectFirstWidget(Insight.class);
            ConfigurationPanel configurationPanel =  indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
            configurationPanel.drillIntoInsight("Amount, Created: Jan 2, 2008 - Mar 4, 2008", DRILL_INSIGHT_NAME);;
            indigoDashboardsPage.saveEditModeWithWidgets();
            indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport().drillOnCellMeasure("$4,000.00");
            indigoDashboardsPage.waitForDrillModalDialogLoading();

            DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
            PivotTableReport drillPivotTableReport = drillModalDialog.getPivotTableReport();
            assertThat(drillPivotTableReport.getAttributeValuePresent(), hasItems("2/1/08"));

            drillModalDialog.close();
        } finally {
            projectRestRequest.updateProjectConfiguration(ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), DEFAULT_DATE_FORMAT);
        }
    }

    @Test(dependsOnMethods = {"drillToInsightOnKDPage"})
    public void testADOverlayOnKDPage() throws IOException {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, true);
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, true);
            projectRestRequest.updateProjectConfiguration(
                ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), "M/d/yy");

            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_NAME)
                .saveEditModeWithWidgets().selectDateFilterByName("All time").waitForWidgetsLoading();
            AnalysisPage analysisPage = indigoDashboardsPage.selectFirstWidget(Insight.class).exploreInsight();

            assertThat(analysisPage.getPivotTableReport().getAttributeValuePresent(), hasItems("2/1/08"));

            analysisPage.cancelEditFromKD();
            indigoDashboardsPage.switchToEditMode().selectFirstWidget(Insight.class);
            analysisPage = indigoDashboardsPage.selectFirstWidget(Insight.class).editInsight();

            assertThat(analysisPage.getPivotTableReport().getAttributeValuePresent(), hasItems("2/1/08"));
        }finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, false);
        }
    }

    @Test(dependsOnMethods = {"testADOverlayOnKDPage"})
    public void checkChangingValueFeatureFlagOfUserLevel() throws IOException {
        // Changing Date Format of current project
        projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(Profile.ADMIN)), sourceProjectId);
        projectRestRequest.createProjectConfiguration(
            ProjectFeatureFlags.RESPONSIVE_UI_DATE_FORMAT.getFlagName(), "M/d/yy");

        // Changing International Date Format of Dynamic user
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(new RestClient(
            new RestClient.RestProfile(testParams.getHost(), testParams.getUser(), testParams.getPassword(), true)),
            sourceProjectId);
        userManagementRestRequest.changeValueInternationalDateFormatOfUser("M/d/yy");

        // Login with dynamic user
        signInAtGreyPages(testParams.getUser(), testParams.getPassword());

        // Checking apply International Date Format for current project
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT);
        AttributesBucket attributesBucket = analysisPage.addDate().getAttributesBucket();
        attributesBucket.changeGranularity(DateGranularity.DAY);
        attributesBucket.changeDateDimension(DATE_DATASET_CREATED);
        assertThat(analysisPage.getPivotTableReport().getAttributeValuePresent(), hasItems("2/1/08"));
        analysisPage.saveInsight("Insight " + generateHashString());

        // Checking apply International Date Format for new default project
        analysisPage.switchProject(NEW_PROJECT_NAME);
        waitForFragmentVisible(analysisPage);

        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addDate();
        attributesBucket.changeGranularity(DateGranularity.DAY);
        attributesBucket.changeDateDimension(DATE_DATASET_CREATED);
        assertThat(analysisPage.getPivotTableReport().getAttributeValuePresent(), hasItems("2/1/08"));
    }

    private String generateDashboardName() {
        return "Dashboard-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String getCurrentDate(String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(new Date());
    }
}
