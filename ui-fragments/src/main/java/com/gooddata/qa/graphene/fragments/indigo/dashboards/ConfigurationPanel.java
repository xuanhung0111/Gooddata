package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public class ConfigurationPanel extends AbstractFragment {

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button.is-loaded")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-dataSet_select button")
    private WebElement dataSetSelectLoaded;

    @FindBy(className = "s-dataSet_select")
    private DataSetSelect dataSetSelect;

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

    public ConfigurationPanel selectDataSetByName(String name) {
        waitForFragmentVisible(dataSetSelect).selectByName(name);
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
        return waitForFragmentVisible(drillToSelect).getDropdownButton().getText();
    }

    public ConfigurationPanel clickRemoveDrillToButton() {
        waitForElementVisible(removeDrillToButton).click();
        return this;
    }

    public String getSelectedDataSet() {
        return waitForFragmentVisible(dataSetSelect).getSelection();
    }

    public Collection<String> getDataSets() {
        return waitForFragmentVisible(dataSetSelect).getValues();
    }

    public boolean isDataSetEnabled() {
        return !waitForFragmentVisible(dataSetSelect)
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
}
