package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class CreateDataproductDialog extends AbstractFragment {
    private static final By DIALOG_CONTENT = By.className("gd-dialog-content");

    @FindBy(className = "dialog-input-field")
    private WebElement nameInputField;

    @FindBy(id = "domainsdata-admin-test1")
    private WebElement domainAdmintest1Checkbox;

    public static final CreateDataproductDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(CreateDataproductDialog.class, waitForElementVisible(DIALOG_CONTENT, searchContext));
    }

    public void setName(String name) {
        waitForElementVisible(nameInputField).clear();
        nameInputField.sendKeys(name);
    }

    public void checkDataAdminTest1Checkbox() {
        waitForElementVisible(domainAdmintest1Checkbox).click();
    }

    public void submitForm() {
        WebElement submitButton = browser.findElement(By.className("s-create"));
        waitForElementVisible(submitButton).click();
    }

}
