package com.gooddata.qa.graphene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.gooddata.qa.graphene.fragments.greypages.md.ValidateFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.Validation;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;

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
	
	protected String downloadFolder;
	
	protected String startPage;
	
	protected DeleteMode deleteMode = DeleteMode.DELETE_IF_SUCCESSFUL;
	protected boolean successfulTest = false;
	
	protected static final By BY_LOGGED_USER_BUTTON = By.xpath("//div[@id='subnavigation']//button[2]");
	protected static final By BY_LOGOUT_LINK = By.xpath("//a[@class='s-logout']");
	protected static final By BY_PANEL_ROOT = By.id("root");
	
	protected static final By BY_GP_FORM = By.tagName("form");
	protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
	protected static final By BY_GP_PRE_JSON = By.tagName("pre");
	protected static final By BY_GP_LINK = By.tagName("a");
	protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");
	
	protected static final By BY_IFRAME = By.tagName("iframe");
	
	protected static final By BY_PROJECTS_PANEL = By.id("projectsCentral");
	protected static final By BY_PROJECT_PAGE_PANEL = By.id("p-projectPage");
	protected static final By BY_SCHEDULES_PAGE_PANEL = By.id("p-emailSchedulePage");
	protected static final By BY_PROJECTS_LIST = By.id("myProjects");
	
	protected static final By BY_REPORTS_PANEL = By.id("p-domainPage");
	protected static final By BY_REPORT_PAGE = By.id("p-analysisPage");
	
	protected static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
	protected static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");
	
	protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
	protected static final String PAGE_GDC = "gdc";
	protected static final String PAGE_GDC_MD = PAGE_GDC + "/md";
	protected static final String PAGE_GDC_PROJECTS = PAGE_GDC + "/projects";
	protected static final String PAGE_ACCOUNT_LOGIN = PAGE_GDC + "/account/login";
	protected static final String PAGE_PROJECTS = "projects.html";
	protected static final String PAGE_UPLOAD = "upload.html";
	
	/** ----- UI fragmnets ----- */
	
	@FindBy(css="#loginPanel")
	protected LoginFragment loginFragment;
	
	@FindBy(id="root")
	protected DashboardsPage dashboardsPage;
	
	@FindBy(id="p-domainPage")
	protected ReportsPage reportsPage;
	
	@FindBy(id="p-analysisPage")
	protected ReportPage reportPage;
	
	@FindBy(id="p-projectPage")
	protected ProjectAndUsersPage projectAndUsersPage;
	
	@FindBy(id="p-emailSchedulePage")
	protected EmailSchedulePage emailSchedulesPage;
	
	@FindBy(id="projectsCentral")
	protected ProjectsPage projectsPage;
	
	@FindBy(css=".l-primary")
	protected UploadFragment upload;
	
	/** ----- Grey pages fragmnets ----- */
	
	@FindBy(tagName="form")
	protected AccountLoginFragment gpLoginFragment;
	
	@FindBy(tagName="form")
	protected ProjectFragment gpProject;

    @FindBy(tagName="form")
    protected ValidateFragment validateFragment;
	/** ---------- */
	
	@BeforeClass
	public void loadProperties() {
		propertiesPath = System.getProperty("propertiesPath", System.getProperty("user.dir") + "/../ui-tests-core/src/test/resources/variables-env-test.properties");
		
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
		System.out.println("Basic properties initialized, host: " + host + ", user: " + user);
		
		deleteMode = DeleteMode.getModeByName(loadProperty("deleteMode"));
		
		downloadFolder = loadProperty("browserDownloadFolder");
	}
	
	/**
	 * Method to return property value from:
	 *  - System.getProperty(key) if present (good for CI integration)
	 *  - properties file defined on path "propertiesPath" (default properties are in variables-env-test.properties)
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
		waitForElementVisible(loginFragment.getRoot());
		loginFragment.login(username, password);
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
        Screenshots.takeScreenshot(browser, projectId+"-validation", this.getClass());
        return statusReturning;
    }

    public String validateProjectPartial(Validation... validationOptions) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + projectId + "/validate");
        waitForElementPresent(validateFragment.getRoot());
        String statusReturning = validateFragment.validateOnly(validationOptions);
        Screenshots.takeScreenshot(browser, projectId+"-validation-partial", this.getClass());
        return statusReturning;
    }

    public void logout() {
		waitForElementVisible(BY_LOGGED_USER_BUTTON).click();
		waitForElementVisible(BY_LOGOUT_LINK).click();
		waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
	}
	
	protected void verifyProjectDashboardTabs(boolean validation, int expectedNumberOfTabs, String[] expectedTabLabels, boolean openPage) throws InterruptedException {
		if (openPage) {
			browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
			waitForElementVisible(BY_LOGGED_USER_BUTTON);
		}
		waitForDashboardPageLoaded();
		Thread.sleep(5000);
		waitForElementVisible(dashboardsPage.getRoot());
		DashboardTabs tabs = dashboardsPage.getTabs();
		int numberOfTabs = tabs.getNumberOfTabs();
		System.out.println("Number of tabs for project: " + numberOfTabs);
		if (validation) Assert.assertTrue(numberOfTabs == expectedNumberOfTabs, "Expected number of dashboard tabs for project is not present");
		List<String> tabLabels = tabs.getAllTabNames();
		System.out.println("These tabs are available for selected project: " + tabLabels.toString());
		for (int i = 0; i < tabLabels.size(); i++) {
			if (validation) Assert.assertEquals(tabLabels.get(i), expectedTabLabels[i], "Expected tab name doesn't not match, index:" + i + ", " + tabLabels.get(i));
			tabs.openTab(i);
			System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
			waitForDashboardPageLoaded();
			Screenshots.takeScreenshot(browser, "dashboards-tab-" + i + "-" + tabLabels.get(i), this.getClass());
			Assert.assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
			checkRedBar();
		}
	}
	
	protected void checkRedBar() {
		if (browser.findElements(BY_RED_BAR).size() != 0) {
			Assert.fail("RED BAR APPEARED - " + browser.findElement(BY_RED_BAR).getText());
		}
		//this kind of error appeared for the first time in geo chart
		if (browser.findElements(BY_REPORT_ERROR).size() != 0 && browser.findElement(BY_REPORT_ERROR).isDisplayed()) {
			Assert.fail("Report error APPEARED - " + browser.findElement(BY_REPORT_ERROR).getText());
		}
	}
	
	protected void verifyDashboardExport(String dashboardName, long minimalSize) {
		File pdfExport = new File(downloadFolder + "/" + dashboardName + ".pdf");
		long fileSize = pdfExport.length();
		System.out.println("File size: " + fileSize);
		Assert.assertTrue(fileSize > minimalSize, "Export is probably invalid, check the PDF manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
	}
	
	protected void verifyReportExport(ExportFormat format, String reportName, long minimalSize) {
		String fileURL = downloadFolder + "/" + reportName + "." + format.getName();
		File export = new File(fileURL);
		long fileSize = export.length();
		System.out.println("File size: " + fileSize);
		Assert.assertTrue(fileSize > minimalSize, "Export is probably invalid, check the file manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
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
		Assert.assertTrue(dashboardsPage.selectDashboard(dashboardName), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		DashboardTabs tabs = dashboardsPage.getTabs();
		int tabsCount = tabs.getNumberOfTabs();
		dashboardsPage.editDashboard();
		waitForDashboardPageLoaded();
		dashboardsPage.addNewTab(tabName);
		checkRedBar();
		Assert.assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present");
		Assert.assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected");
		Assert.assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label");
		dashboardsPage.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboardsPage.getDashboardEditBar().getRoot());
		Assert.assertEquals(tabs.getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
		Assert.assertTrue(tabs.isTabSelected(tabsCount), "New tab is not selected after Save");
		Assert.assertEquals(tabs.getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
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
