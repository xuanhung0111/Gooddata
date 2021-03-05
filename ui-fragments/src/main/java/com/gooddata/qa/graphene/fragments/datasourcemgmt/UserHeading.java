package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class UserHeading extends AbstractFragment {

    @FindBy(className = "s-user-heading-title")
    private WebElement sharedWithText;

    @FindBy(className = "s-add-datasource-user--button")
    private WebElement addDSUserBtn;

    public static UserHeading getInstance(SearchContext context) {
        return Graphene.createPageFragment(UserHeading.class,
                waitForElementVisible(className("user-heading"), context));
    }

    public AddUserDialog openAddUserDialog() {
        addDSUserBtn.click();
        return AddUserDialog.getInstance(browser);
    }

    public boolean isCorrectNumberSharedWith(int number) {
        return sharedWithText.getText().equals("Shared with (" + number + ")");
    }

    public boolean isAddButtonDisable() {
        return isElementDisabled(addDSUserBtn);
    }

}
