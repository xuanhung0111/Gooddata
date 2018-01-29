package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascade;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestApiClient;

public class EditModeTest extends AbstractDashboardTest {

    private static final int MAXIMUM_TRIES = 2;

    private boolean isMobileRunning;

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        if (isMobileRunning) return;

        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        disableAnalyticalDesignerFeatureFlag();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    private void disableAnalyticalDesignerFeatureFlag() {
        //This test only works if insight flag is disabled
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ANALYTICAL_DESIGNER, false);
    }

    @Override
    public RestApiClient getRestApiClient() {
        // see https://jira.intgdc.com/browse/QA-6170
        return getRepeatableRestApiClient(MAXIMUM_TRIES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkEditButtonPresent() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonPresent", getClass());

        assertTrue(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkEditButtonMissing() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonMissing", getClass());

        assertFalse(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkViewerCannotEditDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            assertFalse(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkEditorCanEditDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            assertTrue(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkMessageIsNotShownWhenEditingKpiWithoutAlerts() throws JSONException {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectLastWidget(Kpi.class);

        waitForFragmentVisible(indigoDashboardsPage)
            .getConfigurationPanel()
            .waitForAlertEditWarningMissing();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkMessageIsShownWhenEditingKpiWithOwnAlert() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            waitForFragmentVisible(indigoDashboardsPage)
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            takeScreenshot(browser, "checkMessageIsShownWhenEditingKpiWithOwnAlert", getClass());
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkMessageIsShownWhenEditingKpiWithOthersAlerts() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        // add alert as different user
        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            takeScreenshot(browser, "checkMessageIsShownWhenEditingKpiWithOthersAlerts", getClass());
            waitForFragmentVisible(indigoDashboardsPage)
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkNumberOfAlertAreSet() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            takeScreenshot(browser, "Check alert message with number of users is shown correctly", getClass());
            assertThat(waitForFragmentVisible(indigoDashboardsPage)
                    .getConfigurationPanel()
                    .getKpiAlertMessage(), containsString("2 alerts"));

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertKeptAfterEditedByAnotherUser() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            waitForFragmentVisible(indigoDashboardsPage)
                    .getConfigurationPanel()
                    .selectMetricByName(METRIC_NUMBER_OF_ACTIVITIES);

            indigoDashboardsPage
                    .waitForWidgetsLoading()
                    .leaveEditMode();

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            Kpi kpi = initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            takeScreenshot(browser, "KPI Alert are kept after updating from another user", getClass());
            waitForFragmentVisible(indigoDashboardsPage)
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
            assertEquals(kpi.getHeadline(), METRIC_NUMBER_OF_ACTIVITIES);

        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkNoVisualizationsList() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();

        takeScreenshot(browser, "checkNoVisualizationsList", getClass());
        assertFalse(IndigoInsightSelectionPanel.isPresent(browser));
    }
}
