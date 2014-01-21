package com.gooddata.qa.graphene.connectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "pardotPremium" }, description = "Checklist tests for Pardot Premium connector in GD platform")
public class PardotPremiumCheckTest extends AbstractPardotCheckTest {
	
	@Override
	@BeforeClass
	public void loadRequiredProperties() {
		super.loadRequiredProperties();
		connectorType = Connectors.PARDOT_PREMIUM;
	}
}
