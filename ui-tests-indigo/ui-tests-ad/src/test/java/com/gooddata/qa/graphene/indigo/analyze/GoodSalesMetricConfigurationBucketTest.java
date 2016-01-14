package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;

public class GoodSalesMetricConfigurationBucketTest extends AnalyticalDesignerAbstractTest {

    private static final String DURATION = "Duration";
    private static final String ACTIVITY_DATE = "Activity (Date)";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Metric-Configuration-Bucket";
    }

    @Test(dependsOnGroups = {"init"})
    public void testBuiltInMetric() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        MetricConfiguration activitiesConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES);
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());

        activitiesConfiguration.expandConfiguration();
        analysisPage.addMetric(AMOUNT);
        assertFalse(activitiesConfiguration.isConfigurationCollapsed());

        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(AMOUNT);
        assertTrue(amountConfiguration.isConfigurationCollapsed());

        amountConfiguration.expandConfiguration();
        analysisPage.addMetric(AMOUNT);
        assertFalse(amountConfiguration.isConfigurationCollapsed());
        assertFalse(activitiesConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("MetricConfigurationBucket-testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromFact() {
        String sumOfAmount = "Sum of " + AMOUNT;
        String sumOfDuration = "Sum of " + DURATION;
        String averageAmount = "Avg " + AMOUNT;
        String runningSumOfDuration = "Runsum of " + DURATION;

        initAnalysePage();

        analysisPage.addMetric(AMOUNT, FieldType.FACT);
        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfAmount);
        assertTrue(amountConfiguration.isConfigurationCollapsed());

        amountConfiguration.expandConfiguration().changeAggregation("Average");
        assertTrue(isEqualCollection(analysisPage.getMetricsBucket().getItemNames(), singleton(averageAmount)));

        analysisPage.addMetric(DURATION, FieldType.FACT);
        MetricConfiguration durationConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(sumOfDuration);
        assertTrue(durationConfiguration.isConfigurationCollapsed());

        durationConfiguration.expandConfiguration().changeAggregation("Running sum");
        assertTrue(isEqualCollection(analysisPage.getMetricsBucket().getItemNames(),
                asList(averageAmount, runningSumOfDuration)));

        assertFalse(amountConfiguration.isConfigurationCollapsed());
        assertFalse(durationConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("MetricConfigurationBucket-testMetricFromFact");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromAttribute() {
        initAnalysePage();
        analysisPage.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ACTIVITY_TYPE);
        assertTrue(metricConfiguration.isConfigurationCollapsed());

        metricConfiguration.expandConfiguration();
        assertFalse(isElementPresent(className("s-fact-aggregation-switch"), browser));
        checkingOpenAsReport("MetricConfigurationBucket-testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void showInPercentAndPop() {
        initAnalysePage();
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());

        metricConfiguration.showPercents().showPop();
        analysisPage.getAttributesBucket().changeDateDimension("Created");
        analysisPage.waitForReportComputing()
            .addMetric(AMOUNT, FieldType.FACT);

        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());

        analysisPage.undo();
        metricConfiguration.expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isPopSelected());
        assertTrue(metricConfiguration.isShowPercentSelected());

        metricConfiguration = analysisPage.replaceMetric("% " + NUMBER_OF_ACTIVITIES, AMOUNT)
                .getMetricsBucket()
                .getMetricConfiguration(AMOUNT)
                .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertFalse(metricConfiguration.isShowPercentSelected());

        metricConfiguration.collapseConfiguration();
        metricConfiguration = analysisPage.replaceMetric(AMOUNT, NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertFalse(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("MetricConfigurationBucket-showInPercentAndPop");
    }

    @DataProvider(name = "factMetricCombination")
    public Object[][] factMetricCombination() {
        return new Object[][] {
            {false, false},
            {true, false},
            {false, true},
            {true, true}
        };
    }

    @Test(dependsOnGroups = {"init"}, dataProvider = "factMetricCombination")
    public void shouldNotCreateDuplicateMetricFromFact(boolean pop, boolean percent) {
        initAnalysePage();
        MetricConfiguration configuration = analysisPage.addDate()
            .addMetric(ACTIVITY_DATE, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + ACTIVITY_DATE)
            .expandConfiguration();

        if (pop) configuration.showPop();
        if (percent) configuration.showPercents();

        // css class of metric from fact will be somehow like this:
        // class="s-bucket-item s-id-dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968 ..."
        // and identifier will be dt_activity_activity_generated_sum_9b39e371f6bc8e93b15843c6794f6968
        final String identifier = Stream.of(analysisPage.getMetricsBucket()
            .get((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];

        configuration = analysisPage.removeMetric((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .addMetric(ACTIVITY_DATE, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + ACTIVITY_DATE)
            .expandConfiguration();

        if (pop) configuration.showPop();
        if (percent) configuration.showPercents();

        assertTrue(analysisPage.getMetricsBucket()
            .get((percent ? "% " : "") + "Sum of " + ACTIVITY_DATE)
            .getAttribute("class")
            .contains(identifier));

        if (!pop && !percent) {
            analysisPage.addMetric(ACTIVITY_DATE, FieldType.FACT);
            assertEquals(browser.findElements(className("s-id-" + identifier)).size(), 2);
        }
    }
}
