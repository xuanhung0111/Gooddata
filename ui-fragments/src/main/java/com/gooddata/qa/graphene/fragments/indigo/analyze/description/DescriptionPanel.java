package com.gooddata.qa.graphene.fragments.indigo.analyze.description;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class DescriptionPanel extends AbstractFragment {

    @FindBy(tagName = "h3")
    private WebElement title;

    @FindBy(css = ".adi-item-description")
    private WebElement titleDescription;

    @FindBy(css = ".adi-item-type")
    private WebElement fieldType;

    @FindBy(css = ".adi-item-type~div p")
    private WebElement value;

    @FindBy(css = ".s-dataset-name")
    private WebElement dataset;

    public static final By LOCATOR = By.cssSelector(".adi-catalogue-item-details");
    
    private static final String NEW_LINE = "\n";
    private static final String LOADING = "Loading...";

    public String getTimeDescription() {
        return getPrefix(true).toString();
    }

    public String getAttributeDescription() {
        StringBuilder builder = getPrefix(false);

        builder.append("Values").append(NEW_LINE);
        waitForDataLoaded();
        builder.append(waitForElementVisible(value).getText()).append(NEW_LINE);

        return builder.toString();
    }

    public String getMetricDescription() {
        StringBuilder builder = getPrefix(false);

        builder.append("Defined As").append(NEW_LINE);
        waitForDataLoaded();
        builder.append(waitForElementVisible(value).getText()).append(NEW_LINE);

        return builder.toString();
    }

    public String getFactDescription() {
        StringBuilder builder = getPrefix(false);

        builder.append("Dataset").append(NEW_LINE);
        builder.append(waitForElementVisible(dataset).getText()).append(NEW_LINE);

        return builder.toString();
    }

    private StringBuilder getPrefix(boolean isTime) {
        StringBuilder builder = new StringBuilder();

        builder.append(waitForElementVisible(title).getText()).append(NEW_LINE);
        if (isTime) builder.append(waitForElementVisible(titleDescription).getText()).append(NEW_LINE);
        builder.append("Field Type").append(NEW_LINE);
        builder.append(waitForElementVisible(fieldType).getText()).append(NEW_LINE);

        return builder;
    }

    private void waitForDataLoaded() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !LOADING.equals(value.getText());
            }
        });
    }
}
