package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import java.util.Collection;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;
import static java.lang.String.format;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigurationPanel extends AbstractFragment {

    private static final String CONFIGURATION_PANEL_ROOT = ".s-gd-configuration-bubble";
    private static final By BY_DRILL_TO_SELECT = By.className("s-drill_to_select");
    private static final By BY_DRILL_TO_DASHBOARD = By.className("s-dashboards-dropdown-button");

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-date-dataset-button")
    private WebElement dataSetSelectLoaded;

    @FindBy(css = ".s-filter-date-dropdown")
    private DateDimensionSelect dateDataSetSelect;

    @FindBy(className = "s-compare_with_select")
    private ComparisonSelect comparisonSelect;

    @FindBy(className = "s-button-remove-drill-to")
    private WebElement removeDrillToButton;

    @FindBy(className = "s-widget-alerts-information-loaded")
    private WebElement widgetAlertsLoaded;

    @FindBy(className = "s-alert-edit-warning")
    private WebElement alertEditWarning;

    @FindBy(className = "s-unlisted_measure")
    private WebElement unlistedMeasure;

    @FindBy(className = "s-attribute-filter-by-item")
    private List<FilterByItem> filterByAttributeFilters;

    @FindBy(className = "s-date-filter-by-item")
    private FilterByItem filterByDateFilter;

    @FindBy(className = "s-drill-show-measures")
    private WebElement addInteraction;

    @FindBy(xpath = "//*[@class='insight-configuration']//div[contains(@class, 'is-submenu')]//span[contains(text(), 'Interactions')]")
    private WebElement insightInteractions;

    @FindBy(xpath = "//*[@class='insight-configuration']//div[contains(@class, 'is-submenu')]//span[contains(text(), 'Configuration')]")
    private WebElement insightConfig;


    public static ConfigurationPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(ConfigurationPanel.class,
                waitForElementVisible(By.cssSelector(CONFIGURATION_PANEL_ROOT), context));
    }

    public List<FilterByItem> getFilterByAttributeFilters() {
        return filterByAttributeFilters;
    }

    public FilterByItem getFilterByAttributeFilter(String filterTitle) {
        return getFilterByAttributeFilters()
                .stream()
                .filter(filter -> filter.getTitle().equalsIgnoreCase(filterTitle))
                .findFirst()
                .get();
    }

    public FilterByItem getFilterByDateFilter() {
        waitForElementVisible(insightConfig).click();
        return filterByDateFilter;
    }

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector(".gd-message.error");
    private static final By SUCCESS_MESSAGE_LOCATOR = By.cssSelector(".gd-message.success");

    private ConfigurationPanel waitForVisDateDataSetsLoaded() {
        final Function<WebDriver, Boolean> dataSetLoaded =
                browser -> !dataSetSelectLoaded.getAttribute("class").contains("is-loading");
        Graphene.waitGui().until(dataSetLoaded);
        return this;
    }

    public List<String> getListDateDataset(){
        waitForVisDateDataSetsLoaded();
        waitForElementVisible(dataSetSelectLoaded).click();
        return waitForCollectionIsNotEmpty(browser.findElements(By.className("gd-list-item-shortened"))
                .stream().map(e->e.getText()).collect(Collectors.toList()));
    }

    public ConfigurationPanel waitForButtonsLoaded() {
        waitForElementVisible(metricSelectLoaded);
        return waitForVisDateDataSetsLoaded();
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
        getDrillToSelect().selectByName(name);
        return this;
    }

    public String getDrillToValue() {
        return getDrillToSelect().getSelection();
    }

    public ConfigurationPanel clickRemoveDrillToButton() {
        waitForElementVisible(removeDrillToButton).click();
        return this;
    }

    public String getSelectedDataSet() {
        waitForVisDateDataSetsLoaded();
        return waitForFragmentVisible(dateDataSetSelect).getSelection();
    }

    public Collection<String> getDataSets() {
        return waitForFragmentVisible(dateDataSetSelect).getValues();
    }

    public boolean isDateDataSetDropdownVisible() {
        return isElementPresent(By.className("s-filter-date-dropdown"), browser);
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
        getFilterByDateFilter().setChecked(true);
        return this;
    }

    public ConfigurationPanel disableDateFilter() {
        getFilterByDateFilter().setChecked(false);
        return this;
    }

    public boolean isDateFilterCheckboxChecked() {
        return getFilterByDateFilter().isChecked();
    }

    public boolean isDateFilterCheckboxEnabled() {
        return getFilterByDateFilter().isCheckboxEnabled();
     }

    public DateDimensionSelect openDateDataSet() {
        waitForFragmentVisible(dateDataSetSelect).ensureDropdownOpen();

        return dateDataSetSelect;
    }

    public boolean isDateDataSetSelectCollapsed() {
        return !dateDataSetSelect.isDropdownOpen();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, getRoot()).getText();
    }

    public String getSuccessMessage() {
        return waitForElementVisible(SUCCESS_MESSAGE_LOCATOR, getRoot()).getText();
    }    

    public String getSelectedDataSetColor() {
        return waitForFragmentVisible(dateDataSetSelect).getSelectionColor();
    }

    public boolean isErrorMessagePresent() {
        waitForVisDateDataSetsLoaded();
        return isElementPresent(ERROR_MESSAGE_LOCATOR, getRoot());
    }

    public boolean isDrillToSelectVisible() {
        return isElementVisible(BY_DRILL_TO_SELECT, getRoot());
    }

    public DrillMeasureDropDown.DrillConfigPanel drillIntoInsight(String metric, String insight) {
        waitForElementVisible(insightInteractions).click();
        waitForElementVisible(addInteraction).click();
        DrillMeasureDropDown.getInstance(browser).selectByName(metric);
        return DrillMeasureDropDown.DrillConfigPanel.getInstance(browser).drillIntoInsight(insight);
    }

    public DrillMeasureDropDown.DrillConfigPanel drillIntoDashboard(String metric, String dashboard) {
        waitForElementVisible(addInteraction).click();
        DrillMeasureDropDown.getInstance(browser).selectByName(metric);
        return DrillMeasureDropDown.DrillConfigPanel.getInstance(browser).drillIntoDashboard(dashboard);
    }

    public DrillMeasureDropDown.DrillConfigPanel changeTargetDashboard(String metric, String oldDashboard, String newDashboard) {
        return DrillMeasureDropDown.DrillConfigPanel.getInstance(browser).changeTargetDashboard(metric, oldDashboard, newDashboard);
    }

    private DrillToSelect getDrillToSelect() {
        return Graphene.createPageFragment(DrillToSelect.class, waitForElementVisible(BY_DRILL_TO_SELECT, getRoot()));
    }

    public List<String> getTargetDashboard() {
        return waitForCollectionIsNotEmpty(browser.findElements(BY_DRILL_TO_DASHBOARD)
                .stream().map(e->e.getText()).collect(Collectors.toList()));
    }

    public static class DrillMeasureDropDown extends AbstractReactDropDown {

        public static By ROOT = className("s-drill-measure-selector-dropdown");

        @Override
        public AbstractReactDropDown selectByName(String name) {
            getElementByName(name).click();
            return this;
        }

        @Override
        protected String getDropdownButtonCssSelector() {
            return ".s-add_interaction.gd-button";
        }

        @Override
        protected String getListItemsCssSelector() {
            return ".gd-drill-measure-selector-list .s-drill-measure-selector-item";
        }

        @Override
        protected String getDropdownCssSelector() {
            return ".overlay.dropdown-body";
        }

        public static DrillMeasureDropDown getInstance(SearchContext searchContext) {
            WebElement root = waitForElementVisible(ROOT, searchContext);

            return Graphene.createPageFragment(DrillMeasureDropDown.class, root);
        }

        public static class DrillConfigPanel extends AbstractFragment {

            @FindBy(className = "s-choose_action_")
            private WebElement chooseAction;

            public static By ROOT = className("s-drill-config-panel");
            public static By DRILL_TO_INSIGHT = className("s-drilltoinsight");
            public static By CHOOSE_INSIGHT = className("s-choose_insight_");
            public static By DRILL_TO_DASHBOARD = className("s-drilltodashboard");
            public static By CHOOSE_DASHBOARD = className("s-choose_dashboard_");

            public static DrillConfigPanel getInstance(SearchContext searchContext) {
                WebElement root = waitForElementVisible(ROOT, searchContext);

                return Graphene.createPageFragment(DrillConfigPanel.class, root);
            }

            public DrillConfigPanel drillIntoInsight(String insight) {
                waitForElementVisible(chooseAction).click();
                waitForElementVisible(DRILL_TO_INSIGHT, browser).click();

                //Click action on element does not affect sometimes, so switch to use java script executor.
                BrowserUtils.runScript(browser, "arguments[0].click();",
                    waitForElementVisible(CHOOSE_INSIGHT, getRoot()));
                AnalysisInsightSelectionPanel.getInstance(browser).openInsight(insight);
                return this;
            }

            public DrillConfigPanel drillIntoDashboard(String dashboard) {
                waitForElementVisible(chooseAction).click();
                waitForElementVisible(DRILL_TO_DASHBOARD, browser).click();

                // Click action on element does not affect sometimes, so switch to use java script executor.
                BrowserUtils.runScript(browser, "arguments[0].click();",
                    waitForElementVisible(CHOOSE_DASHBOARD, getRoot()));                
                DashboardSelectionPanel.getInstance(browser).selectByName(dashboard);
                return this;
            }

            public DrillConfigPanel changeTargetDashboard(String metric, String oldDashboard, String newDashboard) {     
                // Click action on element does not affect sometimes, so switch to use java script executor.                
                BrowserUtils.runScript(browser, "arguments[0].click();",
                    waitForElementVisible(browser.findElement(By.cssSelector(
                        ".s-drill-config-item-" + simplifyText(metric) + " .s-" + simplifyText(oldDashboard)))), getRoot());                
                DashboardSelectionPanel.getInstance(browser).selectByName(newDashboard);
                return this;
            }
        }
    }
}
