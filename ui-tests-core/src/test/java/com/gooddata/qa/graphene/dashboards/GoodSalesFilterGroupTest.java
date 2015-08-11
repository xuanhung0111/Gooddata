package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.utils.CssUtils.simplifyText;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.testng.Assert.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.entity.report.ReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.GroupConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class GoodSalesFilterGroupTest extends GoodSalesAbstractTest {

    private static final String REPORT = "Report";

    private static final String AMOUNT = "Amount";

    private static final String STAGE_NAME = "Stage Name";
    private static final String IS_WON = "Is Won?";

    private static final String TEST_DASHBOARD = "TestDashboard";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-filter-group";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init"})
    public void createReport() {
        initReportsPage();
        ReportDefinition rd = new ReportDefinition().withName(REPORT).withWhats(AMOUNT)
                .withHows(STAGE_NAME, IS_WON);
        createReport(rd, REPORT);
        reportPage.saveReport();
    }

    @Test(dependsOnGroups = {"init"})
    public void createFilterGroup() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TEST_DASHBOARD);

        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

        dashboardEditBar.addReportToDashboard(REPORT);
        WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, STAGE_NAME);
        WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getRoot();
        filter.click();
        DashboardWidgetDirection.UP.moveElementToRightPlace(filter);

        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, IS_WON);
        filter = dashboardsPage.getContent().getFilterWidget(simplifyText(IS_WON)).getRoot();
        filter.click();
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(filter);

        dashboardEditBar.saveDashboard();
        dashboardsPage.editDashboard();

        WidgetConfigPanel configPanel = dashboardEditBar.openGroupConfigPanel();
        configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class).selectFilters(STAGE_NAME, IS_WON);
        configPanel.saveConfiguration();

        configPanel = dashboardEditBar.openGroupConfigPanel();
        assertEquals(configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class)
                .getUnavailableExplanationMessage(), "Unavailable filters already belong to a different group.");
        configPanel.discardConfiguration();

        dashboardEditBar.cancelDashboard();
        dashboardsPage.editDashboard();
        configPanel = dashboardEditBar.openGroupConfigPanel();
        configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class).selectFilters(STAGE_NAME, IS_WON);
        configPanel.saveConfiguration();
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"createFilterGroup"})
    public void testFilterGroup() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(TEST_DASHBOARD);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        FilterWidget isWonFilter = dashboardsPage.getContent().getFilterWidget(simplifyText(IS_WON));
        isWonFilter.changeSelectionToOneValue();
        dashboardEditBar.saveDashboard();

        Sleeper.sleepTightInSeconds(2);
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class)
                .getAttributeElementsByRow().size(), 7);

        isWonFilter.changeAttributeFilterValueInSingleMode("true");
        isWonFilter.getRoot().click();
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class)
                .getAttributeElementsByRow().size(), 7);

        FilterWidget stageNameFilter = dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME));
        stageNameFilter.changeAttributeFilterValue("Closed Won");
        stageNameFilter.getRoot().click();
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class)
                .getAttributeElementsByRow().size(), 7);

        waitForElementVisible(By.cssSelector(".s-btn-apply"), browser).click();
        Sleeper.sleepTightInSeconds(2);
        assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class)
                .getAttributeElementsByRow().size(), 1);
    }
}
