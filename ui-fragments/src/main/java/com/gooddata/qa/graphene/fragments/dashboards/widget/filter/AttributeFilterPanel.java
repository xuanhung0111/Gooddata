package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AttributeFilterPanel extends FilterPanel {

    @FindBy(className = "yui3-c-simpleColumn-window")
    private WebElement scroller;

    @FindBy(css = "div.c-checkboxSelectOnly:not(.gdc-hidden)")
    private List<FilterPanelRow> rows;

    @FindBy(className = "clearVisible")
    private WebElement deselectAll;

    @FindBy(className = "selectVisible")
    private WebElement selectAll;

    @FindBy(className = "s-afp-input")
    private WebElement search;

    @FindBy(css = "div.yui3-c-simpleColumn-underlay label.ellipsisEnabled")
    private List<WebElement> listAttrValues;

    private static final String CLEAR_VISIBLE = ":not(.gdc-hidden)>.clearVisible";
    private static final String SELECT_VISIBLE = ":not(.gdc-hidden)>.selectVisible";

    public List<String> getAllAtributeValues() {
        List<String> actualFilterElements = new ArrayList<String>();
        waitForCollectionIsNotEmpty(listAttrValues);
        for (WebElement ele : listAttrValues) {
            actualFilterElements.add(waitForElementVisible(ele).getText());
        }
        close();
        return actualFilterElements;
    }

    public List<FilterPanelRow> getRows() {
        return rows;
    }

    public WebElement getScroller() {
        return scroller;
    }

    public AttributeFilterPanel waitForValuesToLoad() {
        waitForElementPresent(By.cssSelector(".yui3-c-simpleColumn-window.loaded"), browser);
        return this;
    }

    public AttributeFilterPanel selectAll() {
        waitForElementVisible(selectAll).click();
        return this;
    }

    public AttributeFilterPanel deselectAll() {
        waitForElementVisible(deselectAll).click();
        return this;
    }

    public void changeValues(String... values) {
        waitForValuesToLoad();
        waitForElementVisible(deselectAll).click();
        for (String value : values) {
            selectOneValue(value);
        }
        submit();
    }

    public AttributeFilterPanel search(String text) {
        waitForElementVisible(search).sendKeys(text);
        return this;
    }

    public boolean verifyPanelInOneValueMode() {
        return getRoot().findElements(By.cssSelector(SELECT_VISIBLE)).size() +
                getRoot().findElements(By.cssSelector(CLEAR_VISIBLE)).size() +
                getRoot().findElements(By.cssSelector(".s-btn-cancel:not(.gdc-hidden)")).size() +
                getRoot().findElements(By.cssSelector(".s-btn-apply:not(.gdc-hidden)")).size() == 0;
    }

    private void selectOneValue(String value) {
        waitForElementVisible(search).clear();
        search.sendKeys(value);
        waitForValuesToLoad();
        for (FilterPanelRow row : rows) {
            if (!value.equals(row.getLabel().getText())) continue;
            row.getCheckbox().click();
            break;
        }
    }
}
