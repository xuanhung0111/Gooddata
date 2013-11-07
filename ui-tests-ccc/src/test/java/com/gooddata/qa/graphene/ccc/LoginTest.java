package com.gooddata.qa.graphene.ccc;

import org.testng.annotations.Test;

public class LoginTest extends AbstractCCCTest {
	
	@Test(groups = { "cccLoginInit" })
	public void loginPanelPresent() {
		waitForElementVisible(loginFragment.getRoot());
	}
	
	@Test(dependsOnGroups = { "cccLoginInit" })
	public void testLogin() {
		login(user, password);
	}
	
}
