package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DateFilterADMeasureCombineDateTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER =
            "Insight has 2 measures (M1 has measure date filter, M2 doesn’t have), Date 1 on View by and Date 1 on Filter";
    private static final String INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER =
            "Insight has 2 measures (M1 has measure date filter, M2 doesn’t have), Date 1 on Filter only";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private static final String DATE_FILTER_THIS_MONTH = "This month";
    private static final String DATE_FILTER_LAST_YEAR = "Last year";
    //TODO workaround for bug QA-9139 Update Graphene Test for year 2019
    //Will be changed evert year
    private static final String FOUR_YEARS_AGO = "four years ago";
    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Filter-AD-Measures-Test-Part2";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();

        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnAllTimeADAndAllTimeKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME);

        List<List<String>> expectedValues = asList(
                asList("2010", "–", "$15,043.52"),
                asList("2011", "–", "$20,578.25"),
                asList("2012", "–", "$21,881.00"),
                asList("2013", "–", "$66,436.38"),
                asList("2014", "–", "$8,875.86"),
                asList("2016", "–", "–"),
                asList("2017", "$3,644.00", "$3,644.00")
        );
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.ALL_TIME, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        expectedValues = singletonList(asList("$3,644.00", "$20,286.22"));
        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.ALL_TIME, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnAllTimeADAndThisTimeKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_ALL_TIME);

        List<List<String>> expectedValues = singletonList(asList("2017", "$3,644.00", "–"));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        expectedValues = singletonList(asList("$3,644.00", "–"));
        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnThisMonthADAndAllTimeKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);

        List<List<String>> expectedValues = asList(
                asList("2010", "–", "$15,043.52"),
                asList("2011", "–", "$20,578.25"),
                asList("2012", "–", "$21,881.00"),
                asList("2013", "–", "$66,436.38"),
                asList("2014", "–", "$8,875.86"),
                asList("2016", "–", "–"),
                asList("2017", "$3,644.00", "$3,644.00")
        );
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.ALL_TIME, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        expectedValues = singletonList(asList("$3,644.00", "$20,286.22"));
        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.ALL_TIME, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnThisMonthADAndLastYearKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);

        List<List<String>> expectedValues = singletonList(asList("2017", "$3,644.00", "–"));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.LAST_YEAR, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        expectedValues = singletonList(asList("$3,644.00", "–"));
        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.LAST_YEAR, indigoDashboardsPage);

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnLastYearADAndThisMonthKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_LAST_YEAR);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_LAST_YEAR);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);

        List<List<String>> expectedValues = singletonList(asList("2017", "$3,644.00", "–"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);

        expectedValues = singletonList(asList("$3,644.00", "–"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnLastYearADAndThisMonthKDDifferentDateDimension() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_LAST_YEAR);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_LAST_YEAR);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.THIS_MONTH, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");

        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.THIS_MONTH, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnThisMonthADAndLastYearKDDifferentDateDimension() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.LAST_YEAR, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");

        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.LAST_YEAR, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnThisMonthADKDDifferentDateDimension() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.THIS_MONTH, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");

        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CREATED, DateRange.THIS_MONTH, indigoDashboardsPage);
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class)
            .isEmptyValue(), "The empty state on Insight is not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineDateOnThisMonthADAndUncheckedDateKD() {
        createInsightUsingDateViewByOnAD(
                INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);
        createInsightUsingDateFilterOnAD(
                INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, DATE_DATASET_CLOSED, DATE_FILTER_THIS_MONTH);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_DATE_ON_VIEWBY_AND_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();

        List<List<String>> expectedValues = singletonList(asList("2017", "$3,644.00", "–"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);

        indigoDashboardsPage = initIndigoDashboardsPage();
        addInsightOnKD(INSIGHT_HAS_TWO_MEASURE_ONLY_DATE_ON_FILTER, indigoDashboardsPage);
        configIndigoDashboard(DATE_DATASET_CLOSED, DateRange.THIS_MONTH, indigoDashboardsPage);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();

        expectedValues = singletonList(asList("$3,644.00", "–"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport()
            .getBodyContent(), expectedValues);
    }

    private void addInsightOnKD(String insight, IndigoDashboardsPage indigoDashboardsPage) {
        indigoDashboardsPage.addDashboard().addInsight(insight);
    }

    private void configIndigoDashboard(String dateDimension, DateRange dateRange,
                                       IndigoDashboardsPage indigoDashboardsPage) {
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(dateRange).apply();
        indigoDashboardsPage.selectFirstWidget(Insight.class);
        indigoDashboardsPage.getConfigurationPanel().selectDateDataSetByName(dateDimension);
        indigoDashboardsPage.waitForWidgetsLoading();
    }

    private void createInsightUsingDateFilterOnAD(String insight, String switchDimension, String periodTime) {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT)
                .addDateFilter().waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().addFilterByDate(FOUR_YEARS_AGO);
        analysisPage.getFilterBuckets().getDateFilter().click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changePeriodWithScrollbar(DateRange.THIS_YEAR.toString()).apply();
        analysisPage.waitForReportComputing();
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().changeDateDimension(switchDimension);
        analysisPage.waitForReportComputing().saveInsight(insight).waitForReportComputing();
    }

    private void createInsightUsingDateViewByOnAD(String insight, String switchDimension, String periodTime) {
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
                .addMetric(METRIC_AVG_AMOUNT).addDate().waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(FOUR_YEARS_AGO);
        analysisPage.getFilterBuckets().getDateFilter().click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changePeriodWithScrollbar(DateRange.THIS_YEAR.toString()).apply();
        analysisPage.waitForReportComputing();
        analysisPage.getAttributesBucket().changeDateDimension(switchDimension);
        analysisPage.waitForReportComputing().saveInsight(insight).waitForReportComputing();
    }
}
