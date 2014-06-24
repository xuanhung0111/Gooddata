package com.gooddata.qa.graphene.common;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.manage.*;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class UITestUtils extends CommonUtils {

    public UITestUtils(WebDriver browser, CheckUtils checkUtils, TestParameters testParameters) {
        super(browser, checkUtils, testParameters);
    }

    public By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu");
    public static final By BY_LOGOUT_LINK = By.cssSelector("div.s-logout");
    public static final By BY_PANEL_ROOT = By.id("root");
    public static final By BY_IFRAME = By.tagName("iframe");

    public static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
    public static final String PAGE_PROJECTS = "projects.html";
    public static final String PAGE_UPLOAD = "upload.html";
    public static final String PAGE_LOGIN = "account.html#/login";

    /**
     * ----- UI fragmnets -----
     */

    @FindBy(css = ".s-loginPage")
    public LoginFragment loginFragment;

    @FindBy(id = "root")
    public DashboardsPage dashboardsPage;

    @FindBy(id = "p-domainPage")
    public ReportsPage reportsPage;

    @FindBy(id = "p-analysisPage")
    public ReportPage reportPage;

    @FindBy(id = "p-projectPage")
    public ProjectAndUsersPage projectAndUsersPage;

    @FindBy(id = "p-emailSchedulePage")
    public EmailSchedulePage emailSchedulesPage;

    @FindBy(id = "projectsCentral")
    public ProjectsPage projectsPage;

    @FindBy(css = ".l-primary")
    public UploadFragment upload;

    @FindBy(id = "p-dataPage")
    public DataPage dataPage;

    @FindBy(id = "attributesTable")
    public ObjectsTable attributesTable;

    @FindBy(id = "uploadsTable")
    public ObjectsTable datasetsTable;

    @FindBy(id = "variablesTable")
    public ObjectsTable variablesTable;

    @FindBy(id = "p-dataPage")
    public AttributePage attributePage;

    @FindBy(id = "p-objectPage")
    public AttributeDetailPage attributeDetailPage;

    @FindBy(id = "p-objectPage")
    public DatasetDetailPage datasetDetailPage;

    @FindBy(id = "p-dataPage")
    public VariablesPage variablePage;

    @FindBy(id = "p-objectPage")
    public VariableDetailPage variableDetailPage;

    @FindBy(id = "metricsTable")
    public ObjectsTable metricsTable;

    @FindBy(id = "p-objectPage")
    public MetricDetailsPage metricDetailPage;

    @FindBy(id = "new")
    public MetricEditorDialog metricEditorPage;

    @FindBy(id = "factsTable")
    public ObjectsTable factsTable;

    @FindBy(id = "p-objectPage")
    public FactDetailPage factDetailPage;

    @FindBy(id = "p-objectPage")
    public ObjectPropertiesPage objectDetailPage;

    public void signInAtUI(String username, String password) {
        openUrl(PAGE_LOGIN);
        checkUtils.waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(username, password, true);
        checkUtils.waitForElementVisible(BY_LOGGED_USER_BUTTON);
        Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + username);
    }

    public void logout() {
        checkUtils.waitForElementVisible(BY_LOGGED_USER_BUTTON).click();
        checkUtils.waitForElementVisible(BY_LOGOUT_LINK).click();
        checkUtils.waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
    }

    public void verifyProjectDashboardsAndTabs(boolean validation, Map<String, String[]> expectedDashboardsAndTabs,
                                               boolean openPage) throws InterruptedException {
        if (openPage) {
            openUrl(PAGE_UI_PROJECT_PREFIX + testParameters.getProjectId() + "|projectDashboardPage");
            checkUtils.waitForElementVisible(BY_LOGGED_USER_BUTTON);
        }
        checkUtils.waitForDashboardPageLoaded();
        Thread.sleep(5000);
        checkUtils.waitForElementVisible(dashboardsPage.getRoot());
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
            checkUtils.waitForDashboardPageLoaded();
            Screenshots.takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkUtils.checkRedBar();
        }
    }

    public void deleteProjectByDeleteMode(boolean successfulTest) {
        System.out.println("Delete mode is set to " + testParameters.getDeleteMode().toString());
        String projectId = testParameters.getProjectId();
        if (projectId != null && projectId.length() > 0) {
            switch (testParameters.getDeleteMode()) {
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
        checkUtils.waitForProjectPageLoaded();
        checkUtils.waitForElementVisible(projectAndUsersPage.getRoot());
        System.out.println("Going to delete project: " + projectId);
        projectAndUsersPage.deteleProject();
        System.out.println("Deleted project: " + projectId);
    }

    public void initDashboardsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParameters.getProjectId() + "|projectDashboardPage");
        checkUtils.waitForElementVisible(BY_LOGGED_USER_BUTTON);
        checkUtils.waitForDashboardPageLoaded();
        checkUtils.waitForElementVisible(dashboardsPage.getRoot());
    }

    public void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
        initDashboardsPage();
        assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
        checkUtils.waitForDashboardPageLoaded();
        Thread.sleep(3000);
        DashboardTabs tabs = dashboardsPage.getTabs();
        int tabsCount = tabs.getNumberOfTabs();
        dashboardsPage.editDashboard();
        checkUtils.waitForDashboardPageLoaded();
        dashboardsPage.addNewTab(tabName);
        checkUtils.checkRedBar();
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        checkUtils.waitForDashboardPageLoaded();
        checkUtils.waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
        assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
        assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
        assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
        Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
    }

    public void initReportsPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParameters.getProjectId() + "|domainPage");
        checkUtils.waitForReportsPageLoaded();
        checkUtils.waitForElementVisible(reportsPage.getRoot());
    }

    public void createReport(String reportName, ReportTypes reportType, List<String> what, List<String> how, String screenshotName) throws InterruptedException {
        initReportsPage();
        selectReportsDomainFolder("My Reports");
        reportsPage.startCreateReport();
        checkUtils.waitForAnalysisPageLoaded();
        checkUtils.waitForElementVisible(reportPage.getRoot());
        assertNotNull(reportPage, "Report page not initialized!");
        reportPage.createReport(reportName, reportType, what, how);
        Screenshots.takeScreenshot(browser, screenshotName + "-" + reportName + "-" + reportType.getName(), this.getClass());
        checkUtils.checkRedBar();
    }

    public void verifyDashboardExport(String dashboardName, long minimalSize) {
        File pdfExport = new File(testParameters.getDownloadFolder() + "/" + dashboardName + ".pdf");
        System.out.println("pdfExport = " + pdfExport);
        System.out.println(testParameters.getDownloadFolder() + "/" + dashboardName + ".pdf");
        long fileSize = pdfExport.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
    }

    public void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
        String fileURL = testParameters.getDownloadFolder() + "/" + reportName + "." + format.getName();
        File export = new File(fileURL);
        long fileSize = export.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
        if (format == ExportFormat.IMAGE_PNG) {
            browser.get("file://" + fileURL);
            Screenshots.takeScreenshot(browser, "export-report-" + reportName, this.getClass());
            checkUtils.waitForElementPresent(By.xpath("//img[contains(@src, '" + testParameters.getDownloadFolder() + "')]"));
        }
    }

    public void selectReportsDomainFolder(String folderName) {
        reportsPage.getDefaultFolders().openFolder(folderName);
        checkUtils.waitForReportsPageLoaded();
        assertEquals(reportsPage.getSelectedFolderName(), folderName, "Selected folder name doesn't match: " +
                reportsPage.getSelectedFolderName());
    }

    public void uploadCSV(String filePath, Map<Integer, UploadColumns.OptionDataType> columnsWithExpectedType, String screenshotName) throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParameters.getProjectId() + "|projectDashboardPage");
        Thread.sleep(3000);
        openUrl(PAGE_UPLOAD);
        checkUtils.waitForElementVisible(upload.getRoot());
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
        checkUtils.waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"));
        checkUtils.waitForDashboardPageLoaded();
        Screenshots.takeScreenshot(browser, screenshotName + "-dashboard", this.getClass());
    }
}