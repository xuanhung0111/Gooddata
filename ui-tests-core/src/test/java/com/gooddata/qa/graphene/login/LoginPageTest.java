package com.gooddata.qa.graphene.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.utils.CheckUtils;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

public class LoginPageTest extends AbstractUITest {
    
    @Test
    public void initLoginPage() {
        openUrl(PAGE_LOGIN);
        LoginFragment.waitForPageLoaded(browser);
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void gd_Login_001_LoginPanel() {
        assertTrue(LoginFragment.getInstance(browser).allLoginElementsAvailable(),
                "Login panel with valid elements is available");
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void gd_Login_002_SignInAndSignOut() throws JSONException {
        signIn(false, UserRoles.ADMIN);
        logout();
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void gd_Login_003_SignInWithEmptyPassword() {
        LoginFragment.getInstance(browser).login(testParams.getUser(), "", false);
        LoginFragment.getInstance(browser).checkPasswordInvalid();
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void gd_Login_004_SignInWithInvalidPassword() {
        LoginFragment.getInstance(browser).login(testParams.getUser(), "abcdefgh", false);
        LoginFragment.getInstance(browser).checkInvalidLogin();
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void gd_Login_005_SignInWithInvalidEmail() {
        LoginFragment.getInstance(browser).login("email_invalid_format", "abcdefgh", false);
        LoginFragment.getInstance(browser).checkEmailInvalid();
    }

    @Test(dependsOnMethods = {"initLoginPage"})
    public void reopenLoginPageAfterSignIn() throws JSONException {
        try {
            signIn(false, UserRoles.ADMIN);

            openUrl(PAGE_LOGIN);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        } finally {
            logout();
        }
    }

    @Test(dependsOnMethods = {"reopenLoginPageAfterSignIn"}, dataProvider = "getLastUrlXSS")
    public void loginWithXSSShouldNotWork(String lastUrl) throws JSONException {
        try {
            signIn(true, UserRoles.ADMIN);
            openUrl(ACCOUNT_PAGE + lastUrl);
            assertFalse(CheckUtils.isAlertDisplayed(),
                "Javascript from the input parameters should be not executed");
        } finally {
            logout();
        }
    }

    @Test(dependsOnMethods = {"reopenLoginPageAfterSignIn"}, dataProvider = "getLastUrlXSS")
    public void openLoginPageWithXSSShouldNotWork(String lastUrl) throws JSONException {
        openUrl(ACCOUNT_PAGE + lastUrl);
        assertFalse(CheckUtils.isAlertDisplayed(),
            "Javascript from the input parameters should be not executed");
    }

    @DataProvider
    public Object[][] getLastUrlXSS() {
        return new Object[][]{
            {"?lastUrl=javascript:alert(document.location)"},
            {"?lastUrl=vbscript:alert(document.location)"},
            {"?lastUrl=data:alert(document.location)"},
            {"?lastUrl=vbscript:alert<script>alert(document.cookie)</script>"},
            {"?lastUrl=data:text/html,<script>alert(document.domain)</script>"},
            {"?lastUrl=javascript:attackers_script_here"},
            {"?lastUrl=javascript:alert(1)"},
        };
    }
}
