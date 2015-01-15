package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class AddGranteesDialog extends AbstractFragment {

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    @FindBy(css = ".searchfield-input")
    private WebElement searchForGranteeInput;

    @FindBy(css = ".s-btn-share")
    private WebElement shareButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(css = ".gd-list-view-noResults")
    private WebElement noResultsOutput;

    private static final By GRANTEES = By.cssSelector(".grantee-email");

    public int getGranteesCount(final String searchText, boolean expectResult) throws InterruptedException {
        waitForElementVisible(root);

        waitForElementVisible(searchForGranteeInput).clear();
        searchForGranteeInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(getGrantees());

        searchForGranteeInput.clear();
        searchForGranteeInput.sendKeys(searchText);
        if (expectResult) {
            return getNumberOfGrantees();
        } else {
            waitForElementVisible(noResultsOutput);
            return 0;
        }
    }

    public int getNumberOfGrantees() {
        waitForCollectionIsNotEmpty(getGrantees());
        return getGrantees().size();
    }

    public void selectItem(String name) {
        for (WebElement e : getGrantees()) {
            if (!name.equals(e.getText().trim())) {
                continue;
            }
            e.click();
            break;
        }
    }

    public List<WebElement> getGrantees() {
        return waitForElementVisible(root).findElements(GRANTEES);
    }

    public void share() {
        waitForElementVisible(shareButton).click();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

}
