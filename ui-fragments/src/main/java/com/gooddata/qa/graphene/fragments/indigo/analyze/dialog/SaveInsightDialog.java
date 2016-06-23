package com.gooddata.qa.graphene.fragments.indigo.analyze.dialog;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SaveInsightDialog extends AbstractFragment {

    @FindBy(className = "name-insight-dialog-input")
    private WebElement nameTextBox;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

    public static final String ROOT_CLASS = "s-dialog";

    public static SaveInsightDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveInsightDialog.class,
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

    public SaveInsightDialog enterName(final String name) {
        waitForElementVisible(nameTextBox).clear();
        nameTextBox.sendKeys(name);
        return this;
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);
    }

}
