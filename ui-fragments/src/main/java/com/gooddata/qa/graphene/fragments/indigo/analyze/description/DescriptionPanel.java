package com.gooddata.qa.graphene.fragments.indigo.analyze.description;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DescriptionPanel extends AbstractFragment {

    @FindBy(tagName = "h3")
    private WebElement title;

    @FindBy(className = "adi-item-description")
    private WebElement titleDescription;

    @FindBy(className = "adi-item-type")
    private WebElement fieldType;

    @FindBy(css = ".adi-item-type + span p")
    private WebElement value;

    @FindBy(className = "s-dataset-name")
    private WebElement dataset;

    public static final By LOCATOR = By.className("adi-catalogue-item-details");
    private static final By CATALOG_BUBBLE_LOADED = By.className("s-catalogue-bubble-loaded");

    private static final String NEW_LINE = "\n";

    public String getTimeDescription() {
        return getPrefix(true).toString();
    }

    public String getAttributeDescription() {
        waitForDataLoaded();

        return getPrefix(false).append("Values")
                .append(NEW_LINE)
                .append(waitForElementVisible(value).getText())
                .append(NEW_LINE)
                .toString();
    }

    public String getMetricDescription() {
        waitForDataLoaded();

        return getPrefix(false).append("Defined As")
                .append(NEW_LINE)
                .append(waitForElementVisible(value).getText())
                .append(NEW_LINE)
                .toString();
    }

    public String getFactDescription() {
        return getPrefix(false).append("Dataset")
                .append(NEW_LINE)
                .append(waitForElementVisible(dataset).getText())
                .append(NEW_LINE)
                .toString();
    }

    private StringBuilder getPrefix(boolean isTime) {
        return new StringBuilder().append(waitForElementVisible(title).getText())
                .append(NEW_LINE)
                .append(isTime? waitForElementVisible(titleDescription).getText() + NEW_LINE: "")
                .append("Field Type")
                .append(NEW_LINE)
                .append(waitForElementVisible(fieldType).getText())
                .append(NEW_LINE);
    }

    private void waitForDataLoaded() {
        waitForElementPresent(CATALOG_BUBBLE_LOADED, browser);
    }
}
