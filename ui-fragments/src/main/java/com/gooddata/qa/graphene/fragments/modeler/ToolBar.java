package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.stream.Collectors;

public class ToolBar extends AbstractFragment {
    private static final By TOOLBAR = By.className("gdc-ldm-toolbar");
    private static final By ZOOM_BTN = By.className("gdc-ldm-zoom-button");

    @FindBy(className = "s-publish")
    private WebElement btnPublishOnToolbar;

    @FindBy(className = "gd-actions-menu-section")
    private WebElement btnActionMenu;

    @FindBy(css = ".gdc-ldm-switch-button .right-button")
    private WebElement tableViewButton;

    @FindBy(css = ".gdc-ldm-switch-button .left-button")
    private WebElement modelButton;

    @FindBy(className = "search-model-input")
    private WebElement searchModelInput;

    @FindBy(css = ".save-as-draft-timer .title")
    private WebElement saveAsDraftButton;

    @FindBy(css = ".search-model-input .s-input-clear")
    private WebElement clearSearchTextIcon;


    public static final ToolBar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ToolBar.class, waitForElementVisible(TOOLBAR, searchContext));
    }

    public void clickPublish() {
        btnPublishOnToolbar.click();
    }

    public void saveAsDraft() {
        waitForElementVisible(saveAsDraftButton);
        saveAsDraftButton.click();
        OverlayWrapper.getInstance(browser).discardDraft();
    }

    public void switchToTableView() {
        tableViewButton.click();
        TableView.getInstance(browser).getTableViewDataset().waitLoadingTableView();
    }

    public void switchToModel() {
        modelButton.click();
    }

    public OutputStage openOutputStagePopUp() {
        btnActionMenu.click();
        return OverlayWrapper.getInstance(browser).openOutputStage();
    }

    public void exportJson() {
        btnActionMenu.click();
        OverlayWrapper.getInstance(browser).exportJson();
    }

    public void importJson(String jsonFilePath) {
        btnActionMenu.click();
        OverlayWrapper.getInstance(browser).importJson(jsonFilePath);
    }

    public static final ToolBar getInstanceInTableView(SearchContext searchContext, int index) {
        List<WebElement> toolbarList = searchContext.findElements(TOOLBAR);
        return Graphene.createPageFragment(
                ToolBar.class, toolbarList.get(index));
    }

    public SearchDropDown searchItem(String text) {
        getActions().moveToElement(searchModelInput).click().sendKeys(text).build().perform();
        return SearchDropDown.getInstance(browser);
    }

    public ToolBar clearSearchText() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(clearSearchTextIcon).click().build().perform();
        return this;
    }
}
