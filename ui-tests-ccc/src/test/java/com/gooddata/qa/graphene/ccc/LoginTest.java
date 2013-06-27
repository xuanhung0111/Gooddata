package com.gooddata.qa.graphene.ccc;

import org.testng.annotations.Test;

public class LoginTest extends AbstractCCCTest {
	
	@Test(groups = { "cccLoginInit" })
	public void loginPanelPresent() {
		waitForElementVisible(BY_DIV_LOGIN_PANEL);
	}
	
	@Test(dependsOnGroups = { "cccLoginInit" })
	public void testLogin() {
		login(user, password);
	}
	
}
