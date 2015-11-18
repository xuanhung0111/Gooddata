package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import java.util.Collection;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ConfigurationPanel extends AbstractFragment {

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button.is-loaded")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-dimension_select button.is-loaded")
    private WebElement dimensionSelectLoaded;

    @FindBy(className = "s-dimension_select")
    private DateDimensionSelect dateDimensionSelect;

    @FindBy(className = "s-compare_with_select")
    private ComparisonSelect comparisonSelect;

    @FindBy(className = "s-drill_to_select")
    private DrillToSelect drillToSelect;

    @FindBy(className = "s-button-remove-drill-to")
    private WebElement removeDrillToButton;

    public ConfigurationPanel waitForButtonsLoaded() {
        waitForElementVisible(metricSelectLoaded);
        waitForElementVisible(dimensionSelectLoaded);
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
}
