package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.base.Predicate;

public class AnnieUIDialogFragment extends AbstractFragment {

    private static final By BY_SELECTION_AREA = By.cssSelector(".selection-area");
    public static final By BY_DATASOURCES = By.cssSelector(".annie-dialog-list-container");

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
    private By BY_INTEGRATION_STATUS_MESSAGE = By.tagName("p");

    public WebElement getEmptyState() {
        return waitForElementVisible(emptyState);
    }

    public String getAnnieDialogHeadline() {
        return waitForElementVisible(annieDialogHeadline).getText().trim();
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

    public WebElement getIntegrationStatus() {
        return waitForElementVisible(BY_INTEGRATION_STATUS, getRoot());
    }

    public List<WebElement> getIntegrationStatusMessages() {
        return getIntegrationStatus().findElements(BY_INTEGRATION_STATUS_MESSAGE);
    }

    public WebElement getPositiveButton() {
        return waitForElementVisible(positiveButton);
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

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchInput).sendKeys(searchKey);
    }

    public void deselectFields(DataSource deselectedDataSource) {
        getDataSources().clickOnFields(deselectedDataSource, false);
    }

    public void deselectFieldsInSelectionArea(Field... fields) {
        getSelectionArea().deselectFields(fields);
    }

    public void selectFields(DataSource selectedDataSource) {
        getDataSources().clickOnFields(selectedDataSource, true);
    }

    public boolean isNoSelectionArea() {
        return getRoot().findElements(BY_SELECTION_AREA).isEmpty();
    }

    public SelectionArea getSelectionArea() {
        return Graphene.createPageFragment(SelectionArea.class,
                waitForElementVisible(BY_SELECTION_AREA, getRoot()));
    }

    public DataSourcesFragment getDataSources() {
        return Graphene.createPageFragment(DataSourcesFragment.class,
                waitForElementVisible(BY_DATASOURCES, getRoot()));
    }

    private void clickOnSecondaryButton(String buttonTitle) {
        assertEquals(waitForElementVisible(secondaryButton).getText(), buttonTitle);
        waitForElementVisible(secondaryButton).click();
        browser.switchTo().defaultContent();
    }
}
