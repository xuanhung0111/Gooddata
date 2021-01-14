package com.gooddata.qa.graphene.fragments.indigo.analyze.dialog;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.cssSelector;

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

    public static final String ROOT_SELECTOR = ".s-dialog:not(.s-unsave-change-confirmation-dialog)";

    public static SaveInsightDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveInsightDialog.class,
                waitForElementVisible(cssSelector(ROOT_SELECTOR), searchContext));
    }

    public boolean isSaveInsightDialogDisplay() {
        return isElementVisible(cssSelector(ROOT_SELECTOR), browser);
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

    public String getName() {
        return waitForElementVisible(nameTextBox).getText();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);
    }

    public boolean isSubmitButtonDisabled() {
        return submitButton.getAttribute("class").contains("disabled");
    }

}
