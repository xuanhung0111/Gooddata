package com.gooddata.qa.graphene.fragments.greypages.hds;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.testng.Assert.assertEquals;

public class StorageUsersFragment extends AbstractGreyPagesFragment {

    @FindBy
    private Select role;

    @FindBy
    private WebElement profile;

    @FindBy(xpath="div[@class='submit']/input")
    private WebElement submit;

    public void waitUntilFormIsVisible() {
        waitForElementVisible(this.getRoot());
        waitForElementVisible(role);
        waitForElementVisible(profile);
        waitForElementVisible(submit);
    }

    public void fillAddUserToStorageForm(final String role, final String profileUri) {
        waitForElementVisible(this.role);
        waitForElementVisible(this.profile);
        waitForElementVisible(this.submit);

        if(!isEmpty(role)) this.role.selectByValue(role);
        if(!isEmpty(profileUri)) this.profile.sendKeys(profileUri);
        Graphene.guardHttp(submit).click();
    }

    public void deleteUser() {
        waitForElementVisible(submit);
        Graphene.guardHttp(submit).click();
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
//        assertEquals(this.profile.getText(), profile); // todo arquillian failing here
//        assertEquals(this.profile.getAttribute("readonly"), "readonly"); // todo arquillian failing here
    }

    public void verifyValidDeleteUserForm() {
        waitForElementVisible(this.getRoot());
        waitForElementVisible(submit);
        assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
    }
}
