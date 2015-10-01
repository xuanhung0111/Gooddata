package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.IOException;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;

public class MetricFormattingTest extends DashboardWithWidgetsTest {

    private static final String PERCENT_OF_GOAL = "% of Goal";

    @DataProvider(name = "formattingProvider")
    public Object[][] formattingProvider() {
        return new Object[][] {
            {Formatter.BARS, null, true},
            {Formatter.GDC, "GDC154,271.00", false},
            {Formatter.DEFAULT, "154,271.00", false},
            {Formatter.TRUNCATE_NUMBERS, "$154.3 K", false},
            {Formatter.COLORS, "$154,271", false},
            {Formatter.UTF_8, Formatter.UTF_8.toString(), false}
        };
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, dataProvider = "formattingProvider", groups = {"desktop"})
    public void testCustomMetricFormatting(Formatter format, String expectedValue, boolean compareFormat) throws ParseException, JSONException, IOException {
        String customFormatMetricName = "Custom format metric";
        String customFormatMetricMaql = "SELECT 154271";
        Metric customFormatMetric = getMdService().createObj(getProject(), new Metric(customFormatMetricName, customFormatMetricMaql, format.toString()));

        setupKpi(new KpiConfiguration.Builder()
            .metric(customFormatMetricName)
            .dateDimension(DATE_CREATED)
            .build()
        );

        try {
            Kpi lastKpi = initIndigoDashboardsPage()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

            String screenshot = "testCustomMetricFormatting-" + format.name();
            takeScreenshot(browser, screenshot, getClass());

            String kpiValue = lastKpi.getValue();
            if (compareFormat) {
                assertTrue(format.toString().contains(kpiValue));
            } else {
                assertEquals(kpiValue, expectedValue);
            }

        } finally {
            teardownKpi();
            getMdService().removeObj(customFormatMetric);
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkXssInMetricName() {
        String xssHeadline = "<script>alert('Hi')</script>";
        String xssMetricName = "<button>" + PERCENT_OF_GOAL + "</button>";
        String xssMetricMaql = "SELECT 1";
        Metric xssMetric = getMdService().createObj(getProject(), new Metric(xssMetricName, xssMetricMaql, "#,##0.00"));

        setupKpi(
            new KpiConfiguration.Builder()
                .metric(xssMetricName)
                .dateDimension(DATE_CREATED)
                .build()
        );

        try {
            Kpi lastKpi = initIndigoDashboardsPage()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

            assertEquals(lastKpi.getHeadline(), xssMetricName);

            Kpi selectedKpi = indigoDashboardsPage
                .switchToEditMode()
                .selectLastKpi();

            selectedKpi.setHeadline(xssHeadline);
            assertEquals(selectedKpi.getHeadline(), xssHeadline);

        } finally {
            teardownKpi();
            getMdService().removeObj(xssMetric);
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkXssInMetricFormat() throws ParseException, JSONException, IOException {
        String xssFormatMetricName = "<button>" + PERCENT_OF_GOAL + "</button>";
        String xssFormatMetricMaql = "SELECT 1";
        Metric xssFormatMetric = getMdService().createObj(getProject(), new Metric(xssFormatMetricName, xssFormatMetricMaql, "<button>#,##0.00</button>"));

        setupKpi(new KpiConfiguration.Builder()
            .metric(xssFormatMetricName)
            .dateDimension(DATE_CREATED)
            .build()
        );

        try {
            Kpi lastKpi = initIndigoDashboardsPage()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

            assertEquals(lastKpi.getValue(), "<button>1.00</button>");

        } finally {
            teardownKpi();
            getMdService().removeObj(xssFormatMetric);
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiStateWithNoDataMetric() {
        String invalidMetricName = "No data metric";
        String invalidMetricMaql = "SELECT 1 where 2 = 3";
        Metric invalidMetric = getMdService().createObj(getProject(), new Metric(invalidMetricName, invalidMetricMaql, "#,##0.00"));

        try {
            setupKpi(new KpiConfiguration.Builder()
                .metric(invalidMetricName)
                .dateDimension(DATE_CREATED)
                .build()
            );

            Kpi lastKpi = initIndigoDashboardsPage()
                .waitForAllKpiWidgetContentLoaded()
                .getLastKpi();

            takeScreenshot(browser, "checkKpiStateWithNoDataMetric", getClass());
            assertTrue(lastKpi.isEmptyValue());

        } finally {
            teardownKpi();
            getMdService().removeObj(invalidMetric);
        }

    }

}
