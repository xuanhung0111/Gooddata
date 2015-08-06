package com.gooddata.qa.graphene.fragments.greypages.md.query.attributes;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;

import org.json.JSONException;
import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class QueryAttributesFragment extends AbstractGreyPagesFragment {

    public int getAttributeIDByTitle(String attributeTitle) throws JSONException {
        waitForElementPresent(By.linkText(attributeTitle),browser).click();
        waitForElementPresent(BY_GP_PRE_JSON, browser);

        String currentUrl = browser.getCurrentUrl();
        return Integer.parseInt(currentUrl.substring(currentUrl.lastIndexOf("/")+1, currentUrl.length()));
    }
}
