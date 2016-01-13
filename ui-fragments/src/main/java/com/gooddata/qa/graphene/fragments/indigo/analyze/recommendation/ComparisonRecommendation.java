package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ComparisonRecommendation extends AbstractRecommendation {

    public ComparisonRecommendation clickAttributePicker() {
        waitForElementVisible(className("s-attribute-picker"), getRoot()).click();
        return this;
    }

    public ComparisonRecommendation select(String attribute) {
        final By attributeSwitcher = className("s-attribute-switch");
        if (isElementPresent(attributeSwitcher, getRoot())) {
            new Select(waitForElementVisible(attributeSwitcher, getRoot())).selectByVisibleText(attribute);
            return this;
        }

        waitForElementVisible(cssSelector(".gd-list-item.s-" + simplifyText(attribute)), browser).click();
        return this;
    }

    public String getSuitableAttribute(String attribute) {
        WebElement firstAttribute = waitForElementVisible(cssSelector(".infinite-list-content .gd-list-item"), browser);

        if (isElementPresent(cssSelector(".gd-list-item.s-" + simplifyText(attribute)), browser))
            return attribute;

        String ret = firstAttribute.findElement(cssSelector("span:nth-child(2)")).getText();
        log.info(format("Attribute [%s] is not visible in view port. Choose the first attribute in view port: [%s]",
                attribute, ret));
        return ret;
    }
}
