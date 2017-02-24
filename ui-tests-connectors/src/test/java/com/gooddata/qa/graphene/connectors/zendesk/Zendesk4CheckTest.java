/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors.zendesk;

import com.gooddata.qa.graphene.enums.Connectors;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

@SuppressWarnings("serial")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = testParams.loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = testParams.loadProperty("connectors.zendesk4.uploadUser");
        zendeskUploadUserPassword = testParams.loadProperty("connectors.zendesk4.uploadUserPassword");

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<>();
        expectedDashboardsAndTabs.put("Insights - View Only", new String[]{
                "Overview", "Tickets", "Satisfaction", "Efficiency", "Agent Activity", "SLAs", "Learn More"
        });
        expectedDashboardsAndTabs.put("My dashboard", new String[]{"First Tab"});
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testFullLoad() throws IOException, JSONException {
        runConnectorProjectFullLoad();
    }

    @Override
    public String openZendeskSettingsUrl() {
        openUrl(getIntegrationUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK, browser)).click();
        return browser.getCurrentUrl();
    }

}
