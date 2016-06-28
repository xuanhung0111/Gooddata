package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createFilterVariable;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Calendar.YEAR;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.FiltersConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.MetricConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.MetricStyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesKeyMetricTest extends GoodSalesAbstractTest {

    private static final String VARIABLE_NAME = "F_" + ATTR_PRODUCT;

    private static final String METRIC_VARIABLE = "Metric_Variable";
    private static final String COUNT_OF_PRODUCT = "# " + ATTR_PRODUCT;

    private static final String DASHBOARD_NAME = "Test dashboard";
    private static final int YEAR_OF_DATA = 2011;

    private static final By HEADLINE_WIDGET_LOCATOR = className("yui3-c-headlinedashboardwidget");

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-key-metric-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init"})
    public void setupPrecondition() throws ParseException, JSONException, IOException {
        String productUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_PRODUCT));
        String variableUri = createFilterVariable(getRestApiClient(), testParams.getProjectId(), VARIABLE_NAME, productUri);

        createMetric(COUNT_OF_PRODUCT, format("SELECT COUNT([%s])", productUri), "#,##0");

        createMetric(METRIC_VARIABLE, format("SELECT [%s] WHERE [%s]", getMdService().getObjUri(getProject(),
                Metric.class, title(METRIC_AMOUNT)), variableUri), "#,##0");
    }

    @Test(dependsOnGroups = {"init"})
    public void editHeadlineWidget() {
        initDashboardsPage()
            .addNewDashboard(DASHBOARD_NAME)
            .selectDashboard(DASHBOARD_NAME);

        dashboardsPage.editDashboard()
            .addTimeFilterToDashboard(2, format("%s ago", Calendar.getInstance().get(YEAR) - YEAR_OF_DATA));
        DashboardWidgetDirection.UP.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget("filter-time").getRoot());

        dashboardsPage.getDashboardEditBar()
            .addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, ATTR_PRODUCT);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT)).getRoot());

        dashboardsPage.getDashboardEditBar()
            .addListFilterToDashboard(DashFilterTypes.PROMPT, VARIABLE_NAME);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(
                dashboardsPage.getContent().getFilterWidget(simplifyText(VARIABLE_NAME)).getRoot());

        waitForElementVisible(className("s-btn-widget"), browser).click();
        Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(className("yui3-c-adddashboardwidgetpickerpanel-content"), browser))
                .initWidget(WidgetTypes.KEY_METRIC_WITH_TREND);

        WidgetConfigPanel widgetConfigPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        MetricConfigPanel metricConfigPanel = widgetConfigPanel.getTab(Tab.METRIC, MetricConfigPanel.class);
        metricConfigPanel.selectMetric(METRIC_AMOUNT, "Created");
        assertTrue(waitForFragmentVisible(metricConfigPanel).isWhenDropdownVisibled());
        assertFalse(waitForFragmentVisible(metricConfigPanel).isWhenDropdownEnabled());
        assertTrue(waitForFragmentVisible(metricConfigPanel).isLinkExternalFilterVisible());
        assertTrue(waitForFragmentVisible(metricConfigPanel).isLinkExternalFilterSelected());
        widgetConfigPanel.getTab(Tab.METRIC_STYLE, MetricStyleConfigPanel.class)
            .editMetricFormat("#,##0.00USD");
        FiltersConfigPanel filtersConfigPanel = widgetConfigPanel.getTab(Tab.FILTERS, FiltersConfigPanel.class);
        assertTrue(isEqualCollection(filtersConfigPanel.getAllFilters(),
                asList("Date dimension (Created)", VARIABLE_NAME, ATTR_PRODUCT)));
        assertTrue(isEqualCollection(filtersConfigPanel.getAllDisabledFilters(), singleton(VARIABLE_NAME)));
        filtersConfigPanel.removeFiltersFromSelectedList(ATTR_PRODUCT);
        widgetConfigPanel.getTab(Tab.METRIC, MetricConfigPanel.class)
            .selectMetric(METRIC_VARIABLE, "Created");
        widgetConfigPanel.saveConfiguration();
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "60,270,072.20USD");

        dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT))
            .changeAttributeFilterValue("TouchAll");
        waitForKeyMetricUpdateValue();
        assertEquals(getKeyMetricValue(), "60,270,072.20USD");

        dashboardsPage.getContent().getFilterWidget(simplifyText(VARIABLE_NAME))
            .changeAttributeFilterValue("Explorer");
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
            .changeAttributeFilterValue("TouchAll");
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
                HEADLINE_WIDGET_LOCATOR, browser).getAttribute("class").contains("reloading");
        Graphene.waitGui().until(valueLoaded);
    }
}