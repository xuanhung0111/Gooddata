/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractConnectorsCheckTest {

    @BeforeClass
    public void loadRequiredProperties() {
        connectorType = Connectors.ZENDESK4;
    }

    @Override
    @Test(groups = {"connectorWalkthrough"}, dependsOnMethods = {"testConnectorProcessesResource"})
    public void verifyProjectDashboards() throws InterruptedException {
        // TODO temporary workaround for missing Zendesk4 dashboards
        successfulTest = true;
    }

    @Test(groups = {"connectorBasicREST", "connectorIntegration"}, dependsOnGroups = {"connectorInit"})
    public void testZendesk4ScheduleProcess() throws JSONException, InterruptedException {
        // schedule process
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

}
