package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForLoadingIconHidden;

public class ViewMode extends AbstractFragment {

    private static final By VIEW_MODE = By.className("view-mode");

    @FindBy(className = "change-to-edit-mode")
    private WebElement changeToEditModeBtn;

    @FindBy(className = "initial-describe")
    private WebElement initialDescribeContent;

    @FindBy(css = ".guideline-link a")
    private WebElement guidelineLink;

    public static final ViewMode getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ViewMode.class, waitForElementVisible(VIEW_MODE, searchContext));
    }

    public void clickButtonChangeToEditMode() {
        Actions actions = new Actions(browser);
        actions.moveToElement(changeToEditModeBtn).click().build().perform();
        waitForLoadingIconHidden();
    }

    public String getInitialDescribeText() {
        return initialDescribeContent.getText();
    }

    public String getGuideLineLink() {
        return guidelineLink.getAttribute("href");
    }

}
