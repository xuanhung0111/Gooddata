package com.gooddata.qa.graphene.indigo.analyze;

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
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.COLORS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.X_AXIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.Y_AXIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.LEGEND;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.CANVAS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class SecondaryAxisTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TEST_DASHBOARD = "Dashboard";
    private static final String INSIGHT_HAS_TWO_MEASURE_AND_ATTRIBUTE = "Two measure and attribute";
    private static final String INSIGHT_HAS_A_METRIC = "A metric";
    private static final String INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES = "A metric and attributes";
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private final List<String> defaultItemsConfigurationPanel = asList(COLORS, X_AXIS, Y_AXIS, LEGEND, CANVAS);

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Allow user to set a secondary axis for insights with multiple measures";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createCloseEOPMetric();

        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setDualAxesFlag(true);
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
    public void disableDualAxes_ForTheDualAxesCheckboxIsNotShowed() {
        try {
            setDualAxesFlag(false);
            createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT, ReportType.COLUMN_CHART);
            MetricConfiguration metricConfiguration = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC)
                    .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                    .expandConfiguration();
            assertFalse(metricConfiguration.isShowOnSecondaryAxisPresent(),
                     "“Show dual axes” checkbox should be not showed");

            analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
            metricConfiguration = analysisPage.addMetric(METRIC_AMOUNT, FieldType.FACT).waitForReportComputing()
                    .getMetricsBucket().getMetricConfiguration("Sum of " + METRIC_AMOUNT).expandConfiguration();
            assertFalse(metricConfiguration.isShowOnSecondaryAxisPresent(),
                    "“Show dual axes” checkbox should be not showed");

            analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
            metricConfiguration = analysisPage.addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE).waitForReportComputing()
                    .getMetricsBucket().getMetricConfiguration("Count of " + ATTR_DEPARTMENT).expandConfiguration();
            assertFalse(metricConfiguration.isShowOnSecondaryAxisPresent(),
                     "“Show dual axes” checkbox should be not showed");
        } finally {
            setDualAxesFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showSecondaryAxisOnConfigurationPanelWithAMetric() {
        createInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES, METRIC_AMOUNT, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getSencondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        ConfigurationPanelBucket configurationPanelBucket = analysisPage.getConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getSencondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getSencondaryYaxisTitle(), METRIC_AMOUNT);
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
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        ConfigurationPanelBucket configurationPanelBucket = analysisPage.getConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
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
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");

        ConfigurationPanelBucket configurationPanelBucket = analysisPage.getConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
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
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");

        ConfigurationPanelBucket configurationPanelBucket = analysisPage.getConfigurationPanelBucket();
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
        assertFalse(chartReport.isSecondaryYaxisVisible(), "Rerender insight shouldn't have secondary axis");
        assertEquals(configurationPanelBucket.getItemNames(), defaultItemsConfigurationPanel);

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);
        assertTrue(chartReport.isPrimaryYaxisVisible(), "Rerender insight should have primary axis");
        assertEquals(chartReport.getSencondaryYaxisTitle(), StringUtils.EMPTY);
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
        assertEquals(chartReport.getSencondaryYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.isSecondaryYaxisVisible(),
                "The Column chart should render well with Y-axis on right of chart");
    }

    private void createSimpleInsight(String title, String metric, ReportType reportType) {
            indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private void createInsightHasTwoMeasureAndAttribute(
            String title, String attribute, ReportType reportType, String... metric) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[0])),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric[1]))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE))));
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

    private void setDualAxesFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_DUAL_AXIS, status);
    }
}
