package com.gooddata.qa.graphene.connectors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.Connectors;

@Test(groups = { "connectors", "pardotBase" }, description = "Checklist tests for Pardot Base connector in GD platform")
public class PardotBaseCheckTest extends AbstractPardotCheckTest {
	
	@BeforeClass
	@Override
	public void loadRequiredProperties() {
		super.loadRequiredProperties();
		connectorType = Connectors.PARDOT_BASE;
	}
}
