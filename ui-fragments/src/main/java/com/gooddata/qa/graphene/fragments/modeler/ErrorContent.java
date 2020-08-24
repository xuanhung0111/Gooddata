package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class ErrorContent extends AbstractFragment {
    @FindBy(className = "result-error-message")
    WebElement errorTitle;

    @FindBy(css = ".title .fieldset-error-detail .error-detail")
    WebElement errorMessage;

    @FindBy(css = ".title .projects-dic-nav")
    WebElement closeButton;

    public static ErrorContent getInstance(SearchContext context) {
        return Graphene.createPageFragment(ErrorContent.class,
                waitForElementVisible(className("error-boundary"), context));
    }

    public String getErrorTitle() {
        return waitForElementVisible(errorTitle).getText();
    }

    public String getErrorMessage() {
        return waitForElementVisible(errorMessage).getText();
    }

    public void cancel() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }
}
