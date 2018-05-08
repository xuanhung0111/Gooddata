package com.gooddata.qa.graphene;

import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.project.ProjectService;
import com.gooddata.project.ProjectValidationResults;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.testng.listener.AuxiliaryFailureScreenshotListener;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class, AuxiliaryFailureScreenshotListener.class})
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

    protected static final Logger log = Logger.getLogger(AbstractTest.class.getName());

    // Store extra dynamic users in tests and will be deleted after test
    List<String> extraUsers = new ArrayList<>();

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
    }

    @BeforeMethod(alwaysRun = true)
    public void loadPlatformPageBeforeTestMethod() {
        if (startPageContext == null) {
            return;
        }
        openUrl(startPageContext.getStartPage());
        startPageContext.waitForStartPageLoaded();
    }

    @AfterClass(alwaysRun = true)
    public void deleteUsers() throws ParseException, IOException, JSONException {
        RestApiClient restApiClient = testParams.getDomainUser() == null ? getRestApiClient() : getDomainUserRestApiClient();

        for (String user : extraUsers) {
            deleteUserByEmail(restApiClient, testParams.getUserDomain(), user);
        }
    }

    public void openUrl(String url) {
        String pageURL = getRootUrl() + url.replaceAll("^/", "");
        System.out.println("Loading page ... " + pageURL);

        BrowserUtils.addMagicElementToDOM(browser);
        // Request Selenium to load a URL. If current URL is the one we want to load, page is NOT reloaded.
        browser.get(pageURL);

        // If Magic element is still present, refresh the page.
        for (int attempts = 0; attempts < 3; attempts++) {
            if (!BrowserUtils.isMagicElementPresentInDOM(browser)) {
                return;
            }

            browser.navigate().refresh();
        }

        log.warning("Page content might not be fully reloaded");
    }

    public String getRootUrl() {
        return "https://" + testParams.getHost() + "/";
    }

    public String getBasicRootUrl() {
        String rootUrl = getRootUrl();
        return getRootUrl().substring(0, rootUrl.length() - 1);
    }

    public RestApiClient getDomainUserRestApiClient() {
        return getRestApiClient(testParams.getDomainUser() == null ? testParams.getUser() : testParams.getDomainUser(), 
                testParams.getPassword());
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
     *
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} for specific user.
     */
    public RestApiClient getRestApiClient(final String userLogin, final String userPassword) {
        return new RestApiClient(testParams.getHost(), userLogin, userPassword, true, false);
    }

    public GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = getGoodDataClient(testParams.getUser(), testParams.getPassword());
        }
        return goodDataClient;
    }

    public GoodData getGoodDataClient(final String userLogin, final String userPassword) {
        final HttpHost httpHost = RestApiClient.parseHost(testParams.getHost());
        return new GoodData(httpHost.getHostName(), userLogin, userPassword, httpHost.getPort());
    }

    public String generateEmail(String email) {
        return email.replace("@", "+" + generateHashString() + "@");
    }

    public String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public ProjectValidationResults validateProject() {
        final int timeout = testParams.getProjectDriver() == ProjectDriver.VERTICA ?
                testParams.getExtendedTimeout() : testParams.getDefaultTimeout();
        final ProjectService service = getGoodDataClient().getProjectService();
        final Project project = service.getProjectById(testParams.getProjectId());
        final ProjectValidationResults results = service.validateProject(project).get(timeout, TimeUnit.SECONDS);
        System.out.println("Project valid: " + results.isValid());
        return results;
    }

    /**
     * Create {@link com.gooddata.qa.utils.http.RestApiClient} for admin user.
     * The request will be resent if the exception is handled
     * 
     * @param maximumTries
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} client for admin user
     */
    public RestApiClient getRepeatableRestApiClient(int maximumTries) {
        return new RestApiClient(testParams.getHost(), testParams.getUser(), testParams.getPassword(), true, false,
                new HttpRequestRetryHandler() {
                    @Override
                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                        if (executionCount > maximumTries) {
                            return false;
                        }

                        if (exception instanceof NoHttpResponseException) {
                            log.info("Catch NoHttpResponseException: retry count = " + executionCount);
                            Sleeper.sleepTightInSeconds(2);
                            return true;
                        }

                        return false;
                    }
                });
    }

    protected RestProfile getProfile(Profile profile) {
        String username;
        String password;

        switch (profile) {
            case DOMAIN:
                username = testParams.getDomainUser();
                password = testParams.getPassword();
                break;
            case EDITOR:
                username = testParams.getEditorUser();
                password = testParams.getPassword();
                break;
            case VIEWER:
                username = testParams.getViewerUser();
                password = testParams.getPassword();
                break;
            default:
                username = testParams.getUser();
                password = testParams.getPassword();
                break;
        }

        return new RestProfile(testParams.getHost(), username, password, true);
    }

    public enum Profile {
        DOMAIN,
        ADMIN,
        EDITOR,
        VIEWER
    }
}
