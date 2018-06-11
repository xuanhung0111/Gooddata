package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class MetricsAccessibilityTest extends AbstractDashboardTest {

    private static final String PUBLIC_METRIC_OF_ADMIN = "admin-public-metric";
    private static final String PRIVATE_METRIC_OF_ADMIN = "admin-private-metric";

    private static final String PUBLIC_METRIC_OF_EDITOR = "editor-public-metric";
    private static final String PRIVATE_METRIC_OF_EDITOR = "editor-private-metric";

    private static final String SIMPLE_METRIC_EXPRESSION = "SELECT 1";

    @Override
    protected void customizeProject() throws Throwable {
        //add more metrics to display search metric
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createAmountMetric();
        metricCreator.createTimelineEOPMetric();
        metricCreator.createLostMetric();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpportunitiesMetric();
        metricCreator.createTimelineBOPMetric();
        metricCreator.createNumberOfOpportunitiesBOPMetric();
        metricCreator.createWonMetric();
        metricCreator.createPercentOfGoalMetric();
        metricCreator.createQuotaMetric();
        metricCreator.createProbabilityMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void prepareMetrics() {
        createPublicMetric(new RestClient(getProfile(Profile.ADMIN)), PUBLIC_METRIC_OF_ADMIN);
        createPrivateMetric(new RestClient(getProfile(Profile.ADMIN)), PRIVATE_METRIC_OF_ADMIN);

        final RestClient editorRestClient = new RestClient(getProfile(Profile.EDITOR));
        createPublicMetric(editorRestClient, PUBLIC_METRIC_OF_EDITOR);
        createPrivateMetric(editorRestClient, PRIVATE_METRIC_OF_EDITOR);
    }

    @Test(dependsOnMethods = {"prepareMetrics"}, groups = {"desktop"})
    public void testMetricsAccessibility() {
        final MetricSelect metricSelect = initIndigoDashboardsPage().getSplashScreen()
                .startEditingWidgets()
                .dragAddKpiPlaceholder()
                .getConfigurationPanel()
                .getMetricSelect();

        metricSelect.searchForText(PUBLIC_METRIC_OF_ADMIN);
        takeScreenshot(browser, "admin-can-see-his-public-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchForText(PRIVATE_METRIC_OF_ADMIN);
        takeScreenshot(browser, "admin-can-see-his-private-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchForText(PUBLIC_METRIC_OF_EDITOR);
        takeScreenshot(browser, "admin-can-see-editor-public-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchForText(PRIVATE_METRIC_OF_EDITOR);
        takeScreenshot(browser, "admin-can-NOT-see-editor-private-metric", this.getClass());
        assertTrue(metricSelect.getValues().isEmpty());
        assertTrue(metricSelect.isShowingNoMatchingDataMessage());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void createPublicMetric(final RestClient restClient, final String name) {
        final Project project = restClient.getProjectService().getProjectById(testParams.getProjectId());
        restClient.getMetadataService().createObj(project, new Metric(name, SIMPLE_METRIC_EXPRESSION, "#,##0"));
    } 

    private void createPrivateMetric(final RestClient restClient, final String name) {
        final Project project = restClient.getProjectService().getProjectById(testParams.getProjectId());
        final MetadataService mdService = restClient.getMetadataService();
        final Metric privateMetric = mdService.createObj(project, new Metric(name, SIMPLE_METRIC_EXPRESSION, "#,##0"));
        privateMetric.setUnlisted(true);
        mdService.updateObj(privateMetric);
    }
}
