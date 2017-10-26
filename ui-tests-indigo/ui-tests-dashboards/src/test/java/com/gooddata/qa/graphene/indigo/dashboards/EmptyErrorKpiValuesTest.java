package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;

public class EmptyErrorKpiValuesTest extends AbstractDashboardTest {

    private Metric errorMetric;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void createEmptyMetric() {
        String amountUri = getMdService().getObjUri(getProject(), Metric.class, Restriction.title(METRIC_AMOUNT));
        errorMetric = getMdService().createObj(getProject(), new Metric("ERROR",
                "SELECT [" + amountUri + "] WHERE 2 = 1", "#,##0.00"));
    }

    @Test(dependsOnMethods = {"createEmptyMetric"}, groups = {"desktop"})
    public void testEmptyMetricWithoutConditionalFormat() throws ParseException, JSONException, IOException {
        initIndigoDashboardsPage().getSplashScreen()
            .startEditingWidgets()
            .addKpi(new KpiConfiguration.Builder()
                    .metric(errorMetric.getTitle())
                    .dataSet(DATE_DATASET_CREATED)
                    .build())
            .saveEditModeWithWidgets();

        Kpi lastKpi = waitForFragmentVisible(indigoDashboardsPage).getLastWidget(Kpi.class);

        takeScreenshot(browser, "testEmptyMetricWithoutConditionalFormat", getClass());

        assertTrue(lastKpi.isEmptyValue());
        assertEquals(lastKpi.getTooltipOfValue(),
                "No data for current filter settings. Try changing the filters.");
    }

    @Test(dependsOnMethods = {"testEmptyMetricWithoutConditionalFormat"}, groups = {"desktop"})
    public void testEmptyMetricWithConditionalFormat() throws ParseException, JSONException, IOException {
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), errorMetric.getUri(), "[=NULL]empty;#,##0.00");

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

            takeScreenshot(browser, "testEmptyMetricWithConditionalFormat", getClass());

            assertEquals(lastKpi.getValue(), "empty");
        } finally {
            DashboardsRestUtils.changeMetricFormat(getRestApiClient(), errorMetric.getUri(), "#,##0.00");
        }
    }

    @Test(dependsOnMethods = {"testEmptyMetricWithoutConditionalFormat"}, groups = {"desktop"})
    public void testInvalidKpiValue() throws ParseException, JSONException, IOException {
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(ATTR_ACCOUNT));
        DashboardsRestUtils.changeMetricExpression(getRestApiClient(), errorMetric.getUri(),
                "SELECT [" + accountUri + "] WHERE 2 = 1");

        Kpi lastKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);

        takeScreenshot(browser, "testEmptyMetricWithoutConditionalFormat", getClass());

        assertTrue(lastKpi.isErrorValue());
        assertEquals(lastKpi.getTooltipOfValue(),
                "KPI cannot be displayed. Contact your administrator to fix the KPI definition.");

        waitForFragmentVisible(indigoDashboardsPage)
            .switchToEditMode();
        assertEquals(lastKpi.getTooltipOfValue(),
                "KPI cannot be displayed. Check if the measure definition is properly defined.");
        indigoDashboardsPage.leaveEditMode();
    }
}
