package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class TrendingRecommendation extends AbstractRecommendation {

    @FindBy(css = ".s-date-granularity-switch")
    private Select dateGranularitySwitch;

    public TrendingRecommendation select(String item) {
        waitForElementVisible(dateGranularitySwitch).selectByVisibleText(item);
        return this;
    }
}
