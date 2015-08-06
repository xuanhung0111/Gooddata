package com.gooddata.qa.graphene.fragments.greypages.md.ldm.singleloadinterface;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class SingleLoadInterfaceFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement dataset;

    @FindBy
    private WebElement submit;

    public JSONObject postDataset(String datasetName) throws JSONException, InterruptedException {
        waitForElementVisible(this.dataset).sendKeys(datasetName);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        String string = loadJSON().getJSONObject("sliLinks").getJSONObject("manifest").getString("uri");
        Graphene.guardHttp(browser.findElement(By.linkText(string))).click();
        return loadJSON();
    }
}
