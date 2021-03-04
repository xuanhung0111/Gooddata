package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ToastMessage extends AbstractFragment {
    private static final String MESSAGE = "gd-message-overlay";

    @FindBy(className = "gd-message-text")
    private WebElement messageContent;

    @FindBy(className = "gd-message-dismiss")
    private WebElement xButton;

    public static ToastMessage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ToastMessage.class, waitForElementVisible(className(MESSAGE), searchContext));
    }

    public String getToastMessage() {
        return waitForElementVisible(messageContent).getText();
    }

    public void clickCloseToastMessage() {
        log.info("---Closing toast message---");
        waitForElementVisible(xButton).click();
    }
}
