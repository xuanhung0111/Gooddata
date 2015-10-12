package com.gooddata.qa.graphene.indigo.dashboards;

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
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.RestUtils;

public class EmptyErrorKpiValuesTest extends DashboardWithWidgetsTest {

    private Metric errorMetric;

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void createEmptyMetric() {
        String amountUri = getMdService().getObjUri(getProject(), Metric.class, Restriction.title(AMOUNT));
        errorMetric = getMdService().createObj(getProject(), new Metric("ERROR",
                "SELECT [" + amountUri + "] WHERE 2 = 1", "#,##0.00"));
    }

    @Test(dependsOnMethods = {"createEmptyMetric"}, groups = {"desktop"})
    public void testEmptyMetricWithoutConditionalFormat() {
        setupKpi(new KpiConfiguration.Builder()
            .metric(errorMetric.getTitle())
            .dateDimension(DATE_CREATED)
            .build());

        Kpi lastKpi = initIndigoDashboardsPageWithWidgets()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

        takeScreenshot(browser, "testEmptyMetricWithoutConditionalFormat", getClass());

        assertTrue(lastKpi.isEmptyValue());
        assertEquals(lastKpi.getTooltipOfValue(),
                "No data for current filter settings. Try changing the filters.");
    }

    @Test(dependsOnMethods = {"testEmptyMetricWithoutConditionalFormat"}, groups = {"desktop"})
    public void testEmptyMetricWithConditionalFormat() throws ParseException, JSONException, IOException {
        RestUtils.changeMetricFormat(getRestApiClient(), errorMetric.getUri(), "[=NULL]empty;#,##0.00");

        try {
            Kpi lastKpi = initIndigoDashboardsPageWithWidgets()
                    .waitForAllKpiWidgetContentLoaded()
                    .getLastKpi();

            takeScreenshot(browser, "testEmptyMetricWithConditionalFormat", getClass());

            assertEquals(lastKpi.getValue(), "empty");
        } finally {
            RestUtils.changeMetricFormat(getRestApiClient(), errorMetric.getUri(), "#,##0.00");
        }
    }

    @Test(dependsOnMethods = {"testEmptyMetricWithoutConditionalFormat"}, groups = {"desktop"})
    public void testInvalidKpiValue() throws ParseException, JSONException, IOException {
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(ACCOUNT));
        RestUtils.changeMetricExpression(getRestApiClient(), errorMetric.getUri(),
                "SELECT [" + accountUri + "] WHERE 2 = 1");

        Kpi lastKpi = initIndigoDashboardsPageWithWidgets()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

        takeScreenshot(browser, "testEmptyMetricWithoutConditionalFormat", getClass());

        assertTrue(lastKpi.isErrorValue());
        assertEquals(lastKpi.getTooltipOfValue(),
                "KPI cannot be displayed. Contact your administrator to fix the KPI definition.");

        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode();
        assertEquals(lastKpi.getTooltipOfValue(),
                "KPI cannot be displayed. Check if the measure definition is properly defined.");
        indigoDashboardsPage.leaveEditMode();
    }
}
