package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

public class SaveAsDialog extends AbstractFragment {

    @FindBy(className = "gd-input-field")
    private WebElement nameTextBox;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

    public static final String ROOT_CLASS = "s-dialog";

    public static SaveAsDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveAsDialog.class,
                waitForElementVisible(className(ROOT_CLASS), searchContext));
    }

    public void save(final String name) {
        enterName(name).clickSubmitButton();
        waitForFragmentNotVisible(this);
    }

    public void clickSubmitButton() {
        waitForElementEnabled(submitButton).click();
        waitForElementNotVisible(submitButton);
    }

    public SaveAsDialog enterName(final String name) {
        waitForElementVisible(nameTextBox).clear();
        nameTextBox.sendKeys(name);
        return this;
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);
    }
}
