package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.ArrangeConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;

public class GoodSalesDashboardWidgetManipulationTest extends GoodSalesAbstractTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-dashboard-widget-manipulation";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getReportCreator().createActivitiesByTypeReport();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testZIndex() {
        WidgetConfigPanel configPanel;
        ArrangeConfigPanel arrangeConfigPanel;

        initDashboardsPage();
        dashboardsPage.addNewDashboard("Widget Manipulation");
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

        dashboardEditBar.addReportToDashboard(REPORT_ACTIVITIES_BY_TYPE);
        WebElement report = dashboardsPage.getContent().getLatestReport(ChartReport.class).getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

        configPanel = WidgetConfigPanel.openConfigurationPanelFor(report, browser);
        int reportZIndex = configPanel.getTab(Tab.ARRANGE, ArrangeConfigPanel.class).getCurrentZIndex();
        takeScreenshot(browser, "testZIndex-report-ZIndex", getClass());
        assertThat(reportZIndex, equalTo(2));
        configPanel.discardConfiguration();

        dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        FilterWidget stageNameFilter = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME));
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
