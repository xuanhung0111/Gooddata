package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.md.Dataset;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.InsightItem;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.lcm.LcmRestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.CANVAS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.COLORS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.LEGEND;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.X_AXIS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DashboardsDistributedByLcmTest extends AbstractProjectTest {

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;
    protected boolean useK8sExecutor = false;

    private final String FIRST_DASHBOARD = "dashboard_1_" + generateHashString();
    private final String SECOND_DASHBOARD = "dashboard_2_" + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "Client project " + generateHashString();
    private final String FIRST_TEST_INSIGHT = "ATT-Test-Insight1_" + generateHashString();
    private final String SECOND_TEST_INSIGHT = "ATT-Test-Insight2_" + generateHashString();
    private final String MULTI_METRIC_APPLY_COLOR_PALETTE =
            "Multi_Metric_Apply_Color_Palette Via GreyPage" + generateHashString();
    private final String INSIGHT_HAS_VIEW_BY_AND_STACK_BY_APPLY_CUSTOM_COLOR_PICKER =
            "Insight Has View By And Stack By Apply Custom Color Picker" + generateHashString();
    private static List<Pair<String, ColorPalette>> listColorPalettes = Arrays.asList(Pair.of("guid1", ColorPalette.RED),
            Pair.of("guid2", ColorPalette.GREEN), Pair.of("guid3", ColorPalette.BLUE), Pair.of("guid4", ColorPalette.YELLOW));

    private String devProjectId;
    private String clientProjectId;

    private LcmBrickFlowBuilder lcmBrickFlowBuilder;

    @Override
    protected void initProperties() {
        appliedFixture = GOODSALES;
        validateAfterClass = false;
        projectTitle = "KPI Dashboards with LCM";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        String numberKpiUri = createNumOfActivitiesKpi();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getMetricCreator().createAmountMetric();
        getMetricCreator().createBestCaseMetric();
        
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        createInsightToTestCustomColorPicker();
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));

        String insightUri = indigoRestRequest.createInsight(
                new InsightMDConfiguration(FIRST_TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(SECOND_TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        indigoRestRequest.createAnalyticalDashboard(
                asList(numberKpiUri, indigoRestRequest.createVisualizationWidget(
                        insightUri, FIRST_TEST_INSIGHT)), FIRST_DASHBOARD);
        indigoRestRequest.createAnalyticalDashboard(
                asList(numberKpiUri, indigoRestRequest.createVisualizationWidget(
                        insightUri, FIRST_TEST_INSIGHT)), SECOND_DASHBOARD);

        devProjectId = testParams.getProjectId();
        clientProjectId = createProjectUsingFixture(CLIENT_PROJECT_TITLE, ResourceTemplate.GOODSALES,
                testParams.getDomainUser());
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        lcmBrickFlowBuilder.setSegmentId(SEGMENT_ID).setClientId(CLIENT_ID)
                .setDevelopProject(devProjectId).setClientProjects(clientProjectId).buildLcmProjectParameters();

        log.info("------dev project id:" + devProjectId);
        log.info("------client project id:" + clientProjectId);

        testParams.setProjectId(clientProjectId);
        addUsersToClientProject();
    }

    @Test(dependsOnGroups = "createProject")
    public void testSyncLockedFlag() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), devProjectId);

        indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 1);
        indigoRestRequest.setLockedAttribute(SECOND_DASHBOARD, 0);

        int lockedValue = indigoRestRequest.getLockedAttribute(FIRST_DASHBOARD);
        assertEquals(lockedValue, 1, FIRST_DASHBOARD + " should be locked");
        lockedValue = indigoRestRequest.getLockedAttribute(SECOND_DASHBOARD);
        assertEquals(lockedValue, 0, SECOND_DASHBOARD + " should not be locked");

        lcmBrickFlowBuilder.runLcmFlow();

        indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);

        lockedValue = indigoRestRequest.getLockedAttribute(FIRST_DASHBOARD);
        assertEquals(lockedValue, 1, FIRST_DASHBOARD + " should be locked");
        lockedValue = indigoRestRequest.getLockedAttribute(SECOND_DASHBOARD);
        assertEquals(lockedValue, 1, SECOND_DASHBOARD + " should be locked");
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testKpiDashboardsWithAdminRole() {
        logoutAndLoginAs(true, UserRoles.ADMIN);
        initIndigoDashboardsPageWithWidgets();
        indigoDashboardsPage.selectKpiDashboard(FIRST_DASHBOARD);
        assertThat("Admin should be able to edit dashboard", indigoDashboardsPage.isEditButtonVisible());
        indigoDashboardsPage.switchToEditMode();
        indigoDashboardsPage.getInsightSelectionPanel().switchFilter(FilterType.ALL);
        indigoDashboardsPage.addInsight(SECOND_TEST_INSIGHT).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), SECOND_TEST_INSIGHT);
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightsWithAdminRole() {
        AnalysisPage analysisPage = initAnalysePage();
        InsightItem insightItem = analysisPage.getPageHeader()
                .expandInsightSelection().switchFilter(FilterType.ALL)
                .getInsightItem(FIRST_TEST_INSIGHT);
        assertThat("VisualizationsDropdown should have a locked icon", insightItem.hasLockedIcon());
        assertThat("EditorHeader should have a locked icon", analysisPage.openInsight(FIRST_TEST_INSIGHT)
                .getPageHeader().hasLockedIcon());
        analysisPage.getPageHeader().setInsightTitle("new insight");
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), "new insight");
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightWithColorPaletteViaAPI() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .saveInsight(MULTI_METRIC_APPLY_COLOR_PALETTE);
        takeScreenshot(browser, "testInsightWithColorPaletteViaAPI", getClass());
        ChartReport chartReport = analysisPage.openInsight(MULTI_METRIC_APPLY_COLOR_PALETTE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightWithColorPickerConfiguration() {
        setCustomColorPickerFlag(true);
        setExtendedStackingFlag(false);
        try {
            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_VIEW_BY_AND_STACK_BY_APPLY_CUSTOM_COLOR_PICKER)
                    .waitForReportComputing().getChartReport();
            takeScreenshot(browser, "testInsightWithColorPickerConfiguration", getClass());
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(ColorPalette.YELLOW.toString(), ColorPalette.GREEN.toString()));
        }finally {
            setCustomColorPickerFlag(false);
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testDefaultDataProduce() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);
        String identifier = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(FIRST_TEST_INSIGHT))).get(0);
        AnalysisPage analysisPage = initAnalysePage().openInsight(FIRST_TEST_INSIGHT);

        String urlUtilizeClientId = browser.getCurrentUrl().replace(clientProjectId,
                String.format("client/%s:%s", LcmRestUtils.ATT_LCM_DATA_PRODUCT, CLIENT_ID));
        openUrl(urlUtilizeClientId);
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);

        openUrl(format("/analyze/#/client/%s:%s/%s/edit", LcmRestUtils.ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier));
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);

        openUrl(format("/analyze/#/%s/%s/edit", clientProjectId, identifier));
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 1);
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testKpiDashboardsWithEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Editor shouldnot be able to edit dashboard");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightsWithEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            AnalysisPage analysisPage = initAnalysePage();
            InsightItem insightItem = analysisPage.getPageHeader()
                    .expandInsightSelection()
                    .switchFilter(FilterType.ALL)
                    .getInsightItem(SECOND_TEST_INSIGHT);
            assertTrue(insightItem.hasLockedIcon(), "VisualizationsDropdown should have a locked icon");
            assertFalse(insightItem.hasVisibleDeleteButton(), "VisualizationsDropdown should have a locked icon");
            assertTrue(analysisPage.openInsight(SECOND_TEST_INSIGHT).waitForReportComputing()
                    .getPageHeader().hasLockedIcon(), "EditorHeader should have a locked icon");
            assertTrue(analysisPage.getPageHeader().isSaveAsButtonEnabled(), "Editor should able to save as");
            assertFalse(analysisPage.getPageHeader().isInsightTitleEditable(), "Editor should not be able to edit title");
            analysisPage.getPageHeader().saveInsightAs("new insight");
            assertEquals(analysisPage.getPageHeader().getInsightTitle(), "new insight");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testWithViewer() {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Viewer shouldnot be able to edit dashboard");
            indigoDashboardsPage.selectKpiDashboard(SECOND_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Viewer shouldnot be able to edit dashboard");
            assertFalse(ElementUtils.isElementVisible(By.cssSelector(".gd-header-menu-item.s-menu-analyze"), browser),
                    "Viewer should not see AD button");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testUnlockDashboards() throws IOException {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);
        try {
            indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 0);

            logoutAndLoginAs(true, UserRoles.EDITOR);
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertTrue(indigoDashboardsPage.isEditButtonVisible(), "Editor should be able to edit dashboard");

            logoutAndLoginAs(true, UserRoles.VIEWER);
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(FIRST_DASHBOARD);
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Editor should not be able to edit dashboard");
        } finally {
            indigoRestRequest.setLockedAttribute(FIRST_DASHBOARD, 1);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "testSyncLockedFlag")
    public void testInsightAnalyseWithDualAxis() {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), clientProjectId);
        final List<String> itemsConfigurationPanelColumnChart =
                asList(COLORS.toString(), X_AXIS.toString(), Y_AXIS.toString() + " (Left)",
                        Y_AXIS.toString() + " (Right)", LEGEND.toString(), CANVAS.toString());
        final String INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE = "Two measure and attribute";

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        AnalysisPage analysisPage = initAnalysePage();
        MetricsBucket metricsBucket = analysisPage.openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().uncheckShowOnSecondaryAxis();
        analysisPage.waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_BEST_CASE);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(), "Rerender insight should have secondary axis");
        assertEquals(analysisPage.openConfigurationPanelBucket().getItemNames(), itemsConfigurationPanelColumnChart);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }

    private void addUsersToClientProject() throws JSONException, IOException {
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        addUserToProject(testParams.getViewerUser(), UserRoles.VIEWER);
    }

    private String createNumOfActivitiesKpi() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES);
        } catch (ObjNotFoundException e) {
            getMetricCreator().createNumberOfActivitiesMetric();
        }

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getTitle())
                .metric(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
    }

    private Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    private String createKpiUsingRest(final KpiMDConfiguration kpiConfig) {
        String kpiWidget;
        try {
            kpiWidget = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .createKpiWidget(kpiConfig);
        } catch (JSONException | IOException e) {
            throw new RuntimeException("There is error while create Kpi Widget");
        }
        return kpiWidget;
    }

    private String getDateDatasetUri(final String dataset) {
        return getMdService().getObjUri(getProject(), Dataset.class, title(format("Date (%s)", dataset)));
    }

    private void createInsightToTestCustomColorPicker() {
        setCustomColorPickerFlag(true);
        setExtendedStackingFlag(false);
        try {
            createInsightHasAttributeOnStackByAndViewBy(INSIGHT_HAS_VIEW_BY_AND_STACK_BY_APPLY_CUSTOM_COLOR_PICKER,
                    METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            AnalysisPage analysisPage = initAnalysePage()
                    .openInsight(INSIGHT_HAS_VIEW_BY_AND_STACK_BY_APPLY_CUSTOM_COLOR_PICKER).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .openCustomColorPalette().getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            analysisPage.waitForReportComputing().saveInsight();
        } finally {
            setCustomColorPickerFlag(false);
            setExtendedStackingFlag(true);
        }
    }

    private void createInsightHasAttributeOnStackByAndViewBy(String title, String metric, String attribute, String stack) {
        new IndigoRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId()).createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private void setCustomColorPickerFlag(boolean status) {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }

    private void setExtendedStackingFlag(boolean status) {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXTENDED_STACKING, status);
    }
}
