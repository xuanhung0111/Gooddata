package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class LdmWaitingDialog extends AbstractFragment {

    @FindBy(className = "projects-dic-nav")
    WebElement closeButton;

    @FindBy(className = "waiting-icon")
    WebElement waitingIcon;

    @FindBy(className = "waiting-text")
    WebElement waitingText;

    @FindBy(className = "gd-dialog-close")
    WebElement closeBtn;

    public static LdmWaitingDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(LdmWaitingDialog.class,
                waitForElementVisible(className("gdc-ldm-waiting-dialog"), context));
    }

    public LdmWaitingDialog waitForLoading() {
        waitForElementNotVisible(waitingIcon, 180);
        waitForElementNotVisible(waitingText, 180);
        return this;
    }

    public String getWaitingText() {
        waitForElementVisible(waitingText);
        return waitingText.getText();
    }

    public void closeDialog() {
        waitForElementVisible(closeBtn).click();
        waitForFragmentNotVisible(this);
    }
}
