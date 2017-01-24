package com.gooddata.qa.graphene.fragments.projects;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

public class PopupDialog extends AbstractFragment {

    @FindBy(className = "s-btn-leave")
    private WebElement leaveButton;

    public static PopupDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PopupDialog.class,
                waitForElementVisible(className("t-infoMessageDialog"), searchContext));
    }

    public void leaveProject() {
        waitForElementVisible(leaveButton).click();
    }

    public String getMessage() {
        // getText() returns value contains child text, so it should be removed from error msg
        String returnString = waitForElementVisible(tagName("form"), getRoot()).getText();

        return returnString.substring(0, returnString.indexOf(waitForElementVisible(className("bd_controls"),
                getRoot()).getText())).trim();
    }

    public void close() {
        waitForElementVisible(className("container-close"), getRoot()).click();
    }
}
