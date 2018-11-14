package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Objects;

public class BubbleChart extends ChartReport {

    public boolean isColumnHighlighted(int position) {
        WebElement element = getTracker(position);
        final String fillBeforeHover = element.getAttribute("stroke");
        getActions().moveToElement(element).moveByOffset(1, 1).perform();
        final String fillAfterHover = element.getAttribute("stroke");
        return !Objects.equals(fillAfterHover, fillBeforeHover);
    }

    public void clickOnElement(int position) {
        WebElement element = getTracker(position);
        // Because geckodriver follows W3C and moves the mouse pointer from the centre of the screen,
        // Move the mouse pointer to the top-left corner of the fragment before moving to the specific Element
        ElementUtils.moveToElementActions(getRoot(), 0, 0).moveToElement(element)
                .moveByOffset(1, 1).click().perform();
    }

    private WebElement getTracker(int position) {
        return waitForElementVisible(
                By.cssSelector(String.format(".highcharts-series-%s.highcharts-tracker", position)), getRoot());
    }
}
