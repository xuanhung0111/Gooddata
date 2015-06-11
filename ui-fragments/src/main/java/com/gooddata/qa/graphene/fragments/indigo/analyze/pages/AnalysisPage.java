package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.BucketsPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MainEditor;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class AnalysisPage extends AbstractFragment {

    @FindBy(css = ".adi-editor-header")
    private AnalysisPageHeader pageHeader;

    @FindBy(css = ".s-catalogue")
    private CataloguePanel cataloguePanel;

    @FindBy(css = ".adi-buckets-panel")
    private BucketsPanel bucketsPanel;

    @FindBy(css = ".adi-editor-main")
    private MainEditor mainEditor;

    private static final String DATE = "Date";

    public AnalysisPage dragAndDropMetricToShortcutPanel(String metric, ShortcutPanel shortcutPanel) {
        waitForFragmentVisible(mainEditor);
        waitForFragmentVisible(cataloguePanel);
        mainEditor.dragAndDropMetricToShortcutPanel(cataloguePanel.getMetric(metric), shortcutPanel);
        return this;
    }

    public AnalysisPage createReport(ReportDefinition reportDefinition) {
        waitForFragmentVisible(bucketsPanel);
        waitForFragmentVisible(cataloguePanel);
        waitForFragmentVisible(mainEditor);

        bucketsPanel.setReportType(reportDefinition.getType());

        for (String metric : reportDefinition.getMetrics()) {
            addMetric(metric);
        }

        for (String category : reportDefinition.getCategories()) {
            addCategory(category);
        }

        for (String filter : reportDefinition.getFilters()) {
            addFilter(filter);
        }

        if (reportDefinition.isShowInPercents())
            bucketsPanel.turnOnShowInPercents();

        return this;
    }

    public AnalysisPage turnOnShowInPercents() {
        waitForFragmentVisible(bucketsPanel).turnOnShowInPercents();
        return this;
    }

    public AnalysisPage compareToSamePeriodOfYearBefore() {
        waitForFragmentVisible(bucketsPanel).compareToSamePeriodOfYearBefore();
        return this;
    }

    public AnalysisPage removeFilter(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor).removeFilter(dateOrAttribute);
        return this;
    }

    public AnalysisPage addFilter(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor);
        waitForFragmentVisible(cataloguePanel);
        if (DATE.equals(dateOrAttribute))
            mainEditor.addFilter(cataloguePanel.getTime(dateOrAttribute));
        else
            mainEditor.addFilter(cataloguePanel.getCategory(dateOrAttribute));
        return this;
    }

    public AnalysisPage addMetric(String metric) {
        waitForFragmentVisible(bucketsPanel);
        waitForFragmentVisible(cataloguePanel);
        bucketsPanel.addMetric(cataloguePanel.getMetric(metric));
        return this;
    }

    public AnalysisPage addCategory(String category) {
        waitForFragmentVisible(bucketsPanel);
        waitForFragmentVisible(cataloguePanel);
        if (DATE.equals(category)) {
            bucketsPanel.addCategory(cataloguePanel.getTime(category));
        } else {
            bucketsPanel.addCategory(cataloguePanel.getCategory(category));
        }
        return this;
    }

    public AnalysisPage addInapplicableCategory(String category) {
        waitForFragmentVisible(bucketsPanel);
        waitForFragmentVisible(cataloguePanel);
        bucketsPanel.addCategory(cataloguePanel.getInapplicableCategory(category));
        return this;
    }

    public List<String> getAllCatalogueItemsInViewPort() {
        return waitForFragmentVisible(cataloguePanel).getAllCatalogueItemsInViewPort();
    }

    public AnalysisPage removeCategory(String category) {
        waitForFragmentVisible(bucketsPanel).removeCategory(category);
        return this;
    }

    public AnalysisPage removeMetric(String metric) {
        waitForFragmentVisible(bucketsPanel).removeMetric(metric);
        return this;
    }

    public AnalysisPage changeReportType(ReportType type) {
        waitForFragmentVisible(bucketsPanel).setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(bucketsPanel).isReportTypeSelected(type);
    }

    public AnalysisPage resetToBlankState() {
        waitForFragmentVisible(pageHeader);
        waitForFragmentVisible(bucketsPanel);
        waitForFragmentVisible(mainEditor);
        pageHeader.resetToBlankState();
        assertTrue(bucketsPanel.isBlankState());
        assertTrue(mainEditor.isBlankState());
        return this;
    }

    public AnalysisPage configTimeFilter(String period) {
        waitForFragmentVisible(mainEditor).configTimeFilter(period);
        return this;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public AnalysisPage configTimeFilterByRangeButNotApply(String dateFilter, String from, String to) {
        waitForFragmentVisible(mainEditor).configTimeFilterByRangeButNotApply(dateFilter, from, to);
        return this;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     * @throws ParseException 
     */
    public AnalysisPage configTimeFilterByRange(String dateFilter, String from, String to) throws ParseException {
        waitForFragmentVisible(mainEditor).configTimeFilterByRange(dateFilter, from, to);
        return this;
    }

    public AnalysisPage configAttributeFilter(String attribute, String... values) {
        waitForFragmentVisible(mainEditor).configAttributeFilter(attribute, values);
        return this;
    }

    public TableReport getTableReport() {
        return waitForFragmentVisible(mainEditor).getTableReport();
    }

    public ChartReport getChartReport() {
        return waitForFragmentVisible(mainEditor).getChartReport();
    }

    public String getTimeDescription(String time) {
        return waitForFragmentVisible(cataloguePanel).getTimeDescription(time);
    }

    public String getAttributeDescription(String attribute) {
        return waitForFragmentVisible(cataloguePanel).getAttributeDescription(attribute);
    }

    public String getMetricDescription(String metric) {
        return waitForFragmentVisible(cataloguePanel).getMetricDescription(metric);
    }

    public String getFactDescription(String fact) {
        return waitForFragmentVisible(cataloguePanel).getFactDescription(fact);
    }

    public List<String> getAllAddedCategoryNames() {
        return waitForFragmentVisible(bucketsPanel).getAllAddedCategoryNames();
    }

    public List<String> getAllAddedMetricNames() {
        return waitForFragmentVisible(bucketsPanel).getAllAddedMetricNames();
    }

    public boolean isExportToReportButtonEnabled() {
        return waitForFragmentVisible(pageHeader).isExportToReportButtonEnable();
    }

    public boolean isShowPercentConfigEnabled() {
        return waitForFragmentVisible(bucketsPanel).isShowPercentConfigEnabled();
    }

    public boolean isShowPercentConfigSelected() {
        return waitForFragmentVisible(bucketsPanel).isShowPercentConfigSelected();
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        return waitForFragmentVisible(bucketsPanel).isCompareSamePeriodConfigEnabled();
    }

    public String getFilterText(String dateOrAttribute) {
        return waitForFragmentVisible(mainEditor).getFilterText(dateOrAttribute);
    }

    public String getDateFilterText() {
        return waitForFragmentVisible(mainEditor).getDateFilterText();
    }

    public boolean isFilterVisible(String dateOrAttribute) {
        return waitForFragmentVisible(mainEditor).isFilterVisible(dateOrAttribute);
    }

    public boolean isDateFilterVisible() {
        return waitForFragmentVisible(mainEditor).isDateFilterVisible();
    }

    public AnalysisPage exportReport() {
        waitForFragmentVisible(pageHeader).exportReport();
        return this;
    }

    public String getExplorerMessage() {
        return waitForFragmentVisible(mainEditor).getExplorerMessage();
    }

    public AnalysisPage changeGranularity(String time) {
        waitForFragmentVisible(bucketsPanel).changeGranularity(time);
        return this;
    }

    public String getSelectedDimensionSwitch() {
        return waitForFragmentVisible(bucketsPanel).getSelectedDimensionSwitch();
    }

    public List<String> getAllGranularities() {
        return waitForFragmentVisible(bucketsPanel).getAllGranularities();
    }

    public WebElement getFilter(String dateOrAttribute) {
        return waitForFragmentVisible(mainEditor).getFilter(dateOrAttribute);
    }

    public boolean isExplorerMessageVisible() {
        return waitForFragmentVisible(mainEditor).isExplorerMessageVisible();
    }

    public List<String> getAllTimeFilterOptions() {
        return waitForFragmentVisible(mainEditor).getAllTimeFilterOptions();
    }

    public AnalysisPage waitForReportComputing() {
        waitForFragmentVisible(mainEditor).waitForReportComputing();
        return this;
    }

    public boolean isReportComputing() {
        return waitForFragmentVisible(mainEditor).isReportComputing();
    }

    public boolean searchBucketItem(String item) {
        return waitForFragmentVisible(cataloguePanel).searchBucketItem(item);
    }

    public AnalysisPage undo() {
        waitForFragmentVisible(pageHeader).undo();
        return this;
    }

    public AnalysisPage redo() {
        waitForFragmentVisible(pageHeader).redo();
        return this;
    }

    public boolean isUndoButtonEnabled() {
        return waitForFragmentVisible(pageHeader).isUndoButtonEnabled();
    }

    public boolean isRedoButtonEnabled() {
        return waitForFragmentVisible(pageHeader).isRedoButtonEnabled();
    }

    public boolean isMetricBucketEmpty() {
        return waitForFragmentVisible(bucketsPanel).isMetricBucketEmpty();
    }

    public boolean isCategoryBucketEmpty() {
        return waitForFragmentVisible(bucketsPanel).isCategoryBucketEmpty();
    }

    public boolean isBucketBlankState() {
        return waitForFragmentVisible(bucketsPanel).isBlankState();
    }

    public boolean isMainEditorBlankState() {
        return waitForFragmentVisible(mainEditor).isBlankState();
    }

    public AnalysisPage changeDimensionSwitchInFilter(String currentRelatedDate, String dimensionSwitch) {
        waitForFragmentVisible(mainEditor).changeDimensionSwitchInFilter(currentRelatedDate, dimensionSwitch);
        return this;
    }

    public AnalysisPage changeDimensionSwitchInBucket(String dimensionSwitch) {
        waitForFragmentVisible(bucketsPanel).changeDimensionSwitchInBucket(dimensionSwitch);
        return this;
    }

    public boolean isInapplicableAttributeMetricInViewPort() {
        return waitForFragmentVisible(cataloguePanel).isInapplicableAttributeMetricInViewPort();
    }

    public AnalysisPage addStackBy(String category) {
        waitForFragmentVisible(cataloguePanel);
        waitForFragmentVisible(bucketsPanel).addStackBy(cataloguePanel.getCategory(category));
        return this;
    }

    public AnalysisPage replaceCategory(String category) {
        waitForFragmentVisible(cataloguePanel);
        waitForFragmentVisible(bucketsPanel).replaceCategory(cataloguePanel.getCategory(category));
        return this;
    }

    public void replaceStackBy(String category) {
        waitForFragmentVisible(cataloguePanel);
        waitForFragmentVisible(bucketsPanel).replaceStackBy(cataloguePanel.getCategory(category));
    }

    public boolean isStackByDisabled() {
        return waitForFragmentVisible(bucketsPanel).isStackByDisabled();
    }

    public String getStackByMessage() {
        return waitForFragmentVisible(bucketsPanel).getStackByMessage();
    }

    public String getMetricMessage() {
        return waitForFragmentVisible(bucketsPanel).getMetricMessage();
    }

    public String getAddedStackByName() {
        return waitForFragmentVisible(bucketsPanel).getAddedStackByName();
    }

    public boolean isStackByBucketEmpty() {
        return waitForFragmentVisible(bucketsPanel).isStackByBucketEmpty();
    }

    public String getExportToReportButtonTooltipText() {
        return waitForFragmentVisible(pageHeader).getExportToReportButtonTooltipText();
    }
}
