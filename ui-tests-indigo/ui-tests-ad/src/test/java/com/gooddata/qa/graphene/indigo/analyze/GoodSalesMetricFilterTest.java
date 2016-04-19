package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesMetricFilterTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Metric-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testAddFilterToMetric() {
        addFilterToMetric();
        checkingOpenAsReport("testAddFilterToMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeFilterByNewOne() {
        addFilterToMetric();
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES);

        assertTrue(metricConfiguration.removeFilter().canAddAnotherFilter());

        metricConfiguration.addFilter(DEPARTMENT, "Inside Sales");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Inside Sales)",
                NUMBER_OF_ACTIVITIES, DEPARTMENT));
        assertEquals(metricConfiguration.getFilterText(),
                format("%s: Inside Sales", DEPARTMENT));
        assertFalse(metricConfiguration.canAddAnotherFilter());

        analysisPage.undo()
            .waitForReportComputing();
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.canAddAnotherFilter());

        analysisPage.redo()
            .waitForReportComputing();
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.canAddAnotherFilter());
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("replaceAttributeFilterByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceMetricHasAttributeFilter() {
        addFilterToMetric();
        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT)
            .waitForReportComputing();
        assertTrue(analysisPage.getMetricsBucket()
                .getMetricConfiguration(AMOUNT)
                .expandConfiguration()
                .canAddAnotherFilter());
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), AMOUNT);
        checkingOpenAsReport("replaceMetricHasAttributeFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void addAttributeFilterForMultipleMetrics() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilter(DEPARTMENT, "Direct Sales");

        analysisPage.addMetric(AMOUNT)
            .getMetricsBucket()
            .getMetricConfiguration(AMOUNT)
            .expandConfiguration()
            .addFilter(DEPARTMENT, "Inside Sales");

        ChartReport report = analysisPage.waitForReportComputing()
            .getChartReport();

        assertEquals(report.getTrackersCount(), 2);
        assertTrue(isEqualCollection(report.getLegends(),
                asList(format("%s (%s: Direct Sales)", NUMBER_OF_ACTIVITIES, DEPARTMENT),
                        format("%s (%s: Inside Sales)", AMOUNT, DEPARTMENT))));
        checkingOpenAsReport("addAttributeFilterForMultipleMetrics");
    }

    @Test(dependsOnGroups = {"init"})
    public void searchOnlyAttributeElement() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilterBySelectOnly(ACTIVITY_TYPE, "Email");

        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email)", NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE));
        assertEquals(metricConfiguration.getFilterText(), format("%s: Email", ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"}, description = "Cover issue: https://jira.intgdc.com/browse/CL-7952")
    public void checkReportWhenFilterContainManyCharacters() {
        String unselectedValue = "14 West";

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .addFilterWithLargeNumberValues(ACCOUNT, unselectedValue);
        analysisPage.waitForReportComputing();

        takeScreenshot(browser, "checkReportWhenFilterContainManyCharacters", getClass());
        ChartReport report = analysisPage.getChartReport();
        assertThat(report.getYaxisTitle(), not(containsString("14 West")));
    }

    private void addFilterToMetric() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();

        assertTrue(metricConfiguration.canAddAnotherFilter());

        metricConfiguration.addFilter(ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email, Phone Call, Web Meeting)",
                NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE));
        assertEquals(metricConfiguration.getFilterText(),
                format("%s: Email, Phone Call, Web Meeting\n(3)", ACTIVITY_TYPE));
        assertFalse(metricConfiguration.canAddAnotherFilter());
    }
}
