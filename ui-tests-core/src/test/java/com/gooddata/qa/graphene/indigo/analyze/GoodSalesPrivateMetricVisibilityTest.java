package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.UserRoles;

public class GoodSalesPrivateMetricVisibilityTest extends AnalyticalDesignerAbstractTest {

    private static final String RATIO_METRIC = "Ratio metric";
    private static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    private static final String NUMBER_OF_OPEN_OPPS = "# of Open Opps.";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Demo-Private-Metric-Visibility-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void addUsersWithOtherRolesToProject() throws ParseException, IOException, JSONException {
        super.addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"addUsersWithOtherRolesToProject"})
    public void createPrivateMetric() {
        assertTrue(deleteMetric(RATIO_METRIC));
        initMetricPage();
        waitForFragmentVisible(metricPage).createRatioMetric(RATIO_METRIC, NUMBER_OF_WON_OPPS,
                NUMBER_OF_OPEN_OPPS);

        String expectedMaql = "SELECT " + NUMBER_OF_WON_OPPS + " / " + NUMBER_OF_OPEN_OPPS;
        assertTrue(metricPage.isMetricCreatedSuccessfully(RATIO_METRIC, expectedMaql, "#,##0.00"));
    }

    @Test(dependsOnMethods = {"createPrivateMetric"})
    public void testPrivateMetric() {
        initAnalysePage();
        assertEquals(analysisPage.addMetric(RATIO_METRIC)
                .addCategory(DEPARTMENT)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 2);
    }

    @Test(dependsOnMethods = {"createPrivateMetric"})
    public void testPrivateMetricVisibility() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initAnalysePage();
            assertFalse(analysisPage.searchBucketItem(RATIO_METRIC));
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    private boolean deleteMetric(String metric) {
        initMetricPage();
        if (!waitForFragmentVisible(metricPage).isMetricVisible(RATIO_METRIC)) {
            return true;
        }
        metricPage.openMetricDetailPage(RATIO_METRIC);
        waitForFragmentVisible(metricDetailPage).deleteMetric();

        initMetricPage();
        return !waitForFragmentVisible(metricPage).isMetricVisible(RATIO_METRIC);
    }
}
