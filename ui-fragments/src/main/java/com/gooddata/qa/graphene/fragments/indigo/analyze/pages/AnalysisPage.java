package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MainEditor;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.VisualizationReportTypePicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForIndigoMessageDisappear;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;

/**
 * many React fragments used
 */
public class AnalysisPage extends AbstractFragment {

    @FindBy(className = "adi-editor-header")
    private AnalysisPageHeader pageHeader;

    @FindBy(className = "s-catalogue")
    private CataloguePanel cataloguePanel;

    @FindBy(className = "adi-editor-main")
    private MainEditor mainEditor;

    @FindBy(className = "s-visualization-picker")
    private VisualizationReportTypePicker reportTypePicker;

    @FindBy(className = "s-bucket-measures")
    private MetricsBucket metricsBucket;

    @FindBy(className = "s-bucket-secondary_measures")
    private MetricsBucket metricsSecondaryBucket;

    @FindBy(css = ".s-bucket-attribute, .s-bucket-view, .s-bucket-trend")
    private AttributesBucket attributesBucket;

    @FindBy(className = "s-bucket-columns")
    private AttributesBucket attributesColumnsBucket;

    @FindBy(css = ".s-bucket-attribute-title, .s-bucket-view-title, .s-bucket-trend-title")
    private WebElement attributesBucketTitle;

    @FindBy(css = StacksBucket.CSS_SELECTOR)
    private StacksBucket stacksBucket;

    @FindBy(className = "s-bucket-filters")
    private FiltersBucket filterBuckets;

    @FindBy(className = "s-configuration-panel")
    private ConfigurationPanelBucket configurationPanelBucket;

    public static final String MAIN_CLASS = "adi-editor";

    private static final By BY_TRASH_PANEL = className("s-trash");

    private static final By BY_BUCKET_NOT_EMPTY = className("s-bucket-not-empty");

    public static AnalysisPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AnalysisPage.class,
                waitForElementVisible(className(MAIN_CLASS), context));
    }

    public AnalysisPage tryToDrag(WebElement source, WebElement target) {
        startDrag(source);
        getActions().moveToElement(target).release().perform();
        return this;
    }

    public AnalysisPage startDrag(WebElement source) {
        WebElement editor = waitForElementVisible(getRoot());

        Point location = editor.getLocation();
        Dimension dimension = editor.getSize();
        getActions().clickAndHold(source)
                .moveByOffset(location.x + dimension.width / 3, location.y + dimension.height / 3).perform();
        return this;
    }

    public AnalysisPage stopDrag(Point offset) {
        getActions().moveByOffset(offset.x, offset.y).release().perform();
        return this;
    }

    public AnalysisPage addMetricToRecommendedStepsPanelOnCanvas(String metricTitle) {
        return addMetricToRecommendedStepsPanelOnCanvas(metricTitle, FieldType.METRIC);
    }

    public AnalysisPage addMetricToRecommendedStepsPanelOnCanvas(String metricTitle, FieldType type) {
        WebElement source = getCataloguePanel().searchAndGet(metricTitle, type);
        Supplier<WebElement> recommendation = () ->
                waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);
        return drag(source, recommendation);
    }

    public AnalysisPage drag(WebElement source, Supplier<WebElement> target) {
        startDrag(source);
        try {
            getActions().moveToElement(target.get()).moveByOffset(1,1).perform();
            Graphene.waitGui().until(browser -> !isElementPresent(className("s-loading"), target.get()));
        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage drag(WebElement source, WebElement target) {
        startDrag(source);
        try {
            if (!waitForElementVisible(target).getAttribute("class").contains("adi-droppable-active")) {
                moveToBottomDropLineOfElement(target);
                return this;
            }

            // In some specific cases, the target to be dropped is not in viewport,
            // so the selenium script when dragging and dropping element to
            // the target will have a risk that it cannot drop to the right position of target element
            // and element will not be droppable.
            // The solution is move element continuously until the target element is in viewport and droppable.
            Function<WebDriver, Boolean> droppable = browser -> {
                getActions().moveToElement(target).perform();

                return target.getAttribute("class").contains("adi-droppable-hover");
            };

            Graphene.waitGui().until(droppable);

        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage addMetric(String metric) {
        return addMetric(metric, FieldType.METRIC);
    }

    public AnalysisPage addMetric(String data, FieldType type) {
        WebElement source = getCataloguePanel().searchAndGet(data, type);
        WebElement target = getMetricsBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addMetricToSecondaryBucket(String metric) {
        return addMetricToSecondaryBucket(metric, FieldType.METRIC);
    }

    public AnalysisPage addMetricToSecondaryBucket(String data, FieldType type) {
        WebElement source = getCataloguePanel().searchAndGet(data, type);
        WebElement target = getMetricsSecondaryBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addAttribute(String attribute) {
        WebElement target = getAttributesBucket().getInvitation();
        return addAttribute(target, attribute);
    }

    public AnalysisPage addColumnsAttribute(String attribute) {
        WebElement target = getAttributesColumnsBucket().getInvitation();
        return addAttribute(target, attribute);
    }

    public AnalysisPage addDate() {
        WebElement target = getAttributesBucket().getInvitation();
        return addDate(target);
    }

    public AnalysisPage addDateToColumnsAttribute() {
        WebElement target = getAttributesColumnsBucket().getInvitation();
        return addDate(target);
    }

    public AnalysisPage addStack(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addFilter(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addDateFilter() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addMetricAfter(String metric, String newMetric) {
        WebElement source = getCataloguePanel().searchAndGet(newMetric, FieldType.METRIC);
        WebElement target = getMetricsBucket().get(metric);

        try {
            startDrag(source);
            getActions().moveToElement(target).perform();

            Graphene.waitGui().until(browser -> {
                getActions().moveByOffset(0, 5).perform();
                return isElementPresent(By.className("adi-drop-line-bottom"), target.findElement(BY_PARENT));
            });

        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage addAttributeTopRowsBucket(String topAttribute, String newAttribute) {
        WebElement source = getCataloguePanel().searchAndGet(newAttribute, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(topAttribute);

        try {
            startDrag(source);
            moveToTopDropLineOfElement(target);
        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage replaceAttribute(String oldAttr, String newAttr) {
        WebElement source = getCataloguePanel().searchAndGet(newAttr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPage reorderMetric(String sourceMetric, String targetMetric) {
        WebElement source = getMetricsBucket().get(sourceMetric);
        WebElement target = getMetricsBucket().get(targetMetric);
        return drag(source, target);
    }

    public AnalysisPage reorderSecondaryMetric(String sourceMetric, String targetMetric) {
        WebElement source = getMetricsBucket().get(sourceMetric);
        WebElement target = getMetricsSecondaryBucket().get(targetMetric);
        return drag(source, target);
    }

    public AnalysisPage reorderAttribute(String sourceAttribute, String targetAttribute) {
        WebElement source = getAttributesBucket().get(sourceAttribute);
        WebElement target = getAttributesBucket().get(targetAttribute);
        return drag(source, target);
    }

    public AnalysisPage replaceAttributeWithDate(String oldAttr) {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPage replaceAttribute(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getFirst();
        return drag(source, target);
    }

    public AnalysisPage replaceStack(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().get();
        return drag(source, target);
    }

    public AnalysisPage removeMetric(String metric) {
        WebElement header = getMetricsBucket().get(metric).findElement(By.className("s-bucket-item-header"));
        return drag(header ,
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeAttribute(String attr) {
        return drag(getAttributesBucket().get(attr).findElement(By.className("adi-bucket-item-header")),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeFilter(String attr) {
        return drag(getFilterBuckets().getFilter(attr),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeDateFilter() {
        return drag(getFilterBuckets().getDateFilter(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeStack() {
        return drag(getStacksBucket().get(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage changeReportType(ReportType type) {
        waitForFragmentVisible(reportTypePicker).setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(reportTypePicker).isSelected(type);
    }

    public AnalysisPage resetToBlankState() {
        getPageHeader().resetToBlankState();
        assertTrue(isBlankState(), "Should be blank state");
        return this;
    }

    public boolean isBlankState() {
        return getFilterBuckets().isEmpty()
                && getMetricsBucket().isEmpty()
                && getAttributesBucket().isEmpty()
                && getStacksBucket().isEmpty()
                && getMainEditor().isEmpty()
                && getPageHeader().isBlankState();
    }

    public AnalysisPage exportReport() {
        getPageHeader().exportReport();
        return this;
    }

    public String getExplorerMessage() {
        return getMainEditor().getExplorerMessage();
    }

    public boolean isExplorerMessageVisible() {
        return getMainEditor().isExplorerMessageVisible();
    }

    public AnalysisPage waitForReportComputing() {
        getMainEditor().waitForReportComputing();
        return this;
    }

    public boolean isReportComputing() {
        return getMainEditor().isReportComputing();
    }

    public AnalysisPage undo() {
        getPageHeader().undo();
        return this;
    }

    public AnalysisPage redo() {
        getPageHeader().redo();
        return this;
    }

    public TableReport getTableReport() {
        return getMainEditor().getTableReport();
    }

    public TableReport getPivotTableReport() {
        return getMainEditor().getPivotTableReport();
    }

    public ChartReport getChartReport() {
        return getMainEditor().getChartReport();
    }

    public CataloguePanel getCataloguePanel() {
        return waitForFragmentVisible(cataloguePanel).waitForItemLoaded();
    }

    public MetricsBucket getMetricsBucket() {
        return waitForFragmentVisible(metricsBucket);
    }

    //using this fragment when applying the Headline insight(Headline Report Type) for Analysis Page
    public MetricsBucket getMetricsSecondaryBucket() {
        return waitForFragmentVisible(metricsSecondaryBucket);
    }

    public AttributesBucket getAttributesBucket() {
        return waitForFragmentVisible(attributesBucket);
    }

    //To use this function when applying the pivot table
    public AttributesBucket getAttributesColumnsBucket() {
        return waitForFragmentVisible(attributesColumnsBucket);
    }

    public StacksBucket getStacksBucket() {
        return waitForFragmentVisible(stacksBucket);
    }

    public FiltersBucket getFilterBuckets() {
        return waitForFragmentVisible(filterBuckets);
    }

    public MainEditor getMainEditor() {
        return waitForFragmentVisible(mainEditor);
    }

    public AnalysisPageHeader getPageHeader() {
        return waitForFragmentVisible(pageHeader);
    }

    public AnalysisPage switchProject(String name) {
        log.info("Switching to project: " + name);

        Graphene.createPageFragment(Header.class,
                waitForElementVisible(By.className("gd-header"), browser))
                .switchProject(name);

        return this;
    }

    public boolean searchInsight(final String insight) {
        return getPageHeader().expandInsightSelection().searchInsight(insight);
    }

    public AnalysisPage openInsight(final String insight) {
        getPageHeader().expandInsightSelection().openInsight(insight);
        getPageHeader().waitForOpenEnabled();
        return this;
    }

    public AnalysisPage saveInsight() {
        getPageHeader().saveInsight();
        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public boolean isSaveInsightEnabled() {
        return getPageHeader().isSaveButtonEnabled();
    }

    public AnalysisPage saveInsight(final String insight) {
        getPageHeader().saveInsight(insight);
        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public AnalysisPage saveInsightAs(final String insight) {
        getPageHeader().saveInsightAs(insight);
        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public AnalysisPage setInsightTitle(final String title) {
        getPageHeader().setInsightTitle(title);
        return this;
    }

    public AnalysisPage waitForNonEmptyBuckets() {
        waitForElementVisible(BY_BUCKET_NOT_EMPTY, browser);
        return this;
    }

    public AnalysisPage setFilterIsntValues(String filter, String... values) {
        AttributeFilterPickerPanel panel = openFilterPanel(filter);
        panel.checkAllCheckbox();
        Arrays.stream(values).forEach(panel::selectItem);  //To uncheck element
        panel.getApplyButton().click();
        return this;
    }

    public AnalysisPage setFilterIsValues(String filter, String... values) {
        openFilterPanel(filter).select(values);
        return this;
    }

    private AttributeFilterPickerPanel openFilterPanel(String filter) {
        getFilterBuckets().getFilter(filter).click();
        return AttributeFilterPickerPanel.getInstance(browser);
    }

    private void moveToBottomDropLineOfElement(WebElement target) {
        String browserName = ((RemoteWebDriver) BrowserUtils.getBrowserContext()).getCapabilities()
                .getBrowserName().toLowerCase();

        Function<WebDriver, Boolean> droppable = browser -> {
            if ("chrome".equals(browserName)) {
                getActions().moveToElement(target, 1, target.getSize().height / 2 + 1).perform();
            } else {
                getActions().moveToElement(target).perform();
            }

            return isElementPresent(By.className("adi-drop-line-bottom"), getRoot());
        };

        Graphene.waitGui().until(droppable);
    }

    private void moveToTopDropLineOfElement(WebElement target) {
        String browserName = ((RemoteWebDriver) BrowserUtils.getBrowserContext()).getCapabilities()
                .getBrowserName().toLowerCase();
        Function<WebDriver, Boolean> droppable = browser -> {
            if ("chrome".equals(browserName)) {
                getActions().moveToElement(target).perform();
            } else {
                getActions().moveToElement(target, 0, - target.getSize().height / 2).perform();
            }

            return isElementPresent(By.className("adi-drop-line-top"), getRoot());
        };

        Graphene.waitGui().until(droppable);
    }

    private AnalysisPage addAttribute(WebElement typeAttribute, String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        //To avoid move target out of bounds of viewport, should scroll element into view
        Point location = typeAttribute.getLocation();
        Dimension dimension = typeAttribute.getSize();
        if ((location.y + dimension.height) > getRoot().getSize().height) {
            getActions().click(attributesBucketTitle).sendKeys(Keys.END).perform();
        }
        return drag(source, typeAttribute);
    }

    private AnalysisPage addDate(WebElement typeAttribute) {
        WebElement source = getCataloguePanel().getDate();
        return drag(source, typeAttribute);
    }

    /**
     *
     * @param hexColor : Input hex color in custom color palette
     * @return AnalysisPage after input hex color
     */

    public AnalysisPage setCustomColorPicker(String hexColor) {
        getConfigurationPanelBucket().openColorConfiguration().openColorPicker(0)
                .openColorCustomPicker().setColorCustomPicker(hexColor);
        return this;
    }

    public AnalysisPage resetColorPicker() {
        getConfigurationPanelBucket().openColorConfiguration().resetColor();
        return this;
    }

    public ConfigurationPanelBucket getConfigurationPanelBucket() {
        return waitForFragmentVisible(configurationPanelBucket).expandConfigurationPanel();
    }

    public MetricsBucket getMeasureConfigurationPanelBucket() {
        return waitForFragmentVisible(metricsBucket).expandMeasureConfigurationPanel();
    }

    public StacksBucket getStackConfigurationPanelBucket() {
        return waitForFragmentVisible(stacksBucket).expandStackConfigurationPanel();
    }

    public String checkColorItems() {
        getConfigurationPanelBucket().openColorConfiguration();
        return waitForElementPresent(cssSelector(".gd-color-unsupported"), browser).getText();
    }

    public AnalysisPage applyCustomColorPicker() {
        waitForElementPresent(cssSelector(".color-picker-ok-button.s-ok"), browser).click();
        return this;
    }

    public AnalysisPage clickCancelButtonInColorCustomPicker() {
        waitForElementPresent(cssSelector(".color-picker-button.s-cancel"), browser).click();
        return this;
    }

    public AnalysisPage resetColor() {
        waitForElementPresent(cssSelector(".s-reset-colors-button.s-reset_colors"), browser).click();
        return this;
    }

    public boolean isDisableOpenAsReport() {
        return waitForElementVisible(cssSelector(".s-open_as_report"), getRoot()).getAttribute("class").contains("disabled");
    }

}
