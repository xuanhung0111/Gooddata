package com.gooddata.qa.graphene.fragments.common;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class LoginFragment extends AbstractFragment {
	
	@FindBy // automatic findBy - ID_OR_NAME
	private WebElement username;
	
	@FindBy(id="password")
	private WebElement password;
	
	@FindBy(css="button.loginSendButton")
	private WebElement signInButton;
	
	@FindBy
	private WebElement loginErrorMessages;
	
	private static final By BY_VALIDATION_ICON = By.xpath("//div[contains(@class, 'validationIcon')]");
	
	public void login(String username, String password) {
		waitForElementVisible(this.username);
		this.username.sendKeys(username);
		this.password.sendKeys(password);
		signInButton.click();
	}
	
	public boolean allLoginElementsAvailable() {
		return username.isDisplayed() && password.isDisplayed() && signInButton.isDisplayed();
	}
	
	public void waitForErrorMessageDisplayed() {
		Graphene.waitAjax().until().element(loginErrorMessages).text().contains("Login or password is wrong");
		waitForElementVisible(BY_VALIDATION_ICON);
	}
}
