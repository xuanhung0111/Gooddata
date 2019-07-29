package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.testng.Assert.assertEquals;

public class DashboardPermissionsBasicTest extends GoodSalesAbstractTest {
    private final String REPORT_TEST = "Report Test";
    private static final String DASHBOARD_TEXT = "DASHBOARD TEXT";
    private static final String WARNING_MESSAGE = "You do not have permission to access the dashboard." +
            " Please contact your administrator to grant you access.";

    private DashboardRestRequest dashboardRequest;

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard Permissions Basic Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId());
        new ProjectRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DASHBOARD_ACCESS_CONTROL, true);
    }

    @DataProvider(name = "editorRoles")
    public Object[][] getEditorRoles() {
        return new Object[][]{
                {UserRoles.EDITOR},
                {UserRoles.EDITOR_AND_INVITATIONS},
                {UserRoles.EDITOR_AND_USER_ADMIN}
        };
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
        createAndAddUserToProject(UserRoles.EDITOR_AND_INVITATIONS);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
    protected void prepareData() {
        createReport(REPORT_TEST);
    }

    /**
     * senario 18
     * https://docs.google.com/document/d/1FtJ-Q2Q-SfbUh5Cp7dD75tVVKdgDEbPeBqdkgj9Rfh8
     */
    @Test(dependsOnMethods = {"prepareData"}, groups = {"admin-tests"}, dataProvider = "editorRoles")
    public void shouldShowPrivateDashboardWhenDrillingDashboardHasNotSACByEditorRoles(UserRoles userRoles) throws Exception {
        String PRIVATE_DASHBOARD = "Private Dashboard Has Not SAC " + generateHashString();
        String TARGET_TAB = "Target Tab" + generateHashString();
        String SOURCE_TAB = "Source Tab" + generateHashString();
        preparePrivateDashboard(PRIVATE_DASHBOARD);
        dashboardsPage.hasNotSAC();
        initDashboardsPage().selectDashboard(PRIVATE_DASHBOARD).renameTab(0, TARGET_TAB);
        dashboardsPage.editDashboard().addTextToDashboard(TextObject.HEADLINE, DASHBOARD_TEXT, DASHBOARD_TEXT).saveDashboard();
        Screenshots.takeScreenshot(browser, PRIVATE_DASHBOARD, DashboardPermissionsBasicTest.class);

        String PUBLIC_DASHBOARD = "Public Dashboard Has Not SAC " + generateHashString();
        String publicDashboardUri = preparePublicDashboard(PUBLIC_DASHBOARD);
        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).renameTab(0, SOURCE_TAB).addReportToDashboard(REPORT_TEST);
        ChartReport chartReport = dashboardsPage.getContent().getReport(REPORT_TEST, ChartReport.class);
        chartReport.addDrilling(Pair.of(Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE), TARGET_TAB), "Dashboards");
        dashboardsPage.saveDashboard();
        Screenshots.takeScreenshot(browser, PUBLIC_DASHBOARD, DashboardPermissionsBasicTest.class);
        try {
            log.info("Logout and Login with user : " + userRoles);
            signInAndAccessDashboardWithUserRoles(publicDashboardUri, userRoles);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            String embeddedUri = embedDashboardDialog.getPreviewURI();

            EmbeddedDashboard embeddedDashboard = dashboardsPage.openEmbedDashboard(embeddedUri);
            embeddedDashboard.waitForReportLoaded(browser);

            embeddedDashboard.clickOnAttributeItem("In Person Meeting");
            embeddedDashboard.waitForReportLoaded(browser);
            String headline = embeddedDashboard.getHeadLine();
            assertEquals(headline, DASHBOARD_TEXT);

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(publicDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"prepareData"}, groups = {"admin-tests"}, dataProvider = "editorRoles")
    public void shouldShowRedMessageWhenDrillingDashboardHasSACByEditorRoles(UserRoles userRoles) throws Exception {
        String PRIVATE_DASHBOARD = "Private Dashboard Has SAC " + generateHashString();
        String TARGET_TAB = "Target Tab" + generateHashString();
        String SOURCE_TAB = "Source Tab" + generateHashString();
        preparePrivateDashboard(PRIVATE_DASHBOARD);
        dashboardsPage.hasSAC();
        initDashboardsPage().selectDashboard(PRIVATE_DASHBOARD).renameTab(0, TARGET_TAB).saveDashboard();
        Screenshots.takeScreenshot(browser, PRIVATE_DASHBOARD, DashboardPermissionsBasicTest.class);

        String PUBLIC_DASHBOARD = "Public Dashboard Has SAC " + generateHashString();
        String publicDashboardUri = preparePublicDashboard(PUBLIC_DASHBOARD);
        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).renameTab(0, SOURCE_TAB).addReportToDashboard(REPORT_TEST);
        ChartReport chartReport = dashboardsPage.getContent().getReport(REPORT_TEST, ChartReport.class);
        chartReport.addDrilling(Pair.of(Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE), TARGET_TAB), "Dashboards");
        dashboardsPage.saveDashboard();
        Screenshots.takeScreenshot(browser, PUBLIC_DASHBOARD, DashboardPermissionsBasicTest.class);
        try {
            log.info("Logout and Login with user : " + userRoles);
            signInAndAccessDashboardWithUserRoles(publicDashboardUri, userRoles);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            String embeddedUri = embedDashboardDialog.getPreviewURI();

            EmbeddedDashboard embeddedDashboard = dashboardsPage.openEmbedDashboard(embeddedUri);
            embeddedDashboard.waitForReportLoaded(browser);

            embeddedDashboard.clickOnAttributeItem("In Person Meeting");
            embeddedDashboard.waitForReportLoaded(browser);

            String warningMessage = dashboardsPage.getStatusMessage();
            assertEquals(warningMessage, WARNING_MESSAGE);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(publicDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"prepareData"}, groups = {"admin-tests"}, dataProvider = "editorRoles")
    public void shouldShowPrivateDashboardWhenDrillingDashboardAddUserShareByEditorRoles(UserRoles userRoles) throws Exception {
        String PRIVATE_DASHBOARD = "Private Dashboard Add Users Access " + generateHashString();
        String TARGET_TAB = "Target Tab" + generateHashString();
        String SOURCE_TAB = "Source Tab" + generateHashString();
        preparePrivateDashboard(PRIVATE_DASHBOARD);
        dashboardsPage.hasSAC();
        addUserShare(userRoles);
        initDashboardsPage().selectDashboard(PRIVATE_DASHBOARD).renameTab(0, TARGET_TAB);
        dashboardsPage.editDashboard().addTextToDashboard(TextObject.HEADLINE, DASHBOARD_TEXT, DASHBOARD_TEXT).saveDashboard();
        Screenshots.takeScreenshot(browser, PRIVATE_DASHBOARD, DashboardPermissionsBasicTest.class);

        String PUBLIC_DASHBOARD = "Public Dashboard Add Users Access " + generateHashString();
        String publicDashboardUri = preparePublicDashboard(PUBLIC_DASHBOARD);
        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).renameTab(0, SOURCE_TAB).addReportToDashboard(REPORT_TEST);
        ChartReport chartReport = dashboardsPage.getContent().getReport(REPORT_TEST, ChartReport.class);
        chartReport.addDrilling(Pair.of(Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE), TARGET_TAB), "Dashboards");
        dashboardsPage.saveDashboard();
        Screenshots.takeScreenshot(browser, PUBLIC_DASHBOARD, DashboardPermissionsBasicTest.class);
        try {
            log.info("Logout and Login with user : " + userRoles);
            signInAndAccessDashboardWithUserRoles(publicDashboardUri, userRoles);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            String embeddedUri = embedDashboardDialog.getPreviewURI();

            EmbeddedDashboard embeddedDashboard = dashboardsPage.openEmbedDashboard(embeddedUri);
            embeddedDashboard.waitForReportLoaded(browser);

            embeddedDashboard.clickOnAttributeItem("In Person Meeting");
            embeddedDashboard.waitForReportLoaded(browser);
            String headline = embeddedDashboard.getHeadLine();
            assertEquals(headline, DASHBOARD_TEXT);

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(publicDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"prepareData"}, groups = {"admin-tests"}, dataProvider = "editorRoles")
    public void shouldShowPrivateDashboardWhenDrillingDashboardNotAddUserShareByEditorRoles(UserRoles userRoles) throws Exception {
        String PRIVATE_DASHBOARD = "Private Dashboard Not Add Users Access " + generateHashString();
        String TARGET_TAB = "Target Tab" + generateHashString();
        String SOURCE_TAB = "Source Tab" + generateHashString();
        preparePrivateDashboard(PRIVATE_DASHBOARD);
        dashboardsPage.hasSAC();
        initDashboardsPage().selectDashboard(PRIVATE_DASHBOARD).renameTab(0, TARGET_TAB).saveDashboard();
        Screenshots.takeScreenshot(browser, PRIVATE_DASHBOARD, DashboardPermissionsBasicTest.class);

        String PUBLIC_DASHBOARD = "Public Dashboard Not Add Users Access " + generateHashString();
        String publicDashboardUri = preparePublicDashboard(PUBLIC_DASHBOARD);
        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).renameTab(0, SOURCE_TAB).addReportToDashboard(REPORT_TEST);
        ChartReport chartReport = dashboardsPage.getContent().getReport(REPORT_TEST, ChartReport.class);
        chartReport.addDrilling(Pair.of(Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE), TARGET_TAB), "Dashboards");
        dashboardsPage.saveDashboard();
        Screenshots.takeScreenshot(browser, PUBLIC_DASHBOARD, DashboardPermissionsBasicTest.class);
        try {
            log.info("Logout and Login with user : " + userRoles);
            signInAndAccessDashboardWithUserRoles(publicDashboardUri, userRoles);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            String embeddedUri = embedDashboardDialog.getPreviewURI();

            EmbeddedDashboard embeddedDashboard = dashboardsPage.openEmbedDashboard(embeddedUri);
            embeddedDashboard.waitForReportLoaded(browser);

            embeddedDashboard.clickOnAttributeItem("In Person Meeting");
            embeddedDashboard.waitForReportLoaded(browser);

            String warningMessage = dashboardsPage.getStatusMessage();
            assertEquals(warningMessage, WARNING_MESSAGE);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(publicDashboardUri);
        }
    }

    private String preparePrivateDashboard(String dashboardName) throws JSONException, IOException {
        String dashboardUri = createTestDashboard(dashboardName);
        dashboardRequest.setPrivateDashboard(dashboardName, true);
        return dashboardUri;
    }

    private void signInAndAccessDashboardWithUserRoles(String dashboardUri, UserRoles userRoles) {
        logoutAndLoginAs(true, userRoles);
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardUri);
    }

    private String preparePublicDashboard(String dashboardName) throws IOException {
        String dashboardUri = createTestDashboard(dashboardName);
        dashboardRequest.setPrivateDashboard(dashboardName, false);
        return dashboardUri;
    }

    private void createReport(String reportName) {
        initReportCreation().createReport(new UiReportDefinition().withName(reportName)
                .withWhats(METRIC_NUMBER_OF_ACTIVITIES).withHows(ATTR_ACTIVITY_TYPE));
    }

    private void addUserShare(UserRoles userRoles){
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        String user = getUser(userRoles);
        dashboardsPage.addUserGroup(permissionsDialog, addGranteesDialog, user);
    }
}
