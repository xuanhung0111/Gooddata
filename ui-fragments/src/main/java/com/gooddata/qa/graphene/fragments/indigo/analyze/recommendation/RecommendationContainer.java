package com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.indigo.Recommendation;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class RecommendationContainer extends AbstractFragment {

    public static final By LOCATOR = By.className("adi-recommendations-container");

    @FindBy(className = "adi-recommendation")
    private List<WebElement> recommendations;

    public boolean isRecommendationVisible(RecommendationStep step) {
        //Compare recommendation is loaded slowly than another should wait to load completely
        if (step == RecommendationStep.COMPARE) {
            try {
                Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS)
                        .until(browser -> isElementVisible(By.className("s-recommendation-comparison"), getRoot()));
            } catch (TimeoutException e) {
                //Do nothing
            }
        }
        return nonNull(getRecommendationHelper(step));
    }

    @SuppressWarnings("unchecked")
    public <T extends Recommendation> T getRecommendation(RecommendationStep step) {
        WebElement ret = getRecommendationHelper(step);
        if (isNull(ret)) {
            throw new NoSuchElementException("Can not find recommendation with title " + step);
        }
        return (T) Graphene.createPageFragment(step.getSupportedClass(), ret);
    }

    private WebElement getRecommendationHelper(final RecommendationStep step) {
        return waitForCollectionIsNotEmpty(recommendations).stream()
            .filter(e -> step.toString().equals(Graphene.createPageFragment(step.getSupportedClass(), e).getTitle()))
            .findFirst()
            .orElse(null);
    }
}
