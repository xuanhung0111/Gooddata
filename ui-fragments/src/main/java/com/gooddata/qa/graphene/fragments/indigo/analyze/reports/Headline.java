package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class Headline extends AbstractFragment {

    @FindBy(className = "s-headline-primary-item")
    private WebElement primaryItem;

    @FindBy(className = "s-headline-tertiary-item")
    private WebElement tertiaryItem;

    @FindBy(className = "s-headline-secondary-item")
    private WebElement secondaryItem;

    public String getPrimaryItem() {
        return waitForElementVisible(primaryItem).getText();
    }

    public List<String> getTertiaryItem() {
        return asList(waitForElementVisible(tertiaryItem).getText().split("\n"));
    }

    public List<String> getSecondItem() {
        return asList(waitForElementVisible(secondaryItem).getText().split("\n"));
    }
}
