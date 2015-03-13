package com.gooddata.qa.graphene.fragments;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class AnnieUIDialogFragment extends AbstractFragment {

    @FindBy(css = ".gd-dialog-headline")
    private WebElement annieDialogHeadline;

    @FindBy(css = ".gd-dialog-close.icon-cross")
    private WebElement crossButton;

    @FindBy(css = "input.searchfield-input")
    private WebElement searchInput;

    @FindBy(xpath = "//.[contains(@class, 'dataset')]/..//li")
    private List<WebElement> fields;

    @FindBy(xpath = "//.[contains(@class, 'checked-item-title')]")
    private List<WebElement> selectedFields;

    @FindBy(css = ".empty-state")
    private WebElement emptyState;

    @FindBy(css = ".selection-area")
    private WebElement selectionArea;

    private String XPATH_FIELD_FILTER = "//a[text()='%s']";

    private By BY_EMPTY_STATE_HEADING = By.cssSelector(".empty-state-heading");
    private By BY_EMPTY_STATE_MESSAGE = By.cssSelector(".empty-state-paragraph");
    private By BY_DATASET = By
            .xpath("//.[contains(@class, 'dataset')]/../.[not(contains(@class, 'is-none'))]");
    private By BY_DROPRIGHT_ICON = By.cssSelector(".icon-dropright");
    private By BY_DROPDOWN_ICON = By.cssSelector(".icon-dropdown");
    private By BY_SELECTION_AREA = By.cssSelector(".selection-area");
    private By BY_INTEGRATION_STATUS = By.cssSelector(".integration-status");
    private By BY_APPLY_BUTTON = By
            .xpath("//.[contains(@class, 'button-positive') and text()='Apply']");
    private By BY_DISMISS_BUTTON = By.cssSelector(".btn-dismiss");
    private By BY_ICON_CROSS = By.cssSelector(".icon-cross");

    private String XPATH_SOURCE = "//.[@class='source-title' and text()='${datasource}']/../.";
    private String XPATH_DATASET =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']/../../.";
    private String XPATH_FIELD = "//.[text()='${fieldName}']";
    private String XPATH_FIELDS =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']//../..//li[not(contains(@class, 'is-none'))]";
    private String XPATH_SELECTED_FIELD =
            "//.[contains(@class, 'checked-item') and text()='${selectedFieldTitle}']/..";

    private static final String ANNIE_DIALOG_EMPTY_STATE_HEADING = "No additional data available.";

    private static final String ANNIE_DIALOG_EMPTY_STATE_MESSAGE =
            "Your project already contains all existing data. If you"
                    + " need to add more data, contact a project admin or GoodData Customer Support.";

    private static final String ANNIE_DIALOG_HEADLINE = "Add data";

    public String getAnnieDialogHeadline() {
        return waitForElementVisible(annieDialogHeadline).getText();
    }

    public String getEmptyStateHeading() {
        return waitForElementVisible(BY_EMPTY_STATE_HEADING, browser).getText();
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(BY_EMPTY_STATE_MESSAGE, browser).getText();
    }

    public void clickOnApplyButton() {
        waitForElementVisible(BY_APPLY_BUTTON, getRoot()).click();
    }

    public void clickOnDismissButton() {
        waitForElementVisible(BY_DISMISS_BUTTON, getRoot()).click();
    }

    public void selectFieldFilter(FieldTypes fieldType) {
        final WebElement fieldFilter =
                waitForElementVisible(
                        By.xpath(String.format(XPATH_FIELD_FILTER, fieldType.getFilterName())),
                        browser);
        fieldFilter.click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return fieldFilter.getAttribute("class").contains("is-active");
            }
        });
    }

    public void checkAvailableAdditionalFields(DataSource datasource, FieldTypes fieldType) {
        selectFieldFilter(fieldType);
        List<Dataset> datasetInSpecificFilter = datasource.getDatasetInSpecificFilter(fieldType);
        if (datasetInSpecificFilter.isEmpty()) {
            waitForElementVisible(emptyState);
            return;
        }

        chechAvailableDataSource(datasource, datasetInSpecificFilter, fieldType);
    }

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchInput).sendKeys(searchKey);
    }

    public void deselectFields(DataSource dataSource, Dataset dataset, Field... fields) {
        clickOnFields(dataSource, dataset, false, fields);
    }

    public void deselectFieldsInSelectionArea(Field... fields) {
        for (Field field : fields) {
            WebElement selectedField =
                    waitForElementVisible(
                            By.xpath(XPATH_SELECTED_FIELD.replace("${selectedFieldTitle}",
                                    field.getName())), selectionArea);
            waitForElementVisible(BY_ICON_CROSS, selectedField).click();
            waitForElementNotPresent(selectedField);
        }
    }

    public void selectFields(DataSource dataSource, Dataset dataset, Field... fields) {
        clickOnFields(dataSource, dataset, true, fields);
    }

    public void checkSelectionArea(Collection<Field> expectedFields) {
        if (expectedFields.size() == 0)
            assertTrue(getRoot().findElements(BY_SELECTION_AREA).isEmpty(),
                    "Selection area is displayed!");
        else {
            waitForElementVisible(selectionArea);
            waitForCollectionIsNotEmpty(selectedFields);
            assertEquals(selectedFields.size(), expectedFields.size(),
                    "The number of selected fields is incorrect!");
            List<String> selectedFieldTitles = Lists.newArrayList();
            for (final WebElement selectedField : selectedFields) {
                assertTrue(Iterables.any(expectedFields, new Predicate<Field>() {

                    @Override
                    public boolean apply(Field arg0) {
                        return arg0.getName().equals(selectedField.getText());
                    }
                }), "The field " + selectedField.getText() + "is not selected!");
                selectedFieldTitles.add(selectedField.getText());
            }
            assertTrue(Ordering.natural().isOrdered(selectedFieldTitles),
                    "Selected fields are not sorted!");
        }
    }

    public void checkIntegrationStatus() {
        waitForElementVisible(BY_INTEGRATION_STATUS, getRoot());
    }

    public void checkEmptyAnnieDialog() {
        assertEquals(getAnnieDialogHeadline(), ANNIE_DIALOG_HEADLINE);
        assertEquals(getEmptyStateHeading(), ANNIE_DIALOG_EMPTY_STATE_HEADING);
        assertEquals(getEmptyStateMessage(), ANNIE_DIALOG_EMPTY_STATE_MESSAGE);
    }

    private void verifyValidField(DataSource dataSource, final Dataset dataset, Field... fields) {
        assertNotNull(Iterables.find(dataSource.getDatasetInSpecificFilter(FieldTypes.ALL),
                new Predicate<Dataset>() {

                    @Override
                    public boolean apply(Dataset arg0) {
                        return arg0.getName().equals(dataset.getName());
                    }
                }), "Data source " + dataSource.getName() + " doesn't contain " + dataset.getName());
        for (final Field field : fields) {
            assertNotNull(Iterables.find(dataset.getFieldsInSpecificFilter(FieldTypes.ALL),
                    new Predicate<Field>() {

                        @Override
                        public boolean apply(Field arg0) {
                            return arg0.getName().equals(field.getName());
                        }
                    }), "Dataset " + dataset.getName() + " doesn't contain " + field.getName());
        }
    }

    private void clickOnFields(DataSource dataSource, Dataset dataset, boolean isChecked,
            Field... fields) {
        verifyValidField(dataSource, dataset, fields);
        WebElement dataSourceElement = selectDataSource(dataSource);
        WebElement datasetElement = selectDataset(dataSourceElement, dataset);
        for (Field field : fields) {
            clickOnField(datasetElement, field, isChecked);
        }
    }
    
    private void clickOnField(WebElement datasetElement, Field field, boolean isChecked) {
        waitForElementVisible(By.xpath(XPATH_FIELD.replace("${fieldName}", field.getName())),
                datasetElement).click();
        if (isChecked)
            waitForElementVisible(By.xpath(XPATH_SELECTED_FIELD.replace("${selectedFieldTitle}",
                    field.getName())), selectionArea);
        else
            waitForElementNotPresent(By.xpath(XPATH_SELECTED_FIELD.replace("${selectedFieldTitle}",
                    field.getName())));
    }

    private void chechAvailableDataSource(DataSource dataSource,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        WebElement datasourceElement = selectDataSource(dataSource);
        assertEquals(datasourceElement.findElements(BY_DATASET).size(),
                datasetInSpecificFilter.size());
        checkAvailableDatasets(datasourceElement, datasetInSpecificFilter, fieldType);
    }

    private void checkAvailableDatasets(WebElement dataSourceElement,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        for (Dataset dataset : datasetInSpecificFilter) {
            WebElement datasetElement = selectDataset(dataSourceElement, dataset);
            List<Field> fieldsInSpecificFilter = dataset.getFieldsInSpecificFilter(fieldType);
            assertEquals(
                    datasetElement.findElements(
                            By.xpath(XPATH_FIELDS.replace("${dataset}", dataset.getName()))).size(),
                    fieldsInSpecificFilter.size(), "Incorrect number of fields in dataset: "
                            + dataset.getName());
            checkAvailableFieldsOfDataset(datasetElement, fieldsInSpecificFilter);
        }
    }

    private void checkAvailableFieldsOfDataset(final WebElement datasetElement,
            Collection<Field> fields) {
        assertTrue(Iterables.all(fields, new Predicate<Field>() {

            @Override
            public boolean apply(Field field) {
                return datasetElement.findElements(
                        By.xpath(XPATH_FIELD.replace("${fieldName}", field.getName()))).size() == 1;
            }
        }));
    }

    private WebElement selectDataSource(DataSource dataSource) {
        WebElement dataSourceElement =
                waitForElementVisible(
                        By.xpath(XPATH_SOURCE.replace("${datasource}", dataSource.getName())),
                        browser);
        expandElement(dataSourceElement);
    
        return dataSourceElement;
    }

    private WebElement selectDataset(WebElement dataSourceElement, Dataset dataset) {
        WebElement datasetElement =
                waitForElementVisible(
                        By.xpath(XPATH_DATASET.replace("${dataset}", dataset.getName())),
                        dataSourceElement);
        expandElement(datasetElement);
    
        return datasetElement;
    }

    private void expandElement(WebElement element) {
        if (!element.findElements(BY_DROPRIGHT_ICON).isEmpty())
            waitForElementPresent(BY_DROPRIGHT_ICON, element).click();;
        waitForElementVisible(BY_DROPDOWN_ICON, element);
    }
}
