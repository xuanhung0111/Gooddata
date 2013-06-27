package com.gooddata.qa.graphene.fragments.ccc;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class LoginFragment extends AbstractFragment {
	
	@FindBy
	private WebElement username;
	
	@FindBy
	private WebElement password;
	
	@FindBy(xpath="//button[text()='Sign in']")
	private WebElement signInButton;
	
	public void login(String username, String password) {
		waitForElementVisible(this.username);
		this.username.sendKeys(username);
		this.password.sendKeys(password);
		Graphene.guardAjax(signInButton).click();
	}

}
