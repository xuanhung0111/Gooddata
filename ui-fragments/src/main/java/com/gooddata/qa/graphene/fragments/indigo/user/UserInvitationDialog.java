package com.gooddata.qa.graphene.fragments.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.google.common.base.Joiner;

public class UserInvitationDialog extends AbstractFragment {

    public static final By LOCATOR = By.className("invitationDialog");

    @FindBy(id = "invitationDialog-emailsInput")
    private WebElement invitationEmails;

    @FindBy(className = "invitationDialog-roles")
    private WebElement roles;

    @FindBy(id = "invitationDialog-message")
    private WebElement invitationMessage;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-invite_people")
    private WebElement inviteButton;

    public void invitePeople(UserRoles role, String message, String... emails) {
        waitForFragmentVisible(this);
        if (emails.length == 0) {
            throw new IllegalArgumentException("Must provide at least 1 email.");
        }
        enterEmails(emails).selectRoles(role).enterMessage(message);
        waitForElementVisible(inviteButton).click();
    }

    public String getErrorMessage() {
        waitForElementVisible(invitationEmails).click();
        return waitForElementVisible(By.className("content"), browser).getText().trim();
    }

    public void cancelInvitation() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private UserInvitationDialog enterEmails(String... emails) {
        waitForElementVisible(invitationEmails).sendKeys(Joiner.on(",").join(emails));
        return this;
    }

    private UserInvitationDialog selectRoles(UserRoles role) {
        waitForElementVisible(roles).click();
        Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("ember-list-container"), browser))
                    .selectItem(role.getName());
        return this;
    }

    private UserInvitationDialog enterMessage(String message) {
        waitForElementVisible(invitationMessage).sendKeys(message);
        return this;
    }
}
