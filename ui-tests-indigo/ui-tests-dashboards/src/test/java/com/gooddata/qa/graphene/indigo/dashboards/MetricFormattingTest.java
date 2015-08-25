package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.utils.http.RestUtils;

public class MetricFormattingTest extends DashboardWithWidgetsTest {

    private static final String PERCENT_OF_GOAL = "% of Goal";
    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";
    private static final String NUMBER_OF_ACTIVITIES_URI = "/gdc/md/%s/obj/14636";

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
        String screenshot = "testCustomMetricFormatting-" + format.name();
        String uri = format(NUMBER_OF_ACTIVITIES_URI, testParams.getProjectId());
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        try {
            RestUtils.changeMetricFormat(getRestApiClient(), uri, format.toString());
            initIndigoDashboardsPage()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME);
            takeScreenshot(browser, screenshot, getClass());

            String kpiValue = indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES);
            if (compareFormat) {
                assertTrue(format.toString().contains(kpiValue));
            } else {
                assertEquals(kpiValue, expectedValue);
            }
        } finally {
            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
            waitForFragmentVisible(metricDetailPage).changeMetricFormat(Formatter.DEFAULT);
            assertEquals(metricDetailPage.getMetricFormat(), Formatter.DEFAULT.toString());
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkXssInMetricName() {
        String xssMetric = "<button>" + PERCENT_OF_GOAL + "</button>";
        String xssHeadline = "<script>alert('Hi')</script>";
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        waitForFragmentVisible(metricDetailPage).renameMetric(xssMetric);

        try {
            Kpi selectedKpi = initIndigoDashboardsPage()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .switchToEditMode()
                .addWidget(xssMetric, DATE_CREATED)
                .selectLastKpi();

            indigoDashboardsPage
                .getConfigurationPanel()
                .selectMetricByName(xssMetric);

            assertEquals(selectedKpi.getHeadline(), xssMetric);

            selectedKpi.setHeadline(xssHeadline);
            assertEquals(selectedKpi.getHeadline(), xssHeadline);

        } finally {
            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(xssMetric);
            waitForFragmentVisible(metricDetailPage).renameMetric(PERCENT_OF_GOAL);
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkXssInMetricFormat() throws ParseException, JSONException, IOException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        RestUtils.changeMetricFormat(getRestApiClient(), uri, "<button>#,##0.00</button>");

        try {
            Kpi selectedKpi = initIndigoDashboardsPage()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .switchToEditMode()
                .selectKpi(0);

            indigoDashboardsPage
                .getConfigurationPanel()
                .selectMetricByName(PERCENT_OF_GOAL);

            // Check that loading happened
            indigoDashboardsPage
                .waitForAnyKpiWidgetContentLoading()
                .waitForAllKpiWidgetContentLoaded();
            assertEquals(selectedKpi.getValue(), "<button>11.61</button>");
        } finally {
            RestUtils.changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }
}
