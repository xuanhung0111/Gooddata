package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.ColorPaletteRequestData;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.sdk.model.md.AttributeElement;
import com.gooddata.sdk.model.md.Fact;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.X_AXIS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.LEGEND;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
    .LogicalOperator.GREATER_THAN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.SP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette.CYAN;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BulletChartTest extends AbstractAnalyseTest {

    private static final String BULLET_CHART = "Bullet Chart";
    private static final String SPECIAL_BULLET_CHART = "Special Bullet Chart";
    private static final String BULLET_CHART_TEST_ON_KD = "Bullet Chart on KD";
    private static final String BULLET_CHART_TEST_SOME_ACTIONS = "Bullet Chart Test Some Actions";
    private static final String RENAMED_BULLET_CHART = "Renamed Bullet Chart";
    private static final String INSIGHT_RESTRICTED_FACT_TEST = "Restricted";
    private static final String INSIGHT_PROTECTED_ATTRIBUTE_TEST = "Protected";
    private static final String INSIGHT_MASKING_ELEMENT_TEST = "Masking";
    private static final String MASKING_METRIC = "Masking Metric";
    private static final String SAVED_AS_BULLET_CHART = "Saved As Bullet Chart";

    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private AttributeRestRequest attributeRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Updated AD filter Insight Value";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAmountBOPMetric();
        metrics.createBestCaseMetric();

        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        // TODO: BB-1675 enableNewADFilterBar FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_MEASURE_VALUE_FILTERS, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createValidBulletChart() {
        initAnalysePage().changeReportType(ReportType.BULLET_CHART).addMetric(METRIC_AMOUNT_BOP)
            .addMetricToSecondaryBucket(METRIC_AMOUNT).addMetricToTertiaryBucket(METRIC_BEST_CASE)
            .addAttribute(ATTR_DEPARTMENT).addAttribute(ATTR_FORECAST_CATEGORY).saveInsight(BULLET_CHART)
            .waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Direct Sales", "Include", "Exclude",
            "Inside Sales", "Include"));

        assertThat(chartReport.getYaxisLabels(), hasItems("0", "50M"));

        assertThat(chartReport.getLegends(), hasItems(METRIC_AMOUNT_BOP, METRIC_AMOUNT, METRIC_BEST_CASE));

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList(METRIC_AMOUNT_BOP, "$2,402,313.29")));

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 1),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList(METRIC_BEST_CASE, "20,457,990")));

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 5),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList(METRIC_AMOUNT, "$46,843,842.45")));
    }

    @Test(dependsOnMethods = {"createValidBulletChart"})
    public void createInvalidBulletChart() {
        initAnalysePage().changeReportType(ReportType.BULLET_CHART).addMetricToTertiaryBucket(METRIC_BEST_CASE)
            .waitForReportComputing();
        assertThat(analysisPage.getMainEditor().getCanvasMessage(),
            containsString("NO PRIMARY MEASURE IN YOUR INSIGHT"));
    }

    @Test(dependsOnMethods = {"createInvalidBulletChart"})
    public void createSpecialBulletChart() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(SPECIAL_BULLET_CHART, ReportType.BULLET_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_OPP_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        assertThat(initAnalysePage().openInsight(SPECIAL_BULLET_CHART).getMainEditor().getCanvasMessage(),
            containsString("TOO MANY DATA POINTS TO DISPLAY"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testChartIsAppliedConfiguration() {
        ConfigurationPanelBucket configurationPanelBucket = initAnalysePage().openInsight(BULLET_CHART)
            .openConfigurationPanelBucket();

        configurationPanelBucket.openColorConfiguration().openColorsPaletteDialog(CYAN.toCssFormatString())
            .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
            .setColorCustomPicker(ColorPaletteRequestData.ColorPalette.RED.getHexColor()).apply();

        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 1),
            ColorPaletteRequestData.ColorPalette.RED.toString());

        configurationPanelBucket.getItemConfiguration(X_AXIS.toString()).expandConfiguration().switchOff();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getYaxisLabels(), Collections.EMPTY_LIST);

        configurationPanelBucket.getItemConfiguration(Y_AXIS.toString()).expandConfiguration().switchOff();
        assertEquals(chartReport.getXaxisLabels(), Collections.EMPTY_LIST);

        configurationPanelBucket.getItemConfiguration(LEGEND.toString()).expandConfiguration().switchOff();
        assertEquals(analysisPage.getChartReport().getLegends(), Collections.EMPTY_LIST);
    }

    @Test(dependsOnMethods = {"testChartIsAppliedConfiguration"})
    public void testChartIsAppliedFilters() {
        initAnalysePage().openInsight(BULLET_CHART).addFilter(ATTR_DEPARTMENT).getFilterBuckets()
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT_BOP).expandConfiguration()
            .addFilterBySelectOnly(ATTR_FORECAST_CATEGORY, "Exclude");
        analysisPage.openFilterBarPicker().checkItem(METRIC_BEST_CASE, 3).apply();
        analysisPage.openMeasureFilterPanel(METRIC_BEST_CASE, 3)
            .addMeasureValueFilter(GREATER_THAN, "10000000");

        assertEquals(analysisPage.waitForReportComputing().getChartReport()
            .getTooltipTextOnTrackerByIndex(2, 0), asList(asList(ATTR_DEPARTMENT, "Direct Sales"),
            asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_BEST_CASE, "20,457,990")));
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);
    }

    @Test(dependsOnMethods = {"testChartIsAppliedFilters"})
    public void testChartIsAppliedCompareToSamePreviousPeriodYear() throws NoSuchFieldException {
        initAnalysePage().openInsight(BULLET_CHART).addDateFilter().waitForReportComputing();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
            .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames(METRIC_AMOUNT_BOP).apply();
        dateFilterPickerPanel.apply();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        MetricsBucket metricsTertiaryBucket = analysisPage.getMetricsTertiaryBucket();
        ChartReport chartReport = analysisPage.getChartReport();

        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT_BOP));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsTertiaryBucket.getItemNames(), singletonList(METRIC_AMOUNT_BOP + SP_YEAR_AGO));
        assertEquals(chartReport.getTrackersCount(), 12);

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT_BOP,
            METRIC_AMOUNT_BOP + SP_YEAR_AGO, METRIC_AMOUNT, METRIC_BEST_CASE));
        assertEquals(analysisPage.getPivotTableReport().getHeadersMeasure(), asList(METRIC_AMOUNT_BOP,
            METRIC_AMOUNT_BOP + SP_YEAR_AGO, METRIC_AMOUNT, METRIC_BEST_CASE));
    }

    @Test(dependsOnMethods = {"testChartIsAppliedCompareToSamePreviousPeriodYear"})
    public void testChartIsAppliedCompareToPreviousPeriodYear() {
        initAnalysePage().openInsight(BULLET_CHART).addDateFilter().waitForReportComputing();

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDateFilterPickerPanel()
            .configTimeFilterByRangeHelper("1/1/2000", "1/1/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.PREVIOUS_PERIOD);
        dateFilterPickerPanel.apply();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        MetricsBucket metricsTertiaryBucket = analysisPage.getMetricsTertiaryBucket();

        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT_BOP));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsTertiaryBucket.getItemNames(), singletonList(METRIC_AMOUNT_BOP + " - period ago"));

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT_BOP, METRIC_AMOUNT_BOP + " - period ago",
            METRIC_AMOUNT, METRIC_AMOUNT + " - period ago", METRIC_BEST_CASE, METRIC_BEST_CASE + " - period ago"));

        assertEquals(analysisPage.getPivotTableReport().getHeadersMeasure(), asList(METRIC_AMOUNT_BOP,
            METRIC_AMOUNT_BOP + " - period ago", METRIC_AMOUNT, METRIC_AMOUNT + " - period ago", METRIC_BEST_CASE,
            METRIC_BEST_CASE + " - period ago"));
    }

    @Test(dependsOnMethods = {"testChartIsAppliedCompareToPreviousPeriodYear"})
    public void testChartIsAppliedCalculatedMeasure() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.BULLET_CHART)
            .addMetric(METRIC_AMOUNT).addMetricToSecondaryBucket(METRIC_AMOUNT_BOP);
        MetricConfiguration metricConfiguration = analysisPage.getMetricsTertiaryBucket().createCalculatedMeasure()
            .getMetricConfiguration("Ratio of …");
        metricConfiguration.chooseArithmeticMeasureA(METRIC_AMOUNT, 1);
        metricConfiguration.chooseArithmeticMeasureB(METRIC_AMOUNT, 1);
        metricConfiguration.chooseOperator(MetricConfiguration.OperatorCalculated.SUM);
        analysisPage.removeSecondaryMetric(METRIC_AMOUNT_BOP).waitForReportComputing();

        FiltersBucket filterBucket = analysisPage.addDateFilter().getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel
            .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames("Sum of Amount and Amount").apply();
        dateFilterPickerPanel.apply();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        MetricsBucket metricsTertiaryBucket = analysisPage.getMetricsTertiaryBucket();

        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricsSecondaryBucket.getItemNames(), singletonList("Sum of Amount and Amount - SP year ago"));
        assertEquals(metricsTertiaryBucket.getItemNames(), singletonList("Sum of Amount and Amount"));
    }

    @Test(dependsOnMethods = {"testChartIsAppliedCalculatedMeasure"})
    public void testChartIsAppliedRestrictedFact() throws IOException{
        FactRestRequest factRestRequest = new FactRestRequest(getAdminRestClient(), testParams.getProjectId());
        try {
            factRestRequest.setFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());

            initAnalysePage().changeReportType(ReportType.BULLET_CHART).addMetric(FACT_AMOUNT, FieldType.FACT)
                .addAttribute(ATTR_DEPARTMENT).saveInsight(INSIGHT_RESTRICTED_FACT_TEST)
                .waitForReportComputing();

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            initAnalysePage().openInsight(INSIGHT_RESTRICTED_FACT_TEST)
                .waitForReportComputing();

            analysisPage.exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).confirmExport();
            assertEquals(ElementUtils.getErrorMessage(browser), "You cannot export this insight because it contains restricted data.");

        } finally {
            factRestRequest.unsetFactRestricted(getFactByTitle(FACT_AMOUNT).getUri());
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"testChartIsAppliedRestrictedFact"})
    public void testChartIsAppliedProtectedAttribute() throws IOException{
        AttributeRestRequest attributeRestRequest = new AttributeRestRequest(
            new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String attributeUri = attributeRestRequest.getAttributeByTitle(ATTR_DEPARTMENT).getUri();
        try {
            attributeRestRequest.setAttributeProtected(attributeUri);

            initAnalysePage().changeReportType(ReportType.BULLET_CHART).addMetric(FACT_AMOUNT, FieldType.FACT)
                .addAttribute(ATTR_DEPARTMENT).saveInsight(INSIGHT_PROTECTED_ATTRIBUTE_TEST)
                .waitForReportComputing();

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            initAnalysePage().openInsight(INSIGHT_PROTECTED_ATTRIBUTE_TEST)
                .waitForReportComputing();
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" +
                "Contact your administrator.");

        } finally {
            attributeRestRequest.unsetAttributeProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"testChartIsAppliedProtectedAttribute"})
    public void testChartIsAppliedMaskingElement() {
        List<String> attrEleUris = getMdService().getAttributeElements(getAttributeByTitle(ATTR_STAGE_NAME)).stream()
            .map(AttributeElement::getUri)
            .collect(Collectors.toList());

        createMetric(MASKING_METRIC, format(
            "SELECT SUM([%s]) WHERE [%s] IN ([%s], [%s], [%s])",
            getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)),
            getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
            attrEleUris.get(0), attrEleUris.get(1), attrEleUris.get(2)), DEFAULT_CURRENCY_METRIC_FORMAT);

        attributeRestRequest.setElementMaskingForAttribute(getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
            getMetricByTitle(MASKING_METRIC).getUri(), AttributeRestRequest.MaskNames.UNDISCLOSED, "Masking of the Stage Name");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_MASKING_ELEMENT_TEST, ReportType.BULLET_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_STAGE_NAME),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(INSIGHT_MASKING_ELEMENT_TEST).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getXaxisLabels(), asList("Interest", "Discovery", "Short List",
            "UNDISCLOSED", "UNDISCLOSED", "UNDISCLOSED", "UNDISCLOSED", "UNDISCLOSED"));
        assertEquals(chartReport.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSomeActionsWithBulletChart() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(BULLET_CHART_TEST_SOME_ACTIONS, ReportType.BULLET_CHART)
                .setMeasureBucket(asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_BEST_CASE), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.VIEW),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(BULLET_CHART_TEST_SOME_ACTIONS).saveInsightAs(SAVED_AS_BULLET_CHART).waitForReportComputing();
        analysisPage.getPageHeader().expandInsightSelection().deleteInsight(SAVED_AS_BULLET_CHART);
        assertFalse(analysisPage.getPageHeader().expandInsightSelection().isExist(SAVED_AS_BULLET_CHART),
            "Insight should be deleted");

        analysisPage.openInsight(BULLET_CHART).waitForReportComputing();

        analysisPage.replaceAttribute(ATTR_FORECAST_CATEGORY, ATTR_REGION).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_REGION, "West Coast"),
                asList(METRIC_AMOUNT_BOP, "$2,081,051.63")));

        analysisPage.undo();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList(METRIC_AMOUNT_BOP, "$2,402,313.29")));

        analysisPage.redo();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_REGION, "West Coast"),
                asList(METRIC_AMOUNT_BOP, "$2,081,051.63")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void placeBulletChartOnKD() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(BULLET_CHART_TEST_ON_KD, ReportType.BULLET_CHART)
                .setMeasureBucket(asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_BEST_CASE), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.VIEW),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.VIEW))));

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addInsight(BULLET_CHART_TEST_ON_KD)
            .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        indigoDashboardsPage.resizeWidthOfWidget(ResizeBullet.TEN);
        assertTrue(indigoDashboardsPage.getWidgetFluidLayout(BULLET_CHART_TEST_ON_KD)
                .getAttribute("class").contains("s-fluid-layout-column-width-" + ResizeBullet.TEN.getNumber()),
            "The width widget should be resized");

        Widget widget = indigoDashboardsPage.getWidgetByHeadline(Insight.class, BULLET_CHART_TEST_ON_KD);
        widget.setHeadline(RENAMED_BULLET_CHART);

        ConfigurationPanel configurationPanel =  indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.drillIntoInsight(METRIC_BEST_CASE, BULLET_CHART_TEST_ON_KD);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();

        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, RENAMED_BULLET_CHART).getChartReport();
        chartReport.clickOnElement(Pair.of(2, 1));
        indigoDashboardsPage.waitForDrillModalDialogLoading();

        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        ChartReport drillChartReport = drillModalDialog.getChartReport();

        assertEquals(drillChartReport.getXaxisLabels(), asList("Include", "Direct Sales"));
        assertEquals(drillChartReport.getTrackersCount(), 3);
        assertEquals(drillModalDialog.getTitleInsight(), BULLET_CHART_TEST_ON_KD);
        assertEquals(drillModalDialog.getTooltipTextOnTrackerByIndex(2, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(ATTR_FORECAST_CATEGORY, "Include"),
                asList(METRIC_BEST_CASE, "20,457,990")));
    }

    @Test(dependsOnMethods = {"placeBulletChartOnKD"})
    public void exportBulletChartToXLSX() throws IOException {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(BULLET_CHART_TEST_ON_KD).waitForWidgetsLoading();
        indigoDashboardsPage.saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.waitForWidgetsLoading().selectFirstWidget(Insight.class)
            .exportTo(OptionalExportMenu.File.XLSX);

        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.checkOption(ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT)
            .checkOption(ExportXLSXDialog.OptionalExport.CELL_MERGED).confirmExport();

        final java.io.File exportFile = new java.io.File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + BULLET_CHART_TEST_ON_KD + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info(BULLET_CHART_TEST_ON_KD + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertThat(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            hasItems(asList("Department", "Direct Sales", "Inside Sales"),
                asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include", "Exclude", "Include"),
                asList(METRIC_AMOUNT_BOP, "1441087.25", "2402313.29", "804499.13", "486497.98"),
                asList(METRIC_AMOUNT, "3.356248251E7", "4.684384245E7", "1.537015708E7", "2.08489745E7"),
                asList(METRIC_BEST_CASE, "8403394.34", "2.045798973E7", "3500178.24", "3482569.62")));
    }

    @Test(dependsOnMethods = {"placeBulletChartOnKD"})
    public void exportBulletChartToPDF() {
        String pdfFileName = generateHashString();
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(BULLET_CHART_TEST_ON_KD).changeDashboardTitle(pdfFileName).waitForWidgetsLoading();
        indigoDashboardsPage.saveEditModeWithWidgets().openExtendedDateFilterPanel()
            .selectPeriod(DateRange.ALL_TIME).apply();

        indigoDashboardsPage.waitForWidgetsLoading().exportDashboardToPDF();

        List<String> contents = asList(getContentFrom(pdfFileName).split("\n"));
        takeScreenshot(browser, pdfFileName, getClass());
        log.info(pdfFileName + contents.toString());

        assertThat(contents, hasItems(BULLET_CHART_TEST_ON_KD, METRIC_AMOUNT_BOP, METRIC_AMOUNT));
    }
}
