package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertTrue;

import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MainEditor;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.VisualizationReportTypePicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class AnalysisPage extends AbstractFragment {

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

    private static final By BY_TRASH_PANEL = className("adi-trash-panel");

    public AnalysisPage startDrag(WebElement source) {
        WebElement editor = waitForElementVisible(getRoot());

        Point location = editor.getLocation();
        Dimension dimension = editor.getSize();
        getActions().clickAndHold(source)
                .moveByOffset(location.x + dimension.width / 2, location.y + dimension.height / 2).perform();
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
            getActions().moveToElement(waitForElementPresent(target)).perform();
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

    public AnalysisPage replaceMetric(String oldMetric, String newMetric) {
        WebElement source = getCataloguePanel().searchAndGet(newMetric, FieldType.METRIC);
        WebElement target = getMetricsBucket().get(oldMetric);
        return drag(source, target);
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
        return drag(getMetricsBucket().get(metric),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeAttribute(String attr) {
        return drag(getAttributesBucket().get(attr),
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
        return getFilterBuckets().isEmpty() &&
            getMetricsBucket().isEmpty() &&
            getAttributesBucket().isEmpty() &&
            getStacksBucket().isEmpty() &&
            getMainEditor().isEmpty();
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
}
