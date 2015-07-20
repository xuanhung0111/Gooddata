package com.gooddata.qa.graphene;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.common.CheckUtils;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.testng.Arquillian;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class})
public abstract class AbstractTest extends Arquillian {

    protected Properties testVariables;
    private String propertiesPath;

    protected TestParameters testParams;

    @Drone
    protected WebDriver browser;

    protected String imapHost;
    protected String imapUser;
    protected String imapPassword;

    protected GoodData goodDataClient = null;
    protected RestApiClient restApiClient = null;

    protected StartPageContext startPageContext = null;
    protected static final String PAGE_PROJECTS = "projects.html";
    
    //the projectInit group which will be skipped for loadPlatformPageBeforeTestMethod 
    protected static final String PROJECT_INIT_GROUP = "projectInit";

    @BeforeClass(alwaysRun = true)
    public void loadProperties() {
        propertiesPath = System.getProperty("propertiesPath", System.getProperty("user.dir") +
                "/ui-tests-core/src/test/resources/variables-env-test.properties".replace("/",
                        System.getProperty("file.separator")));
        System.out.println("User properties: " + propertiesPath);

        testVariables = new Properties();
        try {
            FileInputStream in = new FileInputStream(propertiesPath);
            testVariables.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties weren't loaded from path: " + propertiesPath);
        }

        testParams = new TestParameters(testVariables);
        
        startPageContext = new StartPageContext() {
            
            @Override
            public void waitForStartPageLoaded() {
                CheckUtils.waitForProjectsPageLoaded(browser);
            }
            
            @Override
            public String getStartPage() {
                return PAGE_PROJECTS;
            }
        };
    }

    @BeforeMethod(alwaysRun = true)
    public void loadPlatformPageBeforeTestMethod(Method m) {
        if (Arrays.asList(m.getAnnotation(Test.class).groups()).contains(PROJECT_INIT_GROUP)){
            return;
        }
        openUrl(startPageContext.getStartPage());
        startPageContext.waitForStartPageLoaded();
    }

    public void openUrl(String url) {
        String pageURL = getRootUrl() + url;
        System.out.println("Loading page ... " + pageURL);
        browser.get(pageURL);
    }

    public String getRootUrl() {
        return "https://" + testParams.getHost() + "/";
    }

    public String getBasicRootUrl() {
        String rootUrl = getRootUrl();
        return getRootUrl().substring(0, rootUrl.length() - 1);
    }

    /**
     * Create {@link com.gooddata.qa.utils.http.RestApiClient} for admin user and save it to the test context.
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} client for admin user
     */
    public RestApiClient getRestApiClient() {
        if (restApiClient == null) {
            restApiClient = getRestApiClient(testParams.getUser(), testParams.getPassword());
        }
        return restApiClient;
    }

    /**
     * Create {@link com.gooddata.qa.utils.http.RestApiClient} for specific user. It doesn't save it to the context!
     * @param userLogin
     * @param userPassword
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} for specific user.
     */
    public RestApiClient getRestApiClient(final String userLogin, final String userPassword) {
        return new RestApiClient(testParams.getHost(), userLogin, userPassword, true, false);
    }

    public GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = new GoodData(testParams.getHost(), testParams.getUser(), testParams.getPassword());
        }
        return goodDataClient;
    }
}
