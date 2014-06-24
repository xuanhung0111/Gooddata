package com.gooddata.qa.graphene.common;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;


@Test(groups = {"login"}, description = "Tests for basic login functionality in GD platform")
public class LoginPageTest extends AbstractTest {

    @BeforeClass
    public void initStartPage() {
        startPage = uiUtils.PAGE_LOGIN;
    }

    @Test(groups = {"loginInit"})
    public void gd_Login_001_LoginPanel() {
        waitForElementVisible(uiUtils.loginFragment.getRoot());
        Assert.assertTrue(uiUtils.loginFragment.allLoginElementsAvailable(), "Login panel with valid elements is available");
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_002_SignInAndSignOut() throws InterruptedException {
        waitForElementVisible(uiUtils.loginFragment.getRoot());
        uiUtils.loginFragment.login(testParams.getUser(), testParams.getPassword(), true);
        waitForElementVisible(uiUtils.BY_LOGGED_USER_BUTTON);
        Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
        uiUtils.logout();
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_003_SignInWithEmptyPassword() {
        waitForElementVisible(uiUtils.loginFragment.getRoot());
        uiUtils.loginFragment.login(testParams.getUser(), "", false);
        uiUtils.loginFragment.checkPasswordInvalid();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_004_SignInWithInvalidPassword() {
        waitForElementVisible(uiUtils.loginFragment.getRoot());
        uiUtils.loginFragment.login(testParams.getUser(), "abcdefgh", false);
        uiUtils.loginFragment.checkInvalidLogin();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_005_SignInWithInvalidEmail() {
        waitForElementVisible(uiUtils.loginFragment.getRoot());
        uiUtils.loginFragment.login("email_invalid_format", "abcdefgh", false);
        uiUtils.loginFragment.checkEmailInvalid();
    }
}
