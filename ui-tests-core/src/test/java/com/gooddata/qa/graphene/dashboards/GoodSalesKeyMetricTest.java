package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Calendar.YEAR;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Calendar;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.FiltersConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.MetricConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.MetricStyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesKeyMetricTest extends GoodSalesAbstractTest {

    private static final String VARIABLE_NAME = "F_" + ATTR_PRODUCT;

    private static final String METRIC_VARIABLE = "Metric_Variable";
    private static final String COUNT_OF_PRODUCT = "# " + ATTR_PRODUCT;

    private static final String DASHBOARD_NAME = "Test dashboard";
    private static final int YEAR_OF_DATA = 2011;

    private static final By HEADLINE_WIDGET_LOCATOR = className("yui3-c-headlinedashboardwidget");

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-key-metric-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        VariableRestRequest request = new VariableRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String productUri = request.getAttributeByTitle(ATTR_PRODUCT).getUri();
        String variableUri = request.createFilterVariable(VARIABLE_NAME, productUri);

        createMetric(COUNT_OF_PRODUCT, format("SELECT COUNT([%s])", productUri), "#,##0");
        createMetric(METRIC_VARIABLE, format("SELECT [%s] WHERE [%s]",
                getMetricCreator().createAmountMetric().getUri(), variableUri), "#,##0");
    }

    @Test(dependsOnGroups = "createProject")
    public void editHeadlineWidget() {
        initDashboardsPage()
            .addNewDashboard(DASHBOARD_NAME)
            .selectDashboard(DASHBOARD_NAME);

        dashboardsPage.editDashboard()
            .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR,
                    format("%s ago", Calendar.getInstance().get(YEAR) - YEAR_OF_DATA));
        DashboardWidgetDirection.UP.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget("filter-time").getRoot());

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT)).getRoot());

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_NAME);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget(simplifyText(VARIABLE_NAME)).getRoot());

        waitForElementVisible(className("s-btn-widget"), browser).click();
        Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(DashboardAddWidgetPanel.LOCATOR, browser))
                .initWidget(WidgetTypes.KEY_METRIC_WITH_TREND);

        WidgetConfigPanel widgetConfigPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        MetricConfigPanel metricConfigPanel = widgetConfigPanel.getTab(Tab.METRIC, MetricConfigPanel.class);
        metricConfigPanel.selectMetric(METRIC_AMOUNT, "Created");

        waitForKeyMetricUpdateValue();
        assertTrue(waitForFragmentVisible(metricConfigPanel).isWhenDropdownVisibled());
        assertFalse(waitForFragmentVisible(metricConfigPanel).isWhenDropdownEnabled());
        assertTrue(waitForFragmentVisible(metricConfigPanel).isLinkExternalFilterVisible());
        assertTrue(waitForFragmentVisible(metricConfigPanel).isLinkExternalFilterSelected());
        widgetConfigPanel.getTab(Tab.METRIC_STYLE, MetricStyleConfigPanel.class)
            .editMetricFormat("#,##0.00USD");
        FiltersConfigPanel filtersConfigPanel = widgetConfigPanel.getTab(Tab.FILTERS, FiltersConfigPanel.class);
        assertTrue(isEqualCollection(filtersConfigPanel.getAllFilters(),
                asList(DATE_DIMENSION_CREATED, VARIABLE_NAME, ATTR_PRODUCT)));
        filtersConfigPanel.removeFiltersFromSelectedList(ATTR_PRODUCT);
        widgetConfigPanel.getTab(Tab.METRIC, MetricConfigPanel.class)
            .selectMetric(METRIC_VARIABLE, "Created");
        widgetConfigPanel.saveConfiguration();
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "60,270,072.20USD");

        dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT))
            .changeAttributeFilterValues("TouchAll");
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "60,270,072.20USD");

        dashboardsPage.getContent().getFilterWidget(simplifyText(VARIABLE_NAME))
            .changeAttributeFilterValues("Explorer");
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "24,922,645.37USD");

        dashboardsPage.getContent().getFilterWidget("filter-time").changeTimeFilterValueByClickInTimeLine("2013");
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "No data");

        dashboardsPage.editDashboard();
        widgetConfigPanel = WidgetConfigPanel.openConfigurationPanelFor(
                Iterables.getLast(browser.findElements(HEADLINE_WIDGET_LOCATOR)), browser);
        widgetConfigPanel.getTab(Tab.METRIC, MetricConfigPanel.class)
            .selectMetric(COUNT_OF_PRODUCT);
        widgetConfigPanel.getTab(Tab.FILTERS, FiltersConfigPanel.class)
            .addFiltersToAffectedList(ATTR_PRODUCT);
        widgetConfigPanel.saveConfiguration();
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "7.00USD");

        dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT))
            .changeAttributeFilterValues("TouchAll");
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "1.00USD");
    }

    private String getKeyMetricValue() {
        return Iterables.getLast(browser.findElements(xpath("//*[contains(@class,'indicatorValue')]/span")))
            .getText();
    }

    private void waitForKeyMetricUpdateValue() {
        sleepTight(1000); // need buffer time to make sure css class 'reloading' appear in DOM
        Predicate<WebDriver> valueLoaded = browser -> !waitForElementPresent(
                HEADLINE_WIDGET_LOCATOR, browser).getAttribute("class").contains("reloading") &&
                !isElementPresent(className("c-report-overlap"), browser);
        Graphene.waitGui().until(valueLoaded);
    }
}
