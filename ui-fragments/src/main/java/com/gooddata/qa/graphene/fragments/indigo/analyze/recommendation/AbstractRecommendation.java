package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.indigo.Recommendation;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractRecommendation extends AbstractFragment implements Recommendation {

    @FindBy(css = ".gd-heading-3")
    protected WebElement title;

    @FindBy(css = ".s-btn-apply")
    protected WebElement applyButton;

    @Override
    public void apply() {
        waitForElementVisible(applyButton).click();
    }

    @Override
    public String getTitle() {
        return waitForElementVisible(title).getText();
    }
}
