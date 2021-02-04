package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class EditUserDialog extends AbstractFragment {
    @FindBy(className = "input-radio-use")
    private WebElement useBtn;

    @FindBy(className = "input-radio-manage")
    private WebElement manageBtn;

    @FindBy(className = "s-cancel")
    private WebElement cancelBtn;

    @FindBy(className = "s-save")
    private WebElement saveBtn;

    @FindBy(className = "dataSourceUser-editPermission-fullName")
    private WebElement fullNameText;

    @FindBy(className = "dataSourceUser-editPermission-email")
    private WebElement emailText;

    @FindBy(className = "input-radio-label")
    private List<WebElement> inputPermission;

    public static EditUserDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(EditUserDialog.class,
                waitForElementVisible(className("data-source-user-edit-dialog"), context));
    }

    public String getEmail() {
        return emailText.getText();
    }

    public WebElement getPermission(String name) {
        WebElement label = inputPermission.stream().filter(el -> el.findElement(By.tagName("span")).getText().equals(name)).findFirst().get();
        return label.findElement(By.tagName("input"));
    }

    public void setUsePermission() {
        Actions actions = getActions();
        actions.moveToElement(getPermission("Use")).click();
    }

    public void setManagePermission() {
        Actions actions = getActions();
        actions.moveToElement(getPermission("Manage")).click();
    }

    public void clickCancel() {
        Actions actions = getActions();
        actions.moveToElement(cancelBtn).click().build().perform();
        waitForFragmentNotVisible(this);
    }

    public void clickSave() {
        Actions actions = getActions();
        actions.moveToElement(saveBtn).click().build().perform();;
        waitForFragmentNotVisible(this);
    }
}
