package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import com.gooddata.qa.graphene.common.Sleeper;

public class IndigoDashboardsPage extends AbstractFragment {
    @FindBy(css = Kpi.KPI_CSS_SELECTOR)
    private List<Kpi> kpis;

    @FindBy(className = EDIT_BUTTON_SELECTOR)
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

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector(".s-save_button:not(.disabled)");
    private static final String EDIT_BUTTON_SELECTOR = "s-edit_button";

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

    public IndigoDashboardsPage deleteLastKpi() {
        selectLastKpi().deleteKpi();

        return this;
    }

    public IndigoDashboardsPage deleteKpi(int index) {
        selectKpi(index).deleteKpi();

        return this;
    }

    public String getValueFromKpi(final String name) {
        return Iterables.find(kpis, new Predicate<Kpi>() {
            @Override
            public boolean apply(Kpi input) {
                return name.equals(input.getHeadline());
            }
        }).getValue();
    }

    public Kpi getKpiByIndex(int index) {
        return kpis.get(index);
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetsLoaded(){
        waitForElementNotPresent(Kpi.IS_WIDGET_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetContentLoading(){
        waitForElementPresent(Kpi.IS_CONTENT_LOADING, browser);

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetContentLoaded(){
        waitForAllKpiWidgetsLoaded();
        waitForElementNotPresent(Kpi.IS_CONTENT_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForKpiEditable(){
        waitForElementVisible(Kpi.IS_EDITABLE, browser);

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

    public boolean isEditButtonPresent() {
        return isElementPresent(By.className(EDIT_BUTTON_SELECTOR), browser);
    }
}
