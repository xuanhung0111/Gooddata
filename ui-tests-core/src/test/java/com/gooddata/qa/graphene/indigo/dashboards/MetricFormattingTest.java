package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import com.gooddata.qa.utils.http.RestUtils;
import java.io.IOException;
import static java.lang.String.format;
import org.apache.http.ParseException;
import org.json.JSONException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MetricFormattingTest extends DashboardWithWidgetsTest {
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

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, dataProvider = "formattingProvider")
    public void testCustomMetricFormatting(Formatter format, String expectedValue, boolean compareFormat) throws ParseException, JSONException, IOException {
        String screenshot = "testCustomMetricFormatting-" + format.name();
        String uri = format(NUMBER_OF_ACTIVITIES_URI, testParams.getProjectId());
        initMetricPage();
        waitForFragmentVisible(metricEditorPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        try {
            RestUtils.changeMetricFormat(getRestApiClient(), uri, format.toString());
            initIndigoDashboardsPage();
            takeScreenshot(browser, screenshot, getClass());

            String kpiValue = indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES);
            if (compareFormat) {
                assertTrue(format.toString().contains(kpiValue));
            } else {
                assertEquals(kpiValue, expectedValue);
            }
        } finally {
            initMetricPage();
            waitForFragmentVisible(metricEditorPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
            waitForFragmentVisible(metricDetailPage).changeMetricFormat(Formatter.DEFAULT);
            assertEquals(metricDetailPage.getMetricFormat(), Formatter.DEFAULT.toString());
        }
    }
}
