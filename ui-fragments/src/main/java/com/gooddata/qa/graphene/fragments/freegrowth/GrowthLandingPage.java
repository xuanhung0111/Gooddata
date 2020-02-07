package com.gooddata.qa.graphene.fragments.freegrowth;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;

public class GrowthLandingPage extends FreeLandingPage {

    @FindBy(css = ".create-workspace-button,.create-workspace-link")
    private WebElement createWorkspaceButton;

    @FindBy(css = ".create-workspace-link")
    private WebElement createWorkspaceLink;

    public static final GrowthLandingPage getGrowthInstance(SearchContext context) {
        waitForElementNotVisible(id("appLoading"), context);
        return Graphene.createPageFragment(GrowthLandingPage.class, waitForElementVisible(id("root"), context));
    }

    public void clickCreateWorkspaceButton() {
        waitForElementVisible(createWorkspaceButton).click();
    }

    public void clickCreateWorkspaceLink() {
        waitForElementVisible(createWorkspaceLink).click();
    }
}
