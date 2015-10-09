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

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesFilterMetricsTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Filter-Metrics-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testAddFilterToMetric() {
        addFilterToMetric();
        checkingOpenAsReport("testAddFilterToMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeFilterByNewOne() {
        addFilterToMetric();
        assertTrue(analysisPage.removeAttributeFilterFromMetric(NUMBER_OF_ACTIVITIES)
                .canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
        analysisPage.addFilterMetric(NUMBER_OF_ACTIVITIES, DEPARTMENT, "Inside Sales")
            .waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Inside Sales)",
                NUMBER_OF_ACTIVITIES, DEPARTMENT));
        assertEquals(analysisPage.getFilterMetricText(NUMBER_OF_ACTIVITIES),
                format("%s: Inside Sales", DEPARTMENT));
        assertFalse(analysisPage.canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
        assertFalse(analysisPage.undo()
                .waitForReportComputing()
                .expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
        assertFalse(analysisPage.redo()
                .expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("replaceAttributeFilterByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceMetricHasAttributeFilter() {
        addFilterToMetric();
        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT)
            .expandMetricConfiguration(AMOUNT)
            .waitForReportComputing();
        assertTrue(analysisPage.canAddAnotherAttributeFilterToMetric(AMOUNT));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), AMOUNT);
        checkingOpenAsReport("replaceMetricHasAttributeFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void addAttributeFilterForMultipleMetrics() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .addMetric(AMOUNT)
            .expandMetricConfiguration(AMOUNT);

        ChartReport report = analysisPage.addFilterMetric(NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE, "Email")
            .addFilterMetric(AMOUNT, ACTIVITY_TYPE, "Phone Call", "Web Meeting")
            .waitForReportComputing()
            .getChartReport();

        assertEquals(report.getTrackersCount(), 2);
        assertTrue(isEqualCollection(report.getLegends(),
                asList(format("%s (%s: Email)", NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE),
                        format("%s (%s: Phone Call, Web Meeting)", AMOUNT, ACTIVITY_TYPE))));
        checkingOpenAsReport("addAttributeFilterForMultipleMetrics");
    }

    @Test(dependsOnGroups = {"init"})
    public void searchOnlyAttributeElement() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .addFilterMetricBySelectOnly(NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE, "Email")
            .waitForReportComputing();

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email)", NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterMetricText(NUMBER_OF_ACTIVITIES), format("%s: Email", ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"}, description = "Cover issue: https://jira.intgdc.com/browse/CL-7952")
    public void checkReportWhenFilterContainManyCharacters() {
        String unselectedValue = "14 West";

        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .addFilterMetricWithLargeNumberValues(NUMBER_OF_ACTIVITIES, ACCOUNT, unselectedValue)
            .waitForReportComputing();

        takeScreenshot(browser, "checkReportWhenFilterContainManyCharacters", getClass());
        ChartReport report = analysisPage.getChartReport();
        assertThat(report.getYaxisTitle(), not(containsString("14 West")));
    }

    private void addFilterToMetric() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES);

        assertTrue(analysisPage.canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
        analysisPage.addFilterMetric(NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting")
            .waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertEquals(report.getYaxisTitle(), format("%s (%s: Email, Phone Call, Web Meeting)",
                NUMBER_OF_ACTIVITIES, ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterMetricText(NUMBER_OF_ACTIVITIES),
                format("%s: Email, Phone Call, Web Meeting\n(3)", ACTIVITY_TYPE));
        assertFalse(analysisPage.canAddAnotherAttributeFilterToMetric(NUMBER_OF_ACTIVITIES));
    }
}
