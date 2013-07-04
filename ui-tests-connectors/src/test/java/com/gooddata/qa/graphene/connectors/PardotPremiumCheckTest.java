package com.gooddata.qa.graphene.connectors;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "pardotPremium" }, description = "Checklist tests for Pardot Premium connector in GD platform")
public class PardotPremiumCheckTest extends AbstractPardotCheckTest {
	
	@Test(groups = { "pardotPremiumBasicWalkthrough" })
	public void gd_Connectors_PP_001_PrepareProjectFromTemplate()
			throws InterruptedException, JSONException {
		prepareProjectFromTemplate("PardotPremiumCheckConnector", Connectors.PARDOT_PREMIUM);
	}
	
	@Test(dependsOnGroups = { "pardotPremiumBasicWalkthrough" }, alwaysRun = true)
	public void disableConnectorIntegration() throws JSONException {
		disableIntegration(Connectors.PARDOT_PREMIUM);
	}
}
