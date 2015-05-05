package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.util.Collection;
import java.util.List;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DataSourceList extends AbstractFragment {

    private static final By BY_SOURCE_ITEM = By.cssSelector(".source-item:not(.is-none)");
    private static final By BY_DATASET = By.cssSelector("li:not(.is-none):not(.checkbox-item)");
    private static final By BY_FIELD = By.cssSelector(".checkbox-item:not(.is-none)");

    private static final By BY_DATASOURCE_TITLE = By.cssSelector(".source-title");
    private static final By BY_DATASET_TITLE = By.cssSelector("label");
    private static final By BY_FIELD_TITLE = By.cssSelector(".column-title");

    private static final By BY_DROPRIGHT_ICON = By.cssSelector(".icon-dropright");
    private static final By BY_DROPDOWN_ICON = By.cssSelector(".icon-dropdown");
    private static final By BY_FIELD_CHECKBOX = By.cssSelector(".ember-checkbox");

    private static final By BY_SOURCE_ITEM_SELECTION_HINT = By
            .cssSelector(".source-item-selection-hint");
    private static final By BY_DATASET_ITEM_SELECTION_HINT = By
            .cssSelector(".category-item-section-hint");

    public void checkSelectedFieldNumber(DataSource selectedDataSource) {
        WebElement dataSourceElement = selectDataSource(selectedDataSource);
        int totalSelectedFieldNumber = 0;
        for (Dataset selectedDataset : selectedDataSource.getSelectedDataSets()) {
            WebElement datasetElement = selectDataset(dataSourceElement, selectedDataset);
            assertEquals(datasetElement.findElement(BY_DATASET_ITEM_SELECTION_HINT).getText(),
                    String.format("(%d)", selectedDataset.getSelectedFields().size()));
            totalSelectedFieldNumber += selectedDataset.getSelectedFields().size();
        }
        assertEquals(dataSourceElement.findElement(BY_SOURCE_ITEM_SELECTION_HINT).getText(),
                String.format("%d selected", totalSelectedFieldNumber),
                "Incorrect selected field number in DataSource: " + selectedDataSource.getName());
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

    public void chechAvailableDataSource(DataSource dataSource,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        WebElement datasourceElement = selectDataSource(dataSource);
        assertEquals(datasourceElement.findElements(BY_DATASET).size(),
                datasetInSpecificFilter.size());
        checkAvailableDatasets(datasourceElement, datasetInSpecificFilter, fieldType);
    }

    public int getAvailableDataSourceNumber() {
        return getRoot().findElements(BY_SOURCE_ITEM).size();
    }

    public boolean isAvailable(final DataSource dataSource) {
        List<WebElement> dataSourceElements = getRoot().findElements(BY_SOURCE_ITEM);
        return Iterables.any(dataSourceElements, findDataSourcePredicate(dataSource));
    }

    private void clickOnField(WebElement datasetElement, final Field field, final boolean isChecked) {
        final WebElement selectedFieldElement =
                Iterables.find(datasetElement.findElements(BY_FIELD), new Predicate<WebElement>() {

                    @Override
                    public boolean apply(WebElement fieldElement) {
                        return field.getName().equals(
                                waitForElementVisible(BY_FIELD_TITLE, fieldElement).getText());
                    }
                });

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


    private void checkAvailableDatasets(WebElement dataSourceElement,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        for (Dataset dataset : datasetInSpecificFilter) {
            WebElement datasetElement = selectDataset(dataSourceElement, dataset);
            List<Field> fieldsInSpecificFilter = dataset.getFieldsInSpecificFilter(fieldType);
            assertEquals(datasetElement.findElements(BY_FIELD).size(),
                    fieldsInSpecificFilter.size(), "Incorrect number of fields in dataset: "
                            + dataset.getName());
            checkAvailableFieldsOfDataset(datasetElement, fieldsInSpecificFilter);
        }
    }

    private void checkAvailableFieldsOfDataset(final WebElement datasetElement,
            Collection<Field> fields) {
        List<WebElement> fieldElements = datasetElement.findElements(BY_FIELD);
        for (final Field field : fields) {
            assertNotNull(Iterables.find(fieldElements, new Predicate<WebElement>() {

                @Override
                public boolean apply(WebElement fieldElement) {
                    return field.getName().equals(fieldElement.getText());
                }
            }));
        }
    }

    private WebElement selectDataSource(final DataSource dataSource) {
        final List<WebElement> dataSourceElements = getRoot().findElements(BY_SOURCE_ITEM);
        WebElement dataSourceElement =
                Iterables.find(dataSourceElements, findDataSourcePredicate(dataSource));

        expandElement(dataSourceElement);

        return dataSourceElement;
    }

    private WebElement selectDataset(WebElement dataSourceElement, final Dataset dataset) {
        WebElement datasetElement =
                Iterables.find(dataSourceElement.findElements(BY_DATASET),
                        new Predicate<WebElement>() {

                            @Override
                            public boolean apply(WebElement datasetElement) {
                                return dataset.getName().equals(
                                        datasetElement.findElement(BY_DATASET_TITLE).getText());
                            }
                        });

        expandElement(datasetElement);

        return datasetElement;
    }

    private void expandElement(WebElement element) {
        if (!element.findElements(BY_DROPRIGHT_ICON).isEmpty())
            waitForElementPresent(BY_DROPRIGHT_ICON, element).click();;
        waitForElementVisible(BY_DROPDOWN_ICON, element);
    }

    private Predicate<WebElement> findDataSourcePredicate(final DataSource dataSource) {
        return new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement dataSourceElement) {
                return !dataSourceElement.getAttribute("class").contains("is-none")
                        && dataSource.getName().equals(
                                waitForElementPresent(BY_DATASOURCE_TITLE, dataSourceElement)
                                        .getText());
            }
        };
    }
}
