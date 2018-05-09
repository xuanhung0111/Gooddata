package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
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
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.mail.ImapClientAction;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.*;

public class AbstractUITest extends AbstractGreyPageTest {

    protected static By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu,.gd-header-account,.hamburger-icon");
    protected static By BY_LOGOUT_LINK = By.className("s-logout");
    protected static final By BY_PANEL_ROOT = By.id("root");
    protected static final By BY_IFRAME = By.tagName("iframe");
    private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

    protected static final String PAGE_PROJECTS = "projects.html";
    protected static final String PAGE_UI_ANALYSE_PREFIX = "analyze/#/";
    protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
    protected static final String ACCOUNT_PAGE = "account.html";
    protected static final String PAGE_LOGIN = ACCOUNT_PAGE + "#/login";
    protected static final String DASHBOARD_PAGE_SUFFIX = "|projectDashboardPage";
    protected static final String PAGE_USER_MANAGEMENT = "users/#/users";
    protected static final String PAGE_INDIGO_DASHBOARDS = "dashboards/";
    protected static final String PAGE_LOST_PASSWORD = "account.html#/lostPassword";
    protected static final String PAGE_REGISTRATION =
            "account.html#/registration/projectTemplate/urn%3Agooddata%3AOnboardingProductTour";

    protected static final String CSV_UPLOADER_PROJECT_ROOT_TEMPLATE = "data/#/projects/%s";
    protected static final String DATA_UPLOAD_PAGE_URI_TEMPLATE = CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/datasets";

    private static final String LOCALIZATION_PAGE = "localization.html";
    private static final String USER_PROFILE_PAGE = PAGE_UI_PROJECT_PREFIX + "%s|profilePage|%s";

    private static final String EMBEDDED_INDIGO_DASHBOARD_PAGE_URI = "dashboards/embedded/#/p/%s";
    private static final String EMBEDDED_IFRAME_WRAPPER_URL = "https://s3.amazonaws.com/gdc-testing/embedding-wrapper.html";

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
        String user;
        String password;
        switch (userRole) {
            case ADMIN:
                user = testParams.getUser();
                password = testParams.getPassword();
                break;
            case EDITOR:
                user = testParams.getEditorUser();
                password = testParams.getPassword();
                break;
            case VIEWER:
                user = testParams.getViewerUser();
                password = testParams.getPassword();
                break;
            case DASHBOARD_ONLY:
                user = testParams.getDashboardOnlyUser();
                password = testParams.getPassword();
                break;
            default:
                throw new IllegalArgumentException("Unknow user role " + userRole);
        }
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
        waitForElementVisible(cssSelector("a.account-menu,.gd-header-account,.hamburger-icon,.logo-anchor"), browser);
        takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
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

    public void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) {
        initDashboardsPage();
        dashboardsPage.selectDashboard(dashboardName);
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.addNewTab(tabName);
        checkRedBar(browser);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void addReportToNewDashboard(String reportName, String dashboardName) {
        initDashboardsPage()
                .addNewDashboard(dashboardName)
                .addReportToDashboard(reportName)
                .saveDashboard();
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

    public ReportPage initReportCreation() {
        return initReportsPage()
            .openFolder("My Reports")
            .startCreateReport();
    }

    public void verifyDashboardExport(String dashboardName, String tabName, long minimalSize) {
        // client-demo does not support dashboard export
        if (testParams.isClientDemoEnvironment()) {
            log.info("client-demo does not support dashboard export");
            return; 
        }

        File pdfExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + dashboardName + ".pdf");
        System.out.println("pdfExport = " + pdfExport);
        Function<WebDriver, Boolean> exportCompleted = browser -> pdfExport.length() > minimalSize;
        Graphene.waitGui()
            .pollingEvery(5, TimeUnit.SECONDS)
            .withTimeout(5, TimeUnit.MINUTES)
            .until(exportCompleted);
        long fileSize = pdfExport.length();
        System.out.println("File size: " + fileSize);

        assertTrue(PdfUtils.getTextContentFrom(pdfExport).contains(tabName));
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is "
                + fileSize + ", but minimum " + minimalSize + " was expected");
    }

    public void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
        String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + reportName + "."
                + format.getName();
        File export = new File(fileURL);
        System.out.println("pdfExport = " + export);

        try {
            Function<WebDriver, Boolean> exportCompleted = browser -> export.length() > minimalSize;
            Graphene.waitGui()
                .pollingEvery(5, TimeUnit.SECONDS)
                .withTimeout(5, TimeUnit.MINUTES)
                .until(exportCompleted);
        } catch (TimeoutException e) { // do nothing
        } finally {
            long fileSize = export.length();
            System.out.println("File size: " + fileSize);
            assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is "
                    + fileSize + ", but minimum " + minimalSize + " was expected");
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

    private DashboardsPage waitForDashboardPage() {
        waitForDashboardPageLoaded(browser);
        return waitForFragmentVisible(dashboardsPage);
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

    public void initEmptyDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX);
        waitForElementVisible(By.id("p-projectDashboardPage"), browser);
    }

    public DashboardsPage initDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX);
        return waitForDashboardPage();
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
        openUrl(getIndigoDashboardsPageUri());
        waitForOpeningIndigoDashboard();
        return IndigoDashboardsPage.getInstance(browser);
    }

    public IndigoDashboardsPage initIndigoDashboardsPageWithWidgets() {
        openUrl(getIndigoDashboardsPageUri());
        waitForOpeningIndigoDashboard();

        return IndigoDashboardsPage.getInstance(browser)
                .waitForDashboardLoad()
                .waitForWidgetsLoading();
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardPageByType(EmbeddedType type) {
        if (type == EmbeddedType.IFRAME) {
            return initEmbeddedIndigoDashboardPageByIframe();
        }

        return initEmbeddedIndigoDashboardPageByUrl();
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

    public <T> T doActionWithImapClient(ImapClientAction<T> action) {
        return ImapClientAction.Utils.doActionWithImapClient(imapHost, imapUser, imapPassword, action);
    }

    public IndigoDashboardsPage initEmbeddedIndigoDashboardPageByIframe() {
        tryToInitEmbeddedIndigoDashboardPage();
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return IndigoDashboardsPage.getInstance(browser);
    }

    public void tryToInitEmbeddedIndigoDashboardPage(){
        browser.get(EMBEDDED_IFRAME_WRAPPER_URL);

        waitForElementVisible(By.id("url"), browser).sendKeys(getRootUrl() + getEmbeddedIndigoDashboardPageUri());
        waitForElementVisible(By.cssSelector("input[value='Go']"), browser).click();
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

    private IndigoDashboardsPage initEmbeddedIndigoDashboardPageByUrl() {
        openUrl(getEmbeddedIndigoDashboardPageUri());
        return IndigoDashboardsPage.getInstance(browser);
    }

    private String getEmbeddedIndigoDashboardPageUri() {
        return format(EMBEDDED_INDIGO_DASHBOARD_PAGE_URI, testParams.getProjectId());
    }

    public enum EmbeddedType {
        IFRAME, URL
    }
}
