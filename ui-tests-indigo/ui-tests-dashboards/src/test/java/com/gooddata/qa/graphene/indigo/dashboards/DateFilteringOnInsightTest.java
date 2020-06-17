package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT ;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.Sleeper;

public class DateFilteringOnInsightTest extends AbstractDashboardTest {

    private static String SNAPSHOT = "Snapshot";
    private static String DATE_SNAPSHOT = "Date (Snapshot)";
    private static String DATE_SNAPSHOT_RENAMED = "Date (Snapshot)_Renamed";
    private final String TEST_INSIGHT = "Test-Insight" + generateHashString();
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Filtering-On-Insight-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        // creating insight which is set date filter via REST is not recommended in these tests
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(TEST_INSIGHT)
                .waitForWidgetsLoading();
        Graphene.waitGui().until(browser -> !indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet().contains("is-loading"));
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void rememberLastSettingAfterSelectingAnotherInsight()
            throws ParseException, JSONException, IOException {
        String anotherInsight = "Another-Insight" + generateHashString();
        String anotherInsightUri = createInsightWidget(
                new InsightMDConfiguration(anotherInsight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_LOST_OPPS)))));

        addWidgetToWorkingDashboardFluidLayout(anotherInsightUri, 0);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .selectWidgetByHeadline(Insight.class,
                    TEST_INSIGHT);

            indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_CREATED);
            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(),
                    DATE_DATASET_CREATED);

            assertEquals(indigoDashboardsPage.selectWidgetByHeadline(Insight.class, anotherInsight).getHeadline(),
                    anotherInsight);

            indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_CREATED);

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(anotherInsightUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableDateFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);

        ConfigurationPanel panel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        assertTrue(panel.isDateDataSetDropdownVisible(), "Default state of date filter is not enabled");

        panel.disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                "State of date filter is not disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateFilterStatusWithEditor() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode()
                    .selectWidgetByHeadline(Insight.class, TEST_INSIGHT);

            ConfigurationPanel panel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
            assertTrue(panel.getFilterByDateFilter().isChecked(), "Default state of date filter item is not checked");

            panel.disableDateFilter();

            indigoDashboardsPage.cancelEditModeWithChanges().switchToEditMode()
                    .selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
            assertTrue(panel.getFilterByDateFilter().isChecked(),
                    "Default state of date filter item is not checked");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enableDateFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);

        ConfigurationPanel panel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                "Date filter is not disabled");

        panel.enableDateFilter();
        assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                "Date filter is not enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeFilterOnAddedInsight() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);
        indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_CREATED);
        takeScreenshot(browser, "Change-Filter-On-Added-Insight", getClass());
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_CREATED,
                "Date fillter is not applied");

        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Insight.class, TEST_INSIGHT)
            .getChartReport().getDataLabels(), singletonList("154,271"), "Chart renders incorrectly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void rememberDisabledDateFilterAfterSaving() throws JSONException, IOException {
        String insight = "Disabled-Date-Filter-Insight" + generateHashString();
        String insightUri = indigoRestRequest.createInsight(
                new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsightNext(insight).waitForWidgetsLoading()
                .getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                "Date filter is not disabled");
        indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, insight);
            assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                    "Date filter is not disabled");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(insightUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void rememberEnabledDateFilterAfterSaving() throws JSONException, IOException {
        String insight = "Enabled-Date-Filter-Insight" + generateHashString();
        String insightUri = indigoRestRequest.createInsight(
                new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsightNext(insight).waitForWidgetsLoading();
        // this test should not depend on default state of date filter
        indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().disableDateFilter().enableDateFilter();
        assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(), "Date filter is not enabled");
        indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, insight);
            assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetDropdownVisible(),
                    "Date filter is not enabled");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(insightUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void rememberModifiedDateFilterAfterSaving() throws JSONException, IOException {
        String widgetUri = createInsightWidget(new InsightMDConfiguration("Modified-Date-Filter-Insight",
                ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        addWidgetToWorkingDashboardFluidLayout(widgetUri, 0);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().enableDateFilter().selectDateDataSetByName(DATE_DATASET_CREATED);

            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(),
                    DATE_DATASET_CREATED);

            indigoDashboardsPage.saveEditModeWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_CREATED);
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(widgetUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyRenamedDateOnDateFilterInsight() throws JSONException, IOException {
        // currently, we can't create a new date value, so we will use 1 existing date for tests
        // which need to modify date in order to narrow affected area
        String widgetUri = createInsightWidget(new InsightMDConfiguration("Renamed-Date-Filter-Insight",
                ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        addWidgetToWorkingDashboardFluidLayout(widgetUri, 0);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            takeScreenshot(browser, "123456", getClass());
            indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().enableDateFilter().selectDateDataSetByName(SNAPSHOT);
            indigoDashboardsPage.saveEditModeWithWidgets();

            initManagePage();
            ObjectsTable.getInstance(By.id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser)
                    .selectObject(DATE_SNAPSHOT);

            DatasetDetailPage.getInstance(browser).changeName(DATE_SNAPSHOT_RENAMED);
            Sleeper.sleepTightInSeconds(1);

            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            ConfigurationPanel panel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
            takeScreenshot(browser, "Renamed-Date-Filter-Test", getClass());
            assertEquals(panel.getSelectedDataSet(), DATE_SNAPSHOT_RENAMED,
                    "New name is not applied to the insight");

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(widgetUri);
        }
    }

    @Test(dependsOnMethods = "applyRenamedDateOnDateFilterInsight")
    public void deleteDateUsedOnDateFilterInsight() throws JSONException, IOException {
        // currently, we can't create a new date value, so we will use 1 existing date for tests
        // which need to modified date in order to narrow affected area
        String insight = "Insight-Containing-Date-Filter" + generateHashString();
        String widgetUri =
                createInsightWidget(new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        addWidgetToWorkingDashboardFluidLayout(widgetUri, 0);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().enableDateFilter()
                    .selectDateDataSetByName(DATE_SNAPSHOT_RENAMED);
            indigoDashboardsPage.saveEditModeWithWidgets();

            initManagePage();
            ObjectsTable.getInstance(By.id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser)
                    .selectObject(DATE_SNAPSHOT_RENAMED);

            DatasetDetailPage.getInstance(browser).deleteObject();

            assertFalse(indigoRestRequest.getInsightWidgetTitles()
                    .contains(insight), "The expected insight has not been deleted");
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(widgetUri);
        }
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }
}
