package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class GoodSalesDrillReportInReportPageTest extends GoodSalesAbstractTest {

    private static final String ATTRIBUTE_REPORT_NAME = "Drill attribute report";
    private static final String METRIC_REPORT_NAME = "Drill metric report";

    private static final String AMOUNT = "Amount";
    private static final String ACCOUNT = "Account";
    private static final String PRODUCT = "Product";
    private static final String DEPARTMENT = "Department";
    private static final String STAGE_NAME = "Stage Name";
    private static final String MONTH_YEAR_SNAPSHOT = "Month/Year (Snapshot)";

    private static final String ATTRIBUTE_VALUE_TO_DRILL = "14 West";
    private static final String METRIC_VALUE_TO_DRILL = "$18,447,266.14";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-drill-report";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void drillDownAttributeElement() {
        setDrillAttribute(ACCOUNT, PRODUCT);

        UiReportDefinition rd = new UiReportDefinition().withName(ATTRIBUTE_REPORT_NAME)
                .withWhats(AMOUNT, "Avg. " + AMOUNT).withHows(ACCOUNT);
        createReport(rd, rd.getName());
        checkRedBar(browser);

        TableReport report = openTableReport(ATTRIBUTE_REPORT_NAME);
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertTrue(reportPage.getFilters().isEmpty());

        report = drillOnAttributeFromReport(report, ATTRIBUTE_VALUE_TO_DRILL);
        assertEquals(report.getAttributesHeader(), asList(PRODUCT));
        assertEquals(reportPage.getFilters(), asList(ACCOUNT + " is " + ATTRIBUTE_VALUE_TO_DRILL));

        report = backToPreviousReport();
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertTrue(reportPage.getFilters().isEmpty());
    }

    @Test(dependsOnMethods = {"drillDownAttributeElement"})
    public void updateDrillDownInReport() {
        setDrillAttribute(ACCOUNT, DEPARTMENT);

        TableReport report = drillOnAttributeFromReport(openTableReport(ATTRIBUTE_REPORT_NAME),
                ATTRIBUTE_VALUE_TO_DRILL);
        assertEquals(report.getAttributesHeader(), asList(DEPARTMENT));
        assertEquals(reportPage.getFilters(), asList(ACCOUNT + " is " + ATTRIBUTE_VALUE_TO_DRILL));

        report = backToPreviousReport();
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertTrue(reportPage.getFilters().isEmpty());
    }

    @Test(dependsOnMethods = {"updateDrillDownInReport"})
    public void removeDrillDownForAttribute() {
        openTableReport(ATTRIBUTE_REPORT_NAME);
        assertTrue(isLinkAppearedWhenHoveringOn(getAttributeValueElement(ATTRIBUTE_VALUE_TO_DRILL)));

        clearDrillAttribute(ACCOUNT);

        openTableReport(ATTRIBUTE_REPORT_NAME);
        assertFalse(isLinkAppearedWhenHoveringOn(getAttributeValueElement(ATTRIBUTE_VALUE_TO_DRILL)));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void drillDownMetric() {
        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName(METRIC_REPORT_NAME)
            .withWhats(new WhatItem(AMOUNT, ACCOUNT))
            .withHows(STAGE_NAME);
        createReport(rd, rd.getName());
        checkRedBar(browser);

        TableReport report = openTableReport(METRIC_REPORT_NAME);
        assertEquals(report.getAttributesHeader(), asList(STAGE_NAME));
        assertTrue(reportPage.getFilters().isEmpty());

        report = drillOnMetricFromReport(report, METRIC_VALUE_TO_DRILL);
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertEquals(reportPage.getFilters(), asList(STAGE_NAME + " is Interest"));

        report = backToPreviousReport();
        assertEquals(report.getAttributesHeader(), asList(STAGE_NAME));
        assertTrue(reportPage.getFilters().isEmpty());
    }

    @Test(dependsOnMethods = {"drillDownMetric"})
    public void removeDrillDownForMetric() {
        openTableReport(METRIC_REPORT_NAME);
        assertTrue(isLinkAppearedWhenHoveringOn(getMetricElement(METRIC_VALUE_TO_DRILL)));

        reportPage.showConfiguration()
            .removeDrillStepInConfigPanel(AMOUNT, ACCOUNT)
            .waitForReportExecutionProgress();
        assertFalse(isLinkAppearedWhenHoveringOn(getMetricElement(METRIC_VALUE_TO_DRILL)));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void drillOnAttributeHaveMultiDisplayForm() {
        String reportName = "multi display";
        String attributeValue = "6/2010";

        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName(reportName)
            .withWhats(AMOUNT)
            .withHows(MONTH_YEAR_SNAPSHOT);
        createReport(rd, rd.getName());
        checkRedBar(browser);

        openTableReport(reportName)
            .changeAttributeDisplayLabelByRightClick(MONTH_YEAR_SNAPSHOT, "Number (1/2010) (Snapshot)");
        reportPage.waitForReportExecutionProgress()
            .clickSaveReport()
            .waitForReportSaved();

        initDashboardsPage();
        dashboardsPage.addNewDashboard(reportName);

        try {
            addTableReportToDashboard(reportName).drillOnAttributeValue(attributeValue);
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            assertEquals(drillDialog.getBreadcrumbsString(), reportName + ">>" + attributeValue);
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"drillOnAttributeHaveMultiDisplayForm", "removeDrillDownForMetric",
            "removeDrillDownForAttribute"})
    public void drillToRemovedTarget() {
        String reportName = "drill removed target";
        String attributeValueToDrill = "1000Bulbs.com";
        String metricValueToDrill = "$18,000.00";

        setDrillAttribute(ACCOUNT, PRODUCT);

        UiReportDefinition rd = new UiReportDefinition().withName(reportName)
                .withWhats(new WhatItem(AMOUNT, DEPARTMENT)).withHows(ACCOUNT);
        createReport(rd, rd.getName());
        checkRedBar(browser);

        deleteAttribute(DEPARTMENT);
        deleteAttribute(PRODUCT);

        TableReport report = openTableReport(reportName);
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertTrue(reportPage.getFilters().isEmpty());

        report = drillOnAttributeFromReport(report, ATTRIBUTE_VALUE_TO_DRILL);
        assertEquals(report.getAttributesHeader(), asList(PRODUCT));
        assertEquals(reportPage.getFilters(), asList(ACCOUNT + " is " + ATTRIBUTE_VALUE_TO_DRILL));

        report = backToPreviousReport();
        assertEquals(report.getAttributesHeader(), asList(ACCOUNT));
        assertTrue(reportPage.getFilters().isEmpty());

        report = drillOnMetricFromReport(report, metricValueToDrill);
        assertEquals(report.getAttributesHeader(), asList(DEPARTMENT));
        assertEquals(reportPage.getFilters(), asList(ACCOUNT + " is " + attributeValueToDrill));
        backToPreviousReport();

        initDashboardsPage();
        dashboardsPage.addNewDashboard(reportName);

        try {
            addTableReportToDashboard(reportName).drillOnAttributeValue(attributeValueToDrill);
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            assertEquals(drillDialog.getBreadcrumbsString(), reportName + ">>" + attributeValueToDrill);
            drillDialog.closeDialog();

            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue(metricValueToDrill);
            waitForFragmentVisible(drillDialog);
            assertEquals(drillDialog.getBreadcrumbsString(), reportName + ">>" + attributeValueToDrill);
            drillDialog.closeDialog();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private boolean isLinkAppearedWhenHoveringOn(WebElement element) {
        new Actions(browser).moveToElement(element).perform();
        return element.getCssValue("text-decoration").equals("underline");
    }

    private WebElement getAttributeValueElement(String value) {
        return waitForFragmentVisible(reportPage).getTableReport().getAttributeValueElement(value);
    }

    private WebElement getMetricElement(String value) {
        return waitForFragmentVisible(reportPage).getTableReport().getMetricElement(value);
    }

    private void setDrillAttribute(String source, String target) {
        initAttributePage();
        attributePage.initAttribute(source);
        attributeDetailPage.setDrillToAttribute(target);
    }

    private void clearDrillAttribute(String source) {
        initAttributePage();
        attributePage.initAttribute(source);
        attributeDetailPage.clearDrillingSetting();
    }

    private void deleteAttribute(String attribute) {
        initAttributePage();
        attributePage.initAttribute(attribute);
        attributeDetailPage.deleteAttribute();
    }

    private TableReport openTableReport(String reportName) {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(reportName);

        return waitForFragmentVisible(reportPage).waitForReportExecutionProgress().getTableReport();
    }

    private TableReport drillOnAttributeFromReport(TableReport report, String value) {
        report.drillOnAttributeValue(value);
        return waitForFragmentVisible(reportPage).waitForReportExecutionProgress().getTableReport();
    }

    private TableReport drillOnMetricFromReport(TableReport report, String value) {
        report.drillOnMetricValue(value);
        return waitForFragmentVisible(reportPage).waitForReportExecutionProgress().getTableReport();
    }

    private TableReport backToPreviousReport() {
        browser.navigate().back();
        return waitForFragmentVisible(reportPage).waitForReportExecutionProgress().getTableReport();
    }

    private TableReport addTableReportToDashboard(String reportName) {
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(reportName);
        dashboardEditBar.saveDashboard();

        return dashboardsPage.getContent().getLatestReport(TableReport.class);
    }
}
