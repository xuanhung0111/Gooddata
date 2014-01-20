package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

import static org.testng.Assert.*;

@Test(groups = { "connectors", "brightidea" }, description = "Checklist tests for Brightidea connector in GD platform")
public class BrightideaCheckTest extends AbstractConnectorsCheckTest {
	
	private String brightideaApiKey;
	private String brightideaAffiliateId;
	private String brightideaHostname;
	
	private static final String BRIGHTIDEA_TIMEZONE = "Europe/Prague";
	
	private static final By BY_INPUT_API_KEY = By.xpath("//div/label[text()='API key']/../input");
	private static final By BY_INPUT_AFFILIATE_ID = By.xpath("//div/label[text()='Affiliate ID']/../input");
	private static final By BY_INPUT_HOSTNAME = By.xpath("//div/label[text()='Hostname']/../input");
	private static final By BY_SELECT_TIMEZONE = By.xpath("//div/label[text()='Timezone']/../select");
	private static final By BY_SELECT_TIMEZONE_OPTION = By.xpath("//option[@value='" + BRIGHTIDEA_TIMEZONE + "']");
	private static final By BY_FINISH_BUTTON = By.xpath("//button[text()='Finish']");
	
	private static final By BY_SPAN_WELCOME_BEFORE_CONFIG = By.xpath("//span[text()='Welcome to GoodData for Brightidea!']");
	private static final By BY_SPAN_SYNCHRONIZATION_PROGRESS = By.xpath("//span[text()='Almost There!']");
	
	@BeforeClass
	public void setCheckLimits() {
		projectCreateCheckIterations = 120;
		integrationProcessCheckLimit = 720;
	}
	
	@BeforeClass
	public void loadRequiredProperties() {
		brightideaApiKey = loadProperty("connectors.brightidea.apiKey");
		brightideaAffiliateId = loadProperty("connectors.brightidea.affiliateId");
		brightideaHostname = loadProperty("connectors.brightidea.hostname");
		
		connectorType = Connectors.BRIGHTIDEA;
		expectedDashboardTabs = new String[]{
			"WebStorms", "Ideas", "Users", "Switchboard", "Pipeline", "Headlines", "Learn More"
		};
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testConnectorIntegrationResource" })
	public void testBrightideaIntegrationConfiguration() throws InterruptedException, JSONException {
		// Brightidea specific configuration of integration (tfue page)
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId);
		waitForElementVisible(BY_IFRAME);
		browser.switchTo().frame(browser.findElement(BY_IFRAME));
		waitForElementVisible(BY_SPAN_WELCOME_BEFORE_CONFIG);
		waitForElementVisible(BY_INPUT_API_KEY).sendKeys(brightideaApiKey);
		waitForElementVisible(BY_INPUT_AFFILIATE_ID).sendKeys(brightideaAffiliateId);
		waitForElementVisible(BY_INPUT_HOSTNAME).clear();
		waitForElementVisible(BY_INPUT_HOSTNAME).sendKeys(brightideaHostname);
		WebElement select = waitForElementVisible(BY_SELECT_TIMEZONE);
		select.findElement(BY_SELECT_TIMEZONE_OPTION).click();
		waitForElementVisible(BY_FINISH_BUTTON).click();
		waitForElementVisible(BY_SPAN_SYNCHRONIZATION_PROGRESS);
	}
	
	@Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testBrightideaIntegrationConfiguration" })
	public void testBrightideaIntegration() throws InterruptedException, JSONException {
		// process is scheduled automatically - check status
		openUrl(getProcessesUri());
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 1, "Integration process wasn't started...");
		waitForElementVisible(BY_GP_LINK);
		Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
		waitForIntegrationProcessSynchronized(browser, integrationProcessCheckLimit);
	}
}
