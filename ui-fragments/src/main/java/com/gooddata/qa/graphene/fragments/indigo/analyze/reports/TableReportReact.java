package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Objects;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

public class TableReportReact extends AbstractFragment {

    @FindBy(css = ".public_fixedDataTable_header ." + CELL_CONTENT)
    private List<WebElement> headers;

    @FindBy(css = ".fixedDataTableRowLayout_rowWrapper:not([data-reactid *= '$header'])")
    private List<WebElement> rows;

    private static final String CELL_CONTENT = "public_fixedDataTableCell_cellContent";

    public List<String> getHeaders() {
        return getElementTexts(waitForCollectionIsNotEmpty(headers));
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

    public TableReportReact sortBaseOnHeader(final String name) {
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

    private boolean isHeaderSorted(final String name, final String css) {
        return waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .findFirst()
            .get()
            .findElements(tagName("span"))
            .stream()
            .anyMatch(e -> e.getAttribute("class").contains(css));
    }
}
