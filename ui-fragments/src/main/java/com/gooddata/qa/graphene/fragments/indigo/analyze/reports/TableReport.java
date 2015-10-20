package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

import java.util.List;
import java.util.Objects;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class TableReport extends AbstractFragment {

    @FindBy(css = ".public_fixedDataTable_header ." + CELL_CONTENT)
    private List<WebElement> headers;

    @FindBy(css = ".fixedDataTableRowLayout_rowWrapper:not([data-reactid *= '$header'])")
    private List<WebElement> rows;

    private static final String CELL_CONTENT = "public_fixedDataTableCell_cellContent";

    public List<String> getHeaders() {
        return waitForCollectionIsNotEmpty(headers).stream()
            .map(WebElement::getText)
            .collect(toList());
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
            .map(es -> es.stream().map(e -> e.getAttribute("style")).collect(toList()))
            .flatMap(e -> e.stream())
            .filter(Objects::nonNull)
            .distinct()
            .findAny()
            .orElse("");
    }

    public TableReport sortBaseOnHeader(final String name) {
        waitForCollectionIsNotEmpty(headers).stream()
            .filter(e -> name.equalsIgnoreCase(e.getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find table header: " + name))
            .click();
        return this;
    }
}
