package com.gooddata.qa.graphene.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
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

            waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        } finally {
            logout();
        }
    }
}
