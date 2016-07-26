package com.gooddata.qa.graphene.connectors.coupa;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.connectors.AbstractConnectorsCheckTest;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.greypages.connectors.CoupaInstanceFragment;
import com.google.common.base.Predicate;

@Test(groups = {"connectors", "coupa"}, description = "Checklist tests for Coupa connector in GD platform")
public class CoupaCheckTest extends AbstractConnectorsCheckTest {

    private static final By BY_INPUT_TIMEZONE = By.name("timeZone");
    private static final By BY_GP_LINK_INSTANCES = By.partialLinkText("instances");

    private static final By BY_DIV_BEFORE_CONFIG = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()=\"Oops, there's information missing.\"]");
    private static final By BY_DIV_SYNCHRONIZATION_PROGRESS = By.xpath("//div[contains(@class,'yui3-coupa-ftue-content')]//span[text()='Your Coupa Optimizer project is almost ready!']");

    private static final String COUPA_INTEGRATION_TIMEZONE = "Europe/Prague";

    private String coupaInstanceApiUrl;
    private String coupaInstanceApiKey;
    private String coupaUploadUser;
    private String coupaUploadUserPassword;

    @FindBy(tagName = "form")
    private CoupaInstanceFragment coupaInstance;

    @BeforeClass
    public void loadRequiredProperties() {
        coupaInstanceApiUrl = testParams.loadProperty("connectors.coupa.instance.apiUrl");
        coupaInstanceApiKey = testParams.loadProperty("connectors.coupa.instance.apiKey");
        coupaUploadUser = testParams.loadProperty("connectors.coupa.uploadUser");
        coupaUploadUserPassword = testParams.loadProperty("connectors.coupa.uploadUserPassword");

        connectorType = Connectors.COUPA;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Coupa Spend Optimizer", new String[]{
                "KPIs", "Requisitions", "Approvals", "Purchase Orders", "Suppliers", "Invoices", "Commodities",
                "Contracts", "Expenses", "Budgets", "All Spend"
        });

        projectCreateCheckIterations = 120;
        integrationProcessCheckLimit = 720;
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testCoupaIntegrationConfiguration() throws JSONException {
        if (testParams.isReuseProject()) return;
        // verify empty Coupa dashboard
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId());
        waitForElementVisible(BY_IFRAME, browser);
        browser.switchTo().frame(browser.findElement(BY_IFRAME));
        waitForElementVisible(BY_DIV_BEFORE_CONFIG, browser);

        // go to page with integration settings
        openUrl(getIntegrationUri());
        gotoIntegrationSettings();

        // coupa specific configuration
        waitForElementVisible(BY_INPUT_TIMEZONE, browser).sendKeys(COUPA_INTEGRATION_TIMEZONE);
        Graphene.guardHttp(browser.findElement(BY_GP_BUTTON_SUBMIT)).click();
        Graphene.waitGui().until().element(BY_INPUT_TIMEZONE).value().equalTo(COUPA_INTEGRATION_TIMEZONE);
        Graphene.guardHttp(waitForElementVisible(BY_GP_LINK_INSTANCES, browser)).click();
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("coupaInstances").getJSONArray("items").length() == 0,
                "There are no coupa instances for new project yet");

        // create coupa instance
        waitForElementPresent(coupaInstance.getRoot());
        coupaInstance.createCoupaInstance(Connectors.COUPA.getConnectorId(), coupaInstanceApiUrl, coupaInstanceApiKey);
        verifyCoupaInstance(coupaInstanceApiUrl);

        // verify progress on Coupa dashboard
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId());
        waitForElementVisible(BY_IFRAME, browser);
        browser.switchTo().frame(browser.findElement(BY_IFRAME));
        waitForElementVisible(BY_DIV_SYNCHRONIZATION_PROGRESS, browser);
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testCoupaIntegrationConfiguration"})
    public void testCoupaIntegrationWithUploadUser() throws JSONException {
        // pardot specific configuration of API Url (with specific upload user)
        signInAtGreyPages(coupaUploadUser, coupaUploadUserPassword);
        testConnectorIntegrationResource();
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testCoupaIntegrationWithUploadUser"})
    public void testCoupaIntegration() throws JSONException {
        // sign in back with demo user
        signIn(true, UserRoles.ADMIN);
        // process schedule
        scheduleIntegrationProcessOrUseExisting(integrationProcessCheckLimit);
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testCoupaIntegration"})
    public void testIncrementalSynchronization() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    /**
     * Verifies if a created Coupa instance with the given api url exists on /instances endpoint.
     * This method refreshes /instances page until the instance with api url is not present.
     * If the refreshing exceeds given time period, the verification fails.
     *
     * @param apiUrl api url of the coupa instance to be verified
     */
    private void verifyCoupaInstance(final String apiUrl) {
        openUrl(getIntegrationUri() + "/config/settings/instances");

        final Predicate<WebDriver> predicate = browser -> {
            browser.navigate().refresh();
            System.out.println("Checking existence of instance with API URL: " + apiUrl);
            return isInstanceCreated(apiUrl);
        };

        Graphene.waitGui()
                .withTimeout(5, TimeUnit.MINUTES)
                .withMessage("Coupa instance with API URL: '" + apiUrl + "' wasn't created.")
                .pollingEvery(10, TimeUnit.SECONDS)
                .until(predicate);

        takeScreenshot(browser, "created-instance-exists", getClass());
        System.out.println("Coupa instance with API URL: " + apiUrl + " was created");
    }

    private boolean isInstanceCreated(final String apiUrl) {
        notEmpty(apiUrl, "apiUrl cannot be empty!");

        try {
            final JSONObject json = loadJSON();
            final JSONArray coupaInstances = json.getJSONObject("coupaInstances").getJSONArray("items");

            for (int index = 0; index < coupaInstances.length(); index++) {
                if (apiUrl.equals(coupaInstances.getJSONObject(index).getJSONObject("coupaInstance").getString("apiUrl"))) {
                    return true;
                }
            }
        } catch (JSONException e) {
            fail("Reading JSON structure of Coupa instances endpoint failed.", e);
        }

        return false;
    }
}
