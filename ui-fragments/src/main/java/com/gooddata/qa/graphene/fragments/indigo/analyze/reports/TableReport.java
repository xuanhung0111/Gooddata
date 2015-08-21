package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

import java.util.List;

import org.openqa.selenium.By;
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

    // TODO: has issue: https://jira.intgdc.com/browse/CL-8159 Value in table doesn't apply color format
    public String getFormatFromValue(String value) {
        waitForCollectionIsNotEmpty(rows);
        for (WebElement row: rows) {
            WebElement ele = row.findElement(By.cssSelector(".text-align-right>span>span"));
            if (value.equals(ele.getText())) {
                return ele.getAttribute("style");
            }
        }
        System.out.println("Cannot find value: " + value);
        return "";
    }
}
