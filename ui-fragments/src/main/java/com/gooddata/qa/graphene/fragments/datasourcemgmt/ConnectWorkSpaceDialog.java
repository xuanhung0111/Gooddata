package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static org.openqa.selenium.By.className;

public class ConnectWorkSpaceDialog extends AbstractFragment {

    public static final String CONNECT_WORKSPACE_DIALOG = "s-select-workspace-dialog";

    @FindBy(className = "gd-input-search")
    private WebElement searchField;

    @FindBy(className = "s-select-button")
    private WebElement selectButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-workspace-radio-input")
    protected List<WebElement> workspacesList;

    public static final ConnectWorkSpaceDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ConnectWorkSpaceDialog.class, waitForElementVisible(className(CONNECT_WORKSPACE_DIALOG), context));
    }

    public ConnectWorkSpaceDialog clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
        return this;
    }

    public LogicalDataModelPage clickSelect() {
        waitForElementVisible(selectButton).click();
        waitForFragmentNotVisible(this);
        return LogicalDataModelPage.getInstance(browser);
    }

    public ConnectWorkSpaceDialog searchWorkspace(String name) {
        Actions driverActions = getActions();
        driverActions.moveToElement(searchField).click().sendKeys(name).build().perform();
        return this;
    }

    public ConnectWorkSpaceDialog selectedWorkspaceOnSearchList(String workspaceID) {
        waitForCollectionIsNotEmpty(workspacesList).stream().filter(e -> e.getText().contains(workspaceID)).findFirst().get().click();
        return this;
    }
}
