package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class MetricsAccessibilityTest extends DashboardWithWidgetsTest {

    private static final String PUBLIC_METRIC_OF_ADMIN = "admin-public-metric";
    private static final String PRIVATE_METRIC_OF_ADMIN = "admin-private-metric";

    private static final String PUBLIC_METRIC_OF_EDITOR = "editor-public-metric";
    private static final String PRIVATE_METRIC_OF_EDITOR = "editor-private-metric";

    private static final String SIMPLE_METRIC_EXPRESSION = "SELECT 1";

    @BeforeClass(alwaysRun = true)
    public void before() {
        super.before();
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void prepareMetrics() {
        createPublicMetric(getGoodDataClient(), PUBLIC_METRIC_OF_ADMIN);
        createPrivateMetric(getGoodDataClient(), PRIVATE_METRIC_OF_ADMIN);

        final GoodData goodData = getGoodDataClient(testParams.getEditorUser(), testParams.getEditorPassword());
        createPublicMetric(goodData, PUBLIC_METRIC_OF_EDITOR);
        createPrivateMetric(goodData, PRIVATE_METRIC_OF_EDITOR);
    }

    @Test(dependsOnMethods = {"prepareMetrics"}, groups = {"desktop"})
    public void testMetricsAccessibility() {
        final MetricSelect metricSelect = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .clickAddWidget()
            .getConfigurationPanel()
            .getMetricSelect();

        metricSelect.searchByName(PUBLIC_METRIC_OF_ADMIN);
        takeScreenshot(browser, "admin-can-see-his-public-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchByName(PRIVATE_METRIC_OF_ADMIN);
        takeScreenshot(browser, "admin-can-see-his-private-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchByName(PUBLIC_METRIC_OF_EDITOR);
        takeScreenshot(browser, "admin-can-see-editor-public-metric", this.getClass());
        assertFalse(metricSelect.getValues().isEmpty());
        assertFalse(metricSelect.isShowingNoMatchingDataMessage());

        metricSelect.searchByName(PRIVATE_METRIC_OF_EDITOR);
        takeScreenshot(browser, "admin-can-NOT-see-editor-private-metric", this.getClass());
        assertTrue(metricSelect.getValues().isEmpty());
        assertTrue(metricSelect.isShowingNoMatchingDataMessage());
    }

    private void createPublicMetric(final GoodData goodData, final String name) {
        final Project project = goodData.getProjectService().getProjectById(testParams.getProjectId());
        goodData.getMetadataService().createObj(project, new Metric(name, SIMPLE_METRIC_EXPRESSION, "#,##0"));
    } 

    private void createPrivateMetric(final GoodData goodData, final String name) {
        final Project project = goodData.getProjectService().getProjectById(testParams.getProjectId());
        final MetadataService mdService = goodData.getMetadataService();
        final Metric privateMetric = mdService.createObj(project, new Metric(name, SIMPLE_METRIC_EXPRESSION, "#,##0"));
        privateMetric.setUnlisted(true);
        mdService.updateObj(privateMetric);
    }
}
