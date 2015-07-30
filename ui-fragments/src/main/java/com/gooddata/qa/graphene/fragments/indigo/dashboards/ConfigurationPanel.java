package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
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

    public String getSelectedDateDimension() {
        return waitForFragmentVisible(dateDimensionSelect).getSelection();
    }

    public Collection<String> getDateDimensions() {
        return waitForFragmentVisible(dateDimensionSelect).getValues();
    }

}
