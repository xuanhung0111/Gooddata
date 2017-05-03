package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CREATED;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.lang.String.format;
import static java.util.Calendar.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.google.common.collect.Sets;
import com.google.common.base.Predicate;

public class GoodSalesDrillReportTest extends GoodSalesAbstractTest {

    private static final String TEST_DASHBOAD_NAME = "test-drill-report";
    private static final String TARGET_DASHBOAD_NAME = "drill-target-dashboard";
    private static final String REPORT_NAME = "Drill report";
    private static final String TARGET_DASHBOARD_TAB_NAME = "Target Tab";
    private static final int YEAR_2010 = 2010;
    private static final int YEAR_2011 = 2011;
    private static final String YEAR_2012 = "2012";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-drill-report";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createDrillReport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName(REPORT_NAME)
            .withWhats(new WhatItem("Amount", "Account"))
            .withWhats("Avg. Amount")
            .withHows("Stage Name")
            .withHows(new HowItem("Year (Snapshot)", HowItem.Position.TOP));
        createReport(reportDefinition, REPORT_NAME);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void drillOnDashboard() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertFalse(tableReport.isRollupTotalVisible());
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");

            tableReport.clickOnAttributeToOpenDrillReport("2010");
            DashboardDrillDialog drillDialog =
                    Graphene.createPageFragment(DashboardDrillDialog.class,
                            waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

            tableReport = drillDialog.getReport(TableReport.class);
            assertTrue(tableReport.isRollupTotalVisible());
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill report", "2010"), ">>"));
            assertEquals(drillDialog.getChartTitles(), Arrays.asList("Table", "Line chart", "Bar chart", "Pie chart"));
            assertEquals(drillDialog.getSelectedChartTitle(), "Table");

            drillDialog.clickOnBreadcrumbs(REPORT_NAME);
            assertFalse(tableReport.isRollupTotalVisible());
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertEquals(drillDialog.getBreadcrumbsString(), "Drill report");
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");

            tableReport.drillOnMetricValue();
            tableReport.waitForReportLoading();
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Account"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount"),
                    "Metric headers are not correct!");
            assertTrue(tableReport.isRollupTotalVisible());

            drillDialog.closeDialog();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            checkRedBar(browser);
            assertFalse(tableReport.isRollupTotalVisible());
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void modifyOnDrillingOverlay() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            DashboardDrillDialog drillDialog = drillReportYear2010();
            TableReport tableReport = drillDialog.getReport(TableReport.class);

            drillDialog.changeChartType("Line chart");
            tableReport.waitForReportLoading();
            checkRedBar(browser);

            drillDialog.changeChartType("Bar chart");
            tableReport.waitForReportLoading();
            checkRedBar(browser);

            drillDialog.changeChartType("Pie chart");
            tableReport.waitForReportLoading();
            checkRedBar(browser);

            drillDialog.changeChartType("Table");
            tableReport.waitForReportLoading();
            checkRedBar(browser);
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
            assertTrue(tableReport.isRollupTotalVisible());
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void verifyReportInfoOnDrillingOverlay() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            DashboardDrillDialog drillDialog = drillReportYear2010();

            assertTrue(drillDialog.isReportInfoButtonVisible());
            ReportInfoViewPanel reportInfoPanel = drillDialog.openReportInfoViewPanel();
            assertEquals(reportInfoPanel.getReportTitle(), "");
            assertEquals(reportInfoPanel.getAllMetricNames(), Arrays.asList("Amount", "Avg. Amount"));

            String currentWindowHandle = browser.getWindowHandle();
            reportInfoPanel.clickViewReportButton();

            // switch to newest window handle
            for (String s : browser.getWindowHandles()) {
                if (!s.equals(currentWindowHandle)) {
                    browser.switchTo().window(s);
                    break;
                }
            }
            waitForAnalysisPageLoaded(browser);
            waitForElementVisible(reportPage.getRoot());

            TableReport tableReport = Graphene.createPageFragment(TableReport.class,
                    waitForElementVisible(By.id("gridContainerTab"), browser));
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
            assertTrue(tableReport.isRollupTotalVisible());
            browser.close();
            browser.switchTo().window(currentWindowHandle);

            reportInfoPanel.downloadReportAsFormat(ExportFormat.PDF_PORTRAIT);
            sleepTight(4000);
            verifyReportExport(ExportFormat.PDF_PORTRAIT, "2010", 30000L);
            checkRedBar(browser);
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void verifyBreadcrumbInDrillingOverlay() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            DashboardDrillDialog drillDialog = drillReportYear2010();

            assertEquals(drillDialog.getBreadcrumbTitle(REPORT_NAME), REPORT_NAME);
            assertEquals(drillDialog.getBreadcrumbTitle("2010"), "Year (Snapshot) is 2010");

            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void drillAttribute() {
        assertTrue(initAttributePage().initAttribute("Opportunity").isDrillToExternalPage());

        assertFalse(AttributeDetailPage.getInstance(browser).clearDrillingSetting().isDrillToExternalPage());

        AttributeDetailPage.getInstance(browser).setDrillToExternalPage();

        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName("Drill-Opportunity")
            .withWhats("# of Opportunities")
            .withHows(new HowItem("Opportunity", "14 West > Explorer", "1-800 Postcards > Educationly",
                    "1-800 We Answer > Explorer"));

        createReport(reportDefinition, "Drill-Opportunity");
        checkRedBar(browser);

        try {
            addReportToNewDashboard("Drill-Opportunity", TEST_DASHBOAD_NAME);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnMetricValue();
            tableReport.waitForReportLoading();

            String currentWindowHandle = browser.getWindowHandle();
            // wait for google window
            sleepTight(1500);
            // switch to newest window handle
            for (String s : browser.getWindowHandles()) {
                if (!s.equals(currentWindowHandle)) {
                    browser.switchTo().window(s);
                    break;
                }
            }
            assertTrue(browser.getCurrentUrl().contains("www.google.com"));
            browser.close();
            browser.switchTo().window(currentWindowHandle);

            initAttributePage().initAttribute("Opportunity")
                .setDrillToAttribute("Account");

            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnMetricValue();
            tableReport.waitForReportLoading();

            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            tableReport = drillDialog.getReport(TableReport.class);
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Account"));
            drillDialog.closeDialog();
        } finally {
            initDashboardsPage();
            dashboardsPage.selectDashboard(TEST_DASHBOAD_NAME);
            dashboardsPage.deleteDashboard();
            initAttributePage().initAttribute("Opportunity")
                .clearDrillingSetting()
                .setDrillToExternalPage();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void drillAcrossReport() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();

            tableReport.drillOnAttributeValue();
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            tableReport = drillDialog.getReport(TableReport.class);
            assertTrue(tableReport.isRollupTotalVisible());
            assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Year (Snapshot)", "Account"));
            assertSetEquals(tableReport.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill report", "Interest"), ">>"));
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void overrideDrilldownAndDrillIn() {
        initAttributePage().initAttribute("Activity")
            .setDrillToAttribute("Activity Type");

        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName("Drill-Activity")
            .withWhats(new WhatItem("# of Activities", "Account"))
            .withHows(new HowItem("Activity", "Email with AirSplat on Apr-21-11"))
            .withHows(new HowItem("Year (Activity)", HowItem.Position.TOP));

        createReport(reportDefinition, "Drill-Activity");
        checkRedBar(browser);

        try {
            addReportToNewDashboard("Drill-Activity", TEST_DASHBOAD_NAME);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);

            dashboardsPage.editDashboard();
            tableReport.addDrilling(Pair.of(Arrays.asList("Activity"), "Priority"));
            tableReport.addDrilling(Pair.of(Arrays.asList("Year (Activity)"), REPORT_NAME), "Reports");
            tableReport.addDrilling(Pair.of(Arrays.asList("# of Activities"), "Status"));
            dashboardsPage.saveDashboard();
            checkRedBar(browser);

            tableReport.drillOnAttributeValue();
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            TableReport tableReportInDialog = drillDialog.getReport(TableReport.class);
            assertTrue(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Year (Activity)", "Priority"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("# of Activities"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "Email with AirSplat on Apr-21-11"), ">>"));
            drillDialog.closeDialog();

            tableReport.drillOnMetricValue("1");
            assertTrue(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Status"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("# of Activities"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "Email with AirSplat on Apr-21..."), ">>"));
            drillDialog.closeDialog();

            tableReport.clickOnAttributeToOpenDrillReport("2011");
            assertFalse(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("Amount", "Avg. Amount"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "2011"), ">>"));
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"overrideDrilldownAndDrillIn"})
    public void drillReportContainsFilter() {
        try {
            addReportToNewDashboard("Drill-Activity", TEST_DASHBOAD_NAME);

            dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Activity")
                    .saveDashboard();

            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            dashboardsPage.getFirstFilter().changeAttributeFilterValues("Email with Bulbs.com on Aug-06-10");

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.waitForReportLoading();

            tableReport.drillOnAttributeValue();
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            TableReport tableReportInDialog = drillDialog.getReport(TableReport.class);
            assertTrue(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Year (Activity)", "Activity Type"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("# of Activities"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "Email with Bulbs.com on Aug-0..."), ">>"));
            drillDialog.closeDialog();

            tableReport.drillOnMetricValue("1");
            assertTrue(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Account"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("# of Activities"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "Email with Bulbs.com on Aug-0..."), ">>"));
            drillDialog.closeDialog();

            tableReport.clickOnAttributeToOpenDrillReport("2010");
            assertTrue(tableReportInDialog.isRollupTotalVisible());
            assertEquals(tableReportInDialog.getAttributesHeader(), Arrays.asList("Quarter/Year (Activity)", "Activity"));
            assertSetEquals(tableReportInDialog.getMetricsHeader(), Sets.newHashSet("# of Activities"),
                    "Metric headers are not correct!");
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill-Activity", "2010"), ">>"));
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();

            initAttributePage().initAttribute("Activity")
                .clearDrillingSetting();
        }
    }

    @Test(dependsOnMethods = { "createDrillReport" })
    public void drillReportToDashboard() {
        try {
            prepareDrillReportToDashboard();
            initDashboardsPage()
                    .selectDashboard(TEST_DASHBOAD_NAME)
                    .editDashboard();

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList(ATTR_STAGE_NAME), TARGET_DASHBOARD_TAB_NAME), "Dashboards");
            dashboardsPage.saveDashboard();

            tableReport.drillOnAttributeValue();

            Predicate<WebDriver> waitDrilledDashboardLoaded = browser -> dashboardsPage.getDashboardName()
                    .equals(TARGET_DASHBOAD_NAME);
            Graphene.waitGui().withTimeout(1, TimeUnit.MINUTES).until(waitDrilledDashboardLoaded);

            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
        } finally {
            initDashboardsPage().selectDashboard(TARGET_DASHBOAD_NAME).deleteDashboard();
            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME).deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "createDrillReport" })
    public void drillToDashboardPassValueListFilter() {
        try {
            prepareDrillReportToDashboard();

            // setup for origin tab
            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME).editDashboard();

            dashboardsPage.openTab(0)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_ACCOUNT,
                            DashboardWidgetDirection.RIGHT)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME,
                            DashboardWidgetDirection.LEFT)
                    .getFilterWidgetByName(ATTR_STAGE_NAME).changeSelectionToOneValue();

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList(ATTR_STAGE_NAME), TARGET_DASHBOARD_TAB_NAME), "Dashboards");
            dashboardsPage.saveDashboard();

            //setup for target tab
            dashboardsPage.selectDashboard(TARGET_DASHBOAD_NAME).openTab(1)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_ACCOUNT,
                            DashboardWidgetDirection.RIGHT)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME,
                            DashboardWidgetDirection.LEFT)
                    .getFilterWidgetByName(ATTR_STAGE_NAME).changeSelectionToOneValue();

            dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT,
                    DashboardWidgetDirection.MIDDLE)
                    .saveDashboard();

            dashboardsPage.selectDashboard(TEST_DASHBOAD_NAME)
                    .getFilterWidgetByName(ATTR_ACCOUNT).changeAttributeFilterValues("101 Financial", "14 West");
            dashboardsPage.getFilterWidgetByName(ATTR_STAGE_NAME).changeAttributeFilterValues("Risk Assessment");

            tableReport.drillOnAttributeValue();

            Predicate<WebDriver> waitDrilledDashboardLoaded = browser -> dashboardsPage.getDashboardName()
                    .equals(TARGET_DASHBOAD_NAME);
            Graphene.waitGui().withTimeout(1, TimeUnit.MINUTES).until(waitDrilledDashboardLoaded);

            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
            assertEquals(dashboardsPage.getFilterWidgetByName(ATTR_ACCOUNT).getCurrentValue(), "101 Financial, 14 West");
            assertEquals(dashboardsPage.getFilterWidgetByName(ATTR_STAGE_NAME).getCurrentValue(), "Risk Assessment");
            assertEquals(dashboardsPage.getFilterWidgetByName(ATTR_PRODUCT).getCurrentValue(), "All");
        } finally {
            initDashboardsPage().selectDashboard(TARGET_DASHBOAD_NAME).deleteDashboard();
            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME).deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "createDrillReport" })
    public void drillToDashboardAndPassDateFilterValue() {
        try {
            prepareDrillReportToDashboard();

            // setup for origin tab
            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME).editDashboard();

            dashboardsPage.openTab(0)
                    .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, TimeFilterPanel.DateGranularity.YEAR,
                            format("%s ago", Calendar.getInstance().get(YEAR) - YEAR_2010),
                            DashboardWidgetDirection.RIGHT);

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList(ATTR_STAGE_NAME), TARGET_DASHBOARD_TAB_NAME), "Dashboards");
            dashboardsPage.saveDashboard();

            //setup for target tab
            dashboardsPage.selectDashboard(TARGET_DASHBOAD_NAME).openTab(1)
                    .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, TimeFilterPanel.DateGranularity.YEAR,
                            format("%s ago", Calendar.getInstance().get(YEAR) - YEAR_2011))
                    .saveDashboard();

            dashboardsPage.selectDashboard(TEST_DASHBOAD_NAME)
                    .getFilterWidgetByName(DATE_DIMENSION_CREATED)
                    .changeTimeFilterValueByClickInTimeLine(YEAR_2012);

            tableReport.drillOnAttributeValue();

            Predicate<WebDriver> waitDrilledDashboardLoaded = browser -> dashboardsPage.getDashboardName()
                    .equals(TARGET_DASHBOAD_NAME);
            Graphene.waitGui().withTimeout(1, TimeUnit.MINUTES).until(waitDrilledDashboardLoaded);

            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
            assertEquals(dashboardsPage.getFilterWidgetByName(DATE_DIMENSION_CREATED).getCurrentValue(), YEAR_2012);
        } finally {
            initDashboardsPage().selectDashboard(TARGET_DASHBOAD_NAME).deleteDashboard();
            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME).deleteDashboard();
        }
    }

    private static void assertSetEquals(Set<?> actual, Set<?> expected, String message) {
        if (actual.size() != expected.size())
            fail(message);

        if (!actual.containsAll(expected) || !expected.containsAll(actual))
            fail(message);
    }

    private void prepareDrillReportToDashboard() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TEST_DASHBOAD_NAME)
                .addReportToDashboard(REPORT_NAME)
                .saveDashboard();

        dashboardsPage.addNewDashboard(TARGET_DASHBOAD_NAME)
                .addNewTab(TARGET_DASHBOARD_TAB_NAME)
                .saveDashboard();

        checkRedBar(browser);
    }

    private DashboardDrillDialog drillReportYear2010() {
        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
        tableReport.clickOnAttributeToOpenDrillReport("2010");

        return Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
    }
}
