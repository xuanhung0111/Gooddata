package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SUM_OF_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SUM_OF_AMOUNT_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class GoodSalesMetricBucketTest extends AbstractAnalyseTest {

    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Metric-Bucket-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createQuotaMetric();
        metricCreator.createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSeriesStateTransitions() {
        ChartReport report = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .waitForReportComputing()
                .getChartReport();
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");

        analysisPage.addMetric(METRIC_QUOTA).waitForReportComputing();
        sleepTight(3000);
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent shouldn't be enabled");
        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1, "Tracker should display");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("checkSeriesStateTransitions");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testBuiltInMetric() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        MetricConfiguration activitiesConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(activitiesConfiguration.isConfigurationCollapsed(), "Configuration should collapse");

        activitiesConfiguration.expandConfiguration();
        analysisPage.addMetric(METRIC_AMOUNT);
        assertFalse(activitiesConfiguration.isConfigurationCollapsed(), "Configuration should expand");

        MetricConfiguration amountConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT);
        assertTrue(amountConfiguration.isConfigurationCollapsed(), "Configuration should collapse");

        amountConfiguration.expandConfiguration();
        analysisPage.addMetric(METRIC_AMOUNT);
        assertFalse(amountConfiguration.isConfigurationCollapsed(), "Configuration should expand");
        assertTrue(activitiesConfiguration.isConfigurationCollapsed(), "Configuration should collapse");
        checkingOpenAsReport("testBuiltInMetric");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMetricFromAttribute() {
        initAnalysePage().addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACTIVITY_TYPE);
        assertTrue(metricConfiguration.isConfigurationCollapsed(), "Configuration should collapse");

        metricConfiguration.expandConfiguration();
        assertFalse(isElementPresent(className("s-fact-aggregation-switch"), browser),
                "Aggregation switcher shouldn't present");
        checkingOpenAsReport("testMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showInPercentAndCompare() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");

        metricConfiguration.showPercents();

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        analysisPage.getAttributesBucket().changeDateDimension("Created");
        analysisPage.waitForReportComputing()
            .addMetric(METRIC_AMOUNT, FieldType.FACT);

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(
                METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO,
                METRIC_NUMBER_OF_ACTIVITIES,
                METRIC_SUM_OF_AMOUNT_YEAR_AGO,
                METRIC_SUM_OF_AMOUNT
        ));

        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES);
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent should be disabled");

        analysisPage.undo();
        metricConfiguration.expandConfiguration();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(
                "% " + METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO,
                "% " + METRIC_NUMBER_OF_ACTIVITIES));

        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");
        assertTrue(metricConfiguration.isShowPercentSelected(), "Show percent should be selected");

        metricConfiguration = analysisPage
                .removeMetric("% " + METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_AMOUNT)
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");
        assertFalse(metricConfiguration.isShowPercentSelected(), "Show percent shouldn't be selected");

        metricConfiguration.collapseConfiguration();
        metricConfiguration = analysisPage
            .removeMetric(METRIC_AMOUNT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_NUMBER_OF_ACTIVITIES));
        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");
        assertFalse(metricConfiguration.isShowPercentSelected(), "Show percent shouldn't be selected");
        checkingOpenAsReport("showInPercentAndCompare");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        MetricConfiguration metricConfiguration = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(metricConfiguration.isShowPercentEnabled(), "Show percent should be enabled");

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent should be disabled");
        checkingOpenAsReport("disablePopCheckboxOnDroppingNonDateAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void compareIsStillActiveWhenReplaceAttribute() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate().addAttribute(ATTR_IS_CLOSED);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Tracker should display");

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));

        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Tracker should display");

        analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Tracker should display");
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_IS_CLOSED));
        checkingOpenAsReport("compareIsStillActiveWhenReplaceAttribute");
    }
}
