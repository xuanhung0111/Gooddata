package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class EditUserDialog extends AbstractFragment {
    @FindBy(className = "input-radio-use")
    private WebElement useBtn;

    @FindBy(className = "input-radio-manage")
    private WebElement manageBtn;

    @FindBy(className = "s-cancel")
    private WebElement cancelBtn;

    @FindBy(className = "s-save")
    private WebElement SaveBtn;

    @FindBy(className = "dataSourceUser-editPermission-fullName")
    private WebElement fullNameText;

    @FindBy(className = "dataSourceUser-editPermission-email")
    private WebElement emailText;

    public static EditUserDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(EditUserDialog.class,
                waitForElementVisible(className("data-source-user-edit-dialog"), context));
    }
}
