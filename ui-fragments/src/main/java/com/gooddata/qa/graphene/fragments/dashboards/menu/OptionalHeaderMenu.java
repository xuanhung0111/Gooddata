package com.gooddata.qa.graphene.fragments.dashboards.menu;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoEmbedDashboardDialogs;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ScheduleEmailDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class OptionalHeaderMenu extends AbstractReactDropDown {

    @FindBy(className = "s-pdf-export-item")
    private WebElement exportToPDF;

    @FindBy(className = "s-schedule-email-item")
    private WebElement scheduleEmail;

    @FindBy(className = "s-delete_dashboard")
    private WebElement deleteDashboardMenuItem;

    @FindBy(className = "s-save_as_menu_item")
    private WebElement saveAsButton;

    @FindBy(className = "s-embed-item")
    private WebElement embedOption;

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

    public void saveAs(String title) {
        waitForElementVisible(saveAsButton).click();
        SaveAsDialog.getInstance(browser).enterName(title).clickSubmitButton();
    }

    public void clickOnDeleteMenuItem() {
        waitForElementVisible(deleteDashboardMenuItem).click();
    }

    public ScheduleEmailDialog scheduleEmailing() {
        waitForElementVisible(scheduleEmail).click();
        return ScheduleEmailDialog.getInstance(browser);
    }

    public IndigoEmbedDashboardDialogs openEmbedDashboardDialog() {
        waitForElementVisible(embedOption).click();
        return IndigoEmbedDashboardDialogs.getInstance(browser);
    }

    public SaveAsDialog saveAsNew() {
        waitForElementVisible(saveAsButton).click();
        return SaveAsDialog.getInstance(browser);
    }

    public Boolean isPDFExportItemVisible() {
        return isElementPresent(By.className("s-pdf-export-item"), browser);
    }

    public Boolean isDeleteItemVisible() {
        return isElementPresent(By.className("s-delete_dashboard"), browser);
    }

    public Boolean isSaveAsNewItemVisible() {
        return isElementPresent(By.className("s-save_as_menu_item"), browser);
    }
}
