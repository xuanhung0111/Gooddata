package com.gooddata.qa.graphene;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.graphene.fragments.greypages.gdc.GdcFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.etl.pull.PullFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2.Manage2Fragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.singleloadinterface.SingleLoadInterfaceFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.ExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.ImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.validate.ValidateFragment;
import com.gooddata.qa.graphene.enums.Validation;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.manage.DataPage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.manage.VariablesPage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;
import com.gooddata.qa.utils.webdav.WebDavClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class})
public abstract class AbstractTest extends Arquillian {

    public static enum DeleteMode {
        DELETE_ALWAYS,
        DELETE_IF_SUCCESSFUL,
        DELETE_NEVER;

        public static DeleteMode getModeByName(String deleteMode) {
            if (deleteMode != null && deleteMode.length() > 0) {
                for (DeleteMode mode : values()) {
                    if (mode.toString().toLowerCase().equals(deleteMode)) return mode;
                }
            }
            return DELETE_IF_SUCCESSFUL;
        }
    }

    protected Properties testVariables;
    private String propertiesPath;

    @Drone
    protected WebDriver browser;

    protected String host;
    protected String projectId;
    protected String projectName;
    protected String user;
    protected String password;
    protected String authorizationToken;
    protected String dssAuthorizationToken;

    protected RestApiClient restApiClient = null;

    protected String imapHost;
    protected String imapUser;
    protected String imapPassword;

    protected GoodData goodDataClient = null;

    protected String testIdentification;

    protected String downloadFolder;

    protected String startPage;

    protected DeleteMode deleteMode = DeleteMode.DELETE_IF_SUCCESSFUL;
    protected boolean successfulTest = false;

    protected By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu");
    protected static final By BY_LOGOUT_LINK = By.cssSelector("div.s-logout");
    protected static final By BY_PANEL_ROOT = By.id("root");

    protected static final By BY_GP_FORM = By.tagName("form");
    protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    protected static final By BY_GP_PRE_JSON = By.tagName("pre");
    protected static final By BY_GP_LINK = By.tagName("a");
    protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    protected static final By BY_IFRAME = By.tagName("iframe");

    protected static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
    protected static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
    protected static final String PAGE_GDC = "gdc";
    protected static final String PAGE_GDC_MD = PAGE_GDC + "/md";
    protected static final String PAGE_GDC_PROJECTS = PAGE_GDC + "/projects";
    protected static final String PAGE_ACCOUNT_LOGIN = PAGE_GDC + "/account/login";
    protected static final String PAGE_PROJECTS = "projects.html";
    protected static final String PAGE_UPLOAD = "upload.html";
    protected static final String PAGE_LOGIN = "account.html#/login";

    /**
     * ----- UI fragmnets -----
     */

    @FindBy(css = ".s-loginPage")
    protected LoginFragment loginFragment;

    @FindBy(id = "root")
    protected DashboardsPage dashboardsPage;

    @FindBy(id = "p-domainPage")
    protected ReportsPage reportsPage;

    @FindBy(id = "p-analysisPage")
    protected ReportPage reportPage;

    @FindBy(id = "p-projectPage")
    protected ProjectAndUsersPage projectAndUsersPage;

    @FindBy(id = "p-emailSchedulePage")
    protected EmailSchedulePage emailSchedulesPage;

    @FindBy(id = "projectsCentral")
    protected ProjectsPage projectsPage;

    @FindBy(css = ".l-primary")
    protected UploadFragment upload;

    @FindBy(id = "p-dataPage")
    protected DataPage dataPage;

    @FindBy(id = "attributesTable")
    protected ObjectsTable attributesTable;

    @FindBy(id = "p-objectPage")
    protected AttributeDetailPage attributeDetailPage;
    
    @FindBy(id = "p-dataPage")
    protected VariablesPage variablePage;

    @FindBy(id = "p-objectPage")
    protected VariableDetailPage variableDetailPage;

    @FindBy(id = "metricsTable")
    protected ObjectsTable metricsTable;

    @FindBy(id = "p-objectPage")
    protected MetricDetailsPage metricDetailPage;

    @FindBy(id = "new")
    protected MetricEditorDialog metricEditorPage;

    @FindBy(id = "factsTable")
    protected ObjectsTable factsTable;
    
    @FindBy(id = "p-objectPage")
    protected FactDetailPage factDetailPage;
    
    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectDetailPage;
    
    /**
     * ----- Grey pages fragments -----
     */

    @FindBy(tagName = "form")
    protected AccountLoginFragment gpLoginFragment;

    @FindBy(tagName = "form")
    protected ProjectFragment gpProject;

    @FindBy(tagName = "form")
    protected ValidateFragment validateFragment;

    @FindBy(className = "param")
    protected GdcFragment gdcFragment;

    @FindBy(tagName = "form")
    protected Manage2Fragment manage2Fragment;

    @FindBy(tagName = "form")
    protected ExportFragment exportFragment;

    @FindBy(tagName = "form")
    protected ImportFragment importFragment;

    @FindBy(tagName = "form")
    protected PullFragment pullFragment;

    @FindBy(tagName = "form")
    protected SingleLoadInterfaceFragment singleLoadInterfaceFragment;
    /**
     * ----------
     */

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

        host = loadProperty("host");
        user = loadProperty("user");
        password = loadProperty("password");
        authorizationToken = loadProperty("project.authorizationToken");
        dssAuthorizationToken = loadProperty("dss.authorizationToken");
        System.out.println("Basic properties initialized, host: " + host + ", user: " + user);

        deleteMode = DeleteMode.getModeByName(loadProperty("deleteMode"));

        downloadFolder = loadProperty("browserDownloadFolder");

        imapHost = loadProperty("imap.host");
        imapUser = loadProperty("imap.user");
        imapPassword = loadProperty("imap.password");

        testIdentification = loadProperty("testIdentification");
        if (testIdentification == null) {
            testIdentification = UUID.randomUUID().toString();
        }
    }

    /**
     * Method to return property value from:
     * - System.getProperty(key) if present (good for CI integration)
     * - properties file defined on path "propertiesPath" (default properties are in variables-env-test.properties)
     *
     * @param propertyKey
     * @return value of required property
     */
    protected String loadProperty(String propertyKey) {
        String property = System.getProperty(propertyKey);
        if (property != null && property.length() > 0) {
            return property;
        }
        return testVariables.getProperty(propertyKey);
    }

    @BeforeMethod
    public void loadPlatformPageBeforeTestMethod() {
        // register RedBasInterceptor - to check errors in UI
        //GrapheneProxyInstance proxy = (GrapheneProxyInstance) browser;
        //proxy.registerInterceptor(new RedBarInterceptor());

        openUrl(startPage != null ? startPage : "");
    }

    protected void openUrl(String url) {
        String pageURL = getRootUrl() + url;
        System.out.println("Loading page ... " + pageURL);
        browser.get(pageURL);
    }

    protected String getRootUrl() {
        return "https://" + host + "/";
    }

    protected String getBasicRootUrl() {
        String rootUrl = getRootUrl();
        return getRootUrl().substring(0, rootUrl.length() - 1);
    }

    /**
     * Help method which provides verification if login page is present a sign in a demo user if needed
     *
     * @param greyPages - indicator for login at greyPages/UI
     * @throws JSONException
     */
    protected void validSignInWithDemoUser(boolean greyPages) throws JSONException {
        if (greyPages) {
            signInAtGreyPages(user, password);
        } else {
            signInAtUI(user, password);
        }
    }

    public void signInAtUI(String username, String password) {
        openUrl(PAGE_LOGIN);
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(username, password, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON);
        Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
    }

    public void signInAtGreyPages(String username, String password) throws JSONException {
        openUrl(PAGE_ACCOUNT_LOGIN);
        waitForElementPresent(gpLoginFragment.getRoot());
        gpLoginFragment.login(username, password);
        Screenshots.takeScreenshot(browser, "login-gp", this.getClass());
    }

    public String validateProject() throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/validate");
        waitForElementPresent(validateFragment.getRoot());
        String statusReturning = validateFragment.validate();
        Screenshots.takeScreenshot(browser, projectId + "-validation", this.getClass());
        return statusReturning;
    }

    public void postMAQL(String maql, int statusPollingCheckIterations) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/ldm/manage2");
        waitForElementPresent(manage2Fragment.getRoot());
        assertTrue(manage2Fragment.postMAQL(maql, statusPollingCheckIterations), "MAQL was not successfully processed");
    }

    public String uploadFileToWebDav(URL resourcePath, String webContainer) throws URISyntaxException {
        WebDavClient webDav = WebDavClient.getInstance(user, password);
        File resourceFile = new File(resourcePath.toURI());
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()), " Create WebDav storage structure");
        } else webDav.setWebDavStructure(webContainer);

        webDav.uploadFile(resourceFile);
        return webDav.getWebDavStructure();
    }
    public java.io.InputStream getFileFromWebDav(String webContainer,URL resourcePath) throws URISyntaxException, IOException {
        File resourceFile = new File(resourcePath.toURI());
        return WebDavClient.getInstance(user, password).getFile(webContainer+"/"+resourceFile.getName());
    }

    public String exportProject(boolean exportUsers, boolean exportData, int statusPollingCheckIterations) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/maintenance/export");
        waitForElementPresent(exportFragment.getRoot());
        return exportFragment.invokeExport(exportUsers, exportData, statusPollingCheckIterations);
    }

    public void importProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/maintenance/import");
        waitForElementPresent(importFragment.getRoot());
        assertTrue(importFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Project import failed");
    }

    public void postPullIntegration(String integrationEntry, int statusPollingCheckIterations)
            throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/etl/pull");
        waitForElementPresent(pullFragment.getRoot());
        assertTrue(pullFragment.invokePull(integrationEntry, statusPollingCheckIterations),
                "ETL PULL was not successfully processed");
    }

    public JSONObject fetchSLIManifest(String dataset) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/ldm/singleloadinterface");
        waitForElementPresent(singleLoadInterfaceFragment.getRoot());
        return singleLoadInterfaceFragment.postDataset(dataset);
    }

    public String validateProjectPartial(Validation... validationOptions) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/validate");
        waitForElementPresent(validateFragment.getRoot());
        String statusReturning = validateFragment.validateOnly(validationOptions);
        Screenshots.takeScreenshot(browser, projectId + "-validation-partial", this.getClass());
        return statusReturning;
    }

    public void logout() {
        waitForElementVisible(BY_LOGGED_USER_BUTTON).click();
        waitForElementVisible(BY_LOGOUT_LINK).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
    }

    protected void verifyProjectDashboardsAndTabs(boolean validation, Map<String, String[]> expectedDashboardsAndTabs,
                                                  boolean openPage) throws InterruptedException {
        if (openPage) {
            browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
            waitForElementVisible(BY_LOGGED_USER_BUTTON);
        }
        waitForDashboardPageLoaded();
        Thread.sleep(5000);
        waitForElementVisible(dashboardsPage.getRoot());
        if (expectedDashboardsAndTabs == null) {
            int dashboardsCount = dashboardsPage.getDashboardsCount();
            for (int i = 1; i <= dashboardsCount; i++) {
                dashboardsPage.selectDashboard(i);
                Thread.sleep(5000);
                System.out.println("Current dashboard index: " + i);
                singleDashboardWalkthrough(validation, null, dashboardsPage.getDashboardName());
            }
        } else {
            for (String dashboardName : expectedDashboardsAndTabs.keySet()) {
                int dashboardsCount = dashboardsPage.getDashboardsCount();
                assertEquals(dashboardsCount, expectedDashboardsAndTabs.size(), "Number of dashboards doesn't match");
                dashboardsPage.selectDashboard(dashboardName);
                Thread.sleep(5000);
                String[] expectedTabs = expectedDashboardsAndTabs.get(dashboardName);
                System.out.println("Current dashboard: " + dashboardName);
                singleDashboardWalkthrough(validation, expectedTabs, dashboardName);
            }
        }
    }

    private void singleDashboardWalkthrough(boolean validation, String[] expectedTabs, String dashboardName) {
        DashboardTabs tabs = dashboardsPage.getTabs();
        int numberOfTabs = tabs.getNumberOfTabs();
        System.out.println("Number of tabs on dashboard " + dashboardName + ": " + numberOfTabs);
        if (validation) assertTrue(numberOfTabs == expectedTabs.length,
                "Expected number of dashboard tabs for project is not present");
        List<String> tabLabels = tabs.getAllTabNames();
        System.out.println("These tabs are available for selected project: " + tabLabels.toString());
        for (int i = 0; i < tabLabels.size(); i++) {
            if (validation) assertEquals(tabLabels.get(i), expectedTabs[i],
                    "Expected tab name doesn't not match, index:" + i + ", " + tabLabels.get(i));
            tabs.openTab(i);
            System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
            waitForDashboardPageLoaded();
            Screenshots.takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar();
        }
    }

    protected void checkRedBar() {
        if (browser.findElements(BY_RED_BAR).size() != 0) {
            fail("RED BAR APPEARED - " + browser.findElement(BY_RED_BAR).getText());
        }
        //this kind of error appeared for the first time in geo chart
        if (browser.findElements(BY_REPORT_ERROR).size() != 0 && browser.findElement(BY_REPORT_ERROR).isDisplayed()) {
            fail("Report error APPEARED - " + browser.findElement(BY_REPORT_ERROR).getText());
        }
    }

    protected void verifyDashboardExport(String dashboardName, long minimalSize) {
        File pdfExport = new File(downloadFolder + "/" + dashboardName + ".pdf");
        System.out.println("pdfExport = " + pdfExport);
        System.out.println(downloadFolder + "/" + dashboardName + ".pdf");
        long fileSize = pdfExport.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
    }

    protected void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
        String fileURL = downloadFolder + "/" + reportName + "." + format.getName();
        File export = new File(fileURL);
        long fileSize = export.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
        if (format == ExportFormat.IMAGE_PNG) {
            browser.get("file://" + fileURL);
            Screenshots.takeScreenshot(browser, "export-report-" + reportName, this.getClass());
            waitForElementPresent(By.xpath("//img[contains(@src, '" + downloadFolder + "')]"));
        }
    }

    protected void deleteProjectByDeleteMode(boolean successfulTest) {
        System.out.println("Delete mode is set to " + deleteMode.toString());
        if (projectId != null && projectId.length() > 0) {
            switch (deleteMode) {
                case DELETE_ALWAYS:
                    System.out.println("Project will be deleted...");
                    deleteProject(projectId);
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (successfulTest) {
                        System.out.println("Test was successful, project will be deleted...");
                        deleteProject(projectId);
                    } else {
                        System.out.println("Test wasn't successful, project won't be deleted...");
                    }
                    break;
                case DELETE_NEVER:
                    System.out.println("Delete mode set to NEVER, project won't be deleted...");
                    break;
            }
        } else {
            System.out.println("No project created -> no delete...");
        }
    }

    protected void deleteProject(String projectId) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
        waitForProjectPageLoaded();
        waitForElementVisible(projectAndUsersPage.getRoot());
        System.out.println("Going to delete project: " + projectId);
        projectAndUsersPage.deteleProject();
        System.out.println("Deleted project: " + projectId);
    }

    protected void initDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
        waitForElementVisible(BY_LOGGED_USER_BUTTON);
        waitForDashboardPageLoaded();
        waitForElementVisible(dashboardsPage.getRoot());
    }

    protected void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
        initDashboardsPage();
        assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
        waitForDashboardPageLoaded();
        Thread.sleep(3000);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.editDashboard();
        waitForDashboardPageLoaded();
        dashboardsPage.addNewTab(tabName);
        checkRedBar();
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded();
        waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
    }

    protected void initReportsPage() {
        browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|domainPage");
        waitForReportsPageLoaded();
        waitForElementVisible(reportsPage.getRoot());
    }

    protected void uploadSimpleCSV(String filePath, String screenshotName) throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
        waitForDashboardPageLoaded();
        openUrl(PAGE_UPLOAD);
        waitForElementVisible(upload.getRoot());
        upload.uploadFile(filePath);
        Screenshots.takeScreenshot(browser, screenshotName + "upload", this.getClass());
        UploadColumns uploadColumns = upload.getUploadColumns();
        System.out.println(uploadColumns.getNumberOfColumns() + " columns are available for upload, " + uploadColumns.getColumnNames() + " ," + uploadColumns.getColumnTypes());
        Screenshots.takeScreenshot(browser, "upload-definition", this.getClass());
        upload.confirmloadCsv();
        waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"));
        waitForDashboardPageLoaded();
        Screenshots.takeScreenshot(browser, screenshotName + "-dashboard", this.getClass());
    }

    public JSONObject loadJSON() throws JSONException {
        waitForElementPresent(BY_GP_PRE_JSON);
        return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
    }

    public RestApiClient getRestApiClient() {
        if (restApiClient == null) {
            restApiClient = new RestApiClient(host, user, password, true, false);
        }
        return restApiClient;
    }

    public GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = new GoodData(host, user, password);
        }
        return goodDataClient;
    }

    public void waitForDashboardPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"));
        checkRedBar();
    }

    public void waitForReportsPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-domainPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForDataPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-dataPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForProjectPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-projectPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForPulsePageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-pulsePage' and contains(@class,'s-displayed')]"));
    }

    public void waitForProjectsPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"));
    }

    public void waitForAnalysisPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForSchedulesPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"));
    }

    public void waitForObjectPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]"));
    }

    public WebElement waitForElementVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().visible();
        return browser.findElement(byElement);
    }

    public WebElement waitForElementVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().visible();
        return element;
    }

    public void waitForElementNotVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().visible();
    }

    public WebElement waitForElementPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().present();
        return browser.findElement(byElement);
    }

    public WebElement waitForElementPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().present();
        return element;
    }

    public void waitForElementNotPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().present();
    }

    public void waitForElementNotPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().present();
    }

}
