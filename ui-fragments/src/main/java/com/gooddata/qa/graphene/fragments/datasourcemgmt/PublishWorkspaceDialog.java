package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class PublishWorkspaceDialog extends AbstractFragment {
    @FindBy(className = "s-dialog-submit-button")
    private WebElement searchField;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement workspace;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement cancelButton;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement selectButton;


    public static PublishWorkspaceDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(PublishWorkspaceDialog.class,
                waitForElementVisible(className("s-dataset-delete-dialog"), context));
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickSelect () {
        waitForElementVisible(selectButton).click();
        waitForFragmentNotVisible(this);
    }

    public void selectWorkspace () {
        waitForElementVisible(workspace).click();
    }

    public PublishWorkspaceDialog searchWorkspace(String name) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(searchField).click().sendKeys(name).build().perform();
        return this;
    }
}
