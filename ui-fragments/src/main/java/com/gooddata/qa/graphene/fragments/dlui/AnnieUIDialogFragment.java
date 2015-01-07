package com.gooddata.qa.graphene.fragments.dlui;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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

    private String XPATH_SOURCE = "//.[@class='source-title' and text()='${datasource}']/../.";
    private String XPATH_DATASET =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']/../../.";
    private String XPATH_FIELD = "//.[text()='${fieldName}']";
    private String XPATH_FIELDS =
            "//.[contains(@class, 'dataset')]//label[text()='${dataset}']//../..//li[not(contains(@class, 'is-none'))]";
    private String XPATH_SELECTED_FIELD =
            "//.[contains(@class, 'checked-item') and text()='${selectedFieldTitle}']/..";

    public String getAnnieDialogHeadline() {
        return waitForElementVisible(annieDialogHeadline).getText();
    }

    public String getEmptyStateHeading() {
        return waitForElementVisible(BY_EMPTY_STATE_HEADING, browser).getText();
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(BY_EMPTY_STATE_MESSAGE, browser).getText();
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
        WebElement datasourceElement =
                waitForElementVisible(
                        By.xpath(XPATH_SOURCE.replace("${datasource}", datasource.getName())),
                        browser);
        if (!datasourceElement.findElements(BY_DROPRIGHT_ICON).isEmpty())
            datasourceElement.click();
        waitForElementVisible(BY_DROPDOWN_ICON, datasourceElement);
        assertEquals(datasourceElement.findElements(BY_DATASET).size(),
                datasetInSpecificFilter.size());
        for (final Dataset dataset : datasetInSpecificFilter) {
            final WebElement datasetElement =
                    waitForElementVisible(
                            By.xpath(XPATH_DATASET.replace("${dataset}", dataset.getName())),
                            browser);
            if (!datasetElement.findElements(BY_DROPRIGHT_ICON).isEmpty())
                datasetElement.findElement(BY_DROPRIGHT_ICON).click();
            waitForElementVisible(BY_DROPDOWN_ICON, datasetElement);
            final List<Field> fieldsInSpecificFilter = dataset.getFieldsInSpecificFilter(fieldType);
            assertEquals(
                    datasetElement.findElements(
                            By.xpath(XPATH_FIELDS.replace("${dataset}", dataset.getName()))).size(),
                    fieldsInSpecificFilter.size(), "Incorrect number of fields in dataset: "
                            + dataset.getName());
            checkAvailableFieldsOfDataset(datasetElement, fieldsInSpecificFilter);
        }
    }

    public void checkAvailableFieldsOfDataset(final WebElement datasetElement,
            Collection<Field> fields) {
        assertTrue(Iterables.all(fields, new Predicate<Field>() {

            @Override
            public boolean apply(Field field) {
                return datasetElement.findElements(
                        By.xpath(XPATH_FIELD.replace("${fieldName}", field.getFieldName()))).size() == 1;
            }

        }));
    }

    public void searchFields(String searchKey) {
        waitForElementVisible(searchInput).sendKeys(searchKey);
    }

    public void selectField(Field field) {
        getRoot().findElement(By.xpath(XPATH_FIELD.replace("${fieldName}", field.getFieldName())))
                .click();
        waitForElementVisible(selectionArea.findElement(By.xpath(XPATH_SELECTED_FIELD.replace(
                "${selectedFieldTitle}", field.getFieldName()))));
    }
}
