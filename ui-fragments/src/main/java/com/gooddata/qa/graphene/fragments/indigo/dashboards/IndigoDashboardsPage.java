package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import org.openqa.selenium.TimeoutException;

public class IndigoDashboardsPage extends AbstractFragment {
    @FindBy(css = Kpi.KPI_CSS_SELECTOR)
    private List<Kpi> kpis;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = "s-save_button")
    private WebElement saveButton;

    @FindBy(className = "configuration-panel")
    private ConfigurationPanel configurationPanel;

    @FindBy(className = "kpi-placeholder")
    private WebElement addWidget;

    @FindBy(className = "dashboard")
    private WebElement dashboard;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' s-dialog ')]")
    private ConfirmDialog dialog;

    @FindBy(className = "dash-filters-date")
    private DateFilter dateFilter;

    @FindBy(css = ".dash-filters-attribute.are-loaded")
    private AttributeFiltersPanel attributeFiltersPanel;

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector(".s-save_button:not(.disabled)");

    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";

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

    public IndigoDashboardsPage saveEditMode() {
        waitForElementVisible(saveButton).click();
        waitForElementVisible(editButton);

        return this;
    }

    // if save is disabled, use cancel. But leave edit mode in any case
    public IndigoDashboardsPage leaveEditMode() {
        boolean isSaveEnabled = isElementPresent(SAVE_BUTTON_ENABLED, browser);
        if (isSaveEnabled) {
            return saveEditMode();
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
        tempKpi.getRoot().click();

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

    public IndigoDashboardsPage addWidget(String metricName, String dateDimensionName) {
        clickAddWidget();
        configurationPanel
                .selectMetricByName(metricName)
                .selectDateDimensionByName(dateDimensionName);

        return waitForAllKpiWidgetContentLoaded();
    }

    public IndigoDashboardsPage addWidget(String metricName, String dateDimensionName, String comparisonName) {
        clickAddWidget();
        configurationPanel
                .selectMetricByName(metricName)
                .selectDateDimensionByName(dateDimensionName)
                .selectComparisonByName(comparisonName);

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

    public AttributeFiltersPanel waitForAttributeFilters() {
        return waitForFragmentVisible(attributeFiltersPanel);
    }

    public IndigoDashboardsPage waitForAlertsLoaded() {
        waitForElementPresent(By.className(ALERTS_LOADED_CLASS_NAME), browser);
        return this;
    }
}
