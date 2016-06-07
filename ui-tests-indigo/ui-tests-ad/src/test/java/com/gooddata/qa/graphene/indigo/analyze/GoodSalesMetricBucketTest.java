package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
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
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesMetricBucketTest extends GoodSalesAbstractAnalyseTest {

    private static final String EXPECTED = "Expected";
    private static final String REMAINING_QUOTA = "Remaining Quota";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Metric-Bucket-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void checkSeriesStateTransitions() {
        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addDate()
                .getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isPopEnabled());
        assertTrue(metricConfiguration.isShowPercentEnabled());
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addMetric(QUOTA).waitForReportComputing();
        sleepTight(3000);
        assertTrue(report.getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertEquals(report.getLegends(), asList(NUMBER_OF_ACTIVITIES, QUOTA));
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(NUMBER_OF_ACTIVITIES, QUOTA));

        analysisPage.addMetric(SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(NUMBER_OF_ACTIVITIES, QUOTA, SNAPSHOT_BOP));
        checkingOpenAsReport("checkSeriesStateTransitions");
    }

    @Test(dependsOnGroups = {"init"})
    public void testBuiltInMetric() {
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
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"init"})
    public void testMetricFromAttribute() {
        analysisPage.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ACTIVITY_TYPE);
        assertTrue(metricConfiguration.isConfigurationCollapsed());

        metricConfiguration.expandConfiguration();
        assertFalse(isElementPresent(className("s-fact-aggregation-switch"), browser));
        checkingOpenAsReport("testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void showInPercentAndPop() {
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
        checkingOpenAsReport("showInPercentAndPop");
    }

    @Test(dependsOnGroups = {"init"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(metricConfiguration.isShowPercentEnabled());

        analysisPage.addStack(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        checkingOpenAsReport("disablePopCheckboxOnDroppingNonDateAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void uncheckSelectedPopWhenReplaceAttribute() {
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(metricConfiguration.isPopEnabled());

        metricConfiguration.showPop();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.replaceAttribute(DATE, ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), Arrays.asList(ACTIVITY_TYPE));
        checkingOpenAsReport("uncheckSelectedPopWhenReplaceAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceMetricByNewOne() {
        final MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(AMOUNT)));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES, AMOUNT)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, EXPECTED);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(EXPECTED, AMOUNT)));

        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, REMAINING_QUOTA);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));

        analysisPage.undo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.redo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));
        checkingOpenAsReport("replaceMetricByNewOne");
    }
}
