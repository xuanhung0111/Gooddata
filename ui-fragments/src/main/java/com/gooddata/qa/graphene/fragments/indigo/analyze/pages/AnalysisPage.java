package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertTrue;

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
            bucketsPanel.addMetric(cataloguePanel.getMetric(metric));
        }

        for (String category : reportDefinition.getCategories()) {
            if (DATE.equals(category))
                bucketsPanel.addCategory(cataloguePanel.getTime(category));
            else
                bucketsPanel.addCategory(cataloguePanel.getCategory(category));
        }

        for (String filter : reportDefinition.getFilters()) {
            if (DATE.equals(filter))
                mainEditor.addFilter(cataloguePanel.getTime(filter));
            else
                mainEditor.addFilter(cataloguePanel.getCategory(filter));
        }

        if (reportDefinition.isShowInPercents())
            bucketsPanel.turnOnShowInPercents();

        return this;
    }

    public AnalysisPage removeFilter(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor);
        mainEditor.removeFilter(dateOrAttribute);
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
        bucketsPanel.addCategory(cataloguePanel.getCategory(category));
        return this;
    }

    public AnalysisPage changeReportType(ReportType type) {
        waitForFragmentVisible(bucketsPanel);
        bucketsPanel.setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.isReportTypeSelected(type);
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
        waitForFragmentVisible(mainEditor);
        mainEditor.configTimeFilter(period);
        return this;
    }

    public AnalysisPage configAttributeFilter(String attribute, String... values) {
        waitForFragmentVisible(mainEditor);
        mainEditor.configAttributeFilter(attribute, values);
        return this;
    }

    public TableReport getTableReport() {
        waitForFragmentVisible(mainEditor);
        return mainEditor.getTableReport();
    }

    public ChartReport getChartReport() {
        waitForFragmentVisible(mainEditor);
        return mainEditor.getChartReport();
    }

    public String getTimeDescription(String time) {
        waitForFragmentVisible(cataloguePanel);
        return cataloguePanel.getTimeDescription(time);
    }

    public String getAttributeDescription(String attribute) {
        waitForFragmentVisible(cataloguePanel);
        return cataloguePanel.getAttributeDescription(attribute);
    }

    public String getMetricDescription(String metric) {
        waitForFragmentVisible(cataloguePanel);
        return cataloguePanel.getMetricDescription(metric);
    }

    public List<String> getAllCategoryNames() {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.getAllCategoryNames();
    }

    public boolean isExportToReportButtonEnabled() {
        waitForFragmentVisible(pageHeader);
        return pageHeader.isExportToReportButtonEnable();
    }

    public boolean isShowPercentConfigEnabled() {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.isShowPercentConfigEnabled();
    }

    public boolean isShowPercentConfigSelected() {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.isShowPercentConfigSelected();
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.isCompareSamePeriodConfigEnabled();
    }

    public String getFilterText(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor);
        return mainEditor.getFilterText(dateOrAttribute);
    }

    public boolean isFilterVisible(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor);
        return mainEditor.isFilterVisible(dateOrAttribute);
    }

    public AnalysisPage exportReport() {
        waitForFragmentVisible(pageHeader);
        pageHeader.exportReport();
        return this;
    }

    public String getExplorerMessage() {
        waitForFragmentVisible(mainEditor);
        return mainEditor.getExplorerMessage();
    }

    public AnalysisPage changeGranularity(String time) {
        waitForFragmentVisible(bucketsPanel);
        bucketsPanel.changeGranularity(time);
        return this;
    }

    public List<String> getAllGranularities() {
        waitForFragmentVisible(bucketsPanel);
        return bucketsPanel.getAllGranularities();
    }

    public WebElement getFilter(String dateOrAttribute) {
        waitForFragmentVisible(mainEditor);
        return mainEditor.getFilter(dateOrAttribute);
    }
}
