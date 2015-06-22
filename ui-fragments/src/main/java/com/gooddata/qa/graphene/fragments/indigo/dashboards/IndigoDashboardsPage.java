package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

public class IndigoDashboardsPage extends AbstractFragment {
    @FindBy(className = Kpi.MAIN_CLASS)
    protected List<Kpi> kpis;

    @FindBy(className = "s-edit_button")
    protected WebElement editButton;

    @FindBy(className = "s-cancel_button")
    protected WebElement cancelButton;

    @FindBy(className = "s-metric_select")
    protected WebElement metricSelect;

    @FindBy(className = "dashboard")
    protected WebElement dashboard;

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementVisible(editButton).click();
        waitForElementVisible(this.cancelButton);

        return this;
    }

    public IndigoDashboardsPage cancelEditMode() {
        waitForElementVisible(cancelButton).click();
        waitForElementVisible(this.editButton);

        return this;
    }

    public Kpi selectKpi(int index) {
        Kpi tempKpi = getKpiByIndex(index);
        tempKpi.getRoot().click();
        waitForElementPresent(metricSelect);
        return tempKpi;
    }

    public Kpi getKpiByIndex(int index) {
        return kpis.get(index);
    }

    public IndigoDashboardsPage selectMetricByIndex(int index) {
        Select dropDown = new Select(waitForElementVisible(this.metricSelect));
        dropDown.selectByIndex(index);
        waitForKpiLoading();

        return this;
    }

    public void waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);
    }

    public void waitForKpiLoading(){
        waitForElementNotPresent(Kpi.IS_LOADING);
    }

}
