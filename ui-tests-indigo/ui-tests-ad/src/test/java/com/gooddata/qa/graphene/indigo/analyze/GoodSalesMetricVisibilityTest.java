package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesMetricVisibilityTest extends AbstractAnalyseTest {

    private static final String RATIO_METRIC = "Ratio metric";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Metric-Visibility-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfOpenOppsMetric();
        createNumberOfWonOppsMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void createPrivateMetric() {
        String expectedMaql = "SELECT " + METRIC_NUMBER_OF_WON_OPPS + " / " + METRIC_NUMBER_OF_OPEN_OPPS;
        assertTrue(initMetricPage()
            .createRatioMetric(RATIO_METRIC, METRIC_NUMBER_OF_WON_OPPS, METRIC_NUMBER_OF_OPEN_OPPS)
            .isMetricCreatedSuccessfully(RATIO_METRIC, expectedMaql, "#,##0.00"));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"test"})
    public void testPrivateMetric() {
        assertEquals(analysisPage.addMetric(RATIO_METRIC)
                .addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"test"})
    public void testPrivateMetricVisibility() throws JSONException, IOException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initAnalysePage();
            assertFalse(analysisPage.getCataloguePanel().search(RATIO_METRIC));
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private boolean deleteMetric(String metric) {
        if (!initMetricPage().isMetricVisible(RATIO_METRIC)) {
            return true;
        }
        MetricPage.getInstance(browser).openMetricDetailPage(RATIO_METRIC)
            .deleteObject();

        return !initMetricPage().isMetricVisible(RATIO_METRIC);
    }
}
