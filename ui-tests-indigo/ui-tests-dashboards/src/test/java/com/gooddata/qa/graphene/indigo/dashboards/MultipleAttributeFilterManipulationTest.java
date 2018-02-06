package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_TASK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_HISTORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class MultipleAttributeFilterManipulationTest extends AbstractDashboardTest {

    private static final String METRIC_LONG_NAME = "Metric has long name which is used to test shorten be applied Metric has " +
            "long name which is used to test shorten be applied Metric has long name which is used to test shorten be applied";
    private static final String HEADER_NAME = format("%s (%s: 1st Choice Staffing & Consulting)", METRIC_LONG_NAME, ATTR_ACCOUNT);
    private final String UNTITLED_REPORT = "Untitled report";
    private AnalysisPage analysisPage;
    private MetricConfiguration metricConfiguration;
    private String currentWindowHandle;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Multiple-Attribute-Filter-Manipulation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createPercentOfPipelineMetric(METRIC_LONG_NAME, getMetricCreator().createAmountMetric());
        metricCreator.createTimelineBOPMetric();
    }

    @DataProvider(name = "chartType")
    public Object[][] getChartType() {
        return new Object[][] {
                {ReportType.COLUMN_CHART},
                {ReportType.BAR_CHART},
                {ReportType.LINE_CHART},
                {ReportType.PIE_CHART}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartType")
    public void openAsReportWithAttributeFiltersToMetric(ReportType type) {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_AMOUNT,
            asList(Pair.of(ATTR_ACCOUNT, "123 Exteriors"), Pair.of(ATTR_DEPARTMENT, "Direct Sales")));
        analysisPage
            .changeReportType(type)
            .waitForReportComputing()
            .exportReport();

        waitForSwitchWindow();
        try {
            reportPage.waitForReportExecutionProgress();
            takeScreenshot(browser, "open-as-" + type + "report-with-attribute-filter-to-metric", getClass());
            assertEquals(reportPage.getSelectedChartType(),
                    type == ReportType.COLUMN_CHART ? ReportType.BAR_CHART.toString() : type.toString());
            assertEquals(reportPage.getReportName(), UNTITLED_REPORT);
            assertEquals(reportPage.openWhatPanel().getSelectedMetrics(),
                    asList(format("%s (%s: 123 Exteriors; %s: Direct Sales)", METRIC_AMOUNT, ATTR_ACCOUNT, ATTR_DEPARTMENT)));
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartType")
    public void openAsReportWithLimitCountOfAttributeFiltersToMetric(ReportType type) {
        final List<String> attributeFilterList = asList(ATTR_ACCOUNT, ATTR_ACTIVITY_TYPE, ATTR_ACTIVITY, ATTR_DEPARTMENT,
                ATTR_FORECAST_CATEGORY, ATTR_IS_ACTIVE, ATTR_IS_CLOSED, ATTR_IS_CLOSED, ATTR_IS_TASK, ATTR_IS_WON, ATTR_OPP_SNAPSHOT,
                ATTR_OPPORTUNITY, ATTR_PRIORITY, ATTR_PRODUCT, ATTR_REGION, ATTR_SALES_REP, ATTR_STAGE_HISTORY, ATTR_STAGE_NAME,
                ATTR_STATUS, ATTR_STATUS);

        analysisPage = initAnalysePage();
        addMetricWithAllValueAttributeFilters(METRIC_TIMELINE_BOP, attributeFilterList);
        takeScreenshot(browser, "test", getClass());
        analysisPage
                .changeReportType(type)
                .waitForReportComputing()
                .exportReport();

        waitForSwitchWindow();
        try {
            reportPage.waitForReportExecutionProgress();
            takeScreenshot(browser, "open-as-" + type + "-report-with-limit-count-of-attribute-filters-to-metric-by", getClass());
            assertEquals(reportPage.getSelectedChartType(),
                    type == ReportType.COLUMN_CHART ? ReportType.BAR_CHART.toString() : type.toString());
            assertEquals(reportPage.getReportName(), UNTITLED_REPORT);
            assertEquals(reportPage.openWhatPanel().getSelectedMetrics(), asList(METRIC_TIMELINE_BOP, METRIC_TIMELINE_BOP));
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartType")
    public void openAsReportWithAttributeFiltersToMetrics(ReportType type) {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_AMOUNT,
                asList(Pair.of(ATTR_ACCOUNT, "14 West"), Pair.of(ATTR_DEPARTMENT, "Direct Sales")));
        addMetricWithAttributeFilters(METRIC_NUMBER_OF_ACTIVITIES,
                asList(Pair.of(ATTR_ACCOUNT, "14 West"), Pair.of(ATTR_DEPARTMENT, "Direct Sales")));
        analysisPage
            .changeReportType(type)
            .waitForReportComputing()
            .exportReport();

        waitForSwitchWindow();
        try {
            reportPage.waitForReportExecutionProgress();
            takeScreenshot(browser, "open-as" + type + "report-with-attribute-filter-to-metrics", getClass());
            assertEquals(reportPage.getSelectedChartType(),
                    type == ReportType.COLUMN_CHART ? ReportType.BAR_CHART.toString() : type.toString());
            assertEquals(reportPage.getReportName(), UNTITLED_REPORT);
            assertEquals(reportPage.openWhatPanel().getSelectedMetrics(),
                    asList(format("%s (%s: 14 West; %s: Direct Sales)", METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACCOUNT, ATTR_DEPARTMENT),
                            format("%s (%s: 14 West; %s: Direct Sales)", METRIC_AMOUNT, ATTR_ACCOUNT, ATTR_DEPARTMENT)));
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openAsReportWithAttributeFiltersToSameMetrics() {
        analysisPage = initAnalysePage();
        addMetricWithAllValueAttributeFilters(METRIC_AMOUNT, asList(ATTR_ACCOUNT));
        addMetricWithAllValueAttributeFilters(METRIC_AMOUNT, asList(ATTR_DEPARTMENT));
        assertFalse(analysisPage.waitForReportComputing().getPageHeader().isExportButtonEnabled(),
                "Should be disable Open as report button");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartType")
    public void checkShortenMeasureNameInLegend(ReportType type) {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_LONG_NAME, asList(Pair.of(ATTR_ACCOUNT, "1st Choice Staffing & Consulting")));
        addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        metricConfiguration
            .addFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call", "Web Meeting")
            .addFilter(ATTR_SALES_REP, "Adam Bradley", "Alejandro Vabiano", "Alexsandr Fyodr", "Cory Owens");

        analysisPage
            .changeReportType(type)
            .waitForReportComputing();
        takeScreenshot(browser, "check-shorten-measure-name-in-legend-with-" + type, getClass());
        assertThat(analysisPage.getChartReport().getLegends(), containsInAnyOrder(
                format( "%s: \"%s: Email, Phone Call, Web Meeting; %s (4)\"",
                        METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_SALES_REP), HEADER_NAME));
        assertTrue(analysisPage.getChartReport().isShortenNameInLegend(HEADER_NAME, 1160),
                "measure name should be shorten by too long");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartType")
    public void checkShortenMeasureNameInTooltip(ReportType type) {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_AMOUNT,
                asList(Pair.of(ATTR_ACCOUNT, "14 West")));
        addMetricWithAttributeFilters(METRIC_NUMBER_OF_ACTIVITIES,
                asList(Pair.of(ATTR_DEPARTMENT, "Direct Sales")));

        analysisPage
            .changeReportType(type)
            .waitForReportComputing();
        takeScreenshot(browser, "check-shorten-measure-name-in-tooltip-with-" + type, getClass());

        assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0),
                asList(asList(format("%s (%s: 14 West)", METRIC_AMOUNT, ATTR_ACCOUNT), "$109,941.82")));
        assertTrue(analysisPage.getChartReport().isShortenTooltipTextOnTrackerByIndex(0, 140),
                "Tooltip should be shorten by too long");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkShortenMeasureNameInTableReport() {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_LONG_NAME, asList(Pair.of(ATTR_ACCOUNT, "1st Choice Staffing & Consulting")));
        addMetricWithAttributeFilters(METRIC_NUMBER_OF_ACTIVITIES, asList(Pair.of(ATTR_DEPARTMENT, "Direct Sales")));

        analysisPage
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        takeScreenshot(browser, "check-shorten-measure-name-in-table-column-header", getClass());
        assertTrue(analysisPage.getTableReport().isShortenHeader(HEADER_NAME, 579),
                "Measure Name should be shorten by too long");
        assertEquals(analysisPage.getTableReport().getTooltipText(HEADER_NAME), HEADER_NAME);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkShortenMeasureNameOnKpiDashboard() {
        String insightName = "Insight" + generateHashString();
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_LONG_NAME, asList(Pair.of(ATTR_ACCOUNT, "1st Choice Staffing & Consulting")));
        addMetricWithAttributeFilters(METRIC_NUMBER_OF_ACTIVITIES, asList(Pair.of(ATTR_DEPARTMENT, "Direct Sales")));

        analysisPage
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .saveInsight(insightName);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage()
                .addDashboard().addInsight(insightName).waitForWidgetsLoading();
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "check-shorten-measure-name-on-KPI-dashboard", getClass());
        assertTrue(indigoDashboardsPage.getFirstWidget(Insight.class).getTableReport().isShortenHeader(HEADER_NAME, 573),
                "Measure Name should be shorten by too long");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkShortenAttributeFilterAndTooltip() {
        analysisPage = initAnalysePage();
        addMetricWithAttributeFilters(METRIC_NUMBER_OF_ACTIVITIES, asList(Pair.of(ATTR_ACCOUNT, "1st Choice Staffing & Consulting")));
        assertEquals(metricConfiguration.getSubHeader(), format("%s: 1st Ch…ng & Consulting", ATTR_ACCOUNT));
        assertEquals(metricConfiguration.getToolTipSubHeader(), format("%s: 1st Choice Staffing & Consulting", ATTR_ACCOUNT));
    }

    private void addMetricWithAttributeFilters(String metric, List<Pair<String, String>> attributes) {
        addMetric(metric);
        attributes.stream()
                .forEach(attribute -> metricConfiguration.addFilterBySelectOnly(attribute.getLeft(), attribute.getRight()));
    }

    private void addMetricWithAllValueAttributeFilters(String metric, List<String> attributes) {
        addMetric(metric);
        attributes.stream().forEach(attribute -> metricConfiguration.addFilterWithAllValue(attribute));
    }

    private void addMetric(String metric) {
        metricConfiguration = analysisPage
            .addMetric(metric)
            .getMetricsBucket()
            .getLastMetricConfiguration()
            .expandConfiguration();
    }

    private void waitForSwitchWindow() {
        currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
    }
}
