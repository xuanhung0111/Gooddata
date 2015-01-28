package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class CategoriesBucket extends AbstractFragment {

//    @FindBy(css = ".adi-bucket-invitation")
//    private WebElement addCategoryBucket;

    @FindBy(css = ".adi-bucket-item")
    private List<WebElement> items;

    @FindBy(css = ".s-date-granularity-switch")
    private Select granularity;

    private static final By BY_TEXT = By.cssSelector(".adi-bucket-item-handle>div");
    private static final String EMPTY = "s-bucket-empty";

    public void addCategory(WebElement category) {
        new Actions(browser).dragAndDrop(category, waitForElementVisible(getRoot()))
                .perform();
        assertEquals(items.get(items.size() - 1).findElement(BY_TEXT).getText(), category.getText());
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public List<String> getItemsName() {
        return Lists.newArrayList(Collections2.transform(items, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(BY_TEXT).getText();
            }
        }));
    }

    public void changeGranularity(String time) {
        waitForElementVisible(granularity).selectByVisibleText(time);
    }

    public List<String> getAllGranularities() {
        return Lists.newArrayList(Collections2.transform(granularity.getOptions(), new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        }));
    }
}
