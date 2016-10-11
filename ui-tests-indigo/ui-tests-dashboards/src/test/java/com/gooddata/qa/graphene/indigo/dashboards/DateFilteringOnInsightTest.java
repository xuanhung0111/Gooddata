package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static  com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT ;
import static  com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static  com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createInsight;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascade;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getInsightWidgetTitles;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascade;
import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class DateFilteringOnInsightTest extends GoodSalesAbstractDashboardTest {

    private static String ALL_TIME = "All time";
    private static String SNAPSHOT = "Snapshot";
    private static String DATE_SNAPSHOT = "Date (Snapshot)";
    private static String DATE_SNAPSHOT_RENAMED = "Date (Snapshot)_Renamed";
    private static String TEST_INSIGHT = "Test-Insight";
    private static String NON_RELATED_DATE_INSIGHT = "Non-Related-Date-Insight";
    private static String LARGE_INSIGHT = "Large-Insight";
    private static String EMPTY_INSIGHT = "Empty-Insight";
    private static String WITHOUT_METRIC_INSIGHT = "Without-Metric-Insight";
    private static String INCOMPUTED_INSIGHT = "Incomputed-Insight";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "Date-Filtering-On-Insight-Test";
    }

    @Override
    protected void prepareSetupProject() throws Throwable {
        createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        // creating insight which is set date filter via REST is not recommended in these tests
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(TEST_INSIGHT)
                .waitForWidgetsLoading().saveEditModeWithWidgets();
    }

    @DataProvider
    public Object[][] defaultFilterStateProvider() throws ParseException, JSONException, IOException {
        return new Object[][] {
                {INCOMPUTED_INSIGHT, ALL_TIME, false, createIncomputedReport()},
                {NON_RELATED_DATE_INSIGHT, ALL_TIME, false, createUnrelatedDateInsight()},
                {LARGE_INSIGHT, ALL_TIME, true, createLargeInsight()},
                {WITHOUT_METRIC_INSIGHT, ALL_TIME, true, createTableInsight()},
                {EMPTY_INSIGHT, "Last year", true, createEmptyInsight()}
        };
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, dataProvider = "defaultFilterStateProvider")
    public void testDefaultDateFilterState(String insight, String dateValue, boolean expectedState,
            String insightUri) throws JSONException, IOException {
        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectDateFilterByName(dateValue)
                    .waitForWidgetsLoading().addInsight(insight).waitForWidgetsLoading();
            ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
            takeScreenshot(browser, "default-date-filter-state-test-" + insight.toUpperCase(), getClass());
            assertEquals(panel.isDateFilterCheckboxEnabled(), expectedState, "Date filter state is not correct");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), insightUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void rememberLastSettingAfterSelectingAnotherInsight()
            throws ParseException, JSONException, IOException {
        String anotherInsight = "Another-Insight";
        String anotherInsightUri = createInsightWidget(
                new InsightMDConfiguration(anotherInsight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_LOST_OPPS)))));

        addWidgetToWorkingDashboard(anotherInsightUri);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                    TEST_INSIGHT);

            indigoDashboardsPage.getConfigurationPanel().selectDateDataSetByName(DATE_CREATED);
            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(),
                    DATE_CREATED);

            assertEquals(indigoDashboardsPage.selectWidgetByHeadline(Insight.class, anotherInsight).getHeadline(),
                    anotherInsight);

            indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_CREATED);

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), anotherInsightUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void disableDateFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(panel.isDataSetEnabled(), "Default state of date filter is not enabled");

        panel.disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDataSetEnabled(),
                "State of date filter is not disabled");
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void enableDateFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDataSetEnabled(),
                "Date filter is not disabled");

        panel.enableDateFilter();
        assertTrue(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDataSetEnabled(),
                "Date filter is not enabled");
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void changeFilterOnAddedInsight() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                TEST_INSIGHT);
        indigoDashboardsPage.getConfigurationPanel().selectDateDataSetByName(DATE_CREATED);
        takeScreenshot(browser, "Change-Filter-On-Added-Insight", getClass());
        assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_CREATED,
                "Date fillter is not applied");

        indigoDashboardsPage.selectDateFilterByName("All time").waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT).getChartReport()
                .getDataLabels(), singletonList("154,271"), "Chart renders incorrectly");
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void rememberDisabledDateFilterAfterSaving() throws JSONException, IOException {
        String insight = "Disabled-Date-Filter-Insight";
        String insightUri = createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(insight).waitForWidgetsLoading()
                .getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDataSetEnabled(),
                "Date filter is not disabled");
        indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, insight);
            assertFalse(indigoDashboardsPage.getConfigurationPanel().isDataSetEnabled(),
                    "Date filter is not disabled");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), insightUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void rememberEnabledDateFilterAfterSaving() throws JSONException, IOException {
        String insight = "Enabled-Date-Filter-Insight";
        String insightUri = createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        initIndigoDashboardsPageWithWidgets().switchToEditMode().addInsight(insight).waitForWidgetsLoading();
        // this test should not depend on default state of date filter
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter().enableDateFilter();
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDataSetEnabled(), "Date filter is not enabled");
        indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, insight);
            assertTrue(indigoDashboardsPage.getConfigurationPanel().isDataSetEnabled(),
                    "Date filter is not enabled");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), insightUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void rememberModifiedDateFilterAfterSaving() throws JSONException, IOException {
        String widgetUri = createInsightWidget(new InsightMDConfiguration("Modified-Date-Filter-Insight",
                ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        addWidgetToWorkingDashboard(widgetUri);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            indigoDashboardsPage.getConfigurationPanel().enableDateFilter().selectDateDataSetByName(DATE_CREATED);

            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().getSelectedDataSet(),
                    DATE_CREATED);

            indigoDashboardsPage.saveEditModeWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_CREATED);
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), widgetUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void applyRenamedDateOnDateFilterInsight() throws JSONException, IOException {
        // currently, we can't create a new date value, so we will use 1 existing date for tests
        // which need to modify date in order to narrow affected area
        String widgetUri = createInsightWidget(new InsightMDConfiguration("Renamed-Date-Filter-Insight",
                ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        addWidgetToWorkingDashboard(widgetUri);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            indigoDashboardsPage.getConfigurationPanel().enableDateFilter().selectDateDataSetByName(SNAPSHOT);
            indigoDashboardsPage.saveEditModeWithWidgets();

            initManagePage();
            ObjectsTable.getInstance(By.id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser)
                    .selectObject(DATE_SNAPSHOT);

            DatasetDetailPage.getInstance(browser).changeName(DATE_SNAPSHOT_RENAMED);

            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
            takeScreenshot(browser, "Renamed-Date-Filter-Test", getClass());
            assertEquals(panel.getSelectedDataSet(), DATE_SNAPSHOT_RENAMED,
                    "New name is not applied to the insight");

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), widgetUri);
        }
    }

    @Test(dependsOnMethods = "applyRenamedDateOnDateFilterInsight")
    public void deleteDateUsedOnDateFilterInsight() throws JSONException, IOException {
        // currently, we can't create a new date value, so we will use 1 existing date for tests
        // which need to modified date in order to narrow affected area
        String insight = "Insight-Containing-Date-Filter";
        String widgetUri =
                createInsightWidget(new InsightMDConfiguration(insight, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        addWidgetToWorkingDashboard(widgetUri);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            indigoDashboardsPage.getConfigurationPanel().enableDateFilter()
                    .selectDateDataSetByName(DATE_SNAPSHOT_RENAMED);
            indigoDashboardsPage.saveEditModeWithWidgets();

            initManagePage();
            ObjectsTable.getInstance(By.id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser)
                    .selectObject(DATE_SNAPSHOT_RENAMED);

            DatasetDetailPage.getInstance(browser).deleteObject();

            assertFalse(getInsightWidgetTitles(getRestApiClient(), testParams.getProjectId())
                    .contains(insight), "The expected insight has not been deleted");
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), widgetUri);
        }
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    private String createUnrelatedDateInsight() throws ParseException, JSONException, IOException {
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_ACCOUNT));
        Metric nonRelatedMetric = getMdService().createObj(getProject(),
                new Metric("Non-Related-Date-Metric", "SELECT COUNT([" + accountUri + "])", "#,##0"));

        return createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(NON_RELATED_DATE_INSIGHT, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.getSimpleInstance(nonRelatedMetric))));
    }

    private String createLargeInsight() throws ParseException, JSONException, IOException {
        Metric numberOfActivies =
                getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES));
        Attribute activity = getMdService().getObj(getProject(), Attribute.class, title(ATTR_ACTIVITY));

        return createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(LARGE_INSIGHT, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.getSimpleInstance(numberOfActivies)))
                        .setCategoryBucket(singletonList(CategoryBucket.createViewByBucket(activity))));
    }

    private String createEmptyInsight() throws ParseException, JSONException, IOException {
        return createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(EMPTY_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(getMetric(METRIC_NUMBER_OF_LOST_OPPS)))));
    }

    private String createIncomputedReport() throws ParseException, JSONException, IOException {
        String productUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_PRODUCT));
        Metric incomputedMetric = getMdService().createObj(getProject(),
                new Metric("Incomputed-Metric", "SELECT [" + productUri + "]", "#,##0"));

        return createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(INCOMPUTED_INSIGHT, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.getSimpleInstance(incomputedMetric))));
    }

    private String createTableInsight() throws ParseException, JSONException, IOException {
        Attribute department = getMdService().getObj(getProject(), Attribute.class, title(ATTR_DEPARTMENT));

        return createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(WITHOUT_METRIC_INSIGHT, ReportType.TABLE)
                        .setCategoryBucket(singletonList(CategoryBucket.createViewByBucket(department))));
    }
}
