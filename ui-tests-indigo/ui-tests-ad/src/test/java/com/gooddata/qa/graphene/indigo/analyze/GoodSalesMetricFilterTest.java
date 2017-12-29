package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesMetricFilterTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Metric-Filter-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-10362 Attribute filter didn't connect with measure")
    public void makeSureAttributeFilterConnectWithMeasure() {
        assertTrue(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getAllAttributesInViewPort()
            .size() > 0);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAddFilterToMetric() {
        addFilterToMetric();
        checkingOpenAsReport("testAddFilterToMetric");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceAttributeFilterByNewOne() {
        addFilterToMetric();
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);

        assertTrue(metricConfiguration.removeFilter().canAddAnotherFilter());

        metricConfiguration.addFilter(ATTR_DEPARTMENT, "Inside Sales");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Inside Sales)",
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT));
        assertEquals(metricConfiguration.getFilterText(),
                format("%s: Inside Sales", ATTR_DEPARTMENT));

        analysisPage.undo()
            .waitForReportComputing();
        metricConfiguration.expandConfiguration();

        analysisPage.redo()
            .waitForReportComputing();
        metricConfiguration.expandConfiguration();
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("replaceAttributeFilterByNewOne");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addAttributeFilterForMultipleMetrics() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.addMetric(METRIC_AMOUNT)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration()
            .addFilter(ATTR_DEPARTMENT, "Inside Sales");

        ChartReport report = analysisPage.waitForReportComputing()
            .getChartReport();

        assertEquals(report.getTrackersCount(), 2);
        assertTrue(isEqualCollection(report.getLegends(),
                asList(format("%s (%s: Direct Sales)", METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT),
                        format("%s (%s: Inside Sales)", METRIC_AMOUNT, ATTR_DEPARTMENT))));
        checkingOpenAsReport("addAttributeFilterForMultipleMetrics");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchOnlyAttributeElement() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilterBySelectOnly(ATTR_ACTIVITY_TYPE, "Email");

        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email)", METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE));
        assertEquals(metricConfiguration.getFilterText(), format("%s: Email", ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "Cover issue: https://jira.intgdc.com/browse/CL-7952")
    public void checkReportWhenFilterContainManyCharacters() {
        String unselectedValue = "14 West";

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilterWithLargeNumberValues(ATTR_ACCOUNT, unselectedValue);
        analysisPage.waitForReportComputing();

        takeScreenshot(browser, "checkReportWhenFilterContainManyCharacters", getClass());
        ChartReport report = analysisPage.getChartReport();
        assertThat(report.getYaxisTitle(), not(containsString("14 West")));
    }

    private void addFilterToMetric() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertTrue(metricConfiguration.canAddAnotherFilter());

        metricConfiguration.addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email, Phone Call, Web Meeting)",
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE));
        assertEquals(metricConfiguration.getFilterText(),
                format("%s: Email, Phone Call, Web Meeting\n(3)", ATTR_ACTIVITY_TYPE));
    }
}
