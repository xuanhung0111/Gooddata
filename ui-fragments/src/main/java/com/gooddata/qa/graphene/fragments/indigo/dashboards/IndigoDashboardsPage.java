package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class IndigoDashboardsPage extends AbstractFragment {
    @FindBy(className = Kpi.MAIN_CLASS)
    private List<Kpi> kpis;

    @FindBy(className = EDIT_BUTTON_SELECTOR)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = "s-save_button")
    private WebElement saveButton;

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(className = "dashboard")
    private WebElement dashboard;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' s-dialog ')]")
    private ConfirmDialog dialog;

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final String EDIT_BUTTON_SELECTOR = "s-edit_button";

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementVisible(editButton).click();
        waitForElementVisible(this.cancelButton);

        return this;
    }

    public IndigoDashboardsPage cancelEditMode() {
        waitForElementVisible(cancelButton).click();

        return this;
    }

    public IndigoDashboardsPage saveEditMode() {
        waitForElementVisible(saveButton).click();
        waitForElementVisible(this.editButton);

        return this;
    }

    public ConfirmDialog waitForDialog() {
        waitForElementVisible(dialog.getRoot());
        return dialog;
    }

    public Kpi selectKpi(int index) {
        Kpi tempKpi = getKpiByIndex(index);
        tempKpi.getRoot().click();
        waitForElementVisible(metricSelect.getRoot());
        return tempKpi;
    }

    public Kpi getKpiByIndex(int index) {
        return kpis.get(index);
    }

    public IndigoDashboardsPage selectMetricByName(String name) {
        waitForElementVisible(metricSelect.getRoot());
        metricSelect.byName(name);
        waitForKpiLoading();

        return this;
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage waitForKpiLoading() {
        waitForElementNotPresent(Kpi.IS_LOADING);

        return this;
    }

    public boolean checkIfEditButtonIsPresent() {
        try {
            browser.findElement(By.className(EDIT_BUTTON_SELECTOR));
            return true;
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

}
