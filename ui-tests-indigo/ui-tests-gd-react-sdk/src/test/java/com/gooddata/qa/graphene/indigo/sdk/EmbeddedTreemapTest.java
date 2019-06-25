package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATIONS;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_ABSOLUTE_DATE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_CONFIGURATIONS;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_NEGATIVE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_BY_IDENTIFIER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_BY_URI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EmbeddedTreemapTest extends AbstractReactSdkTest {

    private static final String DIRECT_SALES = "Direct Sales";
    private static final String INSIDE_SALES = "Inside Sales";
    private IndigoRestRequest indigoRestRequest;
    private DashboardRestRequest dashboardRestRequest;

    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login")
    public void embedTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        String insightUrl = createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap), Pair.of("visualizationUrl", insightUrl));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_URI);
        initSDKAnalysisPage();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));
    }

    @Test(dependsOnMethods = "login")
    public void embedTreemapInsights() throws IOException {
        String treemap = "Treemap Report";
        String tableReport = "Table Report";
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(tableReport, ReportType.TABLE).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        createCatalogJSON(Pair.of("firstVisualizationName", treemap), Pair.of("secondVisualizationName", tableReport));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATIONS);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();
        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        PivotTableReport sdkTableReport = sdkAnalysisPage.getPivotTableReport();
        assertEquals(sdkTableReport.getHeaders(), singletonList(METRIC_AMOUNT));
        assertEquals(sdkTableReport.getBodyContent(), singletonList(singletonList("$116,625,456.54")));
    }

    @Test(dependsOnMethods = "login")
    public void updateTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        initSDKAnalysisPage(); //refresh SDK page to apply change
        initAnalysePage().openInsight(treemap).addStack(ATTR_IS_CLOSED).changeReportType(ReportType.TABLE).saveInsight();
        assertEquals(initSDKAnalysisPage().getPivotTableReport().getHeaders(),
                asList(ATTR_IS_CLOSED, "TRUE", ATTR_DEPARTMENT, METRIC_NUMBER_OF_ACTIVITIES));

        initAnalysePage().openInsight(treemap).removeColumn(ATTR_IS_CLOSED).changeReportType(ReportType.TREE_MAP).saveInsight();
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));
    }

    @Test(dependsOnMethods = "login")
    public void deleteTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        indigoRestRequest.deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(treemap));
        assertEquals(initSDKAnalysisPage().getWarning(),
                "SORRY, WE CAN'T DISPLAY THIS INSIGHT\nContact your administrator.");
    }

    @Test(dependsOnMethods = "login")
    public void filterEmbedTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES)),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_NEGATIVE_FILTER);
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), singletonList("rgb(20,178,226)"));
        assertEquals(chartReport.getLegends(), singletonList(INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        createCatalogJSON(Pair.of("visualizationName", treemap),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES)),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_FILTER);
        initSDKAnalysisPage();
        assertEquals(chartReport.getLegendColors(), singletonList("rgb(20,178,226)"));
        assertEquals(chartReport.getLegends(), singletonList(DIRECT_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));

        createCatalogJSON(Pair.of("visualizationName", treemap),
                Pair.of("dateAttributeName", "Date (Created)"),
                Pair.of("from", "2010-01-01"),
                Pair.of("to", "2011-01-01"));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_ABSOLUTE_DATE_FILTER);
        initSDKAnalysisPage();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "33,211")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "17,283")));
    }

    @Test(dependsOnMethods = "login")
    public void embedTreemapHasRestrictedFact() throws IOException {
        FactRestRequest factRestRequest = new FactRestRequest(getAdminRestClient(), testParams.getProjectId());
        factRestRequest.setFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());
        try {
            String treemap = "Treemap " + generateHashString();
            createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_AMOUNT),
                    singletonList(ATTR_DEPARTMENT));
            createCatalogJSON(Pair.of("visualizationName", treemap));
            replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
            assertEquals(initSDKAnalysisPage().getWarning(), "SORRY, WE CAN'T DISPLAY THIS INSIGHT\nContact your administrator.");
        } finally {
            factRestRequest.unsetFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());
        }
    }

    @Test(dependsOnMethods = "login")
    public void embedToLargeTreemapReport() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_OPPORTUNITY));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        assertEquals(initSDKAnalysisPage().getWarning(), "TOO MANY DATA POINTS TO DISPLAY\nTry applying filters.");
    }

    @Test(dependsOnMethods = "login")
    public void embedLimitedReport() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_ACTIVITY));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        assertEquals(initSDKAnalysisPage().getWarning(), "SORRY, WE CAN'T DISPLAY THIS INSIGHT\nContact your administrator.");
    }

    @Test(dependsOnMethods = "login")
    public void embedNoDataReport() throws IOException {
        Metric metric = getMdService().createObj(getProject(),
                new Metric("nodata_metric", "SELECT [" + getMetricByTitle(METRIC_AMOUNT).getUri() + "] WHERE 2 = 1", "#,##0.00"));
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(metric.getTitle()),
                singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        assertEquals(initSDKAnalysisPage().getWarning(), "NO DATA\nNo data for your filter selection.");
    }

    @Test(dependsOnMethods = "login")
    public void embedTreemapWithMetricApplyPercent() throws IOException {
        String treemap = "Treemap " + generateHashString();
        indigoRestRequest.createInsight(new InsightMDConfiguration(treemap, ReportType.TREE_MAP)
                .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                        getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES), true)))
                .setCategoryBucket(singletonList(CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.VIEW))));

        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "65.50%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "34.50%")));
    }

    @Test(dependsOnMethods = "login")
    public void changeFormatEmbeddedTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), Formatter.DEFAULT.toString());
        try {
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES).getMetricFormat(),
                    Formatter.DEFAULT.toString());
            ChartReport chartReport = initSDKAnalysisPage().getChartReport();
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054.00")));
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217.00")));
        } finally {
            dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), "#,##0");
        }
    }

    @Test(dependsOnMethods = "login")
    public void configEmbeddedTreemapInsight() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap),
                Pair.of("hasLegend", "true"),
                Pair.of("firstColor", "rgb(168,194,86)"),
                Pair.of("secondColor", "rgb(195,49,73)"));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_CONFIGURATIONS);
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(168,194,86)", "rgb(195,49,73)"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        createCatalogJSON(Pair.of("visualizationName", treemap),
                Pair.of("hasLegend", "false"),
                Pair.of("firstColor", "rgb(168,194,86)"),
                Pair.of("secondColor", "rgb(195,49,73)"));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_CONFIGURATIONS);
        initSDKAnalysisPage();
        assertFalse(chartReport.isLegendVisible(), "Legend shouldn't be visible");
    }

    private String createInsight(String title, ReportType type, List<String> metricTitles, List<String> attributeTitles) {
        List<MeasureBucket> measureBuckets = metricTitles.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());
        List<CategoryBucket> categoryBuckets = attributeTitles.stream()
                .map(attribute ->
                        CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.VIEW))
                .collect(Collectors.toList());
        return indigoRestRequest.createInsight(new InsightMDConfiguration(title, type)
                .setMeasureBucket(measureBuckets)
                .setCategoryBucket(categoryBuckets));
    }
}
