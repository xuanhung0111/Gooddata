package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import com.gooddata.qa.browser.DragAndDropUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.*;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static com.gooddata.qa.utils.CssUtils.convertCSSClassTojQuerySelector;
import static org.openqa.selenium.By.className;

/**
 * This class uses HTML5 simulated DragAndDrop from DragAndDropUtils.
 * Also assertTrue(isBlankState()) is commented out from resetToBlankState()
 * method until main-editor is fully functional.
 */
public class AnalysisPageReact extends AbstractFragment {

    @FindBy(className = "adi-editor-header")
    private AnalysisPageHeader pageHeader;

    @FindBy(className = "s-catalogue")
    private CataloguePanel cataloguePanel;

    @FindBy(className = "adi-editor-main")
    private MainEditor mainEditor;

    @FindBy(className = "s-visualization-picker")
    private VisualizationReportTypePicker reportTypePicker;

    @FindBy(className = "s-bucket-metrics")
    private MetricsBucket metricsBucket;

    @FindBy(className = "s-bucket-categories")
    private AttributesBucket attributesBucket;

    @FindBy(className = StacksBucket.CSS_CLASS)
    private StacksBucket stacksBucket;

    @FindBy(className = "s-bucket-filters")
    private FiltersBucket filterBuckets;

    public static final String MAIN_CLASS = "adi-editor";

    private static final By BY_TRASH_PANEL = className("s-trash");

    public AnalysisPageReact drag(WebElement source, Supplier<WebElement> target) {
        String src = convertCSSClassTojQuerySelector(source.getAttribute("class"));
        String trg = convertCSSClassTojQuerySelector(target.get().getAttribute("class"));
        DragAndDropUtils.dragAndDrop(browser, src, trg);
        return this;
    }

    public AnalysisPageReact drag(WebElement source, WebElement target) {
        String src = convertCSSClassTojQuerySelector(source.getAttribute("class"));
        String trg = convertCSSClassTojQuerySelector(target.getAttribute("class"));
        DragAndDropUtils.dragAndDrop(browser, src, trg);
        return this;
    }

    public AnalysisPageReact addMetric(String metric) {
        return addMetric(metric, FieldType.METRIC);
    }

    public AnalysisPageReact addMetric(String data, FieldType type) {
        WebElement source = getCataloguePanel().searchAndGet(data, type);
        WebElement target = getMetricsBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addAttribute(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addDate() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addStack(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addFilter(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addDateFilter() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact replaceMetric(String oldMetric, String newMetric) {
        WebElement source = getCataloguePanel().searchAndGet(newMetric, FieldType.METRIC);
        WebElement target = getMetricsBucket().get(oldMetric);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttribute(String oldAttr, String newAttr) {
        WebElement source = getCataloguePanel().searchAndGet(newAttr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttributeWithDate(String oldAttr) {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttribute(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getFirst();
        return drag(source, target);
    }

    public AnalysisPageReact replaceStack(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().get();
        return drag(source, target);
    }

    public AnalysisPageReact removeMetric(String metric) {
        return drag(getMetricsBucket().get(metric),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeAttribute(String attr) {
        return drag(getAttributesBucket().get(attr),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeFilter(String attr) {
        return drag(getFilterBuckets().getFilter(attr),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeDateFilter() {
        return drag(getFilterBuckets().getDateFilter(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeStack() {
        return drag(getStacksBucket().get(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact changeReportType(ReportType type) {
        waitForFragmentVisible(reportTypePicker).setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(reportTypePicker).isSelected(type);
    }

    public AnalysisPageReact resetToBlankState() {
        getPageHeader().resetToBlankState();
//        assertTrue(isBlankState());
        return this;
    }

    public boolean isBlankState() {
        return getFilterBuckets().isEmpty() &&
            getMetricsBucket().isEmpty() &&
            getAttributesBucket().isEmpty() &&
            getStacksBucket().isEmpty() &&
            getMainEditor().isEmpty();
    }

    public AnalysisPageReact exportReport() {
        getPageHeader().exportReport();
        return this;
    }

    public String getExplorerMessage() {
        return getMainEditor().getExplorerMessage();
    }

    public boolean isExplorerMessageVisible() {
        return getMainEditor().isExplorerMessageVisible();
    }

    public AnalysisPageReact waitForReportComputing() {
        getMainEditor().waitForReportComputing();
        return this;
    }

    public boolean isReportComputing() {
        return getMainEditor().isReportComputing();
    }

    public AnalysisPageReact undo() {
        getPageHeader().undo();
        return this;
    }

    public AnalysisPageReact redo() {
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

    public AnalysisPageReact switchProject(String name) {
        log.info("Switching to project: " + name);

        Graphene.createPageFragment(Header.class,
                waitForElementVisible(By.className("gd-header"), browser))
                .switchProject(name);

        return this;
    }
}
