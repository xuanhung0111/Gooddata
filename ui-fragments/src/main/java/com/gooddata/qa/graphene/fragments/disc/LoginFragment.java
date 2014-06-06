package com.gooddata.qa.graphene.fragments.disc;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class LoginFragment extends AbstractFragment {

    @FindBy
    private WebElement username;

    @FindBy
    private WebElement password;

    @FindBy(xpath = "//button[text()='Sign in']")
    private WebElement signInButton;

    public void login(String username, String password) {
        waitForElementVisible(this.username).sendKeys(username);
        waitForElementVisible(this.password).sendKeys(password);
        Graphene.guardAjax(signInButton).click();
    }

}
