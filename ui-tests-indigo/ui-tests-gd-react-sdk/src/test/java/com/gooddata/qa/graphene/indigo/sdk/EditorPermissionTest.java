package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_BY_IDENTIFIER;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EditorPermissionTest extends AbstractReactSdkTest {

    private static final String DIRECT_SALES = "Direct Sales";
    private IndigoRestRequest indigoRestRequest;
    private DashboardRestRequest dashboardRestRequest;

    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.EDITOR);
    }

    @Test(dependsOnMethods = "login")
    public void embedTreemapHasMUF() throws IOException {
        final String productValues = format("[%s]", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES));
        final String expression = format("[%s] IN (%s)", getAttributeByTitle(ATTR_DEPARTMENT).getUri(), productValues);
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        final String mufUri = dashboardRestRequest.createMufObjectByUri("muf", expression);
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String assignedMufUserId = userManagementRestRequest
                .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());

        dashboardRestRequest.addMufToUser(assignedMufUserId, mufUri);
        String treemap = "Treemap " + generateHashString();
        createInsight(treemap, ReportType.TREE_MAP, singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_DEPARTMENT));
        initAnalysePage().openInsight(treemap);
        createCatalogJSON(Pair.of("visualizationName", treemap));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        ChartReport chartReport = initSDKAnalysisPage().getChartReport();
        assertEquals(chartReport.getLegendColors(), singletonList("rgb(20,178,226)"));
        assertEquals(chartReport.getLegends(), singletonList(DIRECT_SALES));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, DIRECT_SALES), asList(METRIC_NUMBER_OF_ACTIVITIES, "101,054")));
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
