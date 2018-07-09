package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SUM_OF_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.CompareType;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesMetricBucketTest extends AbstractAnalyseTest {

    private static final String SP_YEAR_AGO = " - SP year ago";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Metric-Bucket-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createQuotaMetric();
        metricCreator.createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSeriesStateTransitions() {
        ChartReport report = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .waitForReportComputing()
                .getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());

        analysisPage.addMetric(METRIC_QUOTA).waitForReportComputing();
        sleepTight(3000);
        assertTrue(report.getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("checkSeriesStateTransitions");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testBuiltInMetric() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        MetricConfiguration activitiesConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());

        activitiesConfiguration.expandConfiguration();
        analysisPage.addMetric(METRIC_AMOUNT);
        assertFalse(activitiesConfiguration.isConfigurationCollapsed());

        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT);
        assertTrue(amountConfiguration.isConfigurationCollapsed());

        amountConfiguration.expandConfiguration();
        analysisPage.addMetric(METRIC_AMOUNT);
        assertFalse(amountConfiguration.isConfigurationCollapsed());
        assertTrue(activitiesConfiguration.isConfigurationCollapsed());
        checkingOpenAsReport("testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMetricFromAttribute() {
        initAnalysePage().addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACTIVITY_TYPE);
        assertTrue(metricConfiguration.isConfigurationCollapsed());

        metricConfiguration.expandConfiguration();
        assertFalse(isElementPresent(className("s-fact-aggregation-switch"), browser));
        checkingOpenAsReport("testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showInPercentAndCompare() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertTrue(metricConfiguration.isShowPercentEnabled());

        metricConfiguration.showPercents();
        analysisPage.applyCompareType(CompareType.SAME_PERIOD_LAST_YEAR);
        analysisPage.getAttributesBucket().changeDateDimension("Created");
        analysisPage.waitForReportComputing()
            .addMetric(METRIC_AMOUNT, FieldType.FACT);

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(
                METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO,
                METRIC_NUMBER_OF_ACTIVITIES,
                METRIC_SUM_OF_AMOUNT + SP_YEAR_AGO,
                METRIC_SUM_OF_AMOUNT
        ));

        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);
        assertFalse(metricConfiguration.isShowPercentEnabled());

        analysisPage.undo();
        metricConfiguration.expandConfiguration();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(
                "% " + METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO,
                "% " + METRIC_NUMBER_OF_ACTIVITIES));

        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        metricConfiguration = analysisPage
                .removeMetric("% " + METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_AMOUNT)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isShowPercentSelected());

        metricConfiguration.collapseConfiguration();
        metricConfiguration = analysisPage
            .removeMetric(METRIC_AMOUNT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_NUMBER_OF_ACTIVITIES));
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("showInPercentAndCompare");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(metricConfiguration.isShowPercentEnabled());

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        checkingOpenAsReport("disablePopCheckboxOnDroppingNonDateAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void compareIsStillActiveWhenReplaceAttribute() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.applyCompareType(CompareType.SAME_PERIOD_LAST_YEAR);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));

        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + SP_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
        checkingOpenAsReport("compareIsStillActiveWhenReplaceAttribute");
    }
}
