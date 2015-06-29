package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.menu.DashboardMenu;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardsPage extends AbstractFragment {
    private static final By SAVE_AS_DIALOG_LOCATOR = By.className("dashboardSettingsDialogView"); 
    private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
    private static final By BY_PRINTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Preparing printable PDF for download…']");
    private static final By BY_TAB_DROPDOWN_MENU = By.xpath("//div[contains(@class, 's-tab-menu')]");
    private static final By BY_TAB_DROPDOWN_DELETE_BUTTON = By.xpath("//li[contains(@class, 's-delete')]//a");
    private static final By BY_TAB_DROPDOWN_DUPLICATE_BUTTON = By.xpath("//li[contains(@class, 's-duplicate')]//a");
    private static final By BY_TAB_DROPDOWN_COPY_TO_BUTTON = By.xpath("//li[contains(@class, 's-copy_to')]//a");

    @FindBy(xpath = "//div[@id='abovePage']/div[contains(@class,'yui3-dashboardtabs-content')]")
    private DashboardTabs tabs;

    @FindBy(css = ".q-dashboardSwitcher")
    private WebElement dashboardSwitcherButton;

    @FindBy(css = ".menuArrow")
    private WebElement dashboardSwitcherArrowMenu;

    @FindBy(xpath = "//button[@title='Edit, Embed or Export']")
    private WebElement editExportEmbedButton;

    @FindBy(xpath = "//button[@title='Download PDF']")
    private WebElement printPdfButton;

    @FindBy(xpath = "//button[@title='Add a new tab']")
    private WebElement addNewTabButton;

    @FindBy(xpath = "//div[contains(@class,'editTitleDialogView')]")
    private TabDialog newTabDialog;

    @FindBy(xpath = "//div[contains(@class,'s-dashboard-edit-bar')]")
    private DashboardEditBar editDashboardBar;

    @FindBy(xpath = "//div[contains(@class,'dashboardTitleEditBox')]/input")
    private WebElement newDashboardNameInput;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]")
    private WebElement dashboardTabDeleteDialog;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]//button[text()='Delete']")
    private WebElement dashboardTabDeleteConfirmButton;

    @FindBy(className = "yui3-c-projectdashboard-content")
    private DashboardContent content;

    @FindBy(xpath = "//div[contains(@class,'s-permissionSettingsDialog')]")
    private PermissionsDialog permissionsDialog;

    @FindBy(css = ".s-unlistedIcon")
    private WebElement unlistedIcon;

    @FindBy(css = ".s-lockIcon")
    private WebElement lockIcon;

    @FindBy(xpath = "//div[@class='yui3-d-embeddialog-content']")
    private DashboardEmbedDialog dashboardEmbedDialog;

    @FindBy(css = ".s-scheduleButton")
    private WebElement scheduleButton;

    @FindBy(xpath = "//div[contains(@class,'s-mailScheduleDialog')]")
    private DashboardScheduleDialog scheduleDialog;

    /**
     * Fragment represents link for saved view dialog on dashboard
     * when saved view mode is turned on
     *
     * @see SavedViewWidget
     */
    @FindBy(xpath = "//div[contains(@class,'savedFilters')]/button")
    private SavedViewWidget savedViewWidget;
    private By emptyTabPlaceholder = By.xpath("//div[contains(@class, 'yui3-c-projectdashboard-placeholder-visible')]");

    public DashboardTabs getTabs() {
        return tabs;
    }

    public List<FilterWidget> getFilters() {
        return content.getFilters();
    }

    public DashboardContent getContent() {
        return content;
    }

    public DashboardEditBar getDashboardEditBar() {
        return editDashboardBar;
    }

    public PermissionsDialog getPermissionsDialog() {
        return waitForFragmentVisible(permissionsDialog);
    }

    public SavedViewWidget getSavedViewWidget() {
        return savedViewWidget;
    }

    public String getDashboardName() {
        String name = waitForElementVisible(dashboardSwitcherButton).getText();
        if (getDashboardsCount() > 1) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    public boolean selectDashboard(String dashboardName) throws InterruptedException {
        if (getDashboardName().contains(dashboardName)) {
            System.out.println("Dashboard '" + dashboardName + "'already selected");
            return true;
        }

        return openDashboardMenu().selectDashboardByName(dashboardName);
    }

    public boolean selectDashboard(int dashboardIndex) throws InterruptedException {
        DashboardMenu menu = openDashboardMenu();
        if (menu == null) {
            System.out.println("This project has only one dashboard!");
            return false;
        }
        Thread.sleep(3000);

        return menu.selectDashboardByIndex(dashboardIndex);
    }

    public List<String> getDashboardsNames() {
        List<String> dashboardsNames = new ArrayList<String>();
        waitForDashboardPageLoaded(browser);

        if (dashboardSwitcherArrowMenu.isDisplayed()) {
            dashboardsNames.addAll(openDashboardMenu().getAllItemNames());
            dashboardSwitcherArrowMenu.click();
        } else if (dashboardSwitcherButton.isDisplayed()) {
            dashboardsNames.add(getDashboardName());
        }
        return dashboardsNames;
    }

    public int getDashboardsCount() {
        waitForDashboardPageLoaded(browser);

        if (dashboardSwitcherArrowMenu.isDisplayed()) {
            int dashboardsCount = openDashboardMenu().getItemsCount();
            dashboardSwitcherArrowMenu.click();
            return dashboardsCount;
        } else if (waitForElementPresent(dashboardSwitcherButton).isDisplayed()) {
            return 1;
        }
        return 0;
    }

    public void editDashboard() {
        waitForDashboardPageLoaded(browser);
        SimpleMenu menu = openEditExportEmbedMenu();
        if (menu == null)
            return;
        menu.select("Edit");
        waitForElementPresent(editDashboardBar.getRoot());
    }

    public String exportDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        String tabName = tabs.getTabLabel(0);
        openEditExportEmbedMenu().select("Export to PDF");
        waitForElementVisible(BY_EXPORTING_PANEL, browser);
        Thread.sleep(3000);
        waitForElementNotPresent(BY_EXPORTING_PANEL);
        Thread.sleep(3000);
        System.out.println("Dashboard " + tabName + " exported to PDF...");
        return tabName;
    }

    public String printDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        String tabName = tabs.getTabLabel(0);
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(printPdfButton).click();
        waitForElementVisible(BY_PRINTING_PANEL, browser);
        Thread.sleep(3000);
        waitForElementNotPresent(BY_PRINTING_PANEL);
        Thread.sleep(3000);
        System.out.println("Dashboard " + tabName + " printed to Pdf");
        return tabName;
    }

    public DashboardEmbedDialog embedDashboard() throws InterruptedException {
        waitForDashboardPageLoaded(browser);
        openEditExportEmbedMenu().select("Embed");
        waitForElementVisible(dashboardEmbedDialog.getRoot());
        return dashboardEmbedDialog;
    }

    public void addNewTab(String tabName) {
        waitForElementVisible(addNewTabButton).click();
        waitForElementVisible(newTabDialog.getRoot());
        newTabDialog.createTab(tabName);
    }

    public void deleteDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        editDashboard();
        boolean nonEmptyTab = browser.findElements(emptyTabPlaceholder).size() == 0;
        tabs.selectDropDownMenu(tabIndex);
        waitForElementVisible(BY_TAB_DROPDOWN_MENU, browser).findElement(BY_TAB_DROPDOWN_DELETE_BUTTON).click();
        if (nonEmptyTab) {
            waitForElementVisible(dashboardTabDeleteDialog);
            waitForElementVisible(dashboardTabDeleteConfirmButton).click();
            waitForElementNotPresent(dashboardTabDeleteDialog);
        }
        editDashboardBar.saveDashboard();
        waitForElementNotPresent(editDashboardBar.getRoot());
        waitForDashboardPageLoaded(browser);
    }

    public void duplicateDashboardTab(int tabIndex) {
        tabs.openTab(tabIndex);
        editDashboard();
        tabs.selectDropDownMenu(tabIndex);
        waitForElementVisible(BY_TAB_DROPDOWN_MENU, browser).findElement(BY_TAB_DROPDOWN_DUPLICATE_BUTTON).click();
        editDashboardBar.saveDashboard();
        waitForElementNotPresent(editDashboardBar.getRoot());
        waitForDashboardPageLoaded(browser);
    }

    public void copyDashboardTab(int tabIndex, String dashboardName) {
        tabs.openTab(tabIndex);
        editDashboard();
        tabs.selectDropDownMenu(tabIndex);
        Actions actions = new Actions(browser);
        actions.moveToElement(waitForElementVisible(BY_TAB_DROPDOWN_MENU, browser)
                .findElement(BY_TAB_DROPDOWN_COPY_TO_BUTTON)).perform();
        actions.click(waitForElementVisible(By.className("s-" + CssUtils.simplifyText(dashboardName)), browser))
            .perform();
        waitForElementVisible(By.className("s-btn-dismiss"), browser).click();
        editDashboardBar.saveDashboard();
        waitForElementNotPresent(editDashboardBar.getRoot());
        waitForDashboardPageLoaded(browser);
    }

    public void addNewDashboard(String dashboardName) throws InterruptedException {
        openEditExportEmbedMenu().select("Add Dashboard");
        waitForElementVisible(newDashboardNameInput);

        newDashboardNameInput.click(); //sleep wasn't necessary, getting focus on the input field helps
        newDashboardNameInput.clear();
        newDashboardNameInput.sendKeys(dashboardName);
        newDashboardNameInput.sendKeys(Keys.ENTER);
        editDashboardBar.saveDashboard();
    }

    public void deleteDashboard() throws InterruptedException {
        editDashboard();
        editDashboardBar.deleteDashboard();
    }

    public PermissionsDialog openPermissionsDialog() {
        waitForDashboardPageLoaded(browser);
        openEditExportEmbedMenu().select("Sharing & Permissions");
        return getPermissionsDialog();
    }

    public void publishDashboard(boolean listed) {
        openPermissionsDialog();

        permissionsDialog.publish(listed ? PublishType.EVERYONE_CAN_ACCESS : PublishType.SPECIFIC_USERS_CAN_ACCESS);
        permissionsDialog.submit();
    }

    public void lockDashboard(boolean lock) {
        openPermissionsDialog();

        if (lock) {
            permissionsDialog.lock();
        } else {
            permissionsDialog.unlock();
        }

        permissionsDialog.submit();
    }

    public PermissionsDialog lockIconClick() {
        waitForElementVisible(lockIcon).click();
        return getPermissionsDialog();
    }

    public PermissionsDialog unlistedIconClick() {
        waitForElementVisible(unlistedIcon).click();
        return getPermissionsDialog();
    }

    public void saveAsDashboard(String dashboardName, PermissionType permissionType) throws InterruptedException {
        saveAsDashboard(dashboardName, false, permissionType);
    }
    
    public void saveAsDashboardAndEnableSavedViews(String dashboardName, PermissionType permissionType) 
            throws InterruptedException {
        saveAsDashboard(dashboardName, true, permissionType);
    }
    
    private void saveAsDashboard(String dashboardName, boolean isSavedViews, PermissionType permissionType) 
            throws InterruptedException{
        SaveAsDialog saveAsDialog = openSaveAsDialog();
        
        saveAsDialog.saveAs(dashboardName, isSavedViews, permissionType);
        waitForFragmentNotVisible(saveAsDialog);
        
        editDashboardBar.saveDashboard();
        waitForElementNotPresent(editDashboardBar.getRoot());
        waitForDashboardPageLoaded(browser);
    }
    
    private SaveAsDialog openSaveAsDialog() {
        waitForDashboardPageLoaded(browser);
        openEditExportEmbedMenu().select("Save as...");
        return Graphene.createPageFragment(SaveAsDialog.class, 
                waitForElementVisible(SAVE_AS_DIALOG_LOCATOR, browser));
    }

    public FilterWidget getFilterWidget(String condition) {
        return content.getFilterWidget(condition);
    }

    public FilterWidget getFirstFilter() {
        return content.getFirstFilter();
    }

    public boolean isLocked() {
        return lockIcon.isDisplayed();
    }

    public boolean isUnlisted() {
        return unlistedIcon.isDisplayed();
    }

    public boolean isEditButtonPresent() {
        return openEditExportEmbedMenu().contains("Edit");
    }

    public DashboardScheduleDialog showDashboardScheduleDialog() {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(scheduleButton).click();
        waitForElementVisible(scheduleDialog.getRoot());
        return scheduleDialog;
    }

    public boolean isScheduleButtonVisible() {
        try {
            return scheduleButton.isDisplayed();
        } catch (NoSuchElementException nsee) {
            return false;
        }
    }

    private SimpleMenu openEditExportEmbedMenu() {
        if (waitForElementPresent(editExportEmbedButton).getAttribute("class").contains("gdc-hidden")) {
            return null;
        }
        editExportEmbedButton.click();
        SimpleMenu menu = Graphene.createPageFragment(SimpleMenu.class,
                waitForElementVisible(SimpleMenu.LOCATOR, browser));
        waitForElementVisible(menu.getRoot());

        return menu;
    }

    private DashboardMenu openDashboardMenu() {
        waitForElementVisible(dashboardSwitcherButton);
        if (dashboardSwitcherButton.getAttribute("class").contains("disabled")) {
            System.out.println("Dashboard switcher button is disabled!"
                    + "That means project just has only one dashboard!");
            return null;
        }
        dashboardSwitcherButton.click();
        DashboardMenu menu = Graphene.createPageFragment(DashboardMenu.class,
                waitForElementVisible(DashboardMenu.LOCATOR, browser));
        waitForElementVisible(menu.getRoot());
        return menu;
    }
}
