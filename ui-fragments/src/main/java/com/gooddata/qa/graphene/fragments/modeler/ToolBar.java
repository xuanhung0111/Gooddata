package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ToolBar extends AbstractFragment {
    private static final By TOOLBAR = By.className("gdc-ldm-toolbar");

    @FindBy(className = "s-publish")
    private WebElement btnPublishOnToolbar;

    @FindBy(className = "gd-actions-menu-section")
    private WebElement btnActionMenu;

    public static final ToolBar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ToolBar.class, waitForElementVisible(TOOLBAR, searchContext));
    }

    public void clickPublish() {
        btnPublishOnToolbar.click();
    }

    public OutputStage openOutputStagePopUp() {
        btnActionMenu.click();
        return OverlayWrapper.getInstance(browser).openOutputStage();
    }
}
