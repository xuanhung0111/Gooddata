package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class ColorPalettePickerBasicInsightAndKPITest extends AbstractAnalyseTest {
    private static final String INSIGHT_HAS_VIEW_BY = "Insight Has View By";
    private static final String INSIGHT_HAS_STACK_BY = "Insight Has Stack By";
    private static final String INSIGHT_FILTER_DATE_BY_SPPY = "Insight Filter Date By SPPY";
    private static final String INSIGHT_DATE_FILTERING_BY_SPPY_APPLY_CUSTOM_COLOR = "Insight Date Filtering By SPPY Apply Custom Color";
    private static final String INSIGHT_DATE_FILTERING_BY_SPPY_APPLY_COLOR_MAPPING = "Insight Date Filtering By SPPY Apply Color Mapping";
    private static final String APPLY_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY = "Apply Color Mapping Picker With Insight Has Stack By";
    private static final String RESET_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY = "Reset Color Mapping Picker With Insight Has Stack By";
    private static final String APPLY_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY = "Apply Custom Color Picker With Insight Has Stack By";
    private static final String RESET_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY = "Reset Custom Color Picker With Insight Has Stack By";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Color-Palette-Picker-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        metrics.createSnapshotBOPMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setCustomColorPickerFlag(false);
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsightsApplyColorsPalette() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).saveInsight(INSIGHT_HAS_VIEW_BY);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addStack(ATTR_ACTIVITY_TYPE).saveInsight(INSIGHT_HAS_STACK_BY);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter()
                .saveInsight(INSIGHT_FILTER_DATE_BY_SPPY);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testInsightFilterDateBySPPYApplyColorMappingPicker() {
        initAnalysePage().openInsight(INSIGHT_FILTER_DATE_BY_SPPY).waitForReportComputing()
                .getFilterBuckets().openDateFilterPickerPanel().configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        analysisPage.waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.BRIGHT_RED.toReportFormatString());
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.SOFT_RED.toString());
        assertEquals(analysisPage.getChartReport().checkColorColumn(1, 0), ColorPalette.BRIGHT_RED.toString());
        assertEquals(analysisPage.getChartReport().getLegendColors(), asList(ColorPalette.SOFT_RED.toString(), ColorPalette.BRIGHT_RED.toString()));
        analysisPage.saveInsightAs(INSIGHT_DATE_FILTERING_BY_SPPY_APPLY_COLOR_MAPPING);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testInsightFilterDateBySPPYApplyCustomColorPicker() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(INSIGHT_FILTER_DATE_BY_SPPY).waitForReportComputing()
                    .getFilterBuckets().openDateFilterPickerPanel().configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                    .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
            analysisPage.waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.RED.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.LIGHT_RED.toString());
            assertEquals(analysisPage.getChartReport().checkColorColumn(1, 0), ColorPalette.RED.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(), asList(ColorPalette.LIGHT_RED.toString(), ColorPalette.RED.toString()));
            analysisPage.saveInsightAs(INSIGHT_DATE_FILTERING_BY_SPPY_APPLY_CUSTOM_COLOR);
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testInsightApplyColorMappingPicker() {
        initAnalysePage().openInsight(INSIGHT_HAS_STACK_BY).waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPalette.BRIGHT_RED.toReportFormatString());
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.BRIGHT_RED.toString());
        assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                ColorPalette.BRIGHT_RED.toString(), ColorPalette.LIME_GREEN.toString(),
                ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
        analysisPage.saveInsightAs(APPLY_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testInsightApplyCustomColorPickerWithCancelButton() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(INSIGHT_HAS_STACK_BY).waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.RED.getHexColor()).cancel();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                    ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testInsightApplyCustomColorPickerWithOkButton() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(INSIGHT_HAS_STACK_BY).waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.RED.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(),asList(
                    ColorPalette.RED.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            analysisPage.saveInsightAs(APPLY_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY);
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void testSpecialInsightApplyColorPalette() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACCOUNT);
        assertEquals(analysisPage.checkColorItems(), "There are no colors for this configuration of the insight");
    }

    @Test(dependsOnMethods = {"testInsightApplyColorMappingPicker"})
    protected void resetColorMappingPickerOnAnalyze() {
        initAnalysePage().openInsight(APPLY_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY);
        ChartReport chartReport = analysisPage.resetColorPicker().getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        assertEquals(chartReport.getLegendColors(), asList(ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString(),
            ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
        analysisPage.saveInsightAs(RESET_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY);
    }

    @Test(dependsOnMethods = {"testInsightApplyCustomColorPickerWithOkButton"})
    protected void resetCustomColorPickerOnAnalyze() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(APPLY_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY);
            ChartReport chartReport = analysisPage.resetColorPicker().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.getLegendColors(), asList(ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            analysisPage.saveInsightAs(RESET_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY);
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    protected void resetInsightApplyColorPickerMappingSwitchingBetweenColumnChartAndPieChart() {
        initAnalysePage().openInsight(INSIGHT_HAS_VIEW_BY).waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPalette.BRIGHT_RED.toReportFormatString());
        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.BRIGHT_RED.toReportFormatString());
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration().resetColor();
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.BRIGHT_RED.toString());
        assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                ColorPalette.BRIGHT_RED.toString(), ColorPalette.LIME_GREEN.toString(),
                ColorPalette.CYAN.toString(), ColorPalette.PURE_ORANGE.toString()));
    }

    @Test(dependsOnMethods = {"resetInsightApplyColorPickerMappingSwitchingBetweenColumnChartAndPieChart"})
    protected void resetInsightApplyCustomColorPickerSwitchingBetweenColumnChartAndPieChart() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(INSIGHT_HAS_VIEW_BY).waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.RED.getHexColor()).apply();
            analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.RED.getHexColor()).apply();
            analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration().resetColor();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.BRIGHT_RED.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.CYAN.toString(), ColorPalette.PURE_ORANGE.toString()));
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"resetColorMappingPickerOnAnalyze"})
    protected void resetColorMappingPickerOnKPI() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addInsight(RESET_COLOR_MAPPING_PICKER_WITH_INSIGHT_HAS_STACK_BY)
                .waitForWidgetsLoading().selectDateFilterByName(DATE_FILTER_ALL_TIME).clickDashboardBody();
        assertEquals(indigoDashboardsPage.getColor(0), ColorPalette.CYAN.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(),asList(ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString(),
                ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
    }

    @Test(dependsOnMethods = {"resetCustomColorPickerOnAnalyze"})
    protected void resetCustomColorPickerOnKPI() {
    IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
    indigoDashboardsPage.addDashboard().addInsight(RESET_CUSTOM_COLOR_PICKER_WITH_INSIGHT_HAS_STACK_BY).waitForWidgetsLoading()
            .selectDateFilterByName(DATE_FILTER_ALL_TIME).clickDashboardBody();
    assertEquals(indigoDashboardsPage.getColor(0), ColorPalette.CYAN.toString());
    assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString(),
            ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
    }

    private void setCustomColorPickerFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }
}
