package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardTabs extends AbstractFragment {

    private static final By BY_TAB_DROP_DOWN_BUTTON = By.cssSelector(".tab-dropdown");

    @FindBy(className = "scroll-right")
    private WebElement scrollRightButton;

    @FindBy(className = "scroll-left")
    private WebElement scrollLeftButton;

    @FindBy(className = "yui3-dashboardtab")
    private List<DashboardTab> tabs;

    /**
     * Method to get number of dashboard tabs for selected project
     *
     * @return number of dashboard tabs for selected project
     */
    public int getNumberOfTabs() {
        return tabs.size();
    }

    /**
     * Method for switching tab by index, no HTTP/XHR requests is expected on this click
     *
     * @param i - tab index
     */
    public void openTab(int i) {
        while (true) {
            // can not scroll to left anymore
            if (Float.parseFloat(scrollLeftButton.getCssValue("opacity")) < 1.0)
                break;
            scrollLeftButton.click();
        }
        DashboardTab tab = tabs.get(i);
        // if tab is not visible, we cannot get its label
        while (tab.getLabel().isEmpty())
            scrollRightButton.click();
        tab.getRoot().click();

        Function<WebDriver, Boolean> isSelected = browser -> tab.isSelected();
        Graphene.waitGui().until(isSelected);
    }

    /**
     * Method to verify that tab with given index is selected
     *
     * @param i - tab index
     * @return true is tab with given index is selected
     */
    public boolean isTabSelected(int i) {
        return tabs.get(i).isSelected();
    }

    /**
     * Method to get tab with given name
     *
     * @param name - tab name
     * @return Dashboard Tab having given name
     */
    public DashboardTab getTab(String name) {
        return tabs.stream()
                .filter(tab -> tab.getLabel().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("can't find given tab"));
    }

    /**
     * Method to get index of selected tab
     *
     * @return index of selected tab
     */
    public int getSelectedTabIndex() {
        for (int i = 0; i < tabs.size(); i++) {
            if (isTabSelected(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get working dashboard tab
     * @return working tab
     */
    public DashboardTab getSelectedTab() {
        return getTab(getSelectedTabIndex());
    }

    /**
     * Method to get label of tab with given index
     *
     * @param i - tab index
     * @return label of tab with given index
     */
    public String getTabLabel(int i) {
        return tabs.get(i).getLabel();
    }

    /**
     * Method to get all dashboard tab labels of selected project
     *
     * @return List<String> with all tab names
     */
    public List<String> getAllTabNames() {
        List<String> tabNames = new ArrayList<String>();
        for (int i = 0; i < tabs.size(); i++) {
            tabNames.add(tabs.get(i).getLabel());
        }
        return tabNames;
    }

    public SimpleMenu selectDropDownMenu(int i) {
        WebElement button = getTabWebElement(i).findElement(BY_TAB_DROP_DOWN_BUTTON);
        waitForElementPresent(button);
        while (!button.isDisplayed()) {
            waitForElementVisible(scrollRightButton).click();
        }
        button.click();
        return SimpleMenu.getInstance(browser);
    }

    public DashboardTab getTab(int i) {
        return tabs.get(i);
    }

    public SimpleMenu expandCopyToDropDown(int i) {
        selectDropDownMenu(i).openSubMenu("Copy To");
        return SimpleMenu.getInstance(By.cssSelector(".s-tab-menu:not(.overlayPlugin-plugged)"), browser);
    }

    private WebElement getTabWebElement(int i) {
        return tabs.get(i).getRoot();
    }
}
