package com.gooddata.qa.graphene.fragments.greypages.projects;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

public class ClearCaches extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement hours;

    @FindBy
    private WebElement submit;

    public void clearCaches(int hours) throws JSONException {
        waitForElementVisible(this.hours).clear();
        this.hours.sendKeys(String.valueOf(hours));
        Graphene.guardHttp(submit).click();
        waitForElementNotVisible(this.hours);
        JSONObject json = loadJSON();
        Assert.assertEquals(json.getJSONObject("clearCachesResult").getString("status"), "OK");
    }
}