package com.gooddata.qa.graphene.fragments.indigo.user;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UsersTable extends AbstractFragment {

    @FindBy(css = ".list-item.user")
    private List<WebElement> users;

    private static final By BY_USER_EMAIL = By.className("users-user-email");
    private static final By BY_USER_ROLE = By.className("users-user-role");
    private static final By BY_CHECKBOX_SELECT_USER = By.cssSelector("input.users-select-user");

    public void selectUsers(String... emails) {
        waitForCollectionIsNotEmpty(users);
        WebElement checkbox = null;
        outer: for (String email: emails) {
            for (WebElement user: users) {
                if (!email.equals(waitForElementVisible(BY_USER_EMAIL, user).getText().trim()))
                    continue;
                checkbox = waitForElementVisible(BY_CHECKBOX_SELECT_USER, user);
                if (!checkbox.isSelected()) {
                    checkbox.click();
                }
                assertTrue(checkbox.isSelected(), "Check box isn't selected");
                continue outer;
            }
            System.out.println(String.format("Email %s is not found! Skip it!", email));
        }
    }

    public String getUserRole(String email) {
        waitForCollectionIsNotEmpty(users);
        for (WebElement user : users) {
            if(!email.equals(waitForElementVisible(BY_USER_EMAIL, user).getText().trim()))
                continue;
            return waitForElementVisible(BY_USER_ROLE, user).getText().trim();
        }
        throw new IllegalArgumentException(String.format("Email %s is not found!", email));
    }
}
