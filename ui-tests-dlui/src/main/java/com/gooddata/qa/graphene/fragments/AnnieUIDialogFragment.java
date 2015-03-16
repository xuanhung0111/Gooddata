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

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class AnnieUIDialogFragment extends AbstractFragment {

    private static final String ANNIE_DIALOG_HEADLINE = "Add data";

    private static final String ANNIE_DIALOG_EMPTY_STATE_HEADING = "No additional data available.";

    private static final String ANNIE_DIALOG_EMPTY_STATE_MESSAGE =
            "Your project already contains all existing data. If you"
                    + " need to add more data, contact a project admin or GoodData Customer Support.";

    private final static String CSS_SELECTION_AREA = ".selection-area";

    @FindBy(css = ".gd-dialog-headline")
    private WebElement annieDialogHeadline;

    @FindBy(css = ".gd-dialog-close.icon-cross")
    private WebElement closeDialogButton;

    @FindBy(css = ".empty-state")
    private WebElement emptyState;

    @FindBy(css = "input.searchfield-input")
    private WebElement searchInput;

    @FindBy(css = ".annie-dialog-list-container")
    private DataSourceList dataSourceList;

    @FindBy(css = CSS_SELECTION_AREA)
    private SelectionArea selectionArea;

    @FindBy(css = ".button-positive")
    private WebElement applyButton;

    @FindBy(css = ".btn-dismiss")
    private WebElement dismissButton;

    private By BY_EMPTY_STATE_HEADING = By.cssSelector(".empty-state-heading");
    private By BY_EMPTY_STATE_MESSAGE = By.cssSelector(".empty-state-paragraph");

    private By BY_INTEGRATION_STATUS = By.cssSelector(".integration-status");

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
        assertFalse(applyButton.getAttribute("class").contains("disabled"),
                "Apply button is not enable!");
        waitForElementVisible(applyButton).click();
    }

    public void clickOnDismissButton() {
        waitForElementVisible(dismissButton).click();
    }

    public void selectFieldFilter(FieldTypes fieldType) {
        final WebElement fieldFilter = waitForElementVisible(fieldType.getFilterBy(), browser);
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
        waitForElementVisible(dataSourceList.getRoot());
        dataSourceList.chechAvailableDataSource(datasource, datasetInSpecificFilter, fieldType);
    }

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchInput).sendKeys(searchKey);
    }

    public void deselectFields(DataSource dataSource, Dataset dataset, Field... fields) {
        verifyValidField(dataSource, dataset, fields);
        waitForElementVisible(dataSourceList.getRoot());
        dataSourceList.clickOnFields(dataSource, dataset, false, fields);
    }

    public void deselectFieldsInSelectionArea(Field... fields) {
        waitForElementVisible(selectionArea.getRoot());
        selectionArea.deselectFields(fields);
    }

    public void selectFields(DataSource dataSource, Dataset dataset, Field... fields) {
        verifyValidField(dataSource, dataset, fields);
        waitForElementVisible(dataSourceList.getRoot());
        dataSourceList.clickOnFields(dataSource, dataset, true, fields);
    }

    public void checkSelectionArea(Collection<Field> expectedFields) {
        if (expectedFields.size() == 0)
            assertTrue(getRoot().findElements(By.cssSelector(CSS_SELECTION_AREA)).isEmpty(),
                    "Selection area is displayed!");
        else {
            waitForElementVisible(selectionArea.getRoot());
            selectionArea.checkSelectedFields(expectedFields);
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
}
