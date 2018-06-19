package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_TASK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_HISTORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import org.testng.annotations.Test;

import java.util.List;

public class MultipleAttributeFilteringTest extends AbstractDashboardTest {

    private AnalysisPage analysisPage;
    private MetricConfiguration metricConfiguration;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Multiple-Attribute-Filtering-Test";
    }

    @Override
    protected void customizeProject() {
        getMetricCreator().createTimelineBOPMetric();
        getMetricCreator().createTimelineEOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkAddAttributeFilterButtonTest() {
        analysisPage = initAnalysePage();
        addMetricWithTwoAttributeFilter();
        assertEquals(metricConfiguration.getAllFilterText(), asList(ATTR_ACCOUNT + ":All"));
        assertTrue(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button is not displayed");

        metricConfiguration = analysisPage
            .addMetric(METRIC_TIMELINE_EOP)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_TIMELINE_EOP)
            .expandConfiguration()
            .addFilterWithAllValue(ATTR_ACCOUNT);

        takeScreenshot(browser, "check-add-attribute-filter-button-test", getClass());
        assertEquals(metricConfiguration.getFilterText(), ATTR_ACCOUNT + ":All");
        assertTrue(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button is not displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkAttributeFilterButtonAfterUndoOrRedoTest() {
        analysisPage = initAnalysePage();
        addMetricWithTwoAttributeFilter();

        //Undo
        metricConfiguration = analysisPage.undo().getMetricsBucket().getMetricConfiguration(METRIC_TIMELINE_BOP).expandConfiguration();
        takeScreenshot(browser, "check-add-attribute-filter-button-after-undo-test", getClass());
        assertEquals(metricConfiguration.getAllFilterText(), asList(ATTR_ACCOUNT + ":All"));
        assertTrue(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button is not displayed");

        //Redo
        metricConfiguration = analysisPage.redo().getMetricsBucket().getMetricConfiguration(METRIC_TIMELINE_BOP).expandConfiguration();
        takeScreenshot(browser, "check-add-attribute-filter-button-after-redo-test", getClass());
        assertEquals(metricConfiguration.getAllFilterText(), asList(ATTR_ACCOUNT + ":All"));
        assertTrue(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button is not displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void limitAttributeFiltersPerMetricTest() {
        analysisPage = initAnalysePage();
        addMetricWithTwoAttributeFilter();
        final List<String> attributeFilterList = asList(ATTR_ACCOUNT, ATTR_ACTIVITY_TYPE, ATTR_ACTIVITY, ATTR_DEPARTMENT,
            ATTR_FORECAST_CATEGORY, ATTR_IS_ACTIVE, ATTR_IS_CLOSED, ATTR_IS_CLOSED, ATTR_IS_TASK, ATTR_IS_WON, ATTR_OPP_SNAPSHOT,
            ATTR_OPPORTUNITY, ATTR_PRIORITY, ATTR_PRODUCT, ATTR_REGION, ATTR_SALES_REP, ATTR_STAGE_HISTORY, ATTR_STAGE_NAME,
            ATTR_STATUS, ATTR_STATUS);
        attributeFilterList.subList(2, 20).stream().forEach(attribute -> metricConfiguration.addFilterWithAllValue(attribute));

        takeScreenshot(browser, "limit-attribute-filters-per-metric-test", getClass());
        assertFalse(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button shouldn't be displayed");
        assertTrue(metricConfiguration.removeFilter().canAddAnotherFilter(), "Add attribute filter button is not displayed");

        //Have to collapse due to list be very long
        //To avoid any bug relate to over lapse
        metricConfiguration.collapseConfiguration();
        metricConfiguration = analysisPage
            .addMetric(METRIC_TIMELINE_EOP)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_TIMELINE_EOP)
            .expandConfiguration();
        attributeFilterList.stream().forEach(attribute -> metricConfiguration.addFilterWithAllValue(attribute));
        assertFalse(metricConfiguration.canAddAnotherFilter(), "Add attribute filter button shouldn't be displayed");
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDuplicatedAttributeFilter() {
        analysisPage = initAnalysePage();
        addMetricWithTwoAttributeFilter();

        //duplicated filter on metric
        assertTrue(metricConfiguration.isDisabledAttribute(ATTR_ACCOUNT), "Duplicated attribute filter is not apply");

        //duplicated filter on other metric
        metricConfiguration = analysisPage
            .addMetric(METRIC_TIMELINE_EOP)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_TIMELINE_EOP)
            .expandConfiguration()
            .addFilterWithAllValue(ATTR_ACCOUNT);

        assertEquals(metricConfiguration.getFilterText(), ATTR_ACCOUNT + ":All");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addRemovedAttributeFilter() {
        analysisPage = initAnalysePage();
        addMetricWithTwoAttributeFilter();

        assertFalse(metricConfiguration.removeAttributeFilter(ATTR_ACTIVITY_TYPE + ":All").isDisabledAttribute(ATTR_ACTIVITY_TYPE),
            ATTR_ACTIVITY_TYPE + " can add after removed");
    }

    private void addMetricWithTwoAttributeFilter() {
        metricConfiguration = analysisPage
            .addMetric(METRIC_TIMELINE_BOP)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_TIMELINE_BOP)
            .expandConfiguration()
            .addFilterWithAllValue(ATTR_ACCOUNT)
            .addFilterWithAllValue(ATTR_ACTIVITY_TYPE);
    }
}
