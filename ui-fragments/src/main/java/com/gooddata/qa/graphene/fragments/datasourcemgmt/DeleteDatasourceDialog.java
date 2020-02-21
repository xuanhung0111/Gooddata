package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class DeleteDatasourceDialog extends AbstractFragment {
    @FindBy(className = "s-dialog-submit-button")
    private WebElement deleteButton;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement message;

    public static DeleteDatasourceDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DeleteDatasourceDialog.class,
                waitForElementVisible(className("gd-confirm"), context));
    }

    public void clickDelete() {
        waitForElementVisible(deleteButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public String getMessage() {
        return waitForElementVisible(message).getText();
    }
}
