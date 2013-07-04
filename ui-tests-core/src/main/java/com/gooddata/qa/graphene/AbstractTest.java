package com.gooddata.qa.graphene;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.proxy.GrapheneProxyInstance;
import org.jboss.arquillian.testng.Arquillian;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.utils.graphene.RedBarInterceptor;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class})
public abstract class AbstractTest extends Arquillian {
	
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
	
	protected String startPage;
	
	protected static final By BY_LOGGED_USER_BUTTON = By.xpath("//div[@id='subnavigation']//button[2]");
	protected static final By BY_LOGIN_PANEL = By.xpath("//div[@id='loginPanel']");
	protected static final By BY_PANEL_ROOT = By.id("root");
	
	protected static final By BY_GP_FORM = By.tagName("form");
	protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
	protected static final By BY_GP_PRE_JSON = By.tagName("pre");
	protected static final By BY_GP_LINK = By.tagName("a");
	protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");
	
	protected static final By BY_IFRAME = By.tagName("iframe");
	
	protected static final By BY_PROJECTS_PANEL = By.id("projectsCentral");
	protected static final By BY_PROJECT_PANEL = By.id("p-projectPage");
	protected static final By BY_PROJECTS_LIST = By.id("myProjects");
	
	protected static final By BY_REPORTS_PANEL = By.id("p-domainPage");
	protected static final By BY_REPORT_PAGE = By.id("p-analysisPage");
	
	protected static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
	
	protected static final String PAGE_UI_PROJECT_PREFIX = "#s=/gdc/projects/";
	protected static final String PAGE_GDC_PROJECTS = "gdc/projects";
	protected static final String PAGE_ACCOUNT_LOGIN = "gdc/account/login";
	protected static final String PAGE_PROJECTS = "projects.html";
	
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
		
		String pageURL = getRootUrl() + (startPage != null ? startPage : "");
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
		waitForElementVisible(BY_LOGIN_PANEL);
		LoginFragment loginFragment = Graphene.createPageFragment(LoginFragment.class, browser.findElement(BY_LOGIN_PANEL));
		loginFragment.login(username, password);
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
		System.out.println("Successful login with user: " + username);
	}
	
	public void signInAtGreyPages(String username, String password) throws JSONException {
		browser.get(getRootUrl() + PAGE_ACCOUNT_LOGIN);
		waitForElementPresent(BY_GP_FORM);
		AccountLoginFragment accountLoginFragment = Graphene.createPageFragment(AccountLoginFragment.class, browser.findElement(BY_GP_FORM));
		accountLoginFragment.login(username, password);
		Screenshots.takeScreenshot(browser, "login-gp", this.getClass());
	}
	
	protected void verifyProjectDashboardTabs(int expectedNumberOfTabs, String[] expectedTabLabels) throws InterruptedException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		Thread.sleep(5000);
		DashboardsPage dashboards = Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
		DashboardTabs tabs = dashboards.getTabs();
		int numberOfTabs = tabs.getNumberOfTabs();
		System.out.println("Number of tabs fo project: " + numberOfTabs);
		Assert.assertTrue(numberOfTabs == expectedNumberOfTabs, "Expected number of dashboard tabs for project is not present");
		List<String> tabLabels = tabs.getAllTabNames();
		System.out.println("These tabs are available for selected project: " + tabLabels.toString());
		for (int i = 0; i < tabLabels.size(); i++) {
			Assert.assertEquals(tabLabels.get(i), expectedTabLabels[i], "Expected tab name doesn't not match, index:" + i + ", " + tabLabels.get(i));
			tabs.openTab(i);
			System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
			waitForDashboardPageLoaded();
			Screenshots.takeScreenshot(browser, "dashboards-tab-" + i + "-" + tabLabels.get(i), this.getClass());
			Assert.assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
			if (browser.findElements(BY_RED_BAR).size() != 0) {
				Assert.fail("RED BAR APPEARED - " + browser.findElement(BY_RED_BAR).getText());
			}
		}
	}
	
	public JSONObject loadJSON() throws JSONException {
		waitForElementPresent(BY_GP_PRE_JSON);
		return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
	}
	
	public void waitForDashboardPageLoaded() {
		waitForElementVisible(By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"));
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
	
	public void waitForEmailSchedulePageLoaded() {
		waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"));
	}
	
	public void waitForProjectsPageLoaded() {
		waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"));
	}
	
	public void waitForAnalysisPageLoaded() {
		waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"));
	}
	
	public void waitForElementVisible(By byElement) {
		Graphene.waitGui().until().element(byElement).is().visible();
	}
	
	public void waitForElementNotVisible(By byElement) {
		Graphene.waitGui().until().element(byElement).is().not().visible();
	}
	
	public void waitForElementPresent(By byElement) {
		Graphene.waitGui().until().element(byElement).is().present();
	}
	
	public void waitForElementNotPresent(By byElement) {
		Graphene.waitGui().until().element(byElement).is().not().present();
	}
}
