package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.*;

public class CreateDataproductDialog extends AbstractFragment {
    private static final By DIALOG_CLASS = By.className("gd-dialog-content");

    @FindBy(className = "dialog-input-field")
    private WebElement nameInputField;

    public static final CreateDataproductDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(CreateDataproductDialog.class, waitForElementVisible(DIALOG_CLASS, searchContext));
    }

    public void submitDialog(String dataProductId, String domainId) {
        setName(dataProductId);
        checkDomain(domainId);
        submitForm();
    }

    private void setName(String dataProductId) {
        waitForElementVisible(nameInputField).clear();
        nameInputField.sendKeys(dataProductId);
    }

    private void checkDomain(String domainId) {
        waitForElementVisible(getCssSelectorForDomainChecker(domainId), browser).click();
    }

    private void submitForm() {
        waitForElementVisible(By.className("s-create"), browser).click();
    }

    private By getCssSelectorForDomainChecker(String domainId) {
        return By.cssSelector(format("[name = 'domains%s']", domainId));
    }

}
