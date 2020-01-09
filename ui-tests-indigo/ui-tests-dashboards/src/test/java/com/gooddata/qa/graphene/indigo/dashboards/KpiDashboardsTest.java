package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.google.common.collect.Ordering;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class KpiDashboardsTest extends AbstractDashboardTest {

    private static final String ALL_TIME = "All time";
    private IndigoRestRequest indigoRestRequest;
    private static final String NEW_METRIC = "Create New Metrics has names too long and contain three cores";
    private static final String NEW_FORMAT = "#,##0 test tooltip has long names and contain three cores test tooltip has long names and contain three cores";
    private String dashboardUri;
    private String windowSize;

    @Override
    protected void customizeProject() {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createNumOfActivitiesKpi()),
                METRIC_NUMBER_OF_ACTIVITIES);
        indigoRestRequest.createAnalyticalDashboard(singletonList(createLostKpi()), METRIC_LOST);
        indigoRestRequest.createAnalyticalDashboard( Arrays.asList(
                createNumOfActivitiesKpi(), createLostKpi(), createAmountKpi()), "TRUNCATED BOTTOM");
        dashboardUri = indigoRestRequest.createAnalyticalDashboard( Arrays.asList(
                createNumOfActivitiesKpi()), "KPI TRUNCATED BOTTOM");
    }

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        windowSize = context.getCurrentXmlTest().getParameter("windowSize");
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }
	
    @DataProvider
    public Object[][] getUserRoles() {
        return new Object[][] { { UserRoles.ADMIN }, { UserRoles.EDITOR } };
    }
	
    @Test(dependsOnGroups = { "createProject" }, groups = { "desktop" }, dataProvider = "getUserRoles")
    public void selectKpiDashboardsTest(UserRoles role) throws JSONException, IOException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(indigoDashboardsPage.isNavigationBarVisible(), "Navigation bar is not display");

            indigoDashboardsPage.selectKpiDashboard(METRIC_LOST);
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "Select-KPI-dashboard-test-with-" + role.getName(), getClass());
			
            assertFalse(indigoDashboardsPage.isOnEditMode(), "KpiDashboard doesn't open in view mode");
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(METRIC_LOST)));
            assertEquals(indigoDashboardsPage.getDashboardTitle(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
	
    @Test(dependsOnGroups = { "createProject" }, groups = { "mobile" }, dataProvider = "getUserRoles")
    public void selectKpiDashboardsOnMobileTest(UserRoles role) throws JSONException, IOException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(METRIC_LOST);
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "Select-KPI-dashboard-on-mobile-test-with-" + role.getName(), getClass());
	
            assertEquals(indigoDashboardsPage.getDashboardTitle(), METRIC_LOST);
            assertThat(browser.getCurrentUrl(), containsString(getKpiDashboardIdentifierByTitle(METRIC_LOST)));
            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
    	    logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
	
    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "getUserRoles")
    public void kpiDashboardsSortByAlphabetTest(UserRoles role) throws JSONException {
        String urlAmountKpiDashboard;
        logoutAndLoginAs(true, role);
        urlAmountKpiDashboard = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()),
                METRIC_AMOUNT);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertFalse(indigoDashboardsPage.isOnEditMode(), "should be on View Mode");
            takeScreenshot(browser, "KPI-dashboard-sort-by-alphabet-test-with-" + role.getName(), getClass());
            assertTrue(Ordering.natural().isOrdered(indigoDashboardsPage.getDashboardTitles()),
                    "New kpi dashboards should be sorted by alphabet in list");
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(urlAmountKpiDashboard);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "desktop", "mobile" }, dataProvider = "getUserRoles")
    public void openKpiDashboardByUrlTest(UserRoles role) throws IOException, JSONException {
        logoutAndLoginAs(true, role);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertThat(browser.getCurrentUrl(),
                    containsString(getKpiDashboardIdentifierByTitle(METRIC_NUMBER_OF_ACTIVITIES)));

            openUrl(PAGE_INDIGO_DASHBOARDS + "#/project/" + testParams.getProjectId() + "/dashboard/"
                    + getKpiDashboardIdentifierByTitle(METRIC_LOST));
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "open-KPI-dashboard-by-Url-test-with-" + role.getName(), getClass());

            assertEquals(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(), METRIC_LOST);
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), ALL_TIME);
        } finally {
        	logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkTooltipShowCorrectlyOnKPIs() throws IOException {
        String factAmountUri = getFactByTitle(FACT_AMOUNT).getUri();
        Attribute stageNameAttribute = getAttributeByTitle(ATTR_STAGE_NAME);
        String stageNameValues = getMdService()
                .getAttributeElements(stageNameAttribute)
                .subList(0, 2)
                .stream()
                .map(AttributeElement::getUri)
                .map(e -> format("[%s]", e))
                .collect(joining(","));
        String expression = format("SELECT SUM([%s]) WHERE [%s] IN (%s)",
                factAmountUri, stageNameAttribute.getUri(), stageNameValues);

        createMetric(NEW_METRIC, expression, NEW_FORMAT);

        String insightUri = indigoRestRequest.createInsight(
                new InsightMDConfiguration("INSIGHT TOOLTIP TEST", ReportType.PIE_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket
                                .createSimpleMeasureBucket(getMetricByTitle(NEW_METRIC)))));

        indigoRestRequest.createAnalyticalDashboard(
                asList(indigoRestRequest.createVisualizationWidget(
                        insightUri, "INSIGHT TOOLTIP TEST")), "KPI TOOLTIP TEST");

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                .selectKpiDashboard("KPI TOOLTIP TEST").waitForDashboardLoad().waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getLastWidget(Insight.class).getChartReport();
        takeScreenshot(browser, "KPI TOOLTIP TEST", getClass());
        assertTrue(chartReport.isTooltipContainThreeDots(0, 0), "Tooltip title should contain three dots");
        assertTrue(chartReport.isTooltipContainTwoRowsAndThreeDots(0, 0), "Tooltip value should contain three dots and two rows");
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "desktop"})
    public void checkUIDialogContentTruncatedBottomOnDesktopTest() throws JSONException, IOException {
        try {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 1);
            initIndigoDashboardsPageWithWidgets();
            indigoDashboardsPage.selectKpiDashboard("KPI TRUNCATED BOTTOM").waitForWidgetsLoading();
            Kpi kpi = indigoDashboardsPage.getFirstWidget(Kpi.class).waitForContentLoading();
            takeScreenshot(browser, "Break Bottom Line ", getClass());

            assertTrue(CheckUtils.isLessThan180PX(removePX(kpi.getWidth())), "Widget should be less than 180px");
            assertTrue(kpi.isBreakLineBottom(), "Bottom should break line");

            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 2);
            browser.navigate().refresh();
            indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
            kpi = indigoDashboardsPage.getFirstWidget(Kpi.class).waitForContentLoading();
            takeScreenshot(browser, "Truncated Bottom", getClass());

            assertTrue(kpi.isNonBreakLineBottom(), "Bottom should not break line");
            assertFalse(CheckUtils.isLessThan180PX(removePX(kpi.getWidth())), "Widget should be bigger than 180px");
        }finally {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 2);
        }
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "mobile"})
    public void checkUIDialogContentTruncatedBottomOnKpiTest() throws JSONException {
        try {
            initIndigoDashboardsPageWithWidgets();
            indigoDashboardsPage.selectKpiDashboard("TRUNCATED BOTTOM").waitForWidgetsLoading();
            Kpi kpi = indigoDashboardsPage.getFirstWidget(Kpi.class).waitForContentLoading();
            takeScreenshot(browser, "Kpi Truncated Bottom", getClass());

            assertTrue(kpi.isNonBreakLineBottom(), "Bottom should not break line");
            assertFalse(CheckUtils.isLessThan180PX(removePX(kpi.getWidth())), "Widget should be bigger than 180px");

            browser.manage().window().setSize(new Dimension(790,800));
            browser.navigate().refresh();
            indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();

            kpi = indigoDashboardsPage.getFirstWidget(Kpi.class).waitForContentLoading();
            takeScreenshot(browser, "Kpi Break Bottom Line ", getClass());

            assertTrue(CheckUtils.isLessThan180PX(removePX(kpi.getWidth())), "Widget should be less than 180px");
            assertTrue(kpi.isBreakLineBottom(), "Bottom should break line");
        }finally {
            String[] windowSizeArr = windowSize.split(",");
            int width = Integer.parseInt(windowSizeArr[0]);
            int high = Integer.parseInt(windowSizeArr[1]);
            log.info("windowSize : " + width + " " + high);
            browser.manage().window().setSize(new Dimension(width, high));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkMinimizeSpacingTest() {
        initIndigoDashboardsPage().addDashboard()
                .addKpi(new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED)
                        .build()).changeDashboardTitle("DASHBOARD NAME").selectDateFilterByName(ALL_TIME).saveEditModeWithWidgets();
        takeScreenshot(browser, "TEST MINIMIZE SPACING ON KD", getClass());
        assertTrue(indigoDashboardsPage.isMinimizeSpacingChanged(), "Minimize Spacing should be changed");
    }

    private String removePX(String value) {
        return value.replace("px", "");
    }
}
