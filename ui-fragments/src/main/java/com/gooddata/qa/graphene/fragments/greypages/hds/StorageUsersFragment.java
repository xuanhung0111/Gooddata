package com.gooddata.qa.graphene.fragments.greypages.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class StorageUsersFragment extends AbstractHDSFragment {

    @FindBy
    private Select role;

    @FindBy
    private WebElement profile;

    @FindBy
    private WebElement login;

    @FindBy(xpath = "div[@class='submit']/input")
    private WebElement submit;

    public void waitUntilFormIsVisible() {
        waitForElementVisible(this.getRoot());
        waitForElementVisible(role);
        waitForElementVisible(profile);
        waitForElementVisible(submit);
    }

    public void fillAddUserToStorageForm(final String role, final String profileUri, final String login, final boolean poll) throws JSONException, InterruptedException {
        waitForElementVisible(this.role);
        waitForElementVisible(this.profile);
        waitForElementVisible(this.login);
        waitForElementVisible(this.submit);

        if (!isEmpty(role)) this.role.selectByValue(role);
        if (!isEmpty(profileUri)) this.profile.sendKeys(profileUri);
        if (!isEmpty(login)) this.login.sendKeys(login);
        Graphene.guardHttp(submit).click();
        if (poll) {
            waitForUserAdded(10);
            waitForElementPresent(BY_GP_LINK, browser).click();
        }
    }

    public void fillUpdateUserForm(final String role, final String profileUri) {
        waitForElementVisible(this.role);
        waitForElementVisible(this.profile);
        waitForElementVisible(this.submit);

        if (!isEmpty(role)) this.role.selectByValue(role);
        if (!isEmpty(profileUri)) this.profile.sendKeys(profileUri);
        Graphene.guardHttp(submit).click();
    }

    public void deleteUser() throws JSONException, InterruptedException {
        waitForElementVisible(submit);
        Graphene.guardHttp(submit).click();
        waitTaskFinished(10);
        waitForElementPresent(BY_GP_LINK, browser).click();
    }

    public void verifyValidAddUserForm() {
        waitUntilFormIsVisible();
        assertEquals(submit.getAttribute("value"), "Add user to the storage",
                "Submit button is not 'Add user to the storage'");
    }

    public void verifyValidUpdateUserForm(final String role, final String profile) {
        waitUntilFormIsVisible();
        assertEquals(submit.getAttribute("value"), "Update role", "Submit button is not 'Update role'");
        assertEquals(this.role.getFirstSelectedOption().getText(), role);
        assertEquals(this.profile.getAttribute("value"), profile);
        assertEquals(this.profile.getAttribute("readonly"), "true");
    }

    public void verifyValidDeleteUserForm() {
        waitForElementVisible(this.getRoot());
        waitForElementVisible(submit);
        assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
    }

    public String waitForUserAdded(int checkIterations) throws JSONException, InterruptedException {
        return waitTaskSucceed(checkIterations, "user");
    }
}
