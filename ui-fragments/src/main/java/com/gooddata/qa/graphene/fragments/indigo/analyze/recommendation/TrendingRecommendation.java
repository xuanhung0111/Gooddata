package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class TrendingRecommendation extends AbstractRecommendation {

    @FindBy(className = "s-date-granularity-switch")
    private Select dateGranularitySwitch;

    public TrendingRecommendation select(String item) {
        waitForElementVisible(dateGranularitySwitch).selectByVisibleText(item);
        return this;
    }
}
