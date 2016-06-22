package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesMetricBucketTest extends GoodSalesAbstractAnalyseTest {

    private static final String EXPECTED = "Expected";
    private static final String REMAINING_QUOTA = "Remaining Quota";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Metric-Bucket-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void checkSeriesStateTransitions() {
        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.addMetric(METRIC_QUOTA).waitForReportComputing();
        sleepTight(3000);
        assertTrue(report.getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);
        assertEquals(analysisPageReact.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));

        analysisPageReact.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPageReact.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("checkSeriesStateTransitions");
    }

    @Test(dependsOnGroups = {"init"})
    public void testBuiltInMetric() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        MetricConfiguration activitiesConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());

        activitiesConfiguration.expandConfiguration();
        analysisPageReact.addMetric(METRIC_AMOUNT);
        assertFalse(activitiesConfiguration.isConfigurationCollapsed());

        MetricConfiguration amountConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT);
        assertTrue(amountConfiguration.isConfigurationCollapsed());

        amountConfiguration.expandConfiguration();
        analysisPageReact.addMetric(METRIC_AMOUNT);
        assertFalse(amountConfiguration.isConfigurationCollapsed());
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromAttribute() {
        analysisPageReact.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE);

        MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACTIVITY_TYPE);
        assertTrue(metricConfiguration.isConfigurationCollapsed());

        metricConfiguration.expandConfiguration();
        assertFalse(isElementPresent(className("s-fact-aggregation-switch"), browser));
        checkingOpenAsReport("testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void showInPercentAndPop() {
        MetricConfiguration metricConfiguration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());

        metricConfiguration.showPercents().showPop();
        analysisPageReact.getAttributesBucket().changeDateDimension("Created");
        analysisPageReact.waitForReportComputing()
            .addMetric(METRIC_AMOUNT, FieldType.FACT);

        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());

        analysisPageReact.undo();
        metricConfiguration.expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isPopSelected());
        assertTrue(metricConfiguration.isShowPercentSelected());

        metricConfiguration = analysisPageReact.replaceMetric("% " + METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertFalse(metricConfiguration.isShowPercentSelected());

        metricConfiguration.collapseConfiguration();
        metricConfiguration = analysisPageReact.replaceMetric(METRIC_AMOUNT, METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertFalse(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("showInPercentAndPop");
    }

    @Test(dependsOnGroups = {"init"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        MetricConfiguration metricConfiguration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(metricConfiguration.isShowPercentEnabled());

        analysisPageReact.addStack(ATTR_DEPARTMENT);
        analysisPageReact.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        checkingOpenAsReport("disablePopCheckboxOnDroppingNonDateAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void uncheckSelectedPopWhenReplaceAttribute() {
        MetricConfiguration metricConfiguration = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(metricConfiguration.isPopEnabled());

        metricConfiguration.showPop();
        assertTrue(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPageReact.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertTrue(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), Arrays.asList(ATTR_ACTIVITY_TYPE));
        checkingOpenAsReport("uncheckSelectedPopWhenReplaceAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceMetricByNewOne() {
        final MetricsBucket metricsBucket = analysisPageReact.getMetricsBucket();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES)));

        analysisPageReact.replaceMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(METRIC_AMOUNT)));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT)));

        analysisPageReact.replaceMetric(METRIC_NUMBER_OF_ACTIVITIES, EXPECTED);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(EXPECTED, METRIC_AMOUNT)));

        analysisPageReact.changeReportType(ReportType.TABLE);
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT, EXPECTED)));

        analysisPageReact.replaceMetric(METRIC_NUMBER_OF_ACTIVITIES, REMAINING_QUOTA);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, METRIC_AMOUNT, EXPECTED)));

        analysisPageReact.undo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT, EXPECTED)));

        analysisPageReact.redo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, METRIC_AMOUNT, EXPECTED)));
        checkingOpenAsReport("replaceMetricByNewOne");
    }
}
