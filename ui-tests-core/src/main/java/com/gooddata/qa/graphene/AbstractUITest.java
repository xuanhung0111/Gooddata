package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.disc.DISCNavigation;
import com.gooddata.qa.graphene.fragments.disc.DISCProjectsList;
import com.gooddata.qa.graphene.fragments.disc.DeployForm;
import com.gooddata.qa.graphene.fragments.disc.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.ScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.SchedulesTable;
import com.gooddata.qa.graphene.fragments.manage.*;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class AbstractUITest extends AbstractGreyPageTest {

    protected By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu");
    protected By BY_LOGOUT_LINK = By.cssSelector("div.s-logout");
    protected static final By BY_PANEL_ROOT = By.id("root");
    protected static final By BY_IFRAME = By.tagName("iframe");

    protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
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
    protected MetricEditorDialog metricEditorPage;

    @FindBy(id = "factsTable")
    protected ObjectsTable factsTable;

    @FindBy(id = "p-objectPage")
    protected FactDetailPage factDetailPage;

    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectDetailPage;

    /**
     * ----- DISC fragments -----
     */
    @FindBy(css = ".nav-global")
    protected DISCNavigation discNavigation;

    @FindBy(css = ".project-list")
    protected DISCProjectsList discProjectsList;

    @FindBy(xpath = "//div[@class='row page-header']/..")
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

    /**
     * Help method which provides verification if login page is present a sign in a demo user if needed
     *
     * @param greyPages - indicator for login at greyPages/UI
     * @throws org.json.JSONException
     */
    protected void validSignInWithDemoUser(boolean greyPages) throws JSONException {
        if (greyPages) {
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        } else {
            signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    public void signInAtUI(String username, String password) {
        // this is a temporary workaround for usage of old navigation header in GoodData platform
        String navigationProperty = System.getProperty("ui.navigation");
        if (navigationProperty != null && !Boolean.valueOf(navigationProperty)) {
            BY_LOGGED_USER_BUTTON = By.xpath("//div[@id='subnavigation']//button[2]");
            BY_LOGOUT_LINK = By.cssSelector("li.s-logout");
        }
        openUrl(PAGE_LOGIN);
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(username, password, true);
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
    }

    public void logout() {
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).click();
        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
    }

    public void verifyProjectDashboardsAndTabs(boolean validation, Map<String, String[]> expectedDashboardsAndTabs,
                                               boolean openPage) throws InterruptedException {
        if (openPage) {
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        }
        waitForDashboardPageLoaded(browser);
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
            waitForDashboardPageLoaded(browser);
            Screenshots.takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar(browser);
        }
    }

    public void deleteProjectByDeleteMode(boolean successfulTest) {
        System.out.println("Delete mode is set to " + testParams.getDeleteMode().toString());
        String projectId = testParams.getProjectId();
        if (projectId != null && projectId.length() > 0) {
            switch (testParams.getDeleteMode()) {
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

    public void deleteProject(String projectId) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
        waitForProjectPageLoaded(browser);
        waitForElementVisible(projectAndUsersPage.getRoot());
        System.out.println("Going to delete project: " + projectId);
        projectAndUsersPage.deteleProject();
        System.out.println("Deleted project: " + projectId);
    }

    public void initDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(dashboardsPage.getRoot());
    }

    public void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
        initDashboardsPage();
        assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
        waitForDashboardPageLoaded(browser);
        Thread.sleep(3000);
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
        Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void initReportsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage");
        waitForReportsPageLoaded(browser);
        waitForElementVisible(reportsPage.getRoot());
    }

    public void createReport(String reportName, ReportTypes reportType, List<String> what, List<String> how, String screenshotName) throws InterruptedException {
        initReportsPage();
        selectReportsDomainFolder("My Reports");
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        assertNotNull(reportPage, "Report page not initialized!");
        reportPage.createReport(reportName, reportType, what, how);
        Screenshots.takeScreenshot(browser, screenshotName + "-" + reportName + "-" + reportType.getName(), this.getClass());
        checkRedBar(browser);
    }

    public void verifyDashboardExport(String dashboardName, long minimalSize) {
        File pdfExport = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + dashboardName + ".pdf");
        System.out.println("pdfExport = " + pdfExport);
        System.out.println(testParams.getDownloadFolder() + testParams.getFolderSeparator() + dashboardName + ".pdf");
        long fileSize = pdfExport.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
    }

    public void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
        String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + reportName + "." + format.getName();
        File export = new File(fileURL);
        long fileSize = export.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
        if (format == ExportFormat.IMAGE_PNG) {
            browser.get("file://" + fileURL);
            Screenshots.takeScreenshot(browser, "export-report-" + reportName, this.getClass());
            waitForElementPresent(By.xpath("//img[contains(@src, '" + testParams.getDownloadFolder() + "')]"), browser);
        }
    }

    public void selectReportsDomainFolder(String folderName) {
        reportsPage.getDefaultFolders().openFolder(folderName);
        waitForReportsPageLoaded(browser);
        assertEquals(reportsPage.getSelectedFolderName(), folderName, "Selected folder name doesn't match: " +
                reportsPage.getSelectedFolderName());
    }

    public void uploadCSV(String filePath, Map<Integer, UploadColumns.OptionDataType> columnsWithExpectedType, String screenshotName) throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        Thread.sleep(3000);
        openUrl(PAGE_UPLOAD);
        waitForElementVisible(upload.getRoot());
        upload.uploadFile(filePath);
        Screenshots.takeScreenshot(browser, screenshotName + "upload", this.getClass());
        UploadColumns uploadColumns = upload.getUploadColumns();
        if (columnsWithExpectedType != null) {
            Screenshots.takeScreenshot(browser, screenshotName + "-upload-definition-before-changing-column-type", this.getClass());
            for (int columnIndex : columnsWithExpectedType.keySet()) {
                uploadColumns.setColumnType(columnIndex, columnsWithExpectedType.get(columnIndex));
            }
            Screenshots.takeScreenshot(browser, screenshotName + "-upload-definition-after-changing-column-type", this.getClass());
        }
        Screenshots.takeScreenshot(browser, "upload-definition", this.getClass());
        upload.confirmloadCsv();
        waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"), browser);
        waitForDashboardPageLoaded(browser);
        Screenshots.takeScreenshot(browser, screenshotName + "-dashboard", this.getClass());
    }
}
