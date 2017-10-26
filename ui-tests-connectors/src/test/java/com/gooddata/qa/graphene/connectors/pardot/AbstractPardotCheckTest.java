package com.gooddata.qa.graphene.connectors.pardot;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.connectors.AbstractConnectorsCheckTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;

public abstract class AbstractPardotCheckTest extends AbstractConnectorsCheckTest {

    protected static final By BY_INPUT_PARDOT_ACCOUNT_ID = By.name("accountId");

    protected String pardotAccountId;
    protected String pardotUploadUser;
    protected String pardotUploadUserPassword;
    protected Map<String, String[]> expectedDashboardsAndTabs;

    @Override
    protected void initProperties() {
        super.initProperties();
        pardotAccountId = testParams.loadProperty("connectors.pardot.accountId");
        pardotUploadUser = testParams.loadProperty("connectors.pardot.uploadUser");
        pardotUploadUserPassword = testParams.loadProperty("connectors.pardot.uploadUserPassword");

        expectedDashboardsAndTabs = new HashMap<>();
        expectedDashboardsAndTabs.put("Pardot Analytics", new String[]{
                "Marketing KPIs", "Contribution", "Prospects", "Opportunities", "and more"
        });
        expectedDashboardsAndTabs.put("Prospects Only", new String[]{
                "Marketing KPIs", "Prospects", "Leaderboard", "and more"
        });

        projectCreateCheckIterations = 120;
        integrationProcessCheckLimit = 240;
    }

    @Test(groups = {"connectorBasicREST"}, dependsOnGroups = {"connectorIntegration"})
    public void testConnectorProcessesResource() throws JSONException {
        openUrl(getProcessesUri());
        verifyProcessesResourceJSON();
    }

    @Test(groups = {"connectorWalkthrough"}, dependsOnMethods = {"testConnectorProcessesResource"})
    public void verifyProjectDashboards() {
        // verify created project and count dashboard tabs
        verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, true);
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testPardotIntegrationConfiguration() throws JSONException {
        openUrl(getIntegrationUri());
        // go to page with integration settings
        String settingsUrl = gotoIntegrationSettings();

        // pardot specific configuration of API Url (with specific upload user)
        signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
        browser.get(settingsUrl);
        if (!testParams.isReuseProject()) {
            waitForElementVisible(BY_INPUT_PARDOT_ACCOUNT_ID, browser).sendKeys(pardotAccountId);
            Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT, browser)).click();
        }
        JSONObject json = loadJSON();
        assertEquals(json.getJSONObject("settings").getString("accountId"), pardotAccountId,
                "Pardot accountId was not set to expected value");
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testPardotIntegrationConfiguration"})
    public void testPardotIntegrationWithUploadUser() throws JSONException {
        // pardot specific configuration of API Url (with specific upload user)
        signInAtGreyPages(pardotUploadUser, pardotUploadUserPassword);
        testConnectorIntegrationResource();
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testPardotIntegrationWithUploadUser"})
    public void testPardotIntegration() throws JSONException {
        // sign in back with demo user
        signIn(true, UserRoles.ADMIN);
        // process schedule
        scheduleIntegrationProcessOrUseExisting(integrationProcessCheckLimit);
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testPardotIntegration"})
    public void testIncrementalSynchronization() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    private void verifyProcessesResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertThat("Processes resource JSON " + json.toString() + " contains 'processes' key",
                json.has("processes"),
                is(true));

        JSONObject processes = json.getJSONObject("processes");
        assertThat("Processes resource JSON " + json.toString() + " contains 'items' key",
                processes.has("items"),
                is(true));

        JSONArray items = processes.getJSONArray("items");
        assertThat("Processes resource JSON contains at least one process in 'items' array",
                items.length(),
                is(greaterThan(0)));
        assertThat("First item from processes resource JSON " + json.toString() + " contains 'process' key",
                items.getJSONObject(0).has("process"),
                is(true));

        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        assertThat("First item from processes resource JSON " + json.toString() + " contains 'started' key",
                process.has("started"),
                is(true));
        assertThat("First item from processes resource JSON " + json.toString() + " contains 'finished' key",
                process.has("finished"),
                is(true));
        final JSONObject status = process.getJSONObject("status");
        assertThat("First item's status from processes resource JSON " + json.toString() + " contains 'detail' key",
                status.has("detail"),
                is(true));
        assertThat(
                "First item's status from processes resource JSON " + json.toString() + " contains 'description' key",
                status.has("description"),
                is(true));
        assertThat("First item's status from processes resource JSON " + json.toString() + " contains 'code' key",
                status.has("code"),
                is(true));
        assertThat("First item's links from processes resource JSON contains correct self link",
                process.getJSONObject("links").getString("self"),
                startsWith("/" + getProcessesUri()));
    }
}
