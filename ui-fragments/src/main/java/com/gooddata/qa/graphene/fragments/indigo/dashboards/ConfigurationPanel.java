package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public class ConfigurationPanel extends AbstractFragment {

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-dataSet_select button")
    private WebElement dataSetSelectLoaded;

    @FindBy(css = ".s-dataSet_select,.s-viz-filter-date-dropdown")
    private DateDimensionSelect dateDataSetSelect;

    @FindBy(className = "s-compare_with_select")
    private ComparisonSelect comparisonSelect;

    @FindBy(className = "s-drill_to_select")
    private DrillToSelect drillToSelect;

    @FindBy(className = "s-button-remove-drill-to")
    private WebElement removeDrillToButton;

    @FindBy(className = "s-widget-alerts-information-loaded")
    private WebElement widgetAlertsLoaded;

    @FindBy(className = "s-alert-edit-warning")
    private WebElement alertEditWarning;

    @FindBy(className = "s-unlisted_measure")
    private WebElement unlistedMeasure;

    @FindBy(css = ".s-viz-filters-date input")
    private WebElement dateFilterCheckbox;

    public ConfigurationPanel waitForButtonsLoaded() {
        waitForElementVisible(metricSelectLoaded);
        final Predicate<WebDriver> dataSetLoaded =
                browser -> !dataSetSelectLoaded.getAttribute("class").contains("is-loading");
        Graphene.waitGui().until(dataSetLoaded);
        return this;
    }

    public ConfigurationPanel selectMetricByName(String name) {
        waitForFragmentVisible(metricSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectDateDataSetByName(String name) {
        waitForFragmentVisible(dateDataSetSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectComparisonByName(String name) {
        waitForFragmentVisible(comparisonSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectDrillToByName(String name) {
        waitForFragmentVisible(drillToSelect).selectByName(name);
        return this;
    }

    public String getDrillToValue() {
        return waitForFragmentVisible(drillToSelect).getSelection();
    }

    public ConfigurationPanel clickRemoveDrillToButton() {
        waitForElementVisible(removeDrillToButton).click();
        return this;
    }

    public String getSelectedDataSet() {
        return waitForFragmentVisible(dateDataSetSelect).getSelection();
    }

    public Collection<String> getDataSets() {
        return waitForFragmentVisible(dateDataSetSelect).getValues();
    }

    public boolean isDataSetEnabled() {
        return !waitForFragmentVisible(dateDataSetSelect)
                .getDropdownButton()
                .getAttribute("class")
                .contains("disabled");
    }

    public MetricSelect getMetricSelect() {
        return waitForFragmentVisible(metricSelect);
    }

    public String getSelectedMetric() {
        return waitForFragmentVisible(metricSelect).getSelection();
    }

    public ConfigurationPanel waitForSelectedMetricIsUnlisted() {
        waitForElementVisible(unlistedMeasure);
        return this;
    }

    public ConfigurationPanel waitForAlertEditWarning() {
        waitForElementPresent(widgetAlertsLoaded);
        waitForElementVisible(alertEditWarning);

        return this;
    }

    public ConfigurationPanel waitForAlertEditWarningMissing() {
        waitForElementPresent(widgetAlertsLoaded);
        waitForElementNotVisible(alertEditWarning, 20);

        return this;
    }

    public String getKpiAlertMessage() {
        return waitForElementVisible(alertEditWarning).getText();
    }

    public ConfigurationPanel enableDateFilter() {
        if (!waitForElementVisible(dateFilterCheckbox).isSelected())
            dateFilterCheckbox.click();
        return this;
    }

    public ConfigurationPanel disableDateFilter() {
        if (waitForElementVisible(dateFilterCheckbox).isSelected())
            dateFilterCheckbox.click();
        return this;
    }

    public boolean isDateFilterCheckboxEnabled() {
        return waitForElementVisible(dateFilterCheckbox).isEnabled();
    }

    public DateDimensionSelect openDateDataset() {
        waitForFragmentVisible(dateDataSetSelect).ensureDropdownOpen();

        return dateDataSetSelect;
    }

    public boolean isDateDatasetSelectCollapsed() {
        return dateDataSetSelect.getDropdownButton().getAttribute("class").contains("s-collapsed");
    }
}
