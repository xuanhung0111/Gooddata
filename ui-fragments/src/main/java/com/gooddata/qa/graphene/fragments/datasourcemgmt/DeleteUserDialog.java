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

public class DeleteUserDialog extends AbstractFragment {

    @FindBy(className = "s-remove")
    private WebElement removeBtn;

    @FindBy(className = "s-cancel")
    private WebElement cancelBtn;

    public static DeleteUserDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DeleteUserDialog.class,
                waitForElementVisible(className("s-delete-datasource-user-dialog"), context));
    }

    public void clickDeleteButton() {
        Actions actions = getActions();
        actions.moveToElement(removeBtn).click().build().perform();
        waitForFragmentNotVisible(this);
    }

    public void clickCancel() {
        Actions actions = getActions();
        actions.moveToElement(cancelBtn).click().build().perform();
        waitForFragmentNotVisible(this);
    }
}
