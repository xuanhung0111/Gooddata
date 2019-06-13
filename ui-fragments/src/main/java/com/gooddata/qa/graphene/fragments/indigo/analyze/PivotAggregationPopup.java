package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;

public class PivotAggregationPopup extends AbstractFragment {

    public static final By LOCATOR = By.className("s-table-header-menu-content");
    public static final String AGGREGATE_ITEM_CSS = ".s-menu-aggregation-%s .s-menu-aggregation-inner";
    public static final String AGGREGATE_TABLE_SUBMENU_CLASS_NAME = "s-table-header-submenu-content";

    @FindBy(className = "gd-menu-item")
    private List<WebElement> items;

    @FindBy(css = ".s-table-header-submenu-content .s-menu-aggregation-inner")
    private WebElement ofAllRows;

    public List<String> getItemsList() {
        return waitForCollectionIsNotEmpty(items).stream().map(WebElement::getText).collect(toList());
    }

    public void selectItem(AggregationItem item) {
        waitForElementVisible(cssSelector(
            format(AGGREGATE_ITEM_CSS, item.getMetadataName())), getRoot()).click();
    }

    public Boolean isItemChecked(AggregationItem type) {
        int indexItem = getElementTexts(waitForCollectionIsNotEmpty(items)).indexOf(type.getFullName());
        return waitForCollectionIsNotEmpty(items)
            .get(indexItem).getAttribute("class").contains("is-checked");
    }

    public PivotAggregationPopup hoverItem(AggregationItem item) {
        WebElement webElement  = waitForElementVisible(
            cssSelector(format(AGGREGATE_ITEM_CSS, item.getMetadataName())), getRoot());

            // The Firefox browser has problem with action #moveToElement when working inside docker container.
            // This additional moveTo getRoot(0,0) is a work around to make it work properly.
            // {@link https://github.com/SeleniumHQ/docker-selenium/issues/910}

            getActions().moveToElement(getRoot(), 0 ,0).moveToElement(webElement)
                .moveByOffset(1, 1).perform();
            waitForElementVisible(By.className(AGGREGATE_TABLE_SUBMENU_CLASS_NAME), getRoot());

        return this;
    }

    public AnalysisPage selectRowsItem(String item) {
        return setRowsItem(item, true);
    }

    public AnalysisPage unSelectRowsItem(String item) {
        return setRowsItem(item, false);
    }

    private AnalysisPage setRowsItem(String item, boolean isSelected) {
        WebElement webElementItem = getRowsItem(item);

        if (isRowsItemChecked(webElementItem) != isSelected) {
            getActions().moveToElement(webElementItem).moveByOffset(1, 1).click().perform();
        }

        return AnalysisPage.getInstance(browser).waitForReportComputing();
    }

    public Boolean isRowsItemChecked(WebElement webElement) {
        return webElement.getAttribute("class").contains("is-checked");
    }

    public WebElement getRowsItem(String item) {
        return waitForCollectionIsNotEmpty(items)
            .stream()
            .filter(e -> item.equals(e.getText()))
            .findFirst()
            .get();
    }
}
