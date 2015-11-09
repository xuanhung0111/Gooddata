package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class StyleConfigPanel extends AbstractFragment {

    private static final String GENERAL_XPATH = "//input[./following-sibling::label";

    @FindBy(xpath = GENERAL_XPATH + "[.='Hidden']]")
    private WebElement hiddenTitle;

    @FindBy(xpath = GENERAL_XPATH + "[.='Visible']]")
    private WebElement visibleTitle;

    public void setTitleHidden() {
        waitForElementVisible(hiddenTitle).click();
    }

    public void setTitleVisible() {
        waitForElementVisible(visibleTitle).click();
    }
}
