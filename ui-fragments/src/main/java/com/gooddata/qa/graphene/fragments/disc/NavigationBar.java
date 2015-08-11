package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class NavigationBar extends AbstractFragment {

    @FindBy(css = ".ait-header-fragment .app-title")
    private WebElement headerTitle;
    
    @FindBy(css = ".ait-header-projects-btn")
    private WebElement projectsButton;
    
    @FindBy(css = ".ait-header-overview-btn")
    private WebElement overviewButton;

    public void clickOnProjectsButton() {
        waitForElementPresent(projectsButton).click();
    }
    
    public void clickOnOverviewButton() {
        waitForElementVisible(overviewButton).click();
    }
    
    public String getHeaderTitle() {
        return waitForElementVisible(headerTitle).getText();
    }
    
    public String getProjectsButtonTitle() {
        return waitForElementPresent(projectsButton).getText();
    }
    
    public String getOverviewButtonTitle() {
        return waitForElementVisible(overviewButton).getText();
    }
}
