package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.collect.Iterables;

public class IndigoDashboardsPage extends AbstractFragment {

    @FindBy(css = Kpi.KPI_CSS_SELECTOR)
    private List<Kpi> kpis;

    @FindBy(className = "splashscreen")
    private SplashScreen splashScreen;

    @FindBy(className = "gd-header")
    private Header header;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = SAVE_BUTTON_CLASS_NAME)
    private WebElement saveButton;

    @FindBy(className = "configuration-panel")
    private ConfigurationPanel configurationPanel;

    @FindBy(className = "kpi-placeholder")
    private WebElement addWidget;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' s-dialog ')]")
    private ConfirmDialog dialog;

    @FindBy(className = "dash-filters-date")
    private DateFilter dateFilter;

    @FindBy(className = DELETE_BUTTON_CLASS_NAME)
    private WebElement deleteButton;

    @FindBy(css = ".dash-filters-attribute.are-loaded")
    private AttributeFiltersPanel attributeFiltersPanel;

    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String SAVE_BUTTON_CLASS_NAME = "s-save_button";
    private static final String DELETE_BUTTON_CLASS_NAME = "s-delete_dashboard";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector("." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)");

    public static final String MAIN_ID = "app-dashboards";

    public SplashScreen getSplashScreen() {
        return waitForFragmentVisible(splashScreen);
    }

    public ConfigurationPanel getConfigurationPanel() {
        return configurationPanel;
    }

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementVisible(editButton).click();
        waitForElementVisible(cancelButton);

        // There's an animation switching to edit mode,
        // so wait until the css transition is finished
        Sleeper.sleepTight(500);

        // wait until editing is allowed
        return waitForKpiEditable();
    }

    public IndigoDashboardsPage cancelEditMode() {
        waitForElementVisible(cancelButton).click();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithKpis() {
        waitForElementVisible(saveButton).click();
        waitForElementVisible(editButton);

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithoutKpis() {
        waitForElementVisible(saveButton).click();
        this.waitForDialog()
                .submitClick();
        waitForFragmentVisible(splashScreen);

        return this;
    }

    public boolean isSaveEnabled() {
        return isElementPresent(SAVE_BUTTON_ENABLED, browser);
    }

    // if save is disabled, use cancel. But leave edit mode in any case
    public IndigoDashboardsPage leaveEditMode() {
        if (isSaveEnabled()) {
            return saveEditModeWithKpis();
        }

        return cancelEditMode();
    }

    public ConfirmDialog waitForDialog() {
        waitForElementVisible(dialog.getRoot());
        return dialog;
    }

    public int getKpisCount() {
        return kpis.size();
    }

    public Kpi selectKpi(int index) {
        Kpi tempKpi = getKpiByIndex(index);
        waitForElementPresent(tempKpi.getRoot());
        tempKpi.clickKpiContent();

        waitForFragmentVisible(configurationPanel).waitForButtonsLoaded();
        return tempKpi;
    }

    public Kpi selectLastKpi() {
        return selectKpi(kpis.size() - 1);
    }

    public Kpi getFirstKpi() {
        return getKpiByIndex(0);
    }

    public Kpi getLastKpi() {
        return getKpiByIndex(kpis.size() - 1);
    }

    public IndigoDashboardsPage clickLastKpiDeleteButton() {
        selectLastKpi().clickKpiDeleteButton();

        return this;
    }

    public IndigoDashboardsPage deleteKpi(int index) {
        selectKpi(index).clickKpiDeleteButton();

        return this;
    }

    public IndigoDashboardsPage deleteKpi(Kpi kpi) {
        kpi.clickKpiDeleteButton();

        return this;
    }

    public String getValueFromKpi(final String name) {
        return Iterables.find(kpis, input -> name.equals(input.getHeadline())).getValue();
    }

    public Kpi getKpiByIndex(int index) {
        return kpis.get(index);
    }

    public Kpi getKpiByHeadline(final String headline) {
        return kpis.stream()
            .filter(kpi -> headline.equals(kpi.getHeadline()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find Kpi with headline: " + headline));
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetsLoaded() {
        waitForElementNotPresent(Kpi.IS_WIDGET_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForAnyKpiWidgetContentLoading() {
        int KPI_INITIATE_LOAD_TIMEOUT_SECONDS = 2;
        // wait until the loading is initiated. If not, assume kpi widget
        // was already reloaded, even before we started waiting
        try {
            waitForElementVisible(Kpi.IS_CONTENT_LOADING, browser, KPI_INITIATE_LOAD_TIMEOUT_SECONDS);
        } catch (TimeoutException ex) { }

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetContentLoaded() {
        waitForAllKpiWidgetsLoaded();
        waitForElementNotPresent(Kpi.IS_CONTENT_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForKpiEditable() {
        waitForElementNotPresent(Kpi.IS_NOT_EDITABLE);

        return this;
    }

    public IndigoDashboardsPage clickAddWidget() {
        waitForElementPresent(addWidget).click();
        return this;
    }

    public IndigoDashboardsPage addWidget(KpiConfiguration config) {
        clickAddWidget();
        configurationPanel
            .selectMetricByName(config.getMetric())
            .selectDateDimensionByName(config.getDateDimension());

        if (config.hasComparison()) {
            configurationPanel.selectComparisonByName(config.getComparison());
        }

        if (config.hasDrillTo()) {
            configurationPanel.selectDrillToByName(config.getDrillTo());
        }

        return waitForAllKpiWidgetContentLoaded();
    }

    public boolean isEditButtonVisible() {
        By buttonVisible = By.className(EDIT_BUTTON_CLASS_NAME);
        return isElementPresent(buttonVisible, browser);
    }

    public DateFilter waitForDateFilter() {
        return waitForFragmentVisible(dateFilter);
    }

    public IndigoDashboardsPage selectDateFilterByName(String dateFilterName) {
        waitForAllKpiWidgetContentLoaded();

        waitForFragmentVisible(dateFilter)
            .selectByName(dateFilterName);

        return waitForAllKpiWidgetContentLoaded();
    }

    public String getDateFilterSelection() {
        return waitForFragmentVisible(dateFilter).getSelection();
    }

    public AttributeFiltersPanel waitForAttributeFilters() {
        return waitForFragmentVisible(attributeFiltersPanel);
    }

    public IndigoDashboardsPage waitForAlertsLoaded() {
        waitForElementPresent(By.className(ALERTS_LOADED_CLASS_NAME), browser);
        return this;
    }

    public IndigoDashboardsPage deleteDashboard(boolean confirm) {
        waitForElementVisible(deleteButton).click();
        ConfirmDialog deleteConfirmDialog = waitForDialog();

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

    public IndigoDashboardsPage waitForEditingControls() {
        waitForElementVisible(saveButton);
        waitForElementVisible(cancelButton);
        return this;
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
}
