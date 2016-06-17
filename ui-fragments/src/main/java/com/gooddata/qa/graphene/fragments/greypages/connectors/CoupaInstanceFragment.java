package com.gooddata.qa.graphene.fragments.greypages.connectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class CoupaInstanceFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement name;

    @FindBy
    private WebElement apiUrl;

    @FindBy
    private WebElement apiKey;

    @FindBy(xpath = "div[@class='submit']/input")
    private WebElement createCoupaInstanceButton;

    public void createCoupaInstance(String name, String apiUrl, String apiKey) throws JSONException {
        waitForElementVisible(this.name).sendKeys(name);
        waitForElementVisible(this.apiUrl).sendKeys(apiUrl);
        waitForElementVisible(this.apiKey).sendKeys(apiKey);
        Graphene.guardHttp(createCoupaInstanceButton).click();
    }
}
