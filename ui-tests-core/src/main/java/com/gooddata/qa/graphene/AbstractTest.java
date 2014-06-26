package com.gooddata.qa.graphene;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.common.*;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class})
public abstract class AbstractTest extends Arquillian {

    protected Properties testVariables;
    private String propertiesPath;

    protected TestParameters testParams;
    protected CheckUtils checkUtils;
    protected CommonUtils commonUtils;
    protected UITestUtils ui;
    protected GreyPageUtils greyPages;

    @Drone
    protected WebDriver browser;

    protected String imapHost;
    protected String imapUser;
    protected String imapPassword;

    protected GoodData goodDataClient = null;
    protected RestApiClient restApiClient = null;

    protected String startPage;

    protected boolean successfulTest = false;

    @BeforeClass
    public void loadProperties() {
        propertiesPath = System.getProperty("propertiesPath", System.getProperty("user.dir") + "/ui-tests-core/src/test/resources/variables-env-test.properties");

        testVariables = new Properties();
        try {
            FileInputStream in = new FileInputStream(propertiesPath);
            testVariables.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties weren't loaded from path: " + propertiesPath);
        }

        testParams = new TestParameters(testVariables);
        checkUtils = new CheckUtils(browser);
        commonUtils = new CommonUtils(browser, checkUtils, testParams);
        ui = new UITestUtils(browser, checkUtils, testParams);
        greyPages = new GreyPageUtils(browser, checkUtils, testParams);

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @BeforeMethod
    public void loadPlatformPageBeforeTestMethod() {
        // register RedBasInterceptor - to check errors in UI
        //GrapheneProxyInstance proxy = (GrapheneProxyInstance) browser;
        //proxy.registerInterceptor(new RedBarInterceptor());

        openUrl(startPage != null ? startPage : "");
    }

    protected void openUrl(String url) {
        commonUtils.openUrl(url);
    }

    /**
     * Help method which provides verification if login page is present a sign in a demo user if needed
     *
     * @param greyPages - indicator for login at greyPages/UI
     * @throws JSONException
     */
    protected void validSignInWithDemoUser(boolean greyPages) throws JSONException {
        if (greyPages) {
            this.greyPages.signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        } else {
            ui.signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    public RestApiClient getRestApiClient() {
        if (restApiClient == null) {
            restApiClient = new RestApiClient(testParams.getHost(), testParams.getUser(), testParams.getPassword(), true, false);
        }
        return restApiClient;
    }

    public GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = new GoodData(testParams.getHost(), testParams.getUser(), testParams.getPassword());
        }
        return goodDataClient;
    }

    public WebElement waitForElementVisible(By byElement) {
        return checkUtils.waitForElementVisible(byElement);
    }

    public WebElement waitForElementVisible(WebElement element) {
        return checkUtils.waitForElementVisible(element);
    }

    public void waitForElementNotVisible(By byElement) {
        checkUtils.waitForElementNotVisible(byElement);
    }

    public WebElement waitForElementPresent(By byElement) {
        return checkUtils.waitForElementPresent(byElement);
    }

    public WebElement waitForElementPresent(WebElement element) {
        return checkUtils.waitForElementPresent(element);
    }

    public void waitForElementNotPresent(By byElement) {
        checkUtils.waitForElementNotPresent(byElement);
    }

    public void waitForElementNotPresent(WebElement element) {
        checkUtils.waitForElementNotPresent(element);
    }
}
