package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.google.common.base.Predicate;

public class DataSourcesFragment extends AbstractFragment {

    private static final By BY_SOURCE_ITEM = By.cssSelector(".source-item:not(.is-none)");
    private static final By BY_DATASET = By.cssSelector("li:not(.is-none):not(.checkbox-item)");
    private static final By BY_FIELD = By.cssSelector(".checkbox-item:not(.is-none)");

    private static final By BY_FIELD_TITLE = By.cssSelector(".column-title");

    private static final By BY_DROPRIGHT_ICON = By.cssSelector(".icon-dropright");
    private static final By BY_DROPDOWN_ICON = By.cssSelector(".icon-dropdown");
    private static final By BY_FIELD_CHECKBOX = By.cssSelector(".ember-checkbox");

    private static final By BY_SOURCE_ITEM_SELECTION_HINT = By
            .cssSelector(".source-item-selection-hint");
    private static final By BY_DATASET_ITEM_SELECTION_HINT = By
            .cssSelector(".category-item-section-hint");

    public String getSourceItemSelectionHint(WebElement dataSourceElement) {
        return dataSourceElement.findElement(BY_SOURCE_ITEM_SELECTION_HINT).getText();
    }

    public String getDatasetItemSelectionHint(WebElement datasetElement) {
        return datasetElement.findElement(BY_DATASET_ITEM_SELECTION_HINT).getText();
    }

    public int getAvailableDataSourceCount() {
        return getRoot().findElements(BY_SOURCE_ITEM).size();
    }

    public int getAvailableDatasetCount(WebElement dataSourceElement) {
        return dataSourceElement.findElements(BY_DATASET).size();
    }

    public int getAvailableFieldCount(WebElement datasetElement) {
        return getAvailableFields(datasetElement).size();
    }

    public List<WebElement> getAvailableFields(WebElement datasetElement) {
        return datasetElement.findElements(BY_FIELD);
    }

    public void clickOnFields(DataSource selectedDataSource, boolean isChecked) {
        WebElement dataSourceElement = selectDataSource(selectedDataSource);
        for (Dataset selectedDataset : selectedDataSource.getSelectedDataSets()) {
            WebElement datasetElement = selectDataset(dataSourceElement, selectedDataset);
            for (Field field : selectedDataset.getSelectedFields()) {
                clickOnField(datasetElement, field, isChecked);
            }
        }
    }

    public boolean isAvailable(final DataSource dataSource) {
        List<WebElement> dataSourceElements = getRoot().findElements(BY_SOURCE_ITEM);
        return dataSource.hasCorrespondingWebElement(dataSourceElements);
    }

    public WebElement selectDataSource(final DataSource dataSource) {
        final List<WebElement> dataSourceElements = getRoot().findElements(BY_SOURCE_ITEM);
        WebElement dataSourceElement = dataSource.getCorrespondingWebElement(dataSourceElements);

        expandElement(dataSourceElement);

        return dataSourceElement;
    }

    public WebElement selectDataset(WebElement dataSourceElement, final Dataset dataset) {
        WebElement datasetElement =
                dataset.getCorrespondingWebElement(dataSourceElement.findElements(BY_DATASET));

        expandElement(datasetElement);

        return datasetElement;
    }

    private void clickOnField(WebElement datasetElement, final Field field, final boolean isChecked) {
        final WebElement selectedFieldElement =
                field.getCorrespondingWebElement(datasetElement.findElements(BY_FIELD));
    
        waitForElementVisible(BY_FIELD_CHECKBOX, selectedFieldElement).click();
    
        Graphene.waitGui().until(new Predicate<WebDriver>() {
    
            @Override
            public boolean apply(WebDriver browser) {
                boolean result =
                        waitForElementVisible(BY_FIELD_TITLE, selectedFieldElement).getAttribute(
                                "class").contains("is-strong");
                return isChecked ? result : !result;
            }
        });
    }

    private void expandElement(WebElement element) {
        if (!element.findElements(BY_DROPRIGHT_ICON).isEmpty())
            waitForElementPresent(BY_DROPRIGHT_ICON, element).click();
        waitForElementVisible(BY_DROPDOWN_ICON, element);
    }
}
