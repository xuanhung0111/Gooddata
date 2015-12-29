package com.gooddata.qa.graphene.fragments.greypages.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class AccountLoginFragment extends AbstractGreyPagesFragment {

    @FindBy(name = "USER")
    private WebElement username;

    @FindBy(name = "PASSWORD")
    private WebElement password;

    //todo - token verification level...

    @FindBy(name = "submit")
    private WebElement submitButton;

    public void login(String username, String password) throws JSONException {
        fillLoginForm(username, password);
        JSONObject json = loadJSON();
        Assert.assertTrue(json.getJSONObject("userLogin") != null);
        System.out.println("Successful GP login with user: " + username);
    }

    public void fillLoginForm(String username, String password) {
        waitForElementVisible(this.username).sendKeys(username);
        waitForElementVisible(this.password).sendKeys(password);
        Graphene.guardHttp(submitButton).click();
    }

}
