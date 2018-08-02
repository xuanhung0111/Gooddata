package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.isShortendTilteDesignByCss;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

/**
 * finding by span instead of BY_LINK in sortBaseOnHeader()
 */
public class TableReport extends AbstractFragment {

    @FindBy(css = ".public_fixedDataTable_header ." + CELL_CONTENT)
    private List<WebElement> headers;

    @FindBy(className = "public_fixedDataTable_bodyRow")
    private List<WebElement> rows;

    @FindBy(className = "indigo-totals-enable-column-button")
    private WebElement addTotalsCellButton;

    @FindBy(css = ".indigo-table-footer-cell.col-0:not(.indigo-totals-add-cell)")
    private List<WebElement> totalsTitleElements;

    @FindBy(className = REMOVE_TOTALS_CELL_BUTTON)
    private WebElement removeTotalsCellButton;

    private static final By BY_TOTALS_RESULTS =
            cssSelector(".col-0:not(.fixedDataTableCellLayout_wrap1):not(.indigo-totals-add-cell)");
    private static final By ADD_TOTAL_ROW_BUTTON = className("indigo-totals-add-row-button");
    private static final String CELL_CONTENT = "public_fixedDataTableCell_cellContent";
    private static final String REMOVE_TOTALS_CELL_BUTTON = "indigo-totals-disable-column-button";

    public List<String> getHeaders() {
        return getElementTexts(waitForCollectionIsNotEmpty(headers));
    }

    public boolean isShortenHeader(String headerName, int width) {
        return isShortendTilteDesignByCss(getHeader(headerName), width);
    }

    public String getTooltipText(String headerName) {
        getActions().moveToElement(getHeader(headerName)).perform();
        return waitForElementVisible(className("content"), browser).getText();
    }

    public List<List<String>> getContent() {
        return waitForCollectionIsNotEmpty(rows).stream()
            .map(e -> e.findElements(className(CELL_CONTENT)))
            .map(es -> es.stream().map(WebElement::getText).collect(toList()))
            .collect(toList());
    }

    public WebElement getCellElement(String columnTitle, int cellIndex) {
        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(rows).stream()
                .map(tableRow -> tableRow.findElements(className(CELL_CONTENT)))
                .map(cells -> cells.stream()
                        .map(webElement -> webElement.findElement(By.className("s-table-cell"))).collect(toList()))
                .collect(toList());
        int columnIndex = getHeaders().indexOf(columnTitle);
        WebElement cell = elements.get(cellIndex).get(columnIndex);
        return cell;
    }

    public WebElement getCellElement(int columnIndex, int cellIndex) {
        List<List<WebElement>> elements = waitForCollectionIsNotEmpty(rows).stream()
                .map(tableRow -> tableRow.findElements(className(CELL_CONTENT)))
                .map(cells -> cells.stream()
                        .map(webElement -> webElement.findElement(By.className("s-table-cell"))).collect(toList()))
                .collect(toList());
        return elements.get(cellIndex).get(columnIndex);
    }

    public boolean isCellUnderlined(String columnTitle, int cellIndex) {
        WebElement cell = getCellElement(columnTitle, cellIndex);
        hoverItem(cell);
        return cell.getCssValue("text-decoration").contains("underline");
    }

    public String getFormatFromValue() {
        return waitForCollectionIsNotEmpty(rows).stream()
            .map(e -> e.findElements(className(CELL_CONTENT)))
            .map(es -> es.stream()
                    .map(e -> e.findElement(tagName("span")))
                    .map(e -> e.getAttribute("style"))
                    .collect(toList()))
            .flatMap(e -> e.stream())
            .filter(Objects::nonNull)
            .distinct()
            .findAny()
            .orElse("");
    }

    public TableReport sortBaseOnHeader(final String name) {
        waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .map(e -> e.findElement(By.tagName("span")))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find table header: " + name))
            .click();
        return this;
    }

    public boolean isHeaderSortedUp(final String name) {
        return isHeaderSorted(name, "gd-table-arrow-up");
    }

    public boolean isHeaderSortedDown(final String name) {
        return isHeaderSorted(name, "gd-table-arrow-down");
    }

    public boolean hasTotalsResult() {
        return isElementVisible(BY_TOTALS_RESULTS, getRoot());
    }

    public boolean isRemoveTotalsResultCellButtonVisible() {
        return isElementVisible(removeTotalsCellButton);
    }

    public boolean isRemoveTotalsResultButtonVisible() {
        return isElementVisible(className("indigo-totals-row-remove-button"), browser);
    }

    public boolean isAddTotalResultCellButtonVisible() {
        return isElementVisible(addTotalsCellButton);
    }

    public TableReport addNewTotals(AggregationItem type, String metricName) {
        openAggregationPopup(metricName).selectItem(type);
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public TableReport addTotalsForCell(AggregationItem type, String metricName) {
        hoverItem(getTotalsElement(type, metricName));
        addTotalsCellButton.click();
        waitForElementNotVisible(addTotalsCellButton);
        return this;
    }

    public TableReport deleteTotalsResultRow(AggregationItem type) {
        hoverItem(getTotalsElement(type, null));
        waitForElementVisible(className("indigo-totals-row-remove-button"), browser).click();
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public TableReport deleteTotalsResultCell(AggregationItem type, String metricName) {
        WebElement totalsElement = getTotalsElement(type, metricName);
        hoverItem(totalsElement);
        waitForElementVisible(className(REMOVE_TOTALS_CELL_BUTTON), totalsElement).click();
        //To be handled wait for computing in test
        //If totals cell is only one.
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public TableReport hoverItem(WebElement item) {
        getActions().moveToElement(item).perform();
        return this;
    }

    public List<String> getEnabledAggregations(String metricName) {
        List<String> list = openAggregationPopup(metricName).getEnabledItemList();
        closeAggregationPopup(metricName);
        return list;
    }

    public List<String> getAggregations(String metricName) {
        List<String> list = openAggregationPopup(metricName).getItemsList();
        closeAggregationPopup(metricName);
        return list;
    }

    public String getTotalsValue(AggregationItem type, String metricName) {
        return getTotalsElement(type, metricName).getText();
    }

    public WebElement getTotalsElement(AggregationItem type, String metricName) {
        return getRoot().findElements(cssSelector(format(".indigo-table-footer-cell.col-%d:not(.indigo-totals-add-cell)",
                metricName == null ? 0 : getColumn(metricName)))).get(getFooterRow(type.fullName));
    }

    public void hoverOnColumn(String columnName) {
        final String column = format("col-%d", getColumn(columnName));
        //Just need to hover on any row of column
        hoverItem(getRoot().findElements(className(column)).get(0));
    }

    public boolean isTotalsElementShowed(String columnName) {
        try {
            final String script = format("return window.getComputedStyle(document.querySelector(" +
                            "'.indigo-table-footer-cell.col-%d .indigo-totals-add-row-button:not(.hidden)'),':before')" +
                            ".getPropertyValue('content')",
                    columnName == null ? 0 : getColumn(columnName));
            JavascriptExecutor js = (JavascriptExecutor) browser;
            String content = (String) js.executeScript(script);
            return "\"Σ\"".equals(content);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isAddAggregationButtonOnCell(AggregationItem type, String columnName) {
        hoverItem(getTotalsElement(type, columnName));
        return isAddTotalResultCellButtonVisible();
    }

    public List<String> getAggregationRows() {
        return totalsTitleElements.stream().map(e -> e.getText()).collect(toList());
    }

    private int getColumn(String columnTitle) {
        return getHeaders().indexOf(columnTitle);
    }

    private int getFooterRow(String rowTitle) {
        return totalsTitleElements.stream().map(e-> e.getText()).collect(toList()).indexOf(rowTitle);
    }

    public AggregationPopup openAggregationPopup(String metricName) {
        hoverOnColumn(metricName);
        waitForElementVisible(getAddRowButtonSelector(metricName), getRoot()).click();
        return Graphene.createPageFragment(AggregationPopup.class,
                waitForElementVisible(AggregationPopup.LOCATOR, browser));
    }

    public void closeAggregationPopup(String metricName) {
        waitForElementVisible(getAddRowButtonSelector(metricName), getRoot()).click();
        waitForElementNotPresent(AggregationPopup.LOCATOR, browser);
    }

    private By getAddRowButtonSelector(String metricName) {
        String column = format("col-%d", getColumn(metricName));
        return cssSelector(format(".%s .indigo-totals-add-row-button", column));
    }

    private boolean isHeaderSorted(final String name, final String css) {
        return waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .findFirst()
            .get()
            .findElements(tagName("span"))
            .stream()
            .anyMatch(e -> e.getAttribute("class").contains(css));
    }

    private WebElement getHeader(String headerName) {
        return waitForCollectionIsNotEmpty(headers)
            .stream()
            .filter(e -> e.getText().equals(headerName))
            .findFirst()
            .get();
    }

    public static class AggregationPopup extends AbstractFragment {
        public static final By LOCATOR = By.className("indigo-totals-select-type-list");

        @FindBy(className = "gd-list-item-shortened")
        private List<WebElement> items;

        @FindBy(css = ".gd-list-item-shortened:not(.indigo-totals-select-type-item-disabled)")
        private List<WebElement> enableItems;

        public List<String> getItemsList() {
            return items.stream().map(e-> e.getText()).collect(toList());
        }

        public void selectItem(AggregationItem item) {
            waitForElementVisible(className(format("s-totals-select-type-item-%s", item.metadataName)), getRoot()).click();
        }

        private List<String> getEnabledItemList() {
            return enableItems.stream().map(e-> e.getText()).collect(toList());
        }
    }

    public enum AggregationItem {
        SUM("Sum", "sum", "Sum"),
        MAX("Max", "max", "Max"),
        MIN("Min", "min", "Min"),
        AVG("Avg", "avg", "Avg"),
        MEDIAN("Median", "med", "Median"),
        ROLLUP("Rollup (Total)", "nat", "Total");

        private String fullName;
        private String metadataName;
        private String rowName;

        AggregationItem(String fullName, String metadataName, String rowName) {
            this.fullName = fullName;
            this.metadataName = metadataName;
            this.rowName = rowName;
        }

        public static AggregationItem fromString(String fullName) {
            for (AggregationItem aggregation : AggregationItem.values()) {
                if (aggregation.getFullName().equals(fullName)) {
                    return aggregation;
                }
            }
            throw new IllegalArgumentException("Displayed name does not match with any AggregationItem");
        }

        public static List<String> getAllRowNames() {
            return Arrays.stream(AggregationItem.values()).map(AggregationItem::getRowName).collect(toList());
        }

        public static List<String> getAllFullNames() {
            return Arrays.stream(AggregationItem.values()).map(AggregationItem::getFullName).collect(toList());
        }

        public String getFullName() {
            return fullName;
        }

        public String getMetadataName() {
            return metadataName;
        }

        public String getRowName() {
            return rowName;
        }
    }
}
