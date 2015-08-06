package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class TableReport extends AbstractFragment {

    @FindBy(css = ".ember-table-header-cell .ember-table-content")
    private List<WebElement> headers;

    @FindBy(css = ".ember-table-body-container .ember-table-table-row:not([style*='display:none'])>div")
    private List<WebElement> rows;

    private static final By LEFT_CONTENT = By.cssSelector(".text-align-left>span");
    private static final By RIGHT_CONTENT = By.cssSelector(".text-align-right");

    public List<String> getHeaders() {
        waitForCollectionIsNotEmpty(headers);
        return Lists.newArrayList(Collections2.transform(headers,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    public List<List<String>> getContent() {
        waitForCollectionIsNotEmpty(rows);
        List<List<String>> result = Lists.newArrayList();

        for (WebElement row: rows) {
            List<String> content = Lists.newArrayList();
            if (!row.findElements(LEFT_CONTENT).isEmpty()) {
                content.add(row.findElement(LEFT_CONTENT).getText().trim());
            }

            for (WebElement col: row.findElements(RIGHT_CONTENT)) {
                content.add(col.findElement(By.tagName("span")).getText().trim());
            }

            result.add(content);
        }

        return result;
    }

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
