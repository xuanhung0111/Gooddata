package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class PublishResult extends AbstractFragment {
    private static final String PUBLISH_RESULT_CLASS = "publish-result-modal";

    @FindBy(css = ".publish-result-modal-container .gd-heading-2")
    private WebElement message;

    @FindBy(className = "gd-dialog-close")
    private WebElement closebtn;

    public static PublishResult getInstance(SearchContext context) {
        return Graphene.createPageFragment(PublishResult.class,
                waitForElementVisible(className(PUBLISH_RESULT_CLASS), context));
    }

    public String getResultMessage() {
        return waitForElementVisible(message).getText();
    }

    public void closeResultDialog() {
        waitForElementVisible(closebtn).click();
        waitForElementNotPresent(closebtn);
    }
}
