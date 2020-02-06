package com.gooddata.qa.graphene.fragments.dashboards.menu;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ScheduleEmailDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class OptionalHeaderMenu extends AbstractReactDropDown {

    @FindBy(className = "s-pdf-export-item")
    private WebElement exportToPDF;

    @FindBy(className = "s-schedule-email-item")
    private WebElement scheduleEmail;

    @Override
    protected String getDropdownCssSelector() {
        throw new UnsupportedOperationException("Unsupported getDropdownCssSelector() method");
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }

    public static OptionalHeaderMenu getInstance(SearchContext context) {
        return Graphene.createPageFragment(OptionalHeaderMenu.class,
            waitForElementVisible(By.className("gd-header-menu-overlay"), context));
    }

    public void exportToPDF() {
        waitForElementVisible(exportToPDF).click();
    }

    public ScheduleEmailDialog scheduleEmailing() {
        waitForElementVisible(scheduleEmail).click();
        return ScheduleEmailDialog.getInstance(browser);
    }

    public Boolean isPDFExportItemVisible() {
        return isElementPresent(By.className("s-pdf-export-item"), browser);
    }
}
