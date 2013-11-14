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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractConnectorsCheckTest {
	
	@BeforeClass
	public void loadRequiredProperties() {
		connectorType = Connectors.ZENDESK4;
	}

    @Test(groups = {"connectorBasicREST"}, dependsOnGroups = {"connectorInit"})
    public void testZendesk4ProcessesResource() throws JSONException {
        // schedule process
        scheduleNewProcess();

        openUrl(getProcessesUri());
        verifyProcessesResourceJSONZ4();
    }

    private void verifyProcessesResourceJSONZ4() throws JSONException {
        verifyProcessesResourceJSON();
    	JSONObject json = loadJSON();
        JSONObject processes = json.getJSONObject("processes");
        JSONArray items = processes.getJSONArray("items");
        JSONObject process = items.getJSONObject(0).getJSONObject("process");
        // TODO Update when zendesk4-connector is ready
        Assert.assertEquals(process.getJSONObject("status").getString("code"), "SCHEDULED", "status code");
    }

    // TODO Replace by scheduleIntegrationProcess() when zendesk4-connector is ready
    private void scheduleNewProcess() throws JSONException {
    	openUrl(getProcessesUri());
        JSONObject json = loadJSON();
        Assert.assertTrue(json.getJSONObject("processes").getJSONArray("items").length() == 0, "There shouldn't be any processes for new project yet");
        Graphene.guardHttp(waitForElementVisible(BY_GP_BUTTON_SUBMIT)).click();
    }

}
