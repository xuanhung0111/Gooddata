package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATIONS_OF_PROJECTS_BY_IDENTIFIER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATIONS_OF_PROJECTS_BY_URI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizationTest extends AbstractReactSdkTest {

    private static final String DIRECT_SALES = "Direct Sales";
    private static final String INSIDE_SALES = "Inside Sales";
    private String sourceProjectId;
    private String targetProjectId;

    @Override
    protected void customizeProject() throws IOException {
        getMetricCreator().createNumberOfActivitiesMetric();
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createProjectUsingFixture(
                "Target-Project", ResourceManagement.ResourceTemplate.GOODSALES, testParams.getDomainUser());
        testParams.setProjectId(targetProjectId);
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        getMetricCreator().createNumberOfActivitiesMetric();
        testParams.setProjectId(sourceProjectId);
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login")
    public void embedVisualizationInsightOfProjectsByIdentifier() throws IOException {
        String treemap = "Treemap " + generateHashString();
        createTreemap(treemap, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationName", treemap));

        String headline = "Headline " + generateHashString();
        createHeadline(headline, METRIC_NUMBER_OF_ACTIVITIES, targetProjectId);
        createTestingVariable(
                Pair.of("firstVisualizationName", treemap),
                Pair.of("secondVisualizationName", headline));
        createCatalogExportConfig(targetProjectId, "other-catalog.json");
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATIONS_OF_PROJECTS_BY_IDENTIFIER);
        initDashboardsPage(); //clear cache of localhost:3000
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        assertEquals(sdkAnalysisPage.getHeadline().getPrimaryItem(), "154,271");
    }

    @Test(dependsOnMethods = "login")
    public void embedVisualizationInsightOfProjectsByUri() throws IOException {
        String treemap = "Treemap " + generateHashString();
        String treemapUri = createTreemap(treemap, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_DEPARTMENT));
        createCatalogJSON(Pair.of("visualizationUri", treemapUri));

        String headline = "Headline " + generateHashString();
        String headlineUri = createHeadline(headline, METRIC_NUMBER_OF_ACTIVITIES, targetProjectId);
        createTestingVariable(
                Pair.of("firstVisualizationUri", treemapUri),
                Pair.of("secondVisualizationUri", headlineUri));
        createCatalogExportConfig(targetProjectId, "other-catalog.json");
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATIONS_OF_PROJECTS_BY_URI);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getLegendColors(), asList("rgb(20,178,226)", "rgb(0,193,141)"));
        assertEquals(chartReport.getLegends(), asList(DIRECT_SALES, INSIDE_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, INSIDE_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "53,217")));

        assertEquals(sdkAnalysisPage.getHeadline().getPrimaryItem(), "154,271");
    }

    private String createTreemap(String title, List<String> metricTitles, List<String> attributeTitles) {
        List<MeasureBucket> measureBuckets = metricTitles.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());
        List<CategoryBucket> categoryBuckets = attributeTitles.stream()
                .map(attribute ->
                        CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.VIEW))
                .collect(Collectors.toList());
        return new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .createInsight(new InsightMDConfiguration(title, ReportType.TREE_MAP)
                .setMeasureBucket(measureBuckets)
                .setCategoryBucket(categoryBuckets));
    }

    private String createHeadline(String title, String metric) {
        return new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId()).createInsight(
                new InsightMDConfiguration(title, ReportType.HEADLINE).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private String createHeadline(String title, String metric, String projectID) {
        testParams.setProjectId(projectID);
        String chartUri = createHeadline(title, metric);
        testParams.setProjectId(sourceProjectId);
        return chartUri;
    }
}
