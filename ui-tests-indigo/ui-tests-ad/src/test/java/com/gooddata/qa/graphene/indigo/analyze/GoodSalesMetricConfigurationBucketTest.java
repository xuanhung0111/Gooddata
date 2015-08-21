package com.gooddata.qa.graphene.indigo.analyze;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;

public class GoodSalesMetricConfigurationBucketTest extends AnalyticalDesignerAbstractTest {

    private static final String DURATION = "Duration";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Metric-Configuration-Bucket";
    }

    @Test(dependsOnGroups = {"init"})
    public void testBuiltInMetric() {
        initAnalysePage();
        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .isMetricConfigurationCollapsed(NUMBER_OF_ACTIVITIES));

        assertFalse(analysisPage.expandMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .addMetric(AMOUNT).isMetricConfigurationCollapsed(NUMBER_OF_ACTIVITIES));

        assertTrue(analysisPage.isMetricConfigurationCollapsed(AMOUNT));

        assertFalse(analysisPage.expandMetricConfiguration(AMOUNT)
                .addMetric(AMOUNT).isMetricConfigurationCollapsed(AMOUNT));
        assertFalse(analysisPage.isMetricConfigurationCollapsed(NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("MetricConfigurationBucket-testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromFact() {
        String sumOfAmount = "Sum of " + AMOUNT;
        String sumOfDuration = "Sum of " + DURATION;
        String averageAmount = "Avg " + AMOUNT;
        String runningSumOfDuration = "Runsum of " + DURATION;

        initAnalysePage();
        assertTrue(analysisPage.addMetricFromFact(AMOUNT)
                .isMetricConfigurationCollapsed(sumOfAmount));

        analysisPage.expandMetricConfiguration(sumOfAmount).changeMetricAggregation(sumOfAmount, "Average");
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), singleton(averageAmount)));

        assertTrue(analysisPage.addMetricFromFact(DURATION)
                .isMetricConfigurationCollapsed(sumOfDuration));

        analysisPage.expandMetricConfiguration(sumOfDuration)
            .changeMetricAggregation(sumOfDuration, "Running sum");
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(),
                asList(averageAmount, runningSumOfDuration)));

        assertFalse(analysisPage.isMetricConfigurationCollapsed(averageAmount));
        assertFalse(analysisPage.isMetricConfigurationCollapsed(runningSumOfDuration));
        checkingOpenAsReport("MetricConfigurationBucket-testMetricFromFact");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromAttribute() {
        String countOfActivityType = "Count of " + ACTIVITY_TYPE;

        initAnalysePage();
        assertTrue(analysisPage.addMetricFromAttribute(ACTIVITY_TYPE)
                .isMetricConfigurationCollapsed(countOfActivityType));

        analysisPage.expandMetricConfiguration(countOfActivityType);
        assertTrue(browser.findElements(MetricsBucket.BY_FACT_AGGREGATION).isEmpty());
        checkingOpenAsReport("MetricConfigurationBucket-testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void showInPercentAndPop() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE))
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES);

        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigEnabled());

        analysisPage.turnOnShowInPercents()
            .compareToSamePeriodOfYearBefore()
            .waitForReportComputing()
            .addMetricFromFact(AMOUNT);

        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertFalse(analysisPage.isShowPercentConfigEnabled());

        analysisPage.undo()
            .expandMetricConfiguration("% " + NUMBER_OF_ACTIVITIES);
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isCompareSamePeriodConfigSelected());
        assertTrue(analysisPage.isShowPercentConfigSelected());

        analysisPage.replaceMetric("% " + NUMBER_OF_ACTIVITIES, AMOUNT)
            .expandMetricConfiguration(AMOUNT);
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigSelected());
        assertFalse(analysisPage.isShowPercentConfigSelected());

        analysisPage.collapseMetricConfiguration(AMOUNT)
            .replaceMetric(AMOUNT, NUMBER_OF_ACTIVITIES)
            .expandMetricConfiguration(NUMBER_OF_ACTIVITIES);
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigSelected());
        assertFalse(analysisPage.isShowPercentConfigSelected());
        checkingOpenAsReport("MetricConfigurationBucket-showInPercentAndPop");
    }
}
