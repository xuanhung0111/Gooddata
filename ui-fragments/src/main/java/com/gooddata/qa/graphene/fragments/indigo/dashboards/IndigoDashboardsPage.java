package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.browser.DragAndDropUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.utils.CssUtils.convertCSSClassTojQuerySelector;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.base.Predicate;

@SuppressWarnings("unchecked")
public class IndigoDashboardsPage extends AbstractFragment {

    @FindBy(className = "dash-item")
    private List<Widget> widgets;

    @FindBy(className = SPLASH_SCREEN_CLASS_NAME)
    private SplashScreen splashScreen;

    @FindBy(className = "gd-header")
    private Header header;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = SAVE_BUTTON_CLASS_NAME)
    private WebElement saveButton;

    @FindBy(css = "." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)")
    private WebElement enabledSaveButton;

    @FindBy(css = ".dash-nav-right .configuration-panel")
    private ConfigurationPanel configurationPanel;

    @FindBy(className = ATTRIBUTE_FITERS_SELECT_CLASS_NAME)
    private AttributeSelect attributeSelect;

    @FindBy(className = "dash-filters-date")
    private DateFilter dateFilter;

    @FindBy(className = DELETE_BUTTON_CLASS_NAME)
    private WebElement deleteButton;

    @FindBy(className = ATTRIBUTE_FITERS_PANEL_CLASS_NAME)
    private AttributeFiltersPanel attributeFiltersPanel;

    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String SAVE_BUTTON_CLASS_NAME = "s-save_button";
    private static final String DELETE_BUTTON_CLASS_NAME = "s-delete_dashboard";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";
    private static final String SPLASH_SCREEN_CLASS_NAME = "splashscreen";
    private static final String ATTRIBUTE_FITERS_PANEL_CLASS_NAME = "dash-filters-attribute";
    private static final String ATTRIBUTE_FITERS_SELECT_CLASS_NAME = "s-attribute_select";

    private static final String ADD_KPI_PLACEHOLDER = ".add-kpi-placeholder";
    private static final String DASHBOARD_BODY = ".dash-section";

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector("." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)");

    public static final String MAIN_ID = "app-dashboards";

    public static final IndigoDashboardsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(IndigoDashboardsPage.class, waitForElementVisible(id(MAIN_ID), context));
    }

    public SplashScreen getSplashScreen() {
        return waitForFragmentVisible(splashScreen);
    }

    public boolean isSplashScreenPresent() {
        return isElementPresent(className(SPLASH_SCREEN_CLASS_NAME), browser);
    }

    public ConfigurationPanel getConfigurationPanel() {
        return waitForFragmentVisible(configurationPanel);
    }

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementEnabled(waitForElementVisible(editButton)).click();

        waitForElementVisible(cancelButton);

        // There's an animation switching to edit mode,
        // so wait until the css transition is finished
        Sleeper.sleepTight(500);

        // wait until editing is allowed
        return waitForWidgetsEditable();
    }

    public IndigoDashboardsPage cancelEditModeWithoutChange() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);

        return this;
    }

    public IndigoDashboardsPage cancelEditModeWithChanges() {
        waitForElementVisible(cancelButton).click();
        ConfirmDialog.getInstance(browser).submitClick();
        waitForElementNotVisible(cancelButton);

        return this;
    }

    public IndigoDashboardsPage tryCancelingEditModeWithoutApplying() {
        waitForElementVisible(cancelButton).click();
        ConfirmDialog.getInstance(browser).cancelClick();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        waitForElementVisible(editButton);
        waitForWidgetsLoading();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithoutWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        ConfirmDialog.getInstance(browser).submitClick();
        waitForFragmentVisible(splashScreen);

        return this;
    }

    public boolean isSaveEnabled() {
        return isElementPresent(SAVE_BUTTON_ENABLED, browser);
    }

    // if save is disabled, use cancel. But leave edit mode in any case
    public IndigoDashboardsPage leaveEditMode() {
        if (isSaveEnabled()) {
            return saveEditModeWithWidgets();
        }

        waitForElementVisible(cancelButton).click();
        if(ConfirmDialog.isPresent(browser))
            ConfirmDialog.getInstance(browser).submitClick();

        waitForElementNotVisible(cancelButton);
        return this;
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage addAttributeFilter(String attributeTitle) {
        attributeSelect.selectByName(attributeTitle);
        return this;
    }

    public IndigoDashboardsPage addKpi(KpiConfiguration config) {
        dragAddKpiPlaceholder();
        configurationPanel
            .selectMetricByName(config.getMetric())
            .selectDateDataSetByName(config.getDataSet());

        if (config.hasComparison()) {
            configurationPanel.selectComparisonByName(config.getComparison());
        }

        if (config.hasDrillTo()) {
            configurationPanel.selectDrillToByName(config.getDrillTo());
        }

        return waitForWidgetsLoading();
    }

    public boolean isEditButtonVisible() {
        By buttonVisible = By.className(EDIT_BUTTON_CLASS_NAME);
        return isElementPresent(buttonVisible, browser);
    }

    public DateFilter waitForDateFilter() {
        return waitForFragmentVisible(dateFilter);
    }

    public IndigoDashboardsPage selectDateFilterByName(String dateFilterName) {
        waitForWidgetsLoading();

        waitForFragmentVisible(dateFilter)
            .selectByName(dateFilterName);

        return waitForWidgetsLoading();
    }

    public IndigoDashboardsPage dragWidget(final Widget source, final Widget target, DropZone dropZone) {
        final String sourceSelector = convertCSSClassTojQuerySelector(source.getRoot().getAttribute("class"));
        final String targetSelector = convertCSSClassTojQuerySelector(target.getRoot().getAttribute("class"));
        final String dropZoneSelector = targetSelector + " " + dropZone.getCss();

        dragAndDropWithCustomBackend(browser, sourceSelector, targetSelector, dropZoneSelector);

        return this;
    }

    public boolean searchInsight(final String insight) {
        return getInsightSelectionPanel().searchInsight(insight);
    }

    public void logout() {
        waitForFragmentVisible(header).logout();
    }

    public boolean isHamburgerMenuLinkPresent() {
        return waitForFragmentVisible(header).isHamburgerMenuLinkPresent();
    }

    public HamburgerMenu openHamburgerMenu() {
        return waitForFragmentVisible(header).openHamburgerMenu();
    }

    public IndigoDashboardsPage closeHamburgerMenu() {
        waitForFragmentVisible(header).closeHamburgerMenu();
        return this;
    }

    public IndigoDashboardsPage switchProject(String name) {
        log.info("Switching to project: " + name);
        waitForFragmentVisible(header).switchProject(name);

        return this;
    }

    public String getCurrentProjectName() {
        return waitForFragmentVisible(header).getCurrentProjectName();
    }

    public IndigoDashboardsPage deleteDashboard(boolean confirm) {
        waitForElementVisible(deleteButton).click();
        ConfirmDialog deleteConfirmDialog = ConfirmDialog.getInstance(browser);

        if (confirm) {
            deleteConfirmDialog.submitClick();
            waitForElementNotPresent(deleteButton);
        } else {
            deleteConfirmDialog.cancelClick();
        }

        return this;
    }

    public boolean isDeleteButtonVisible() {
        return isElementPresent(By.className(DELETE_BUTTON_CLASS_NAME), this.getRoot());
    }

    public IndigoDashboardsPage waitForSplashscreenMissing() {
        waitForFragmentNotVisible(splashScreen);
        return this;
    }

    public String getDateFilterSelection() {
        return waitForFragmentVisible(dateFilter).getSelection();
    }

    public AttributeFiltersPanel getAttributeFiltersPanel() {
        waitForElementPresent(By.className(ATTRIBUTE_FITERS_PANEL_CLASS_NAME), browser);
        return attributeFiltersPanel;
    }

    public IndigoDashboardsPage waitForAlertsLoaded() {
        waitForElementPresent(By.className(ALERTS_LOADED_CLASS_NAME), browser);
        return this;
    }

    public IndigoDashboardsPage waitForEditingControls() {
        waitForElementVisible(saveButton);
        waitForElementVisible(cancelButton);
        return this;
    }

    public IndigoDashboardsPage waitForWidgetsLoading() {
        // ensure dots element is present
        sleepTightInSeconds(1);

        Predicate<WebDriver> isDotsElementPresent = browser -> !isElementPresent(className("gd-loading-dots"), browser);
        Graphene.waitGui().until(isDotsElementPresent);

        return this;
    }

    public <T extends Widget> T getWidgetByIndex(final Class<T> clazz, final int index) {
        return initWidgetObject(clazz, scrollWidgetIntoView(getWidgets().get(index)));
    }

    public <T extends Widget> T getWidgetByHeadline(final Class<T> clazz, final String headline) {
        return initWidgetObject(clazz, scrollWidgetIntoView(
                getWidgets().stream()
                    .filter(widget -> headline.equals(widget.getHeadline()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find widget with headline: " + headline))));
    }

    public <T extends Widget> T getLastWidget(final Class<T> clazz) {
        return getWidgetByIndex(clazz, getWidgets().size() - 1);
    }

    public <T extends Widget> T getFirstWidget(final Class<T> clazz) {
        return getWidgetByIndex(clazz, 0);
    }

    public <T extends Widget> T selectWidget(final Class<T> clazz, final int index) {
        return (T) getWidgetByIndex(clazz, index).clickOnContent();
    }

    public <T extends Widget> T selectWidgetByHeadline(final Class<T> clazz, final String headline) {
        return (T) getWidgetByHeadline(clazz, headline).clickOnContent();
    }

    public <T extends Widget> T selectFirstWidget(final Class<T> clazz) {
        return selectWidget(clazz, 0);
    }

    public <T extends Widget> T selectLastWidget(final Class<T> clazz) {
        return selectWidget(clazz, getWidgets().size() - 1);
    }

    public int getKpisCount() {
        return (int) getWidgets().stream().filter(Kpi::isKpi).count();
    }

    public int getInsightsCount() {
        return (int) getWidgets().stream().filter(Insight::isInsight).count();
    }

    public IndigoDashboardsPage dragAddKpiPlaceholder() {
        // should fetch dashboard elements to avoid caching in view mode
        waitForElementVisible(cssSelector(ADD_KPI_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_KPI_PLACEHOLDER, DASHBOARD_BODY, DropZone.LAST.getCss());

        return this;
    }

    /**
     * Add an insight to last position in dashboard by drag and drop mode
     * @param insight
     * @return
     */
    public IndigoDashboardsPage addInsight(final String insight) {
        dragAndDropWithCustomBackend(browser,
                convertCSSClassTojQuerySelector(
                        getInsightSelectionPanel().getInsightItem(insight).getRoot().getAttribute("class")),
                DASHBOARD_BODY, DropZone.LAST.getCss());

        return this;
    }

    public IndigoInsightSelectionPanel getInsightSelectionPanel() {
        return IndigoInsightSelectionPanel.getInstance(browser);
    }

    public boolean isOnEditMode() {
        return isElementPresent(By.className("edit-mode-on"), getRoot());
    }

    public boolean isAttributeFilterVisible() {
        return isElementVisible(By.className(ATTRIBUTE_FITERS_SELECT_CLASS_NAME), browser);
    }

    public AttributeSelect openAttributeSelect() {
        waitForFragmentVisible(attributeSelect).ensureDropdownOpen();
        return attributeSelect;
    }

    private IndigoDashboardsPage waitForWidgetsEditable() {
        waitForElementNotPresent(By.cssSelector(".dash-item-content > div:not(.is-editable)"));
        return this;
    }

    private Widget scrollWidgetIntoView(final Widget widget) {
        // Indigo DashBoard can load only one widget on mobile screen, and the others will present in DOM
        // and just visible when scrolling down, so the widget we need to work with may not be visible and interact.
        // And selenium script cannot do anything when an element is not visible.
        // In this stage, java script will be good solution when it can scroll to an element
        // although the element just present and not visible.

        // And the way java script makes element visible is not like when doing manual,
        // it just scroll and calculate until it think the element visible.
        // So in reality view, the element may show full or just a small part of it.
        if (!isElementVisible(widget.getRoot())) {
            scrollElementIntoView(widget.getRoot(), browser);
        }

        return waitForFragmentVisible(widget);
    }

    private List<Widget> getWidgets() {
        return waitForDashboardLoad().waitForWidgetsLoading().widgets;
    }

    private <T extends Widget> T initWidgetObject(final Class<T> clazz, final Widget widget) {
        if (clazz.isAssignableFrom(Kpi.class) && Kpi.isKpi(widget)) {
            return (T) Kpi.getInstance(widget.getRoot());
        }

        if (clazz.isAssignableFrom(Insight.class) && Insight.isInsight(widget)) {
            return (T) Insight.getInstance(widget.getRoot());
        }

        if (clazz.isAssignableFrom(Widget.class))
            return (T) widget;

        throw new RuntimeException("Widget type is not correct !!!");
    }
}
