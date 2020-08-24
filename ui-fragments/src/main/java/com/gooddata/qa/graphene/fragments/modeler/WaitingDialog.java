package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

public class WaitingDialog extends AbstractFragment {
    @FindBy(className = "projects-dic-nav")
    WebElement closeButton;

    @FindBy(className = "waiting-icon")
    WebElement waitingIcon;

    @FindBy(className = "waiting-text")
    WebElement waitingText;

    public static WaitingDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(WaitingDialog.class,
                waitForElementVisible(cssSelector(".gdc-ldm-waiting-dialog .waiting-dialog-content"), context));
    }

    public WaitingDialog waitForLoading() {
        waitForElementNotVisible(waitingIcon);
        waitForElementNotVisible(waitingText);
        return this;
    }

    public String getWaitingText () {
        waitForElementVisible(waitingText);
        return waitingText.getText();
    }

    public ErrorContent getErrorContent() {
        return ErrorContent.getInstance(browser);
    }
}
