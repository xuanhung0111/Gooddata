package com.gooddata.qa.graphene.fragments.indigo;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class OptionalExportMenu extends AbstractReactDropDown {

    @FindBy(className = "s-gd-export-menu-open-report")
    private WebElement openAsReportButton;

    @FindBy(className = "s-options-menu-explore-insight")
    private WebElement exploreInsight;

    @Override
    protected String getDropdownCssSelector() {
        throw new UnsupportedOperationException("Unsupported getDropdownCssSelector() method");
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }

    public void exportTo(File file) {
        waitForElementVisible(
            By.cssSelector("[class*='menu-export-" + file.toString().toLowerCase() + "']"), getRoot()).click();
    }

    public boolean isExportToButtonEnabled(File file) {
        return !isElementDisabled(waitForElementVisible(
            By.cssSelector("[class*='menu-export-" + file.toString().toLowerCase() + "']"), getRoot()));
    }

    public boolean isOpenAsReportButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(openAsReportButton));
    }

    public void exploreInsight() {
       waitForElementVisible(exploreInsight).click();
    }

    public void exportReport() {
        final int numberOfWindows = browser.getWindowHandles().size();
        waitForElementVisible(openAsReportButton).click();

        //make sure the new window is displayed to prevent unexpected errors
        Function<WebDriver, Boolean> hasNewWindow = browser -> browser.getWindowHandles().size() == numberOfWindows + 1;
        Graphene.waitGui().until(hasNewWindow);
    }

    public String getExportButtonTooltipText() {
        getActions().moveToElement(openAsReportButton).perform();
        return waitForElementVisible(By.cssSelector(".gd-bubble .content"), browser).getText().trim();
    }

    public enum File {
        CSV, XLSX;
    }
}
