package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class CategoriesBucket extends AbstractFragment {

    @FindBy(css = ".adi-bucket-item")
    private List<WebElement> items;

    @FindBy(css = ".s-date-granularity-switch")
    private Select granularity;

    @FindBy(className = "s-date-dimension-switch")
    private Select dimensionSwitch;

    private static final By BY_TRASH_PANEL = By.cssSelector(".adi-trash-panel");
    private static final By BY_HEADER = By.className("adi-bucket-item-header");
    private static final String EMPTY = "s-bucket-empty";

    public void addCategory(WebElement category) {
        new Actions(browser).dragAndDrop(category, waitForElementVisible(getRoot()))
                .perform();
        assertEquals(items.get(items.size() - 1).findElement(BY_HEADER).getText(), category.getText());
    }

    public void removeCategory(final String category) {
        int oldItemsCount = items.size();
        WebElement element = Iterables.find(items, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return category.equals(input.findElement(BY_HEADER).getText());
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

        assertEquals(items.size(), oldItemsCount - 1, "Category is not removed yet!");
    }

    public void replaceCategory(WebElement category) {
        addCategory(category);
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public List<String> getItemNames() {
        return Lists.newArrayList(Collections2.transform(items, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(BY_HEADER).getText();
            }
        }));
    }

    public void changeGranularity(String time) {
        waitForElementVisible(granularity).selectByVisibleText(time);
    }

    public String getSelectedGranularity() {
        return waitForElementVisible(granularity).getFirstSelectedOption().getText();
    }

    public List<String> getAllGranularities() {
        waitForElementVisible(granularity);
        return Lists.newArrayList(Collections2.transform(granularity.getOptions(), new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        }));
    }

    public String getSelectedDimensionSwitch() {
        waitForElementVisible(dimensionSwitch);
        return dimensionSwitch.getFirstSelectedOption().getText();
    }

    public void changeDimensionSwitchInBucket(String dimensionSwitch) {
        waitForElementVisible(this.dimensionSwitch);
        this.dimensionSwitch.selectByVisibleText(dimensionSwitch);
    }

    public WebElement getFirstItem() {
        return items.get(0);
    }
}
