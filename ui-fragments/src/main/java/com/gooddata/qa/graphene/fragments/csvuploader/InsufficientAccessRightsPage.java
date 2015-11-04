package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Fragment representing page where user without sufficient access rights to CSV uploader is redirected after navigating
 * to any CSV uploader valid page.
 */
public class InsufficientAccessRightsPage extends AbstractFragment {

    @FindBy(tagName = "h3")
    private WebElement header1;

    @FindBy(tagName = "h4")
    private WebElement header2;

    @FindBy(tagName = "h5")
    private WebElement header3;

    public String getHeader1() {
        return waitForElementVisible(header1).getText();
    }

    public String getHeader2() {
        return waitForElementVisible(header2).getText();
    }

    public String getHeader3() {
        return waitForElementVisible(header3).getText();
    }

}
