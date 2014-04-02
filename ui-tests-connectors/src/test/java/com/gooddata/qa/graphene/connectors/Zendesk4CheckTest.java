/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    @BeforeClass
    public void loadRequiredProperties() {
        super.loadRequiredProperties();

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Agent Performance", new String[]{
                "Public comments", "Comments by hour", "Group reassignments", "Comments by group"
        });
        expectedDashboardsAndTabs.put("Customer Support", new String[]{
                "Open tickets", "Problem tickets", "Time metrics", "Channels", "Ticket trends", "(Beta) Explorers", "Monthly"
        });
        expectedDashboardsAndTabs.put("Events", new String[]{
                "Events", "Update channel", "Satisfaction"
        });
        expectedDashboardsAndTabs.put("Users & Organizations", new String[]{
                "Users created", "Last login", "Tickets by user", "Top time zones"
        });
    }

    @Override
    public String openZendeskSettingsUrl() {
        openUrl(getIntegrationUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK)).click();
        return browser.getCurrentUrl();
    }
}
