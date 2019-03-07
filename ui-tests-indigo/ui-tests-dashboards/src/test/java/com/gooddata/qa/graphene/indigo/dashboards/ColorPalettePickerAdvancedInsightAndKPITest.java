package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import com.gooddata.qa.utils.http.ColorPaletteRequestData;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static java.util.Collections.singletonList;

public class ColorPalettePickerAdvancedInsightAndKPITest extends GoodSalesAbstractTest {

    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private final String DATE_FILTER_ALL_TIME = "All time";
    private final String KPI_DASHBOARD = "KPI_Dashboard" + generateHashString();
    private final String DEFAULT_INSIGHT = "Default Insight" + generateHashString();
    private final String NEW_INSIGHT = "New Insight" + generateHashString();
    private final String INSIGHT_APPLY_CUSTOM_COLOR_PICKER = "Insight Apply Custom Color Picker" + generateHashString();
    private final String INSIGHT_APPLY_COLOR_PICKER_MAPPING =
            "Insight Apply Color Picker Mapping" + generateHashString();
    private final String INSIGHT_TEST = "Insight Test" + generateHashString();
    private final String INSIGHT_TEST_SAVE_AS = "Insight Test Save As" + generateHashString();
    private final String IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT =
            "Import Export Partial Project With Insight" + generateHashString();
    private final String IMPORT_EXPORT_PROJECT_WITH_INSIGHT =
            "Import Export Project With Insight" + generateHashString();
    private final String INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES =
            "Insight Switch between insight types" + generateHashString();
    private static List<Pair<String, ColorPaletteRequestData.ColorPalette>> listColorPalettes = asList(
            Pair.of("guid1", ColorPaletteRequestData.ColorPalette.RED),
            Pair.of("guid2", ColorPaletteRequestData.ColorPalette.GREEN),
            Pair.of("guid3", ColorPaletteRequestData.ColorPalette.BLUE),
            Pair.of("guid4", ColorPaletteRequestData.ColorPalette.YELLOW));

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Color-Palette-Picker-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getMetricCreator().createSnapshotBOPMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setCustomColorPickerFlag(false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchBetweenColumnChartAndPieChartOnInsight() {
        try {
            setCustomColorPickerFlag(true);
            createInsightHasAttributeOnViewBy(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES,
                    METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ReportType.COLUMN_CHART);
            AnalysisPage analysisPage = initAnalysePage()
                    .openInsight(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES).waitForReportComputing();
            ChartReport chartReport = analysisPage.setCustomColorPicker(ColorPalette.YELLOW.getHexColor())
                    .applyCustomColorPicker().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
            setColorPickerMapping(analysisPage, 5);
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 2), ColorPalette.PURPLE.toString());
            chartReport = analysisPage.changeReportType(ReportType.COLUMN_CHART)
                    .waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            chartReport = analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 2), ColorPalette.PURPLE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"testSwitchBetweenColumnChartAndPieChartOnInsight"})
    public void testSwitchBetweenTreeMapChartAndHeadMapChartOnInsight() {
        try {
            setCustomColorPickerFlag(true);
            AnalysisPage analysisPage = initAnalysePage()
                    .openInsight(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES).waitForReportComputing()
                    .changeReportType(ReportType.TREE_MAP).waitForReportComputing();
            ChartReport chartReport = analysisPage.setCustomColorPicker(ColorPalette.YELLOW.getHexColor())
                    .applyCustomColorPicker().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            setColorPickerMapping(analysisPage, 4);
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 2), ColorPalette.PURE_ORANGE.toString());
            chartReport = analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            chartReport = analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 2), ColorPalette.PURE_ORANGE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightApplyPaletteColorPickerOnAttribute() {
        AnalysisPage analysisPage = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        setColorPickerMapping(analysisPage, 4);
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURE_ORANGE.toString());
        analysisPage.getStackConfigurationPanelBucket();
        analysisPage.addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        chartReport = analysisPage.removeStack().waitForReportComputing().getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURE_ORANGE.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightApplyPaletteColorPickerOnMeasure() {
        AnalysisPage analysisPage = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        analysisPage.getStackConfigurationPanelBucket();
        analysisPage.removeStack().waitForReportComputing();
        analysisPage.addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchBetweenXAxisMetricAndYAxisMetricOnInsight() {
        try {
            setCustomColorPickerFlag(true);
            AnalysisPage analysisPage = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
            analysisPage.changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
            ChartReport chartReport = analysisPage.setCustomColorPicker(ColorPalette.YELLOW.getHexColor())
                    .applyCustomColorPicker().getChartReport();
            assertEquals(chartReport.getColor(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.getMeasureConfigurationPanelBucket();
            analysisPage.reorderSecondaryMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
            setColorPickerMapping(analysisPage, 4);
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.getColor(0, 0), ColorPalette.PURE_ORANGE.toString());
            analysisPage.getMeasureConfigurationPanelBucket();
            analysisPage.reorderSecondaryMetric(METRIC_OPP_FIRST_SNAPSHOT, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
            assertEquals(chartReport.getColor(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.reorderSecondaryMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
            assertEquals(chartReport.getColor(0, 0), ColorPalette.PURE_ORANGE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOff() {
        try {
            setCustomColorPickerFlag(false);
            createInsightHasAttributeOnStackByAndViewBy(DEFAULT_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            ChartReport chartReport = initAnalysePage().openInsight(DEFAULT_INSIGHT)
                    .waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            createInsightHasAttributeOnStackByAndViewBy(INSIGHT_APPLY_COLOR_PICKER_MAPPING, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            AnalysisPage analysisPage = initAnalysePage()
                    .openInsight(INSIGHT_APPLY_COLOR_PICKER_MAPPING).waitForReportComputing();
            setColorPickerMapping(analysisPage, 5);
            analysisPage.saveInsight();
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
            setCustomColorPickerFlag(true);
            createInsightHasAttributeOnStackByAndViewBy(INSIGHT_APPLY_CUSTOM_COLOR_PICKER, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            chartReport = initAnalysePage().openInsight(INSIGHT_APPLY_CUSTOM_COLOR_PICKER).waitForReportComputing()
                    .setCustomColorPicker(ColorPalette.YELLOW.getHexColor()).applyCustomColorPicker().getChartReport();
            analysisPage.saveInsight();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOff"})
    public void testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOn() {
        try {
            createInsightHasAttributeOnStackByAndViewBy(NEW_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            applyColorPaletteOnGrayPage();
            browser.navigate().refresh();
            AnalysisPage analysisPage = initAnalysePage().openInsight(NEW_INSIGHT).waitForReportComputing();
            ChartReport chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
            chartReport = initAnalysePage().openInsight(INSIGHT_APPLY_COLOR_PICKER_MAPPING)
                    .waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
            chartReport = initAnalysePage().openInsight(INSIGHT_APPLY_CUSTOM_COLOR_PICKER)
                    .waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.GREEN.toString()));
        } finally {
            deleteColorPaletteOnGrayPage();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOpenAsReportButtonDisabledWhenAppliedPaletteColorOnInsight() {
        createInsightHasAttributeOnViewBy(INSIGHT_TEST, METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ReportType.COLUMN_CHART);
        AnalysisPage analysisPage = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        assertTrue(analysisPage.isDisableOpenAsReport(), "The insight is not compatible with custom colors");
    }

    @Test(dependsOnMethods = {"testOpenAsReportButtonDisabledWhenAppliedPaletteColorOnInsight"})
    public void testSaveAsInsightWhenAppliedPaletteColorOnInsight() {
        AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        analysisPage.saveInsightAs(INSIGHT_TEST_SAVE_AS);
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnMethods = {"testSaveAsInsightWhenAppliedPaletteColorOnInsight"})
    public void testActionsUndoAndRedoWhenAppliedPaletteColorOnInsight() {
        AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        analysisPage.undo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        analysisPage.redo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnMethods = {"testSaveAsInsightWhenAppliedPaletteColorOnInsight"})
    public void testActionReorderingWhenAppliedPaletteColorOnInsight() {
        AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        analysisPage.getMeasureConfigurationPanelBucket();
        ChartReport chartReport = analysisPage.reorderMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.getLegendColors(), asList(
                ColorPalette.CYAN.toString(), ColorPalette.PURPLE.toString()));
        analysisPage.getMeasureConfigurationPanelBucket();
        chartReport = analysisPage.reorderMetric(METRIC_OPP_FIRST_SNAPSHOT, METRIC_NUMBER_OF_ACTIVITIES).getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(
                ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
    }

    @Test(dependsOnMethods = {"testActionReorderingWhenAppliedPaletteColorOnInsight"})
    public void testActionClearWhenAppliedPaletteColorOnInsight() {
        AnalysisPage analysisPage = initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        analysisPage.getPageHeader().getResetButton();
        assertTrue(analysisPage.getPageHeader().isResetButtonEnabled(),
                "Insight has been cleared ,So can not apply Color Palette");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testApplyColorPalettePickerOnKpi() {
        createInsightHasAttributeOnStackByAndViewBy(IMPORT_EXPORT_PROJECT_WITH_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        AnalysisPage analysisPage = initAnalysePage().openInsight(IMPORT_EXPORT_PROJECT_WITH_INSIGHT).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        analysisPage.saveInsight();
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(IMPORT_EXPORT_PROJECT_WITH_INSIGHT).waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME).clickDashboardBody()
                .changeDashboardTitle(KPI_DASHBOARD)
                .saveEditModeWithWidgets();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(
                ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
    }

    @Test(dependsOnMethods = {"testApplyColorPalettePickerOnKpi"})
    public void testExportAndImportProjectWithInsight() {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
                true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                    .selectKpiDashboard(KPI_DASHBOARD).waitForWidgetsLoading();
            Screenshots.takeScreenshot(browser, "export import project apply color picker in "
                    + KPI_DASHBOARD, getClass());
            assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(
                    ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testExportAndImportProjectWithInsight"})
    public void testPartialExportAndImportWithInsight() {
        String insight = createInsightHasAttributeOnStackByAndViewBy(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        AnalysisPage analysisPage = initAnalysePage()
                .openInsight(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT).waitForReportComputing();
        setColorPickerMapping(analysisPage, 5);
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(
                ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            chartReport = initAnalysePage().openInsight(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT)
                    .waitForReportComputing().getChartReport();
            takeScreenshot(browser, "testPartialExportAndImportWithInsight", getClass());
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

    private void createInsightHasAttributeOnViewBy(String title, String metric,
                                                   String attribute, ReportType reportType) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private String createInsightHasAttributeOnStackByAndViewBy(String title, String metric,
                                                               String attribute, String stack) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }

    private void setCustomColorPickerFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }

    private void applyColorPaletteOnGrayPage() {
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));
    }

    public void deleteColorPaletteOnGrayPage() {
        indigoRestRequest.deleteColorsPalette();
    }

    /**
     * Opening the color configuration bucket, list color items measure are showed .
     * Select the first color at position (0) in list color items to match color column of chart
     * and then select the palette in position (indexColor)
     *
     * @param analysisPage the page apply color palette
     * @param indexColor   the palette in position (indexColor)
     * @return ConfigurationPanelBucket after applied color to a column of chart
     */
    private ConfigurationPanelBucket setColorPickerMapping(AnalysisPage analysisPage, Integer indexColor) {
        return analysisPage.getConfigurationPanelBucket().openColorConfiguration()
                .openColorPicker(0).getItemColorPicker(indexColor);
    }
}
