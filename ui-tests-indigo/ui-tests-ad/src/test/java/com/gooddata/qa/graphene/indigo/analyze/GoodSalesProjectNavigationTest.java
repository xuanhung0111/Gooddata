package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

import java.io.IOException;
import java.util.UUID;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesProjectNavigationTest extends AbstractAnalyseTest {

    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 10);
    private static final String NEW_PROJECT_NAME = "New-project-navigation-" + UNIQUE_ID;
    private static final String ANALYZE_PAGE_URL = "analyze";

    private String embeddedDashboardUser;

    private String currentProjectId;
    private String newProjectId;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Project-navigation-" + UNIQUE_ID;
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void getMoreProject() {
        currentProjectId = testParams.getProjectId();
        newProjectId = createProjectUsingFixture(NEW_PROJECT_NAME, ResourceTemplate.GOODSALES);
        new Metrics(new RestClient(getProfile(ADMIN)), newProjectId).createAmountMetric();
    }

    @DataProvider(name = "userRoleProvider")
    public Object[][] userRoleProvider() {
        return new Object[][] {
            {embeddedDashboardUser, testParams.getPassword(), UserRoles.DASHBOARD_ONLY},
            {testParams.getViewerUser(), testParams.getPassword(), UserRoles.VIEWER}
        };
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "userRoleProvider", groups = {"switchProject"})
    public void switchProjectWithOtherUserRoles(String user, String password, UserRoles role)
            throws ParseException, IOException, JSONException {
        final RestClient restClient = new RestClient(new RestProfile(testParams.getHost(), user, password, true));

        logout();
        signInAtGreyPages(user, password);

        String newProjectId = createNewEmptyProject(restClient, NEW_PROJECT_NAME);
        final ProjectRestRequest newProjectRestRequest = new ProjectRestRequest(restClient, newProjectId);

        newProjectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ANALYTICAL_DESIGNER, true);

        testParams.setProjectId(newProjectId);
        try {
            initAnalysePage();
            analysisPage.switchProject(projectTitle);

            if (role == UserRoles.DASHBOARD_ONLY) {
                // With Embeded Dashboard role, user cannot access to Analyze
                // page of project and automatically directed to Projects.html page
                waitForProjectsPageLoaded(browser);
            } else {
                waitForDashboardPageLoaded(browser);
            }

            takeScreenshot(browser, "Switch-to-Analyse-Page-of-project-" + projectTitle +
                    "-with-user-role-" + role.toString(), getClass());

            if (role == UserRoles.DASHBOARD_ONLY) {
                assertThat(browser.getCurrentUrl(), containsString("cannotAccessWorkbench"));
            } else {
                assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
            }

        } finally {
            testParams.setProjectId(currentProjectId);

            logout();
            signIn(false, UserRoles.ADMIN);

            newProjectRestRequest.deleteProject();
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"switchProject"})
    public void switchProjectWithFeatureFlagDisabled() {
         new ProjectRestRequest(new RestClient(getProfile(ADMIN)), newProjectId).setFeatureFlagInProject(
                 ProjectFeatureFlags.ANALYTICAL_DESIGNER, false);

        initAnalysePage();

        analysisPage.switchProject(NEW_PROJECT_NAME);
        waitForDashboardPageLoaded(browser);

        takeScreenshot(browser, "User-cannot-access-to-Analyse-Page-when-Feature-Flag-disabled", getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"switchProject"})
    public void switchProject() {
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), newProjectId).setFeatureFlagInProject(
                ProjectFeatureFlags.ANALYTICAL_DESIGNER, true);

        initAnalysePage();

        analysisPage.switchProject(NEW_PROJECT_NAME);
        waitForFragmentVisible(analysisPage);

        takeScreenshot(browser, "Switch-to-project-" + NEW_PROJECT_NAME, getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        analysisPage.switchProject(projectTitle);
        waitForFragmentVisible(analysisPage);

        takeScreenshot(browser, "Switch-to-project-" + projectTitle, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"switchProject"})
    public void checkLastVisitedProject() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), newProjectId).setFeatureFlagInProject(
                ProjectFeatureFlags.ANALYTICAL_DESIGNER, true);

        initAnalysePage();

        analysisPage.switchProject(NEW_PROJECT_NAME);
        waitForFragmentVisible(analysisPage);

        takeScreenshot(browser, "Switch-to-project-" + NEW_PROJECT_NAME, getClass());
        assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        testParams.setProjectId(newProjectId);
        try {
            initDashboardsPage();
            openUrl(ANALYZE_PAGE_URL);
            waitForFragmentVisible(analysisPage);

            takeScreenshot(browser,
                    "User-is-directed-to-Analyze-Page-of-last-visited-project-" + NEW_PROJECT_NAME, getClass());
            assertThat(browser.getCurrentUrl(), containsString(newProjectId));

        } finally {
            testParams.setProjectId(currentProjectId);
        }

        initAnalysePage();
        initProjectsPage();

        openUrl(ANALYZE_PAGE_URL);
        waitForFragmentVisible(analysisPage);

        takeScreenshot(browser,
                "User-is-directed-to-Analyze-Page-of-last-visited-project-" + projectTitle, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));

        logout();
        signIn(false, UserRoles.ADMIN);

        takeScreenshot(browser, "Last-visited-project-is-updated-with-project-" + projectTitle, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Test(dependsOnGroups = {"switchProject"}, groups = {"analyse"})
    public void openAnalysePageAfterDeleteAnotherProject() throws ParseException, JSONException, IOException {
        try {
            testParams.setProjectId(newProjectId);

            initDashboardsPage();
            assertThat(browser.getCurrentUrl(), containsString(newProjectId));

            createReport(new UiReportDefinition()
                    .withName("Report")
                    .withWhats(METRIC_AMOUNT)
                    .withHows(ATTR_STAGE_NAME),
                    "Create Report");

            initProjectsAndUsersPage()
                .deteleProject();
            waitForProjectsPageLoaded(browser);

        } finally {
            testParams.setProjectId(currentProjectId);
        }

        initAnalysePage();
        takeScreenshot(browser, "Re-open-Analyse-page-of-project-:"
                + currentProjectId + "after-delete-project-" + newProjectId, getClass());
        assertThat(browser.getCurrentUrl(), containsString(currentProjectId));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
        embeddedDashboardUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
    }
}
