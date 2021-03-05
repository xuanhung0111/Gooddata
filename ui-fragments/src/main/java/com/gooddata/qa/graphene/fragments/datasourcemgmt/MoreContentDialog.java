package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class MoreContentDialog extends AbstractFragment {

    public static final String MENU_TABLE = "gd-menu-wrapper";

    @FindBy(className = "s-edit-menu-item")
    private WebElement editButton;

    @FindBy(className = "s-delete-menu-item")
    private WebElement deleteButton;

    @FindBy(className = "s-publish-ws-menu-item")
    private WebElement openPublishIntoWorkspace;

    @FindBy(className = "s-generate-os-menu-item")
    private WebElement outputStageButton;

    public static final MoreContentDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(MoreContentDialog.class, waitForElementVisible(className(MENU_TABLE), searchContext));
    }

    public void clickEditButton() {
        waitForElementVisible(editButton).click();
    }

    public boolean isEditButtonEnable() {
        final By editButton = className("s-edit-menu-item");
        return isElementPresent(editButton, browser);
    }

    public boolean isDeleteButtonEnable() {
        final By deleteButton = className("s-delete-menu-item");
        return isElementPresent(deleteButton, browser);
    }

    public DeleteDatasourceDialog clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
        return DeleteDatasourceDialog.getInstance(browser);
    }

    public PublishWorkspaceDialog openPublishIntoWorkSpaceDialog() {
        waitForElementVisible(openPublishIntoWorkspace).click();
        return PublishWorkspaceDialog.getInstance(browser);
    }

    public DatasourceMessageBar getErrorMessageDialog() {
        waitForElementVisible(outputStageButton).click();
        return DatasourceMessageBar.getInstance(browser);
    }

    public GenerateOutputStageDialog getGenerateDialog() {
        waitForElementVisible(outputStageButton).click();
        return GenerateOutputStageDialog.getInstance(browser);
    }

    public boolean isGenerateOutputStageEnabled() {
        return isElementPresent(By.className("s-generate-os-menu-item"), getRoot());
    }

    public boolean isPublishIntoWorkspaceEnabled() {
        return isElementPresent(By.className("s-publish-ws-menu-item"), getRoot());
    }
}
