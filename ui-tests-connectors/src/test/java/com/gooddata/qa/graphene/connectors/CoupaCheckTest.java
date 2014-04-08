package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.greypages.connectors.CoupaInstanceFragment;

import java.util.HashMap;

import static org.testng.Assert.*;

@Test(groups = {"connectors", "coupa"}, description = "Checklist tests for Coupa connector in GD platform")
public class CoupaCheckTest extends AbstractConnectorsCheckTest {

    private static final By BY_INPUT_TIMEZONE = By.name("timeZone");
    private static final By BY_GP_LINK_INSTANCES = By.partialLinkText("instances");

    private static final By BY_DIV_BEFORE_CONFIG = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()=\"Oops, there's information missing.\"]");
    private static final By BY_DIV_SYNCHRONIZATION_PROGRESS = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()='Your Coupa Optimizer project is almost ready!']");

    private static final String COUPA_INTEGRATION_TIMEZONE = "Europe/Prague";

    private String coupaInstanceApiUrl;
    private String coupaInstanceApiKey;

    @FindBy(tagName = "form")
    private CoupaInstanceFragment coupaInstance;

    @BeforeClass
    public void loadRequiredProperties() {
        coupaInstanceApiUrl = loadProperty("connectors.coupa.instance.apiUrl");
        coupaInstanceApiKey = loadProperty("connectors.coupa.instance.apiKey");

        connectorType = Connectors.COUPA;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Coupa Spend Optimizer", new String[]{
                "KPIs", "Requisitions", "Approvals", "Purchase Orders", "Suppliers", "Invoices", "Commodities", "Contracts", "Expenses", "Budgets", "All Spend"
        });

        projectCreateCheckIterations = 120;
        integrationProcessCheckLimit = 720;
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testCoupaIntegrationConfiguration() throws InterruptedException, JSONException {
        // verify empty Coupa dashboard
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId);
        waitForElementVisible(BY_IFRAME);
        browser.switchTo().frame(browser.findElement(BY_IFRAME));
        waitForElementVisible(BY_DIV_BEFORE_CONFIG);

        // go to page with integration settings
        browser.get(getRootUrl() + getIntegrationUri());
        gotoIntegrationSettings();

        // coupa specific configuration
        waitForElementVisible(BY_INPUT_TIMEZONE).sendKeys(COUPA_INTEGRATION_TIMEZONE);
        Graphene.guardHttp(browser.findElement(BY_GP_BUTTON_SUBMIT)).click();
        Graphene.waitGui().until().element(BY_INPUT_TIMEZONE).value().equalTo(COUPA_INTEGRATION_TIMEZONE);
        Graphene.guardHttp(waitForElementVisible(BY_GP_LINK_INSTANCES)).click();
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("coupaInstances").getJSONArray("items").length() == 0, "There are no coupa instances for new project yet");

        // create coupa instance
        waitForElementPresent(coupaInstance.getRoot());
        coupaInstance.createCoupaInstance(Connectors.COUPA.getConnectorId(), coupaInstanceApiUrl, coupaInstanceApiKey);

        // verify progress on Coupa dashboard
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId);
        waitForElementVisible(BY_IFRAME);
        browser.switchTo().frame(browser.findElement(BY_IFRAME));
        waitForElementVisible(BY_DIV_SYNCHRONIZATION_PROGRESS);
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = {"testCoupaIntegrationConfiguration"})
    public void testCoupaIntegration() throws InterruptedException, JSONException {
        // process schedule
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }
}
