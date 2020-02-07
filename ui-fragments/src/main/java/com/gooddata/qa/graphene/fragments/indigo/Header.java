package com.gooddata.qa.graphene.fragments.indigo;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.HeaderAccountMenu;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Optional;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class Header extends AbstractFragment {

    @FindBy(className = "gd-header-account")
    private WebElement accountMenuButton;

    public static final By HAMBURGER_LINK = cssSelector(".hamburger-icon");

    public static final By LOCATOR = By.className("gd-header");

    @FindBy(className = "gd-header-project-wrapper")
    private ReactProjectSwitch projectSwitch;

    public List<WebElement> getMeasureMenuItems() {
        return measureMenuItems;
    }

    @FindBy(css = ".gd-header-measure .gd-header-menu-item")
    private List<WebElement> measureMenuItems;

    public Optional<WebElement> getDashboardMenuItem() {
        return measureMenuItems.stream().filter(item -> item.getAttribute("class").contains("s-menu-dashboards")).findAny();
    }

    public Optional<WebElement> getReportMenuItem() {
        return measureMenuItems.stream().filter(item -> item.getAttribute("class").contains("s-menu-reports")).findAny();
    }

    public Optional<WebElement> getActiveMenuItem() {
        return measureMenuItems.stream().filter(item -> item.getAttribute("class").contains("active")).findAny();
    }

    public Optional<WebElement> getManageMenuItem() {
        return measureMenuItems.stream().filter(item -> item.getAttribute("class").contains("s-menu-manage")).findAny();
    }

    public static final Header getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Header.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public static final boolean isVisible(SearchContext searchContext) {
        return isElementVisible(LOCATOR, searchContext);
    }

    public void switchProject(String projectName) {
         waitForFragmentVisible(projectSwitch)
                .selectByName(projectName);
    }

    public String getCurrentProjectName() {
        return waitForFragmentVisible(projectSwitch)
                .getSelection();
    }

    public HeaderAccountMenu openAccountMenu() {
        waitForElementVisible(accountMenuButton).click();

        return Graphene.createPageFragment(
                HeaderAccountMenu.class,
                waitForElementVisible(className(HeaderAccountMenu.CLASS_NAME), browser));
    }

    public void logout() {
        openAccountMenu()
            .logout();
    }

    public boolean isHamburgerMenuLinkPresent() {
        return isElementPresent(HAMBURGER_LINK, getRoot());
    }

    public HamburgerMenu openHamburgerMenu() {
        if (!isHamburgerMenuLinkPresent()) {
            log.warning("Hamburger menu link is not visible!");
            return null;
        }

        WebElement menuLink = waitForElementPresent(HAMBURGER_LINK, getRoot());
        if (!menuLink.getAttribute("class").contains("is-open")) {
            menuLink.click();
        }

        return Graphene.createPageFragment(HamburgerMenu.class,
                waitForElementVisible(HamburgerMenu.LOCATOR, browser));
    }

    public void closeHamburgerMenu() {
        WebElement menuLink = waitForElementPresent(HAMBURGER_LINK, getRoot());
        if (!menuLink.getAttribute("class").contains("is-open")) {
            return;
        }

        menuLink.click();
        waitForElementNotPresent(HamburgerMenu.LOCATOR);
    }
}
