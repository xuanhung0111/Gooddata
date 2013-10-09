package com.gooddata.qa.graphene.connectors;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "pardotBase" }, description = "Checklist tests for Pardot Base connector in GD platform")
public class PardotBaseCheckTest extends AbstractPardotCheckTest {
	
	@Test(groups = { "pardotBaseBasicWalkthrough" })
	public void gd_Connectors_PB_001_PrepareProjectFromTemplate()
			throws InterruptedException, JSONException {
		prepareProjectFromTemplate("PardotBaseCheckConnector", Connectors.PARDOT_BASE);
	}
	
	@Test(dependsOnGroups = { "pardotBaseBasicWalkthrough" }, alwaysRun = true)
	public void disableConnectorIntegration() throws JSONException {
		disableIntegration(Connectors.PARDOT_BASE);
	}
	
	@Test(dependsOnMethods = { "disableConnectorIntegration"}, alwaysRun = true)
	public void deleteProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
