package com.gooddata.qa.graphene.i18n;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;

public class LocalizationTest extends GoodSalesAbstractTest {

    @Override
    protected void customizeProject() throws Throwable {
        initLocalizationPage().selectLanguge(testParams.getLanguageCode());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"entry-point"})
    public void verifyLoginPage() {
        logout();
        checkLocalization(browser);
    }

    @Test(dependsOnMethods = {"verifyLoginPage"}, groups = {"entry-point"})
    public void verifyRegistrationPage() {
        openUrl(PAGE_LOGIN);
        LoginFragment.getInstance(browser).registerNewAccount();
        checkLocalization(browser);
    }

    @Test(dependsOnMethods = {"verifyLoginPage"}, groups = {"entry-point"})
    public void verifyResetPasswordPage() {
        openUrl(PAGE_LOGIN);
        LoginFragment.getInstance(browser).openLostPasswordPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"entry-point"}, groups = {"configure"}, alwaysRun = true)
    public void login() throws JSONException {
        openUrl(PAGE_LOGIN);
        signIn(false, UserRoles.ADMIN);
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
