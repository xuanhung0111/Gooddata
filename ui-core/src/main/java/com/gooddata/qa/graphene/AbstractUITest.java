package com.gooddata.qa.graphene;

import com.gooddata.qa.boilerplate.fragments.LoginBoilerPlateFragment;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.AccountPage;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataTypeSelect.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.i18n.LocalizationPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.*;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.mail.ImapClientAction;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.*;

public class AbstractUITest extends AbstractGreyPageTest {

    protected static By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu,.gd-header-account,.hamburger-icon");
    protected static By BY_LOGOUT_LINK = By.className("s-logout");
    protected static final By BY_IFRAME = By.tagName("iframe");
    protected static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

    protected static final String PAGE_PROJECTS = "projects.html";
    protected static final String PAGE_UI_ANALYSE_PREFIX = "analyze/#/";
    protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
    protected static final String ACCOUNT_PAGE = "account.html";
    protected static final String PAGE_LOGIN = ACCOUNT_PAGE + "#/login";
    protected static final String DASHBOARD_PAGE_SUFFIX = "|projectDashboardPage";
    protected static final String PAGE_USER_MANAGEMENT = "users/#/users";
    protected static final String PAGE_INDIGO_DASHBOARDS = "dashboards/";
    protected static final String PAGE_LOST_PASSWORD = "account.html#/lostPassword";
    protected static final String PAGE_REGISTRATION = "account.html#/registration";
    protected static final String PAGE_UI_MODEL_DATA_PREFIX = "modeler/#/projects/";
    protected static final String PAGE_UI_MODEL_DATA_SUFFIX = "?navigation=disc";

    protected static final String CSV_UPLOADER_PROJECT_ROOT_TEMPLATE = "data/#/projects/%s";
    protected static final String DATA_UPLOAD_PAGE_URI_TEMPLATE = CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/datasets";

    private static final String LOCALIZATION_PAGE = "localization.html";
    private static final String USER_PROFILE_PAGE = PAGE_UI_PROJECT_PREFIX + "%s|profilePage|%s";
    private static final String EMBEDDED_INDIGO_DASHBOARD_PAGE_URI = "dashboards/embedded/#/project/%s";
    private static final String EMBEDDED_ANALYZE_PAGE_URI = "analyze/embedded/#/%s/reportId/edit";
    private static final String EMBEDDED_IFRAME_WRAPPER_URL = "https://s3.amazonaws.com/gdc-testing-public/embedding-wrapper.html";
    /**
     * ----- UI fragmnets -----
     */

    @FindBy(id = "root")
    protected DashboardsPage dashboardsPage;

    @FindBy(id = "p-analysisPage")
    protected ReportPage reportPage;

    /**
     * Help method which provides verification if login page is present a sign in a demo user if needed
     *
     * @param greyPages - indicator for login at greyPages/UI
     * @param userRole  - user role (based on this enum, parameter with user credentials is used)
     * @throws org.json.JSONException
     */
    protected void signIn(boolean greyPages, UserRoles userRole) throws JSONException {
        Pair<String, String> infoUser = testParams.getInfoUser(userRole);
        String user = infoUser.getKey();
        String password = infoUser.getValue();
        if (greyPages) {
            signInAtGreyPages(user, password);
        } else {
            signInAtUI(user, password);
        }
    }

    public void signInAtUI(String username, String password) {
        if (!browser.getCurrentUrl().contains(ACCOUNT_PAGE)) {
            openUrl(PAGE_LOGIN);
        }
        LoginFragment.getInstance(browser).login(username, password, true);
        waitForElementVisible(cssSelector("a.account-menu,.gd-header-account,.hamburger-icon,.logo-anchor,.adi-editor-main,.is-dashboard-loaded"), browser);
        takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
    }

    protected void signInFromReact(UserRoles userRole) throws JSONException {
        Pair<String, String> infoUser = testParams.getInfoUser(userRole);
        signInFromReact(infoUser.getKey(), infoUser.getValue());
    }

    public void signInFromReact(String username, String password) {
        if (testParams.useBoilerPlate()) {
            openNodeJsUrl("login");
            LoginBoilerPlateFragment.getInstance(browser).login(username, password);
        } else {
            openNodeJsUrl(PAGE_LOGIN);
            LoginFragment.getInstance(browser).login(username, password, true);
        }
        System.out.println(format("Successful login with user: %s to react project", username));
    }

    public LoginFragment logout() {
        openUrl(PAGE_PROJECTS);

        //after logged user button displays,
        //there could be still a layer of spinner icon, for loading projects paging
        //and the button is not clickable
        //we must wait until the button is clickable
        //see CL-11186 for more details
        waitForProjectsPageLoaded(browser);
        Graphene.waitGui()
                .until(ExpectedConditions.elementToBeClickable(BY_LOGGED_USER_BUTTON))
                .click();

        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
        return LoginFragment.getInstance(browser);
    }

    public void logoutAndLoginAs(boolean greyPages, UserRoles userRole) throws JSONException {
        logout();
        signIn(greyPages, userRole);
    }

    public void verifyProjectDashboardsAndTabs(boolean validation, Map<String, String[]> expectedDashboardsAndTabs,
                                               boolean openPage) {
        // sleep to avoid RED BAR - An error occurred while performing this operation.
        sleepTightInSeconds(5);
        if (openPage) {
            initDashboardsPage();
        }
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(5);
        waitForElementVisible(dashboardsPage.getRoot());
        if (expectedDashboardsAndTabs == null || expectedDashboardsAndTabs.isEmpty()) {
            log.info("Going to check all dashboard & tabs");
            int dashboardsCount = dashboardsPage.getDashboardsCount();
            log.info("Dashboards count: " + dashboardsCount);
            for (int i = 1; i <= dashboardsCount; i++) {
                if (dashboardsCount > 1) {
                    dashboardsPage.selectDashboard(i);
                    sleepTightInSeconds(5);
                    log.info("Current dashboard index: " + i);
                }
                singleDashboardWalkthrough(validation, null, dashboardsPage.getDashboardName());
            }
        } else {
            log.info("Going to check expected dashboards & tabs");
            for (String dashboardName : expectedDashboardsAndTabs.keySet()) {
                int dashboardsCount = dashboardsPage.getDashboardsCount();
                assertEquals(dashboardsCount, expectedDashboardsAndTabs.size(),
                        "Number of dashboards doesn't match");
                dashboardsPage.selectDashboard(dashboardName);
                sleepTightInSeconds(5);
                String[] expectedTabs = expectedDashboardsAndTabs.get(dashboardName);
                log.info("Current dashboard: " + dashboardName);
                singleDashboardWalkthrough(validation, expectedTabs, dashboardName);
            }
        }
    }

    public void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) {
        initDashboardsPage();
        dashboardsPage.selectDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.addNewTab(tabName);
        checkRedBar(browser);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void addNewTabOnNewDashboard(String dashboardName, String tabName, String screenshotName, Boolean isEmptyTab) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.addNewTab(tabName);
        if (!isEmptyTab) {
            dashboardsPage.addReportToDashboard(REPORT_AMOUNT_BY_PRODUCT);
            dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class).waitForLoaded();
        }
        checkRedBar(browser);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void addReportToNewDashboard(String reportName, String dashboardName) {
        initDashboardsPage()
                .addNewDashboard(dashboardName)
                .addReportToDashboard(reportName);
        dashboardsPage.waitForReportLoaded(singletonList(reportName));
        dashboardsPage.saveDashboard();
        checkRedBar(browser);
    }

    public void createDashboard(String name) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(name);
    }

    public void lockDashboard(boolean lock) {
        initDashboardsPage();
        dashboardsPage.lockDashboard(lock);
        waitForElementVisible(dashboardsPage.getRoot());
    }

    public void publishDashboard(boolean publish) {
        By okayBtnLocator = By.cssSelector(".s-btn-ok__got_it");
        initDashboardsPage();
        dashboardsPage.publishDashboard(publish);
        waitForElementVisible(dashboardsPage.getRoot());
        if (publish && browser.findElements(okayBtnLocator).size() != 0) {
            waitForElementVisible(okayBtnLocator, browser).click();
        }
    }

    public void selectDashboard(String name) {
        initDashboardsPage();
        dashboardsPage.selectDashboard(name);
        waitForDashboardPage();
    }

    public void createReport(UiReportDefinition reportDefinition, String screenshotName) {
        initReportCreation();
        reportPage.createReport(reportDefinition);
        takeScreenshot(browser, screenshotName + "-" + reportDefinition.getName() + "-" +
                reportDefinition.getType().getName(), this.getClass());
        checkRedBar(browser);
    }

    public void createReport(UiReportDefinition reportDefinition, ReportTypes reportTypes) {
        initReportCreation();
        reportPage.createReport(reportDefinition, reportTypes);
        takeScreenshot(browser, reportDefinition.getName() + "-" + reportDefinition.getName() + "-" +
                reportDefinition.getType().getName(), this.getClass());
    }

    public ReportPage initReportCreation() {
        return initReportsPage()
                .openFolder("My Reports")
                .startCreateReport();
    }

    public void verifyDashboardExport(String dashboardName, String tabName) {
        // client-demo does not support dashboard export
        if (testParams.isClientDemoEnvironment()) {
            log.info("client-demo does not support dashboard export");
            return;
        }

        File pdfExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + dashboardName);
        System.out.println("pdfExport = " + pdfExport);
        Function<WebDriver, Boolean> exportCompleted = browser -> pdfExport.exists() && pdfExport.length() != 0;
        Graphene.waitGui()
                .pollingEvery(5, TimeUnit.SECONDS)
                .withTimeout(5, TimeUnit.MINUTES)
                .until(exportCompleted);
        System.out.println("File size: " + pdfExport.length());

        assertTrue(PdfUtils.getTextContentFrom(pdfExport).contains(tabName), "Content of exported PDF is wrong");
    }

    public String getContentFrom(File pdfFile) {
        if (!pdfFile.exists()) {
            throw new NullPointerException();
        }
        return PdfUtils.getTextContentFrom(pdfFile);
    }

    public String getContentFrom(String pdfFile) {
        File pdfExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + pdfFile + ".pdf");
        return getContentFrom(pdfExport);
    }

    public String getContentCSVFileFrom(String csvFile) {
        File csvExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + csvFile + ".csv");
        return getContentFrom(csvExport);
    }

    public void verifyReportExport(ExportFormat format, String reportName) {
        String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + reportName + "."
                + format.getName();
        File export = new File(fileURL);
        System.out.println("pdfExport = " + export);

        try {
            Function<WebDriver, Boolean> exportCompleted = browser -> export.exists() && export.length() != 0;
            Graphene.waitGui()
                    .pollingEvery(5, TimeUnit.SECONDS)
                    .withTimeout(5, TimeUnit.MINUTES)
                    .until(exportCompleted);
        } catch (TimeoutException e) { // do nothing
        } finally {
            System.out.println("File size: " + export.length());
        }

        if (format == ExportFormat.IMAGE_PNG) {
            browser.get("file://" + fileURL);
            takeScreenshot(browser, "export-report-" + reportName, this.getClass());
            waitForElementPresent(By.xpath("//img[contains(@src, '" + testParams.getDownloadFolder() + "')]"),
                    browser);
        }
    }

    public void uploadCSV(String filePath) {
        uploadCSV(filePath, null);
    }

    public void uploadCSV(String filePath, Map<String, ColumnType> columnsWithExpectedType) {
        initDataUploadPage().waitForHeaderVisible();
        final int datasetCountBeforeUpload = DatasetsListPage.getInstance(browser).getMyDatasetsCount();
        log.info("Number of dataset before upload is " + datasetCountBeforeUpload);

        final DataPreviewPage dataPreviewPage = DatasetsListPage.getInstance(browser).uploadFile(filePath);
        takeScreenshot(browser, "upload-definition", this.getClass());

        if (columnsWithExpectedType != null) {
            DataPreviewTable dataPreviewTable = dataPreviewPage.getDataPreviewTable();
            for (String columnName : columnsWithExpectedType.keySet()) {
                dataPreviewTable.changeColumnType(columnName, columnsWithExpectedType.get(columnName));
            }
        }

        dataPreviewPage.triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        final DatasetMessageBar csvDatasetMessageBar = DatasetMessageBar.getInstance(browser);

        final int datasetCountAfterUpload = DatasetsListPage.getInstance(browser).getMyDatasetsCount();
        log.info("Number of dataset after upload is " + datasetCountAfterUpload);

        if (datasetCountAfterUpload == datasetCountBeforeUpload + 1) {
            log.info("Upload succeeds with message: " + csvDatasetMessageBar.waitForSuccessMessageBar().getText());
        } else {
            fail("Upload failed with error message: " + csvDatasetMessageBar.waitForErrorMessageBar().getText());
        }
    }

    public void updateCsvDataset(String datasetName, String filePath) {
        initDataUploadPage()
                .getMyDatasetsTable()
                .getDataset(datasetName)
                .clickUpdateButton()
                .pickCsvFile(filePath)
                .clickUploadButton();
        DataPreviewPage.getInstance(browser).triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar();
    }

    public DatasetsListPage initDataUploadPage() {
        openUrl(format(DATA_UPLOAD_PAGE_URI_TEMPLATE, testParams.getProjectId()));
        return DatasetsListPage.getInstance(browser);
    }

    public ProjectsPage initProjectsPage() {
        openUrl(PAGE_PROJECTS);
        waitForProjectsPageLoaded(browser);
        return ProjectsPage.getInstance(browser);
    }

    public DashboardsPage initDashboardsPage() {
        // Calling this method when dashboardsPage already initialized and in
        // edit mode (possibly prepared by previous test) will make dashboard not refresh in
        // default stage (edit mode instead).
        // This is expected behavior of browser and we cannot do anything with WebDriver.get() method.
        // So using this work around for expected purpose.
        if (browser.getCurrentUrl().matches(".*\\|projectDashboardPage\\|.*\\|edit")) {
            openUrl(PAGE_GDC);
        }
        openUrl(getDashboardsPageUri());
        return waitForDashboardPage();
    }

    public String getDashboardsPageUri() {
        return PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX;
    }

    public ReportsPage initReportsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage");
        waitForReportsPageLoaded(browser);
        return ReportsPage.getInstance(browser);
    }

    public AttributePage initAttributePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        waitForDataPageLoaded(browser);
        return AttributePage.getInstance(browser);
    }

    public void initModelPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|ldmModel");
        waitForDataPageLoaded(browser);
    }

    public MetricPage initMetricPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        waitForDataPageLoaded(browser);
        return MetricPage.getInstance(browser);

    }

    public AnalysisPage initAnalysePage() {
        openUrl(PAGE_UI_ANALYSE_PREFIX + testParams.getProjectId() + "/reportId/edit");
        return AnalysisPage.getInstance(browser);
    }

    public AccountPage initAccountPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|accountPage|");
        waitForAccountPageLoaded(browser);
        return AccountPage.getInstance(browser);
    }

    public VariablesPage initVariablePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        waitForDataPageLoaded(browser);
        return VariablesPage.getInstance(browser);
    }

    public void initFactPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        waitForDataPageLoaded(browser);
    }

    public DataPage initManagePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|");
        waitForDataPageLoaded(browser);
        return DataPage.getInstance(browser);
    }

    public ProjectAndUsersPage initProjectsAndUsersPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectPage|");
        waitForProjectsAndUsersPageLoaded(browser);

        return ProjectAndUsersPage.getInstance(browser);
    }

    public UserManagementPage initUserManagementPage() {
        openUrl(PAGE_USER_MANAGEMENT);
        return UserManagementPage.getInstance(browser);
    }

    public UserManagementPage initUngroupedUsersPage() {
        openUrl(PAGE_USER_MANAGEMENT + "?groupId=GROUP_UNGROUPED");
        return UserManagementPage.getInstance(browser);
    }

    public String getIndigoDashboardsPageUri() {
        return PAGE_INDIGO_DASHBOARDS + "#/project/" + testParams.getProjectId();
    }

    public IndigoDashboardsPage initIndigoDashboardsPage() {
        return initIndigoDashboardsPageSpecificProject(testParams.getProjectId());
    }

    public IndigoDashboardsPage initIndigoDashboardsPage(int timeout) {
        return initIndigoDashboardsPageSpecificProject(testParams.getProjectId(), timeout);
    }

    public String getIndigoDashboardsPageUriSpecificProject(String projectId) {
        return PAGE_INDIGO_DASHBOARDS + "#/project/" + projectId;
    }

    public IndigoDashboardsPage initIndigoDashboardsPageSpecificProject(String projectId) {
        openUrl(getIndigoDashboardsPageUriSpecificProject(projectId));
        waitForOpeningIndigoDashboard();
        return IndigoDashboardsPage.getInstance(browser);
    }

    public IndigoDashboardsPage initIndigoDashboardsPageSpecificProject(String projectId, int timeout) {
        openUrl(getIndigoDashboardsPageUriSpecificProject(projectId));
        waitOpeningIndigoDashboard(timeout);
        return IndigoDashboardsPage.getInstance(browser);
    }

    public IndigoDashboardsPage initIndigoDashboardsPageWithWidgets() {
        openUrl(getIndigoDashboardsPageUri());
        waitForOpeningIndigoDashboard();

        return IndigoDashboardsPage.getInstance(browser)
                .waitForDashboardLoad()
                .waitForWidgetsLoading();
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardPageByType(EmbeddedType type, boolean... preventDefault) {
        if (type == EmbeddedType.IFRAME) {
            return initEmbeddedIndigoDashboardPageByIframe();
        }

        return initEmbeddedIndigoDashboardPageByUrl(preventDefault);
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardWithShowNavigation(EmbeddedType type, boolean... showNavigation) {
        return type == EmbeddedType.IFRAME ? initEmbeddedIndigoDashboardPageByIframe() : initEmbeddedIndigoDashboardWithShowNavigationByUrl(showNavigation);
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardWithFilterByTags(String tagType, String tags) {
        openUrl(getEmbeddedIndigoDashboardFilterByTagUri(tagType, tags));
        return IndigoDashboardsPage.getInstance(browser);
    }

    public void initModelDataPage() {
        openUrl(PAGE_UI_MODEL_DATA_PREFIX + testParams.getProjectId() + PAGE_UI_MODEL_DATA_SUFFIX);
        waitForModelDataPageLoaded(browser);
    }

    private String getEmbeddedIndigoDashboardFilterByTagUri(String tagType, String tags) {
        String filterByTagParameter = "";
        if (tagType.length() != 0 && tags.length() != 0) {
            filterByTagParameter = "?" + tagType + "=[" + tags + "]";
        }
        return format(EMBEDDED_INDIGO_DASHBOARD_PAGE_URI, testParams.getProjectId() + filterByTagParameter);
    }

    public EmailSchedulePage initEmailSchedulesPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|emailSchedulePage");
        waitForSchedulesPageLoaded(browser);
        waitForElementNotVisible(BY_SCHEDULES_LOADING);
        return EmailSchedulePage.getInstance(browser);
    }

    public LostPasswordPage initLostPasswordPage() {
        openUrl(PAGE_LOST_PASSWORD);
        return LostPasswordPage.getInstance(browser);
    }

    public RegistrationPage initRegistrationPage() {
        openUrl(PAGE_REGISTRATION);
        return RegistrationPage.getInstance(browser);
    }

    public LocalizationPage initLocalizationPage() {
        openUrl(LOCALIZATION_PAGE);
        return LocalizationPage.getInstance(browser);
    }

    public UserProfilePage initUserProfilePage(String userProfile) {
        openUrl(format(USER_PROFILE_PAGE, testParams.getProjectId(), userProfile));
        return UserProfilePage.getInstance(browser);
    }

    public SDKAnalysisPage initSDKAnalysisPage() {
        try {
            browser.get("https://google.com.vn");
            openNodeJsUrl(testParams.getLocalhostSDK());
            waitForElementVisible(id("root"), browser);
        } catch (WebDriverException e) {
            browser.navigate().refresh();
        }
        return SDKAnalysisPage.getInstance(browser);
    }

    public <T> T doActionWithImapClient(ImapClientAction<T> action) {
        return ImapClientAction.Utils.doActionWithImapClient(imapHost, imapUser, imapPassword, action);
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardPageByIframe() {
        return initEmbeddedIndigoDashboardPageByIframe(true);
    }

    public <T extends AbstractFragment> T initEmbeddedIndigoDashboardPageByIframe(boolean isLoggedIn) {
        tryToInitEmbeddedIndigoDashboardPage();
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        waitForOpeningIndigoDashboard();
        return isLoggedIn ? (T) IndigoDashboardsPage.getInstance(browser) : (T) LoginFragment.getInstance(browser);
    }

    public void tryToInitEmbeddedIndigoDashboardPage(){
        browser.get(EMBEDDED_IFRAME_WRAPPER_URL);

        waitForElementVisible(By.id("url"), browser).sendKeys(getRootUrl() + getEmbeddedIndigoDashboardPageUri());
        waitForElementVisible(By.cssSelector("input[value='Go']"), browser).click();
        Graphene.waitGui().until(e -> !waitForElementVisible(By.id("demo"), browser).getText().equals("Content of postMessage API... "));
    }

    protected IndigoDashboardsPage initIndigoDashboardsPage(String params) {
        openIndigoDashboardWithCustomSetting(params);
        return IndigoDashboardsPage.getInstance(browser);
    }

    protected IndigoDashboardsPage initIndigoDashboardsPageWithWidgets(String params) {
        openIndigoDashboardWithCustomSetting(params);

        return IndigoDashboardsPage.getInstance(browser)
                .waitForDashboardLoad()
                .waitForWidgetsLoading();
    }

    protected IndigoDashboardsPage initEmbeddedIndigoDashboardPageByType(EmbeddedType type, String params) {
        if (type == EmbeddedType.IFRAME) {
            tryToInitEmbeddedIndigoDashboardPage(params);
            browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
            return IndigoDashboardsPage.getInstance(browser);
        }

        openUrl(getEmbeddedIndigoDashboardPageUri() + "?" + params);
        return IndigoDashboardsPage.getInstance(browser);
    }

    protected AnalysisPage initEmbeddedAnalysisPage() {
        openUrl(format(EMBEDDED_ANALYZE_PAGE_URI, testParams.getProjectId()));
        return AnalysisPage.getInstance(browser);
    }

    protected void waitForOpeningIndigoDashboard() {
        final By loadingLabel = className("gd-loading-equalizer");
        try {
            Function<WebDriver, Boolean> isLoadingLabelPresent = browser -> isElementPresent(loadingLabel, browser);
            Graphene.waitGui().withTimeout(2, TimeUnit.SECONDS).until(isLoadingLabelPresent);
        } catch (TimeoutException e) {
            //do nothing
        }

        waitForElementNotPresent(loadingLabel);
    }

    protected void waitOpeningIndigoDashboard(int timeout) {
        final By loadingLabel = className("gd-loading-equalizer");
        try {
            Function<WebDriver, Boolean> isLoadingLabelPresent = browser -> isElementPresent(loadingLabel, browser);
            Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until(isLoadingLabelPresent);
        } catch (TimeoutException e) {
            browser.navigate().refresh();
        }

        waitForElementNotPresent(loadingLabel);
    }

    protected void waitForOpeningPage(By locator, int timeout) {
        final By loadingLabel = locator;
        try {
            Function<WebDriver, Boolean> isLoadingLabelPresent = browser -> isElementPresent(loadingLabel, browser);
            Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until(isLoadingLabelPresent);
        } catch (TimeoutException e) {
            //do nothing
        }

        waitForElementNotPresent(loadingLabel);
    }

    protected void waitForOpeningModelPage() {
        waitForOpeningPage(By.cssSelector(".gd-spinner.large"), 2);
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
                    "Expected tab name doesn't match, index:" + i + ", " + tabLabels.get(i));
            tabs.openTab(i);
            System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar(browser);
        }
    }

    private DashboardsPage waitForDashboardPage() {
        waitForDashboardPageLoaded(browser);
        return waitForFragmentVisible(dashboardsPage);
    }

    private void tryToInitEmbeddedIndigoDashboardPage(String params) {
        browser.get(EMBEDDED_IFRAME_WRAPPER_URL);

        waitForElementVisible(By.id("url"), browser)
                .sendKeys(getRootUrl() + getEmbeddedIndigoDashboardPageUri() + "?" + params);
        waitForElementVisible(By.cssSelector("input[value='Go']"), browser).click();
    }

    private void openIndigoDashboardWithCustomSetting(String params) {
        openUrl(getIndigoDashboardsPageUri() + "?" + params);
        waitForOpeningIndigoDashboard();
    }

    private IndigoDashboardsPage initEmbeddedIndigoDashboardPageByUrl(boolean... preventDefault) {
        openUrl(getEmbeddedIndigoDashboardPageUri(preventDefault));
        return IndigoDashboardsPage.getInstance(browser);
    }

    private IndigoDashboardsPage initEmbeddedIndigoDashboardWithShowNavigationByUrl(boolean... showNavigation) {
        openUrl(getEmbeddedIndigoDashboardShowNavigationUri(showNavigation));
        return IndigoDashboardsPage.getInstance(browser);
    }

    private String getEmbeddedIndigoDashboardPageUri( boolean... preventDefault) {
        String prevent = "";
        if (preventDefault.length != 0) {
            prevent = preventDefault[0] ? "?preventDefault=true" : "?preventDefault=false";
        }
        return format(EMBEDDED_INDIGO_DASHBOARD_PAGE_URI, testParams.getProjectId() + prevent);
    }

    public enum EmbeddedType {
        IFRAME, URL
    }

    private String getEmbeddedIndigoDashboardShowNavigationUri( boolean... showNavigation) {
        String navigation = "";
        if (showNavigation.length != 0) {
            navigation = showNavigation[0] ? "?showNavigation=true" : "?showNavigation=false";
        }
        return format(EMBEDDED_INDIGO_DASHBOARD_PAGE_URI, testParams.getProjectId() + navigation);
    }

    public boolean comparePDF(String exportedDashboardName) throws IOException {
        // Exported pdf file name contain generic hashcode , So need to remove hashcode .
        // For Example : Table_a12df.pdf (Exported in Download folder) -> Table.pdf (PDFTemplate folder)
        String PDFTemplateName = exportedDashboardName.replaceAll("_[a-z0-9]{5}", "");
        File PDFExport = new File(testParams.getExportFilePath(exportedDashboardName));
        if (!PDFExport.exists()) {
            throw new RuntimeException("Exported PDF File " + exportedDashboardName + " not found ");
        }
        File PDFTemplate = new File("src/test/resources/pdfTemplate/" + PDFTemplateName);
        if (!PDFTemplate.exists()) {
            throw new RuntimeException("Template PDF File " + PDFTemplateName + " not found ");
        }
        return PdfUtils.comparePDF(PDFExport.getPath(), PDFTemplate.getPath());
    }

    public boolean comparePDF(String exportedDashboardName, int pageFrom, int pageTo) throws IOException {
        // Exported pdf file name contain generic hashcode , So need to remove hashcode .
        // For Example : Table_a12df.pdf (Exported in Download folder) -> Table.pdf (PDFTemplate folder)
        String PDFTemplateName = exportedDashboardName.replaceAll("_[a-z0-9]{5}", "");
        File PDFExport = new File(testParams.getExportFilePath(exportedDashboardName));
        if (!PDFExport.exists()) {
            throw new RuntimeException("Exported PDF File " + exportedDashboardName + " not found ");
        }
        File PDFTemplate = new File("src/test/resources/pdfTemplate/" + PDFTemplateName);
        if (!PDFTemplate.exists()) {
            throw new RuntimeException("Template PDF File " + PDFTemplateName + " not found ");
        }
        return PdfUtils.comparePDFFromPageToPage(PDFExport.getPath(), PDFTemplate.getPath(), pageFrom, pageTo);
    }

    public AnalysisPage openAnalyzeEmbeddedPage(String type, String name) {
        String hasIdPart = browser.getCurrentUrl().split(testParams.getProjectId() + "/")[1];
        openUrl("analyze/embedded/#/" + testParams.getProjectId() + "/" + hasIdPart + "?" + type + "=[" + name + "]");
        return AnalysisPage.getInstance(browser);
    }

    public AnalysisPage openAnalyzePage(String type, String name) {
        initAnalysePage();
        String hasIdPart = browser.getCurrentUrl().split(testParams.getProjectId() + "/")[1];
        openUrl("analyze/#/" + testParams.getProjectId() + "/" + hasIdPart + "?" + type + "=[" + name + "]");
        return AnalysisPage.getInstance(browser);
    }

    public String getUser(UserRoles userRoles){
        switch (userRoles){
            case EDITOR:
                return testParams.getEditorUser();
            case VIEWER:
                return testParams.getViewerUser();
            case EDITOR_AND_USER_ADMIN:
                return testParams.getEditorAdminUser();
            case EDITOR_AND_INVITATIONS:
                return testParams.getEditorInvitationsUser();
            case VIEWER_DISABLED_EXPORT:
                return testParams.getViewerDisabledExport();
            default:
                throw new IllegalArgumentException("Unknown user role " + userRoles);
        }
    }

    public String createTestDashboard(String name) throws JSONException, IOException {
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(
                new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId());
        JSONObject dashboardObj = dashboardRestRequest.prepareDataDashboardJson(name);

        String dashboardURI = dashboardRestRequest.createDashboard(dashboardObj);

        //refresh page to update the dashboards has just been created
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardURI);
        waitForDashboardPageLoaded(browser);
        return dashboardURI;
    }
}
