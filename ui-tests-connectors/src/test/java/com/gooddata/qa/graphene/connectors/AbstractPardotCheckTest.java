package com.gooddata.qa.graphene.connectors;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.*;

public abstract class AbstractPardotCheckTest extends AbstractConnectorsCheckTest {

    protected static final By BY_INPUT_PARDOT_ACCOUNT_ID = By.name("accountId");

    protected String pardotAccountId;
    protected String pardotUploadUser;
    protected String pardotUploadUserPassword;

    @BeforeClass
    public void loadRequiredProperties() {
        pardotAccountId = testParams.loadProperty("connectors.pardot.accountId");
        pardotUploadUser = testParams.loadProperty("connectors.pardot.uploadUser");
        pardotUploadUserPassword = testParams.loadProperty("connectors.pardot.uploadUserPassword");

        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Pardot Analytics", new String[]{
                "Marketing KPIs", "Contribution", "Prospects", "Opportunities", "and more"
        });
        expectedDashboardsAndTabs.put("Prospects Only", new String[]{
                "Marketing KPIs", "Prospects", "Leaderboard", "and more"
        });

        projectCreateCheckIterations = 120;
        integrationProcessCheckLimit = 240;
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testPardotIntegrationConfiguration() throws InterruptedException, JSONException {
        openUrl(getIntegrationUri());
        // go to page with integration settings
        String settingsUrl = gotoIntegrationSettings();

        // pardot specific configuration of API Url (with specific upload user)
        greyPageUtils.signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
        browser.get(settingsUrl);
        waitForElementVisible(BY_INPUT_PARDOT_ACCOUNT_ID).sendKeys(pardotAccountId);
        Graphene.guardHttp(waitForElementVisible(greyPageUtils.BY_GP_BUTTON_SUBMIT)).click();
        JSONObject json = greyPageUtils.loadJSON();
        assertEquals(json.getJSONObject("settings").getString("accountId"), pardotAccountId,
                "Pardot accountId was not set to expected value");
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testPardotIntegrationConfiguration"})
    public void testPardotIntegration() throws InterruptedException, JSONException {
        // sign in back with demo user
        validSignInWithDemoUser(true);
        // process schedule
        scheduleIntegrationProcess(integrationProcessCheckLimit, 0);
    }

}
