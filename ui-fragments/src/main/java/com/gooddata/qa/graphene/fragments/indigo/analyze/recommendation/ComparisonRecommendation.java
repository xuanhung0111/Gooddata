package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class ComparisonRecommendation extends AbstractRecommendation {

    @FindBy(className = "s-attribute-switch")
    private Select attributeSwitch;

    public ComparisonRecommendation select(String attribute) {
        waitForElementVisible(attributeSwitch).selectByVisibleText(attribute);
        return this;
    }

}
