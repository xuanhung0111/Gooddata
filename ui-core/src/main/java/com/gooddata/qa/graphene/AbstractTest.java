package com.gooddata.qa.graphene;

import com.gooddata.qa.utils.testng.listener.VideoRecorderListener;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.sdk.model.project.ProjectDriver;
import com.gooddata.sdk.service.project.ProjectService;
import org.openqa.selenium.UnhandledAlertException;
import com.gooddata.sdk.model.project.ProjectValidationResults;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.testng.listener.AuxiliaryFailureScreenshotListener;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;
import org.apache.http.ParseException;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;

import org.openqa.selenium.WebDriver;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class, AuxiliaryFailureScreenshotListener.class, VideoRecorderListener.class})
public abstract class AbstractTest extends Arquillian {

    protected static TestParameters testParams;

    @Drone
    protected WebDriver browser;

    protected String imapHost;
    protected String imapUser;
    protected String imapPassword;

    protected static final Logger log = Logger.getLogger(AbstractTest.class.getName());
    protected ch.qos.logback.classic.Logger logger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    // Store extra dynamic users in tests and will be deleted after test
    List<String> extraUsers = new ArrayList<>();

    /* Viet fix to force it follows Arquillian cycle */
    @BeforeSuite(groups = {"arquillian"}, inheritGroups = true)
    @Override
    public void arquillianBeforeSuite() throws Exception {
        super.arquillianBeforeSuite();
    }

    /* Viet fix to force it follows Arquillian cycle */
    // Reserve for use later
    @AfterSuite(groups = {"arquillian"}, inheritGroups = true, alwaysRun = true)
    @Override
    public void arquillianAfterSuite() throws Exception {
        super.arquillianAfterSuite();
    }

    /* Viet fix to force it follows Arquillian cycle */
    @BeforeClass(groups = {"arquillian"}, inheritGroups = true)
    @Override
    public void arquillianBeforeClass() throws Exception {
        super.arquillianBeforeClass();
        loadProperties();
    }

    /* Viet fix to force it follows Arquillian cycle */
    @AfterClass(groups = {"arquillian"}, inheritGroups = true, alwaysRun = true)
    @Override
    public void arquillianAfterClass() throws Exception {
        super.arquillianAfterClass();
        deleteUsers();
    }

    /* Viet fix to force it follows Arquillian cycle */
    @BeforeMethod(groups = {"arquillian"}, inheritGroups = true)
    @Override
    public void arquillianBeforeTest(Method testMethod) throws Exception {
        super.arquillianBeforeTest(testMethod);
    }

    /* Viet fix to force it follows Arquillian cycle */
    @AfterMethod(groups = {"arquillian"}, inheritGroups = true, alwaysRun = true)
    @Override
    public void arquillianAfterTest(Method testMethod) throws Exception {
        super.arquillianAfterTest(testMethod);
    }

    public static void loadProperties() {
        testParams = TestParameters.getInstance();
    }

    //@AfterClass(alwaysRun = true)
    public void deleteUsers() throws ParseException, IOException, JSONException {
        final String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(new RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true)),
                testParams.getProjectId());

        for (String user : extraUsers) {
            userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), user);
        }
    }

    public void openUrl(String url) {
        try {
            openUrl(getRootUrl(), url);
        }
        catch (UnhandledAlertException unhandledAlert) {
            browser.navigate().refresh();
            browser.switchTo().alert().accept();
            browser.switchTo().defaultContent();
            openUrl(getRootUrl(), url);
        }
    }

    public void openNodeJsUrl(String url) {
        openUrl(getNodeJsUrl(), url);
    }

    private void openUrl(String rootUrl, String url) {
        String pageURL;

        if (url.contains(rootUrl)) {
            pageURL = url.replaceAll("^/", "");
        } else {
            pageURL = rootUrl + url.replaceAll("^/", "");
        }

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

    public String getNodeJsUrl() {
        return testParams.getLocalhostSDK();
    }

    public String generateEmail(String email) {
        return email.replace("@", "+" + generateHashString() + "@");
    }

    public String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public String generate8HashString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public ProjectValidationResults validateProject() {
        final int timeout = testParams.getProjectDriver() == ProjectDriver.VERTICA ?
                testParams.getExtendedTimeout() : testParams.getDefaultTimeout();
        final ProjectService service = new RestClient(getProfile(Profile.ADMIN)).getProjectService();
        final Project project = service.getProjectById(testParams.getProjectId());
        final ProjectValidationResults results = service.validateProject(project).get(timeout, TimeUnit.SECONDS);
        System.out.println("Project valid: " + results.isValid());
        return results;
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
            case EDITOR_AND_INVITATIONS:
                username = testParams.getEditorInvitationsUser();
                password = testParams.getPassword();
                break;
            case EDITOR_AND_USER_ADMIN:
                username = testParams.getEditorAdminUser();
                password = testParams.getPassword();
                break;
            case EXPLORER:
                username = testParams.getExplorerUser();
                password = testParams.getPassword();
                break;
            case VIEWER:
                username = testParams.getViewerUser();
                password = testParams.getPassword();
                break;
            case VIEWER_DISABLED_EXPORT:
                username = testParams.getViewerDisabledExport();
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
        EDITOR_AND_INVITATIONS,
        EDITOR_AND_USER_ADMIN,
        EXPLORER,
        VIEWER,
        VIEWER_DISABLED_EXPORT
    }
}
