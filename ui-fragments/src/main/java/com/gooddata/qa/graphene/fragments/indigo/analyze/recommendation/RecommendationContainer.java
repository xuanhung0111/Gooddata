package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.entity.indigo.Recommendation;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class RecommendationContainer extends AbstractFragment {

    public static final By LOCATOR = By.cssSelector(".adi-recommendations-container");

    @FindBy(css = ".adi-recommendation")
    private List<WebElement> recommendations;

    public boolean isRecommendationVisible(RecommendationStep step) {
        return getRecommendationHelper(step) != null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Recommendation> T getRecommendation(RecommendationStep step) {
        WebElement ret = getRecommendationHelper(step);
        if (ret == null) {
            throw new NoSuchElementException("Can not find recommendation with title " + step);
        }
        return (T) Graphene.createPageFragment(step.getSupportedClass(), ret);
    }

    private WebElement getRecommendationHelper(final RecommendationStep step) {
        waitForCollectionIsNotEmpty(recommendations);
        return Iterables.find(recommendations, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return step.toString().equals(
                        Graphene.createPageFragment(step.getSupportedClass(), input).getTitle());
            }
        }, null);
    }
}
