package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Objects;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
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

    @FindBy(className = "indigo-totals-add-row-button")
    private WebElement addTotalsRowButton;

    @FindBy(className = "indigo-totals-enable-column-button")
    private WebElement addTotalsCellButton;

    @FindBy(css = ".indigo-table-footer-cell.col-0:not(.indigo-totals-add-cell)")
    private List<WebElement> totalsTitleElements;

    @FindBy(className = "indigo-totals-disable-column-button")
    private WebElement removeTotalsCellButton;

    private static final String CELL_CONTENT = "public_fixedDataTableCell_cellContent";

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
        hoverItem(getTotalsElement(type, metricName));
        waitForElementVisible(removeTotalsCellButton).click();
        waitForElementNotVisible(removeTotalsCellButton);
        //To be handled wait for computing in test
        //If totals cell is only one.
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public TableReport hoverItem(WebElement item) {
        getActions().moveToElement(item).perform();
        return this;
    }

    public List<String> getListAggregation(String metricName) {
        List<String> list = openAggregationPopup(metricName).getItemsList();
        //click again to close Aggregation Popup
        waitForElementVisible(addTotalsRowButton).click();
        return list;
    }

    public String getTotalsValue(AggregationItem type, String metricName) {
        return getTotalsElement(type, metricName).getText();
    }

    public WebElement getTotalsElement(AggregationItem type, String metricName) {
        return getRoot().findElements(cssSelector(format(".indigo-table-footer-cell.col-%d:not(.indigo-totals-add-cell)",
                metricName == null ? 0 : getColumn(metricName)))).get(getFooterRow(type.fullName));
    }

    private int getColumn(String columnTilte) {
        return getHeaders().indexOf(columnTilte);
    }

    private int getFooterRow(String rowTitle) {
        return totalsTitleElements.stream().map(e-> e.getText()).collect(toList()).indexOf(rowTitle);
    }

    private AggregationPopup openAggregationPopup(String metricName) {
        final By CELL_CONTENT = By.className(format("col-%d", getColumn(metricName)));
        hoverItem(waitForElementVisible(
            rows.stream()
                .findFirst()
                .get()
                .findElement(CELL_CONTENT)));
        waitForElementVisible(addTotalsRowButton).click();
        return Graphene.createPageFragment(AggregationPopup.class,
                waitForElementVisible(className("indigo-totals-select-type-list"), browser));
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

    private class AggregationPopup extends AbstractFragment {

        @FindBy(css = ".gd-list-item:not(.gd-list-item-header)")
        private List<WebElement> items;

        private List<String> getItemsList() {
            return items.stream().map(e-> e.getText()).collect(toList());
        }

        private void selectItem(AggregationItem item) {
            waitForElementVisible(className(format("s-totals-select-type-item-%s", item.shortenedName)), getRoot()).click();
        }
    }

    public enum AggregationItem {
        SUM("Sum", "sum"),
        MAX("Max", "max"),
        MIN("Min", "min"),
        AVG("Avg", "avg"),
        MEDIAN("Median", "med"),
        ROLLUP("Total", "nat");

        private String fullName;
        private String shortenedName;

        AggregationItem(String fullName, String shortenedName) {
            this.fullName = fullName;
            this.shortenedName = shortenedName;
        }

        public String getFullName() {
            return fullName;
        }

        public String getShortenedName() {
            return shortenedName;
        }
    }
}
