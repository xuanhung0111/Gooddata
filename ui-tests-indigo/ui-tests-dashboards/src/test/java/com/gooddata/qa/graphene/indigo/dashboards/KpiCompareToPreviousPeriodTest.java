package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KpiCompareToPreviousPeriodTest extends AbstractDashboardTest {

    private static final String INSIGHT_ACTIVITIES = "Insight Activities";

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsight() {
        createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES, ReportType.TABLE)
                .setMeasureBucket(singletonList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_ACTIVITIES).addDateFilter();
        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .changeDateDimension(DATE_DATASET_CREATED)
                .configTimeFilterByRangeHelper("01/01/2010", "12/31/2012")
                .changeCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD).apply();
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight();
    }

    @Test(dependsOnMethods = "prepareInsight")
    public void addInsightHasPreviousPeriodWithDateFilter() {
        initIndigoDashboardsPage()
                .addDashboard()
                .addInsight(INSIGHT_ACTIVITIES).waitForWidgetsLoading();
        indigoDashboardsPage.selectDateFilterByName("All time").waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getFirstWidget(Insight.class);
        PivotTableReport tableReport = insight.getPivotTableReport();
        assertEquals(tableReport.getBodyContent(), singletonList(asList("154,271", "154,271")));
        assertEquals(tableReport.getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES));

        indigoDashboardsPage.selectDateFilterByName("This month").waitForWidgetsLoading();
        assertTrue(insight.isEmptyValue(), "Insight should be empty");
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = "createProject")
    public void addInsightHasThisMonthPeriodWithAllTimeFilter() {
        createInsightWidget(new InsightMDConfiguration("Insight Test", ReportType.TABLE)
                .setMeasureBucket(singletonList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        AnalysisPage analysisPage = initAnalysePage().openInsight("Insight Test").addDateFilter();
        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .changeDateDimension(DATE_DATASET_CREATED)
                .changePeriod(DateRange.THIS_MONTH.toString())
                .changeCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD).apply();
        analysisPage.saveInsight();

        initIndigoDashboardsPage()
                .addDashboard()
                .addInsight("Insight Test").waitForWidgetsLoading();
        indigoDashboardsPage.selectDateFilterByName("All time").waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getFirstWidget(Insight.class);
        PivotTableReport tableReport = insight.getPivotTableReport();
        assertEquals(tableReport.getBodyContent(), singletonList(asList("154,271", "154,271")));
        assertEquals(tableReport.getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES));

        indigoDashboardsPage.selectDateFilterByName("This month").waitForWidgetsLoading();
        assertTrue(insight.isEmptyValue(), "Insight should be empty");
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = "prepareInsight")
    public void addInsightHasPreviousPeriodWithoutDateFilter() {
        initIndigoDashboardsPage()
                .addDashboard()
                .addInsight(INSIGHT_ACTIVITIES)
                .getConfigurationPanel()
                .disableDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading();

        PivotTableReport tableReport = indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport();
        assertEquals(tableReport.getBodyContent(), singletonList(asList("8,329", "145,942")));
        assertEquals(tableReport.getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - period ago", METRIC_NUMBER_OF_ACTIVITIES));
    }
}
