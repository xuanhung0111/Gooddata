package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AddGranteesDialog extends AbstractFragment {

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    @FindBy(css = ".searchfield-input")
    private WebElement searchForGranteeInput;

    @FindBy(css = ".s-btn-share")
    private WebElement shareButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(css = ".grantee-footer a")
    private WebElement userManagementLink;

    private final String NO_RESULTS_OUTPUT_LOCATOR = ".gd-list-view-noResults";

    private static final By GRANTEES = By.cssSelector(".grantee-candidate");

    public int getGranteesCount(final String searchText, boolean expectResult) {
        waitForElementVisible(root);

        waitForElementVisible(searchForGranteeInput).clear();
        searchForGranteeInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(getGrantees());

        searchForGranteeInput.clear();
        searchForGranteeInput.sendKeys(searchText);
        sleepTightInSeconds(1);
        if (expectResult) {
            return getNumberOfGrantees();
        } else {
            String mesage = waitForElementVisible(By.cssSelector(NO_RESULTS_OUTPUT_LOCATOR), browser).getText();
            if (isSearchFieldContainString()) {
                assertEquals(mesage, "No matching user name or email address exists in this project.");
            } else {
                assertEquals(mesage, "This dashboard is already shared with all users.");
            }
            return 0;
        }
    }

    private boolean isSearchFieldContainString() {
        waitForElementVisible(searchForGranteeInput);
        return searchForGranteeInput.findElement(BY_PARENT).findElements(By.cssSelector("span")).size() == 2;
    }

    public int getNumberOfGrantees() {
        return waitForCollectionIsNotEmpty(getGrantees()).size();
    }

    public void searchAndSelectItem(final String name) {
        final By loginSelector = By.cssSelector(".grantee-email");
        final By groupNameSelector = By.cssSelector(".grantee-name");

        //this make sure the next search is not failed due to caching the previous search result
        searchForGranteeInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(getGrantees());
        searchForGranteeInput.clear();
        
        //just search if name is user. UserGroup cannot be searched.
        if (!isUserGroup(name)) {
            searchForGranteeInput.sendKeys(name);
            sleepTightInSeconds(1);
        }

        waitForCollectionIsNotEmpty(getGrantees());

        getGrantees().stream()
            .filter( e -> {
            WebElement nameElement = e.findElement(isUserGroup(name) ? groupNameSelector : loginSelector);
            return name.equals(nameElement.getText().trim());
            }).findFirst().get().click();
    }

    private boolean isUserGroup(String name) {
        //regular expression for email format: ([\\w\\d+-.]+)@((?:[\\w]+\\.)+)([a-zA-Z]{2,4})
        return !(name.matches("([\\w\\d+-.]+)@((?:[\\w]+\\.)+)([a-zA-Z]{2,4})"));
    }

    public List<WebElement> getGrantees() {
        return waitForElementVisible(root).findElements(GRANTEES);
    }

    public void share() {
        waitForElementVisible(shareButton).click();
        waitForElementNotVisible(getRoot());
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

    public void openUserManagementPage() {
        waitForElementVisible(userManagementLink).click();
    }

    public boolean isUserGroupLinkShown() {
        return waitForElementVisible(By.className("grantee-footer"), browser).findElements(By.cssSelector("a"))
                .size() > 0;
    }
}
