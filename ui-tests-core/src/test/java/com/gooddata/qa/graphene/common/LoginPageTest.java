package com.gooddata.qa.graphene.common;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.UserRoles;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;


@Test(groups = {"login"}, description = "Tests for basic login functionality in GD platform")
public class LoginPageTest extends AbstractUITest {

    @BeforeClass
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

    @Test(groups = {"loginInit"})
    public void gd_Login_001_LoginPanel() {
        waitForElementVisible(loginFragment.getRoot());
        Assert.assertTrue(loginFragment.allLoginElementsAvailable(), "Login panel with valid elements is available");
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_002_SignInAndSignOut() throws InterruptedException, JSONException {
        waitForElementVisible(loginFragment.getRoot());
        signIn(false, UserRoles.ADMIN);
        logout();
        waitForElementVisible(loginFragment.getRoot());
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_003_SignInWithEmptyPassword() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(testParams.getUser(), "", false);
        loginFragment.checkPasswordInvalid();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_004_SignInWithInvalidPassword() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(testParams.getUser(), "abcdefgh", false);
        loginFragment.checkInvalidLogin();
    }

    @Test(dependsOnGroups = {"loginInit"})
    public void gd_Login_005_SignInWithInvalidEmail() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login("email_invalid_format", "abcdefgh", false);
        loginFragment.checkEmailInvalid();
    }
}
