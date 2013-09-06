package com.gooddata.qa.graphene.common;

import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.common.LoginFragment;
import com.gooddata.qa.utils.graphene.Screenshots;


@Test(groups = { "login" }, description = "Tests for basic login functionality in GD platform")
public class LoginPageTest extends AbstractTest {

	public static final By BY_LOGOUT_LINK = By.xpath("//a[@class='s-logout']");
	
	@FindBy(id="loginPanel")
	LoginFragment loginFragment;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "login.html";
	}
	
	@Test(groups = {"loginInit"})
	public void gd_Login_001_LoginPanel() {
		waitForElementVisible(BY_LOGIN_PANEL);
		Assert.assertTrue(loginFragment.allLoginElementsAvailable(), "Login panel with valid elements is available");
	}
	
	@Test(dependsOnGroups = {"loginInit"})
	public void gd_Login_002_SignInAndSignOut() throws InterruptedException {
		loginFragment.login(user, password);
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		Screenshots.takeScreenshot(browser, "login-ui", this.getClass());
		browser.findElement(BY_LOGGED_USER_BUTTON).click();
		waitForElementVisible(BY_LOGOUT_LINK);
		browser.findElement(BY_LOGOUT_LINK).click();
		waitForElementVisible(loginFragment.getRoot());
	}
	
	@Test(dependsOnGroups = {"loginInit"})
	public void gd_Login_003_SignInWithEmptyPassword() {
		loginFragment.login(user, "");
		loginFragment.waitForErrorMessageDisplayed();
	}
	
	@Test(dependsOnGroups = {"loginInit"})
	public void gd_Login_004_SignInWithInvalidPassword() {
		loginFragment.login(user, "abcdefgh");
		loginFragment.waitForErrorMessageDisplayed();
	}
}
