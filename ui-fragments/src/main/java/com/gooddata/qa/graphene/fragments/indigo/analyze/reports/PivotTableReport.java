package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.PivotAggregationPopup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.isShortenedTitleDesignByCss;
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
    private static final String PIVOT_COLUMN_INDEX_CLASS_NAME_PREFIX = "gd-column-index-";
    private static final String CLASS_NAME_SEPARATOR = " ";
    private static final String TABLE_HEADER_VALUE_SELECTOR = ".s-header-cell-label span";

    @FindBy(css = TABLE_HEADER_ALL_CSS)
    private List<WebElement> headers;

    @FindBy(css = ".ag-body-viewport .gd-table-row")
    private List<WebElement> rows;

    @FindBy(css = ".ag-center-cols-viewport .gd-table-row .gd-table-row-subtotal:not(.s-gd-cell-hide)")
    private List<WebElement> subTotalCells;

    @FindBy(css = ".ag-body-viewport .s-value")
    private WebElement attributeValuePresent;

    @FindBy(css = ".gd-measure-column .s-value")
    private List<WebElement> valueMeasuresPresent;

    @FindBy(css = ".ag-numeric-header .gd-pivot-table-header-label")
    private List<WebElement> headersMeasure;

    @FindBy(css = ".ag-header-group-cell-with-group[col-id='0_0']")
    private List<WebElement> headersColumns;

    @FindBy(css = ".gd-row-attribute-column-header.gd-column-group-header")
    private List<WebElement> headersRows;

    @FindBy(css = ".ag-floating-bottom .gd-table-row")
    private List<WebElement> grandTotalsRows;

    @FindBy(css = ".gd-row-attribute-column:not(.gd-cell-hide) .s-value")
    private List<WebElement> rowAttributeColumns;

    @FindBy(css = ".ag-header-group-cell-with-group:not([col-id='0_0']) .gd-pivot-table-header-label")
    private List<WebElement> columnGroupHeaders;

    public List<String> getRowAttributeColumns() {
        waitForElementVisible(attributeValuePresent);

        return waitForCollectionIsNotEmpty(rowAttributeColumns).stream()
            .filter(ElementUtils::isElementVisible)
            .map(WebElement::getText)
            .collect(toList());
    }

    public List<String> getValueMeasuresPresent() {
        waitForElementVisible(attributeValuePresent);
        return getElementTexts(valueMeasuresPresent);
    }

    public List<String> getColumnGroupHeaders() {
        waitForElementVisible(attributeValuePresent);

        return columnGroupHeaders.stream()
            .filter(ElementUtils::isElementVisible)
            .map(WebElement::getText)
            .collect(toList());
    }

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

    public boolean containsGrandTotals() {
        waitForElementVisible(attributeValuePresent);

        return !grandTotalsRows.isEmpty();
    }

    public List<List<String>> getGrandTotalsContent() {
        return getContent(grandTotalsRows);
    }

    public List<String> getSubTotalsContent() {
        waitForElementVisible(attributeValuePresent);

        return waitForCollectionIsNotEmpty(subTotalCells).stream()
            .filter(ElementUtils::isElementVisible)
            .map(WebElement::getText)
            .collect(toList());
    }

    public boolean hasSubTotals() {
        waitForElementVisible(attributeValuePresent);

        return !subTotalCells.isEmpty();
    }

    public List<String> getGrandTotalValues(final AggregationItem type) {
        return getGrandTotalsContent()
                .stream()
                .filter(values -> values.size() > 0 && values.get(0).equals(type.getFullName()))
                .map(values -> values.subList(1, values.size()))
                .findFirst()
                .orElse(null);
    }

    public WebElement getTotalCellElement(int columnIndex, int cellIndex) {
        waitForElementVisible(attributeValuePresent);

        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(grandTotalsRows).stream()
            .filter(ElementUtils::isElementVisible)
            .map(tableRow -> tableRow.findElements(className("s-table-cell")))
            .collect(toList());
        return elements.get(cellIndex).get(columnIndex);
    }

    public List<List<String>> getBodyContent() {
        return getContent(rows);
    }
    
    public List<List<String>> getBodyContentColumn(int columnIndex) {
        return getContentOfColumn(rows, columnIndex);
    }

    private List<List<String>> getContent(List<WebElement> contentElements) {
        waitForElementVisible(attributeValuePresent);

        return waitForCollectionIsNotEmpty(contentElements).stream()
                .filter(ElementUtils::isElementVisible)
                .map(row -> row.findElements(className("s-table-cell")))
                .map(rowCells -> rowCells
                        .stream()
                        // because of custom React LoadingRendered, sometimes the first pivot attribute cell is the last in the DOM
                        // (visually it is always the first as it is absolutely positioned but wrong result is returned by this method if cells are not sorted)
                        .sorted(Comparator.comparingInt(this::parseColumnIndex))
                        .map(WebElement::getText)
                        .collect(toList())
                )
                .collect(toList());
    }

    private List<List<String>> getContentOfColumn(List<WebElement> contentElements, int columnIndex) {
        waitForElementVisible(attributeValuePresent);   
        return waitForCollectionIsNotEmpty(contentElements).stream()
                .filter(ElementUtils::isElementVisible)
                .map(row -> row.findElements(By.className(format("gd-column-index-%s", columnIndex))))
                .map(rowCells -> rowCells
                        .stream()
                        // because of custom React LoadingRendered, sometimes the first pivot attribute cell is the last in the DOM
                        // (visually it is always the first as it is absolutely positioned but wrong result is returned by this method if cells are not sorted)
                        .sorted(Comparator.comparingInt(this::parseColumnIndex))
                        .map(WebElement::getText)
                        .collect(toList())
                )
                .collect(toList());
    }

    private int parseColumnIndex(final WebElement cell) {
        final String classAttributeValue = cell.getAttribute("class");
        if (classAttributeValue == null) {
            return -1;
        }
        return Arrays.stream(classAttributeValue.split(CLASS_NAME_SEPARATOR))
                .filter(className -> className.matches(PIVOT_COLUMN_INDEX_CLASS_NAME_PREFIX + "\\d+"))
                .findFirst()
                .map(indexClassName -> Integer.parseInt(indexClassName.substring(PIVOT_COLUMN_INDEX_CLASS_NAME_PREFIX.length())))
                .orElse(-1);
    }

    public WebElement getCellElement(String columnTitle, int cellIndex) {
        int columnIndex = getHeaders().indexOf(columnTitle);
        return getCellElement(columnIndex, cellIndex);
    }

    public WebElement getCellElement(int columnIndex, int cellIndex) {
        waitForElementVisible(attributeValuePresent);

        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(rows).stream()
            .filter(ElementUtils::isElementVisible)
            .map(tableRow -> tableRow.findElements(className("s-table-cell")))
            .collect(toList());
        return elements.get(cellIndex).get(columnIndex);
    }

    public String getCellElementText(String columnTitle, int headerIndex, int rowIndex) {
        return getCellElement(columnTitle, headerIndex, rowIndex).getText();
    }

    public PivotTableReport sortBaseOnHeader(final String name) {
        WebElement webElement = waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .collect(toList())
            .get(0);

        waitForElementVisible(webElement).click();
        // Because the mouse pointer hovered the attribute which is sorted, so making unexpected result
        // Move the mouse pointer to the top-left corner of the fragment to avoid this
        ElementUtils.moveToElementActions(waitForElementVisible(getRoot()), 0, 0).perform();
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

    public boolean isBurgerMenuVisible(String header, int columnIndex) {
        return waitForElementPresent(getHeaderElement(header, columnIndex).findElement(className(BURGER_MENU_CLASS_NAME)))
            .getAttribute("class").contains("gd-pivot-table-header-menu--show");
    }

    public PivotTableReport addTotal(AggregationItem type, String columnTitle, int columnIndex) {
        waitForElementVisible(attributeValuePresent);

        PivotAggregationPopup pivotAggregationPopup = openAggregationPopup(columnTitle, columnIndex);
        if (!pivotAggregationPopup.isItemChecked(type)) {
            pivotAggregationPopup.selectItem(type);
        }
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public PivotTableReport removeTotal(AggregationItem type, String columnTitle, int columnIndex) {
        waitForElementVisible(attributeValuePresent);

        PivotAggregationPopup pivotAggregationPopup = openAggregationPopup(columnTitle, columnIndex);
        if (pivotAggregationPopup.isItemChecked(type)) {
            pivotAggregationPopup.selectItem(type);
        }
        return this;
    }

    public PivotTableReport hoverItem(WebElement item) {
        // Selenium 3.8.1 + gecko 0.20.0 has problem with action #moveToElement when working inside docker container.
        // The element hovered by this action will not trigger the event so it's meaningless when need to check a
        // tooltip show from that. This additional #moveByOffset is a work around to make it work properly.

        getActions().moveToElement(getRoot(), 0 ,0).moveToElement(item)
            .moveByOffset(1, 1).perform();
        sleepTightInSeconds(1);
        return this;
    }

    public PivotTableReport hoverOnBurgerMenuColumn(String columnTitle, int columnIndex) {
        hoverItem(waitForElementPresent(
            getHeaderElement(columnTitle, columnIndex).findElement(className("ag-react-container"))));
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

    public boolean isBurgerMenuPresent() {
        return isElementPresent(className(BURGER_MENU_CLASS_NAME), getRoot());
    }

    private boolean isHeaderSorted(final String name, final String css, int index) {
         WebElement webElement = waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .collect(toList())
            .get(index);

        return waitForElementVisible(webElement).findElements(tagName("span"))
            .stream().anyMatch(e -> e.getAttribute("class").contains(css));
    }

    private void expandBurgerMenuColumn(String columnTitle, int columnIndex) {
        Graphene.waitGui().pollingEvery(1, TimeUnit.SECONDS)
            .withTimeout(5, TimeUnit.SECONDS).until(browser -> {
            hoverOnBurgerMenuColumn(columnTitle, columnIndex);

            return isElementVisible(getHeaderElement(columnTitle, 0)
                .findElement(By.className(BURGER_MENU_CLASS_NAME)));
        });

        WebElement webElementBurger = waitForElementVisible(getHeaderElement(columnTitle, 0))
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

    public boolean isCellUnderlined(String columnTitle, int cellIndex) {
        WebElement cell = getCellElement(columnTitle, cellIndex);
        hoverItem(cell);
        return cell.getCssValue("text-decoration").contains("underline");
    }

    public boolean isTotalCellUnderlined(int columnIndex, int cellIndex) {
        WebElement cell = getTotalCellElement(columnIndex, cellIndex);
        hoverItem(cell);
        return cell.getCssValue("text-decoration").contains("underline");
    }

    public boolean isShortenHeader(String headerName, int columnIndex, int width) {
        final WebElement headerElement = getHeaderElement(headerName, columnIndex);
        final WebElement headerValueElement = headerElement.findElement(By.cssSelector(TABLE_HEADER_VALUE_SELECTOR));
        return isShortenedTitleDesignByCss(headerValueElement, width);
    }
}
