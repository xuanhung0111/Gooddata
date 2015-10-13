package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForProjectPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForReportsPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForSchedulesPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.disc.DISCProjectsPage;
import com.gooddata.qa.graphene.fragments.disc.DeployForm;
import com.gooddata.qa.graphene.fragments.disc.NavigationBar;
import com.gooddata.qa.graphene.fragments.disc.NotificationRulesDialog;
import com.gooddata.qa.graphene.fragments.disc.OverviewProjects;
import com.gooddata.qa.graphene.fragments.disc.OverviewStates;
import com.gooddata.qa.graphene.fragments.disc.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.ProjectsList;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.ScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.SchedulesTable;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.user.UserManagementPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.AttributePage;
import com.gooddata.qa.graphene.fragments.manage.CreateAttributePage;
import com.gooddata.qa.graphene.fragments.manage.DataPage;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.manage.VariablesPage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.google.common.base.Predicate;

public class AbstractUITest extends AbstractGreyPageTest {

    protected static By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu");
    protected static By BY_LOGOUT_LINK = By.cssSelector("div.s-logout");
    protected static final By BY_PANEL_ROOT = By.id("root");
    protected static final By BY_IFRAME = By.tagName("iframe");
    private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

    protected static final String PAGE_UI_ANALYSE_PREFIX = "analyze/#/";
    protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
    protected static final String PAGE_UPLOAD = "upload.html";
    protected static final String ACCOUNT_PAGE = "account.html";
    protected static final String PAGE_LOGIN = ACCOUNT_PAGE + "#/login";
    protected static final String DASHBOARD_PAGE_SUFFIX = "|projectDashboardPage";
    protected static final String PAGE_USER_MANAGEMENT = "users/#/users";
    protected static final String PAGE_INDIGO_DASHBOARDS = "dashboards/";

    protected static final String DISC_PROJECTS_PAGE_URL = "admin/disc/#/projects";
    protected static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";

    protected static final String DATA_UPLOAD_PAGE_URI_TEMPLATE = "data/#/projects/%s/datasets";

    /**
     * ----- UI fragmnets -----
     */

    @FindBy(css = ".s-loginPage.s-ready")
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

    @FindBy(id = "uploadsTable")
    protected ObjectsTable datasetsTable;

    @FindBy(id = "variablesTable")
    protected ObjectsTable variablesTable;

    @FindBy(id = "p-dataPage")
    protected AttributePage attributePage;

    @FindBy(id = "p-objectPage")
    protected AttributeDetailPage attributeDetailPage;

    @FindBy(id = "p-objectPage")
    protected DatasetDetailPage datasetDetailPage;

    @FindBy(id = "p-dataPage")
    protected VariablesPage variablePage;

    @FindBy(id = "p-objectPage")
    protected VariableDetailPage variableDetailPage;

    @FindBy(id = "metricsTable")
    protected ObjectsTable metricsTable;

    @FindBy(id = "p-objectPage")
    protected MetricDetailsPage metricDetailPage;

    @FindBy(id = "new")
    protected MetricPage metricPage;

    @FindBy(id = "factsTable")
    protected ObjectsTable factsTable;

    @FindBy(id = "p-objectPage")
    protected FactDetailPage factDetailPage;

    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectDetailPage;

    @FindBy(id = "p-objectPage")
    protected CreateAttributePage createAttributePage;

    /**
     * ----- DISC fragments -----
     */
    @FindBy(css = ".ait-header-fragment")
    protected NavigationBar discNavigation;

    @FindBy(css = ".project-list")
    protected ProjectsList discProjectsList;

    @FindBy(css = ".ait-project-detail-fragment")
    protected ProjectDetailPage projectDetailPage;

    @FindBy(css = ".l-page .overlay")
    protected DeployForm deployForm;

    @FindBy(css = ".ait-new-schedule-fragment")
    protected ScheduleForm scheduleForm;

    @FindBy(css = ".ait-schedule-detail-fragment")
    protected ScheduleDetail scheduleDetail;

    @FindBy(css = ".active .ait-process-schedule-list")
    protected SchedulesTable schedulesTable;

    @FindBy(css = ".active .broken-schedules-section .selectable-domain-table")
    protected SchedulesTable brokenSchedulesTable;

    @FindBy(css = ".ait-projects-fragment")
    protected DISCProjectsPage discProjectsPage;

    @FindBy(css = ".ait-notification-rules-fragment")
    protected NotificationRulesDialog discNotificationRules;

    @FindBy(css = ".ait-overview-fragment")
    protected OverviewStates discOverview;

    @FindBy(css = ".ait-overview-projects-fragment")
    protected OverviewProjects discOverviewProjects;

    @FindBy(css = ".adi-editor")
    protected AnalysisPage analysisPage;

    @FindBy(css = ".ember-application")
    protected UserManagementPage userManagementPage;

    @FindBy(css = "#app-dashboards")
    protected IndigoDashboardsPage indigoDashboardsPage;

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
                password = testParams.getEditorPassword();
                break;
            case VIEWER:
                user = testParams.getViewerUser();
                password = testParams.getViewerPassword();
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
            waitForElementVisible(loginFragment.getRoot());
        }
        loginFragment.login(username, password, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
    }

    public void logout() {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).click();
        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
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
            System.out.println("Going to check all dashboard & tabs");
            int dashboardsCount = dashboardsPage.getDashboardsCount();
            System.out.println("Dashboards count: " + dashboardsCount);
            for (int i = 1; i <= dashboardsCount; i++) {
                dashboardsPage.selectDashboard(i);
                sleepTightInSeconds(5);
                System.out.println("Current dashboard index: " + i);
                singleDashboardWalkthrough(validation, null, dashboardsPage.getDashboardName());
            }
        } else {
            System.out.println("Going to check expected dashboards & tabs");
            for (String dashboardName : expectedDashboardsAndTabs.keySet()) {
                int dashboardsCount = dashboardsPage.getDashboardsCount();
                assertEquals(dashboardsCount, expectedDashboardsAndTabs.size(),
                        "Number of dashboards doesn't match");
                dashboardsPage.selectDashboard(dashboardName);
                sleepTightInSeconds(5);
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
                    "Expected tab name doesn't match, index:" + i + ", " + tabLabels.get(i));
            tabs.openTab(i);
            System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar(browser);
        }
    }

    public void deleteProject(String projectId) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
        waitForProjectPageLoaded(browser);
        waitForElementVisible(projectAndUsersPage.getRoot());
        System.out.println("Going to delete project: " + projectId);
        projectAndUsersPage.deteleProject();
        System.out.println("Deleted project: " + projectId);
    }

    public void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) {
        initDashboardsPage();
        assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
        waitForDashboardPageLoaded(browser);
        sleepTightInSeconds(3);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.editDashboard();
        waitForDashboardPageLoaded(browser);
        dashboardsPage.addNewTab(tabName);
        checkRedBar(browser);
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
        waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void addReportToNewDashboard(String reportName, String dashboardName) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        dashboardEditBar.addReportToDashboard(reportName);
        // Need to sleep, if run too fast, saved dashboard will not contain the added report
        sleepTightInSeconds(3);
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);
    }

    public void createDashboard(String name) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(name);
        initDashboardsPage();
    }

    public void createDashboard(String name, boolean lock, boolean publish) {
        createDashboard(name);
        lockDashboard(lock);
        publishDashboard(publish);
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
        assertTrue(dashboardsPage.selectDashboard(name),
                String.format("Cannot select dashboard named: %s", name));
        waitForDashboardPage();
    }

    public void createReport(UiReportDefinition reportDefinition, String screenshotName) {
        initReportCreation();
        reportPage.createReport(reportDefinition);
        takeScreenshot(browser, screenshotName + "-" + reportDefinition.getName() + "-" +
                reportDefinition.getType().getName(), this.getClass());
        checkRedBar(browser);
    }

    public void initReportCreation() {
        initReportsPage();
        selectReportsDomainFolder("My Reports");
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        assertNotNull(reportPage, "Report page not initialized!");
    }

    public void verifyDashboardExport(String dashboardName, long minimalSize) {
        File pdfExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + dashboardName.replaceAll(" ", "_") + ".pdf");
        System.out.println("pdfExport = " + pdfExport);
        System.out.println(testParams.getDownloadFolder() + testParams.getFolderSeparator() + dashboardName
                + ".pdf");
        Predicate<WebDriver> exportCompleted = browser -> pdfExport.length() > minimalSize;
        Graphene.waitGui().pollingEvery(5, TimeUnit.SECONDS).until(exportCompleted);
        long fileSize = pdfExport.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is "
                + fileSize + ", but minimum " + minimalSize + " was expected");
    }

    public void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
        String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + reportName + "."
                + format.getName();
        File export = new File(fileURL);
        System.out.println("pdfExport = " + export);
        Predicate<WebDriver> exportCompleted = browser -> export.length() > minimalSize;
        Graphene.waitGui().pollingEvery(5, TimeUnit.SECONDS).until(exportCompleted);
        long fileSize = export.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is "
                + fileSize + ", but minimum " + minimalSize + " was expected");
        if (format == ExportFormat.IMAGE_PNG) {
            browser.get("file://" + fileURL);
            takeScreenshot(browser, "export-report-" + reportName, this.getClass());
            waitForElementPresent(By.xpath("//img[contains(@src, '" + testParams.getDownloadFolder() + "')]"),
                    browser);
        }
    }

    public void selectReportsDomainFolder(String folderName) {
        reportsPage.getDefaultFolders().openFolder(folderName);
        waitForReportsPageLoaded(browser);
        assertEquals(reportsPage.getSelectedFolderName(), folderName, "Selected folder name doesn't match: " +
                reportsPage.getSelectedFolderName());
    }

    public void uploadCSV(String filePath, Map<Integer, UploadColumns.OptionDataType> columnsWithExpectedType,
            String screenshotName) {
        initProjectsPage();
        initEmptyDashboardsPage();
        initUploadPage();
        upload.uploadFile(filePath);
        takeScreenshot(browser, screenshotName + "upload", this.getClass());
        UploadColumns uploadColumns = upload.getUploadColumns();
        if (columnsWithExpectedType != null) {
            takeScreenshot(browser, screenshotName + "-upload-definition-before-changing-column-type",
                    this.getClass());
            for (int columnIndex : columnsWithExpectedType.keySet()) {
                uploadColumns.setColumnType(columnIndex, columnsWithExpectedType.get(columnIndex));
            }
            takeScreenshot(browser, screenshotName + "-upload-definition-after-changing-column-type",
                    this.getClass());
        }
        takeScreenshot(browser, "upload-definition", this.getClass());
        upload.confirmloadCsv();
        waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"), browser);
        waitForDashboardPageLoaded(browser);
        takeScreenshot(browser, screenshotName + "-dashboard", this.getClass());
    }

    private void waitForDashboardPage() {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(dashboardsPage.getRoot());
    }

    public void initProjectsPage() {
        openUrl(PAGE_PROJECTS);
        waitForProjectsPageLoaded(browser);
        waitForElementVisible(projectsPage.getRoot());
    }

    public void initUploadPage() {
        openUrl(PAGE_UPLOAD);
        waitForElementVisible(upload.getRoot());
    }

    public void initEmptyDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX);
        waitForElementVisible(By.id("p-projectDashboardPage"), browser);
    }

    public void initDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX);
        waitForDashboardPage();
    }

    public void initReportsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage");
        waitForReportsPageLoaded(browser);
        waitForElementVisible(reportsPage.getRoot());
    }

    public void initAttributePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        waitForDataPageLoaded(browser);
    }

    public void initModelPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|ldmModel");
        waitForDataPageLoaded(browser);
    }

    public void initMetricPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        waitForDataPageLoaded(browser);
    }

    public void initAnalysePage() {
        // work around CL-8058
        initEmptyDashboardsPage();
        ApplicationHeaderBar.goToAnalysisPage(browser);
    }


    public void initVariablePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        waitForDataPageLoaded(browser);
    }

    public void initFactPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        waitForDataPageLoaded(browser);
    }

    public void initManagePage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|");
        waitForDataPageLoaded(browser);
    }

    public void initProjectsAndUsersPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectPage|");
        waitForProjectPageLoaded(browser);
    }

    public void initUserManagementPage() {
        openUrl(PAGE_USER_MANAGEMENT);
        waitForFragmentVisible(userManagementPage);
    }

    public void initUngroupedUsersPage() {
        openUrl(PAGE_USER_MANAGEMENT + "?groupId=GROUP_UNGROUPED");
        waitForFragmentVisible(userManagementPage);
    }

    public String getIndigoDashboardsPageUri() {
        return PAGE_INDIGO_DASHBOARDS + "#/p/" + testParams.getProjectId();
    }

    public IndigoDashboardsPage initIndigoDashboardsPage() {
        openUrl(getIndigoDashboardsPageUri());
        waitForFragmentVisible(indigoDashboardsPage);

        return indigoDashboardsPage
                .waitForDashboardLoad()
                .waitForAllKpiWidgetsLoaded();
    }

    public void initEmailSchedulesPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|emailSchedulePage");
        waitForSchedulesPageLoaded(browser);
        waitForElementNotVisible(BY_SCHEDULES_LOADING);
        waitForElementVisible(emailSchedulesPage.getRoot());
    }

    public void initDISCOverviewPage() {
        openUrl(DISC_OVERVIEW_PAGE);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver browser) {
                return !discOverview.getStateNumber(OverviewProjectStates.FAILED).isEmpty();
            }
        });
        waitForFragmentVisible(discOverviewProjects);
    }

    public void initDISCProjectsPage() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForFragmentVisible(discProjectsPage);
    }

}
