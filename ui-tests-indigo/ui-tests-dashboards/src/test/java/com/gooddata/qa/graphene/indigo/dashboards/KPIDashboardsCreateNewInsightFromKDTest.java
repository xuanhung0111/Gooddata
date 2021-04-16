package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillCustomUrlDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.LeaveConfirmNewInsightOnKDDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel.LogicalOperator.*;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.*;

public class KPIDashboardsCreateNewInsightFromKDTest extends AbstractDashboardTest {
    private String hyperlinkType = "GDC.link";
    private String dashboardID = "#dashboard_id";
    private final String UNDO_MESSAGE = "Undo button should be ";
    private final String REDO_MESSAGE = "Redo button should be ";
    private final String RESET_MESSAGE = "Reset button should be ";
    private final String CANCEL_MESSAGE = "Cancel button should be ";
    private final String COLUMN_INSIGHT = "Existing Insight";
    private final String DASHBOARD_HAS_TABLE = "Dashboard Table";
    private final String DASHBOARD_HAS_TABLE_EMBEDDED = "Dashboard Table Embedded";
    private final String PIVOT_TABLE = "Pivot Table";
    private final String PIVOT_TABLE_EMBEDDED = "Pivot Table Embedded";
    private final String DASHBOARD_HAS_INSIGHT = "Dashboard Insight";
    private final String DASHBOARD_WITHOUT_TITLE_TEST = "Without title";
    private final String DASHBOARD_WITH_TITLE_TEST = "With title";
    private final String COLUMN_CHART = "Column Chart";
    private final String PIVOT_TABLE_TEST_WITHOUT_NAME = "Table test without title";
    private final String PIVOT_TABLE_TEST_WITH_NAME = "Pivot test with title";
    private final String PIVOT_TABLE_RENAME = "Rename Table";
    private final String ADAM_BRADLEY = "Adam Bradley";

    private String today;
    private IndigoRestRequest indigoRestRequest;
    private ConfigurationPanel configurationPanel;
    ProjectRestRequest projectRestRequest;
    AttributeRestRequest attributeRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createAmountBOPMetric();
        getMetricCreator().createBestCaseMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_NEW_INSIGHT, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);

        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest.setDrillDown(ATTR_SALES_REP, getAttributeDisplayFormUri(ATTR_DEPARTMENT));
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_REGION, hyperlinkType);

        createInsight(COLUMN_INSIGHT, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the creating new table on KD")
    public void createNewTableOnKD() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_HAS_TABLE);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        AnalysisPage newTable = indigoDashboardsPage.addNewInsight();
        //Check enabled undo/redo/clear/cancel/save buttons
        assertFalse(newTable.isUndoInsightEnabled(), UNDO_MESSAGE + "disabled.");
        assertFalse(newTable.isRedoInsightEnabled(), REDO_MESSAGE + "disabled.");
        assertFalse(newTable.isResetInsightEnabled(), RESET_MESSAGE + "disabled.");
        assertTrue(newTable.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");

        newTable.addMetric(METRIC_AMOUNT).addAttribute(ATTR_SALES_REP).addFilter(ATTR_SALES_REP).getFilterBuckets().configAttributeFilter(ATTR_SALES_REP,
            "Adam Bradley", "Alejandro Vabiano", "Cory Owens", "Ellen Jones", "John Jovi");
        List<String> expectedAttributes = asList("Adam Bradley", "Alejandro Vabiano", "Cory Owens", "Ellen Jones", "John Jovi");
        List<String> expectedValues = asList("$4,108,360.80", "$2,267,528.48", "$2,376,100.41", "$3,759,893.44", "$4,584,127.47");
        assertEquals(newTable.getPivotTableReport().getValueMeasuresPresent(), expectedValues);
        assertEquals(waitForElementVisible(cssSelector(".s-global-attribute-filter-title .is-whole"), browser).getText(), ATTR_SALES_REP);
        assertEquals(waitForElementVisible(cssSelector(".s-attribute-filter-label .s-total-count"), browser).getText(), "(5)");

        newTable.saveInsight(PIVOT_TABLE);
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE));
        PivotTableReport pivotTableReportKD = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        assertThat(pivotTableReportKD.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedAttributes));
        assertThat(pivotTableReportKD.getValueMeasuresPresent(), equalTo(expectedValues));

        AnalysisPage newInsight = indigoDashboardsPage.addNewInsightToNextPosision();
        //Check enabled undo/redo/clear/cancel/save buttons
        assertFalse(newTable.isUndoInsightEnabled(), UNDO_MESSAGE + "disabled.");
        assertFalse(newTable.isRedoInsightEnabled(), REDO_MESSAGE + "disabled.");
        assertFalse(newTable.isResetInsightEnabled(), RESET_MESSAGE + "disabled.");
        assertTrue(newTable.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");

        newInsight.cancel();
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad().saveEditModeWithWidgets();
        assertThat(pivotTableReportKD.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedAttributes));
        assertThat(pivotTableReportKD.getValueMeasuresPresent(), equalTo(expectedValues));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the creating new column chart on KD")
    public void createNewInsightOnKD() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_HAS_INSIGHT);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "01/01/2015").apply();
        AnalysisPage newInsight = indigoDashboardsPage.addNewInsight();
        newInsight.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addAttribute(ATTR_SALES_REP)
            .openFilterBarPicker().checkItem(METRIC_AMOUNT, 1).apply();
        newInsight.openMeasureFilterPanel(METRIC_AMOUNT, 1).addMeasureValueFilter(LESS_THAN, "2,500,000");
        //Check enabled undo/redo/clear/cancel/save buttons
        assertTrue(newInsight.isUndoInsightEnabled(), UNDO_MESSAGE + "enabled.");
        assertFalse(newInsight.isRedoInsightEnabled(), REDO_MESSAGE + "disabled.");
        assertTrue(newInsight.isResetInsightEnabled(), RESET_MESSAGE + "enabled.");
        assertTrue(newInsight.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");
        ChartReport chartReport = newInsight.getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("$2,267,528.48", "$2,376,100.41", "$2,128,260.34"));

        newInsight.undo();
        //Check enabled undo/redo/clear/cancel/save buttons
        assertTrue(newInsight.isUndoInsightEnabled(), UNDO_MESSAGE + "enabled.");
        assertTrue(newInsight.isRedoInsightEnabled(), REDO_MESSAGE + "enabled.");
        assertTrue(newInsight.isResetInsightEnabled(), RESET_MESSAGE + "enabled.");
        assertTrue(newInsight.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");

        newInsight.redo().saveInsight(COLUMN_CHART);
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(COLUMN_CHART));
        ChartReport chartReportKD = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART).getChartReport();
        List<String> expectedTitles = asList("Alejandro Vabiano", "Cory Owens", "Dale Perdadtin", "Jessica Traven", "Jon Jons");
        List<String> expectedValues = asList("$1,970,990.59", "$2,056,706.91", "$1,883,787.74", "$1,717,284.40", "$2,429,123.51");
        assertThat(chartReportKD.getAxisLabels(), equalTo(expectedTitles));
        assertThat(chartReportKD.getDataLabels(), equalTo(expectedValues));
        indigoDashboardsPage.waitForDashboardLoad().saveEditModeWithWidgets();
        assertThat(chartReportKD.getAxisLabels(), equalTo(expectedTitles));
        assertThat(chartReportKD.getDataLabels(), equalTo(expectedValues));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the cancelling create new insight without insight name on KD")
    public void cancelCreatingNewInsightsWithoutInsightNameOnKD() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_WITHOUT_TITLE_TEST);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        AnalysisPage newTable = indigoDashboardsPage.addNewInsight();
        newTable.addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT).cancel();

        LeaveConfirmNewInsightOnKDDialog leaveConfirmDialog = LeaveConfirmNewInsightOnKDDialog.getInstance(browser);
        leaveConfirmDialog.clickCloseDialog();
        assertEquals(newTable.getPivotTableReport().getValueMeasuresPresent(), asList("$80,406,324.96", "$36,219,131.58"));

        newTable.cancel();
        leaveConfirmDialog.clickLeaveAnywayButton();

        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad();
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time");
        assertTrue(indigoDashboardsPage.getDashboardBodyText().contains("Drag insight here"), "Dashboard does not empty!!!");

        AnalysisPage newTableAgain = indigoDashboardsPage.addNewInsight();
        newTableAgain.addMetric(METRIC_AMOUNT).addMetric(METRIC_BEST_CASE).addMetric(METRIC_AMOUNT_BOP)
            .addAttribute(ATTR_DEPARTMENT).addAttribute(ATTR_REGION).cancel();
        assertEquals(leaveConfirmDialog.getDialogContent(), "If you leave now, all changes will be lost.");
        leaveConfirmDialog.clickSaveButton();

        SaveInsightDialog saveInsightDialog = SaveInsightDialog.getInstance(browser);
        assertTrue(saveInsightDialog.isSaveInsightDialogDisplay(), "Save insight dialog should be displayed");

        saveInsightDialog.save(PIVOT_TABLE_TEST_WITHOUT_NAME);
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE_TEST_WITHOUT_NAME));
        indigoDashboardsPage.getSaveButton().click();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE_TEST_WITHOUT_NAME));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the cancelling create new insight with insight name on KD")
    public void cancelCreatingNewInsightsWithInsightNameOnKD() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_WITH_TITLE_TEST);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        AnalysisPage newTable = indigoDashboardsPage.addNewInsight();
        newTable.addMetric(METRIC_AMOUNT).addAttribute(ATTR_SALES_REP).setInsightTitle(PIVOT_TABLE_TEST_WITH_NAME).cancel();

        LeaveConfirmNewInsightOnKDDialog leaveConfirmDialog = LeaveConfirmNewInsightOnKDDialog.getInstance(browser);
        leaveConfirmDialog.clickLeaveAnywayButton();

        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad();
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time");
        assertTrue(indigoDashboardsPage.getDashboardBodyText().contains("Drag insight here"), "Dashboard does not empty!!!");

        AnalysisPage newTableAgain = indigoDashboardsPage.addNewInsight();
        newTableAgain.addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).addAttribute(ATTR_SALES_REP)
            .setInsightTitle(PIVOT_TABLE_TEST_WITH_NAME).cancel();
        leaveConfirmDialog.clickSaveButton();

        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE_TEST_WITH_NAME));
        indigoDashboardsPage.getSaveButton().click();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE_TEST_WITH_NAME));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the editing KD after created new insight on KD")
    public void editKDAfterCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_WITHOUT_TITLE_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().getLastWidget(Insight.class).clickOnContent().setHeadline(PIVOT_TABLE_RENAME);
        configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, ATTR_REGION);
        indigoDashboardsPage.addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales")
            .openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "01/01/2014").apply();

        indigoDashboardsPage.addInsight(COLUMN_INSIGHT, Widget.DropZone.LAST).selectWidgetByHeadline(Insight.class, PIVOT_TABLE_RENAME);
        indigoDashboardsPage.saveEditModeWithWidgets();

        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_WITH_TITLE_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, PIVOT_TABLE_TEST_WITH_NAME);
        configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.drillIntoDashboard(METRIC_AMOUNT, DASHBOARD_HAS_INSIGHT);
        configurationPanel.drillIntoInsight(METRIC_AVG_AMOUNT, COLUMN_CHART );
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down after created new insight on KD")
    public void testDrillDownWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_HAS_TABLE).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        List<String> expectedTitles = asList("Adam Bradley", "Alejandro Vabiano", "Cory Owens", "Ellen Jones", "John Jovi");
        List<String> expectedValues = asList("$4,108,360.80", "$2,267,528.48", "$2,376,100.41", "$3,759,893.44", "$4,584,127.47");
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(expectedTitles));
        assertThat(pivotTableReport.getValueMeasuresPresent(), equalTo(expectedValues));

        DrillModalDialog drillModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ADAM_BRADLEY);
        assertEquals(drillModalDialog.getTitleInsight(), PIVOT_TABLE + " › " + ADAM_BRADLEY);

        PivotTableReport targetReport = drillModalDialog.getPivotTableReport();
        assertThat(targetReport.getRowAttributeColumns(), equalTo(asList("Direct Sales")));
        assertThat(targetReport.getValueMeasuresPresent(), equalTo(asList("$4,108,360.80")));

        drillModalDialog.close();
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(expectedTitles));
        assertThat(pivotTableReport.getValueMeasuresPresent(), equalTo(expectedValues));

        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_HAS_INSIGHT).waitForWidgetsLoading();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART).getChartReport();
        assertThat(chartReport.getAxisLabels(), equalTo(asList("Alejandro Vabiano", "Cory Owens", "Dale Perdadtin", "Jessica Traven", "Jon Jons")));
        assertThat(chartReport.getDataLabels(), equalTo(asList("$1,970,990.59", "$2,056,706.91", "$1,883,787.74", "$1,717,284.40", "$2,429,123.51")));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialogInsight = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialogInsight.getTitleInsight(), COLUMN_CHART + " › " + "Alejandro Vabiano");
        ChartReport targetInsightReport = drillModalDialogInsight.getChartReport();
        assertThat(targetInsightReport.getAxisLabels(), equalTo(asList("Direct Sales")));
        assertThat(targetInsightReport.getDataLabels(), equalTo(asList("$1,970,990.59")));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling to dashboard after created new insight on KD")
    public void testDrillToDashboardWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_WITH_TITLE_TEST).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        pivotTableReport.clickOnElement(METRIC_AMOUNT, 0, 0);

        indigoDashboardsPage.waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART).getChartReport();
        assertThat(chartReport.getAxisLabels(), equalTo(asList("Alejandro Vabiano", "Cory Owens", "Jessica Traven")));
        assertThat(chartReport.getDataLabels(), equalTo(asList("$2,267,528.48", "$2,376,100.41", "$2,128,260.34")));
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling into insight after created new insight on KD")
    public void testDrillIntoInsightWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_WITH_TITLE_TEST).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        pivotTableReport.clickOnElement(METRIC_AVG_AMOUNT, 0, 1);
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        ChartReport drillChartReport = drillModalDialog.getChartReport();

        assertEquals(drillChartReport.getYaxisTitle(), METRIC_AMOUNT);
        assertEquals(drillChartReport.getXaxisLabels(), asList("Alejandro Vabiano"));
        assertEquals(drillChartReport.getDataLabels(), asList("$2,267,528.48"));
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling to custom URL after created new insight on KD")
    public void testDrillToCustomURLWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_HAS_TABLE).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, PIVOT_TABLE);
        configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT);
        DrillCustomUrlDialog drillCustomUrlDialog = DrillCustomUrlDialog.getInstance(browser);
        String dashboardIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(dashboardID).toLowerCase();
        DrillCustomUrlDialog.getInstance(browser).addDashboardID().apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        pivotTableReport.clickOnElement(METRIC_AMOUNT, 0, 0);
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(dashboardIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url.");
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling to hyperlink after created new insight on KD")
    public void testDrillToHyperlinkWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_WITHOUT_TITLE_TEST).waitForWidgetsLoading();
            PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Insight.class, PIVOT_TABLE_RENAME).getPivotTableReport();
        pivotTableReport.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/East%20Coast");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url.");
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the exporting PDF after created new insight on KD")
    public void testExportKDWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_HAS_INSIGHT).exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(DASHBOARD_HAS_INSIGHT).split("\n"));
        log.info(DASHBOARD_HAS_INSIGHT + contents.toString());
        assertThat(contents, hasItems(COLUMN_CHART, "$1,970,990.59", "$2,056,706.91", "$1,883,787.74", "$1,717,284.40", "$2,429,123.51"));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the creating new insight on KD with embedded mode")
    public void testEmbeddedKDWithCreatingNewInsightsOnKD() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_HAS_INSIGHT).waitForWidgetsLoading();
        initEmbeddedIndigoDashboardWithShowNavigation(EmbeddedType.URL, true).waitForWidgetsLoading()
            .addDashboard().changeDashboardTitle(DASHBOARD_HAS_TABLE_EMBEDDED);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        AnalysisPage newTable = indigoDashboardsPage.addNewInsight();
        //Check enabled undo/redo/clear/cancel/save buttons
        assertFalse(newTable.isUndoInsightEnabled(), UNDO_MESSAGE + "disabled.");
        assertFalse(newTable.isRedoInsightEnabled(), REDO_MESSAGE + "disabled.");
        assertFalse(newTable.isResetInsightEnabled(), RESET_MESSAGE + "disabled.");
        assertTrue(newTable.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");

        newTable.addMetric(METRIC_AMOUNT).addAttribute(ATTR_SALES_REP).addFilter(ATTR_SALES_REP).getFilterBuckets().configAttributeFilter(ATTR_SALES_REP,
                "Adam Bradley", "Alejandro Vabiano", "Cory Owens", "Ellen Jones", "John Jovi");
        List<String> expectedAttributes = asList("Adam Bradley", "Alejandro Vabiano", "Cory Owens", "Ellen Jones", "John Jovi");
        List<String> expectedValues = asList("$4,108,360.80", "$2,267,528.48", "$2,376,100.41", "$3,759,893.44", "$4,584,127.47");
        assertEquals(newTable.getPivotTableReport().getValueMeasuresPresent(), expectedValues);
        assertEquals(waitForElementVisible(cssSelector(".s-global-attribute-filter-title .is-whole"), browser).getText(), ATTR_SALES_REP);
        assertEquals(waitForElementVisible(cssSelector(".s-attribute-filter-label .s-total-count"), browser).getText(), "(5)");

        newTable.saveInsight(PIVOT_TABLE_EMBEDDED);
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(PIVOT_TABLE_EMBEDDED));
        PivotTableReport pivotTableReportKD = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        assertThat(pivotTableReportKD.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedAttributes));
        assertThat(pivotTableReportKD.getValueMeasuresPresent(), equalTo(expectedValues));

        AnalysisPage newInsight = indigoDashboardsPage.addNewInsightToNextPosision();
        //Check enabled undo/redo/clear/cancel/save buttons
        assertFalse(newTable.isUndoInsightEnabled(), UNDO_MESSAGE + "disabled.");
        assertFalse(newTable.isRedoInsightEnabled(), REDO_MESSAGE + "disabled.");
        assertFalse(newTable.isResetInsightEnabled(), RESET_MESSAGE + "disabled.");
        assertTrue(newTable.isCancelInsightEnabled(), CANCEL_MESSAGE + "enabled.");

        newInsight.cancel();
        browser.switchTo().window(BrowserUtils.getWindowHandles(browser).get(BrowserUtils.getWindowHandles(browser).size() - 1));
        indigoDashboardsPage.waitForDashboardLoad().saveEditModeWithWidgets();
        assertThat(pivotTableReportKD.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedAttributes));
        assertThat(pivotTableReportKD.getValueMeasuresPresent(), equalTo(expectedValues));
    }

    private String createInsight(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.TABLE)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }
}
