package com.gooddata.qa.graphene.fragments.indigo.user;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DeleteGroupDialog extends AbstractFragment {

    public static final By LOCATOR = By.className("delete-group-dialog");

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-delete")
    private WebElement deleteButton;

    @FindBy(css = ".gd-dialog-header>h3")
    private WebElement title;

    @FindBy(css = ".gd-dialog-content>p")
    private WebElement content;

    public void submit() {
        waitForElementVisible(deleteButton).click();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public String getBodyContent() {
        return waitForElementVisible(content).getText();
    }
}
