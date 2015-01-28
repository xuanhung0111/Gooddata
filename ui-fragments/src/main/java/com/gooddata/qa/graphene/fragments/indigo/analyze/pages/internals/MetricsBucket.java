package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricsBucket extends AbstractFragment {

//    @FindBy(css = ".adi-bucket-invitation")
//    private WebElement addMetricBucket;

    @FindBy(css = ".s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(css = ".s-show-pop")
    private WebElement compareToSamePeriod;

    private static final String DISABLED = "is-disabled";
    private static final String EMPTY = "s-bucket-empty";

    public void addMetric(WebElement metric) {
        new Actions(browser).dragAndDrop(metric, waitForElementVisible(getRoot())).perform();
        assertEquals(
                waitForElementVisible(
                        By.cssSelector(".s-bucket-metrics .adi-bucket-item-handle>div"), browser)
                        .getText().trim(), metric.getText().trim());
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public void turnOnShowInPercents() {
        waitForElementVisible(showInPercents).click();
        assertTrue(showInPercents.isSelected());
    }

    public boolean isShowPercentConfigEnabled() {
        return !waitForElementPresent(showInPercents).findElement(BY_PARENT).getAttribute("class")
                .contains(DISABLED);
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        return !waitForElementPresent(compareToSamePeriod).findElement(BY_PARENT)
                .getAttribute("class").contains(DISABLED);
    }

    public boolean isShowPercentConfigSelected() {
        return waitForElementPresent(showInPercents).isSelected();
    }
}
