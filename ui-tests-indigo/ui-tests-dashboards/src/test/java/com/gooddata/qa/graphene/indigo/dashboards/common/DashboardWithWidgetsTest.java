package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;
import java.io.IOException;
import org.json.JSONException;
import org.testng.annotations.Test;

public abstract class DashboardWithWidgetsTest extends DashboardsGeneralTest {

    public static final String AMOUNT = "Amount";
    public static final String LOST = "Lost";
    public static final String NUMBER_OF_ACTIVITIES = "# of Activities";

    public static final String DATE_CREATED = "Date dimension (Created)";
    public static final String DATE_CLOSED = "Date dimension (Closed)";
    public static final String DATE_ACTIVITY = "Date dimension (Activity)";
    public static final String DATE_SNAPSHOT = "Date dimension (Snapshot)";

    public static final String DATE_FILTER_ALL_TIME = "All time";

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"dashboardWidgetsInit"})
    public void initDashboardWithWidgets() throws JSONException, IOException {
        IndigoRestUtils.prepareAnalyticalDashboardTemplate(getRestApiClient(), testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"dashboardWidgetsInit"})
    public void captureScreenshotForCurrentDashboardWithLoadedWidgets() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .waitForAllKpiWidgetContentLoaded();
        takeScreenshot(browser, "captureScreenshotForCurrentDashboardWithLoadedWidgets", getClass());
    }

}
