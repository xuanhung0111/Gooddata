package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown.CompareType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ExtendedDateFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CLOSE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_WARNING_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class KPIDashboardsDrillToDashboardTest extends AbstractDashboardTest {

        private final String COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY = "Column chart has two measures and viewBy";
        private final String INSIGHT_HAS_TWO_MEASURES = "Pivot Table has two measures";
        private final String INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY = "Insight has one measures and viewBy";
        private final String INSIGHT_HAS_LOCAL_DATE_FILTER = "Insight has local date filter";
        private final String INSIGHT_HAS_POP_MEASURE = "Table has PoP";

        private final String SOURCE_DASHBOARD = "Source Dashboard";
        private final String SOURCE_DASHBOARD_HAS_COLUMN = "Source DB has Column";
        private final String SOURCE_DASHBOARD_HAS_NO_CONNECTED_DATE = "DB has no connected date";
        private final String SOURCE_DASHBOARD_CONFIG_LEVEL = "Dashboard config";
        private final String SOURCE_DASHBOARD_HAS_POP = "Dashboard has PoP";
        private final String TARGET_DASHBOARD = "Target Dashboard";
        private final String TARGER_DASHBOARD_CONFIG_LEVEL = "Target Dashboard config";  
        private final String EMPTY_DASHBOARD = "Empty Dashboard";
        private final String SOURCE_DASHBOARD_DRILL_TO_EMPTY = "Drill To Empty";

        private IndigoRestRequest indigoRestRequest;
        private ProjectRestRequest projectRestRequest;
        private PivotTableReport pivotTable;
        private ChartReport chartReport;
        private ConfigurationPanel configurationPanel;
        private AnalysisPage analysisPage;

        @Override
        protected void customizeProject() throws JSONException, IOException {
                Metrics metrics = getMetricCreator();
                metrics.createAmountMetric();
                metrics.createAvgAmountMetric();
                metrics.createBestCaseMetric();
                metrics.createAmountBOPMetric();
                indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
                projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
                projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
                projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
                
                createInsight(COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY, ReportType.COLUMN_CHART, 
                        asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE),
                        asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW),
                                Pair.of(ATTR_PRODUCT, CategoryBucket.Type.VIEW)));

                createInsight(INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY, ReportType.COLUMN_CHART, 
                        singletonList(METRIC_AMOUNT_BOP),
                        singletonList(Pair.of(ATTR_PRODUCT, CategoryBucket.Type.VIEW)));
                
                createInsight(INSIGHT_HAS_TWO_MEASURES, ReportType.TABLE, 
                        asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE),
                        asList(Pair.of(ATTR_YEAR_CLOSE, CategoryBucket.Type.ATTRIBUTE),
                                Pair.of(ATTR_PRODUCT, CategoryBucket.Type.ATTRIBUTE),
                                Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.COLUMNS)));
        }
    
        @Test(dependsOnGroups = "createProject")
        public void drillToEmptyDashboard() {
                indigoRestRequest.createEmptyAnalyticalDashboard(EMPTY_DASHBOARD, 12);
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(SOURCE_DASHBOARD_DRILL_TO_EMPTY)
                        .addInsight(COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY);
                configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, EMPTY_DASHBOARD);
                indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2012", "12/31/2012").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .getChartReport();
                chartReport.clickOnElement(Pair.of(0, 0));
                indigoDashboardsPage.waitForDashboardLoad();
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/2012–12/31/2012");
                assertTrue(indigoDashboardsPage.getDashboardBodyText().isEmpty(), "Target dashboard should be empty!");
        }

        @Test(dependsOnGroups = "createProject")
        public void drillToTargetDashboardHasSameDateFilter() {
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(TARGET_DASHBOARD)
                        .addInsight(INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY);
                indigoDashboardsPage.openExtendedDateFilterPanel()
                        .selectFloatingRange(DateGranularity.YEARS, "9 years ago", "this year").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();

                indigoDashboardsPage.addDashboard().changeDashboardTitle(SOURCE_DASHBOARD_HAS_COLUMN)
                        .addInsight(COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY);
                configurationPanel = indigoDashboardsPage.getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, TARGET_DASHBOARD);

                indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "01/01/2012").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .getChartReport();
                chartReport.clickOnElement(Pair.of(0, 0));

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();

                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$673,662.80")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/2011–01/01/2012");

                indigoDashboardsPage.addDashboard().changeDashboardTitle(SOURCE_DASHBOARD)
                        .addInsight(INSIGHT_HAS_TWO_MEASURES)
                        .selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, TARGET_DASHBOARD);
                indigoDashboardsPage.openExtendedDateFilterPanel()
                        .selectFloatingRange(DateGranularity.YEARS, "10 years ago", "this year").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();

                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();

                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$1,526,088.14")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "Last 11 years");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasSameDateFilter")
        public void drillToDashboardHasComparisonMeasure() throws NoSuchFieldException {
                analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT).addAttribute(ATTR_PRODUCT)
                        .addColumnsAttribute(ATTR_DEPARTMENT).addDateFilter().waitForReportComputing();
                FiltersBucket filterBucket = analysisPage.getFilterBuckets();
                DateFilterPickerPanel dateFilterPickerPanel = filterBucket
                        .openDatePanelOfFilter(filterBucket.getDateFilter());
                dateFilterPickerPanel.configTimeFilterByRangeHelper("01/01/2011", "12/31/2011")
                        .changeCompareType(CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
                        .selectAllValues().apply();
                dateFilterPickerPanel.apply();
                analysisPage.saveInsight(INSIGHT_HAS_POP_MEASURE).waitForReportComputing();

                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(SOURCE_DASHBOARD_HAS_POP)
                        .addInsight(INSIGHT_HAS_POP_MEASURE).waitForWidgetsLoading()
                        .selectWidgetByHeadline(Insight.class, INSIGHT_HAS_POP_MEASURE);
                configurationPanel.drillIntoDashboard("Amount - SP year ago", TARGET_DASHBOARD);
                indigoDashboardsPage.openExtendedDateFilterPanel()
                        .selectFloatingRange(DateGranularity.YEARS, "10 years ago", "10 years ago").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();

                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_POP_MEASURE)
                        .getPivotTableReport();
                pivotTable.clickOnElement("Amount - SP year ago", 0, 0);
                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$852,425.34")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "From 10 to 10 years ago");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasSameDateFilter")
        public void drillToDashboardHasNoAttributeFilter() {
                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD).waitForWidgetsLoading();
                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$1,526,088.14")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "Last 11 years");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasSameDateFilter")
        public void drillToDashboardHasDiffferentDateDimension() {
                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                indigoDashboardsPage.getConfigurationPanel().selectDateDataSetByName(DATE_DATASET_CREATED);
                indigoDashboardsPage.addAttributeFilter(ATTR_PRODUCT, "Educationly")
                        .addAttributeFilter(ATTR_DEPARTMENT);
                indigoDashboardsPage.waitForWidgetsLoading().saveEditModeWithWidgets();

                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$1,526,088.14")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "Last 11 years");
        }

        @Test(dependsOnMethods = "drillToDashboardHasNoAttributeFilter")
        public void drillToDashboardHasAttributeAndDateFilter() {
                initIndigoDashboardsPage().selectKpiDashboard(TARGET_DASHBOARD).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().addAttributeFilter(ATTR_PRODUCT)
                        .addAttributeFilter(ATTR_DEPARTMENT).saveEditModeWithWidgets();
                indigoDashboardsPage.selectKpiDashboard(SOURCE_DASHBOARD).waitForWidgetsLoading()
                        .openExtendedDateFilterPanel()
                        .selectFloatingRange(DateGranularity.YEARS, "10 years ago", "10 years ago").apply();
                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();
                assertThat(chartReport.getXaxisLabels(), equalTo(singletonList("Educationly")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "Educationly"), asList(METRIC_AMOUNT_BOP, "$807,371.56")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "From 10 to 10 years ago");
                assertEquals(panel.getAttributeFilter(ATTR_PRODUCT).getSelectedItems(), "Educationly");
                assertEquals(panel.getAttributeFilter(ATTR_DEPARTMENT).getSelectedItems(), "Direct Sales");

                // Switch to other Dashboard and check filters do not affect
                indigoDashboardsPage.selectKpiDashboard(SOURCE_DASHBOARD_HAS_COLUMN).waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .getChartReport();
                List<String> expectedXAxisLabels = asList("CompuSci", "Direct Sales", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid", "CompuSci", "Inside Sales", "Educationly",
                        "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");

                assertThat(chartReport.getXaxisLabels(), equalTo(expectedXAxisLabels));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_DEPARTMENT, "Direct Sales"), 
                                asList(ATTR_PRODUCT, "CompuSci"),
                                asList(METRIC_AMOUNT_BOP, "$673,662.80")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/2011–01/01/2012");
        }

        @Test(dependsOnMethods = "drillToDashboardHasAttributeAndDateFilter")
        public void drillToCurrentDashboard() {
                indigoDashboardsPage.selectKpiDashboard(SOURCE_DASHBOARD).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel = indigoDashboardsPage.getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_BEST_CASE, SOURCE_DASHBOARD);

                indigoDashboardsPage.saveEditModeWithWidgets();

                pivotTable = indigoDashboardsPage.waitForWidgetsLoading()
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES).getPivotTableReport();
                pivotTable.clickOnElement(METRIC_BEST_CASE, 0, 0);
                indigoDashboardsPage.waitForWidgetsLoading();

                assertThat(pivotTable.getBodyContent(),
                        hasItems(asList("2010", "Educationly", "$115,873.56", "16,876")));
                AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "Last 11 years");
                assertEquals(panel.getAttributeFilter(ATTR_PRODUCT).getSelectedItems(), "Educationly");
                assertEquals(panel.getAttributeFilter(ATTR_DEPARTMENT).getSelectedItems(), "Direct Sales");

                // Switch to other Dashboard and check filters do not affect
                indigoDashboardsPage.selectKpiDashboard(SOURCE_DASHBOARD_HAS_COLUMN).waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .getChartReport();
                List<String> expectedXAxisLabels = asList("CompuSci", "Direct Sales", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid", "CompuSci", "Inside Sales", "Educationly",
                        "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");

                assertThat(chartReport.getXaxisLabels(), equalTo(expectedXAxisLabels));

                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_PRODUCT, "CompuSci"),
                                asList(METRIC_AMOUNT_BOP, "$673,662.80")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/2011–01/01/2012");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasSameDateFilter")
        public void sourceDashboardHasNoConnectedDateFilter() {
                AnalysisPage analysisPage = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
                        .addMetric(METRIC_AMOUNT_BOP).addDate();
                analysisPage.saveInsight(INSIGHT_HAS_LOCAL_DATE_FILTER).waitForReportComputing();

                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(SOURCE_DASHBOARD_HAS_NO_CONNECTED_DATE)
                        .addInsight(INSIGHT_HAS_LOCAL_DATE_FILTER)
                        .selectWidgetByHeadline(Insight.class, INSIGHT_HAS_LOCAL_DATE_FILTER);
                configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, TARGET_DASHBOARD);
                indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "01/01/2012").apply();
                indigoDashboardsPage.saveEditModeWithWidgets();

                initAnalysePage().openInsight(INSIGHT_HAS_LOCAL_DATE_FILTER).waitForReportComputing();
                analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT_BOP).expandConfiguration()
                        .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2010", "12/31/2010");
                analysisPage.saveInsight().waitForReportComputing();

                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_HAS_NO_CONNECTED_DATE)
                        .waitForWidgetsLoading();
                chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_LOCAL_DATE_FILTER)
                        .getChartReport();
                chartReport.openDrillingPicker(Pair.of(0, 0)).drillToDashboard();

                chartReport = indigoDashboardsPage.waitForWidgetsLoading()
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();

                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$673,662.80")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "Last 10 years");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasSameDateFilter")
        public void sourceDashboardHasDateConfiguration() throws IOException {
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(SOURCE_DASHBOARD_CONFIG_LEVEL)
                        .addInsight(INSIGHT_HAS_TWO_MEASURES).waitForWidgetsLoading();
                indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
                indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, TARGET_DASHBOARD);
                indigoDashboardsPage.saveEditModeWithWidgets();

                indigoRestRequest.addRelativePresetsDashboardLevel(SOURCE_DASHBOARD_CONFIG_LEVEL, "New Date Range",
                        "active", "last_12_years", "Last 12 years", "GDC.time.year", true, -11, 0);

                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().openExtendedDateFilterPanel().selectPeriod("Last 12 years")
                        .checkExcludeCurrent().apply();
                indigoDashboardsPage.saveEditModeWithWidgets();
                indigoRestRequest.hideFilterOptionsOnDashboardLevel(SOURCE_DASHBOARD_CONFIG_LEVEL, "New Date Range",
                        "hidden", "last_12_years");
                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading();

                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);
                indigoDashboardsPage.waitForWidgetsLoading();

                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                assertThat(chartReport.getXaxisLabels(), equalTo(asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$1,526,088.14")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "From 12 to 1 year ago");
        }

        @Test(dependsOnMethods = "sourceDashboardHasDateConfiguration")
        public void drillToTargetDashboardHasDateConfiguration() throws IOException {
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(TARGER_DASHBOARD_CONFIG_LEVEL)
                        .addInsight(COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY).waitForWidgetsLoading();
                indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
                indigoDashboardsPage.saveEditModeWithWidgets();
                indigoRestRequest.hideFilterOptionsOnDashboardLevel(TARGER_DASHBOARD_CONFIG_LEVEL, "New Date Range",
                        "readonly", "absoluteForm");

                indigoRestRequest.addAbsolutePresetsDashboardLevel(SOURCE_DASHBOARD_CONFIG_LEVEL, "New Date Range",
                        "active", "absolute_2010_2013", "[2010-2013]", true, "2010-01-01", "2013-12-31");

                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel = indigoDashboardsPage.getConfigurationPanel();
                configurationPanel.drillIntoDashboard(METRIC_BEST_CASE, TARGER_DASHBOARD_CONFIG_LEVEL);
                indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod("[2010-2013]").apply();

                indigoDashboardsPage.saveEditModeWithWidgets();
                indigoDashboardsPage.waitForWidgetsLoading();
                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_BEST_CASE, 0, 0);
                indigoDashboardsPage.waitForWidgetsLoading();

                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_TWO_MEASURES_AND_VIEWBY)
                        .getChartReport();

                List<String> expectedXAxisLabels = asList("CompuSci", "Direct Sales", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid", "CompuSci", "Inside Sales", "Educationly",
                        "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");
                assertThat(chartReport.getXaxisLabels(), equalTo(expectedXAxisLabels));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_DEPARTMENT, "Direct Sales"), 
                                asList(ATTR_PRODUCT, "CompuSci"),
                                asList(METRIC_AMOUNT_BOP, "$1,255,922.48")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time");
        }

        @Test(dependsOnMethods = "drillToDashboardHasAttributeAndDateFilter")
        public void drillToDashboardOnEmbeddedDashboard() {
                initIndigoDashboardsPage().selectKpiDashboard(TARGET_DASHBOARD).waitForWidgetsLoading();
                initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL).waitForWidgetsLoading();
                pivotTable = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES)
                        .getPivotTableReport();
                pivotTable.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);

                indigoDashboardsPage.waitForWidgetsLoading();
                chartReport = indigoDashboardsPage
                        .getWidgetByHeadline(Insight.class, INSIGHT_HAS_ONE_MEASURE_AND_VIEWBY)
                        .getChartReport();
                AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();
                assertThat(chartReport.getXaxisLabels(), equalTo(singletonList("CompuSci")));
                assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                        asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT_BOP, "$1,255,922.48")));
                assertEquals(indigoDashboardsPage.getDateFilterSelection(), "01/01/2010–12/31/2013");
                assertEquals(panel.getAttributeFilter(ATTR_PRODUCT).getSelectedItems(), "CompuSci");
                assertEquals(panel.getAttributeFilter(ATTR_DEPARTMENT).getSelectedItems(), "Direct Sales");
        }

        @Test(dependsOnMethods = "drillToTargetDashboardHasDateConfiguration")
        public void changeTargetDashboard() {
                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel = indigoDashboardsPage.getConfigurationPanel();
                configurationPanel.changeTargetDashboard(METRIC_AMOUNT_BOP, TARGET_DASHBOARD, SOURCE_DASHBOARD_HAS_COLUMN);
                indigoDashboardsPage.saveEditModeWithWidgets();

                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading()
                        .switchToEditMode().selectWidgetByHeadline(Insight.class, INSIGHT_HAS_TWO_MEASURES);
                configurationPanel = indigoDashboardsPage.getConfigurationPanel();
                assertThat(configurationPanel.getTargetDashboard(), hasItems(SOURCE_DASHBOARD_HAS_COLUMN));
        }

        @Test(dependsOnMethods = "changeTargetDashboard")
        public void deleteTargetDashboard() {
                initIndigoDashboardsPage().selectKpiDashboard(SOURCE_DASHBOARD_HAS_COLUMN).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().deleteDashboardOnMenuItem(true);

                indigoDashboardsPage.selectKpiDashboard(SOURCE_DASHBOARD_CONFIG_LEVEL).waitForWidgetsLoading();
                indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading();
                assertEquals(waitForElementVisible(BY_WARNING_MESSAGE_BAR, browser).getText(),
                                "Some interactions were removedShow more");
        }
        
        private String createInsight(String insightTitle, ReportType reportType, List<String> metricsTitle,
                        List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
                return indigoRestRequest.createInsight(
                        new InsightMDConfiguration(insightTitle, reportType)
                                .setMeasureBucket(metricsTitle.stream()
                                        .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                                        .collect(toList()))
                                .setCategoryBucket(attributeConfigurations.stream()
                                        .map(attribute -> CategoryBucket.createCategoryBucket(
                                                getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                                        .collect(toList())));
        }
}
