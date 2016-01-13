package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.FindBy;

public class ComparisonRecommendation extends AbstractRecommendation {

    @FindBy(className = "s-attribute-picker")
    private WebElement attributePickerButton;

    @FindBy(xpath = "//div[contains(@class, 's-attribute-picker-list')]")
    private WebElement attributePicker;

    public ComparisonRecommendation select(String attribute) {
        waitForElementVisible(attributePickerButton).click();

        waitForElementVisible(attributePicker)
                .findElement(By.className("s-" + simplifyText(attribute)))
                .click();

        return this;
    }

}
