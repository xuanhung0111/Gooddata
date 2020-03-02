package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

public class PublishWorkspaceDialog extends AbstractFragment {
    @FindBy(className = "gd-input-search")
    private WebElement searchField;

    @FindBy(className = "s-select-button")
    private WebElement selectButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-workspace-radio-input")
    protected List<WebElement> workspacesList;


    public static PublishWorkspaceDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(PublishWorkspaceDialog.class,
                waitForElementVisible(className("s-select-workspace-dialog"), context));
    }

    public PublishWorkspaceDialog clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
        return this;
    }

    public PublishWorkspaceDialog clickSelect () {
        waitForElementVisible(selectButton).click();
        waitForFragmentNotVisible(this);
        return this;
    }

    public PublishWorkspaceDialog searchWorkspace(String name) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(searchField).click().sendKeys(name).build().perform();
        return this;
    }

    public PublishWorkspaceDialog selectedWorkspaceOnSearchList(String workspaceName) {
        waitForCollectionIsNotEmpty(workspacesList).stream().filter(e -> e.getText().contains(workspaceName)).findFirst().get().click();
        return this;
    }
}
