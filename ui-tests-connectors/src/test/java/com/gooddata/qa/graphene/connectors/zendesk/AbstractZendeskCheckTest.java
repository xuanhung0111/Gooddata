/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors.zendesk;

import com.gooddata.qa.graphene.connectors.AbstractConnectorsCheckTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public abstract class AbstractZendeskCheckTest extends AbstractConnectorsCheckTest {

    protected static final By BY_INPUT_API_URL = By.name("apiUrl");

    protected String zendeskUploadUser;
    protected String zendeskUploadUserPassword;
    protected String zendeskApiUrl;

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testConnectorIntegrationResource"})
    public void testZendeskIntegrationConfiguration() throws JSONException {
        openUrl(getIntegrationUri());
        String settingsUrl = openZendeskSettingsUrl();
        JSONObject json = loadJSON();
        if (!testParams.isReuseProject()) {
            assertEquals(json.getJSONObject("settings").getString("apiUrl"), "null",
                    String.format("%s API URL was not set to expected value", connectorType.getName()));
        }

        // zendesk specific configuration of API Url (with specific upload user)
        signInAtGreyPages(zendeskUploadUser, zendeskUploadUserPassword);
        browser.get(settingsUrl);
        if (!testParams.isReuseProject()) {
            waitForElementPresent(BY_INPUT_API_URL, browser).sendKeys(zendeskApiUrl);
            Graphene.guardHttp(waitForElementPresent(BY_GP_BUTTON_SUBMIT, browser)).click();
        }
        json = loadJSON();
        assertEquals(json.getJSONObject("settings").getString("apiUrl"), zendeskApiUrl,
                String.format("%s API URL was not set to expected value", connectorType.getName()));
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testZendeskIntegrationConfiguration"})
    public void testZendeskIntegrationWithUploadUser() throws JSONException {
        // zendesk specific configuration of API Url (with specific upload user)
        signInAtGreyPages(zendeskUploadUser, zendeskUploadUserPassword);
        testConnectorIntegrationResource();
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testZendeskIntegrationWithUploadUser"})
    public void testZendeskIntegration() throws JSONException {
        // sign in back with demo user
        signIn(true, UserRoles.ADMIN);
        // process schedule
        scheduleIntegrationProcessOrUseExisting(integrationProcessCheckLimit);
    }

    public abstract String openZendeskSettingsUrl();
}
