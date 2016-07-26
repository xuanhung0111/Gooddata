package com.gooddata.qa.graphene.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = {"login"}, description = "Tests for basic login functionality in GD platform")
public class LoginPageTest extends AbstractUITest {

    @BeforeClass
    public void initStartPage() {
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

    @Test
    public void gd_Login_001_LoginPanel() {
        assertTrue(LoginFragment.getInstance(browser).allLoginElementsAvailable(),
                "Login panel with valid elements is available");
    }

    @Test
    public void gd_Login_002_SignInAndSignOut() throws JSONException {
        LoginFragment.waitForPageLoaded(browser);
        signIn(false, UserRoles.ADMIN);
        logout();
        Screenshots.takeScreenshot(browser, "logout-ui", this.getClass());
    }

    @Test
    public void gd_Login_003_SignInWithEmptyPassword() {
        LoginFragment.getInstance(browser).login(testParams.getUser(), "", false);
        LoginFragment.getInstance(browser).checkPasswordInvalid();
    }

    @Test
    public void gd_Login_004_SignInWithInvalidPassword() {
        LoginFragment.getInstance(browser).login(testParams.getUser(), "abcdefgh", false);
        LoginFragment.getInstance(browser).checkInvalidLogin();
    }

    @Test
    public void gd_Login_005_SignInWithInvalidEmail() {
        LoginFragment.getInstance(browser).login("email_invalid_format", "abcdefgh", false);
        LoginFragment.getInstance(browser).checkEmailInvalid();
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
