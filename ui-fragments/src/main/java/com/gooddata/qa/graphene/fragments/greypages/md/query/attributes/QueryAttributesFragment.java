package com.gooddata.qa.graphene.fragments.greypages.md.query.attributes;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class QueryAttributesFragment extends AbstractGreyPagesFragment {

    public int getAttributeIDByTitle(String attributeTitle) throws JSONException {
        waitForElementPresent(By.linkText(attributeTitle),browser).click();
        waitForElementPresent(BY_GP_PRE_JSON, browser);

        String currentUrl = browser.getCurrentUrl();
        return Integer.parseInt(currentUrl.substring(currentUrl.lastIndexOf("/")+1, currentUrl.length()));
    }
}