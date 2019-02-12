package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class RenamingMeasuresAndAttributesTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Insight has attribute and measure";
    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK = "Insight has attribute and measure, stack";
    private static final String INSIGHT_HAS_SAME_ATTRIBUTES = "Insight has the same attribute on measures," +
            " view by and stack by";
    private static final String INSIGHT_HAS_SAME_METRICS = "Insight has the same metrics";
    private IndigoRestRequest indigoRestRequest;

    private String emptyTitle = "";
    private String longTitle = RandomStringUtils.randomAlphabetic(50);
    private String specialTitle = "@#$%^&*()âêûťžŠô";
    private String xssTitle = "<script>alert(\"TEST\");</script>)";
    private String renamingInsight = "RenamingInsight";
    private String newMetric = "New-Amount";
    private String newAttribute = "New-Department";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Renaming Of Measures and Attributes";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOriginalNameAfterRenamingTitleMetric() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, newMetric);
        MetricConfiguration metricConfiguration = metricsBucket.getMetricConfiguration(newMetric);
        assertEquals(metricConfiguration.getHeader(), newMetric);
        assertEquals(metricConfiguration.getSubHeader(), METRIC_AMOUNT);

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getYaxisTitle(), newMetric);

        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0),
                hasItems(asList(newMetric, "$80,406,324.96"), asList(ATTR_DEPARTMENT, "Direct Sales")));
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(1),
                hasItems(asList(newMetric, "$36,219,131.58"), asList(ATTR_DEPARTMENT, "Inside Sales")));

        createInsightHaveStack(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK, METRIC_AMOUNT, ATTR_DEPARTMENT,
                ATTR_FORECAST_CATEGORY);
        metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK)
                .getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, newMetric);

        metricConfiguration = metricsBucket.getMetricConfiguration(newMetric);
        assertEquals(metricConfiguration.getHeader(), newMetric);
        assertEquals(metricConfiguration.getSubHeader(), METRIC_AMOUNT);
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), newMetric);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOriginalNameAfterDeletingTitleMetric() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, "");
        MetricConfiguration metricConfiguration = metricsBucket.getMetricConfiguration(METRIC_AMOUNT);
        assertEquals(metricConfiguration.getHeader(), METRIC_AMOUNT);
        assertFalse(metricConfiguration.isSubHeaderPresent(), "SubHeader should be removed");
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getYaxisTitle(), METRIC_AMOUNT);
        assertTrue(chartReport.getTooltipTextOnTrackerByIndex(0).get(1).contains(METRIC_AMOUNT),
                "Tooltip should show alias name");
        assertTrue(chartReport.getTooltipTextOnTrackerByIndex(1).get(1).contains(METRIC_AMOUNT),
                "Tooltip should show alias name");

        createInsightHaveStack(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK, METRIC_AMOUNT, ATTR_DEPARTMENT,
                ATTR_FORECAST_CATEGORY);
        metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK)
                .getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, "");
        metricConfiguration = metricsBucket.getMetricConfiguration(METRIC_AMOUNT);

        assertEquals(metricConfiguration.getHeader(), METRIC_AMOUNT);
        assertFalse(metricConfiguration.isSubHeaderPresent(), "SubHeader should be removed");
        assertEquals(analysisPage.getChartReport().getYaxisTitle(), METRIC_AMOUNT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterRenamingTheSameAttributes() {
        //change the same name of attributes
        createInsightHaveSameAttributes(INSIGHT_HAS_SAME_ATTRIBUTES, ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY,
                ATTR_FORECAST_CATEGORY);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTES).getMetricsBucket()
                .setTitleItemBucket("M1\n" + ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY);
        assertEquals(metricsBucket.getMetricConfiguration(ATTR_FORECAST_CATEGORY).getHeader(), ATTR_FORECAST_CATEGORY);

        AttributesBucket attributesBucket =
                analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY);
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        StacksBucket stacksBucket = analysisPage.getStacksBucket()
                .setTitleItemBucket(ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY);
        assertEquals(stacksBucket.getAttributeName(), ATTR_FORECAST_CATEGORY);

        //change different name of attributes
        String newMetric = "New-Forecast-Category";
        metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTES).getMetricsBucket()
                .setTitleItemBucket("M1\n" + ATTR_FORECAST_CATEGORY, newMetric);
        assertEquals(metricsBucket.getMetricConfiguration(newMetric).getHeader(), newMetric);

        attributesBucket = analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, newMetric);
        assertEquals(attributesBucket.getItemNames(), asList(newMetric + "\n" + ATTR_FORECAST_CATEGORY));
        stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, newMetric);
        assertEquals(stacksBucket.getAttributeName(), newMetric + "\n" + ATTR_FORECAST_CATEGORY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterRenamingTheSameMeasures() {
        //change the same name of metrics
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SAME_METRICS, ReportType.COLUMN_CHART)
                        .setMeasureBucket(asList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SAME_METRICS).getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, METRIC_AMOUNT);
        metricsBucket.setTitleItemBucket("M2\n" + METRIC_AMOUNT, METRIC_AMOUNT);
        assertEquals(metricsBucket.getMetricConfiguration(METRIC_AMOUNT).getHeader(), METRIC_AMOUNT);
        assertEquals(metricsBucket.getLastMetricConfiguration().getHeader(), METRIC_AMOUNT);

        //change different name of metrics
        metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SAME_METRICS).getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, newMetric);
        metricsBucket.setTitleItemBucket("M2\n" + METRIC_AMOUNT, newMetric);
        assertEquals(metricsBucket.getMetricConfiguration(newMetric).getHeader(), newMetric);
        assertEquals(metricsBucket.getLastMetricConfiguration().getHeader(), newMetric);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSpecialNameAfterRenamingTitleMetric() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, emptyTitle);
        assertEquals(metricsBucket.getMetricConfiguration(METRIC_AMOUNT).getHeader(), METRIC_AMOUNT);

        metricsBucket.setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        checkRedBar(browser);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);

        metricsBucket.setTitleItemBucket("M1\n" + longTitle + "\n" + METRIC_AMOUNT, specialTitle);
        checkRedBar(browser);
        assertEquals(metricsBucket.getMetricConfiguration(specialTitle).getHeader(), specialTitle);

        metricsBucket.setTitleItemBucket("M1\n" + specialTitle + "\n" + METRIC_AMOUNT, xssTitle);
        checkRedBar(browser);
        assertEquals(metricsBucket.getMetricConfiguration(xssTitle).getHeader(), xssTitle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAndReopeningOnInsightNoStack() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        analysisPage.saveInsight().waitForReportComputing();

        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));

        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAsAndReopeningOnInsightNoStack() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        analysisPage.saveInsightAs(renamingInsight).waitForReportComputing();
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));

        longTitle = RandomStringUtils.randomAlphabetic(50);
        specialTitle = "new-@#$%^&*()âêûťžŠô";

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);
        metricsBucket = analysisPage.getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        attributesBucket = analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAndReopeningOnInsightHasStack() {
        createInsightHaveStack(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK, METRIC_AMOUNT, ATTR_DEPARTMENT,
                ATTR_FORECAST_CATEGORY);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        StacksBucket stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);
        analysisPage.saveInsight().waitForReportComputing();

        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);

        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAsAndReopeningOnInsightHasStack() {
        createInsightHaveStack(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK, METRIC_AMOUNT, ATTR_DEPARTMENT,
                ATTR_FORECAST_CATEGORY);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK);
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        StacksBucket stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);

        analysisPage.saveInsightAs(renamingInsight).waitForReportComputing();
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);

        longTitle = RandomStringUtils.randomAlphabetic(50);
        specialTitle = "NEW-@#$%^&*()âêûťžŠô";
        xssTitle = "<button>alert(\"TEST\");</button>)";

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE_AND_STACK);
        metricsBucket = analysisPage.getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        attributesBucket = analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);
        stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAndReopeningOnInsightSameAttributes() {
        createInsightHaveSameAttributes(INSIGHT_HAS_SAME_ATTRIBUTES, ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY,
                ATTR_FORECAST_CATEGORY);
        initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTES);
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + ATTR_FORECAST_CATEGORY, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_FORECAST_CATEGORY, specialTitle);
        StacksBucket stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);

        analysisPage.saveInsight().waitForReportComputing();
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);

        analysisPage.openInsight(INSIGHT_HAS_SAME_ATTRIBUTES);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAsAndReopeningOnInsightSameAttributes() {
        createInsightHaveSameAttributes(INSIGHT_HAS_SAME_ATTRIBUTES, ATTR_FORECAST_CATEGORY, ATTR_FORECAST_CATEGORY,
                ATTR_FORECAST_CATEGORY);
        initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTES);
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + ATTR_FORECAST_CATEGORY, longTitle);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket()
                .setTitleItemBucket(ATTR_FORECAST_CATEGORY, specialTitle);
        StacksBucket stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);
        analysisPage.saveInsightAs(renamingInsight).waitForReportComputing();

        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);

        longTitle = RandomStringUtils.randomAlphabetic(50);
        specialTitle = "NEW-@#$%^&*()âêûťžŠô";
        xssTitle = "<button>alert(\"TEST\");</button>)";

        initAnalysePage().openInsight(INSIGHT_HAS_SAME_ATTRIBUTES);
        metricsBucket = analysisPage.getMetricsBucket()
                .setTitleItemBucket("M1\n" + ATTR_FORECAST_CATEGORY, longTitle);
        attributesBucket = analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, specialTitle);
        stacksBucket = analysisPage.getStacksBucket().setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssTitle);

        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(attributesBucket.getItemNames(), asList(specialTitle + "\n" + ATTR_FORECAST_CATEGORY));
        assertEquals(stacksBucket.getAttributeName(), xssTitle + "\n" + ATTR_FORECAST_CATEGORY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAndReopeningOnInsightSameMetrics() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SAME_METRICS, ReportType.COLUMN_CHART)
                        .setMeasureBucket(asList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        initAnalysePage();
        MetricsBucket metricsBucket = analysisPage.openInsight(INSIGHT_HAS_SAME_METRICS).getMetricsBucket();
        metricsBucket.setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        metricsBucket.setTitleItemBucket("M2\n" + METRIC_AMOUNT, specialTitle);

        analysisPage.saveInsight().waitForReportComputing();
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(metricsBucket.getMetricConfiguration(specialTitle).getHeader(), specialTitle);

        analysisPage.openInsight(INSIGHT_HAS_SAME_METRICS);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(metricsBucket.getMetricConfiguration(specialTitle).getHeader(), specialTitle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterSavingAsAndReopeningOnInsightSameMetrics() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SAME_METRICS, ReportType.COLUMN_CHART)
                        .setMeasureBucket(asList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        initAnalysePage();
        MetricsBucket metricsBucket = analysisPage.openInsight(INSIGHT_HAS_SAME_METRICS).getMetricsBucket();
        metricsBucket.setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        metricsBucket.setTitleItemBucket("M2\n" + METRIC_AMOUNT, specialTitle);

        analysisPage.saveInsightAs(renamingInsight).waitForReportComputing();
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(metricsBucket.getMetricConfiguration(specialTitle).getHeader(), specialTitle);

        analysisPage.openInsight(INSIGHT_HAS_SAME_METRICS);
        longTitle = RandomStringUtils.randomAlphabetic(50);
        specialTitle = "NEW-@#$%^&*()âêûťžŠô";
        metricsBucket.setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        metricsBucket.setTitleItemBucket("M2\n" + METRIC_AMOUNT, specialTitle);
        assertEquals(metricsBucket.getMetricConfiguration(longTitle).getHeader(), longTitle);
        assertEquals(metricsBucket.getMetricConfiguration(specialTitle).getHeader(), specialTitle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAliasesAfterAddingInsightOnKDDashboard() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);
        analysisPage.getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, newMetric);
        analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_DEPARTMENT, newAttribute);
        analysisPage.saveInsight();

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForWidgetsLoading();
        indigoDashboardsPage.selectFirstWidget(Insight.class);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        ChartReport chartReport = indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport();
        indigoDashboardsPage.waitForWidgetsLoading();
        assertEquals(chartReport.getYaxisTitle(), newMetric);
        assertEquals(chartReport.getXaxisTitle(), newAttribute);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testTooltipIsHiddenOnInsightHasAlias() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);
        analysisPage.getMetricsBucket().setTitleItemBucket("M1\n" + METRIC_AMOUNT, longTitle);
        analysisPage.getAttributesBucket().setTitleItemBucket(ATTR_DEPARTMENT, specialTitle);

        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled(), "Export report should be disabled");
        assertEquals(analysisPage.getPageHeader().getExportButtonTooltipText(), "The insight is not compatible" +
                " with Report Editor. To open the insight as a report," +
                " do not rename measures and/or attributes in the insight definition.");
    }

    private void createInsight(String title, String metric, String attribute) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private void createInsightHaveStack(String title, String metric, String attribute, String stack) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private void createInsightHaveSameAttributes(String title, String metric, String attribute, String stack) {
        Metric nonRelatedMetric = createMetric(metric, format("SELECT COUNT([%s])",
                getAttributeByTitle(metric).getUri()));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(nonRelatedMetric)))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }
}
