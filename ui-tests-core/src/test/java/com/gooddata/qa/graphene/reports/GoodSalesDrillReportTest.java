package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.asserts.AssertUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.asserts.AssertUtils.assertIgnoreCase;
import static com.gooddata.qa.utils.asserts.AssertUtils.assertIgnoreCaseAndIndex;
import static java.lang.String.format;
import static java.util.Calendar.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesDrillReportTest extends GoodSalesAbstractTest {

    private static final String TEST_DASHBOAD_NAME = "test-drill-report";
    private static final String TARGET_DASHBOAD_NAME = "drill-target-dashboard";
    private static final String REPORT_NAME = "Drill report";
    private static final String DRILL_ACTIVITY_REPORT = "Drill-Activity report";
    private static final String TARGET_DASHBOARD_TAB_NAME = "Target Tab";
    private static final int YEAR_2010 = 2010;
    private static final int YEAR_2011 = 2011;
    private static final String YEAR_2012 = "2012";
    private static final String ROLL_UP = "Rollup";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-drill-report";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfOpportunitiesMetric();
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
            assertFalse(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));

            DashboardDrillDialog drillDialog = tableReport.openDrillDialogFrom("2010", CellType.ATTRIBUTE_VALUE);

            tableReport = drillDialog.getReport(TableReport.class);
            assertTrue(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill report", "2010"), ">>"));
            assertEquals(drillDialog.getChartTitles(), Arrays.asList("Table", "Line chart", "Bar chart", "Pie chart"));
            assertEquals(drillDialog.getSelectedChartTitle(), "Table");

            drillDialog.clickOnBreadcrumbs(REPORT_NAME);
            assertFalse(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertEquals(drillDialog.getBreadcrumbsString(), "Drill report");
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));

            tableReport.drillOnFirstValue(CellType.METRIC_VALUE);
            assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Account"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount"));
            assertTrue(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));

            drillDialog.closeDialog();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            checkRedBar(browser);
            assertFalse(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Year (Snapshot)", "Stage Name"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));
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
            tableReport.waitForLoaded();
            checkRedBar(browser);

            drillDialog.changeChartType("Bar chart");
            tableReport.waitForLoaded();
            checkRedBar(browser);

            drillDialog.changeChartType("Pie chart");
            tableReport.waitForLoaded();
            checkRedBar(browser);

            drillDialog.changeChartType("Table");
            tableReport.waitForLoaded();
            checkRedBar(browser);
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));
            assertTrue(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
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

            TableReport tableReport = Graphene.createPageFragment(TableReport.class,
                    waitForElementVisible(By.id("gridContainerTab"), browser));
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Quarter/Year (Snapshot)", "Stage Name"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));
            assertTrue(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
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

            int windowHandles = browser.getWindowHandles().size();
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);

            Predicate<WebDriver> newTabOpened = browser -> browser.getWindowHandles().size() > windowHandles;
            Graphene.waitGui().until(newTabOpened);

            try {
                BrowserUtils.switchToLastTab(browser);
                assertTrue(browser.getCurrentUrl().contains("www.google.com"));
            } finally {
                BrowserUtils.closeCurrentTab(browser);
                BrowserUtils.switchToFirstTab(browser);
            }

            initAttributePage().initAttribute("Opportunity")
                .setDrillToAttribute("Account");

            initDashboardsPage().selectDashboard(TEST_DASHBOAD_NAME);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);

            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            tableReport = drillDialog.getReport(TableReport.class);
            AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Account"));
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
        DashboardDrillDialog drillDialog = null;
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);

            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();

            drillDialog = tableReport.openDrillDialogFrom("Interest", CellType.ATTRIBUTE_VALUE);
            tableReport = drillDialog.getReport(TableReport.class);
            assertTrue(tableReport.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Year (Snapshot)", "Account"));
            assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount", "Avg. Amount"));
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList("Drill report", "Interest"), ">>"));
        } finally {
            drillDialog.closeDialog();
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReport"})
    public void overrideDrilldownAndDrillIn() {
        initAttributePage().initAttribute("Activity")
            .setDrillToAttribute("Activity Type");

        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
                .withName(DRILL_ACTIVITY_REPORT)
                .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACCOUNT))
                .withHows(new HowItem(ATTR_ACTIVITY, "Email with AirSplat on Apr-21-11"))
                .withHows(new HowItem(ATTR_YEAR_ACTIVITY, HowItem.Position.TOP));

        createReport(reportDefinition, DRILL_ACTIVITY_REPORT + "_screenshot");
        checkRedBar(browser);

        try {
            addReportToNewDashboard(DRILL_ACTIVITY_REPORT, TEST_DASHBOAD_NAME);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);

            dashboardsPage.editDashboard();
            tableReport.addDrilling(Pair.of(Arrays.asList(ATTR_ACTIVITY), ATTR_PRIORITY));
            tableReport.addDrilling(Pair.of(Arrays.asList(ATTR_YEAR_ACTIVITY), REPORT_NAME), "Reports");
            tableReport.addDrilling(Pair.of(Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), ATTR_STATUS));
            dashboardsPage.saveDashboard();
            checkRedBar(browser);

            DashboardDrillDialog drillDialog = tableReport
                    .openDrillDialogFrom("Email with AirSplat on Apr-21-11", CellType.ATTRIBUTE_VALUE);
            TableReport tableReportInDialog = drillDialog.getReport(TableReport.class);
            assertTrue(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_YEAR_ACTIVITY, ATTR_PRIORITY));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_NUMBER_OF_ACTIVITIES));
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT,
                    "Email with AirSplat on Apr-21-11"), ">>"));
            drillDialog.closeDialog();

            tableReport.openDrillDialogFrom("1", CellType.METRIC_VALUE);
            assertTrue(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_STATUS));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_NUMBER_OF_ACTIVITIES));
            assertEquals(drillDialog.getBreadcrumbsString(), StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT,
                    "Email with AirSplat on Apr-21..."), ">>"));
            drillDialog.closeDialog();

            tableReport.openDrillDialogFrom("2011", CellType.ATTRIBUTE_VALUE);
            assertFalse(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT, "2011"), ">>"));
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"overrideDrilldownAndDrillIn"})
    public void drillReportContainsFilter() {
        try {
            addReportToNewDashboard(DRILL_ACTIVITY_REPORT, TEST_DASHBOAD_NAME);

            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getReport(DRILL_ACTIVITY_REPORT, TableReport.class);
            DashboardWidgetDirection.LEFT.moveElementToRightPlace(tableReport.getRoot());
            dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_ACTIVITY)
                    .saveDashboard();

            dashboardsPage.getFirstFilter().changeAttributeFilterValues("Email with Bulbs.com on Aug-06-10");

            DashboardDrillDialog drillDialog = tableReport
                    .openDrillDialogFrom("Email with Bulbs.com on Aug-06-10", CellType.ATTRIBUTE_VALUE);
            TableReport tableReportInDialog = drillDialog.getReport(TableReport.class);
            assertTrue(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_YEAR_ACTIVITY, ATTR_ACTIVITY_TYPE));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_NUMBER_OF_ACTIVITIES));
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT, "Email with Bulbs.com on Aug-0..."), ">>"));
            drillDialog.closeDialog();

            tableReport.openDrillDialogFrom("1", CellType.METRIC_VALUE);
            assertTrue(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_ACCOUNT));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_NUMBER_OF_ACTIVITIES));
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT, "Email with Bulbs.com on Aug-0..."), ">>"));
            drillDialog.closeDialog();

            tableReport.openDrillDialogFrom("2010", CellType.ATTRIBUTE_VALUE);
            assertTrue(tableReportInDialog.hasValue(ROLL_UP, CellType.TOTAL_HEADER));
            AssertUtils.assertIgnoreCase(tableReportInDialog.getAttributeHeaders(), Arrays.asList(ATTR_QUARTER_YEAR_ACTIVITY, ATTR_ACTIVITY));
            assertIgnoreCaseAndIndex(tableReportInDialog.getMetricHeaders(), Sets.newHashSet(METRIC_NUMBER_OF_ACTIVITIES));
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(DRILL_ACTIVITY_REPORT, "2010"), ">>"));
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();

            initAttributePage().initAttribute(ATTR_ACTIVITY)
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

            tableReport.drillOn("Interest", CellType.ATTRIBUTE_VALUE);

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

            tableReport.drillOn("Risk Assessment", CellType.ATTRIBUTE_VALUE);

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

            tableReport.drillOn("Interest", CellType.ATTRIBUTE_VALUE);

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
        return tableReport.openDrillDialogFrom("2010", CellType.ATTRIBUTE_VALUE);
    }
}
