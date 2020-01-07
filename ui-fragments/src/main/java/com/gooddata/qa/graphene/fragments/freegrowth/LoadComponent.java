package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoadComponent extends AbstractFragment {

    @FindBy(css = ".splashscreen-text-headline")
    private WebElement headline;

    public WebElement getConnectLink() {
        return connectLink;
    }

    @FindBy(css = ".splashscreen-button-href")
    private WebElement connectLink;

    @FindBy(css = ".gd-button-link-dimmed")
    private WebElement tutorialLink;

    public String getHeadlineText() {
        return headline.getText();
    }
}
