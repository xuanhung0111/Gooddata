package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.fragments.indigo.analyze.PivotAggregationPopup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

public class PivotTableReport extends AbstractFragment {
    private static final String TABLE_HEADER_ARROW_UP_CLASS_NAME = "gd-pivot-table-header-arrow-up";
    private static final String TABLE_SORT_ARROW_CLASS_NAME = "s-sort-direction-arrow";
    private static final String TABLE_HEADER_ALL_CSS
        = ".gd-column-group-header:not(.ag-header-group-cell-no-group)";
    private static final String BURGER_MENU_CLASS_NAME = "gd-pivot-table-header-menu";

    @FindBy(css = TABLE_HEADER_ALL_CSS)
    private List<WebElement> headers;

    @FindBy(className = "gd-table-row")
    private List<WebElement> rows;

    @FindBy(css = ".ag-body-container .s-value")
    private WebElement attributeValuePresent;

    @FindBy(css = ".ag-numeric-header .gd-pivot-table-header-label")
    private List<WebElement> headersMeasure;

    @FindBy(css = ".ag-header-group-cell-with-group[col-id='0_0']")
    private List<WebElement> headersColumns;

    @FindBy(css = ".gd-row-attribute-column-header.gd-column-group-header")
    private List<WebElement> headersRows;

    // represents the most top row header
    public List<String> getHeadersColumn() {
        if (headersColumns.isEmpty()) {
            return emptyList();
        }
        return getElementTexts(headersColumns);
    }

    public List<String> getHeadersRow() {
        if (headersRows.isEmpty()) {
            return emptyList();
        }
        return getElementTexts(headersRows);
    }

    public List<String> getHeadersMeasure() {
        if (headersMeasure.isEmpty()) {
            return emptyList();
        }
        return getElementTexts(headersMeasure);
    }

    public List<String> getHeaders() {
        return getElementTexts(waitForCollectionIsNotEmpty(headers));
    }

    public List<List<String>> getContent() {
        waitForElementVisible(attributeValuePresent);

        return waitForCollectionIsNotEmpty(rows).stream()
            .filter(ElementUtils::isElementVisible)
            .map(e -> e.findElements(className("s-table-cell")))
            .map(es -> es.stream().map(WebElement::getText).collect(toList()))
            .collect(toList());
    }

    public WebElement getCellElement(String columnTitle, int cellIndex) {
        waitForElementVisible(attributeValuePresent);

        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(rows).stream()
            .filter(ElementUtils::isElementVisible)
            .map(tableRow -> tableRow.findElements(className("s-table-cell")))
            .collect(toList());
        int columnIndex = getHeaders().indexOf(columnTitle);
        return elements.get(cellIndex).get(columnIndex);
    }

    public String getCellElementText(String columnTitle, int headerIndex, int rowIndex) {
        return getCellElement(columnTitle, headerIndex, rowIndex).getText();
    }

    public PivotTableReport sortBaseOnHeader(final String name) {
        waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .collect(toList())
            .get(0)
            .click();
        // Because the mouse pointer hovered the attribute which is sorted, so making unexpected result
        // Move the mouse pointer to the top-left corner of the fragment to avoid this
        ElementUtils.moveToElementActions(getRoot(), 0, 0).perform();
        return this;
    }

    public boolean isRowHeaderSortedUp(final String name, int index) {
        return isHeaderSorted(name, TABLE_HEADER_ARROW_UP_CLASS_NAME, index);
    }

    public boolean isRowHeaderSortedUp(final String name) {
        return isRowHeaderSortedUp(name, 0);
    }

    public boolean isTableSortArrowPresent() {
        return isElementPresent(className(TABLE_SORT_ARROW_CLASS_NAME), getRoot());
    }

    public boolean isBurgerMenuVisible() {
        return waitForElementPresent(className(BURGER_MENU_CLASS_NAME), getRoot())
            .getAttribute("class").contains("gd-pivot-table-header-menu--show");
    }

    public PivotTableReport addNewTotals(AggregationItem type, String columnTitle, int columnIndex) {
        openAggregationPopup(columnTitle, columnIndex).selectItem(type);
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public PivotTableReport hoverItem(WebElement item) {
        // Selenium 3.8.1 + gecko 0.20.0 has problem with action #moveToElement when working inside docker container.
        // The element hovered by this action will not trigger the event so it's meaningless when need to check a
        // tooltip show from that. This additional #moveByOffset is a work around to make it work properly.
        getActions().moveToElement(item).moveByOffset(1, 1).perform();
        return this;
    }

    public PivotTableReport hoverOnBurgerMenuColumn(String columnTitle, int columnIndex) {
        hoverItem(getHeaderElement(columnTitle, columnIndex));
        return this;
    }

    public PivotTableReport collapseBurgerMenuColumn(String columnTitle) {
        WebElement webElementBurger = getHeaderElement(columnTitle, 0)
            .findElement(By.className(BURGER_MENU_CLASS_NAME));
        if (isBurgerMenuCollapsed(webElementBurger)) {
            return this;
        }
        webElementBurger.click();
        waitForElementNotPresent(PivotAggregationPopup.LOCATOR, browser);
        return this;
    }

    public PivotAggregationPopup openAggregationPopup(String columnTitle, int columnIndex) {
        expandBurgerMenuColumn(columnTitle, columnIndex);
        return Graphene.createPageFragment(PivotAggregationPopup.class,
            waitForElementVisible(PivotAggregationPopup.LOCATOR, browser));
    }

    private boolean isHeaderSorted(final String name, final String css, int index) {
        return waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .collect(toList())
            .get(index)
            .findElements(tagName("span"))
            .stream()
            .anyMatch(e -> e.getAttribute("class").contains(css));
    }

    private void expandBurgerMenuColumn(String columnTitle, int columnIndex) {
        hoverOnBurgerMenuColumn(columnTitle, columnIndex);
        WebElement webElementBurger = getHeaderElement(columnTitle, 0)
            .findElement(By.className(BURGER_MENU_CLASS_NAME));
        if (isBurgerMenuCollapsed(webElementBurger)) {
            webElementBurger.click();
        }
        waitForElementVisible(PivotAggregationPopup.LOCATOR, browser);
    }

    private Boolean isBurgerMenuCollapsed(WebElement webElementColumn) {
        return !webElementColumn.getAttribute("class").contains("gd-pivot-table-header-menu--open");
    }

    private WebElement getHeaderElement(String columnTitle, int columnIndex) {
        return waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> columnTitle.equals(e.getText()))
            .collect(toList())
            .get(columnIndex);
    }

    private WebElement getCellElement(String columnTitle, int headerIndex, int rowIndex) {
        waitForElementVisible(attributeValuePresent);

        int columnIndex = getHeadersMeasure().indexOf(columnTitle);
        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(rows).stream()
            .filter(ElementUtils::isElementVisible)
            .map(e -> e.findElements(By.className(format("gd-column-measure-%s", columnIndex))))
            .collect(toList());
        return elements.get(rowIndex).get(headerIndex);
    }
}
