package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ArrangeConfigPanel extends AbstractFragment {

    @FindBy(className = "s-btn-backward")
    private WebElement backwardButton;

    @FindBy(className = "yui3-slider-thumb")
    private WebElement sliderThumb;

    @FindBy(className = "s-btn-forward")
    private WebElement forwardButton;

    public int getCurrentZIndex() {
        return Integer.parseInt(waitForElementVisible(sliderThumb).getAttribute("aria-valuenow"));
    }

    public ArrangeConfigPanel increaseZIndex() {
        waitForElementVisible(forwardButton).click();
        return this;
    }

    public ArrangeConfigPanel decreaseZIndex() {
        waitForElementVisible(backwardButton).click();
        return this;
    }
}
