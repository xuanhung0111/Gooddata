package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MetricsBucket extends AbstractFragment {

    @FindBy(css = ".s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(css = ".s-show-pop")
    private WebElement compareToSamePeriod;

    @FindBy(css = ".adi-bucket-item")
    private List<WebElement> items;

    private static final String DISABLED = "is-disabled";
    private static final String EMPTY = "s-bucket-empty";
    private static final By BY_TEXT = By.cssSelector(".adi-bucket-item-handle>div");
    private static final By BY_TRASH_PANEL = By.cssSelector(".adi-trash-panel");
    private static final By BY_BUCKET_INVITATION = By.cssSelector(".adi-bucket-invitation");
    private static final By BY_STACK_WARNING = By.className("adi-stack-warn");

    public void addMetric(WebElement metric) {
        new Actions(browser).dragAndDrop(metric, waitForElementVisible(BY_BUCKET_INVITATION, getRoot())).perform();
        assertTrue(getItemNames().contains(metric.getText().trim()));
    }

    private WebElement getMetric(final String name) {
        return Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return name.equals(input.findElement(BY_TEXT).getText());
            }
        });
    }

    public void replaceMetric(String oldMetric, WebElement newMetric) {
        new Actions(browser).dragAndDrop(newMetric, getMetric(oldMetric)).perform();
        assertTrue(getItemNames().contains(newMetric.getText().trim()));
    }

    public boolean isWarningMessageShown() {
        return false;
    }

    public List<String> getItemNames() {
        return Lists.newArrayList(Collections2.transform(items, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(BY_TEXT).getText();
            }
        }));
    }

    public void removeMetric(final String metric) {
        int oldItemsCount = items.size();
        WebElement element = Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return metric.equals(input.findElement(BY_TEXT).getText());
            }
        });
    
        Actions action = new Actions(browser);
        WebElement catalogue = browser.findElement(By.className("s-catalogue"));
        Point location = catalogue.getLocation();
        Dimension dimension = catalogue.getSize();
        action.clickAndHold(element).moveByOffset(location.x + dimension.width/2, location.y + dimension.height/2)
            .perform();
        action.moveToElement(waitForElementPresent(BY_TRASH_PANEL, browser)).perform();
        action.release().perform();
    
        assertEquals(items.size(), oldItemsCount - 1, "Metric is not removed yet!");
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

    public void compareToSamePeriodOfYearBefore() {
        waitForElementVisible(compareToSamePeriod).click();
        assertTrue(compareToSamePeriod.isSelected());
    }

    public String getStackByMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }
}
