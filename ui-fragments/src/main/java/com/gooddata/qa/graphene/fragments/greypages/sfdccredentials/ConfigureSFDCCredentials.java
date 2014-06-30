package com.gooddata.qa.graphene.fragments.greypages.sfdccredentials;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ConfigureSFDCCredentials extends AbstractGreyPagesFragment {

    @FindBy(xpath = "//input[@name = 'key']")
    private WebElement keyField;

    @FindBy(xpath = "//input[@name = 'secret']")
    private WebElement secretField;

    @FindBy(xpath = "div[@class='submit']/input")
    private WebElement setCredentialsButton;

    public void setSFDCCredentials(String key, String secret)
	    throws JSONException {
	waitForElementVisible(keyField).sendKeys(key);
	waitForElementVisible(secretField).sendKeys(secret);
	Graphene.guardHttp(setCredentialsButton).click();
	JSONObject json = loadJSON();
	Assert.assertTrue(json.getJSONObject("credential").getString("key")
		.equals(key), "SFDC credentials is NOT created properly!");
    }

}
