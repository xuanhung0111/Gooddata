package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public class ConfigurationPanel extends AbstractFragment {

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button.is-loaded")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-dimension_select button")
    private WebElement dimensionSelectLoaded;

    @FindBy(className = "s-dimension_select")
    private DateDimensionSelect dateDimensionSelect;

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
        final Predicate<WebDriver> dateDimensionLoaded =
                browser -> !dimensionSelectLoaded.getAttribute("class").contains("is-loading");
        Graphene.waitGui().until(dateDimensionLoaded);
        return this;
    }

    public ConfigurationPanel selectMetricByName(String name) {
        waitForFragmentVisible(metricSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectDateDimensionByName(String name) {
        waitForFragmentVisible(dateDimensionSelect).selectByName(name);
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

    public String getSelectedDateDimension() {
        return waitForFragmentVisible(dateDimensionSelect).getSelection();
    }

    public Collection<String> getDateDimensions() {
        return waitForFragmentVisible(dateDimensionSelect).getValues();
    }

    public boolean isDateDimensionEnabled() {
        return !waitForFragmentVisible(dateDimensionSelect)
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
}
