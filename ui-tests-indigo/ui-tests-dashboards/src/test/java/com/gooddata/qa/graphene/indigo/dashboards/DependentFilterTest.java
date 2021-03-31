package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.*;

public class DependentFilterTest extends AbstractDashboardTest {

    public static final String STAGE_NAME_INSIGHT = "Stage Name Insight";
    public static final String IS_WON_INSIGHT = "Is Won Insight";
    public static final String ACCOUNT_INSIGHT = "Account Insight";
    public static final String DASHBOARD_TEST = "Dashboard Test";
    public static final String AMOUNT_AND_STAGE_NAME = "Data Insight";

    private IndigoRestRequest indigoRestRequest;
    private PivotTableReport stageNameInsight, isWonInsight, accountInsight;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD, false);
        createInsight(STAGE_NAME_INSIGHT, singletonList(Pair.of(ATTR_STAGE_NAME, CategoryBucket.Type.ATTRIBUTE)));
        createInsight(IS_WON_INSIGHT, singletonList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE)));
        createInsight(ACCOUNT_INSIGHT, singletonList(Pair.of(ATTR_ACCOUNT, CategoryBucket.Type.ATTRIBUTE)));
        createInsight(AMOUNT_AND_STAGE_NAME, asList(Pair.of(ATTR_STAGE_NAME, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE)), METRIC_AMOUNT);

        initIndigoDashboardsPage().addDashboard().addInsight(STAGE_NAME_INSIGHT).resizeWidthOfWidget(ResizeBullet.THREE)
                .addInsight(IS_WON_INSIGHT, Widget.DropZone.LAST).resizeWidthOfWidget(ResizeBullet.THREE)
                .addInsight(ACCOUNT_INSIGHT, Widget.DropZone.LAST).resizeWidthOfWidget(ResizeBullet.THREE)
                .changeDashboardTitle(DASHBOARD_TEST).saveEditModeWithWidgets();
        stageNameInsight = indigoDashboardsPage.getWidgetByHeadline(Insight.class, STAGE_NAME_INSIGHT).getPivotTableReport();
        isWonInsight = indigoDashboardsPage.getWidgetByHeadline(Insight.class, IS_WON_INSIGHT).getPivotTableReport();
        accountInsight = indigoDashboardsPage.getWidgetByHeadline(Insight.class, ACCOUNT_INSIGHT).getPivotTableReport();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"editMode"})
    public void checkConnectionBetweenAttributeFilters() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TEST).switchToEditMode().addAttributeFilter(ATTR_ACCOUNT);
        AttributeFilter accountFilter = indigoDashboardsPage.getAttributeFiltersPanel().getLastFilter();
        assertFalse(accountFilter.isConfigurationDisplay(), "Shouldn't display configuration with one attribute filter");

        indigoDashboardsPage.addAttributeFilter(ATTR_IS_WON).addAttributeFilter(ATTR_ACTIVITY)
                .addAttributeFilter(ATTR_STAGE_NAME);
        AttributeFilter stageNameFilter = indigoDashboardsPage.getAttributeFiltersPanel().getLastFilter();
        stageNameFilter.setDependentFilter(ATTR_IS_WON) //1a case1
                .setDependentFilter(ATTR_ACCOUNT, ATTR_STAGE_HISTORY); //1a case2
        assertFalse(stageNameFilter.getAttributeFilterConfiguration().isItemEnabled(ATTR_ACTIVITY),
                ATTR_PRODUCT + " shouldn't be filtered by " + ATTR_ACTIVITY + " due to no connection"); //1a case3
        stageNameFilter.apply(); //1b case 1 2 3 4
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_IS_WON).getSelectedItems(), "All");
        assertEquals(stageNameFilter.getSelectedItems(), "All");

        accountFilter.ensureDropdownOpen();
        assertFalse(accountFilter.getAttributeFilterConfiguration().isItemEnabled(ATTR_STAGE_NAME),
                "Stage Name should be disable due to being filtered by Account");
        accountFilter.ensureDropdownClosed();
        AttributeFilter isWonFilter = indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_IS_WON);
        isWonFilter.ensureDropdownOpen();
        isWonFilter.setDependentFilter(ATTR_ACCOUNT, ATTR_STAGE_HISTORY).apply();
        assertEquals(stageNameFilter.getSelectedItems(), "All");
        assertEquals(accountFilter.getSelectedItems(), "All");
        assertEquals(isWonFilter.getSelectedItems(), "All");
        assertEquals(indigoDashboardsPage.saveEditModeWithWidgets().getWidgetByHeadline(Insight.class, IS_WON_INSIGHT)
                .getPivotTableReport().getAttributeValuePresent(), asList("false", "true"));
    }

    @Test(dependsOnMethods = {"checkConnectionBetweenAttributeFilters"}, groups = {"editMode"})
    public void applyDependenceFilterOnEditMode() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TEST).switchToEditMode();
        AttributeFiltersPanel attributeFilterPanel = indigoDashboardsPage.getAttributeFiltersPanel();
        AttributeFilter accountFilter = attributeFilterPanel.getAttributeFilter(ATTR_ACCOUNT);
        AttributeFilter stageNameFilter = attributeFilterPanel.getAttributeFilter(ATTR_STAGE_NAME);
        AttributeFilter isWonFilter = attributeFilterPanel.getAttributeFilter(ATTR_IS_WON);

        accountFilter.clearAllCheckedValues().selectByNames("101 Financial", "123 Exteriors", "14 West");
        stageNameFilter.waitForLoading();
        isWonFilter.waitForLoading();
        assertEquals(stageNameFilter.getSelectedItems(), "All");
        assertEquals(isWonFilter.getSelectedItems(), "All");
        indigoDashboardsPage.waitForWidgetsLoading();
        assertEquals(stageNameInsight.getAttributeValuePresent(), asList("Interest", "Discovery", "Short List", "Risk Assessment",
                "Conviction", "Negotiation", "Closed Won", "Closed Lost"));

        assertThat(isWonFilter.getValues(), hasItems("false", "true"));
        assertThat(isWonFilter.getFilterInnerMessage(), hasItem(ATTR_ACCOUNT));
        isWonFilter.apply();
        assertThat(stageNameFilter.getValues(), hasItems("Risk Assessment", "Closed Won", "Closed Lost"));
        stageNameFilter.ensureDropdownClosed();
        isWonFilter.selectByNames("true"); //Except true

        assertThat(stageNameFilter.getValues(), hasItems("Risk Assessment", "Closed Lost"));
        assertThat(stageNameFilter.getFilterInnerMessage(), hasItems(ATTR_ACCOUNT, ATTR_IS_WON));

        stageNameFilter.selectByNames("Closed Lost"); //Except Closed Won
        indigoDashboardsPage.waitForWidgetsLoading();
        assertEquals(stageNameFilter.getSelectedItems(), "Risk Assessment");
        assertEquals(accountFilter.getSelectedItems(), "101 Financial, 123 Exteriors, 14 West");
        assertEquals(isWonFilter.getSelectedItems(), "false");
        assertThat(stageNameInsight.getAttributeValuePresent(), hasItems("Interest", "Discovery", "Short List", "Risk Assessment",
                "Conviction", "Negotiation"));
        assertThat(isWonInsight.getAttributeValuePresent(), hasItems("false"));
        assertThat(accountInsight.getAttributeValuePresent(), hasItems("101 Financial", "123 Exteriors", "14 West"));
    }

    @Test(dependsOnMethods = {"checkConnectionBetweenAttributeFilters"}, groups = {"editMode"})
    public void removeParentFilter() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TEST)
                .switchToEditMode().addAttributeFilter(ATTR_PRODUCT, "TouchAll");

        AttributeFiltersPanel attributeFilterPanel = indigoDashboardsPage.waitForWidgetsLoading().getAttributeFiltersPanel();
        AttributeFilter accountFilter = attributeFilterPanel.getAttributeFilter(ATTR_ACCOUNT);
        AttributeFilter stageNameFilter = attributeFilterPanel.getAttributeFilter(ATTR_STAGE_NAME);

        accountFilter.ensureDropdownOpen();
        accountFilter.setDependentFilter(ATTR_PRODUCT, ATTR_STAGE_HISTORY).apply();
        indigoDashboardsPage.waitForWidgetsLoading();
        assertEquals(accountFilter.getFilterMessage(), "All items are filtered out");
        assertEquals(accountFilter.getSelectedItems(), "All");
        assertEquals(accountInsight.getAttributeValuePresent().size(), 22);
        indigoDashboardsPage.deleteAttributeFilter(ATTR_PRODUCT);
        assertEquals(accountFilter.waitForLoading().getSelectedItems(), "All");

        //cover rail-3001 elements of children filter are reloaded after removing its parent
        accountFilter.clearAllCheckedValues().selectByNames("1000Bulbs.com", "101 Financial", "123 Exteriors").waitForLoading();
        assertThat(stageNameFilter.getValues(), hasItems("Risk Assessment", "Closed Lost"));
        stageNameFilter.clearAllCheckedValues().selectByNames("Risk Assessment").waitForLoading();
        indigoDashboardsPage.deleteAttributeFilter(ATTR_ACCOUNT).waitForWidgetsLoading();
        assertThat(attributeFilterPanel.getAttributeFilter(ATTR_STAGE_NAME).getValues(),
            hasItems("Interest", "Discovery", "Short List", "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost"));
        indigoDashboardsPage.cancelEditModeWithChanges();
    }

    @Test(dependsOnGroups = {"editMode"})
    public void applyDependenceFilterOnViewMode() {
        initIndigoDashboardsPage().switchToEditMode().addInsight(AMOUNT_AND_STAGE_NAME, Widget.DropZone.LAST);
        indigoDashboardsPage.addAttributeFilter(ATTR_DEPARTMENT, "Inside Sales").openHeaderOptionsButton().saveAs("Dashboard");
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading().selectDateFilterByName("All time");
        AttributeFiltersPanel attributeFilterPanel = indigoDashboardsPage.getAttributeFiltersPanel();
        AttributeFilter accountFilter = attributeFilterPanel.getAttributeFilter(ATTR_ACCOUNT);
        AttributeFilter stageNameFilter = attributeFilterPanel.getAttributeFilter(ATTR_STAGE_NAME);
        AttributeFilter isWonFilter = attributeFilterPanel.getAttributeFilter(ATTR_IS_WON);
        attributeFilterPanel.getAttributeFilter(ATTR_IS_WON);
        accountFilter.clearAllCheckedValues().selectByNames("1000Bulbs.com", "101 Financial", "123 Exteriors", "14 West");
        stageNameFilter.waitForLoading();
        isWonFilter.waitForLoading();
        isWonFilter.selectByNames("true"); //Expect true
        stageNameFilter.waitForLoading();
        stageNameFilter.selectByNames("Risk Assessment"); //Except Risk Assessment
        indigoDashboardsPage.waitForWidgetsLoading();
        PivotTableReport tableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, AMOUNT_AND_STAGE_NAME).getPivotTableReport();
        assertThat(tableReport.getBodyContent(), hasItems(asList("Closed Lost", "Inside Sales", "$18,000.00")));
        assertThat(tableReport.getHeaders(), hasItems("Stage Name", "Department", "Amount"));
    }

    @Test(dependsOnMethods = {"applyDependenceFilterOnViewMode"})
    public void exportOnViewMode() throws IOException {
        indigoDashboardsPage.exportDashboardToPDF();
        List<String> contents = asList(getContentFrom("Dashboard").split("\n"));
        assertThat(contents, not(hasItems("true", "Risk Assessment", "Direct Sales")));

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, AMOUNT_AND_STAGE_NAME).clickOnContent()
                .exportTo(OptionalExportMenu.File.CSV);
        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + AMOUNT_AND_STAGE_NAME + "." + ExportFormat.CSV.getName());
        waitForExporting(exportFile);
        assertEquals(CSVUtils.readCsvFile(exportFile), asList(asList("Stage Name", "Department", "Amount"),
                asList("Closed Lost", "Inside Sales", "18000")));

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, AMOUNT_AND_STAGE_NAME).clickOnContent()
                .exportTo(OptionalExportMenu.File.XLSX); //Have to init by not catching css on chrome
        ExportXLSXDialog.getInstance(browser).checkOption(ExportXLSXDialog.OptionalExport.CELL_MERGED)
                .uncheckOption(ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();
        final File xlsxFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + AMOUNT_AND_STAGE_NAME + "." + ExportFormat.EXCEL_XLSX.getName());
        waitForExporting(xlsxFile);
        assertEquals(XlsxUtils.excelFileToRead(xlsxFile.getPath(), 0), asList(asList("Stage Name", "Department", "Amount"),
                asList("Closed Lost", "Inside Sales", "18000.0")));
    }

    private void createInsight(String dashboardTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations, String... metricTitles) {
        InsightMDConfiguration insightMDConfiguration = new InsightMDConfiguration(dashboardTitle, ReportType.TABLE)
                .setCategoryBucket(attributeConfigurations.stream()
                        .map(attribute -> CategoryBucket.createCategoryBucket(
                                getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                        .collect(toList()));
        if ( metricTitles.length != 0 ) {
            insightMDConfiguration.setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricTitles[0]))));
        }

        indigoRestRequest.createInsight(insightMDConfiguration);
    }
}
