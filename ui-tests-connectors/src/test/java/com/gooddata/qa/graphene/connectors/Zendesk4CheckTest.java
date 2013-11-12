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
        startPage = getConnectorUri(Connectors.ZENDESK4);
        loadPlatformPageBeforeTestMethod();
        verifyConnectorResourceJSON();

        // create integration
        initIntegration(Connectors.ZENDESK4);
    }

    @Test(groups = {"zendesk4BasicREST"}, dependsOnGroups = {"zendesk4Init"})
    public void testZendesk4IntegrationResource() throws JSONException {
        startPage = getIntegrationUri(Connectors.ZENDESK4);
        loadPlatformPageBeforeTestMethod();
        verifyIntegrationResourceJSON();
    }

    @Test(groups = {"zendesk4BasicREST"}, dependsOnGroups = {"zendesk4Init"})
    public void testZendesk4ProcessesResource() throws JSONException {
        // schedule process
        scheduleNewProcess();

        startPage = getProcessesUri(Connectors.ZENDESK4);
        loadPlatformPageBeforeTestMethod();
        verifyProcessesResourceJSON();
    }

    @Test(dependsOnGroups = {"zendesk4BasicREST"})
    public void testDisableIntegration() throws JSONException {
        disableIntegration(Connectors.ZENDESK4);
    }

    @Test(dependsOnMethods = {"testDisableIntegration"}, alwaysRun = true)
    public void deleteProject() {
        deleteProject(projectId);
    }

    private void verifyConnectorResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("connector"), "connector object is missing");

        JSONObject connector = json.getJSONObject("connector");
        Assert.assertEquals(connector.getString("connectorId"), Connectors.ZENDESK4.getConnectorId(), "connectorId");
        Assert.assertTrue(connector.has("links"), "links object is missing");

        JSONObject links = connector.getJSONObject("links");
        Assert.assertEquals(links.getString("integration"), "/" + getIntegrationUri(Connectors.ZENDESK4), "integration link");
    }

    private void verifyIntegrationResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("integration"), "integration object is missing");

        JSONObject integration = json.getJSONObject("integration");
        Assert.assertEquals(integration.getBoolean("active"), true, "active integration");
        Assert.assertEquals(integration.getString("projectTemplate"), Connectors.ZENDESK4.getTemplate(), "project template");
        Assert.assertTrue(integration.has("lastFinishedProcess"), "lastFinishedProcess key is missing");
        Assert.assertTrue(integration.has("lastSuccessfulProcess"), "lastSuccessfulProcess key is missing");
        Assert.assertTrue(integration.has("runningProcess"), "runningProcess key is missing");
        Assert.assertTrue(integration.has("links"), "links object is missing");

        JSONObject links = integration.getJSONObject("links");
        Assert.assertEquals(links.getString("self"), "/" + getIntegrationUri(Connectors.ZENDESK4), "self link");
        Assert.assertEquals(links.getString("processes"), "/" + getProcessesUri(Connectors.ZENDESK4), "processes link");
    }

    private void verifyProcessesResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        Assert.assertTrue(json.has("processes"), "processes object is missing");

        JSONObject processes = json.getJSONObject("processes");
        Assert.assertTrue(processes.has("items"), "items array is missing");
        JSONArray items = processes.getJSONArray("items");
        Assert.assertTrue(items.length() > 0, "at least one process should exist");
        Assert.assertTrue(items.getJSONObject(0).has("process"), "process object is missing");

        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        Assert.assertTrue(process.has("started"), "started key is missing");
        Assert.assertTrue(process.has("finished"), "finished key is missing");
        Assert.assertTrue(process.getJSONObject("status").has("detail"), "detail key is missing");
        Assert.assertTrue(process.getJSONObject("status").has("description"), "description key is missing");
        // TODO Update when zendesk4-connector is ready
        Assert.assertEquals(process.getJSONObject("status").getString("code"), "SCHEDULED", "status code");
        Assert.assertTrue(process.getJSONObject("links").getString("self").startsWith("/" + getProcessesUri(Connectors.ZENDESK4)), "self link is incorrect");
    }

    // TODO Replace by scheduleIntegrationProcess() when zendesk4-connector is ready
    private void scheduleNewProcess() throws JSONException {
        browser.get(getRootUrl() + getProcessesUri(Connectors.ZENDESK4));
        JSONObject json = loadJSON();
        Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 0, "There shouldn't be any processes for new project yet");
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT)).click();
    }

}
