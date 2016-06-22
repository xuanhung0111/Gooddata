package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class EmbeddedDashboardDrillReportLocalizationTest extends GoodSalesAbstractLocalizationTest {

    private static final String REPORT_NAME = "Drill report";

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-embeded-dashboard-saved-view-localization-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void createDrillReport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName(REPORT_NAME)
            .withWhats(new WhatItem(METRIC_AMOUNT, ATTR_ACCOUNT))
            .withHows(ATTR_STAGE_NAME);
        createReport(reportDefinition, REPORT_NAME);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createDrillReport"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        initDashboardsPage()
            .addNewDashboard(REPORT_NAME);

        initDashboardsPage()
            .selectDashboard(REPORT_NAME)
            .editDashboard()
            .addReportToDashboard(REPORT_NAME)
            .saveDashboard();

        embeddedUri = dashboardsPage.openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDrillOverlay() {
        initEmbeddedDashboard()
            .getContent()
            .getReport(REPORT_NAME, TableReport.class)
            .drillOnMetricValue();

        DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
        TableReport tableReport = drillDialog.getReport(TableReport.class);
        checkLocalization(browser);

        drillDialog.changeChartType("Line chart");
        tableReport.waitForReportLoading();
        checkLocalization(browser);

        drillDialog.changeChartType("Bar chart");
        tableReport.waitForReportLoading();
        checkLocalization(browser);

        drillDialog.changeChartType("Pie chart");
        tableReport.waitForReportLoading();
        checkLocalization(browser);

        drillDialog.changeChartType("Table");
        tableReport.waitForReportLoading();
        checkLocalization(browser);

        drillDialog.clickOnBreadcrumbs(REPORT_NAME);
        checkLocalization(browser);

        drillDialog.openReportInfoViewPanel().clickViewReportButton();
        EmbeddedReportPage.waitForPageLoaded(browser);
        checkLocalization(browser);
    }
}
