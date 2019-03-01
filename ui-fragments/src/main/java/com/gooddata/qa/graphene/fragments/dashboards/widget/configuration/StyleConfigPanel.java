package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class StyleConfigPanel extends AbstractFragment {

    private static final String GENERAL_XPATH = "//input[./following-sibling::label";

    @FindBy(className = "inlineBubbleHelp")
    private WebElement inlineHelp;

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

    public void clickMoreInfo() {
        hover(inlineHelp);
        waitForElementVisible(
                cssSelector(".yui3-widget-stacked:not(.yui3-overlay-hidden) .bubble-primary a:not(.linkToChange)"), browser).click();

    }

    private void hover(WebElement element) {
        new Actions(browser)
                .moveToElement(waitForElementVisible(element))
                .perform();
    }
}
