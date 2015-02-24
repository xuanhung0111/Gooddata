package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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

    private static final By GRANTEES = By.cssSelector(".grantee-candidate");

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
        return waitForCollectionIsNotEmpty(getGrantees()).size();
    }

    public void selectItem(final String name) {
    	final By loginSelector = By.cssSelector(".grantee-email");
    	final By groupNameSelector = By.cssSelector(".grantee-name");
    	final By groupSelector = By.cssSelector(".grantee-group");
    	
    	Iterables.find(waitForCollectionIsNotEmpty(getGrantees()), new Predicate<WebElement>() {
			@Override
			public boolean apply(WebElement e) {
				boolean isGroup = e.findElements(groupSelector).size() > 0;
				WebElement nameElement = e.findElement(isGroup ? groupNameSelector : loginSelector);
				
				return name.equals(nameElement.getText().trim());
			}
		}).click();
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

}
