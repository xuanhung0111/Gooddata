package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertTrue;

public class MeasureAsColumnBucket extends MetricsBucket {

    @FindBy(className = "s-measure-configuration-measures")
    private WebElement configurationIcon;

    public DisplayAsSelect expandMeasuresDisplayAs() {
        if (!isConfigurationMeasuresExpanded()) {
            waitForElementVisible(configurationIcon).click();
            waitForElementAttributeContainValue(configurationIcon, "class", "is-open");
        }
        assertTrue(isConfigurationMeasuresExpanded(), "Configuration Measures should be expanded");
        return Graphene.createPageFragment(DisplayAsSelect.class,
            waitForElementVisible(className("s-measure-configuration-body"), browser));
    }

    private boolean isConfigurationMeasuresExpanded() {
        return waitForElementVisible(configurationIcon).getAttribute("class").contains("is-open");
    }

    public class DisplayAsSelect extends AbstractReactDropDown {

        @Override
        protected String getDropdownCssSelector() {
            throw new UnsupportedOperationException("Unsupported getDropdownCssSelector() method");
        }

        @Override
        protected String getListItemsCssSelector() {
            return ".gd-list-item:not([class*='item-header'])";
        }

        public void selectTo(ReportType reportType) {
            waitForElementVisible(className("gd-vis-item-type-" + reportType.getLabel()), getRoot()).click();
        }

        public Boolean isOptionCheckPresent(OptionalStacking optional) {
            return isElementPresent(By.cssSelector(optional.toString()), browser);
        }

        public Boolean isOptionCheck(OptionalStacking optional) {
            return waitForElementPresent(By.cssSelector(optional.toString()), getRoot()).isSelected();
        }

        public Boolean isOptionEnabled(OptionalStacking optional) {
            return waitForElementPresent(By.cssSelector(optional.toString()), getRoot()).isEnabled();
        }

        public DisplayAsSelect checkOption(OptionalStacking optional) {
            if (!isOptionCheck(optional)) {
                waitForElementVisible(By.cssSelector(optional.getOptionLabel()), getRoot()).click();
            }
            return this;
        }
    }
}
