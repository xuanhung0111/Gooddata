package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class LocalizationTest extends AbstractUITest {

    @BeforeClass(alwaysRun = true)
    public void initStartPage() {
        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                // no need to wait projects page because it will be redirected to login page
            }

            @Override
            public String getStartPage() {
                return PAGE_PROJECTS;
            }
        };
    }

    @Test(groups = {"precondition"})
    public void changeLanguage() {
        initLocalizationPage().selectLanguge(testParams.getLanguageCode());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"entry-point"})
    public void verifyLoginPage() {
        waitForFragmentVisible(loginFragment);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"entry-point"})
    public void verifyRegistrationPage() {
        waitForFragmentVisible(loginFragment).openRegistrationPage();
        waitForFragmentVisible(registrationPage);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"entry-point"})
    public void verifyResetPasswordPage() {
        waitForFragmentVisible(loginFragment).openLostPasswordPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"entry-point"}, alwaysRun = true)
    public void login() throws JSONException {
        waitForFragmentVisible(loginFragment);
        signIn(false, UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"login"}, groups = {"configure-goodsales"})
    public void createGoodSalesProject() {
        testParams.setProjectId(ProjectRestUtils.createProject(getGoodDataClient(), "GoodSales-Localization-test",
                "/projectTemplates/GoodSalesDemo/2", testParams.getAuthorizationToken(), ProjectDriver.POSTGRES,
                testParams.getProjectEnvironment()));
    }

    @Test(dependsOnMethods = {"login"}, groups = {"configure-goodsales"})
    public void configureStartPage() {
        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                WaitUtils.waitForProjectsPageLoaded(browser);
            }

            @Override
            public String getStartPage() {
                return PAGE_PROJECTS;
            }
        };
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyDashboards() {
        initDashboardsPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyReportsPage() {
        initReportsPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyDatasetPage() {
        initManagePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyMetricPage() {
        initMetricPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyAttributePage() {
        initAttributePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyVariablePage() {
        initVariablePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyFactPage() {
        initFactPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyProjectAndUsersPage() {
        initProjectsAndUsersPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure-goodsales"})
    public void verifyEmailSchedulesPage() {
        initEmailSchedulesPage();
        checkLocalization(browser);
    }

    @AfterClass(alwaysRun = true)
    public void deleteProject() {
        String projectId = testParams.getProjectId();

        if (projectId != null && !projectId.isEmpty())
            ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
    }
}
