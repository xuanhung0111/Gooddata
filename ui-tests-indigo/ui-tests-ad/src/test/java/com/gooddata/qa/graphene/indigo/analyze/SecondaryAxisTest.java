package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.sdk.model.md.Restriction.title;
import static org.hamcrest.Matchers.hasItems;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.CANVAS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.COLORS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.LEGEND;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.X_AXIS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;

public class SecondaryAxisTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TEST_DASHBOARD = "Dashboard";
    private static final String INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE = "Two measure and attribute";
    private static final String INSIGHT_HAS_A_METRIC = "A metric";
    private static final String INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES = "A metric and attributes";
    private IndigoRestRequest indigoRestRequest;
    private final List<String> defaultItemsConfigurationPanel =
            asList(COLORS.toString(), X_AXIS.toString(), Y_AXIS.toString(), LEGEND.toString(), CANVAS.toString());
    private static final String INSIGHT_TEST_CONFIGURATION_HAS_TWO_MEASURE_AND_ATTRIBUTE =
            "Configuration two measure and attribute";
    private final List<String> itemsConfigurationPanelColumnChart =
            asList(COLORS.toString(), X_AXIS.toString(), Y_AXIS.toString() + " (Left)", Y_AXIS.toString() + " (Right)",
                    LEGEND.toString(), CANVAS.toString());
    private final List<String> itemsConfigurationPanelBarChart =
            asList(COLORS.toString(), X_AXIS.toString() + " (Top)", X_AXIS.toString() + " (Bottom)",
                    Y_AXIS.toString(), LEGEND.toString(), CANVAS.toString());
    private String sourceProjectId;
    private String targetProjectId;
    private String insight;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Allow user to set a secondary axis for insights with multiple measures";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createProjectUsingFixture(
                "Copy of " + projectTitle, ResourceManagement.ResourceTemplate.GOODSALES);
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createCloseEOPMetric();
        metrics.createNumberOfActivitiesMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enabledDualAxes_ForTheDualAxesCheckboxIsShowed() {
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT, ReportType.COLUMN_CHART);
        MetricConfiguration metricConfiguration = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC)
                .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        assertEquals(metricConfiguration.getShowOnSecondaryAxis(), "show on right axis");

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        metricConfiguration = analysisPage.addMetric(METRIC_AMOUNT, FieldType.FACT).waitForReportComputing()
                .getMetricsBucket().getMetricConfiguration("Sum of " + METRIC_AMOUNT).expandConfiguration();
        assertEquals(metricConfiguration.getShowOnSecondaryAxis(), "show on right axis");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        metricConfiguration = analysisPage.addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE).waitForReportComputing()
                .getMetricsBucket().getMetricConfiguration("Count of " + ATTR_DEPARTMENT).expandConfiguration();
        assertEquals(metricConfiguration.getShowOnSecondaryAxis(), "show on top axis");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showSecondaryAxisOnConfigurationPanelWithAMetric() {
        createInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES, METRIC_AMOUNT, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Bar chart should render well with X-axis on top");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showSecondaryAxisOnConfigurationPanelWithSomeMetric() {
        createInsightHasTwoMeasureAndAttribute(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ATTR_DEPARTMENT,
                ReportType.COLUMN_CHART, METRIC_AMOUNT, METRIC_BEST_CASE);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Bar chart should render well with X-axis on top");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideSecondaryAxisOnConfigurationPanelWithAMetric() {
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().uncheckShowOnSecondaryAxis();
        analysisPage.waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");

        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideSecondaryAxisOnConfigurationPanelWithSomeMetric() {
        createInsightHasTwoMeasureAndAttribute(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ATTR_DEPARTMENT,
                ReportType.COLUMN_CHART, METRIC_AMOUNT, METRIC_BEST_CASE);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();
        analysisPage.saveInsight().waitForReportComputing().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE);

        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().uncheckShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().uncheckShowOnSecondaryAxis();
        analysisPage.waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");

        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void placeSecondaryAxisOnKPIDashboard() {
        createInsight(INSIGHT_TEST_DASHBOARD, METRIC_AMOUNT, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_TEST_DASHBOARD)
                .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().checkShowOnSecondaryAxis();
        analysisPage.waitForReportComputing().saveInsight();

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST_DASHBOARD).selectDateFilterByName("All time");
        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();

        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isPrimaryYaxisVisible(),
                "Primary Y-axis on left of chart should be hidden");
        assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLegendOnInsight() {
        List<String> expectedLegend = asList(METRIC_BEST_CASE, METRIC_AMOUNT);
        createInsightHasTwoMeasureAndAttribute(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ATTR_DEPARTMENT,
                ReportType.COLUMN_CHART, METRIC_AMOUNT, METRIC_BEST_CASE);
        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertFalse(chartReport.isLegendIndicatorPresent(), "Shouldn't display legend separately left or right");

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().uncheckShowOnSecondaryAxis();

        assertEquals(chartReport.getLegendIndicators(), asList("Left:", "Right:"));
        assertEquals(chartReport.getLegends(), expectedLegend);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkTooltipOnChart() {
        createInsightHasTwoMeasure(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ReportType.COLUMN_CHART,
                METRIC_AMOUNT, METRIC_BEST_CASE);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        assertEquals(chartReport.checkColorColumn(0, 0), "rgb(20,178,226)");
        assertEquals(chartReport.checkColorColumn(1, 0), "rgb(0,193,141)");
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkConfigurationBucketOfSecondaryAxis() {
        insight = createInsightHasTwoMeasureAndAttribute(INSIGHT_TEST_CONFIGURATION_HAS_TWO_MEASURE_AND_ATTRIBUTE,
                ATTR_DEPARTMENT, ReportType.COLUMN_CHART, METRIC_AMOUNT, METRIC_BEST_CASE);
        MetricsBucket metricsBucket = initAnalysePage()
                .openInsight(INSIGHT_TEST_CONFIGURATION_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().uncheckShowOnSecondaryAxis();
        analysisPage.saveInsight().waitForReportComputing();

        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), itemsConfigurationPanelColumnChart);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(configurationPanelBucket.getItemNames(), itemsConfigurationPanelBarChart);

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        configurationPanelBucket.getItemConfiguration(Y_AXIS.toString() + " (Left)").switchOff();

        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isPrimaryYaxisVisible(), "Rerender insight shouldn't have primary axis");

        configurationPanelBucket.getItemConfiguration(Y_AXIS.toString() + " (Right)").switchOff();
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getSecondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkAlignZeroForMeasureHasNegativeValue() {
        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        createInsightHasTwoMeasureAndAttribute(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ATTR_DEPARTMENT,
                ReportType.COLUMN_CHART, METRIC_AMOUNT, metricNegativeValue);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        takeScreenshot(browser, "GoodSales-dashboard-with-report", getClass());
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getPrimaryYaxisTitle(), metricNegativeValue);
        assertThat(chartReport.getValuePrimaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItem("0"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePropertiesForSpecialInsight() {
        MetricsBucket metricsBucket = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
                .addMetric(FACT_AMOUNT, FieldType.FACT).addAttribute(ATTR_ACCOUNT).waitForReportComputing()
                .getMetricsBucket();

        MetricConfiguration metricConfiguration = metricsBucket.getMetricConfiguration("Sum of " + METRIC_AMOUNT)
                .expandConfiguration();
        metricConfiguration.checkShowOnSecondaryAxis();
        assertTrue(metricConfiguration.isShowOnSecondaryAxisChecked(),
                "“Show dual axes” checkbox should enabled, can check or uncheck on it");
        metricConfiguration.uncheckShowOnSecondaryAxis();
        assertFalse(metricConfiguration.isShowOnSecondaryAxisChecked(),
                "“Show dual axes” checkbox shouldn't enabled, can check or uncheck on it");

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertFalse(metricsBucket.getMetricConfiguration("Sum of " + METRIC_AMOUNT).expandConfiguration()
                .isShowOnSecondaryAxisPresent(), "“Show dual axes” checkbox should be hidden");
        ConfigurationPanelBucket configurationPanelBucket = analysisPage.openConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getPropertiesUnsupported(),
                 "This visualization doesn’t support configuration");
        ChartReport chartReport = analysisPage.getChartReport();
        assertFalse(chartReport.areLegendsHorizontal(), "Legend should be hidden");
        assertFalse(chartReport.isPrimaryYaxisVisible(), "Rerender insight shouldn't have primary axis");
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
    }

    @Test(dependsOnMethods = "checkConfigurationBucketOfSecondaryAxis")
    public void testPartialExportAndImportReport() throws Throwable {
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initAnalysePage().openInsight(INSIGHT_TEST_CONFIGURATION_HAS_TWO_MEASURE_AND_ATTRIBUTE)
                    .waitForReportComputing();
            assertEquals(analysisPage.openConfigurationPanelBucket().getItemNames(), itemsConfigurationPanelColumnChart);

            analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
            ChartReport chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_BEST_CASE);
            assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
            assertEquals(chartReport.getSecondaryYaxisTitle(), METRIC_AMOUNT);
            assertTrue(chartReport.isSecondaryYaxisVisible(), "Rerender insight should have secondary axis");
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void recalculateMinMaxValueByFormulaOnChart() {
        String rightMin = "-10";
        String rightMax = "90";
        String leftMin = "-10";
        String leftMax = "30";
        createInsightHasTwoMeasure(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE, ReportType.COLUMN_CHART,
                METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT);

        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE).waitForReportComputing()
                .getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .checkShowOnSecondaryAxis();
        analysisPage.saveInsight().waitForReportComputing();
        analysisPage.openConfigurationPanelBucket()
                .getItemConfiguration(Y_AXIS.toString() + " (Right)").expandConfiguration().setMinMaxValueOnAxis(rightMin, rightMax);
        analysisPage.openConfigurationPanelBucket()
                .getItemConfiguration(Y_AXIS.toString() + " (Left)").expandConfiguration().setMinMaxValueOnAxis(leftMin, leftMax);

        String expectedRightMin = "-20";
        String expectedRightMax = "80";
        String expectedLeftMin = "-8";
        String expectedLeftMax = "32";
        ChartReport chartReport = analysisPage.getChartReport();
        assertThat(chartReport.getValuePrimaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems(expectedLeftMin, expectedLeftMax));

        assertThat(chartReport.getValueSecondaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems(expectedRightMin, expectedRightMax));
    }

    private void createSimpleInsight(String title, String metric, ReportType reportType) {
            indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private String createInsightHasTwoMeasureAndAttribute(
            String title, String attribute, ReportType reportType, String... metric) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[0])),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[1]))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private void createInsightHasTwoMeasure(String title, ReportType reportType, String... metric) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[0])),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[1])))));
    }

    private void createInsight(String title, String metric, String attribute, ReportType reportType) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }
}
