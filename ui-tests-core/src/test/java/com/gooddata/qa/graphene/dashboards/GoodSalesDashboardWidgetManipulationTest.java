package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.ArrangeConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;

public class GoodSalesDashboardWidgetManipulationTest extends GoodSalesAbstractTest {

    private static final String STAGE_NAME = "Stage Name";
    private static final String ACTIVITIES_BY_TYPE_REPORT = "Activities by Type";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-dashboard-widget-manipulation";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void testZIndex() {
        WidgetConfigPanel configPanel;
        ArrangeConfigPanel arrangeConfigPanel;

        initDashboardsPage();
        dashboardsPage.addNewDashboard("Widget Manipulation");
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

        dashboardEditBar.addReportToDashboard(ACTIVITIES_BY_TYPE_REPORT);
        WebElement report = dashboardsPage.getContent().getLatestReport(ChartReport.class).getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

        configPanel = WidgetConfigPanel.openConfigurationPanelFor(report, browser);
        int reportZIndex = configPanel.getTab(Tab.ARRANGE, ArrangeConfigPanel.class).getCurrentZIndex();
        takeScreenshot(browser, "testZIndex-report-ZIndex", getClass());
        assertThat(reportZIndex, equalTo(2));
        configPanel.discardConfiguration();

        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, STAGE_NAME);
        FilterWidget stageNameFilter = dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME));
        WebElement stageNameRootElement = stageNameFilter.getRoot();

        configPanel = WidgetConfigPanel.openConfigurationPanelFor(stageNameRootElement, browser);
        arrangeConfigPanel = configPanel.getTab(Tab.ARRANGE, ArrangeConfigPanel.class);
        int filterZIndex = arrangeConfigPanel.getCurrentZIndex();
        takeScreenshot(browser, "testZIndex-filter-ZIndex", getClass());
        assertThat(filterZIndex, equalTo(3));
        configPanel.discardConfiguration();

        new Actions(browser).keyDown(Keys.SHIFT).click(report).keyUp(Keys.SHIFT).perform();
        waitForElementVisible(className("yui3-toolbar-icon-config"), browser).click();
        configPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        int groupZIndex = configPanel.getTab(Tab.ARRANGE, ArrangeConfigPanel.class).getCurrentZIndex();
        takeScreenshot(browser, "testZIndex-group-ZIndex", getClass());
        assertThat(groupZIndex, equalTo(3));
        dashboardEditBar.saveDashboard();

        dashboardsPage.editDashboard();
        stageNameRootElement.click();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(stageNameRootElement);
        dashboardEditBar.saveDashboard();
        takeScreenshot(browser, "testZIndex-filterOverlapReport", getClass());

        dashboardsPage.editDashboard();
        configPanel = WidgetConfigPanel.openConfigurationPanelFor(stageNameRootElement, browser);
        arrangeConfigPanel = configPanel.getTab(Tab.ARRANGE, ArrangeConfigPanel.class);
        arrangeConfigPanel.decreaseZIndex().decreaseZIndex();
        takeScreenshot(browser, "testZIndex-report-decrease-ZIndex", getClass());
        configPanel.saveConfiguration();
        dashboardEditBar.saveDashboard();
    }
}
