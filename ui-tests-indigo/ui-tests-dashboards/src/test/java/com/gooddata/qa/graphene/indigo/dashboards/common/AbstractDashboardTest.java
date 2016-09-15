package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.addWidgetToAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;

public abstract class AbstractDashboardTest extends AbstractProjectTest {

    protected static final String DATE_FILTER_ALL_TIME = "All time";
    protected static final String DATE_FILTER_THIS_MONTH = "This month";
    protected static final String DATE_FILTER_LAST_MONTH = "Last month";
    protected static final String DATE_FILTER_THIS_QUARTER = "This quarter";
    protected static final String DATE_FILTER_LAST_QUARTER = "Last quarter";
    protected static final String DATE_FILTER_THIS_YEAR = "This year";
    protected static final String DATE_FILTER_LAST_YEAR = "Last year";

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    protected IndigoDashboardsPage indigoDashboardsPage;

    @BeforeClass(alwaysRun = true)
    protected void turnOffProjectValidation() {
        validateAfterClass = false;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"dashboardsInit"})
    protected void setStartPageContext() {
        startPageContext = null;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"dashboardsInit"})
    @Parameters({"windowSize"})
    protected void initDashboardTests(@Optional("maximize") String windowSize) throws Throwable {
        adjustWindowSize(windowSize);
        setDashboardFeatureFlags();
        prepareSetupProject();
    }

    protected void prepareSetupProject() throws Throwable {
        // do nothing, it should be implemented in a specific test
    }

    protected String createKpiUsingRest(final KpiMDConfiguration kpiConfig)
            throws JSONException, IOException {
        return IndigoRestUtils.createKpiWidget(getRestApiClient(), testParams.getProjectId(), kpiConfig);
    }

    protected IndigoDashboardsPage startEditMode() {
        if (!initIndigoDashboardsPage().isSplashScreenPresent())
            return indigoDashboardsPage.switchToEditMode();

        return indigoDashboardsPage.getSplashScreen().startEditingWidgets();
    }

    protected String addWidgetToWorkingDashboard(final String widgetUri) throws JSONException, IOException {
        final RestApiClient client = getRestApiClient();
        final String projectId = testParams.getProjectId();

        if (getAnalyticalDashboards(client, projectId).isEmpty())
            createAnalyticalDashboard(client, projectId, singletonList(widgetUri));
        else
            addWidgetToAnalyticalDashboard(client, projectId, getWorkingDashboardUri(), widgetUri);

        // need widget uri in most of cases which use this helper method
        return widgetUri;
    }

    protected void setAlertForLastKpi(String triggeredWhen, String threshold) {
        waitForFragmentVisible(indigoDashboardsPage)
            .getLastWidget(Kpi.class)
            .openAlertDialog()
            .selectTriggeredWhen(triggeredWhen)
            .setThreshold(threshold)
            .setAlert();
    }

    protected void deleteAlertForLastKpi() {
        waitForFragmentVisible(indigoDashboardsPage)
            .getLastWidget(Kpi.class)
            .openAlertDialog()
            .deleteAlert();
    }

    protected void setDashboardFeatureFlags() {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS_VISUALIZATIONS, true);
    }

    protected String getWorkingDashboardUri() throws JSONException, IOException {
        // if having more than 1 dashboard, the first one will be working project by default
        return getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0);
    }

    protected String convertToUniqueHeadline(final String headline) {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private void setWindowSize(final int width, final int height) {
        browser.manage().window().setSize(new Dimension(width, height));
    }

    private void maximizeWindow() {
        browser.manage().window().maximize();
    }

    private void adjustWindowSize(final String windowSize) {
        String executionEnv = System.getProperty("test.execution.env");
        if ("maximize".equals(windowSize)) {
            maximizeWindow();
        } else if (executionEnv != null && executionEnv.contains("browserstack-mobile")) {
            System.out.println(
                    "Window size is ignored for execution on mobile devices at Browserstack.");
        } else {
            String[] dimensions = windowSize.split(",");
            if (dimensions.length == 2) {
                try {
                    setWindowSize(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]));
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid window size given: " + windowSize
                            + " (fallback to maximize)");
                    maximizeWindow();
                }
            } else {
                System.out.println("ERROR: Invalid window size given: " + windowSize
                        + " (fallback to maximize)");
                maximizeWindow();
            }
        }
    }
}
