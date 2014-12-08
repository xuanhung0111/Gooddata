package com.gooddata.qa.graphene.fragments.indigo.recommendation;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ComparisonRecommendation extends AbstractRecommendation {

    @FindBy(css = ".s-attribute-switch")
    private Select attributeSwitch;

    public ComparisonRecommendation select(String attribute) {
        waitForElementVisible(attributeSwitch).selectByVisibleText(attribute);
        return this;
    }

}
