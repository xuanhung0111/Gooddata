package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

import static org.testng.Assert.*;


@Test(groups = { "connectors", "googleAnalytics" }, description = "Checklist tests for Google Analytics connector in GD platform")
public class GoogleAnalyticsCheckTest extends AbstractConnectorsCheckTest {
	
	private String googleAnalyticsUser;
	private String googleAnalyticsUserPassword;
	private String googleAnalyticsAccount;
	
	private static final By BY_H3_BEFORE_GRANT_CONFIG = By.xpath("//h3[text()='Welcome to your 30-day free trial of GoodData for Google Analytics!']");
	private static final By BY_BUTTON_GRANT_REDIRECT = By.xpath("//div[@id='content']/p/button");
	private static final By BY_SIGN_IN_BOX = By.xpath("//div[@class='signin-box']");
	private static final By BY_INPUT_EMAIL = By.name("Email");
	private static final By BY_INPUT_PASSWORD = By.name("Passwd");
	private static final By BY_BUTTON_SIGN_IN = By.name("signIn");
	private static final By BY_BUTTON_ALLOW = By.name("allow");
	private static final By BY_SELECT_ACCOUNT = By.name("account");
	private static final By BY_IMPORT_BUTTON = By.xpath("//button[@id='cbtn']");
	private static final String XPATH_OPTION_ACCOUNT = "//option[contains(text(), '${account}')]";
	private static final By BY_DIV_SYNCHRONIZATION_PROGRESS = By.xpath("//div[@class='connectorHeader']/span[text()='Please be patient, your data is now being loaded.']");
	
	@BeforeClass
	public void setCheckLimits() {
		projectCreateCheckIterations = 120;
		integrationProcessCheckLimit = 720;
	}
	
	@BeforeClass
	public void loadRequiredProperties() {
		googleAnalyticsUser = loadProperty("connectors.googleAnalytics.user");
		googleAnalyticsUserPassword = loadProperty("connectors.googleAnalytics.userPassword");
		googleAnalyticsAccount = loadProperty("connectors.googleAnalytics.account");
		
		connectorType = Connectors.GOOGLE_ANALYTICS;
		expectedDashboardTabs = new String[]{
				"Summaries", "Content", "Campaigns", "Browsers"
		};
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testConnectorIntegrationResource" })
	public void testGoogleAnalyticsIntegrationConfiguration() throws InterruptedException, JSONException {
		// ga specific configuration
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId);
		browser.switchTo().frame(waitForElementVisible(BY_IFRAME));
		waitForElementVisible(BY_H3_BEFORE_GRANT_CONFIG);
		waitForElementVisible(BY_BUTTON_GRANT_REDIRECT);
		//Store the current window handle
		String winHandleOrig = browser.getWindowHandle();
		browser.findElement(BY_BUTTON_GRANT_REDIRECT).click();
		//Switch to new window opened
		for (String winHandle : browser.getWindowHandles()){
		    if (!winHandle.equals(winHandleOrig)) browser.switchTo().window(winHandle);
		}
		waitForElementVisible(BY_SIGN_IN_BOX);
		waitForElementVisible(BY_INPUT_EMAIL).sendKeys(googleAnalyticsUser);
		waitForElementVisible(BY_INPUT_PASSWORD).sendKeys(googleAnalyticsUserPassword);
		Graphene.guardHttp(browser.findElement(BY_BUTTON_SIGN_IN)).click();
		waitForElementVisible(BY_BUTTON_ALLOW).click();
		//Switch back to original browser (first window)
		browser.switchTo().window(winHandleOrig);
		browser.switchTo().frame(waitForElementVisible(BY_IFRAME));
		waitForElementVisible(BY_SELECT_ACCOUNT).findElement(By.xpath(XPATH_OPTION_ACCOUNT.replace("${account}", googleAnalyticsAccount))).click();
		Graphene.guardHttp(waitForElementVisible(BY_IMPORT_BUTTON)).click();
		waitForElementVisible(BY_DIV_SYNCHRONIZATION_PROGRESS);
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testGoogleAnalyticsIntegrationConfiguration" })
	public void testGoogleAnalyticsIntegration() throws InterruptedException, JSONException {
		// process is scheduled automatically - check status
		openUrl(getProcessesUri());
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 1, "Integration process wasn't started...");
		Graphene.guardHttp(waitForElementVisible(BY_GP_LINK)).click();
		waitForIntegrationProcessSynchronized(browser, integrationProcessCheckLimit);
	}
}
