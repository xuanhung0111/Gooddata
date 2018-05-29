package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
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
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    @FindBy(css = ".s-bucket-attribute, .s-bucket-view, .s-bucket-trend")
    private AttributesBucket attributesBucket;

    @FindBy(css = StacksBucket.CSS_SELECTOR)
    private StacksBucket stacksBucket;

    @FindBy(className = "s-bucket-filters")
    private FiltersBucket filterBuckets;

    public static final String MAIN_CLASS = "adi-editor";

    private static final By BY_TRASH_PANEL = className("s-trash");

    private static final By BY_BUCKET_NOT_EMPTY = className("s-bucket-not-empty");

    public static AnalysisPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AnalysisPage.class,
                waitForElementVisible(className(MAIN_CLASS), context));
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

    public AnalysisPage drag(WebElement source, Supplier<WebElement> target) {
        startDrag(source);
        try {
            getActions().moveToElement(target.get()).perform();
        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage drag(WebElement source, WebElement target) {
        startDrag(source);
        try {
            if (!waitForElementVisible(target).getAttribute("class").contains("adi-droppable-active")) {
                getActions().moveToElement(target).perform();
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

    public AnalysisPage addAttribute(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addDate() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
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

    public AnalysisPage replaceAttribute(String oldAttr, String newAttr) {
        WebElement source = getCataloguePanel().searchAndGet(newAttr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(oldAttr);
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
        assertTrue(isBlankState());
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

    public ChartReport getChartReport() {
        return getMainEditor().getChartReport();
    }

    public CataloguePanel getCataloguePanel() {
        return waitForFragmentVisible(cataloguePanel);
    }

    public MetricsBucket getMetricsBucket() {
        return waitForFragmentVisible(metricsBucket);
    }

    public AttributesBucket getAttributesBucket() {
        return waitForFragmentVisible(attributesBucket);
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
        panel.getSelectAllButton().click();
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
}
