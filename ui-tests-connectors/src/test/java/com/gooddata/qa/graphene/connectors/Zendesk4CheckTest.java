/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractConnectorsCheckTest {

    @Test(groups = {"zendesk4Init"})
    public void createProject() throws JSONException, InterruptedException {
        // sign in with demo user
        validSignInWithDemoUser(true);

        // create connector project
        initProject("Zendesk4CheckConnector", Connectors.ZENDESK4, projectCheckLimit);
    }

    @Test(groups = {"zendesk4Init"}, dependsOnMethods = {"createProject"})
    public void createIntegration() throws JSONException, InterruptedException {
        // verify that zendesk4 resource exist
    	openUrl(getConnectorUri(Connectors.ZENDESK4));
        verifyConnectorResourceJSON(Connectors.ZENDESK4);

        // create integration
        initIntegration(Connectors.ZENDESK4);
    }

    @Test(groups = {"zendesk4BasicREST"}, dependsOnGroups = {"zendesk4Init"})
    public void testZendesk4IntegrationResource() throws JSONException {
    	openUrl(getIntegrationUri(Connectors.ZENDESK4));
        verifyIntegrationResourceJSON(Connectors.ZENDESK4);
    }

    @Test(groups = {"zendesk4BasicREST"}, dependsOnGroups = {"zendesk4Init"})
    public void testZendesk4ProcessesResource() throws JSONException {
        // schedule process
        scheduleNewProcess();

        openUrl(getProcessesUri(Connectors.ZENDESK4));
        verifyProcessesResourceJSONZ4();
    }

    @Test(dependsOnGroups = {"zendesk4BasicREST"})
    public void testDisableIntegration() throws JSONException {
        disableIntegration(Connectors.ZENDESK4);
    }

    @Test(dependsOnMethods = {"testDisableIntegration"}, alwaysRun = true)
    public void deleteProject() {
        deleteProject(projectId);
    }

    private void verifyProcessesResourceJSONZ4() throws JSONException {
        verifyProcessesResourceJSON(Connectors.ZENDESK4);
    	JSONObject json = loadJSON();
        JSONObject processes = json.getJSONObject("processes");
        JSONArray items = processes.getJSONArray("items");
        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        // TODO Update when zendesk4-connector is ready
        Assert.assertEquals(process.getJSONObject("status").getString("code"), "SCHEDULED", "status code");
    }

    // TODO Replace by scheduleIntegrationProcess() when zendesk4-connector is ready
    private void scheduleNewProcess() throws JSONException {
    	openUrl(getProcessesUri(Connectors.ZENDESK4));
        JSONObject json = loadJSON();
        Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 0, "There shouldn't be any processes for new project yet");
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT)).click();
    }

}
