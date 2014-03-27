/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.assertEquals;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractConnectorsCheckTest {

    private String zendeskUploadUser;
    private String zendeskUploadUserPassword;
    private String zendeskApiUrl;

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = loadProperty("connectors.zendesk.uploadUser");
        zendeskUploadUserPassword = loadProperty("connectors.zendesk.uploadUserPassword");

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Agent Performance", new String[]{
                "Public comments", "Comments by hour", "Group reassignments", "Comments by group"
        });
        //TODO
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testConnectorIntegrationResource" })
    public void testZendesk4IntegrationConfiguration() throws InterruptedException, JSONException {
        RestApiClient restApiClient = new RestApiClient(host, zendeskUploadUser, zendeskUploadUserPassword);
        openUrl(getIntegrationUri());
        // go to page with integration settings (note that settings URL is different from Z3
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK)).click();
        JSONObject json = loadJSON();
        assertEquals(json.getJSONObject("settings").getString("apiUrl"), "null", "Zendesk4 API URL was not set to expected value");
        String settingsUrl = browser.getCurrentUrl();

        // zendesk4 specific configuration of API Url (with specific upload user)
        signInAtGreyPages(zendeskUploadUser, zendeskUploadUserPassword);
        browser.get(settingsUrl);
        HttpRequestBase putRequest = restApiClient.newPutMethod(settingsUrl, "{\"settings\": {\"apiUrl\": \"" + zendeskApiUrl + "\"}}");
        HttpResponse putResponse = restApiClient.execute(putRequest);
        Assert.assertTrue(putResponse.getStatusLine().getStatusCode() == 204, "Settings wasn't updated to required value");
        browser.get(settingsUrl);
        json = loadJSON();
        assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendeskApiUrl, "Zendesk4 API URL was not set to expected value");
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"}, dependsOnMethods = { "testZendesk4IntegrationConfiguration" })
    public void testZendesk4Integration() throws InterruptedException, JSONException {
        // sign in back with demo user
        validSignInWithDemoUser(true);
        // process schedule
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

}
