package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static org.openqa.selenium.By.cssSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.PublishType;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.menu.DashboardMenu;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.utils.CssUtils;
import com.google.common.base.Predicate;

public class DashboardsPage extends AbstractFragment {
    protected static final By BY_DASHBOARD_EDIT_BAR = By.className("s-dashboard-edit-bar");

    private static final By SAVE_AS_DIALOG_LOCATOR = By.className("dashboardSettingsDialogView"); 
    private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
    private static final By BY_PRINTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Preparing printable PDF for download…']");
    private static final By BY_TAB_DROPDOWN_MENU = By.xpath("//div[contains(@class, 's-tab-menu')]");
    private static final By BY_TAB_DROPDOWN_DELETE_BUTTON = By.xpath("//li[contains(@class, 's-delete')]//a");
    private static final By BY_TAB_DROPDOWN_DUPLICATE_BUTTON = By.xpath("//li[contains(@class, 's-duplicate')]//a");
    private static final By BY_TAB_DROPDOWN_COPY_TO_BUTTON = By.xpath("//li[contains(@class, 's-copy_to')]//a");
    private static final By BY_PERMISSION_DIALOG_LOCATOR = By.className("s-permissionSettingsDialog");
    private static final By BY_HIDDEN_TAB_BAR = By.cssSelector(".yui3-dashboardtabs-content.gdc-hidden");
    private static final By BY_PRINT_PDF_BUTTON = By.className("s-printButton");

    @FindBy(xpath = "//div[contains(@class,'yui3-dashboardtabs-content')]")
    private DashboardTabs tabs;

    @FindBy(css = ".q-dashboardSwitcher")
    private WebElement dashboardSwitcherButton;

    @FindBy(css = ".menuArrow")
    private WebElement dashboardSwitcherArrowMenu;

    @FindBy(css = ".s-actionsButton")
    private WebElement editExportEmbedButton;

    @FindBy(xpath = "//button[@title='Add a new tab']")
    private WebElement addNewTabButton;

    @FindBy(xpath = "//div[contains(@class,'editTitleDialogView')]")
    private TabDialog newTabDialog;

    @FindBy(xpath = "//div[contains(@class,'dashboardTitleEditBox')]/input")
    private WebElement newDashboardNameInput;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]")
    private WebElement dashboardTabDeleteDialog;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]//button[text()='Delete']")
    private WebElement dashboardTabDeleteConfirmButton;

    @FindBy(className = "yui3-c-projectdashboard-content")
    private DashboardContent content;

    @FindBy(css = ".s-unlistedIcon:not(.disabled)")
    private WebElement unlistedIcon;

    @FindBy(css = ".s-lockIcon")
    private WebElement lockIcon;

    @FindBy(css = ".s-scheduleButton")
    protected WebElement scheduleButton;

    @FindBy(xpath = "//div[contains(@class,'s-mailScheduleDialog')]")
    protected DashboardScheduleDialog scheduleDialog;

    /**
     * Fragment represents link for saved view dialog on dashboard
     * when saved view mode is turned on
     *
     * @see SavedViewWidget
     */
    @FindBy(xpath = "//div[contains(@class,'savedFilters')]/button")
    private SavedViewWidget savedViewWidget;
    private By emptyTabPlaceholder = By.xpath("//div[contains(@class, 'yui3-c-projectdashboard-placeholder-visible')]");

    public boolean isTabBarVisible() {
        return getRoot().findElements(BY_HIDDEN_TAB_BAR).isEmpty();
    }

    public DashboardTabs getTabs() {
        return waitForFragmentVisible(tabs);
    }

    public List<FilterWidget> getFilters() {
        return content.getFilters();
    }

    public DashboardContent getContent() {
        return content;
    }
    
    public boolean isEmptyDashboard() {
        return getContent().isEmpty();
    }

    public PermissionsDialog getPermissionsDialog() {
        return Graphene.createPageFragment(PermissionsDialog.class, waitForElementVisible(
                BY_PERMISSION_DIALOG_LOCATOR, browser));
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

    public DashboardsPage selectDashboard(String dashboardName) {
        if (getDashboardName().contains(dashboardName)) {
            log.info("Dashboard '" + dashboardName + "'already selected");
            return this;
        }

        openDashboardMenu().selectDashboardByName(dashboardName);
        return this;
    }

    public void selectDashboard(int dashboardIndex) {
        DashboardMenu menu = openDashboardMenu();
        if (menu == null) {
            throw new RuntimeException("This project has only one dashboard!");
        }

        sleepTightInSeconds(3);
        menu.selectDashboardByIndex(dashboardIndex);
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

    public DashboardEditBar editDashboard() {
        if (!isElementPresent(BY_DASHBOARD_EDIT_BAR, browser)) {
            openEditExportEmbedMenu().select("Edit");
        }

        return getDashboardEditBar();
    }

    public DashboardEditBar getDashboardEditBar() {
        return Graphene.createPageFragment(DashboardEditBar.class, waitForElementVisible(BY_DASHBOARD_EDIT_BAR, browser));
    }

    public String exportDashboardTab(int tabIndex) {
        tabs.openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        String tabName = tabs.getTabLabel(0);
        openEditExportEmbedMenu().select("Export to PDF");
        waitForElementVisible(BY_EXPORTING_PANEL, browser);
        sleepTightInSeconds(3);
        waitForElementNotPresent(BY_EXPORTING_PANEL);
        sleepTightInSeconds(3);
        System.out.println("Dashboard " + tabName + " exported to PDF...");
        return tabName;
    }

    public String printDashboardTab(int tabIndex) {
        waitForFragmentVisible(tabs).openTab(tabIndex);
        waitForDashboardPageLoaded(browser);

        waitForElementVisible(BY_PRINT_PDF_BUTTON, getRoot()).click();
        waitForElementVisible(BY_PRINTING_PANEL, browser);
        waitForElementNotPresent(BY_PRINTING_PANEL);

        return tabs.getTabLabel(tabIndex).replace(" ", "_");
    }

    public boolean isPrintButtonVisible() {
        return isElementVisible(BY_PRINT_PDF_BUTTON, getRoot());
    }

    public EmbedDashboardDialog openEmbedDashboardDialog() {
        openEditExportEmbedMenu().select("Embed");

        return Graphene.createPageFragment(EmbedDashboardDialog.class,
                waitForElementVisible(EmbedDashboardDialog.LOCATOR, browser));
    }

    public DashboardsPage addNewTab(String tabName) {
        editDashboard();

        waitForElementVisible(addNewTabButton).click();
        waitForElementVisible(newTabDialog.getRoot());
        newTabDialog.createTab(tabName);

        return this;
    }

    public void deleteDashboardTab(int tabIndex) {
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
        getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
    }

    public DashboardsPage duplicateDashboardTab(int tabIndex) {
        tabs.openTab(tabIndex);
        editDashboard();
        tabs.selectDropDownMenu(tabIndex);
        waitForElementVisible(BY_TAB_DROPDOWN_MENU, browser).findElement(BY_TAB_DROPDOWN_DUPLICATE_BUTTON).click();
        getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);

        return this;
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
        getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
    }

    public DashboardsPage addNewDashboard(String dashboardName) {
        openEditExportEmbedMenu().select("Add Dashboard");
        waitForElementVisible(newDashboardNameInput);

        newDashboardNameInput.click(); //sleep wasn't necessary, getting focus on the input field helps
        newDashboardNameInput.clear();
        newDashboardNameInput.sendKeys(dashboardName);
        newDashboardNameInput.sendKeys(Keys.ENTER);
        getDashboardEditBar().saveDashboard();

        Predicate<WebDriver> notificationPanelShowUp =
                driver -> isElementVisible(cssSelector(".unlistedBubble .ss-delete"), driver);

        try {
            Graphene.waitGui().withTimeout(5, TimeUnit.SECONDS).until(notificationPanelShowUp);

            // turn off notification when creating private dashboard
            waitForElementVisible(cssSelector(".unlistedBubble .ss-delete"), browser).click();
        } catch (TimeoutException e) {
            // do nothing
        }
        return this;
    }

    public void deleteDashboard() {
        editDashboard().deleteDashboard();
    }

    public PermissionsDialog openPermissionsDialog() {
        waitForDashboardPageLoaded(browser);
        openEditExportEmbedMenu().select("Sharing & Permissions");
        return getPermissionsDialog();
    }

    public void publishDashboard(boolean listed) {
        PermissionsDialog dialog = openPermissionsDialog();

        dialog.publish(listed ? PublishType.EVERYONE_CAN_ACCESS : PublishType.SPECIFIC_USERS_CAN_ACCESS);
        dialog.submit();

        if (listed) {
            Predicate<WebDriver> notificationPanelShowUp =
                    b -> isElementVisible(cssSelector(".listedConfirmationBubble button"), browser);

            try {
                Graphene.waitGui().withTimeout(5, TimeUnit.SECONDS).until(notificationPanelShowUp);

                // turn off notification panel when dashboard is visible to everyone
                waitForElementVisible(cssSelector(".listedConfirmationBubble button"), browser).click();
            } catch (TimeoutException e) {
                // do nothing
            }
        }
    }

    public void lockDashboard(boolean lock) {
        PermissionsDialog dialog = openPermissionsDialog();

        if (lock) {
            dialog.lock();
        } else {
            dialog.unlock();
        }

        dialog.submit();
    }

    public PermissionsDialog lockIconClick() {
        waitForElementVisible(lockIcon).click();
        return getPermissionsDialog();
    }

    public PermissionsDialog unlistedIconClick() {
        waitForElementVisible(unlistedIcon).click();
        return getPermissionsDialog();
    }

    public void saveAsDashboard(String dashboardName, PermissionType permissionType) {
        saveAsDashboard(dashboardName, false, permissionType);
    }
    
    public void saveAsDashboardAndEnableSavedViews(String dashboardName, PermissionType permissionType) {
        saveAsDashboard(dashboardName, true, permissionType);
    }

    public FilterWidget getFilterWidget(String condition) {
        return content.getFilterWidget(condition);
    }

    public FilterWidget getFilterWidgetByName(String name) {
        return getContent().getFilterWidgetByName(name);
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

    public boolean isPermissionDialogVisible() {
        return browser.findElements(BY_PERMISSION_DIALOG_LOCATOR).size() > 0 ;
    }

    public DashboardsPage addReportToDashboard(String reportName) {
        editDashboard().addReportToDashboard(reportName);
        return this;
    }

    public DashboardsPage saveDashboard() {
        getDashboardEditBar().saveDashboard();
        return this;
    }

    public DashboardsPage addWebContentToDashboard(String content) {
        editDashboard().addWebContentToDashboard(content);
        return this;
    }

    public EmbeddedWidget getLastEmbeddedWidget() {
        return getContent().getLastEmbeddedWidget();
    }

    public DashboardsPage addLineToDashboard() {
        editDashboard().addLineToDashboard();
        return this;
    }

    public DashboardsPage addWidgetToDashboard(WidgetTypes widgetType, String metricLabel) {
        editDashboard().addWidgetToDashboard(widgetType, metricLabel);
        return this;
    }

    public DashboardsPage openTab(int tabIndex) {
        getTabs().openTab(tabIndex);
        return this;
    }

    public DashboardsPage addListFilterToDashboard(DashFilterTypes type, String name) {
        editDashboard().addListFilterToDashboard(type, name);
        return this;
    }

    public DashboardsPage addTimeFilterToDashboard(String dateDimension, DateGranularity dateGranularity,
            String timeLine) {
        editDashboard().addTimeFilterToDashboard(dateDimension, dateGranularity, timeLine);
        return this;
    }

    public DashboardsPage turnSavedViewOption(boolean on) {
        editDashboard().turnSavedViewOption(on);
        return this;
    }

    public <T extends AbstractReport> T getReport(String name, Class<T> clazz) {
        return getContent().getReport(name, clazz);
    }

    private SimpleMenu openEditExportEmbedMenu() {
        if (waitForElementPresent(editExportEmbedButton).getAttribute("class").contains("gdc-hidden")) {
            throw new RuntimeException("Embed menu is not visible");
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

    private void saveAsDashboard(String dashboardName, boolean isSavedViews, PermissionType permissionType) {
        SaveAsDialog saveAsDialog = openSaveAsDialog();

        saveAsDialog.saveAs(dashboardName, isSavedViews, permissionType);
        waitForFragmentNotVisible(saveAsDialog);

        getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
    }

    private SaveAsDialog openSaveAsDialog() {
        waitForDashboardPageLoaded(browser);
        openEditExportEmbedMenu().select("Save as...");
        return Graphene.createPageFragment(SaveAsDialog.class, 
                waitForElementVisible(SAVE_AS_DIALOG_LOCATOR, browser));
    }
}
