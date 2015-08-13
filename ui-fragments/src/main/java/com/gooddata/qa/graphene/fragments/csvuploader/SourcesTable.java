package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import java.util.List;

public class SourcesTable extends AbstractTable {

    private static final By BY_SOURCE_NAME = By.className("s-source-name");
    private static final By BY_SOURCE_STATUS = By.className("s-source-status");

    public List<String> getSourceNames() {
        return getRows().stream()
                .map(row -> waitForElementVisible(BY_SOURCE_NAME, row).getText())
                .collect(toList());
    }

    public WebElement getSourceRow(final String sourceName) {
        notEmpty(sourceName, "sourceName cannot be empty!");

        return getRows().stream()
                .filter(row -> sourceName.equals(row.findElement(BY_SOURCE_NAME).getText()))
                .findFirst()
                .orElse(null);
    }

    public String getSourceStatus(final String sourceName) {
        final WebElement sourceRow = getSourceRow(sourceName);

        notNull(sourceRow, "Source with name '" + sourceName + "' not found.");

        return waitForElementVisible(BY_SOURCE_STATUS, sourceRow).getText();
    }

    public int getNumberOfSources() {
        return getNumberOfRows();
    }
}
