package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
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

    public List<String> getHeaders() {
        waitForCollectionIsNotEmpty(headers);
        return  Lists.newArrayList(Collections2.transform(headers,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    public List<List<String>> getContent() {
        waitForCollectionIsNotEmpty(rows);
        List<List<String>> result = new ArrayList<List<String>>();

        for (WebElement row : rows) {
            result.add(Arrays.asList(
                    row.findElement(By.cssSelector(".text-align-left>span")).getText().trim(),
                    row.findElement(By.cssSelector(".text-align-right>span")).getText().trim()
            ));
        }

        return result;
    }
}
