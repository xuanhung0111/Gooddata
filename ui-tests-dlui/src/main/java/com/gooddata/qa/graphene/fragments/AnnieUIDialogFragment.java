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

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class AnnieUIDialogFragment extends AbstractFragment {

    private static final String ANNIE_DIALOG_HEADLINE = "Add data";
    private static final String ERROR_ANNIE_DIALOG_HEADLINE = "Data loading failed";

    private static final String ANNIE_DIALOG_EMPTY_STATE_HEADING = "No additional data available.";
    private static final String ANNIE_DIALOG_EMPTY_STATE_MESSAGE =
            "Your project already contains all existing data. If you"
                    + " need to add more data, contact a project admin or GoodData Customer Support .";

    private static final String ERROR_ANNIE_DIALOG_MESSAGE_1 =
            "An error occured during loading, so we couldn’t load your data.";
    private static final String ERROR_ANNIE_DIALOG_MESSAGE_2 =
            "Please contact a project admin or GoodData Customer Support .";
    private static final String GOODDATA_SUPPORT_LINK =
            "https://support.gooddata.com/?utm_source=de&utm_campaign=error_message&utm_medium=dialog";

    private static final By BY_SELECTION_AREA = By.cssSelector(".selection-area");
    private static final By BY_DATASOURCE_LIST = By.cssSelector(".annie-dialog-list-container");

    @FindBy(css = ".gd-dialog-headline")
    private WebElement annieDialogHeadline;

    @FindBy(css = ".empty-state")
    private WebElement emptyState;

    @FindBy(css = "input.searchfield-input")
    private WebElement searchInput;

    @FindBy(css = ".button-positive")
    private WebElement positiveButton;

    @FindBy(css = ".button-secondary")
    private WebElement secondaryButton;

    private By BY_EMPTY_STATE_HEADING = By.cssSelector(".empty-state-heading");
    private By BY_EMPTY_STATE_MESSAGE = By.cssSelector(".empty-state-paragraph");

    private By BY_INTEGRATION_STATUS = By.cssSelector(".integration-status");
    private By BY_ANNIE_DIALOG_HEADLINE = By.cssSelector(".gd-dialog-headline");
    private By BY_RUNNING_STATE_HEADING = By.cssSelector(".running-state-heading");
    private By BY_RUNNING_STATE_PARAGRAPH = By.cssSelector(".running-state-paragraph");
    private By BY_INTEGRATION_STATUS_MESSAGE = By.tagName("p");

    public String getAnnieDialogHeadline() {
        return waitForElementVisible(annieDialogHeadline).getText();
    }

    public String getEmptyStateHeading() {
        return waitForElementVisible(BY_EMPTY_STATE_HEADING, browser).getText();
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(BY_EMPTY_STATE_MESSAGE, browser).getText();
    }

    public String getIntegrationFirstMessage() {
        return waitForElementVisible(BY_INTEGRATION_STATUS, getRoot())
                .findElement(By.xpath("//p[1]")).getText().trim();
    }

    public String getIntegrationSecondMessage() {
        return waitForElementVisible(BY_INTEGRATION_STATUS, getRoot())
                .findElement(By.xpath("//p[2]")).getText().trim();
    }

    public boolean isPositiveButtonPresent() {
        return browser.findElements(By.className("button-positive")).size() > 0;
    }

    public void assertErrorMessage() {
        WebElement integrationStatus = waitForElementVisible(BY_INTEGRATION_STATUS, getRoot());
        assertEquals(getAnnieDialogHeadline(), ERROR_ANNIE_DIALOG_HEADLINE);
        assertEquals(getIntegrationFirstMessage(), ERROR_ANNIE_DIALOG_MESSAGE_1,
                "Incorrect error message on Annie dialog!");
        assertEquals(getIntegrationSecondMessage(), ERROR_ANNIE_DIALOG_MESSAGE_2,
                "Incorrect error message on Annie dialog!");
        assertEquals(integrationStatus.findElement(By.xpath("//p[2]/a")).getAttribute("href"),
                GOODDATA_SUPPORT_LINK, "Incorrect support link in error message of Annie dialog!");
        clickOnCloseButton();
    }

    public void clickOnApplyButton() {
        assertEquals(waitForElementVisible(positiveButton).getText(), "Apply");
        assertFalse(positiveButton.getAttribute("class").contains("disabled"),
                "Apply button is not enable!");
        waitForElementVisible(positiveButton).click();
        waitForElementNotPresent(positiveButton);
    }

    public void clickOnDismissButton() {
        clickOnSecondaryButton("Dismiss");
    }

    public void clickOnCloseButton() {
        clickOnSecondaryButton("Close");
    }

    public void assertNoDataSelectedState() {
        assertEquals(getRoot().findElements(BY_SELECTION_AREA).size(), 0);
        assertEquals(waitForElementVisible(positiveButton).getText(), "No data selected",
                "Incorrect apply button title when no data selected!");
        assertTrue(positiveButton.getAttribute("class").contains("disabled"),
                "Apply button is not disable!");
    }

    public void selectFieldFilter(FieldTypes fieldType) {
        final WebElement fieldFilter = waitForElementVisible(fieldType.getFilterBy(), browser);
        fieldFilter.click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver browser) {
                return fieldFilter.getAttribute("class").contains("is-active");
            }
        });
    }

    public void checkAvailableAdditionalFields(DataSource datasource, FieldTypes fieldType) {
        selectFieldFilter(fieldType);
        List<Dataset> datasetInSpecificFilter = datasource.getAvailableDatasets(fieldType);
        if (datasetInSpecificFilter.isEmpty()) {
            assertEquals(waitForElementVisible(emptyState).getText(),
                    fieldType.getEmptyStateMessage(), "Incorrect empty state message: "
                            + emptyState.getText());
            return;
        }
        getDataSourceList()
                .chechAvailableDataSource(datasource, datasetInSpecificFilter, fieldType);
    }

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchInput).sendKeys(searchKey);
    }

    public void assertSelectedFieldNumber(DataSource selectedDataSource) {
        getDataSourceList().checkSelectedFieldNumber(selectedDataSource);
    }

    public void deselectFields(DataSource deselectedDataSource) {
        getDataSourceList().clickOnFields(deselectedDataSource, false);
    }

    public void deselectFieldsInSelectionArea(Field... fields) {
        getSelectionArea().deselectFields(fields);
    }

    public void selectFields(DataSource selectedDataSource) {
        getDataSourceList().clickOnFields(selectedDataSource, true);
    }

    public void checkSelectionArea(Collection<Field> expectedFields) {
        if (expectedFields.size() == 0)
            assertTrue(getRoot().findElements(BY_SELECTION_AREA).isEmpty(),
                    "Selection area is displayed!");
        else {
            getSelectionArea().checkSelectedFields(expectedFields);
        }
    }

    public void checkDataAddingProgress() {
        WebElement integrationStatus = waitForElementVisible(BY_INTEGRATION_STATUS, getRoot());
        assertEquals(getAnnieDialogHeadline(), "Adding data...", "Incorrect Annie dialog headline!");
        assertEquals(waitForElementVisible(BY_RUNNING_STATE_HEADING, integrationStatus).getText(),
                "Adding data to your project...", "Incorrect running state heading!");
        assertEquals(
                waitForElementVisible(BY_RUNNING_STATE_PARAGRAPH, integrationStatus).getText(),
                "Uploading this data for these fields may take a while - we will send you an email when it's ready. "
                        + "If you close this dialog, you can still track the progress of data loading on this page:",
                "Incorrect running state message!");
    }

    public void checkSuccessfulDataAddingResult() {
        checkDataAddingResult(true);
    }

    public void checkFailedDataAddingResult() {
        checkDataAddingResult(false);
    }

    public void checkEmptyAnnieDialog() {
        assertEquals(getAnnieDialogHeadline(), ANNIE_DIALOG_HEADLINE);
        assertEquals(getEmptyStateHeading(), ANNIE_DIALOG_EMPTY_STATE_HEADING);
        assertEquals(getEmptyStateMessage(), ANNIE_DIALOG_EMPTY_STATE_MESSAGE);
    }

    private void clickOnSecondaryButton(String buttonTitle) {
        assertEquals(waitForElementVisible(secondaryButton).getText(), buttonTitle);
        waitForElementVisible(secondaryButton).click();
        browser.switchTo().defaultContent();
    }

    private void checkDataAddingResult(boolean isSuccessful) {
        final WebElement integrationStatus =
                waitForElementVisible(BY_INTEGRATION_STATUS, getRoot());
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver browser) {
                return !waitForElementVisible(BY_ANNIE_DIALOG_HEADLINE, browser).getText().equals(
                        "Adding data...");
            }
        });
        if (isSuccessful) {
            assertEquals(waitForElementVisible(BY_ANNIE_DIALOG_HEADLINE, integrationStatus)
                    .getText(), "Data added successfuly!", "Incorrect dialog headline!");
            assertEquals(waitForElementVisible(BY_INTEGRATION_STATUS_MESSAGE, integrationStatus)
                    .getText(), "Data has been added to your project.",
                    "Incorrect successful message!");
        } else {
            assertEquals(waitForElementVisible(BY_ANNIE_DIALOG_HEADLINE, integrationStatus)
                    .getText(), "Failed to add data", "Incorrect dialog headline!");
            assertEquals(integrationStatus.findElements(BY_INTEGRATION_STATUS_MESSAGE).get(0)
                    .getText(),
                    "We couldn’t add this data because it contains an error. Show log file",
                    "Incorrect failed message!");
            assertEquals(integrationStatus.findElements(BY_INTEGRATION_STATUS_MESSAGE).get(1)
                    .getText(), "Please contact a project admin or GoodData Customer Support .",
                    "Incorrect failed message!");
        }
    }

    private SelectionArea getSelectionArea() {
        return Graphene.createPageFragment(SelectionArea.class,
                waitForElementVisible(BY_SELECTION_AREA, getRoot()));
    }

    private DataSourceList getDataSourceList() {
        return Graphene.createPageFragment(DataSourceList.class,
                waitForElementVisible(BY_DATASOURCE_LIST, getRoot()));
    }
}
