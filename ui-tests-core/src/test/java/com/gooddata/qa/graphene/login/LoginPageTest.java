package com.gooddata.qa.graphene.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;

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

    @Test
    public void gd_Login_001_LoginPanel() {
        waitForElementVisible(loginFragment.getRoot());
        Assert.assertTrue(loginFragment.allLoginElementsAvailable(), "Login panel with valid elements is available");
    }

    @Test
    public void gd_Login_002_SignInAndSignOut() throws JSONException {
        waitForElementVisible(loginFragment.getRoot());
        signIn(false, UserRoles.ADMIN);
        logout();
        waitForElementVisible(loginFragment.getRoot());
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test
    public void gd_Login_003_SignInWithEmptyPassword() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(testParams.getUser(), "", false);
        loginFragment.checkPasswordInvalid();
    }

    @Test
    public void gd_Login_004_SignInWithInvalidPassword() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(testParams.getUser(), "abcdefgh", false);
        loginFragment.checkInvalidLogin();
    }

    @Test
    public void gd_Login_005_SignInWithInvalidEmail() {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login("email_invalid_format", "abcdefgh", false);
        loginFragment.checkEmailInvalid();
    }

    @Test
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
