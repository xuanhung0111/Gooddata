package com.gooddata.qa.graphene.connectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "pardotPremium" }, description = "Checklist tests for Pardot Premium connector in GD platform")
public class PardotPremiumCheckTest extends AbstractPardotCheckTest {
	
	@BeforeClass
	public void initConnectorType() {
		connectorType = Connectors.PARDOT_PREMIUM;
	}
}
