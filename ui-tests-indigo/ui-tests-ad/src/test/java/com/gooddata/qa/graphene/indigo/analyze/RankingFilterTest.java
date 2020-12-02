package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.sdk.model.executeafm.afm.filter.MeasureValueFilter;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.OperatorCalculated;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FilterBarPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.RankingFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
        .LogicalOperator.GREATER_THAN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
        .LogicalOperator.LESS_THAN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
        .LogicalOperator.EQUAL_TO;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
        .LogicalOperator.Range.BETWEEN;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class RankingFilterTest extends AbstractAnalyseTest {

    private final String RANKING_FILTER = "Top/bottom values";
    private final String TOP = "Top";
    private final String BOTTOM = "Bottom";
    private final String METRIC_AMOUNT_PERCENT = "metricAmountPercent";
    private final String TIME_RANGE_FROM = "01/01/2000";
    private final String TIME_RANGE_TO = "01/01/2020";
    private final String ADVANCED_INSIGHT = "Advanced Insight";
    private final String ADVANCED_INSIGHT_SAVE_AS_NEW = "Save as new Advanced Insight";
    private final String ADVANCED_INSIGHT_TWO_ATTRIBUTE = "Advanced Insight 2 attributes";
    private final String INSIGHT_TWO_ATTRIBUTE = "Insight 2 attributes";
    private final String DASHBOARD_CHECK_RANKING_FILTER = "Dashboard check";
    private final String CALCULATED_RATIO_OF = "Ratio of …";


    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private FactRestRequest factRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Updated AD filter Insight Value";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createTimelineEOPMetric();
        metrics.createCloseEOPMetric();
        metrics.createSnapshotEOPMetric();
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        factRestRequest = new FactRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_MEASURE_VALUE_FILTERS, true);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the adding ranking filter with the insight that has only one attribute")
    public void applyRakingFilterOnInsightWithOneAttribute() throws NoSuchFieldException {
        createMetric(METRIC_AMOUNT_PERCENT, format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))), DEFAULT_METRIC_FORMAT + "%");

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetricToRecommendedStepsPanelOnCanvas(METRIC_AMOUNT_PERCENT);

        analysisPage.addMetric(METRIC_AMOUNT).addMetric(FACT_AMOUNT, FieldType.FACT)
            .addDateFilter().addAttribute(ATTR_SALES_REP).waitForReportComputing();

        createDerivedMeasure(METRIC_AMOUNT);

        createCalculatedMeasure(Pair.of(METRIC_AMOUNT, 3), Pair.of(METRIC_SUM_OF_AMOUNT, 4), MetricConfiguration.OperatorCalculated.SUM);
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);

        MetricConfiguration metricConfigurationAmount = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        metricConfigurationAmount.addFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.waitForReportComputing();
        analysisPage.addMetric(ATTR_SALES_REP, FieldType.ATTRIBUTE);

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT, 3), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT + SP_YEAR_AGO, 2),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT, 4), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT + " and " + METRIC_SUM_OF_AMOUNT, 5),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT_PERCENT, 1), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked("Count of " + ATTR_SALES_REP, 6),
            "Default state should be unticked checkbox.");

        filterBarPicker.checkItem(RANKING_FILTER).apply();

        analysisPage.openFilterBarPicker().checkItem("Count of " + ATTR_SALES_REP, 6).apply();
        assertThat(analysisPage.getFilterBuckets().getFilterText("Count of " + ATTR_SALES_REP + " (M6)"), containsString("All"));

        analysisPage.openRankingFilterPanel(TOP + " 10").inputOperator("3").basedOn(METRIC_SUM_OF_AMOUNT).apply();
        assertEquals(analysisPage.openRankingFilterPanel(TOP + " 3").getPreview(), TOP + " 3 of " + METRIC_SUM_OF_AMOUNT);
        assertThat(analysisPage.getFilterBuckets().getFilterText(TOP + " 3"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompare = asList("137,688,967,018%", "164,970,450,043%", "322,495,510,189%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompare));

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD).apply();

        analysisPage.openRankingFilterPanel(TOP + " 3").selectOperator(BOTTOM).inputOperator("4").basedOn(METRIC_SUM_OF_AMOUNT).apply();
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 4"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));
        String messageBubble = analysisPage.openRankingFilterPanel(BOTTOM + " 4").getMessageNoOptionsBubble();
        assertEquals(messageBubble, "The insight is sliced only by one attribute. The filter will apply to all its values.");
        AnalysisPage.getInstance(browser).waitForReportComputing();

        analysisPage.saveInsight(ADVANCED_INSIGHT).waitForReportComputing();
        assertFalse(AnalysisPage.getInstance(browser).isSaveInsightEnabled(), "Should save insight successfully.");
    }

    @Test(dependsOnMethods = "applyRakingFilterOnInsightWithOneAttribute",
        description = "This test case covered the adding ranking filter with the insight that has 2 attributes and checking measure value filter")
    public void applyRakingFilterOnInsightWithManyAttributes() throws NoSuchFieldException {
        createMetric(METRIC_AMOUNT_PERCENT, format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))), DEFAULT_METRIC_FORMAT + "%");

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .addMetricToRecommendedStepsPanelOnCanvas(METRIC_AMOUNT_PERCENT);

        analysisPage.addMetric(METRIC_AMOUNT).addMetric(FACT_AMOUNT, FieldType.FACT)
            .addDateFilter().addAttribute(ATTR_REGION).addAttribute(ATTR_DEPARTMENT);

        createDerivedMeasure(METRIC_AMOUNT);

        createCalculatedMeasure(Pair.of(METRIC_AMOUNT, 3), Pair.of(METRIC_SUM_OF_AMOUNT, 4), MetricConfiguration.OperatorCalculated.SUM);
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);

        MetricConfiguration metricConfigurationAmount = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        metricConfigurationAmount.addFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.waitForReportComputing();
        analysisPage.addMetric(ATTR_SALES_REP, FieldType.ATTRIBUTE);

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT, 3), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT + SP_YEAR_AGO, 2),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT, 4), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT + " and " + METRIC_SUM_OF_AMOUNT, 5),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT_PERCENT, 1), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked("Count of " + ATTR_SALES_REP, 6),
            "Default state should be unticked checkbox.");

        filterBarPicker.checkItem(RANKING_FILTER).apply();

        analysisPage.openFilterBarPicker().checkItem("Count of " + ATTR_SALES_REP, 6).apply();
        assertThat(analysisPage.getFilterBuckets().getFilterText("Count of " + ATTR_SALES_REP + " (M6)"), containsString("All"));

        analysisPage.openRankingFilterPanel(TOP + " 10").inputOperator("5").apply();
        assertThat(analysisPage.getFilterBuckets().getFilterText(TOP + " 5"), containsString(METRIC_AMOUNT_PERCENT));
        List<String> resultCompare = asList("342,535,179,484%", "83,042,163,020%", "859,691,557,522%", "424,227,112,078%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompare));

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD).apply();

        analysisPage.openRankingFilterPanel(TOP + " 5").selectOperator(BOTTOM).inputOperator("3")
            .outOf(ATTR_DEPARTMENT).basedOn(METRIC_SUM_OF_AMOUNT).apply();
        assertEquals(analysisPage.openRankingFilterPanel(BOTTOM + " 3 out of " + ATTR_DEPARTMENT)
            .getPreview(), BOTTOM + " 3" + " out of " + ATTR_DEPARTMENT + " based on " + METRIC_SUM_OF_AMOUNT);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 3 out of " + ATTR_DEPARTMENT), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("340,337,309,938%", "83,042,163,020%", "853,848,794,572%", "424,227,112,078%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));

        String messageBubble = analysisPage.openMeasureFilterPanel("Count of " + ATTR_SALES_REP,6).getWarningMessage();
        assertEquals(messageBubble, "To enable, set the Top/bottom value filter to All.");

        analysisPage.saveInsight(ADVANCED_INSIGHT_TWO_ATTRIBUTE).waitForReportComputing();
        assertFalse(AnalysisPage.getInstance(browser).isSaveInsightEnabled(), "Should save insight successfully.");
    }

    @Test(dependsOnMethods = "applyRakingFilterOnInsightWithManyAttributes",
        description = "This test case covered the opening existing insight that has been applied ranking filter")
    public void openExistingInsightAppliedRakingFilter() {
        analysisPage.openInsight(ADVANCED_INSIGHT);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 4"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));
    }

    @Test(dependsOnMethods = "openExistingInsightAppliedRakingFilter",
        description = "This test case covered the saving as new an existing insight that has been applied ranking filter")
    public void saveAsNewExistingInsightAppliedRakingFilter() {
        analysisPage.openInsight(ADVANCED_INSIGHT).saveInsightAs(ADVANCED_INSIGHT_SAVE_AS_NEW);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 4"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));
    }

    @Test(dependsOnMethods = "saveAsNewExistingInsightAppliedRakingFilter",
        description = "This test case covered the editing an existing insight that has been applied ranking filter")
    public void editExistingInsightAppliedRakingFilter() {
        analysisPage.openInsight(ADVANCED_INSIGHT_SAVE_AS_NEW).changeReportType(ReportType.COMBO_CHART);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 4"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));

        analysisPage.openRankingFilterPanel(BOTTOM + " 4").inputOperator("5").apply();
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 5"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompare = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%","40,724,170,496%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompare));

        assertFalse(analysisPage.clickOptionsButton().isOpenAsReportButtonEnabled(),"Open as Report shouldn't be enabled when Ranking filter is applied.");
    }

    @Test(dependsOnMethods = "editExistingInsightAppliedRakingFilter",
        description = "This test case covered the deleting an existing insight that has been applied ranking filter")
    public void  deleteExistingInsightAppliedRakingFilter() {
        analysisPage.getPageHeader().expandInsightSelection().deleteInsight(ADVANCED_INSIGHT_SAVE_AS_NEW);
        assertFalse(analysisPage.getPageHeader().expandInsightSelection().isExist(ADVANCED_INSIGHT_SAVE_AS_NEW), "Insight should be deleted");
    }

    @Test(dependsOnMethods = "deleteExistingInsightAppliedRakingFilter")
    protected void testSomeActionOnKD() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(ADVANCED_INSIGHT).waitForWidgetsLoading()
            .addInsightNext(ADVANCED_INSIGHT_TWO_ATTRIBUTE).selectDateFilterByName("All time").waitForWidgetsLoading();

        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().searchInsight(ADVANCED_INSIGHT),
            "Insight " + ADVANCED_INSIGHT_TWO_ATTRIBUTE + " should be visible");

        ChartReport firstWidgetChartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        assertThat(firstWidgetChartReport.getTotalsStackedColumn(), equalTo(asList("28,836,698,983%", "34,924,247,200%", "36,632,404,417%", "29,873,098,693%")));

        ChartReport lastWidgetChartReport = indigoDashboardsPage.getLastWidget(Insight.class).getChartReport();
        assertThat(lastWidgetChartReport.getTotalsStackedColumn(), equalTo(asList("342,535,179,484%", "83,042,163,020%", "859,691,557,522%", "424,227,112,078%")));
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_CHECK_RANKING_FILTER).waitForWidgetsLoading().saveEditModeWithWidgets();

        assertThat(firstWidgetChartReport.getTotalsStackedColumn(), equalTo(asList("28,836,698,983%", "34,924,247,200%", "36,632,404,417%", "29,873,098,693%")));
        assertThat(lastWidgetChartReport.getTotalsStackedColumn(), equalTo(asList("342,535,179,484%", "83,042,163,020%", "859,691,557,522%", "424,227,112,078%")));
    }

    @Test(dependsOnMethods = "editExistingInsightAppliedRakingFilter",
        description = "This test case covered the removing ranking filter on existing insight")
    public void removeRankingFilterExistingInsight() {
        analysisPage.openInsight(ADVANCED_INSIGHT).waitForReportComputing();
        removeAttribute(ATTR_SALES_REP);
        analysisPage.waitForReportComputing().addAttribute(ATTR_REGION).waitForReportComputing();
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(asList("423,379,472,958%", "1,278,075,906,650%")));
        analysisPage.undo().undo();

        analysisPage.openFilterBarPicker().checkItem(ATTR_SALES_REP).apply();
        analysisPage.setFilterIsValues(ATTR_SALES_REP, "Adam Bradley", "Alejandro Vabiano").waitForReportComputing();
        assertTrue(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should be enabled");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(asList("56,474,579,377%", "28,609,946,135%")));

        analysisPage.removeMetric(METRIC_SUM_OF_AMOUNT);
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
        analysisPage.undo();

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        filterBarPicker.checkItem(RANKING_FILTER).apply();
        analysisPage.saveInsightAs("Save as after check remove Ranking Filter").waitForReportComputing();
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(asList("56,474,579,377%", "28,609,946,135%")));
    }

    @Test(dependsOnMethods = "editExistingInsightAppliedRakingFilter",
            description = "This test case covered the hiding ranking filter on existing insight")
    public void hiddenRankingFilterExistingInsight() {
        analysisPage.openInsight(ADVANCED_INSIGHT).waitForReportComputing();

        analysisPage.changeReportType(ReportType.HEAD_LINE);
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
        analysisPage.undo();

        analysisPage.changeReportType(ReportType.PIE_CHART);
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
        analysisPage.undo();

        analysisPage.changeReportType(ReportType.SCATTER_PLOT);
        analysisPage.saveInsightAs("Save as after check hidden Ranking Filter").waitForReportComputing();
        assertFalse(analysisPage.getFilterBuckets().isRankingFilterVisible(),"Ranking filter should not be enabled");
    }

    @Test(dependsOnMethods = "applyRakingFilterOnInsightWithOneAttribute",
        description = "This test case covered the ranking filter on embedded mode with the insight that has only one attribute")
    public void applyRakingFilterOnInsightEmbeddedMode() {
        initEmbeddedAnalysisPage().openInsight(ADVANCED_INSIGHT);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 4"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompareUpdated = asList("28,609,946,135%", "34,578,109,813%", "36,394,794,376%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompareUpdated));
        String messageBubble = analysisPage.openRankingFilterPanel(BOTTOM + " 4").getMessageNoOptionsBubble();
        assertEquals(messageBubble, "The insight is sliced only by one attribute. The filter will apply to all its values.");

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR).apply();

        analysisPage.openRankingFilterPanel(BOTTOM + " 4").inputOperator("3").basedOn(METRIC_SUM_OF_AMOUNT).apply();
        assertEquals(analysisPage.openRankingFilterPanel(BOTTOM + " 3").getPreview(), BOTTOM + " 3 of " + METRIC_SUM_OF_AMOUNT);
        assertThat(analysisPage.getFilterBuckets().getFilterText(BOTTOM + " 3"), containsString(METRIC_SUM_OF_AMOUNT));
        List<String> resultCompare = asList("28,836,698,983%", "34,924,247,200%", "29,873,098,693%");
        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(), equalTo(resultCompare));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the adding ranking filter with show in percent")
    public void applyRakingFilterOnInsightWithShowInPercent() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addDateFilter().addAttribute(ATTR_REGION).addAttribute(ATTR_DEPARTMENT);
        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration().showPercents();
        assertThat(analysisPage.getChartReport().getDataLabels(), equalTo(asList("18.85%", "5.18%", "50.10%", "25.88%")));

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        filterBarPicker.checkItem(RANKING_FILTER).apply();
        List<String> resultCompare = asList("$21,978,695.46", "$6,038,400.96", "$58,427,629.50", "$30,180,730.62");
        assertThat(analysisPage.getChartReport().getDataLabels(), equalTo(resultCompare));
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent shouldn't be enabled");

        assertFalse(analysisPage.clickOptionsButton().isOpenAsReportButtonEnabled(),"Open as Report shouldn't be enabled when Ranking filter is applied.");
        analysisPage.saveInsight(INSIGHT_TWO_ATTRIBUTE).waitForReportComputing();
        assertFalse(AnalysisPage.getInstance(browser).isSaveInsightEnabled(), "Should save insight successfully.");
    }

    private void createDerivedMeasure(String metric) throws NoSuchFieldException{
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper(TIME_RANGE_FROM, TIME_RANGE_TO)
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames(metric).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();
    }

    private void createCalculatedMeasure( Pair<String, Integer> metricA, Pair<String, Integer> metricB, MetricConfiguration.OperatorCalculated operatorCalculated) {
        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().createCalculatedMeasure()
            .getMetricConfiguration(CALCULATED_RATIO_OF);
        metricConfiguration.chooseArithmeticMeasureA(metricA.getLeft(), metricA.getRight());
        metricConfiguration.chooseArithmeticMeasureB(metricB.getLeft(), metricB.getRight());
        metricConfiguration.chooseOperator(operatorCalculated);
    }

    private void removeAttribute(String attribute) {
        WebElement from = analysisPage.getAttributesBucket().get(attribute).findElement(By.className("adi-bucket-item-header"));
        waitForElementVisible(from);
        Actions driverActions = new Actions(browser);
        driverActions.clickAndHold(from).moveByOffset(-10, -5).perform();
        try {
            WebElement dropZone = waitForElementVisible(By.className("s-trash"), browser);
            driverActions.moveToElement(dropZone).perform();
        } finally {
            driverActions.release().perform();
        }
    }
}
