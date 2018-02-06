package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.utils.WaitUtils;

public class LocalizationTest extends GoodSalesAbstractTest {

    @Override
    protected void customizeProject() throws Throwable {
        initLocalizationPage().selectLanguge(testParams.getLanguageCode());
    }

    @Override
    public void assignStartPage() {
        logout();

        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                LoginFragment.waitForPageLoaded(browser);
            }

            @Override
            public String getStartPage() {
                return PAGE_LOGIN;
            }
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"entry-point"})
    public void verifyLoginPage() {
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"entry-point"})
    public void verifyRegistrationPage() {
        LoginFragment.getInstance(browser).startAFreeTrial();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"entry-point"})
    public void verifyResetPasswordPage() {
        LoginFragment.getInstance(browser).openLostPasswordPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"entry-point"}, groups = {"configure"}, alwaysRun = true)
    public void login() throws JSONException {
        signIn(false, UserRoles.ADMIN);

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

    @Test(dependsOnGroups = {"configure"})
    public void verifyDashboards() {
        initDashboardsPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyReportsPage() {
        initReportsPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyDatasetPage() {
        initManagePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyMetricPage() {
        initMetricPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyAttributePage() {
        initAttributePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyVariablePage() {
        initVariablePage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyFactPage() {
        initFactPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyProjectAndUsersPage() {
        initProjectsAndUsersPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"configure"})
    public void verifyEmailSchedulesPage() {
        initEmailSchedulesPage();
        checkLocalization(browser);
    }

    @AfterClass(alwaysRun = true)
    public void switchBackToOriginalLanguage() throws JSONException {
        log.info("switch back to original language");
        initLocalizationPage().selectLanguge("en-US");
    }
}
