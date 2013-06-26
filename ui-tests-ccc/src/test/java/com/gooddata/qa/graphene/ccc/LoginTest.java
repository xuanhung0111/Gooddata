package com.gooddata.qa.graphene.ccc;

import org.testng.annotations.Test;

public class LoginTest extends AbstractCCCTest {
	
	@Test(groups = { "cccLoginInit" })
	public void loginPanelPresent() {
		
	}
	
	@Test(dependsOnGroups = { "cccLoginInit" })
	public void testLoginAndLogout() {
		
	}
	
}
