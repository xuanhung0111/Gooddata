package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SaveAndContinueDialog extends AbstractFragment {

    @FindBy(className = "s-btn-cancel")
    private WebElement cancel;

    @FindBy(className = "s-btn-save_and_continue")
    private WebElement saveAndContinue;

    @FindBy(className = "message")
    private WebElement message;

    @FindBy(className = "container-close")
    private WebElement close;

    public static SaveAndContinueDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveAndContinueDialog.class,
                waitForElementVisible(className("t-infoMessageDialog"), searchContext));
    }

    public void cancel() {
        waitForElementVisible(cancel).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void saveAndContinue() {
        waitForElementVisible(saveAndContinue).click();
        waitForElementNotVisible(this.getRoot());
    }

    public String getMessage() {
        return waitForElementVisible(message).getText();
    }

    public void close() {
        waitForElementVisible(close).click();
        waitForElementNotVisible(this.getRoot());
    }
}
