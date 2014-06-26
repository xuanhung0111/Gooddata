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
        startPage = ui.PAGE_LOGIN;
    }

    @Test(groups = {"loginInit"})
    public void gd_Login_001_LoginPanel() {
        waitForElementVisible(ui.loginFragment.getRoot());
        Assert.assertTrue(ui.loginFragment.allLoginElementsAvailable(), "Login panel with valid elements is available");
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_002_SignInAndSignOut() throws InterruptedException {
        waitForElementVisible(ui.loginFragment.getRoot());
        ui.loginFragment.login(testParams.getUser(), testParams.getPassword(), true);
        waitForElementVisible(ui.BY_LOGGED_USER_BUTTON);
        Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
        ui.logout();
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_003_SignInWithEmptyPassword() {
        waitForElementVisible(ui.loginFragment.getRoot());
        ui.loginFragment.login(testParams.getUser(), "", false);
        ui.loginFragment.checkPasswordInvalid();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_004_SignInWithInvalidPassword() {
        waitForElementVisible(ui.loginFragment.getRoot());
        ui.loginFragment.login(testParams.getUser(), "abcdefgh", false);
        ui.loginFragment.checkInvalidLogin();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_005_SignInWithInvalidEmail() {
        waitForElementVisible(ui.loginFragment.getRoot());
        ui.loginFragment.login("email_invalid_format", "abcdefgh", false);
        ui.loginFragment.checkEmailInvalid();
    }
}
