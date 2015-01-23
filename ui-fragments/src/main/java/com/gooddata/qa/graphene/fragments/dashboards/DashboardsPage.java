package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.menu.DashboardMenu;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardsPage extends AbstractFragment {

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

    @FindBy(className = "yui3-c-filterdashboardwidget")
    private List<FilterWidget> filters;

    @FindBy(className = "yui3-c-projectdashboard-content")
    private DashboardContent content;

    @FindBy(xpath = "//div[contains(@class,'s-permissionSettingsDialog')]")
    private PermissionsDialog permissionsDialog;

    @FindBy(css = ".s-unlistedIcon")
    private WebElement unlistedIcon;

    @FindBy(css = ".s-lockIcon")
    private WebElement lockIcon;

    @FindBy (xpath = "//div[@class='yui3-d-embeddialog-content']")
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

    private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
    private static final By BY_PRINTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Preparing printable PDF for download…']");
    private static final By BY_TAB_DROPDOWN_MENU = By.xpath("//div[contains(@class, 's-tab-menu')]");
    private static final By BY_TAB_DROPDOWN_DELETE_BUTTON = By.xpath("//li[contains(@class, 's-delete')]//a");

    public DashboardTabs getTabs() {
        return tabs;
    }

    public List<FilterWidget> getFilters() {
        return filters;
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
        openEditExportEmbedMenu().select("Edit");
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
        waitForElementVisible(BY_PRINTING_PANEL,browser);
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

    public void addNewDashboard(String dashboardName) throws InterruptedException {
        openEditExportEmbedMenu().select("Add Dashboard");
        waitForElementVisible(newDashboardNameInput).clear();
        newDashboardNameInput.click(); //sleep wasn't necessary, getting focus on the input field helps
        newDashboardNameInput.sendKeys(dashboardName);
        System.out.println(newDashboardNameInput.getText());
        Thread.sleep(2000);
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
        permissionsDialog.publish(listed ? PublishType.SPECIFIC_USERS_CAN_ACCESS : PublishType.EVERYONE_CAN_ACCESS);
        permissionsDialog.submit();
    }

    public void lockDashboard(boolean locked) {
        openPermissionsDialog();
        if (locked) {
            permissionsDialog.unlock();
        } else {
            permissionsDialog.lock();
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

    public boolean isLocked() {
        return lockIcon.isDisplayed();
    }

    public boolean isUnlisted() {
        return unlistedIcon.isDisplayed();
    }

    public boolean isEditButtonPresent() {
        return openEditExportEmbedMenu().contains("Edit");
    }

    public DashboardScheduleDialog scheduleDashboard() {
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
        waitForElementVisible(editExportEmbedButton).click();
        SimpleMenu menu = Graphene.createPageFragment(SimpleMenu.class,
                waitForElementVisible(SimpleMenu.LOCATOR, browser));
        waitForElementVisible(menu.getRoot());

        return menu;
    }

    private DashboardMenu openDashboardMenu() {
        waitForElementVisible(dashboardSwitcherButton);
        if (dashboardSwitcherButton.getAttribute("class").contains("disabled")) {
            return null;
        }
        dashboardSwitcherButton.click();
        DashboardMenu menu = Graphene.createPageFragment(DashboardMenu.class,
                waitForElementVisible(DashboardMenu.LOCATOR, browser));
        waitForElementVisible(menu.getRoot());
        return menu;
    }
}
