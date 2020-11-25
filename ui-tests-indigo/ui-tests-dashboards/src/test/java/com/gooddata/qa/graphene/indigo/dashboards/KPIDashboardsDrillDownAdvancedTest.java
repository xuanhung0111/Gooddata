package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.*;

public class KPIDashboardsDrillDownAdvancedTest extends AbstractDashboardTest {
    private final String TABLE_HAS_MEASURES_ROWS_COLUMNS = "Table has measure rows and columns";
    private final String TABLE_TWO_DRILLABLED_ATTRIBUTES = "Table with one attribute and two drillable attributes";
    private final String COLUMN_CHART_ONLY_MEASURES = "Column chart has only measures";
    private final String COLUMN_CHART_ONLY_VIEWBY_MEASURES = "Column chart only measures and viewby";
    private final String COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES = "Column chart with two drillable attribute";
    private final String TABLE_ONLY_MEASURES_COLUMNS = "Table has only measures";
    private final String TABLE_DRILL_INVALID = "Table has drill can't display";
    private final String COLUMN_DRILL_INVALID = "Column has drill can't display";

    private final String DASHBOARD_COLUMN_CHART_DRILL_CHART = "Chart drill chart";
    private final String DASHBOARD_TABLE_DRILL_TABLE = "Table drill table";
    private final String DASHBOARD_COLUMN_CHART_DRILL_TABLE = "Column drill table";
    private final String DASHBOARD_TABLE_DRILL_CHART = "Table drill chart";
    private final String DASHBOARD_COLUMN_CHART_DRILL_DASHBOARD = "Column drill dashboard";
    private final String DASHBOARD_TABLE_DRILL_DASHBOARD = "Table drill dashboard";
    private final String DASHBOARD_COLUMN_CHART_ONLY_VIEWBY = "Target column Viewby";
    private final String DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES = "An Target column";
    private final String DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES = "An target table";
    private final String DASHBOARD_TABLE_DRILL_INVALID = "Table drill invalid";
    private final String DASHBOARD_COLUMN_DRILL_INVALID = "Column drill invalid";

    private final String ATTRIBUTE_TITLE_TRUE = "true";
    private final String ATTRIBUTE_TITLE_FALSE = "false";
    private final String ATTRIBUTE_TITLE_DIRECT_SALES = "Direct Sales";
    private final String ATTRIBUTE_TITLE_ADAM_BRADLEY = "Adam Bradley";

    private IndigoRestRequest indigoRestRequest;
    ProjectRestRequest projectRestRequest;
    AttributeRestRequest attributeRestRequest;
    ConfigurationPanel configurationPanel;

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createSnapshotBOPMetric();
        metrics.createSnapshotEOPMetric();
        metrics.createTimelineEOPMetric();
        metrics.createTimelineBOPMetric();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        getMetricCreator().createBestCaseMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_TO_INSIGHT, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_TO_DASHBOARD, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_DOWN, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);

        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest.setDrillDown(ATTR_IS_WON, getAttributeDisplayFormUri(ATTR_DEPARTMENT));
        attributeRestRequest.setDrillDown(ATTR_DEPARTMENT, getAttributeDisplayFormUri(ATTR_PRODUCT));
        attributeRestRequest.setDrillDown(ATTR_SALES_REP, getAttributeDisplayFormUri(ATTR_STAGE_NAME));

        createInsight(TABLE_ONLY_MEASURES_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE),
            Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_HAS_MEASURES_ROWS_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE),
            Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_IS_ACTIVE, CategoryBucket.Type.COLUMNS)));
        createColumnChart(COLUMN_CHART_ONLY_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList());
        createColumnChart(COLUMN_CHART_ONLY_VIEWBY_MEASURES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_REGION, CategoryBucket.Type.STACK)));
        createInsight(TABLE_ONLY_MEASURES_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_DRILL_INVALID, asList(METRIC_SNAPSHOT_BOP),
            asList(Pair.of(ATTR_SALES_REP, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_PRIORITY, CategoryBucket.Type.COLUMNS)));
        createColumnChart(COLUMN_DRILL_INVALID, asList(METRIC_SNAPSHOT_BOP),
            asList(Pair.of(ATTR_SALES_REP, CategoryBucket.Type.VIEW), Pair.of(ATTR_PRIORITY, CategoryBucket.Type.STACK)));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @DataProvider(name = "prepareDashboardHasDrillToInsight")
    public Object[][] getDashboardHasDrillToInsight() {
        return new Object[][]{
            {
                DASHBOARD_TABLE_DRILL_CHART,TABLE_ONLY_MEASURES_COLUMNS, METRIC_AMOUNT, COLUMN_CHART_ONLY_VIEWBY_MEASURES },
            {
                DASHBOARD_TABLE_DRILL_TABLE,TABLE_ONLY_MEASURES_COLUMNS, METRIC_AMOUNT, TABLE_TWO_DRILLABLED_ATTRIBUTES },
            {
                DASHBOARD_COLUMN_CHART_DRILL_TABLE,COLUMN_CHART_ONLY_MEASURES, METRIC_AMOUNT, TABLE_HAS_MEASURES_ROWS_COLUMNS },
            {
                DASHBOARD_COLUMN_CHART_DRILL_CHART,COLUMN_CHART_ONLY_MEASURES, METRIC_AMOUNT, COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES },
        };
    }

    @DataProvider(name = "prepareDashboardHasDrillDown")
    public Object[][] getDashboardHasDrillDown() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES, COLUMN_CHART_ONLY_VIEWBY_MEASURES },
            {
                DASHBOARD_COLUMN_CHART_ONLY_VIEWBY, COLUMN_CHART_ONLY_VIEWBY_MEASURES },
            {
                DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES, TABLE_TWO_DRILLABLED_ATTRIBUTES },
        };
    }

    @DataProvider(name = "prepareDashboardHasDrillToDashboard")
    public Object[][] getDashboardHasDrillToDashboard() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_DRILL_DASHBOARD, COLUMN_CHART_ONLY_MEASURES, METRIC_AMOUNT, DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES },
            {
                DASHBOARD_TABLE_DRILL_DASHBOARD, TABLE_ONLY_MEASURES_COLUMNS, METRIC_AMOUNT, DASHBOARD_COLUMN_CHART_ONLY_VIEWBY },
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "prepareDashboardHasDrillToInsight")
    public void prepareDashboardWithConfigDrillToInsight(String sourceDashboard, String sourceInsight, String metricDrill, String targetInsight) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(sourceDashboard).addInsight(sourceInsight)
            .selectWidgetByHeadline(Insight.class, sourceInsight);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoInsight(metricDrill, targetInsight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "prepareDashboardHasDrillDown")
    public void prepareDashboardWithConfigDrillDown(String dashboard, String insight) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboard).addInsight(insight)
            .selectWidgetByHeadline(Insight.class, insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillDown", dataProvider = "prepareDashboardHasDrillToDashboard")
    public void prepareDashboardWithConfigDrillToDashboard(String sourceDashboard, String insight, String metricDrill, String targetDashboard) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(sourceDashboard).addInsight(insight)
            .selectWidgetByHeadline(Insight.class, insight);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoDashboard(metricDrill, targetDashboard);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToInsight",
        description = "This test case covered the drilling down on drill to insight with table that has one drillable attribute")
    public void drillDownOnColumnWithOneDrillableOnDrillToInsight() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TABLE_DRILL_CHART).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_ONLY_MEASURES_COLUMNS).getPivotTableReport();
        pivotTableReport.drillOnCellMeasure("$17,474,336.77");
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$17,474,336.77")));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillDownModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillDownModalDialog.getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetDownDrillReport = drillDownModalDialog.getChartReport();
        List<String> expectedDrillDownValuesModal = asList("$13,594,424.44", "$3,879,912.33");
        assertThat(targetDownDrillReport.getDataLabels(), equalTo(expectedDrillDownValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToInsight",
        description = "This test case covered the drilling down on drill to insight with table that has some drillable attributes")
    public void drillDownOnColumnWithTwoDrillableOnDrillToInsight() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_DRILL_CHART).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_MEASURES).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$17,474,336.77","$10,542,759.65","$60,840,366.32","$27,767,993.80")));
        assertThat(targetReport.getTotalsStackedColumn(), equalTo(asList("$78,314,703.09","$38,310,753.45")));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillDownModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillDownModalDialog.getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetDownDrillReport = drillDownModalDialog.getChartReport();
        List<String> expectedDrillDownValuesModal = asList("$53,901,464.88", "$24,413,238.21");
        assertThat(targetDownDrillReport.getTotalsStackedColumn(), equalTo(expectedDrillDownValuesModal));

        targetDownDrillReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog
            .getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueDrillReport = drillContinueModalDialog.getChartReport();
        List<String> expectedContinueDrillValuesModal = asList("$9,470,887.67", "$10,310,590.88", "$23,530,577.73", "$2,953,747.46","$3,470,688.72", "$4,164,972.42");
        assertThat(targetContinueDrillReport.getTotalsStackedColumn(), equalTo(expectedContinueDrillValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToInsight",
        description = "This test case covered the drilling down on drill to insight with table that has one drillable attribute")
    public void drillDownOnTableWithOneDrillableOnDrillToInsight() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_DRILL_TABLE).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_MEASURES).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_HAS_MEASURES_ROWS_COLUMNS);
        PivotTableReport targetReport = drillModalDialog.getPivotTableReport();
        assertThat(targetReport.getRowAttributeColumns(), equalTo(asList("false", "true")));

        targetReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_FALSE);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_HAS_MEASURES_ROWS_COLUMNS + " › " + ATTRIBUTE_TITLE_FALSE);
        PivotTableReport targetDrillReport = drillModalDialog.getPivotTableReport();
        List<String> expectedDrillValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetDrillReport.getRowAttributeColumns(), equalTo(expectedDrillValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToInsight",
        description = "This test case covered the drilling down on drill to insight with table that has some drillable attributes")
    public void drillDownOnTableWithTwoDrillableOnDrillToInsight() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TABLE_DRILL_TABLE).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_ONLY_MEASURES_COLUMNS).getPivotTableReport();
        pivotTableReport.drillOnCellMeasure("$17,474,336.77");
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES);
        PivotTableReport targetReport = drillModalDialog.getPivotTableReport();
        assertThat(targetReport.getRowAttributeColumns(), equalTo(asList("false")));

        targetReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_FALSE);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        PivotTableReport targetDrillReport = drillModalDialog.getPivotTableReport();
        List<String> expectedDrillValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetDrillReport.getRowAttributeColumns(), equalTo(expectedDrillValuesModal));

        DrillModalDialog drillContinueModalDialog = targetDrillReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueModalDialog
            .getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillReport = drillContinueModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToDashboard",
        description = "This test case covered the drilling down on drill to dashboard with table that has some drillable attributes")
    public void drillDownOnTableWithOneDrillableOnDrillToDashboard() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_DRILL_DASHBOARD).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_MEASURES).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        PivotTableReport targetReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_TWO_DRILLABLED_ATTRIBUTES).getPivotTableReport();
        assertThat(targetReport.getRowAttributeColumns(), equalTo(asList("false", "true")));

        DrillModalDialog drillModalDialog = targetReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_FALSE);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        PivotTableReport targetDrillReport = drillModalDialog.getPivotTableReport();
        List<String> expectedDrillValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetDrillReport.getRowAttributeColumns(), equalTo(expectedDrillValuesModal));

        DrillModalDialog drillContinueModalDialog = targetDrillReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueModalDialog
            .getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillReport = drillContinueModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToDashboard",
        description = "This test case covered the drilling down on drill to dashboard with column chart that has some drillable attributes")
    public void drillDownOnColumnWithOneDrillableOnDrillToDashboard() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TABLE_DRILL_DASHBOARD).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_ONLY_MEASURES_COLUMNS).getPivotTableReport();
        pivotTableReport.drillOnCellMeasure("$17,474,336.77");
        ChartReport targetReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_VIEWBY_MEASURES).getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$78,314,703.09", "$38,310,753.45")));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillDownModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillDownModalDialog.getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetDownDrillReport = drillDownModalDialog.getChartReport();
        List<String> expectedDrillDownValuesModal = asList("$53,901,464.88", "$24,413,238.21");
        assertThat(targetDownDrillReport.getDataLabels(), equalTo(expectedDrillDownValuesModal));

        targetDownDrillReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog
            .getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueDrillReport = drillContinueModalDialog.getChartReport();
        List<String> expectedContinueDrillValuesModal = asList("$9,470,887.67", "$10,310,590.88", "$23,530,577.73", "$2,953,747.46","$3,470,688.72", "$4,164,972.42");
        assertThat(targetContinueDrillReport.getDataLabels(), equalTo(expectedContinueDrillValuesModal));
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillDown",
        description = "This test case covered the drilling down chart on Embedded mode with preventDefault is false")
    public void drillDownOnChartExternalPreventDefaultFalse() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_VIEWBY_MEASURES).getChartReport();
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL, false).waitForWidgetsLoading();

        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillDownModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillDownModalDialog.getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetDownDrillReport = drillDownModalDialog.getChartReport();
        List<String> expectedDrillDownValuesModal = asList("$53,901,464.88", "$24,413,238.21");
        assertThat(targetDownDrillReport.getDataLabels(), equalTo(expectedDrillDownValuesModal));
    }

    @Test(dependsOnMethods = "drillDownOnChartExternalPreventDefaultFalse",
        description = "This test case covered the drilling down chart on Embedded mode with preventDefault is true")
    public void drillDownOnChartExternalPreventDefaultTrue() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_VIEWBY_MEASURES).getChartReport();
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL, true).waitForWidgetsLoading();
        chartReport.clickOnElement(Pair.of(0, 0));
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Should not allow to drilldown with chart when preventDefault is true.");

        indigoDashboardsPage.switchToEditMode().changeDashboardTitle("Target column").saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = "drillDownOnChartExternalPreventDefaultTrue",
        description = "This test case covered the drilling down table on Embedded mode with preventDefault is false")
    public void drillDownOnTableExternalPreventDefaultFalse() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_TWO_DRILLABLED_ATTRIBUTES).getPivotTableReport();
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL, false).waitForWidgetsLoading();

        pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_FALSE);
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        PivotTableReport targetDrillReport = drillModalDialog.getPivotTableReport();
        List<String> expectedDrillValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetDrillReport.getRowAttributeColumns(), equalTo(expectedDrillValuesModal));

        DrillModalDialog drillContinueModalDialog = targetDrillReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueModalDialog
            .getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillReport = drillContinueModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesModal));
    }

    @Test(dependsOnMethods = "drillDownOnTableExternalPreventDefaultFalse",
        description = "This test case covered the drilling down table on Embedded mode with preventDefault is true")
    public void drillDownOnTableExternalPreventDefaultTrue() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading();
        PivotTableReport pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, TABLE_TWO_DRILLABLED_ATTRIBUTES).getPivotTableReport();
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL, true).waitForWidgetsLoading();

        List<String> expectedValues = asList("false", "true");
        assertThat(pivotTableReport.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedValues));
        assertFalse(pivotTableReport.isCellUnderlinedByElement(pivotTableReport
            .getFirstCellOfRowAttribute(ATTRIBUTE_TITLE_TRUE)),"Should not allow to drill down with table when preventDefault is true.");
    }

    @Test(dependsOnGroups = {"createProject"},
        description = "This test case covered the drilling down on table with message SORRY, WE CAN'T DISPLAY THIS INSIGHT.")
    public void drillDownOnTableWithInvalidDrill() {
        initIndigoDashboardsPage().addDashboard().addInsight(TABLE_DRILL_INVALID);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TABLE_DRILL_INVALID).waitForWidgetsLoading().saveEditModeWithWidgets();

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        DrillModalDialog drillModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_ADAM_BRADLEY);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_DRILL_INVALID + " › " + ATTRIBUTE_TITLE_ADAM_BRADLEY);
        String cannotDisplayMessage = drillModalDialog.getCannotDisplayMessage();
        assertEquals(cannotDisplayMessage, "SORRY, WE CAN'T DISPLAY THIS INSIGHT");
        String contactAdminMessage = drillModalDialog.getContactAdminMessage();
        assertEquals(contactAdminMessage, "Contact your administrator.");
    }

    @Test(dependsOnGroups = {"createProject"},
        description = "This test case covered the drilling down on chart with message SORRY, WE CAN'T DISPLAY THIS INSIGHT.")
    public void drillDownOnAreaChartWithInvalidDrill() {
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_DRILL_INVALID);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_COLUMN_DRILL_INVALID).waitForWidgetsLoading().saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_DRILL_INVALID + " › " + ATTRIBUTE_TITLE_ADAM_BRADLEY);
        String cannotDisplayMessage = drillModalDialog.getCannotDisplayMessage();
        assertEquals(cannotDisplayMessage, "SORRY, WE CAN'T DISPLAY THIS INSIGHT");
        String contactAdminMessage = drillModalDialog.getContactAdminMessage();
        assertEquals(contactAdminMessage, "Contact your administrator.");
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

    private String createColumnChart(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.COLUMN_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }
}
